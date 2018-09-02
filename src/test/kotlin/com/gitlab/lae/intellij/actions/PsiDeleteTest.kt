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

    fun `test delete complete try-catch`() {
        test("""
            class Main {
              public static int test() {
                try {
                  throw new RuntimeException();
                } catch (RuntimeException e) {
                }
              }
            }
        """.trimIndent(), """
            class Main {
              public static int test() {

              }
            }
        """.trimIndent(), LogicalPosition(2, 0))
    }

    fun `test delete partial try-catch`() {
        test("""
            class Main {
              public static int test() {
                try {
                  throw new RuntimeException();
                } catch (RuntimeException e) {
                }
              }
            }
        """.trimIndent(), """
            class Main {
              public static int test() {
                tr
              }
            }
        """.trimIndent(), LogicalPosition(2, 6))
    }

    fun `test delete complete return statement`() {
        test("""
            class Main {
              public static int test() {
                return
                    1;
              }
            }
        """.trimIndent(), """
            class Main {
              public static int test() {

              }
            }
        """.trimIndent(), LogicalPosition(2, 0))
    }

    fun `test delete partial return statement`() {
        test("""
            class Main {
              public static int test() {
                return
                    1;
              }
            }
        """.trimIndent(), """
            class Main {
              public static int test() {
                retur
              }
            }
        """.trimIndent(), LogicalPosition(2, 9))
    }

    fun `test delete complete method`() {
        test("""
            class Main {
              public static int test() {
                return 1;
              }
            }
        """.trimIndent(), """
            class Main {

            }
        """.trimIndent(), LogicalPosition(1, 0))
    }

    fun `test delete partial method`() {
        test("""
            class Main {
              public static int test() {
                return
                    1;
              }
            }
        """.trimIndent(), """
            class Main {
              public stat
            }
        """.trimIndent(), LogicalPosition(1, 13))
    }

    fun `test delete complete constructor`() {
        test("""
            class Main {
              int i;
              public Main() {
                i = 1;
              }
            }
        """.trimIndent(), """
            class Main {
              int i;

            }
        """.trimIndent(), LogicalPosition(2, 0))
    }

    fun `test delete partial constructor`() {
        test("""
            class Main {
              int i;
              public Main() {
                i = 1;
              }
            }
        """.trimIndent(), """
            class Main {
              int i;
              public Ma
            }
        """.trimIndent(), LogicalPosition(2, 11))
    }

    fun `test delete complete initializer`() {
        test("""
            class Main {
              int i;
              {
                i = 1;
              }
            }
        """.trimIndent(), """
            class Main {
              int i;

            }
        """.trimIndent(), LogicalPosition(2, 0))
    }

    fun `test delete partial initializer`() {
        test("""
            class Main {
              static int i;
              static {
                i = 1;
              }
            }
        """.trimIndent(), """
            class Main {
              static int i;
              stati
            }
        """.trimIndent(), LogicalPosition(2, 7))
    }

    fun `test delete complete field`() {
        test("""
            class Main {
              public static int test = 1;
            }
        """.trimIndent(), """
            class Main {

            }
        """.trimIndent(), LogicalPosition(1, 0))
    }

    fun `test delete partial field`() {
        test("""
            class Main {
              public static int test = 1;
            }
        """.trimIndent(), """
            class Main {
              public stat
            }
        """.trimIndent(), LogicalPosition(1, 13))
    }

    fun `test delete complete inner class`() {
        test("""
            class Main {
              public static class Test {
                int i;
              }
            }
        """.trimIndent(), """
            class Main {

            }
        """.trimIndent(), LogicalPosition(1, 0))
    }

    fun `test delete partial inner class`() {
        test("""
            class Main {
              public static class Test {
                int i;
              }
            }
        """.trimIndent(), """
            class Main {
              public stat
            }
        """.trimIndent(), LogicalPosition(1, 13))
    }

    fun `test delete complete class`() {
        test("""
            class Main {
              public static int test = 1;
            }
        """.trimIndent(), """

        """.trimIndent(), LogicalPosition(0, 0))
    }

    fun `test delete partial class`() {
        test("""
            class Main {
              public static int test = 1;
            }
        """.trimIndent(), """
            class Ma
        """.trimIndent(), LogicalPosition(0, 8))
    }

    fun `test delete empty line`() {
        test("""
            class Main {

              public static int test = 1;
            }
        """.trimIndent(), """
            class Main {
              public static int test = 1;
            }
        """.trimIndent(), LogicalPosition(1, 0))
    }

    fun `test delete text end does nothing`() {
        test("""
            class Main {
              public static int test = 1;
            }
        """.trimIndent(), """
            class Main {
              public static int test = 1;
            }
        """.trimIndent(), LogicalPosition(2, 1))
    }

    private fun test(
            initialText: String,
            expectedText: String,
            vararg initialCaretPositions: LogicalPosition
    ) {
        myFixture.configureByText(JavaFileType.INSTANCE, initialText)
        myFixture.editor.caretModel.caretsAndSelections = initialCaretPositions
                .map { CaretState(it, it, it) }
        myFixture.performEditorAction("com.gitlab.lae.intellij.actions.PsiDelete")
        assertEquals(expectedText, myFixture.editor.document.text)
    }
}