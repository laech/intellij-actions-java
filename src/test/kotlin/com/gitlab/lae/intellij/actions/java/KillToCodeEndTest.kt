package com.gitlab.lae.intellij.actions.java

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_PASTE_SIMPLE
import com.intellij.openapi.application.ApplicationManager.getApplication
import com.intellij.openapi.editor.CaretState
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class KillToCodeEndTest : LightPlatformCodeInsightFixtureTestCase() {

  @Before
  public override fun setUp() {
    super.setUp()
  }

  @After
  public override fun tearDown() {
    super.tearDown()
  }

  @Test
  fun `delete bracket content without deleting closing bracket`() {
    test(
      "class A { int i = (1| - 1); }",
      "class A { int i = (1); }"
    )
  }

  @Test
  fun `delete at end bracket does nothing`() {
    test(
      "class A { int i = (1|); }",
      "class A { int i = (1); }"
    )
  }

  @Test
  fun `delete at start bracket of expression deletes whole expression`() {
    test(
      "class A { int i = |(1); }",
      "class A { int i = ; }"
    )
  }

  @Test
  fun `delete char`() {
    test(
      "class A { char a = '|a'; }",
      "class A { char a = ''; }"
    )
    test(
      "class A { char a = |'a'; }",
      "class A { char a = ; }"
    )
  }

  @Test
  fun `delete complete class body`() {
    test(
      "class A |{}",
      "class A "
    )
  }

  @Test
  fun `delete just before closing brace in class does nothing`() {
    test(
      "class A {|}",
      "class A {}"
    )
    test(
      "class A { int a = 0; |}",
      "class A { int a = 0; }"
    )
  }

  @Test
  fun `delete just before closing brace in method does nothing`() {
    test(
      "class A { void bob() {|} }",
      "class A { void bob() {} }"
    )
  }

  @Test
  fun `delete just before closing brace in initializer does nothing`() {
    test(
      "class A {{|}}",
      "class A {{}}"
    )
  }

  @Test
  fun `delete just before closing brace in static initializer does nothing`() {
    test(
      "class A { static {|} }",
      "class A { static {} }"
    )
  }

  @Test
  fun `delete just before closing brace in statement does nothing`() {
    test(
      "class A { void bob() { {|} } }",
      "class A { void bob() { {} } }"
    )
  }

  @Test
  fun `delete first method invocation parameter`() {
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

  @Test
  fun `delete middle method invocation parameter`() {
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

  @Test
  fun `delete last method invocation parameter`() {
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

  @Test
  fun `delete complete method invocation parameter list`() {
    test(
      "class A {{ bob|(1, 2, 3); }}",
      "class A {{ bob; }}"
    )
  }

  @Test
  fun `delete method invocation just before closing does nothing`() {
    test(
      "class A {{ bob(|); }}",
      "class A {{ bob(); }}"
    )
    test(
      "class A {{ bob(1, 2, 3|); }}",
      "class A {{ bob(1, 2, 3); }}"
    )
  }

  @Test
  fun `delete method invocation whitespace`() {
    test(
      "class A {{ bob(| ); }}",
      "class A {{ bob(); }}"
    )
  }

  @Test
  fun `delete first array initialization element`() {
    test(
      "class A { int[] arr = {|1, 2, 3}; }",
      "class A { int[] arr = {2, 3}; }"
    )
  }

  @Test
  fun `delete middle array initialization`() {
    test(
      "class A { int[] arr = {1, |2, 3}; }",
      "class A { int[] arr = {1, 3}; }"
    )
  }

  @Test
  fun `delete last array initialization`() {
    test(
      "class A { int[] arr = {1, 2 |, 3}; }",
      "class A { int[] arr = {1, 2 }; }"
    )
  }

  @Test
  fun `delete complete array initialization`() {
    test(
      "class A { int[] arr = |{1, 2, 3}; }",
      "class A { int[] arr = ; }"
    )
  }

  @Test
  fun `delete array initialization just before closing does nothing`() {
    test(
      "class A { int[] arr = {|}; }",
      "class A { int[] arr = {}; }"
    )
    test(
      "class A { int[] arr = {1, 2, 3|}; }",
      "class A { int[] arr = {1, 2, 3}; }"
    )
  }

  @Test
  fun `delete complete if condition`() {
    test(
      "class A {{ if (|true) {} }}",
      "class A {{ if () {} }}"
    )
  }

  @Test
  fun `delete partial if condition`() {
    test(
      "class A {{ if (1| == 1) {} }}",
      "class A {{ if (1) {} }}"
    )
  }

  @Test
  fun `delete first complete if condition within multiple conditions`() {
    test(
      "class A {{ if (|1 == 1 && true) {} }}",
      "class A {{ if (1 && true) {} }}"
    )
  }

  @Test
  fun `delete last complete if condition within multiple conditions`() {
    test(
      "class A {{ if (1 == 1| && true) {} }}",
      "class A {{ if (1 == 1) {} }}"
    )
  }

  @Test
  fun `delete middle complete if condition after operator within multiple conditions`() {
    test(
      "class A {{ if (1 == 1 && |true && false) {} }}",
      "class A {{ if (1 == 1 && false) {} }}"
    )
  }

  @Test
  fun `delete middle complete if condition before operator within multiple conditions`() {
    test(
      "class A {{ if (1 == 1| && true && false) {} }}",
      "class A {{ if (1 == 1 && false) {} }}"
    )
  }

  @Test
  fun `delete partial if condition within multiple conditions`() {
    test(
      "class A {{ if (1| == 1 && true) {} }}",
      "class A {{ if (1 && true) {} }}"
    )
  }

  @Test
  fun `delete if negation`() {
    test(
      "class A {{ if (|!true) {} }}",
      "class A {{ if () {} }}"
    )
  }

  @Test
  fun `delete complete string literal`() {
    test(
      "class A { String text = |\"hello world\"; }",
      "class A { String text = ; }"
    )
  }

  @Test
  fun `delete partial string literal`() {
    test(
      "class A { String text = \"he|llo world\"; }",
      "class A { String text = \"he\"; }"
    )
  }

  @Test
  fun `delete string literal from start`() {
    test(
      "class A { String text = \"|hello world\"; }",
      "class A { String text = \"\"; }"
    )
  }

  @Test
  fun `delete string just before end quote does nothing`() {
    test(
      "class A { String text = \"|\"; }",
      "class A { String text = \"\"; }"
    )
    test(
      "class A { String text = \"abc|\"; }",
      "class A { String text = \"abc\"; }"
    )
  }

  @Test
  fun `delete single line comment from line start will delete whole line comment`() {
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

  @Test
  fun `delete single line comment from middle will delete rest of the line`() {
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

  @Test
  fun `delete javadoc description to line end`() {
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

  @Test
  fun `delete javadoc entire tag`() {
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

  @Test
  fun `delete mixed tabs and spaces`() {
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

  @Test
  fun `delete complete statement`() {
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

  @Test
  fun `delete partial statement`() {
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

  @Test
  fun `delete complete try catch`() {
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

  @Test
  fun `delete partial try catch`() {
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

  @Test
  fun `delete complete return statement`() {
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

  @Test
  fun `delete partial return statement`() {
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

  @Test
  fun `delete complete method`() {
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

  @Test
  fun `delete partial method`() {
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

  @Test
  fun `delete first parameter`() {
    test(
      "class A { void test(|int a, int b, int c) {}}",
      "class A { void test(int b, int c) {}}"
    )
  }

  @Test
  fun `delete middle parameter`() {
    test(
      "class A { void test(int a, |int b, int c) {}}",
      "class A { void test(int a, int c) {}}"
    )
  }

  @Test
  fun `delete last parameter`() {
    test(
      "class A { void test(int a, int b|, int c) {}}",
      "class A { void test(int a, int b) {}}"
    )
    test(
      "class A { void test(int a, int b, |int c) {}}",
      "class A { void test(int a, int b, ) {}}"
    )
  }

  @Test
  fun `delete complete parameter list`() {
    test(
      "class A { void test|(int a, int b, int c) {}}",
      "class A { void test {}}"
    )
  }

  @Test
  fun `delete first type parameter`() {
    test(
      "class A<|A, B, C> {}",
      "class A<B, C> {}"
    )
  }

  @Test
  fun `delete middle type parameter`() {
    test(
      "class A<A, |B, C> {}",
      "class A<A, C> {}"
    )
    test(
      "class A<A|, B, C> {}",
      "class A<A, C> {}"
    )
  }

  @Test
  fun `delete last type parameter`() {
    test(
      "class A<A, B|, C> {}",
      "class A<A, B> {}"
    )
    test(
      "class A<A, B,| C> {}",
      "class A<A, B,> {}"
    )
  }

  @Test
  fun `delete complete type parameter list`() {
    test(
      "class A|<A, B, C> {}",
      "class A {}"
    )
  }

  @Test
  fun `argument list just before closing bracket does nothing`() {
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

  @Test
  fun `delete complete constructor`() {
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

  @Test
  fun `delete partial constructor`() {
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

  @Test
  fun `delete complete initializer`() {
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

  @Test
  fun `delete partial initializer`() {
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

  @Test
  fun `delete complete field`() {
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

  @Test
  fun `delete partial field`() {
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

  @Test
  fun `delete complete inner class`() {
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

  @Test
  fun `delete partial inner class`() {
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

  @Test
  fun `delete complete class`() {
    test(
      """
      |class Main {
        public static int test = 1;
      }
      """,
      ""
    )
  }

  @Test
  fun `delete partial class`() {
    test(
      """
      class Ma|in {
        public static int test = 1;
      }
      """,
      "class Ma"
    )
  }

  @Test
  fun `delete empty line`() {
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

  @Test
  fun `delete text end does nothing`() {
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

  @Test
  fun `multiple cursors`() {
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

  @Test
  fun `paste whats been killed will get back original text`() {
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

  @Test
  fun `paste whats been killed with multiple cursors will get back original text`() {
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

  @Test
  fun `delete annotation attribute`() {
    test(
      """
      @MyAnnotation(|String.class)
      class Main {}
      """,
      """
      @MyAnnotation()
      class Main {}
      """
    )
  }

  private fun test(
    input: String,
    output: String,
    pasteAfterKill: Boolean = false
  ) {
    val (initialText, initialCarets) = init(input)
    getApplication().invokeAndWait {
      action(initialText, initialCarets)
      if (pasteAfterKill) {
        myFixture.performEditorAction(ACTION_EDITOR_PASTE_SIMPLE)
      }
      assertEquals(output.trimIndent(), myFixture.editor.document.text)
    }
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
