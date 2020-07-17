package com.gitlab.lae.intellij.actions.java

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_PASTE_SIMPLE
import com.intellij.openapi.editor.CaretState
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase

class KillToCodeEndTest : LightPlatformCodeInsightFixtureTestCase() {

  fun `test delete bracket content without deleting closing bracket`() {
    test(
      "class A { int i = (1| - 1); }",
      "class A { int i = (1); }"
    )
  }

  fun `test delete at end bracket does nothing`() {
    test(
      "class A { int i = (1|); }",
      "class A { int i = (1); }"
    )
  }

  fun `test delete at start bracket of expression deletes whole expression`() {
    test(
      "class A { int i = |(1); }",
      "class A { int i = ; }"
    )
  }

  fun `test delete char`() {
    test(
      "class A { char a = '|a'; }",
      "class A { char a = ''; }"
    )
    test(
      "class A { char a = |'a'; }",
      "class A { char a = ; }"
    )
  }

  fun `test delete complete class body`() {
    test(
      "class A |{}",
      "class A "
    )
  }

  fun `test delete just before closing brace in class does nothing`() {
    test(
      "class A {|}",
      "class A {}"
    )
    test(
      "class A { int a = 0; |}",
      "class A { int a = 0; }"
    )
  }

  fun `test delete just before closing brace in method does nothing`() {
    test(
      "class A { void bob() {|} }",
      "class A { void bob() {} }"
    )
  }

  fun `test delete just before closing brace in initializer does nothing`() {
    test(
      "class A {{|}}",
      "class A {{}}"
    )
  }

  fun `test delete just before closing brace in static initializer does nothing`() {
    test(
      "class A { static {|} }",
      "class A { static {} }"
    )
  }

  fun `test delete just before closing brace in statement does nothing`() {
    test(
      "class A { void bob() { {|} } }",
      "class A { void bob() { {} } }"
    )
  }

  fun `test delete first method invocation parameter`() {
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

  fun `test delete middle method invocation parameter`() {
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

  fun `test delete last method invocation parameter`() {
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

  fun `test delete complete method invocation parameter list`() {
    test(
      "class A {{ bob|(1, 2, 3); }}",
      "class A {{ bob; }}"
    )
  }

  fun `test delete method invocation just before closing does nothing`() {
    test(
      "class A {{ bob(|); }}",
      "class A {{ bob(); }}"
    )
    test(
      "class A {{ bob(1, 2, 3|); }}",
      "class A {{ bob(1, 2, 3); }}"
    )
  }

  fun `test delete method invocation whitespace`() {
    test(
      "class A {{ bob(| ); }}",
      "class A {{ bob(); }}"
    )
  }

  fun `test delete first array initialization element`() {
    test(
      "class A { int[] arr = {|1, 2, 3}; }",
      "class A { int[] arr = {2, 3}; }"
    )
  }

  fun `test delete middle array initialization`() {
    test(
      "class A { int[] arr = {1, |2, 3}; }",
      "class A { int[] arr = {1, 3}; }"
    )
  }

  fun `test delete last array initialization`() {
    test(
      "class A { int[] arr = {1, 2 |, 3}; }",
      "class A { int[] arr = {1, 2 }; }"
    )
  }

  fun `test delete complete array initialization`() {
    test(
      "class A { int[] arr = |{1, 2, 3}; }",
      "class A { int[] arr = ; }"
    )
  }

  fun `test delete array initialization just before closing does nothing`() {
    test(
      "class A { int[] arr = {|}; }",
      "class A { int[] arr = {}; }"
    )
    test(
      "class A { int[] arr = {1, 2, 3|}; }",
      "class A { int[] arr = {1, 2, 3}; }"
    )
  }

  fun `test delete complete if condition`() {
    test(
      "class A {{ if (|true) {} }}",
      "class A {{ if () {} }}"
    )
  }

  fun `test delete partial if condition`() {
    test(
      "class A {{ if (1| == 1) {} }}",
      "class A {{ if (1) {} }}"
    )
  }

  fun `test delete first complete if condition within multiple conditions`() {
    test(
      "class A {{ if (|1 == 1 && true) {} }}",
      "class A {{ if (1 && true) {} }}"
    )
  }

  fun `test delete last complete if condition within multiple conditions`() {
    test(
      "class A {{ if (1 == 1| && true) {} }}",
      "class A {{ if (1 == 1) {} }}"
    )
  }

  fun `test delete middle complete if condition after operator within multiple conditions`() {
    test(
      "class A {{ if (1 == 1 && |true && false) {} }}",
      "class A {{ if (1 == 1 && false) {} }}"
    )
  }

  fun `test delete middle complete if condition before operator within multiple conditions`() {
    test(
      "class A {{ if (1 == 1| && true && false) {} }}",
      "class A {{ if (1 == 1 && false) {} }}"
    )
  }

  fun `test delete partial if condition within multiple conditions`() {
    test(
      "class A {{ if (1| == 1 && true) {} }}",
      "class A {{ if (1 && true) {} }}"
    )
  }

  fun `test delete if negation`() {
    test(
      "class A {{ if (|!true) {} }}",
      "class A {{ if () {} }}"
    )
  }

  fun `test delete complete string literal`() {
    test(
      "class A { String text = |\"hello world\"; }",
      "class A { String text = ; }"
    )
  }

  fun `test delete partial string literal`() {
    test(
      "class A { String text = \"he|llo world\"; }",
      "class A { String text = \"he\"; }"
    )
  }

  fun `test delete string literal from start`() {
    test(
      "class A { String text = \"|hello world\"; }",
      "class A { String text = \"\"; }"
    )
  }

  fun `test delete string just before end quote does nothing`() {
    test(
      "class A { String text = \"|\"; }",
      "class A { String text = \"\"; }"
    )
    test(
      "class A { String text = \"abc|\"; }",
      "class A { String text = \"abc\"; }"
    )
  }

  fun `test delete single line comment from line start will delete whole line comment`() {
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

  fun `test delete single line comment from middle will delete rest of the line`() {
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

  fun `test delete javadoc description to line end`() {
    test(
      """
      /**
       * Hello| world,
       * <p>
       * how are you?
       */
      """,
      """
      /**
       * Hello
       * <p>
       * how are you?
       */
      """
    )
  }

  fun `test delete javadoc entire tag`() {
    test(
      """
      /**
       * Hello world.
       * <p>
       *| @return how
       *         are you
       */
      """,
      """
      /**
       * Hello world.
       * <p>
       *
       */
      """
    )
  }

  fun `test delete mixed tabs and spaces`() {
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

  fun `test delete complete statement`() {
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

  fun `test delete partial statement`() {
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

  fun `test delete complete try catch`() {
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

  fun `test delete partial try catch`() {
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

  fun `test delete complete return statement`() {
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

  fun `test delete partial return statement`() {
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

  fun `test delete complete method`() {
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

  fun `test delete partial method`() {
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

  fun `test delete first parameter`() {
    test(
      "class A { void test(|int a, int b, int c) {}}",
      "class A { void test(int b, int c) {}}"
    )
  }

  fun `test delete middle parameter`() {
    test(
      "class A { void test(int a, |int b, int c) {}}",
      "class A { void test(int a, int c) {}}"
    )
  }

  fun `test delete last parameter`() {
    test(
      "class A { void test(int a, int b|, int c) {}}",
      "class A { void test(int a, int b) {}}"
    )
    test(
      "class A { void test(int a, int b, |int c) {}}",
      "class A { void test(int a, int b, ) {}}"
    )
  }

  fun `test delete complete parameter list`() {
    test(
      "class A { void test|(int a, int b, int c) {}}",
      "class A { void test {}}"
    )
  }

  fun `test delete first type parameter`() {
    test(
      "class A<|A, B, C> {}",
      "class A<B, C> {}"
    )
  }

  fun `test delete middle type parameter`() {
    test(
      "class A<A, |B, C> {}",
      "class A<A, C> {}"
    )
    test(
      "class A<A|, B, C> {}",
      "class A<A, C> {}"
    )
  }

  fun `test delete last type parameter`() {
    test(
      "class A<A, B|, C> {}",
      "class A<A, B> {}"
    )
    test(
      "class A<A, B,| C> {}",
      "class A<A, B,> {}"
    )
  }

  fun `test delete complete type parameter list`() {
    test(
      "class A|<A, B, C> {}",
      "class A {}"
    )
  }

  fun `test argument list just before closing bracket does nothing`() {
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

  fun `test delete complete constructor`() {
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

  fun `test delete partial constructor`() {
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

  fun `test delete complete initializer`() {
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

  fun `test delete partial initializer`() {
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

  fun `test delete complete field`() {
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

  fun `test delete partial field`() {
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

  fun `test delete complete inner class`() {
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

  fun `test delete partial inner class`() {
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

  fun `test delete complete class`() {
    test(
      """
      |class Main {
        public static int test = 1;
      }
      """,
      ""
    )
  }

  fun `test delete partial class`() {
    test(
      """
      class Ma|in {
        public static int test = 1;
      }
      """,
      "class Ma"
    )
  }

  fun `test delete empty line`() {
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

  fun `test delete text end does nothing`() {
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

  fun `test multiple cursors`() {
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

  fun `test paste whats been killed will get back original text`() {
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

  fun `test paste whats been killed with multiple cursors will get back original text`() {
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
