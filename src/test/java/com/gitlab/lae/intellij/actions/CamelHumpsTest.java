package com.gitlab.lae.intellij.actions;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import static com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_NEXT_WORD;
import static com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_PREVIOUS_WORD;
import static com.intellij.openapi.fileTypes.FileTypes.PLAIN_TEXT;

public final class CamelHumpsTest
        extends LightPlatformCodeInsightFixtureTestCase {

    public void testCamelHumpsWords() {
        myFixture.configureByText(PLAIN_TEXT, "HelloWorld");
        myFixture.getEditor().getCaretModel().moveToOffset(0);

        myFixture.performEditorAction(ACTION_EDITOR_NEXT_WORD);
        assertEquals(10, myFixture.getCaretOffset());

        myFixture.performEditorAction(ACTION_EDITOR_PREVIOUS_WORD);
        assertEquals(0, myFixture.getCaretOffset());

        myFixture.performEditorAction("com.gitlab.lae.intellij.actions.CamelHumpsInCurrentEditor");
        myFixture.performEditorAction(ACTION_EDITOR_NEXT_WORD);
        assertEquals(5, myFixture.getCaretOffset());
    }
}
