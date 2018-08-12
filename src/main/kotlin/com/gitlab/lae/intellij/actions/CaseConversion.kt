package com.gitlab.lae.intellij.actions

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.TextRange

internal class UpcaseRegionOrWord : TextAction(
        "Upcase Region or Word at Caret",
        Handler(String::toUpperCase))

internal class DowncaseRegionOrWord : TextAction(
        "Downcase Region or Word at Caret",
        Handler(String::toLowerCase))

internal class CapitalizeRegionOrWord : TextAction(
        "Capitalize Region or Word at Caret",
        Handler { it.toLowerCase().capitalize() })

private class Handler(val f: (String) -> String) : EditorActionHandler(true) {

    override fun doExecute(editor: Editor, caret: Caret?, ctx: DataContext) {
        caret ?: return
        if (!caret.hasSelection()) {
            caret.selectWordAtCaret(false)
        }
        replace(editor, caret.selectionStart, caret.selectionEnd)
    }

    private fun replace(editor: Editor, start: Int, end: Int) {
        val doc = editor.document
        val replacement = f(doc.getText(TextRange(start, end)))
        runWriteCommandAction(editor.project) {
            doc.replaceString(start, end, replacement)
        }
    }
}