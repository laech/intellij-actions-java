package com.gitlab.lae.intellij.actions;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.CaretState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import java.util.Arrays;

import static com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_PASTE_SIMPLE;
import static java.util.stream.Collectors.toList;

public final class PsiKillTest
        extends LightPlatformCodeInsightFixtureTestCase {

    public void testDeleteArrayInitializationFromStart() {
        new Tester()
                .initialCarets(lineColumn(0, 23))
                .initialInput("class A { int[] arr = {1, 2, 3}; }")
                .expectOutput("class A { int[] arr = {}; }");
    }

    public void testDeletePartialArrayInitialization() {
        new Tester()
                .initialCarets(lineColumn(1, 24))
                .initialInput("class A { int[] arr = {1, 2, 3}; }")
                .expectOutput("class A { int[] arr = {1}; }");
    }

    public void testDeleteCompleteArrayInitialization() {
        new Tester()
                .initialCarets(lineColumn(1, 22))
                .initialInput("class A { int[] arr = {1, 2, 3}; }")
                .expectOutput("class A { int[] arr = ; }");
    }

    public void testDeleteCompleteIfCondition() {
        new Tester()
                .initialCarets(lineColumn(0, 15))
                .initialInput("class A {{ if (1 == 1) {} }}")
                .expectOutput("class A {{ if () {} }}");
    }

    public void testDeletePartialIfCondition() {
        new Tester()
                .initialCarets(lineColumn(0, 16))
                .initialInput("class A {{ if (1 == 1) {} }}")
                .expectOutput("class A {{ if (1) {} }}");
    }

    public void testDeleteCompleteStringLiteral() {
        new Tester()
                .initialCarets(lineColumn(0, 24))
                .initialInput("class A { String text = \"hello world\"; }")
                .expectOutput("class A { String text = ; }");
    }

    public void testDeletePartialStringLiteral() {
        new Tester()
                .initialCarets(lineColumn(0, 27))
                .initialInput("class A { String text = \"hello world\"; }")
                .expectOutput("class A { String text = \"he\"; }");
    }

    public void testDeleteStringLiteralFromStart() {
        new Tester()
                .initialCarets(lineColumn(0, 25))
                .initialInput("class A { String text = \"hello world\"; }")
                .expectOutput("class A { String text = \"\"; }");
    }

    public void testDeleteSingleLineCommentFromLineStartWillDeleteWholeLineComment() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "    // bob",
                        "    int bob;",
                        "}"
                )
                .initialCarets(lineColumn(1, 0))
                .expectOutput(
                        "class Main {",
                        "",
                        "    int bob;",
                        "}"
                );
    }

    public void testDeleteSingleLineCommentFromMiddleWillDeleteRestOfTheLine() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "    // bob",
                        "    int bob;",
                        "}"
                )
                .initialCarets(lineColumn(1, 8))
                .expectOutput(
                        "class Main {",
                        "    // b",
                        "    int bob;",
                        "}"
                );
    }

    public void testDeleteMixedTabsAndSpaces() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "    public void test() {",
                        "        Collection<String> test1 = null;",
                        "        Collection<String> test2 = null;",
                        "        test1.forEach(s -> {",
                        "            if(test2.contains(s.toUpperCase())){",
                        "				System.out.println(s);",
                        "				System.out.println(s);",
                        "            }",
                        "		});",
                        "    }",
                        "}"
                )
                .initialCarets(lineColumn(4, 0))
                .expectOutput(
                        "class Main {",
                        "    public void test() {",
                        "        Collection<String> test1 = null;",
                        "        Collection<String> test2 = null;",
                        "",
                        "    }",
                        "}"
                );
    }

    public void testDeleteCompleteStatement() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static void main(String[] args) {",
                        "    System",
                        "        .out",
                        "        .println();",
                        "    System.exit(0);",
                        "  }",
                        "}"
                )
                .initialCarets(lineColumn(2, 0))
                .expectOutput(
                        "class Main {",
                        "  public static void main(String[] args) {",
                        "",
                        "    System.exit(0);",
                        "  }",
                        "}"
                );
    }

    public void testDeletePartialStatement() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static void main(String[] args) {",
                        "    System",
                        "        .out",
                        "        .println();",
                        "    System.exit(0);",
                        "  }",
                        "}"
                )
                .initialCarets(lineColumn(2, 7))
                .expectOutput(
                        "class Main {",
                        "  public static void main(String[] args) {",
                        "    Sys",
                        "    System.exit(0);",
                        "  }",
                        "}"
                );
    }

    public void testDeleteCompleteTryCatch() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static int test() {",
                        "    try {",
                        "      throw new RuntimeException();",
                        "    } catch (RuntimeException e) {",
                        "    }",
                        "  }",
                        "}"
                )
                .initialCarets(lineColumn(2, 0))
                .expectOutput(
                        "class Main {",
                        "  public static int test() {",
                        "",
                        "  }",
                        "}"
                );
    }

    public void testDeletePartialTryCatch() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static int test() {",
                        "    try {",
                        "      throw new RuntimeException();",
                        "    } catch (RuntimeException e) {",
                        "    }",
                        "  }",
                        "}"
                )
                .initialCarets(lineColumn(2, 6))
                .expectOutput(
                        "class Main {",
                        "  public static int test() {",
                        "    tr",
                        "  }",
                        "}"
                );
    }

    public void testDeleteCompleteReturnStatement() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static int test() {",
                        "    return",
                        "        1;",
                        "  }",
                        "}"
                )
                .initialCarets(lineColumn(2, 0))
                .expectOutput(
                        "class Main {",
                        "  public static int test() {",
                        "",
                        "  }",
                        "}"
                );
    }

    public void testDeletePartialReturnStatement() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static int test() {",
                        "    return",
                        "        1;",
                        "  }",
                        "}"
                )
                .initialCarets(lineColumn(2, 9))
                .expectOutput(
                        "class Main {",
                        "  public static int test() {",
                        "    retur",
                        "  }",
                        "}"
                );
    }

    public void testDeleteCompleteMethod() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static int test() {",
                        "    return 1;",
                        "  }",
                        "}"
                )
                .initialCarets(lineColumn(1, 0))
                .expectOutput(
                        "class Main {",
                        "",
                        "}"
                );
    }

    public void testDeletePartialMethod() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static int test() {",
                        "    return",
                        "        1;",
                        "  }",
                        "}"
                )
                .initialCarets(lineColumn(1, 13))
                .expectOutput(
                        "class Main {",
                        "  public stat",
                        "}"
                );
    }

    public void testDeleteCompleteArgumentList() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  void test(int a, int b, int c) {}",
                        "}"
                )
                .initialCarets(lineColumn(1, 12))
                .expectOutput(
                        "class Main {",
                        "  void test() {}",
                        "}"
                );
    }

    public void testDeletePartialArgumentList() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  void test(int a, int b, int c) {}",
                        "}"
                )
                .initialCarets(lineColumn(1, 16))
                .expectOutput(
                        "class Main {",
                        "  void test(int ) {}",
                        "}"
                );
    }

    public void testDeleteEmptyArgumentListDeleteNextBlock() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  void test() {",
                        "    int i = 0;",
                        "  }",
                        "}"
                )
                .initialCarets(lineColumn(1, 12))
                .expectOutput(
                        "class Main {",
                        "  void test(",
                        "}"
                );
    }

    public void testDeleteCompleteConstructor() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  int i;",
                        "  public Main() {",
                        "    i = 1;",
                        "  }",
                        "}"
                )
                .initialCarets(lineColumn(2, 0))
                .expectOutput(
                        "class Main {",
                        "  int i;",
                        "",
                        "}"
                );
    }

    public void testDeletePartialConstructor() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  int i;",
                        "  public Main() {",
                        "    i = 1;",
                        "  }",
                        "}"
                )
                .initialCarets(lineColumn(2, 11))
                .expectOutput(
                        "class Main {",
                        "  int i;",
                        "  public Ma",
                        "}"
                );
    }

    public void testDeleteCompleteInitializer() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  int i;",
                        "  {",
                        "    i = 1;",
                        "  }",
                        "}"
                )
                .initialCarets(lineColumn(2, 0))
                .expectOutput(
                        "class Main {",
                        "  int i;",
                        "",
                        "}"
                );
    }

    public void testDeletePartialInitializer() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  static int i;",
                        "  static {",
                        "    i = 1;",
                        "  }",
                        "}"
                )
                .initialCarets(lineColumn(2, 7))
                .expectOutput(
                        "class Main {",
                        "  static int i;",
                        "  stati",
                        "}"
                );
    }

    public void testDeleteCompleteField() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static int",
                        "    test = 1;",
                        "}"
                )
                .initialCarets(lineColumn(1, 0))
                .expectOutput(
                        "class Main {",
                        "",
                        "}"
                );
    }

    public void testDeletePartialField() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static int",
                        "    test = 1;",
                        "}"
                )
                .initialCarets(lineColumn(1, 13))
                .expectOutput(
                        "class Main {",
                        "  public stat",
                        "}"
                );
    }

    public void testDeleteCompleteInnerClass() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static class Test {",
                        "    int i;",
                        "  }",
                        "}"
                )
                .initialCarets(lineColumn(1, 0))
                .expectOutput(
                        "class Main {",
                        "",
                        "}"
                );
    }

    public void testDeletePartialInnerClass() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static class Test {",
                        "    int i;",
                        "  }",
                        "}"
                )
                .initialCarets(lineColumn(1, 13))
                .expectOutput(
                        "class Main {",
                        "  public stat",
                        "}"
                );
    }

    public void testDeleteCompleteClass() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static int test = 1;",
                        "}"
                )
                .initialCarets(lineColumn(0, 0))
                .expectOutput("");
    }

    public void testDeletePartialClass() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static int test = 1;",
                        "}"
                )
                .initialCarets(lineColumn(0, 8))
                .expectOutput("class Ma");
    }

    public void testDeleteEmptyLine() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "",
                        "  public static int test = 1;",
                        "}"
                )
                .initialCarets(lineColumn(1, 0))
                .expectOutput(
                        "class Main {",
                        "  public static int test = 1;",
                        "}"
                );
    }

    public void testDeleteTextEndDoesNothing() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static int test = 1;",
                        "}"
                )
                .initialCarets(lineColumn(2, 1))
                .expectOutput(
                        "class Main {",
                        "  public static int test = 1;",
                        "}"
                );
    }

    public void testMultipleCursors() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static int test1 = 1;",
                        "  public static int test2 = 2;",
                        "  public static int test3() {",
                        "    return 3;",
                        "  }",
                        "}"
                )
                .initialCarets(
                        lineColumn(1, 0),
                        lineColumn(3, 0)
                )
                .expectOutput(
                        "class Main {",
                        "",
                        "  public static int test2 = 2;",
                        "",
                        "}"
                );
    }

    public void testPasteWhatsBeenKilledWillGetBackOriginalText() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static int test() {",
                        "    return 1;",
                        "  }",
                        "}"
                )
                .initialCarets(lineColumn(1, 0))
                .doPasteAfterKill()
                .expectOutput(
                        "class Main {",
                        "  public static int test() {",
                        "    return 1;",
                        "  }",
                        "}"
                );
    }

    public void testPasteWhatsBeenKilledWithMultipleCursorsWillGetBackOriginalText() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  public static int test1 = 1;",
                        "  public static int test2 = 2;",
                        "  public static int test3() {",
                        "    return 3;",
                        "  }",
                        "}"
                )
                .initialCarets(
                        lineColumn(1, 0),
                        lineColumn(3, 0)
                )
                .doPasteAfterKill()
                .expectOutput(
                        "class Main {",
                        "  public static int test1 = 1;",
                        "  public static int test2 = 2;",
                        "  public static int test3() {",
                        "    return 3;",
                        "  }",
                        "}"
                );
    }

    private static LogicalPosition lineColumn(int line, int column) {
        return new LogicalPosition(line, column);
    }

    private final class Tester {
        private boolean pasteAfterKill;
        private String initialText;
        private LogicalPosition[] initialCarets;

        Tester doPasteAfterKill() {
            this.pasteAfterKill = true;
            return this;
        }

        Tester initialInput(String... lines) {
            initialText = String.join("\n", lines);
            return this;
        }

        Tester initialCarets(LogicalPosition... positions) {
            initialCarets = positions;
            return this;
        }

        void expectOutput(String... expectedLines) {
            myFixture.configureByText(JavaFileType.INSTANCE, initialText);
            Editor editor = myFixture.getEditor();
            editor.getCaretModel().setCaretsAndSelections(
                    Arrays.stream(initialCarets)
                            .map(it -> new CaretState(it, it, it))
                            .collect(toList()));

            myFixture.performEditorAction(
                    "com.gitlab.lae.intellij.actions.PsiKill");
            if (pasteAfterKill) {
                myFixture.performEditorAction(ACTION_EDITOR_PASTE_SIMPLE);
            }
            String expected = String.join("\n", expectedLines);
            assertEquals(expected, editor.getDocument().getText());
        }
    }
}
