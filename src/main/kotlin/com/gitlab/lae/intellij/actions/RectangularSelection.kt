package com.gitlab.lae.intellij.actions

import com.intellij.openapi.editor.CaretState
import com.intellij.openapi.editor.LogicalPosition
import java.util.stream.Collectors.toList
import java.util.stream.IntStream

class CreateRectangularSelectionFromMultiLineSelection
    : TextAction(false, g@{ editor, caret, _ ->

    if (!caret.caretModel.supportsMultipleCarets()) {
        return@g
    }

    val start = caret.selectionStartPosition
    val end = caret.selectionEndPosition
    if (start.line == end.line) {
        return@g
    }

    fun hasEnoughColumns(line: Int): Boolean {
        val doc = editor.document
        val columns = doc.getLineEndOffset(line) - doc.getLineStartOffset(line)
        return start.column < columns || end.column < columns
    }

    fun toSelection(line: Int): CaretState {
        val selectionStart = LogicalPosition(line, start.column)
        val selectionEnd = LogicalPosition(line, end.column)
        return CaretState(selectionStart, selectionStart, selectionEnd)
    }

    caret.removeSelection()
    caret.caretModel.caretsAndSelections = IntStream
            .rangeClosed(start.line, end.line)
            .filter(::hasEnoughColumns)
            .mapToObj(::toSelection)
            .collect(toList())
})
