package com.gitlab.lae.intellij.actions;

import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import static com.intellij.openapi.fileTypes.FileTypes.PLAIN_TEXT;

public final class NoSpaceTest
        extends LightPlatformCodeInsightFixtureTestCase {

    public void testDeletesAllSpacesAndTabs() {
        testDelete("HelloWorld", "HelloWorld", 0);
        testDelete("HelloWorld", "HelloWorld", 1);
        testDelete("HelloWorld", "HelloWorld", 10);
        testDelete(" HelloWorld", "HelloWorld", 0);
        testDelete("  HelloWorld", "HelloWorld", 0);
        testDelete("  HelloWorld", "HelloWorld", 1);
        testDelete("\tHelloWorld", "HelloWorld", 0);
        testDelete("\t\tHelloWorld", "HelloWorld", 0);
        testDelete(" \t \t  HelloWorld", "HelloWorld", 0);
        testDelete("Hello\t  \n World", "Hello\n World", 7);
        testDelete("Hello\t  \n World", "Hello\t  \nWorld", 9);
    }

    private void testDelete(String input, String expected, int caretOffset) {
        myFixture.configureByText(PLAIN_TEXT, input);
        myFixture.getEditor().getCaretModel().moveToOffset(caretOffset);
        myFixture.performEditorAction("com.gitlab.lae.intellij.actions.NoSpace");
        assertEquals(expected, myFixture.getEditor().getDocument().getText());
    }
}
