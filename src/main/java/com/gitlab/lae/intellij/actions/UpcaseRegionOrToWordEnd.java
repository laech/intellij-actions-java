package com.gitlab.lae.intellij.actions;

import static com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_NEXT_WORD;

public final class UpcaseRegionOrToWordEnd extends TextAction {

    private static final Edit upcaseRegion =
            Edit.replacingSelection(String::toUpperCase);

    private static final Edit upcaseToWordEnd =
            Edit.replacingFromCaret(
                    ACTION_EDITOR_NEXT_WORD,
                    String::toUpperCase);

    public UpcaseRegionOrToWordEnd() {
        super(true, upcaseRegion.ifNoSelection(upcaseToWordEnd));
    }
}
