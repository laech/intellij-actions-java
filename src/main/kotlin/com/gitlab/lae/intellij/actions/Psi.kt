package com.gitlab.lae.intellij.actions

import com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.EditorModificationUtil.deleteSelectedTextForAllCarets
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction
import com.intellij.psi.*
import com.intellij.psi.util.parentOfType

class PsiKill : TextComponentEditorAction(object : EditorWriteActionHandler(false) {
    override fun executeWriteAction(editor: Editor, caret: Caret?, ctx: DataContext) {
        selectElementsUnderCarets(editor, ctx)
        EditorCopyPasteHelper.getInstance().copySelectionToClipboard(editor)
        deleteSelectedTextForAllCarets(editor)
    }
})

private fun selectElementsUnderCarets(editor: Editor, ctx: DataContext) {
    val file = ctx.getData(PSI_FILE) ?: return
    editor.caretModel.caretsAndSelections =
            editor.caretModel.caretsAndSelections
                    .mapNotNull { it.caretPosition }
                    .map { select(editor, it, file) }
}

private fun select(editor: Editor, pos: LogicalPosition, file: PsiFile): CaretState {

    if (pos.isAtEndOfLine(editor)) {
        if (!pos.isAtLastLine(editor)) {
            return pos.selectToNextLineStart()
        }
        return pos.asCaret()
    }

    val doc = editor.document
    var element = file.findElementAt(pos.offset(editor))
            ?: return pos.selectToLineEnd(editor)

    while (element is PsiWhiteSpace) {
        element = element.nextSibling
    }

    while (element !is PsiStatement && element !is PsiModifierListOwner) {
        element = element.parent
        if (element is PsiFile) {
            return pos.selectToLineEnd(editor)
        }
    }

    return when (element) {
        is PsiParameter -> selectList(doc, pos, element, PsiParameterList::getParameters)
        is PsiTypeParameter -> selectList(doc, pos, element, PsiTypeParameterList::getTypeParameters)
        else -> pos.selectToElementEnd(editor, element)
    }
}

private inline fun <reified T : PsiElement> selectList(
        doc: Document,
        pos: LogicalPosition,
        element: PsiElement,
        params: (T) -> Array<out PsiElement>
): CaretState {

    val list = element.parentOfType<T>()
            ?: return pos.asCaret()

    val endOffset = params(list).lastOrNull()?.textRange?.endOffset
            ?: return pos.asCaret()

    val endLine = doc.getLineNumber(endOffset)
    val endLineColumn = endOffset - doc.getLineStartOffset(endLine)

    return CaretState(pos, pos, LogicalPosition(endLine, endLineColumn))
}

private fun LogicalPosition.isAtEndOfLine(editor: Editor) =
        editor.document.getLineEndOffset(line) -
                editor.document.getLineStartOffset(line) == column

private fun LogicalPosition.isAtLastLine(editor: Editor) =
        line + 1 >= editor.document.lineCount

private fun LogicalPosition.selectToLineEnd(editor: Editor) =
        CaretState(this, this, LogicalPosition(line,
                editor.document.getLineEndOffset(line)))

private fun LogicalPosition.selectToNextLineStart() =
        CaretState(this, this, LogicalPosition(line + 1, 0))

private fun LogicalPosition.selectToElementEnd(
        editor: Editor, element: PsiElement
): CaretState {
    val endOffset = element.textRange.endOffset
    val endLine = editor.document.getLineNumber(endOffset)
    val endColumn = endOffset - editor.document.getLineStartOffset(endLine)
    return CaretState(this, this, LogicalPosition(endLine, endColumn))
}

private fun LogicalPosition.asCaret() = CaretState(this, null, null)

private fun LogicalPosition.offset(editor: Editor) =
        editor.document.getLineStartOffset(line) + column
