package com.gitlab.lae.intellij.actions.java

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_PASTE_SIMPLE
import com.intellij.openapi.editor.CaretState
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class KillToCodeEndTest : LightPlatformCodeInsightFixtureTestCase() {

  fun testDeleteBracketContentWithoutDeletingClosingBracket() {
    test(
      "class A { int i = (1| - 1); }",
      "class A { int i = (1); }"
    )
  }

  fun testDeleteAtEndBracketDoesNothing() {
    test(
      "class A { int i = (1|); }",
      "class A { int i = (1); }"
    )
  }

  fun testDeleteAtStartBracketOfExpressionDeletesWholeExpression() {
    test(
      "class A { int i = |(1); }",
      "class A { int i = ; }"
    )
  }

  fun testDeleteChar() {
    test(
      "class A { char a = '|a'; }",
      "class A { char a = ''; }"
    )
    test(
      "class A { char a = |'a'; }",
      "class A { char a = ; }"
    )
  }

  fun testDeleteCompleteClassBody() {
    test(
      "class A |{}",
      "class A "
    )
  }

  fun testDeleteJustBeforeClosingBraceInClassDoesNothing() {
    test(
      "class A {|}",
      "class A {}"
    )
    test(
      "class A { int a = 0; |}",
      "class A { int a = 0; }"
    )
  }

  fun testDeleteJustBeforeClosingBraceInMethodDoesNothing() {
    test(
      "class A { void bob() {|} }",
      "class A { void bob() {} }"
    )
  }

  fun testDeleteJustBeforeClosingBraceInInitializerDoesNothing() {
    test(
      "class A {{|}}",
      "class A {{}}"
    )
  }

  fun testDeleteJustBeforeClosingBraceInStaticInitializerDoesNothing() {
    test(
      "class A { static {|} }",
      "class A { static {} }"
    )
  }

  fun testDeleteJustBeforeClosingBraceInStatementDoesNothing() {
    test(
      "class A { void bob() { {|} } }",
      "class A { void bob() { {} } }"
    )
  }

  fun testDeleteFirstMethodInvocationParameter() {
    test(
      "class A {{ bob(|1, 2, 3); }}",
      "class A {{ bob(2, 3); }}"
    )
    test(
      "class A {{ bob( |1, 2, 3); }}",
      "class A {{ bob( 2, 3); }}"
    )
    test(
      "class A {{ bob(| 1, 2, 3); }}",
      "class A {{ bob(2, 3); }}"
    )
  }

  fun testDeleteMiddleMethodInvocationParameter() {
    test(
      "class A {{ bob(1, |2, 3); }}",
      "class A {{ bob(1, 3); }}"
    )
    test(
      "class A {{ bob(1, | 2, 3); }}",
      "class A {{ bob(1, 3); }}"
    )
    test(
      "class A {{ bob(1,| 2, 3); }}",
      "class A {{ bob(1,3); }}"
    )
  }

  fun testDeleteLastMethodInvocationParameter() {
    test(
      "class A {{ bob(1, 2|, 3); }}",
      "class A {{ bob(1, 2); }}"
    )
    test(
      "class A {{ bob(1, 2 |, 3); }}",
      "class A {{ bob(1, 2 ); }}"
    )
    test(
      "class A {{ bob(1, 2,| 3); }}",
      "class A {{ bob(1, 2,); }}"
    )
    test(
      "class A {{ bob(1, 2, | 3); }}",
      "class A {{ bob(1, 2, ); }}"
    )
  }

  fun testDeleteCompleteMethodInvocationParameterList() {
    test(
      "class A {{ bob|(1, 2, 3); }}",
      "class A {{ bob; }}"
    )
  }

  fun testDeleteMethodInvocationJustBeforeClosingDoesNothing() {
    test(
      "class A {{ bob(|); }}",
      "class A {{ bob(); }}"
    )
    test(
      "class A {{ bob(1, 2, 3|); }}",
      "class A {{ bob(1, 2, 3); }}"
    )
  }

  fun testDeleteMethodInvocationWhitespace() {
    test(
      "class A {{ bob(| ); }}",
      "class A {{ bob(); }}"
    )
  }

  fun testDeleteFirstArrayInitializationElement() {
    test(
      "class A { int[] arr = {|1, 2, 3}; }",
      "class A { int[] arr = {2, 3}; }"
    )
  }

  fun testDeleteMiddleArrayInitialization() {
    test(
      "class A { int[] arr = {1, |2, 3}; }",
      "class A { int[] arr = {1, 3}; }"
    )
  }

  fun testDeleteLastArrayInitialization() {
    test(
      "class A { int[] arr = {1, 2 |, 3}; }",
      "class A { int[] arr = {1, 2 }; }"
    )
  }

  fun testDeleteCompleteArrayInitialization() {
    test(
      "class A { int[] arr = |{1, 2, 3}; }",
      "class A { int[] arr = ; }"
    )
  }

  fun testDeleteArrayInitializationJustBeforeClosingDoesNothing() {
    test(
      "class A { int[] arr = {|}; }",
      "class A { int[] arr = {}; }"
    )
    test(
      "class A { int[] arr = {1, 2, 3|}; }",
      "class A { int[] arr = {1, 2, 3}; }"
    )
  }

  fun testDeleteCompleteIfCondition() {
    test(
      "class A {{ if (|true) {} }}",
      "class A {{ if () {} }}"
    )
  }

  fun testDeletePartialIfCondition() {
    test(
      "class A {{ if (1| == 1) {} }}",
      "class A {{ if (1) {} }}"
    )
  }

  fun testDeleteFirstCompleteIfConditionWithinMultipleConditions() {
    test(
      "class A {{ if (|1 == 1 && true) {} }}",
      "class A {{ if (1 && true) {} }}"
    )
  }

  fun testDeleteLastCompleteIfConditionWithinMultipleConditions() {
    test(
      "class A {{ if (1 == 1| && true) {} }}",
      "class A {{ if (1 == 1) {} }}"
    )
  }

  fun testDeleteMiddleCompleteIfConditionAfterOperatorWithinMultipleConditions() {
    test(
      "class A {{ if (1 == 1 && |true && false) {} }}",
      "class A {{ if (1 == 1 && false) {} }}"
    )
  }

  fun testDeleteMiddleCompleteIfConditionBeforeOperatorWithinMultipleConditions() {
    test(
      "class A {{ if (1 == 1| && true && false) {} }}",
      "class A {{ if (1 == 1 && false) {} }}"
    )
  }

  fun testDeletePartialIfConditionWithinMultipleConditions() {
    test(
      "class A {{ if (1| == 1 && true) {} }}",
      "class A {{ if (1 && true) {} }}"
    )
  }

  fun testDeleteCompleteStringLiteral() {
    test(
      "class A { String text = |\"hello world\"; }",
      "class A { String text = ; }"
    )
  }

  fun testDeletePartialStringLiteral() {
    test(
      "class A { String text = \"he|llo world\"; }",
      "class A { String text = \"he\"; }"
    )
  }

  fun testDeleteStringLiteralFromStart() {
    test(
      "class A { String text = \"|hello world\"; }",
      "class A { String text = \"\"; }"
    )
  }

  fun testDeleteStringJustBeforeEndQuoteDoesNothing() {
    test(
      "class A { String text = \"|\"; }",
      "class A { String text = \"\"; }"
    )
    test(
      "class A { String text = \"abc|\"; }",
      "class A { String text = \"abc\"; }"
    )
  }

  fun testDeleteSingleLineCommentFromLineStartWillDeleteWholeLineComment() {
    test(
      """
      class Main {
      |    // bob
          int bob;
      }
      """,
      """
      class Main {
      
          int bob;
      }
      """
    )
  }

  fun testDeleteSingleLineCommentFromMiddleWillDeleteRestOfTheLine() {
    test(
      """
      class Main {
          // b|ob
          int bob;
      }
      """,
      """
      class Main {
          // b
          int bob;
      }
      """
    )
  }

  fun testDeleteMixedTabsAndSpaces() {
    test(
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
      """,
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
    test(
      """
      class Main {
        public static void main(String[] args) {
      |    System
              .out
              .println();
          System.exit(0);
        }
      }
      """,
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
    test(
      """
      class Main {
        public static void main(String[] args) {
          Sys|tem
              .out
              .println();
          System.exit(0);
        }
      }
      """,
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
    test(
      """
      class Main {
        public static int test() {
      |    try {
            throw new RuntimeException();
          } catch (RuntimeException e) {
          }
        }
      }
      """,
      """
      class Main {
        public static int test() {
      
        }
      }
      """
    )
  }

  fun testDeletePartialTryCatch() {
    test(
      """
      class Main {
        public static int test() {
          tr|y {
            throw new RuntimeException();
          } catch (RuntimeException e) {
          }
        }
      }
      """,
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
    test(
      """
      class Main {
        public static int test() {
      |    return
              1;
        }
      }
      """,
      """
      class Main {
        public static int test() {
      
        }
      }
      """
    )
  }

  fun testDeletePartialReturnStatement() {
    test(
      """
      class Main {
        public static int test() {
          retur|n
              1;
        }
      }
      """,
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
    test(
      """
      class Main {
      |  public static int test() {
          return 1;
        }
      }
      """,
      """
      class Main {
      
      }
      """
    )
  }

  fun testDeletePartialMethod() {
    test(
      """
      class Main {
        public stat|ic int test() {
          return
              1;
        }
      }
      """,
      """
      class Main {
        public stat
      }
      """
    )
  }

  fun testDeleteFirstParameter() {
    test(
      "class A { void test(|int a, int b, int c) {}}",
      "class A { void test(int b, int c) {}}"
    )
  }

  fun testDeleteMiddleParameter() {
    test(
      "class A { void test(int a, |int b, int c) {}}",
      "class A { void test(int a, int c) {}}"
    )
  }

  fun testDeleteLastParameter() {
    test(
      "class A { void test(int a, int b|, int c) {}}",
      "class A { void test(int a, int b) {}}"
    )
    test(
      "class A { void test(int a, int b, |int c) {}}",
      "class A { void test(int a, int b, ) {}}"
    )
  }

  fun testDeleteCompleteParameterList() {
    test(
      "class A { void test|(int a, int b, int c) {}}",
      "class A { void test {}}"
    )
  }

  fun testDeleteFirstTypeParameter() {
    test(
      "class A<|A, B, C> {}",
      "class A<B, C> {}"
    )
  }

  fun testDeleteMiddleTypeParameter() {
    test(
      "class A<A, |B, C> {}",
      "class A<A, C> {}"
    )
    test(
      "class A<A|, B, C> {}",
      "class A<A, C> {}"
    )
  }

  fun testDeleteLastTypeParameter() {
    test(
      "class A<A, B|, C> {}",
      "class A<A, B> {}"
    )
    test(
      "class A<A, B,| C> {}",
      "class A<A, B,> {}"
    )
  }

  fun testDeleteCompleteTypeParameterList() {
    test(
      "class A|<A, B, C> {}",
      "class A {}"
    )
  }

  fun testArgumentListJustBeforeClosingBracketDoesNothing() {
    test(
      "class A { void test(int i|) {} }",
      "class A { void test(int i) {} }"
    )
    test(
      """
      class Main {
        void test(|) {
          int i = 0;
        }
      }
      """,
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
    test(
      """
      class Main {
        int i;
      |  public Main() {
          i = 1;
        }
      }
      """,
      """
      class Main {
        int i;
      
      }
      """
    )
  }

  fun testDeletePartialConstructor() {
    test(
      """
      class Main {
        int i;
        public Ma|in() {
          i = 1;
        }
      }
      """,
      """
      class Main {
        int i;
        public Ma
      }
      """
    )
  }

  fun testDeleteCompleteInitializer() {
    test(
      """
      class Main {
        int i;
      |  {
          i = 1;
        }
      }
      """,
      """
      class Main {
        int i;
      
      }
      """
    )
  }

  fun testDeletePartialInitializer() {
    test(
      """
      class Main {
        static int i;
        stati|c {
          i = 1;
        }
      }
      """,
      """
      class Main {
        static int i;
        stati
      }
      """
    )
  }

  fun testDeleteCompleteField() {
    test(
      """
      class Main {
      |  public static int
          test = 1;
      }
      """,
      """
      class Main {
      
      }
      """
    )
  }

  fun testDeletePartialField() {
    test(
      """
      class Main {
        public stat|ic int
          test = 1;
      }
      """,
      """
      class Main {
        public stat
      }
      """
    )
  }

  fun testDeleteCompleteInnerClass() {
    test(
      """
      class Main {
      |  public static class Test {
          int i;
        }
      }
      """,
      """
      class Main {
      
      }
      """
    )
  }

  fun testDeletePartialInnerClass() {
    test(
      """
      class Main {
        public stat|ic class Test {
          int i;
        }
      }
      """,
      """
      class Main {
        public stat
      }
      """
    )
  }

  fun testDeleteCompleteClass() {
    test(
      """
      |class Main {
        public static int test = 1;
      }
      """,
      ""
    )
  }

  fun testDeletePartialClass() {
    test(
      """
      class Ma|in {
        public static int test = 1;
      }
      """,
      "class Ma"
    )
  }

  fun testDeleteEmptyLine() {
    test(
      """
      class Main {
      |
        public static int test = 1;
      }
      """,
      """
      class Main {
        public static int test = 1;
      }
      """
    )
  }

  fun testDeleteTextEndDoesNothing() {
    test(
      """
      class Main {
        public static int test = 1;
      }|
      """,
      """
      class Main {
        public static int test = 1;
      }
      """
    )
  }

  fun testMultipleCursors() {
    test(
      """
      class Main {
      |  public static int test1 = 1;
        public static int test2 = 2;
      |  public static int test3() {
          return 3;
        }
      }
      """,
      """
      class Main {
      
        public static int test2 = 2;
      
      }
      """
    )
  }

  fun testPasteWhatsBeenKilledWillGetBackOriginalText() {
    test(
      """
      class Main {
      |  public static int test() {
          return 1;
        }
      }
      """,
      """
      class Main {
        public static int test() {
          return 1;
        }
      }
      """,
      true
    )
  }

  fun testPasteWhatsBeenKilledWithMultipleCursorsWillGetBackOriginalText() {
    test(
      """
      class Main {
      |  public static int test1 = 1;
        public static int test2 = 2;
      |  public static int test3() {
          return 3;
        }
      }
      """,
      """
      class Main {
        public static int test1 = 1;
        public static int test2 = 2;
        public static int test3() {
          return 3;
        }
      }
      """,
      true
    )
  }

  private fun test(
    input: String,
    output: String,
    pasteAfterKill: Boolean = false
  ) {
    val (initialText, initialCarets) = init(input)
    action(initialText, initialCarets)
    if (pasteAfterKill) {
      myFixture.performEditorAction(ACTION_EDITOR_PASTE_SIMPLE)
    }
    assertEquals(output.trimIndent(), myFixture.editor.document.text)
  }

  private fun action(
    initialText: String,
    initialCarets: List<LogicalPosition>
  ) {
    myFixture.configureByText(JavaFileType.INSTANCE, initialText)
    myFixture.editor.caretModel.caretsAndSelections =
      initialCarets.map { CaretState(it, it, it) }
    myFixture.performEditorAction("com.gitlab.lae.intellij.actions.java.KillToCodeEnd")
  }

  private fun init(input: String): Pair<String, List<LogicalPosition>> {
    val content = input.trimIndent()
      .lineSequence()
      .mapIndexed { row, line ->
        line.replace("|", "") to
          line.asSequence()
            .mapIndexedNotNull { i, char -> if (char == '|') i else null }
            .mapIndexed { i, column -> LogicalPosition(row, column - i) }
            .toList()
      }
      .toList()

    val initialText = content.joinToString("\n") { it.first }
    val initialCarets = content.flatMap { it.second }
    return Pair(initialText, initialCarets)
  }
}
