package com.gitlab.lae.intellij.actions;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.CaretState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_PASTE_SIMPLE;
import static java.util.stream.Collectors.toList;

public final class PsiKillTest
        extends LightPlatformCodeInsightFixtureTestCase {

    public void testDeleteBracketContentWithoutDeletingClosingBracket() {
        new Tester()
                .initialInput("class A { int i = (1| - 1); }")
                .expectOutput("class A { int i = (1); }");
    }

    public void testDeleteAtEndBracketDoesNothing() {
        new Tester()
                .initialInput("class A { int i = (1|); }")
                .expectOutput("class A { int i = (1); }");
    }

    public void testDeleteAtStartBracketOfExpressionDeletesWholeExpression() {
        new Tester()
                .initialInput("class A { int i = |(1); }")
                .expectOutput("class A { int i = ; }");
    }

    public void testDeleteChar() {
        new Tester()
                .initialInput("class A { char a = '|a'; }")
                .expectOutput("class A { char a = ''; }");
        new Tester()
                .initialInput("class A { char a = |'a'; }")
                .expectOutput("class A { char a = ; }");
    }

    public void testDeleteCompleteClassBody() {
        new Tester()
                .initialInput("class A |{}")
                .expectOutput("class A ");
    }

    public void testDeleteJustBeforeClosingBraceInClassDoesNothing() {
        new Tester()
                .initialInput("class A {|}")
                .expectOutput("class A {}");
        new Tester()
                .initialInput("class A { int a = 0; |}")
                .expectOutput("class A { int a = 0; }");
    }

    public void testDeleteJustBeforeClosingBraceInMethodDoesNothing() {
        new Tester()
                .initialInput("class A { void bob() {|} }")
                .expectOutput("class A { void bob() {} }");
    }

    public void testDeleteJustBeforeClosingBraceInInitializerDoesNothing() {
        new Tester()
                .initialInput("class A {{|}}")
                .expectOutput("class A {{}}");
    }

    public void testDeleteJustBeforeClosingBraceInStaticInitializerDoesNothing() {
        new Tester()
                .initialInput("class A { static {|} }")
                .expectOutput("class A { static {} }");
    }

    public void testDeleteJustBeforeClosingBraceInStatementDoesNothing() {
        new Tester()
                .initialInput("class A { void bob() { {|} } }")
                .expectOutput("class A { void bob() { {} } }");
    }

    public void testDeleteFirstMethodInvocationParameter() {
        new Tester()
                .initialInput("class A {{ bob(|1, 2, 3); }}")
                .expectOutput("class A {{ bob(2, 3); }}");
        new Tester()
                .initialInput("class A {{ bob( |1, 2, 3); }}")
                .expectOutput("class A {{ bob( 2, 3); }}");
        new Tester()
                .initialInput("class A {{ bob(| 1, 2, 3); }}")
                .expectOutput("class A {{ bob(2, 3); }}");
    }

    public void testDeleteMiddleMethodInvocationParameter() {
        new Tester()
                .initialInput("class A {{ bob(1, |2, 3); }}")
                .expectOutput("class A {{ bob(1, 3); }}");
        new Tester()
                .initialInput("class A {{ bob(1, | 2, 3); }}")
                .expectOutput("class A {{ bob(1, 3); }}");
        new Tester()
                .initialInput("class A {{ bob(1,| 2, 3); }}")
                .expectOutput("class A {{ bob(1,3); }}");
    }

    public void testDeleteLastMethodInvocationParameter() {
        new Tester()
                .initialInput("class A {{ bob(1, 2|, 3); }}")
                .expectOutput("class A {{ bob(1, 2); }}");
        new Tester()
                .initialInput("class A {{ bob(1, 2 |, 3); }}")
                .expectOutput("class A {{ bob(1, 2 ); }}");
        new Tester()
                .initialInput("class A {{ bob(1, 2,| 3); }}")
                .expectOutput("class A {{ bob(1, 2,); }}");
        new Tester()
                .initialInput("class A {{ bob(1, 2, | 3); }}")
                .expectOutput("class A {{ bob(1, 2, ); }}");
    }

    public void testDeleteCompleteMethodInvocationParameterList() {
        new Tester()
                .initialInput("class A {{ bob|(1, 2, 3); }}")
                .expectOutput("class A {{ bob; }}");
    }

    public void testDeleteMethodInvocationJustBeforeClosingDoesNothing() {
        new Tester()
                .initialInput("class A {{ bob(|); }}")
                .expectOutput("class A {{ bob(); }}");
        new Tester()
                .initialInput("class A {{ bob(1, 2, 3|); }}")
                .expectOutput("class A {{ bob(1, 2, 3); }}");
    }

    public void testDeleteMethodInvocationWhitespace() {
        new Tester()
                .initialInput("class A {{ bob(| ); }}")
                .expectOutput("class A {{ bob(); }}");
    }

    public void testDeleteFirstArrayInitializationElement() {
        new Tester()
                .initialInput("class A { int[] arr = {|1, 2, 3}; }")
                .expectOutput("class A { int[] arr = {2, 3}; }");
    }

    public void testDeleteMiddleArrayInitialization() {
        new Tester()
                .initialInput("class A { int[] arr = {1, |2, 3}; }")
                .expectOutput("class A { int[] arr = {1, 3}; }");
    }

    public void testDeleteLastArrayInitialization() {
        new Tester()
                .initialInput("class A { int[] arr = {1, 2 |, 3}; }")
                .expectOutput("class A { int[] arr = {1, 2 }; }");
    }

    public void testDeleteCompleteArrayInitialization() {
        new Tester()
                .initialInput("class A { int[] arr = |{1, 2, 3}; }")
                .expectOutput("class A { int[] arr = ; }");
    }

    public void testDeleteArrayInitializationJustBeforeClosingDoesNothing() {
        new Tester()
                .initialInput("class A { int[] arr = {|}; }")
                .expectOutput("class A { int[] arr = {}; }");
        new Tester()
                .initialInput("class A { int[] arr = {1, 2, 3|}; }")
                .expectOutput("class A { int[] arr = {1, 2, 3}; }");
    }

    public void testDeleteCompleteIfCondition() {
        new Tester()
                .initialInput("class A {{ if (|true) {} }}")
                .expectOutput("class A {{ if () {} }}");
    }

    public void testDeletePartialIfCondition() {
        new Tester()
                .initialInput("class A {{ if (1| == 1) {} }}")
                .expectOutput("class A {{ if (1) {} }}");
    }

    public void testDeleteFirstCompleteIfConditionWithinMultipleConditions() {
        new Tester()
                .initialInput("class A {{ if (|1 == 1 && true) {} }}")
                .expectOutput("class A {{ if (1 && true) {} }}");
    }

    public void testDeleteLastCompleteIfConditionWithinMultipleConditions() {
        new Tester()
                .initialInput("class A {{ if (1 == 1| && true) {} }}")
                .expectOutput("class A {{ if (1 == 1) {} }}");
    }

    public void testDeleteMiddleCompleteIfConditionAfterOperatorWithinMultipleConditions() {
        new Tester()
                .initialInput("class A {{ if (1 == 1 && |true && false) {} }}")
                .expectOutput("class A {{ if (1 == 1 && false) {} }}");
    }

    public void testDeleteMiddleCompleteIfConditionBeforeOperatorWithinMultipleConditions() {
        new Tester()
                .initialInput("class A {{ if (1 == 1| && true && false) {} }}")
                .expectOutput("class A {{ if (1 == 1 && false) {} }}");
    }

    public void testDeletePartialIfConditionWithinMultipleConditions() {
        new Tester()
                .initialInput("class A {{ if (1| == 1 && true) {} }}")
                .expectOutput("class A {{ if (1 && true) {} }}");
    }

    public void testDeleteCompleteStringLiteral() {
        new Tester()
                .initialInput("class A { String text = |\"hello world\"; }")
                .expectOutput("class A { String text = ; }");
    }

    public void testDeletePartialStringLiteral() {
        new Tester()
                .initialInput("class A { String text = \"he|llo world\"; }")
                .expectOutput("class A { String text = \"he\"; }");
    }

    public void testDeleteStringLiteralFromStart() {
        new Tester()
                .initialInput("class A { String text = \"|hello world\"; }")
                .expectOutput("class A { String text = \"\"; }");
    }

    public void testDeleteStringJustBeforeEndQuoteDoesNothing() {
        new Tester()
                .initialInput("class A { String text = \"|\"; }")
                .expectOutput("class A { String text = \"\"; }");
        new Tester()
                .initialInput("class A { String text = \"abc|\"; }")
                .expectOutput("class A { String text = \"abc\"; }");
    }

    public void testDeleteSingleLineCommentFromLineStartWillDeleteWholeLineComment() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "|    // bob",
                        "    int bob;",
                        "}"
                )
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
                        "    // b|ob",
                        "    int bob;",
                        "}"
                )
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
                        "|        test1.forEach(s -> {",
                        "            if(test2.contains(s.toUpperCase())){",
                        "				System.out.println(s);",
                        "				System.out.println(s);",
                        "            }",
                        "		});",
                        "    }",
                        "}"
                )
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
                        "|    System",
                        "        .out",
                        "        .println();",
                        "    System.exit(0);",
                        "  }",
                        "}"
                )
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
                        "    Sys|tem",
                        "        .out",
                        "        .println();",
                        "    System.exit(0);",
                        "  }",
                        "}"
                )
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
                        "|    try {",
                        "      throw new RuntimeException();",
                        "    } catch (RuntimeException e) {",
                        "    }",
                        "  }",
                        "}"
                )
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
                        "    tr|y {",
                        "      throw new RuntimeException();",
                        "    } catch (RuntimeException e) {",
                        "    }",
                        "  }",
                        "}"
                )
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
                        "|    return",
                        "        1;",
                        "  }",
                        "}"
                )
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
                        "    retur|n",
                        "        1;",
                        "  }",
                        "}"
                )
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
                        "|  public static int test() {",
                        "    return 1;",
                        "  }",
                        "}"
                )
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
                        "  public stat|ic int test() {",
                        "    return",
                        "        1;",
                        "  }",
                        "}"
                )
                .expectOutput(
                        "class Main {",
                        "  public stat",
                        "}"
                );
    }

    public void testDeleteFirstParameter() {
        new Tester()
                .initialInput("class A { void test(|int a, int b, int c) {}}")
                .expectOutput("class A { void test(int b, int c) {}}");
    }

    public void testDeleteMiddleParameter() {
        new Tester()
                .initialInput("class A { void test(int a, |int b, int c) {}}")
                .expectOutput("class A { void test(int a, int c) {}}");
    }

    public void testDeleteLastParameter() {
        new Tester()
                .initialInput("class A { void test(int a, int b|, int c) {}}")
                .expectOutput("class A { void test(int a, int b) {}}");
        new Tester()
                .initialInput("class A { void test(int a, int b, |int c) {}}")
                .expectOutput("class A { void test(int a, int b, ) {}}");
    }

    public void testDeleteCompleteParameterList() {
        new Tester()
                .initialInput("class A { void test|(int a, int b, int c) {}}")
                .expectOutput("class A { void test {}}");
    }

    public void testDeleteFirstTypeParameter() {
        new Tester()
                .initialInput("class A<|A, B, C> {}")
                .expectOutput("class A<B, C> {}");
    }

    public void testDeleteMiddleTypeParameter() {
        new Tester()
                .initialInput("class A<A, |B, C> {}")
                .expectOutput("class A<A, C> {}");
        new Tester()
                .initialInput("class A<A|, B, C> {}")
                .expectOutput("class A<A, C> {}");
    }

    public void testDeleteLastTypeParameter() {
        new Tester()
                .initialInput("class A<A, B|, C> {}")
                .expectOutput("class A<A, B> {}");
        new Tester()
                .initialInput("class A<A, B,| C> {}")
                .expectOutput("class A<A, B,> {}");
    }

    public void testDeleteCompleteTypeParameterList() {
        new Tester()
                .initialInput("class A|<A, B, C> {}")
                .expectOutput("class A {}");
    }

    public void testArgumentListJustBeforeClosingBracketDoesNothing() {
        new Tester()
                .initialInput("class A { void test(int i|) {} }")
                .expectOutput("class A { void test(int i) {} }");

        new Tester()
                .initialInput(
                        "class Main {",
                        "  void test(|) {",
                        "    int i = 0;",
                        "  }",
                        "}"
                )
                .expectOutput(
                        "class Main {",
                        "  void test() {",
                        "    int i = 0;",
                        "  }",
                        "}"
                );
    }

    public void testDeleteCompleteConstructor() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "  int i;",
                        "|  public Main() {",
                        "    i = 1;",
                        "  }",
                        "}"
                )
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
                        "  public Ma|in() {",
                        "    i = 1;",
                        "  }",
                        "}"
                )
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
                        "|  {",
                        "    i = 1;",
                        "  }",
                        "}"
                )
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
                        "  stati|c {",
                        "    i = 1;",
                        "  }",
                        "}"
                )
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
                        "|  public static int",
                        "    test = 1;",
                        "}"
                )
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
                        "  public stat|ic int",
                        "    test = 1;",
                        "}"
                )
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
                        "|  public static class Test {",
                        "    int i;",
                        "  }",
                        "}"
                )
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
                        "  public stat|ic class Test {",
                        "    int i;",
                        "  }",
                        "}"
                )
                .expectOutput(
                        "class Main {",
                        "  public stat",
                        "}"
                );
    }

    public void testDeleteCompleteClass() {
        new Tester()
                .initialInput(
                        "|class Main {",
                        "  public static int test = 1;",
                        "}"
                )
                .expectOutput("");
    }

    public void testDeletePartialClass() {
        new Tester()
                .initialInput(
                        "class Ma|in {",
                        "  public static int test = 1;",
                        "}"
                )
                .expectOutput("class Ma");
    }

    public void testDeleteEmptyLine() {
        new Tester()
                .initialInput(
                        "class Main {",
                        "|",
                        "  public static int test = 1;",
                        "}"
                )
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
                        "}|"
                )
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
                        "|  public static int test1 = 1;",
                        "  public static int test2 = 2;",
                        "|  public static int test3() {",
                        "    return 3;",
                        "  }",
                        "}"
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
                        "|  public static int test() {",
                        "    return 1;",
                        "  }",
                        "}"
                )
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
                        "|  public static int test1 = 1;",
                        "  public static int test2 = 2;",
                        "|  public static int test3() {",
                        "    return 3;",
                        "  }",
                        "}"
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

    private final class Tester {
        private boolean pasteAfterKill;
        private String initialText;
        private final List<LogicalPosition> initialCarets = new ArrayList<>();

        Tester doPasteAfterKill() {
            this.pasteAfterKill = true;
            return this;
        }

        Tester initialInput(String... linesWithCursors) {
            String[] lines = linesWithCursors.clone();
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                for (int j = 0; j > -1 && j < line.length(); ) {
                    j = line.indexOf('|', j);
                    if (j > -1) {
                        line = line.replaceFirst("\\|", "");
                        initialCarets.add(new LogicalPosition(i, j));
                    }
                }
                lines[i] = line;
            }
            initialText = String.join("\n", lines);
            return this;
        }

        void expectOutput(String... expectedLines) {
            myFixture.configureByText(JavaFileType.INSTANCE, initialText);
            Editor editor = myFixture.getEditor();
            editor.getCaretModel().setCaretsAndSelections(
                    initialCarets.stream()
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
