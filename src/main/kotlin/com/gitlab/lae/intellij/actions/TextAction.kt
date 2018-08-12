package com.gitlab.lae.intellij.actions

import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction

internal abstract class TextAction(
        text: String,
        handler: EditorActionHandler
) : TextComponentEditorAction(handler) {

    init {
        templatePresentation.text = text
    }

    val id: String get() = javaClass.name

}
