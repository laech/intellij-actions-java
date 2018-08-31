package com.gitlab.lae.intellij.actions

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

class RectangleCreate
    : TextComponentEditorAction(object : EditorActionHandler(false) {

    override fun doExecute(editor: Editor, caret: Caret?, ctx: DataContext?) {

        val caretModel = editor.caretModel
        if (!caretModel.supportsMultipleCarets()) {
            return
        }

        caretModel.caretsAndSelections = caretModel.caretsAndSelections
                .asSequence()
                .flatMap { it.toRectangle(editor.document) }
                .toList()
    }
})

private fun CaretState.toRectangle(doc: Document): Sequence<CaretState> {

    val selectionStart = selectionStart ?: return emptySequence()
    val selectionEnd = selectionEnd ?: return emptySequence()
    if (selectionEnd.line == selectionStart.line) {
        return sequenceOf(this)
    }

    fun hasEnoughColumns(line: Int): Boolean {
        val columns = doc.getLineEndOffset(line) - doc.getLineStartOffset(line)
        return selectionStart.column < columns || selectionEnd.column < columns
    }

    fun toSelection(line: Int): CaretState {
        val lineSelectionStart = LogicalPosition(line, selectionStart.column)
        val lineSelectionEnd = LogicalPosition(line, selectionEnd.column)
        return CaretState(lineSelectionStart, lineSelectionStart, lineSelectionEnd)
    }

    return IntRange(selectionStart.line, selectionEnd.line).asSequence()
            .filter(::hasEnoughColumns)
            .map(::toSelection)
}
