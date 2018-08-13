package com.gitlab.lae.intellij.actions

import com.intellij.openapi.fileTypes.FileTypes.PLAIN_TEXT
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class CaseConversionTest : LightPlatformCodeInsightFixtureTestCase() {

    private val upcaseRegionOrToWordEnd = "com.gitlab.lae.intellij.actions.UpcaseRegionOrToWordEnd"
    private val downcaseRegionOrToWordEnd = "com.gitlab.lae.intellij.actions.DowncaseRegionOrToWordEnd"
    private val capitalizeRegionOrToWordEnd = "com.gitlab.lae.intellij.actions.CapitalizeRegionOrToWordEnd"

    fun test_upcaseRegionOrToWordEnd_toWordEnd() {
        test("HELLO WORLD", "HELLO WORLD", 0, upcaseRegionOrToWordEnd)
        test("HELLO WORLD", "HELLO WORLD", 0, upcaseRegionOrToWordEnd, 2)
        test("hello world", "HELLO world", 0, upcaseRegionOrToWordEnd)
        test("hello world", "HELLO WORLD", 0, upcaseRegionOrToWordEnd, 2)
        test("hello world", "hELLO world", 1, upcaseRegionOrToWordEnd)
        test("hello world", "hELLO WORLD", 1, upcaseRegionOrToWordEnd, 2)
        test("hello world", "hello worlD", 10, upcaseRegionOrToWordEnd)
        test("hello world", "hello world", 11, upcaseRegionOrToWordEnd)
        test("hello world", "hello world", 11, upcaseRegionOrToWordEnd, 2)
        test("hello world", "hello WORLD", 5, upcaseRegionOrToWordEnd)
        test("hello world", "hello WORLD", 6, upcaseRegionOrToWordEnd)
        test("hello ;; world", "hello ;; WORLD", 5, upcaseRegionOrToWordEnd)
        test("hello ;; world", "hello ;; WORLD", 6, upcaseRegionOrToWordEnd)
        test("hello\nworld", "HELLO\nworld", 0, upcaseRegionOrToWordEnd)
        test("hello\nworld", "HELLO\nWORLD", 0, upcaseRegionOrToWordEnd, 2)
        test("hello-world", "HELLO-world", 0, upcaseRegionOrToWordEnd)
        test("hello-world", "HELLO-WORLD", 0, upcaseRegionOrToWordEnd, 2)
    }

    fun test_upcaseRegionOrToWordEnd_region() {
        test("HELLO WORLD", "HELLO WORLD", 0, 11, upcaseRegionOrToWordEnd)
        test("HELLO WORLD", "HELLO WORLD", 0, 11, upcaseRegionOrToWordEnd, 2)
        test("hello world", "HELLO world", 0, 5, upcaseRegionOrToWordEnd)
        test("hello world", "hELLO world", 1, 5, upcaseRegionOrToWordEnd)
        test("hello world", "hELLO WORLD", 1, 11, upcaseRegionOrToWordEnd)
        test("hello world", "hello worlD", 10, 11, upcaseRegionOrToWordEnd)
        test("hello world", "hello WORLD", 5, 11, upcaseRegionOrToWordEnd)
        test("hello world", "hello WORLD", 6, 11, upcaseRegionOrToWordEnd)
        test("hello ;; \nworld", "hello ;; \nWORLD", 5, 15, upcaseRegionOrToWordEnd)
        test("hello ;; world", "hello ;; WORLD", 6, 14, upcaseRegionOrToWordEnd)
    }

    fun test_downcaseRegionOrToWordEnd_toWordEnd() {
        test("HELLO WORLD", "hello WORLD", 0, downcaseRegionOrToWordEnd)
        test("HELLO WORLD", "HEllo WORLD", 2, downcaseRegionOrToWordEnd)
        test("HELLO WORLD", "hello world", 0, downcaseRegionOrToWordEnd, 2)
    }

    fun test_downcaseRegionOrToWordEnd_region() {
        test("HELLO WORLD", "hello wORLD", 0, 7, downcaseRegionOrToWordEnd)
    }

    fun test_capitalizeRegionOrToWordEnd_toWordEnd() {
        test("Hello", "Hello", 0, capitalizeRegionOrToWordEnd)
        test("HELLO WORLD", "Hello WORLD", 0, capitalizeRegionOrToWordEnd)
        test("HELLO WORLD", "Hello World", 0, capitalizeRegionOrToWordEnd, 2)
        test("hello-world", "hEllo-world", 1, capitalizeRegionOrToWordEnd)
        test("hello-world", "Hello-World", 0, capitalizeRegionOrToWordEnd, 2)
    }

    fun test_capitalizeRegionOrToWordEnd_region() {
        test("HELLO WORLD", "Hello WORLD", 0, 5, capitalizeRegionOrToWordEnd)
        test("HELLO WORLD", "HELlo WorLD", 2, 9, capitalizeRegionOrToWordEnd)
        test("hello-WORLD", "heLlo-WorLD", 2, 9, capitalizeRegionOrToWordEnd)
    }

    private fun test(
            initialText: String,
            expectedText: String,
            caretOffset: Int,
            actionId: String,
            times: Int = 1
    ) {
        test(
                initialText,
                expectedText,
                caretOffset,
                caretOffset,
                actionId,
                times
        )
    }

    private fun test(
            initialText: String,
            expectedText: String,
            selectionStart: Int,
            selectionEnd: Int,
            actionId: String,
            times: Int = 1
    ) {
        myFixture.configureByText(PLAIN_TEXT, initialText)
        myFixture.editor.caretModel.run {
            moveToOffset(selectionStart)
            primaryCaret.setSelection(selectionStart, selectionEnd)
        }
        repeat(times) { myFixture.performEditorAction(actionId) }
        assertEquals(expectedText, myFixture.editor.document.text)
    }
}
