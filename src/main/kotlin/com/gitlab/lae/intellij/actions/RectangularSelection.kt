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

    val startOffset = caret.selectionStart
    val endOffset = caret.selectionEnd

    val doc = editor.document
    val startLine = doc.getLineNumber(startOffset)
    val endLine = doc.getLineNumber(endOffset)
    if (startLine == endLine) {
        return@g
    }

    val startColumn = startOffset - doc.getLineStartOffset(startLine)
    val endColumn = endOffset - doc.getLineStartOffset(endLine)

    fun hasEnoughColumns(line: Int): Boolean {
        val columns = doc.getLineEndOffset(line) - doc.getLineStartOffset(line)
        return startColumn < columns || endColumn < columns
    }

    fun toSelection(line: Int): CaretState {
        val selectionStart = LogicalPosition(line, startColumn)
        val selectionEnd = LogicalPosition(line, endColumn)
        return CaretState(selectionStart, selectionStart, selectionEnd)
    }

    caret.removeSelection()
    caret.caretModel.caretsAndSelections = IntStream
            .rangeClosed(startLine, endLine)
            .filter(::hasEnoughColumns)
            .mapToObj(::toSelection)
            .collect(toList())
})
