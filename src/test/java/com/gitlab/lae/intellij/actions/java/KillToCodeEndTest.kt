package com.gitlab.lae.intellij.actions.java

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_PASTE_SIMPLE
import com.intellij.openapi.editor.CaretState
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import java.util.*

class KillToCodeEndTest : LightPlatformCodeInsightFixtureTestCase() {

  fun testDeleteBracketContentWithoutDeletingClosingBracket() {
    Tester()
      .initialInput("class A { int i = (1| - 1); }")
      .expectOutput("class A { int i = (1); }")
  }

  fun testDeleteAtEndBracketDoesNothing() {
    Tester()
      .initialInput("class A { int i = (1|); }")
      .expectOutput("class A { int i = (1); }")
  }

  fun testDeleteAtStartBracketOfExpressionDeletesWholeExpression() {
    Tester()
      .initialInput("class A { int i = |(1); }")
      .expectOutput("class A { int i = ; }")
  }

  fun testDeleteChar() {
    Tester()
      .initialInput("class A { char a = '|a'; }")
      .expectOutput("class A { char a = ''; }")
    Tester()
      .initialInput("class A { char a = |'a'; }")
      .expectOutput("class A { char a = ; }")
  }

  fun testDeleteCompleteClassBody() {
    Tester()
      .initialInput("class A |{}")
      .expectOutput("class A ")
  }

  fun testDeleteJustBeforeClosingBraceInClassDoesNothing() {
    Tester()
      .initialInput("class A {|}")
      .expectOutput("class A {}")
    Tester()
      .initialInput("class A { int a = 0; |}")
      .expectOutput("class A { int a = 0; }")
  }

  fun testDeleteJustBeforeClosingBraceInMethodDoesNothing() {
    Tester()
      .initialInput("class A { void bob() {|} }")
      .expectOutput("class A { void bob() {} }")
  }

  fun testDeleteJustBeforeClosingBraceInInitializerDoesNothing() {
    Tester()
      .initialInput("class A {{|}}")
      .expectOutput("class A {{}}")
  }

  fun testDeleteJustBeforeClosingBraceInStaticInitializerDoesNothing() {
    Tester()
      .initialInput("class A { static {|} }")
      .expectOutput("class A { static {} }")
  }

  fun testDeleteJustBeforeClosingBraceInStatementDoesNothing() {
    Tester()
      .initialInput("class A { void bob() { {|} } }")
      .expectOutput("class A { void bob() { {} } }")
  }

  fun testDeleteFirstMethodInvocationParameter() {
    Tester()
      .initialInput("class A {{ bob(|1, 2, 3); }}")
      .expectOutput("class A {{ bob(2, 3); }}")
    Tester()
      .initialInput("class A {{ bob( |1, 2, 3); }}")
      .expectOutput("class A {{ bob( 2, 3); }}")
    Tester()
      .initialInput("class A {{ bob(| 1, 2, 3); }}")
      .expectOutput("class A {{ bob(2, 3); }}")
  }

  fun testDeleteMiddleMethodInvocationParameter() {
    Tester()
      .initialInput("class A {{ bob(1, |2, 3); }}")
      .expectOutput("class A {{ bob(1, 3); }}")
    Tester()
      .initialInput("class A {{ bob(1, | 2, 3); }}")
      .expectOutput("class A {{ bob(1, 3); }}")
    Tester()
      .initialInput("class A {{ bob(1,| 2, 3); }}")
      .expectOutput("class A {{ bob(1,3); }}")
  }

  fun testDeleteLastMethodInvocationParameter() {
    Tester()
      .initialInput("class A {{ bob(1, 2|, 3); }}")
      .expectOutput("class A {{ bob(1, 2); }}")
    Tester()
      .initialInput("class A {{ bob(1, 2 |, 3); }}")
      .expectOutput("class A {{ bob(1, 2 ); }}")
    Tester()
      .initialInput("class A {{ bob(1, 2,| 3); }}")
      .expectOutput("class A {{ bob(1, 2,); }}")
    Tester()
      .initialInput("class A {{ bob(1, 2, | 3); }}")
      .expectOutput("class A {{ bob(1, 2, ); }}")
  }

  fun testDeleteCompleteMethodInvocationParameterList() {
    Tester()
      .initialInput("class A {{ bob|(1, 2, 3); }}")
      .expectOutput("class A {{ bob; }}")
  }

  fun testDeleteMethodInvocationJustBeforeClosingDoesNothing() {
    Tester()
      .initialInput("class A {{ bob(|); }}")
      .expectOutput("class A {{ bob(); }}")
    Tester()
      .initialInput("class A {{ bob(1, 2, 3|); }}")
      .expectOutput("class A {{ bob(1, 2, 3); }}")
  }

  fun testDeleteMethodInvocationWhitespace() {
    Tester()
      .initialInput("class A {{ bob(| ); }}")
      .expectOutput("class A {{ bob(); }}")
  }

  fun testDeleteFirstArrayInitializationElement() {
    Tester()
      .initialInput("class A { int[] arr = {|1, 2, 3}; }")
      .expectOutput("class A { int[] arr = {2, 3}; }")
  }

  fun testDeleteMiddleArrayInitialization() {
    Tester()
      .initialInput("class A { int[] arr = {1, |2, 3}; }")
      .expectOutput("class A { int[] arr = {1, 3}; }")
  }

  fun testDeleteLastArrayInitialization() {
    Tester()
      .initialInput("class A { int[] arr = {1, 2 |, 3}; }")
      .expectOutput("class A { int[] arr = {1, 2 }; }")
  }

  fun testDeleteCompleteArrayInitialization() {
    Tester()
      .initialInput("class A { int[] arr = |{1, 2, 3}; }")
      .expectOutput("class A { int[] arr = ; }")
  }

  fun testDeleteArrayInitializationJustBeforeClosingDoesNothing() {
    Tester()
      .initialInput("class A { int[] arr = {|}; }")
      .expectOutput("class A { int[] arr = {}; }")
    Tester()
      .initialInput("class A { int[] arr = {1, 2, 3|}; }")
      .expectOutput("class A { int[] arr = {1, 2, 3}; }")
  }

  fun testDeleteCompleteIfCondition() {
    Tester()
      .initialInput("class A {{ if (|true) {} }}")
      .expectOutput("class A {{ if () {} }}")
  }

  fun testDeletePartialIfCondition() {
    Tester()
      .initialInput("class A {{ if (1| == 1) {} }}")
      .expectOutput("class A {{ if (1) {} }}")
  }

  fun testDeleteFirstCompleteIfConditionWithinMultipleConditions() {
    Tester()
      .initialInput("class A {{ if (|1 == 1 && true) {} }}")
      .expectOutput("class A {{ if (1 && true) {} }}")
  }

  fun testDeleteLastCompleteIfConditionWithinMultipleConditions() {
    Tester()
      .initialInput("class A {{ if (1 == 1| && true) {} }}")
      .expectOutput("class A {{ if (1 == 1) {} }}")
  }

  fun testDeleteMiddleCompleteIfConditionAfterOperatorWithinMultipleConditions() {
    Tester()
      .initialInput("class A {{ if (1 == 1 && |true && false) {} }}")
      .expectOutput("class A {{ if (1 == 1 && false) {} }}")
  }

  fun testDeleteMiddleCompleteIfConditionBeforeOperatorWithinMultipleConditions() {
    Tester()
      .initialInput("class A {{ if (1 == 1| && true && false) {} }}")
      .expectOutput("class A {{ if (1 == 1 && false) {} }}")
  }

  fun testDeletePartialIfConditionWithinMultipleConditions() {
    Tester()
      .initialInput("class A {{ if (1| == 1 && true) {} }}")
      .expectOutput("class A {{ if (1 && true) {} }}")
  }

  fun testDeleteCompleteStringLiteral() {
    Tester()
      .initialInput("class A { String text = |\"hello world\"; }")
      .expectOutput("class A { String text = ; }")
  }

  fun testDeletePartialStringLiteral() {
    Tester()
      .initialInput("class A { String text = \"he|llo world\"; }")
      .expectOutput("class A { String text = \"he\"; }")
  }

  fun testDeleteStringLiteralFromStart() {
    Tester()
      .initialInput("class A { String text = \"|hello world\"; }")
      .expectOutput("class A { String text = \"\"; }")
  }

  fun testDeleteStringJustBeforeEndQuoteDoesNothing() {
    Tester()
      .initialInput("class A { String text = \"|\"; }")
      .expectOutput("class A { String text = \"\"; }")
    Tester()
      .initialInput("class A { String text = \"abc|\"; }")
      .expectOutput("class A { String text = \"abc\"; }")
  }

  fun testDeleteSingleLineCommentFromLineStartWillDeleteWholeLineComment() {
    Tester()
      .initialInput(
        """
        class Main {
        |    // bob
            int bob;
        }
        """
      )
      .expectOutput(
        """
        class Main {
        
            int bob;
        }
        """
      )
  }

  fun testDeleteSingleLineCommentFromMiddleWillDeleteRestOfTheLine() {
    Tester()
      .initialInput(
        """
        class Main {
            // b|ob
            int bob;
        }
        """
      )
      .expectOutput(
        """
        class Main {
            // b
            int bob;
        }
        """
      )
  }

  fun testDeleteMixedTabsAndSpaces() {
    Tester()
      .initialInput(
        """
        class Main {
            public void test() {
                Collection<String> test1 = null;
                Collection<String> test2 = null;
        |        test1.forEach(s -> {
                    if(test2.contains(s.toUpperCase())){
        				System.out.println(s);
        				System.out.println(s);
                    }
        		});
            }
        }
        """
      )
      .expectOutput(
        """
        class Main {
            public void test() {
                Collection<String> test1 = null;
                Collection<String> test2 = null;
        
            }
        }
        """
      )
  }

  fun testDeleteCompleteStatement() {
    Tester()
      .initialInput(
        """
        class Main {
          public static void main(String[] args) {
        |    System
                .out
                .println();
            System.exit(0);
          }
        }
        """
      )
      .expectOutput(
        """
        class Main {
          public static void main(String[] args) {
        
            System.exit(0);
          }
        }
        """
      )
  }

  fun testDeletePartialStatement() {
    Tester()
      .initialInput(
        """
        class Main {
          public static void main(String[] args) {
            Sys|tem
                .out
                .println();
            System.exit(0);
          }
        }
        """
      )
      .expectOutput(
        """
        class Main {
          public static void main(String[] args) {
            Sys
            System.exit(0);
          }
        }
        """
      )
  }

  fun testDeleteCompleteTryCatch() {
    Tester()
      .initialInput(
        """
        class Main {
          public static int test() {
        |    try {
              throw new RuntimeException();
            } catch (RuntimeException e) {
            }
          }
        }
        """
      )
      .expectOutput(
        """
        class Main {
          public static int test() {
        
          }
        }
        """
      )
  }

  fun testDeletePartialTryCatch() {
    Tester()
      .initialInput(
        """
        class Main {
          public static int test() {
            tr|y {
              throw new RuntimeException();
            } catch (RuntimeException e) {
            }
          }
        }
        """
      )
      .expectOutput(
        """
        class Main {
          public static int test() {
            tr
          }
        }
        """
      )
  }

  fun testDeleteCompleteReturnStatement() {
    Tester()
      .initialInput(
        """
        class Main {
          public static int test() {
        |    return
                1;
          }
        }
        """
      )
      .expectOutput(
        """
        class Main {
          public static int test() {
        
          }
        }
        """
      )
  }

  fun testDeletePartialReturnStatement() {
    Tester()
      .initialInput(
        """
        class Main {
          public static int test() {
            retur|n
                1;
          }
        }
        """
      )
      .expectOutput(
        """
        class Main {
          public static int test() {
            retur
          }
        }
        """
      )
  }

  fun testDeleteCompleteMethod() {
    Tester()
      .initialInput(
        """
        class Main {
        |  public static int test() {
            return 1;
          }
        }
        """
      )
      .expectOutput(
        """
        class Main {
        
        }
        """
      )
  }

  fun testDeletePartialMethod() {
    Tester()
      .initialInput(
        """
        class Main {
          public stat|ic int test() {
            return
                1;
          }
        }
        """
      )
      .expectOutput(
        """
        class Main {
          public stat
        }
        """
      )
  }

  fun testDeleteFirstParameter() {
    Tester()
      .initialInput("class A { void test(|int a, int b, int c) {}}")
      .expectOutput("class A { void test(int b, int c) {}}")
  }

  fun testDeleteMiddleParameter() {
    Tester()
      .initialInput("class A { void test(int a, |int b, int c) {}}")
      .expectOutput("class A { void test(int a, int c) {}}")
  }

  fun testDeleteLastParameter() {
    Tester()
      .initialInput("class A { void test(int a, int b|, int c) {}}")
      .expectOutput("class A { void test(int a, int b) {}}")
    Tester()
      .initialInput("class A { void test(int a, int b, |int c) {}}")
      .expectOutput("class A { void test(int a, int b, ) {}}")
  }

  fun testDeleteCompleteParameterList() {
    Tester()
      .initialInput("class A { void test|(int a, int b, int c) {}}")
      .expectOutput("class A { void test {}}")
  }

  fun testDeleteFirstTypeParameter() {
    Tester()
      .initialInput("class A<|A, B, C> {}")
      .expectOutput("class A<B, C> {}")
  }

  fun testDeleteMiddleTypeParameter() {
    Tester()
      .initialInput("class A<A, |B, C> {}")
      .expectOutput("class A<A, C> {}")
    Tester()
      .initialInput("class A<A|, B, C> {}")
      .expectOutput("class A<A, C> {}")
  }

  fun testDeleteLastTypeParameter() {
    Tester()
      .initialInput("class A<A, B|, C> {}")
      .expectOutput("class A<A, B> {}")
    Tester()
      .initialInput("class A<A, B,| C> {}")
      .expectOutput("class A<A, B,> {}")
  }

  fun testDeleteCompleteTypeParameterList() {
    Tester()
      .initialInput("class A|<A, B, C> {}")
      .expectOutput("class A {}")
  }

  fun testArgumentListJustBeforeClosingBracketDoesNothing() {
    Tester()
      .initialInput("class A { void test(int i|) {} }")
      .expectOutput("class A { void test(int i) {} }")
    Tester()
      .initialInput(
        """
        class Main {
          void test(|) {
            int i = 0;
          }
        }
        """
      )
      .expectOutput(
        """
        class Main {
          void test() {
            int i = 0;
          }
        }
        """
      )
  }

  fun testDeleteCompleteConstructor() {
    Tester()
      .initialInput(
        """
        class Main {
          int i;
        |  public Main() {
            i = 1;
          }
        }
        """
      )
      .expectOutput(
        """
        class Main {
          int i;
        
        }
        """
      )
  }

  fun testDeletePartialConstructor() {
    Tester()
      .initialInput(
        """
        class Main {
          int i;
          public Ma|in() {
            i = 1;
          }
        }
        """
      )
      .expectOutput(
        """
        class Main {
          int i;
          public Ma
        }
        """
      )
  }

  fun testDeleteCompleteInitializer() {
    Tester()
      .initialInput(
        """
        class Main {
          int i;
        |  {
            i = 1;
          }
        }
        """
      )
      .expectOutput(
        """
        class Main {
          int i;
        
        }
        """
      )
  }

  fun testDeletePartialInitializer() {
    Tester()
      .initialInput(
        """
        class Main {
          static int i;
          stati|c {
            i = 1;
          }
        }
        """
      )
      .expectOutput(
        """
        class Main {
          static int i;
          stati
        }
        """
      )
  }

  fun testDeleteCompleteField() {
    Tester()
      .initialInput(
        """
        class Main {
        |  public static int
            test = 1;
        }
        """
      )
      .expectOutput(
        """
        class Main {
        
        }
        """
      )
  }

  fun testDeletePartialField() {
    Tester()
      .initialInput(
        """
        class Main {
          public stat|ic int
            test = 1;
        }
        """
      )
      .expectOutput(
        """
        class Main {
          public stat
        }
        """
      )
  }

  fun testDeleteCompleteInnerClass() {
    Tester()
      .initialInput(
        """
        class Main {
        |  public static class Test {
            int i;
          }
        }
        """
      )
      .expectOutput(
        """
        class Main {
        
        }
        """
      )
  }

  fun testDeletePartialInnerClass() {
    Tester()
      .initialInput(
        """
        class Main {
          public stat|ic class Test {
            int i;
          }
        }
        """
      )
      .expectOutput(
        """
        class Main {
          public stat
        }
        """
      )
  }

  fun testDeleteCompleteClass() {
    Tester()
      .initialInput(
        """
        |class Main {
          public static int test = 1;
        }
        """
      )
      .expectOutput("")
  }

  fun testDeletePartialClass() {
    Tester()
      .initialInput(
        """
        class Ma|in {
          public static int test = 1;
        }
        """
      )
      .expectOutput("class Ma")
  }

  fun testDeleteEmptyLine() {
    Tester()
      .initialInput(
        """
        class Main {
        |
          public static int test = 1;
        }
        """
      )
      .expectOutput(
        """
        class Main {
          public static int test = 1;
        }
        """
      )
  }

  fun testDeleteTextEndDoesNothing() {
    Tester()
      .initialInput(
        """
        class Main {
          public static int test = 1;
        }|
        """
      )
      .expectOutput(
        """
        class Main {
          public static int test = 1;
        }
        """
      )
  }

  fun testMultipleCursors() {
    Tester()
      .initialInput(
        """
        class Main {
        |  public static int test1 = 1;
          public static int test2 = 2;
        |  public static int test3() {
            return 3;
          }
        }
        """
      )
      .expectOutput(
        """
        class Main {
        
          public static int test2 = 2;
        
        }
        """
      )
  }

  fun testPasteWhatsBeenKilledWillGetBackOriginalText() {
    Tester()
      .initialInput(
        """
        class Main {
        |  public static int test() {
            return 1;
          }
        }
        """
      )
      .doPasteAfterKill()
      .expectOutput(
        """
        class Main {
          public static int test() {
            return 1;
          }
        }
        """
      )
  }

  fun testPasteWhatsBeenKilledWithMultipleCursorsWillGetBackOriginalText() {
    Tester()
      .initialInput(
        """
        class Main {
        |  public static int test1 = 1;
          public static int test2 = 2;
        |  public static int test3() {
            return 3;
          }
        }
        """
      )
      .doPasteAfterKill()
      .expectOutput(
        """
        class Main {
          public static int test1 = 1;
          public static int test2 = 2;
          public static int test3() {
            return 3;
          }
        }
        """
      )
  }

  private inner class Tester {
    private var pasteAfterKill = false
    private var initialText = ""
    private val initialCarets = ArrayList<LogicalPosition>()

    fun doPasteAfterKill(): Tester {
      pasteAfterKill = true
      return this
    }

    fun initialInput(input: String): Tester {
      val lines = input.trimIndent().lines().toMutableList()
      for (i in lines.indices) {
        var line = lines[i]
        var j = 0
        while (j > -1) {
          j = line.indexOf('|', j)
          if (j > -1) {
            line = line.replaceFirst("|", "")
            initialCarets.add(LogicalPosition(i, j))
          }
        }
        lines[i] = line
      }
      initialText = lines.joinToString("\n")
      return this
    }

    fun expectOutput(output: String) {
      myFixture.configureByText(JavaFileType.INSTANCE, initialText)
      myFixture.editor.caretModel.caretsAndSelections =
        initialCarets.map { CaretState(it, it, it) }
      myFixture.performEditorAction("com.gitlab.lae.intellij.actions.java.KillToCodeEnd")
      if (pasteAfterKill) {
        myFixture.performEditorAction(ACTION_EDITOR_PASTE_SIMPLE)
      }
      assertEquals(output.trimIndent(), myFixture.editor.document.text)
    }
  }
}
