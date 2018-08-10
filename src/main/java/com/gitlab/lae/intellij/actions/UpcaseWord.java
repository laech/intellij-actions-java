package com.gitlab.lae.intellij.actions;

import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;

public final class UpcaseWord extends EditorAction {

    public UpcaseWord() {
        super(new UpcaseWordHandler());
    }

    private static final class UpcaseWordHandler extends EditorActionHandler {

        UpcaseWordHandler() {
            super(true);
        }

    }
}
