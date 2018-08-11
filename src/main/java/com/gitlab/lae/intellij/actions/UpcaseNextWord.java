package com.gitlab.lae.intellij.actions;

import com.intellij.openapi.editor.actionSystem.EditorAction;

import static com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_NEXT_WORD;

public final class UpcaseNextWord extends EditorAction {

    public UpcaseNextWord() {
        super(new TextHandler(ACTION_EDITOR_NEXT_WORD, String::toUpperCase));
    }

}
