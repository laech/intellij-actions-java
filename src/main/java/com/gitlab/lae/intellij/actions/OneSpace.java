package com.gitlab.lae.intellij.actions;

public final class OneSpace extends TextAction {
    public OneSpace() {
        super(true, new NSpace(1));
    }
}
