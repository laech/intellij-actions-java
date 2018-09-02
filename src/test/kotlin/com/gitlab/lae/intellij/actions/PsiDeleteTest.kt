package com.gitlab.lae.intellij.actions

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.editor.CaretState
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class PsiDeleteTest : LightPlatformCodeInsightFixtureTestCase() {

    fun `test delete complete statement`() {
        test("""
            class Main {
              public static void main(String[] args) {
                System
                    .out
                    .println();
                System.exit(0);
              }
            }
        """.trimIndent(), """
            class Main {
              public static void main(String[] args) {

                System.exit(0);
              }
            }
        """.trimIndent(), LogicalPosition(2, 0))
    }

    fun `test delete partial statement`() {
        test("""
            class Main {
              public static void main(String[] args) {
                System
                    .out
                    .println();
                System.exit(0);
              }
            }
        """.trimIndent(), """
            class Main {
              public static void main(String[] args) {
                Sys
                System.exit(0);
              }
            }
        """.trimIndent(), LogicalPosition(2, 7))
    }

    private fun test(
            initialText: String,
            expectedText: String,
            initialCaretPosition: LogicalPosition
    ) {
        myFixture.configureByText(JavaFileType.INSTANCE, initialText)
        myFixture.editor.caretModel.caretsAndSelections = listOf(CaretState(
                initialCaretPosition, initialCaretPosition, initialCaretPosition))
        myFixture.performEditorAction("com.gitlab.lae.intellij.actions.PsiDelete")
        assertEquals(expectedText, myFixture.editor.document.text)
    }
}