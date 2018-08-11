package com.gitlab.lae.intellij.actions

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction
import com.intellij.openapi.util.TextRange

internal class UpcaseRegionOrWord : RegionOrWordAction(
        "Upcase Region or Word at Caret", String::toUpperCase)

internal class DowncaseRegionOrWord : RegionOrWordAction(
        "Downcase Region or Word at Caret", String::toLowerCase)

internal class CapitalizeRegionOrWord : RegionOrWordAction(
        "Capitalize Region or Word at Caret", { it.toLowerCase().capitalize() })

internal open class RegionOrWordAction(
        text: String,
        conversionFn: (String) -> String
) : TextComponentEditorAction(Handler(conversionFn)) {
    init {
        templatePresentation.text = text
    }
}

private class Handler(
        val conversionFn: (String) -> String
) : EditorActionHandler(true) {

    override fun doExecute(editor: Editor, caret: Caret?, ctx: DataContext) {
        caret ?: return
        if (!caret.hasSelection()) {
            caret.selectWordAtCaret(false)
        }
        replace(editor, caret.selectionStart, caret.selectionEnd)
    }

    private fun replace(editor: Editor, start: Int, end: Int) {
        val doc = editor.document
        val replacement = conversionFn(doc.getText(TextRange(start, end)))
        runWriteCommandAction(editor.project) {
            doc.replaceString(start, end, replacement)
        }
    }
}