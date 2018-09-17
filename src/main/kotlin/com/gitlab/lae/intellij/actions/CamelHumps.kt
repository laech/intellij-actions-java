package com.gitlab.lae.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR
import com.intellij.openapi.actionSystem.ToggleAction

class CamelHumpsInCurrentEditor : ToggleAction() {

    override fun isSelected(e: AnActionEvent) =
            e.getData(EDITOR)?.settings?.isCamelWords ?: false

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        e.getData(EDITOR)?.settings?.isCamelWords = state
    }

}
