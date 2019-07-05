package com.gitlab.lae.intellij.actions;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import static com.intellij.openapi.fileTypes.FileTypes.PLAIN_TEXT;

public final class CaseConversionTest
        extends LightPlatformCodeInsightFixtureTestCase {

    private static final String upcaseRegionOrToWordEnd = "com.gitlab.lae.intellij.actions.UpcaseRegionOrToWordEnd";
    private static final String downcaseRegionOrToWordEnd = "com.gitlab.lae.intellij.actions.DowncaseRegionOrToWordEnd";
    private static final String capitalizeRegionOrToWordEnd = "com.gitlab.lae.intellij.actions.CapitalizeRegionOrToWordEnd";

    public void test_upcaseRegionOrToWordEnd_toWordEnd() {
        test("HELLO WORLD", "HELLO WORLD", 0, upcaseRegionOrToWordEnd);
        test("HELLO WORLD", "HELLO WORLD", 0, upcaseRegionOrToWordEnd, 2);
        test("hello world", "HELLO world", 0, upcaseRegionOrToWordEnd);
        test("hello world", "HELLO WORLD", 0, upcaseRegionOrToWordEnd, 2);
        test("hello world", "hELLO world", 1, upcaseRegionOrToWordEnd);
        test("hello world", "hELLO WORLD", 1, upcaseRegionOrToWordEnd, 2);
        test("hello world", "hello worlD", 10, upcaseRegionOrToWordEnd);
        test("hello world", "hello world", 11, upcaseRegionOrToWordEnd);
        test("hello world", "hello world", 11, upcaseRegionOrToWordEnd, 2);
        test("hello world", "hello WORLD", 5, upcaseRegionOrToWordEnd);
        test("hello world", "hello WORLD", 6, upcaseRegionOrToWordEnd);
        test("hello ;; world", "hello ;; WORLD", 5, upcaseRegionOrToWordEnd);
        test("hello ;; world", "hello ;; WORLD", 6, upcaseRegionOrToWordEnd);
        test("hello\nworld", "HELLO\nworld", 0, upcaseRegionOrToWordEnd);
        test("hello\nworld", "HELLO\nWORLD", 0, upcaseRegionOrToWordEnd, 2);
        test("hello-world", "HELLO-world", 0, upcaseRegionOrToWordEnd);
        test("hello-world", "HELLO-WORLD", 0, upcaseRegionOrToWordEnd, 2);
    }

    public void test_upcaseRegionOrToWordEnd_region() {
        test("HELLO WORLD", "HELLO WORLD", 0, 11, upcaseRegionOrToWordEnd, 1);
        test("HELLO WORLD", "HELLO WORLD", 0, 11, upcaseRegionOrToWordEnd, 2);
        test("hello world", "HELLO world", 0, 5, upcaseRegionOrToWordEnd, 1);
        test("hello world", "hELLO world", 1, 5, upcaseRegionOrToWordEnd, 1);
        test("hello world", "hELLO WORLD", 1, 11, upcaseRegionOrToWordEnd, 1);
        test("hello world", "hello worlD", 10, 11, upcaseRegionOrToWordEnd, 1);
        test("hello world", "hello WORLD", 5, 11, upcaseRegionOrToWordEnd, 1);
        test("hello world", "hello WORLD", 6, 11, upcaseRegionOrToWordEnd, 1);
        test("hello ;; \nworld", "hello ;; \nWORLD", 5, 15, upcaseRegionOrToWordEnd, 1);
        test("hello ;; world", "hello ;; WORLD", 6, 14, upcaseRegionOrToWordEnd, 1);
    }

    public void test_downcaseRegionOrToWordEnd_toWordEnd() {
        test("HELLO WORLD", "hello WORLD", 0, downcaseRegionOrToWordEnd);
        test("HELLO WORLD", "HEllo WORLD", 2, downcaseRegionOrToWordEnd);
        test("HELLO WORLD", "hello world", 0, downcaseRegionOrToWordEnd, 2);
    }

    public void test_downcaseRegionOrToWordEnd_region() {
        test("HELLO WORLD", "hello wORLD", 0, 7, downcaseRegionOrToWordEnd, 1);
    }

    public void test_capitalizeRegionOrToWordEnd_toWordEnd() {
        test("Hello", "Hello", 0, capitalizeRegionOrToWordEnd);
        test("HELLO WORLD", "Hello WORLD", 0, capitalizeRegionOrToWordEnd);
        test("HELLO WORLD", "Hello World", 0, capitalizeRegionOrToWordEnd, 2);
        test("hello-world", "hEllo-world", 1, capitalizeRegionOrToWordEnd);
        test("hello-world", "Hello-World", 0, capitalizeRegionOrToWordEnd, 2);
    }

    public void test_capitalizeRegionOrToWordEnd_region() {
        test("HELLO WORLD", "Hello WORLD", 0, 5, capitalizeRegionOrToWordEnd, 1);
        test("HELLO WORLD", "HELlo WorLD", 2, 9, capitalizeRegionOrToWordEnd, 1);
        test("hello-WORLD", "heLlo-WorLD", 2, 9, capitalizeRegionOrToWordEnd, 1);
    }

    private void test(
            String initialText,
            String expectedTest,
            int caretOffset,
            String actionId
    ) {
        test(initialText, expectedTest, caretOffset, actionId, 1);
    }

    private void test(
            String initialText,
            String expectedText,
            int caretOffset,
            String actionId,
            int times
    ) {
        test(
                initialText,
                expectedText,
                caretOffset,
                caretOffset,
                actionId,
                times
        );
    }

    private void test(
            String initialText,
            String expectedText,
            int selectionStart,
            int selectionEnd,
            String actionId,
            int times
    ) {
        myFixture.configureByText(PLAIN_TEXT, initialText);
        CaretModel caretModel = myFixture.getEditor().getCaretModel();
        caretModel.moveToOffset(selectionStart);
        caretModel.getPrimaryCaret().setSelection(selectionStart, selectionEnd);
        for (int i = 0; i < times; i++) {
            myFixture.performEditorAction(actionId);
        }
        assertEquals(expectedText, myFixture.getEditor().getDocument().getText());
    }
}
