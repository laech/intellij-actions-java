package com.gitlab.lae.intellij.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.editor.Editor;

import static com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR;

public final class CamelHumpsInCurrentEditor extends ToggleAction {

    @Override
    public boolean isSelected(AnActionEvent e) {
        Editor editor = e.getData(EDITOR);
        return editor != null &&
                editor.getSettings().isCamelWords();
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        Editor editor = e.getData(EDITOR);
        if (editor != null) {
            editor.getSettings().setCamelWords(true);
        }
    }

}
