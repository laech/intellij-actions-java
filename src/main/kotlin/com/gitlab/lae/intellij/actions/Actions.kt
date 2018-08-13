package com.gitlab.lae.intellij.actions

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

open class TextAction(write: Boolean, f: Edit) : TextComponentEditorAction(
        if (write) f.toWriteHandler()
        else f.toHandler())

typealias Edit = (Editor, Caret, DataContext?) -> Unit

private fun Edit.toHandler() = object : EditorActionHandler(true) {
    override fun doExecute(editor: Editor, caret: Caret?, ctx: DataContext?) {
        caret ?: return
        invoke(editor, caret, ctx)
    }
}

private fun Edit.toWriteHandler() = object : EditorWriteActionHandler(true) {
    override fun executeWriteAction(editor: Editor, caret: Caret?, ctx: DataContext?) {
        caret ?: return
        invoke(editor, caret, ctx)
    }
}
