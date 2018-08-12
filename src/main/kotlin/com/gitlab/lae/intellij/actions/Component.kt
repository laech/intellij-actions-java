package com.gitlab.lae.intellij.actions

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.components.ApplicationComponent

class Component : ApplicationComponent {
    override fun initComponent() {
        val manager = ActionManager.getInstance()
        manager.register(UpcaseRegionOrWord())
        manager.register(DowncaseRegionOrWord())
        manager.register(CapitalizeRegionOrWord())
    }
}

private fun ActionManager.register(action: TextAction) {
    registerAction(action.id, action)
}
