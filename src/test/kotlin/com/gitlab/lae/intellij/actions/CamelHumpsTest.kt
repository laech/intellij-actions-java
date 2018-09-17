package com.gitlab.lae.intellij.actions

import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_NEXT_WORD
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_PREVIOUS_WORD
import com.intellij.openapi.fileTypes.FileTypes.PLAIN_TEXT
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class CamelHumpsTest : LightPlatformCodeInsightFixtureTestCase() {

    fun `test camelHumps words`() {
        myFixture.configureByText(PLAIN_TEXT, "HelloWorld")
        myFixture.editor.caretModel.moveToOffset(0)

        myFixture.performEditorAction(ACTION_EDITOR_NEXT_WORD)
        assertEquals(10, myFixture.caretOffset)

        myFixture.performEditorAction(ACTION_EDITOR_PREVIOUS_WORD)
        assertEquals(0, myFixture.caretOffset)

        myFixture.performEditorAction("com.gitlab.lae.intellij.actions.CamelHumpsInCurrentEditor")
        myFixture.performEditorAction(ACTION_EDITOR_NEXT_WORD)
        assertEquals(5, myFixture.caretOffset)
    }
}
