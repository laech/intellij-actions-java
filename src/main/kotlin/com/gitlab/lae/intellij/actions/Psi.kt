package com.gitlab.lae.intellij.actions

import com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.EditorModificationUtil.deleteSelectedTextForAllCarets
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction
import com.intellij.psi.*
import com.intellij.psi.util.parentOfType
import java.lang.Character.isSpaceChar

class PsiKill : TextComponentEditorAction(object : EditorWriteActionHandler(false) {
    override fun executeWriteAction(editor: Editor, caret: Caret?, ctx: DataContext) {
        selectElementsUnderCarets(editor, ctx)
        EditorCopyPasteHelper.getInstance().copySelectionToClipboard(editor)
        deleteSelectedTextForAllCarets(editor)
    }
})

private fun selectElementsUnderCarets(editor: Editor, ctx: DataContext) {
    val file = ctx.getData(PSI_FILE) ?: return
    editor.caretModel.allCarets.forEach { select(editor, it, file) }
}

private fun select(editor: Editor, caret: Caret, file: PsiFile) {

    val pos = caret.logicalPosition
    if (pos.isAtEndOfLine(editor)) {
        if (!pos.isAtLastLine(editor)) {
            selectToNextLineStart(editor, caret)
            return
        }
        return
    }

    val doc = editor.document
    val chars = doc.immutableCharSequence
    val offset = IntRange(caret.offset, doc.textLength)
            .find { !isSpaceChar(chars[it]) }
            ?: return

    var element = file.findElementAt(offset)
    if (element == null) {
        selectToLineEnd(editor, caret)
        return
    }

    while (element is PsiWhiteSpace) {
        element = element.nextSibling
    }

    while (element !is PsiStatement
            && element !is PsiModifierListOwner
            && element !is PsiComment) {

        if (element == null) {
            selectToLineEnd(editor, caret)
            return
        }
        element = element.parent
        if (element is PsiFile) {
            selectToLineEnd(editor, caret)
            return
        }
    }

    when (element) {
        is PsiParameter -> selectList(editor, caret, element, PsiParameterList::getParameters)
        is PsiTypeParameter -> selectList(editor, caret, element, PsiTypeParameterList::getTypeParameters)
        else -> selectElement(editor, caret, element)
    }
}

private fun selectElement(editor: Editor, caret: Caret, element: PsiElement) {
    val doc = editor.document
    val endOffset = element.textRange.endOffset
    val endLine = doc.getLineNumber(endOffset)
    val endColumn = endOffset - doc.getLineStartOffset(endLine)
    val logicalEndPosition = LogicalPosition(endLine, endColumn)
    val visualEndPosition = editor.logicalToVisualPosition(logicalEndPosition)
    caret.setSelection(caret.visualPosition, caret.offset, visualEndPosition, endOffset)
}

private inline fun <reified T : PsiElement> selectList(
        editor: Editor,
        caret: Caret,
        element: PsiElement,
        params: (T) -> Array<out PsiElement>
) {
    val doc = editor.document
    val list = element.parentOfType<T>() ?: return
    val endOffset = params(list).lastOrNull()?.textRange?.endOffset ?: return
    val endLine = doc.getLineNumber(endOffset)
    val endLineColumn = endOffset - doc.getLineStartOffset(endLine)
    val logicalEndPosition = LogicalPosition(endLine, endLineColumn)
    val visualEndPosition = editor.logicalToVisualPosition(logicalEndPosition)
    caret.setSelection(caret.visualPosition, caret.offset, visualEndPosition, endOffset)
}

private fun LogicalPosition.isAtEndOfLine(editor: Editor) =
        editor.document.getLineEndOffset(line) -
                editor.document.getLineStartOffset(line) == column

private fun LogicalPosition.isAtLastLine(editor: Editor) =
        line + 1 >= editor.document.lineCount

private fun selectToLineEnd(editor: Editor, caret: Caret) {
    val visualStartPosition = caret.visualPosition
    val logicalEndOffset = editor.document.getLineEndOffset(caret.logicalPosition.line)
    val logicalStartPosition = editor.visualToLogicalPosition(visualStartPosition)
    val logicalStartLineOffset = editor.document.getLineStartOffset(logicalStartPosition.line)
    val logicalEndPosition = LogicalPosition(logicalStartPosition.line, logicalEndOffset - logicalStartLineOffset)
    val visualEndPosition = editor.logicalToVisualPosition(logicalEndPosition)
    caret.setSelection(visualStartPosition, caret.offset, visualEndPosition, logicalEndOffset)
}

private fun selectToNextLineStart(editor: Editor, caret: Caret) {
    val visualStartPosition = caret.visualPosition
    val visualEndPosition = VisualPosition(visualStartPosition.line + 1, 0)
    val logicalEndPosition = editor.visualToLogicalPosition(visualEndPosition)
    val logicalEndOffset = editor.logicalPositionToOffset(logicalEndPosition)
    caret.setSelection(visualStartPosition, caret.offset, visualEndPosition, logicalEndOffset)
}
