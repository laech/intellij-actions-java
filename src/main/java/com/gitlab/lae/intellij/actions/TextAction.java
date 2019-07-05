package com.gitlab.lae.intellij.actions;

import com.intellij.openapi.editor.actions.TextComponentEditorAction;

class TextAction extends TextComponentEditorAction {

    TextAction(boolean write, Edit edit) {
        super(write ? edit.toWriteHandler() : edit.toHandler());
    }

}
