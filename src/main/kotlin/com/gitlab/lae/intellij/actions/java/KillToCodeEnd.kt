package com.gitlab.lae.intellij.actions.java

import com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.EditorModificationUtil.deleteSelectedTextForAllCarets
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction
import com.intellij.psi.*
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.javadoc.PsiDocTag
import java.lang.Character.isSpaceChar

class KillToCodeEnd :
  TextComponentEditorAction(object : EditorWriteActionHandler(false) {
    override fun executeWriteAction(
      editor: Editor,
      caret: Caret?,
      context: DataContext
    ) {
      selectElementsUnderCarets(editor, context)
      EditorCopyPasteHelper.getInstance().copySelectionToClipboard(editor)
      deleteSelectedTextForAllCarets(editor)
    }
  })

private val getters = EndOffsetGetter.values()

private fun selectElementsUnderCarets(editor: Editor, context: DataContext) {
  val file = context.getData(PSI_FILE) ?: return
  editor.caretModel.allCarets.forEach { select(editor, it, file) }
}

private fun select(editor: Editor, caret: Caret, file: PsiFile) {
  val pos = caret.logicalPosition
  if (isAtEndOfLine(pos, editor)) {
    if (!isAtLastLine(pos, editor)) {
      selectToNextLineStart(editor, caret)
      return
    }
    return
  }

  val doc = editor.document
  val chars = doc.immutableCharSequence
  val offset = (caret.offset..doc.textLength)
    .firstOrNull { !isSpaceChar(chars[it]) }
    ?: return

  var element = file.findElementAt(offset)
  if (element == null) {
    selectToLineEnd(editor, caret)
    return
  }

  while (element is PsiWhiteSpace) {
    element = element.getNextSibling()
  }

  while (true) {
    if (element == null || element is PsiFile) {
      selectToLineEnd(editor, caret)
      return
    }

    val getter = getter(element, caret)
    if (getter == null) {
      element = element.parent
      continue
    }

    val endOffset = getter.endOffset(element, caret)
    val endLine = doc.getLineNumber(endOffset)
    val endColumn = endOffset - doc.getLineStartOffset(endLine)
    caret.setSelection(
      caret.visualPosition,
      caret.offset,
      editor.logicalToVisualPosition(LogicalPosition(endLine, endColumn)),
      endOffset
    )
    return
  }
}

private fun getter(element: PsiElement, caret: Caret): EndOffsetGetter? =
  getters.firstOrNull { getter -> getter.recognizes(element, caret) }

private fun getListOrNextElementOffset(
  parent: PsiElement,
  element: PsiElement,
  getChildren: () -> Array<out PsiElement>
): Int? {
  val elementRange = element.textRange
  val parentTextRange = parent.textRange
  return if (elementRange.startOffset == parentTextRange.startOffset) {
    parentTextRange.endOffset
  } else {
    getNextElementOffset(getChildren(), element)
  }
}

private fun getNextElementOffset(
  elements: Array<out PsiElement>,
  element: PsiElement
): Int? {
  val index = elements.indexOf(element)
  if (index >= 0 && index < elements.size - 1) {
    return elements[index + 1].textRange.startOffset
  }
  val elementEndOffset = element.textRange.endOffset
  return elements
    .asSequence()
    .map { it.textRange }
    .filter { it.startOffset >= elementEndOffset }
    .map { it.endOffset }
    .firstOrNull()
}

private fun isAtEndOfLine(pos: LogicalPosition, editor: Editor) =
  editor.document.getLineEndOffset(pos.line) -
    editor.document.getLineStartOffset(pos.line) == pos.column

private fun isAtLastLine(pos: LogicalPosition, editor: Editor) =
  pos.line + 1 >= editor.document.lineCount

private fun selectToLineEnd(editor: Editor, caret: Caret) {
  val visualStartPosition = caret.visualPosition
  val logicalEndOffset =
    editor.document.getLineEndOffset(caret.logicalPosition.line)

  val logicalStartPosition =
    editor.visualToLogicalPosition(visualStartPosition)

  val logicalStartLineOffset =
    editor.document.getLineStartOffset(logicalStartPosition.line)

  val logicalEndPosition = LogicalPosition(
    logicalStartPosition.line,
    logicalEndOffset - logicalStartLineOffset
  )

  caret.setSelection(
    visualStartPosition,
    caret.offset,
    editor.logicalToVisualPosition(logicalEndPosition),
    logicalEndOffset
  )
}

private fun selectToNextLineStart(editor: Editor, caret: Caret) {
  val visualStartPosition = caret.visualPosition
  val visualEndPosition = VisualPosition(visualStartPosition.line + 1, 0)
  val logicalEndPosition = editor.visualToLogicalPosition(visualEndPosition)
  val logicalEndOffset = editor.logicalPositionToOffset(logicalEndPosition)
  caret.setSelection(
    visualStartPosition,
    caret.offset,
    visualEndPosition,
    logicalEndOffset
  )
}

private enum class EndOffsetGetter {

  ENCLOSURE_PARENT {

    override fun recognizes(element: PsiElement, caret: Caret) =
      (element is PsiParenthesizedExpression
        || element is PsiArrayInitializerExpression
        || element is PsiParameterList
        || element is PsiCodeBlock
        || isCharLiteral(element)
        || isStringLiteral(element)
        || isCompleteCodeBlock(element)
        || isCaretBetweenClassBraces(caret, element))

    override fun endOffset(element: PsiElement, caret: Caret) =
      element.textRange.endOffset -
        if (caret.offset > element.textRange.startOffset) 1 else 0

    private fun isStringLiteral(element: PsiElement) =
      element is PsiLiteralValue &&
        (element as PsiLiteralValue).value is String

    private fun isCharLiteral(element: PsiElement) =
      element is PsiLiteralValue &&
        (element as PsiLiteralValue).value is Char

    private fun isCompleteCodeBlock(element: PsiElement) =
      element is PsiCodeBlock &&
        element.lBrace != null &&
        element.rBrace != null

    private fun isCompleteClass(element: PsiElement) =
      element is PsiClass &&
        element.lBrace != null &&
        element.rBrace != null

    private fun isCaretBetweenClassBraces(caret: Caret, element: PsiElement) =
      isCompleteClass(element) &&
        (element as PsiClass).lBrace!!.textRange.endOffset <= caret.offset
  },

  POLYADIC_CHILD {

    override fun recognizes(element: PsiElement, caret: Caret) =
      element.parent is PsiPolyadicExpression

    override fun endOffset(element: PsiElement, caret: Caret): Int {
      val parent = element.parent as PsiPolyadicExpression
      return getNextElementOffset(parent.operands, element)
        ?: element.textRange.endOffset
    }
  },

  ARRAY_INITIALIZER_CHILD {

    override fun recognizes(element: PsiElement, caret: Caret) =
      element.parent is PsiArrayInitializerExpression

    override fun endOffset(element: PsiElement, caret: Caret): Int {
      val parent = element.parent as PsiArrayInitializerExpression
      return getListOrNextElementOffset(parent, element) { parent.initializers }
        ?: parent.textRange.endOffset - 1
    }
  },

  ENCLOSURE_CHILD {

    override fun recognizes(element: PsiElement, caret: Caret) =
      getChildren(element.parent) != null

    override fun endOffset(element: PsiElement, caret: Caret): Int {
      val parent = element.parent
      val children = getChildren(parent) ?: return element.textRange.endOffset
      return getListOrNextElementOffset(parent, element) { children }
        ?: parent.textRange.endOffset - 1
    }

    private fun getChildren(parent: PsiElement): Array<out PsiElement>? =
      when (parent) {
        is PsiParameterList -> parent.parameters
        is PsiExpressionList -> parent.expressions
        is PsiTypeParameterList -> parent.typeParameters
        else -> null
      }
  },

  GENERAL {

    override fun recognizes(element: PsiElement, caret: Caret) =
      (element is PsiStatement
        || element is PsiModifierListOwner
        || element is PsiDocTag
        || (element is PsiComment && element !is PsiDocComment)
        || element is PsiLiteralExpression
        || element is PsiNameValuePair
        || element is PsiUnaryExpression)

    override fun endOffset(element: PsiElement, caret: Caret) =
      element.textRange.endOffset
  };

  abstract fun recognizes(element: PsiElement, caret: Caret): Boolean
  abstract fun endOffset(element: PsiElement, caret: Caret): Int
}
