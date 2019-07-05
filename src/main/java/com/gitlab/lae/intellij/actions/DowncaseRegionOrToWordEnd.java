package com.gitlab.lae.intellij.actions;

import static com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_NEXT_WORD;

public final class DowncaseRegionOrToWordEnd extends TextAction {

    private static final Edit downcaseRegion =
            Edit.replacingSelection(String::toLowerCase);

    private static final Edit downcaseToWordEnd =
            Edit.replacingFromCaret(
                    ACTION_EDITOR_NEXT_WORD,
                    String::toLowerCase);

    public DowncaseRegionOrToWordEnd() {
        super(true, downcaseRegion.ifNoSelection(downcaseToWordEnd));
    }
}
