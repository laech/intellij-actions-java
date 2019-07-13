package com.gitlab.lae.intellij.actions;

public final class NoSpace extends TextAction {
    public NoSpace() {
        super(true, new NSpace(0));
    }
}
