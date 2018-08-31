package com.gitlab.lae.intellij.actions

import com.intellij.openapi.editor.CaretState
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.fileTypes.FileTypes.PLAIN_TEXT
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class RectangleCreateTest
    : LightPlatformCodeInsightFixtureTestCase() {

    fun `test single line selection remains unchanged`() {
        test("hello", listOf(
                CaretState(
                        LogicalPosition(0, 1),
                        LogicalPosition(0, 1),
                        LogicalPosition(0, 2)
                )
        ), listOf(
                CaretState(
                        LogicalPosition(0, 1),
                        LogicalPosition(0, 1, true),
                        LogicalPosition(0, 2)
                )
        ))
    }

    fun `test selects content in rectangle`() {

        // h[e]llo
        // w[o]rld
        test("""
            hello
            world
        """.trimIndent(), listOf(
                CaretState(
                        LogicalPosition(0, 1),
                        LogicalPosition(0, 1),
                        LogicalPosition(1, 2)
                )
        ), listOf(
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
    }

    fun `test selects short line in middle of rectangle`() {

        // h[ell]o
        // a[a]
        // w[orl]d
        test("""
            hello
            aa
            world
        """.trimIndent(), listOf(
                CaretState(
                        LogicalPosition(0, 1),
                        LogicalPosition(0, 1),
                        LogicalPosition(2, 4)
                )
        ), listOf(
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
    }

    fun `test skips line if line has no content in rectangle`() {

        // h[ell]o
        // a
        // w[orl]d
        test("""
            hello
            a
            world
        """.trimIndent(), listOf(
                CaretState(
                        LogicalPosition(0, 1),
                        LogicalPosition(0, 1),
                        LogicalPosition(2, 4)
                )
        ), listOf(
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

    }

    fun `test negative selection`() {

        // h[ell]o
        // a
        // w[orl]d
        test("""
            hello
            a
            world
        """.trimIndent(), listOf(
                CaretState(
                        LogicalPosition(0, 4),
                        LogicalPosition(0, 4),
                        LogicalPosition(2, 1)
                )
        ), listOf(
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

    fun `test works with multiple cursors`() {
        test("""
            hello world
            hi how
            are you today
        """.trimIndent(), listOf(
                CaretState(
                        LogicalPosition(0, 1),
                        LogicalPosition(0, 1),
                        LogicalPosition(1, 3)
                ),
                CaretState(
                        LogicalPosition(1, 5),
                        LogicalPosition(1, 5),
                        LogicalPosition(2, 9)
                )
        ), listOf(
                CaretState(
                        LogicalPosition(0, 1),
                        LogicalPosition(0, 1),
                        LogicalPosition(0, 3)
                ),
                CaretState(
                        LogicalPosition(1, 1),
                        LogicalPosition(1, 1),
                        LogicalPosition(1, 3)
                ),
                CaretState(
                        LogicalPosition(1, 5),
                        LogicalPosition(1, 5),
                        LogicalPosition(1, 6)
                ),
                CaretState(
                        LogicalPosition(2, 5),
                        LogicalPosition(2, 5),
                        LogicalPosition(2, 9)
                )
        ))
    }

    private fun test(
            text: String,
            initialSelections: List<CaretState>,
            expectedSelections: List<CaretState>
    ) {
        myFixture.configureByText(PLAIN_TEXT, text)
        val caretModel = myFixture.editor.caretModel
        caretModel.caretsAndSelections = initialSelections
        myFixture.performEditorAction("com.gitlab.lae.intellij.actions.CreateRectangularSelectionFromMultiLineSelection")
        assertEquals(
                positions(expectedSelections),
                positions(myFixture.editor.caretModel.caretsAndSelections))
    }

    private fun positions(caretStates: List<CaretState>) = caretStates.map {
        Triple(it.caretPosition, it.selectionStart, it.selectionEnd)
    }
}
