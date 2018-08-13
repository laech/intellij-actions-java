package com.gitlab.lae.intellij.actions

import com.intellij.openapi.editor.CaretState
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.fileTypes.FileTypes.PLAIN_TEXT
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class RectangularSelectionTest
    : LightPlatformCodeInsightFixtureTestCase() {

    fun test_createRectangularSelectionFromMultiLineSelection() {

        test("hello", 1, 2, listOf(
                CaretState(
                        LogicalPosition(0, 1),
                        LogicalPosition(0, 1, true),
                        LogicalPosition(0, 2)
                )
        ))

        // h[e]llo
        // w[o]rld
        test("""
            hello
            world
        """.trimIndent(), 1, 8, listOf(
                CaretState(
                        LogicalPosition(0, 1),
                        LogicalPosition(0, 1, true),
                        LogicalPosition(0, 2)
                ),
                CaretState(
                        LogicalPosition(1, 1),
                        LogicalPosition(1, 1, true),
                        LogicalPosition(1, 2)
                )
        ))

        // h[ell]o
        // a[a]
        // w[orl]d
        test("""
            hello
            aa
            world
        """.trimIndent(), 1, 13, listOf(
                CaretState(
                        LogicalPosition(0, 1),
                        LogicalPosition(0, 1, true),
                        LogicalPosition(0, 4)
                ),
                CaretState(
                        LogicalPosition(1, 1),
                        LogicalPosition(1, 1, true),
                        LogicalPosition(1, 2)
                ),
                CaretState(
                        LogicalPosition(2, 1),
                        LogicalPosition(2, 1, true),
                        LogicalPosition(2, 4)
                )
        ))

        // h[ell]o
        // a
        // w[orl]d
        test("""
            hello
            a
            world
        """.trimIndent(), 1, 12, listOf(
                CaretState(
                        LogicalPosition(0, 1),
                        LogicalPosition(0, 1, true),
                        LogicalPosition(0, 4)
                ),
                CaretState(
                        LogicalPosition(2, 1),
                        LogicalPosition(2, 1, true),
                        LogicalPosition(2, 4)
                )
        ))

        // h[ell]o
        // a
        // w[orl]d
        test("""
            hello
            a
            world
        """.trimIndent(), 4, 9, listOf(
                CaretState(
                        LogicalPosition(0, 4),
                        LogicalPosition(0, 1),
                        LogicalPosition(0, 4)
                ),
                CaretState(
                        LogicalPosition(2, 4),
                        LogicalPosition(2, 1),
                        LogicalPosition(2, 4)
                )
        ))
    }

    private fun test(
            text: String,
            selectionStart: Int,
            selectionEnd: Int,
            expectedSelections: List<CaretState>
    ) {
        myFixture.configureByText(PLAIN_TEXT, text)
        val caretModel = myFixture.editor.caretModel
        caretModel.moveToOffset(selectionStart)
        caretModel.primaryCaret.setSelection(selectionStart, selectionEnd)
        myFixture.performEditorAction("com.gitlab.lae.intellij.actions.CreateRectangularSelectionFromMultiLineSelection")
        assertEquals(
                positions(expectedSelections),
                positions(myFixture.editor.caretModel.caretsAndSelections))
    }

    private fun positions(caretStates: List<CaretState>) = caretStates.map {
        Triple(it.caretPosition, it.selectionStart, it.selectionEnd)
    }
}
