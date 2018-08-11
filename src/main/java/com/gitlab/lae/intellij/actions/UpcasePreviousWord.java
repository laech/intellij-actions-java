package com.gitlab.lae.intellij.actions;

import com.intellij.openapi.editor.actionSystem.EditorAction;

import static com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_PREVIOUS_WORD;

public final class UpcasePreviousWord extends EditorAction {

    public UpcasePreviousWord() {
        super(new TextHandler(ACTION_EDITOR_PREVIOUS_WORD, String::toUpperCase));
    }

}
