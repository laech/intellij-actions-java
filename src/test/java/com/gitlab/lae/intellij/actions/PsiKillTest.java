package com.gitlab.lae.intellij.actions;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.CaretState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

import java.util.Arrays;

import static com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_PASTE_SIMPLE;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;

public final class PsiKillTest
        extends LightPlatformCodeInsightFixtureTestCase {

    public void testDeleteSingleLineCommentFromLineStartWillDeleteWholeLineComment() {
        test(join("\n",
                "class Main {",
                "    // bob",
                "    int bob;",
                "}"
        ), join("\n",
                "class Main {",
                "",
                "    int bob;",
                "}"
        ), new LogicalPosition(1, 0));
    }

    public void testDeleteSingleLineCommentFromMiddleWillDeleteRestOfTheLine() {
        test(join("\n",
                "class Main {",
                "    // bob",
                "    int bob;",
                "}"
        ), join("\n",
                "class Main {",
                "    // b",
                "    int bob;",
                "}"
        ), new LogicalPosition(1, 8));
    }

    public void testDeleteMixedTabsAndSpaces() {
        test(join("\n",
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
        ), join("\n",
                "class Main {",
                "    public void test() {",
                "        Collection<String> test1 = null;",
                "        Collection<String> test2 = null;",
                "",
                "    }",
                "}"
        ), new LogicalPosition(4, 0));
    }

    public void testDeleteCompleteStatement() {
        test(join("\n",
                "class Main {",
                "  public static void main(String[] args) {",
                "    System",
                "        .out",
                "        .println();",
                "    System.exit(0);",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "  public static void main(String[] args) {",
                "",
                "    System.exit(0);",
                "  }",
                "}"
        ), new LogicalPosition(2, 0));
    }

    public void testDeletePartialStatement() {
        test(join("\n",
                "class Main {",
                "  public static void main(String[] args) {",
                "    System",
                "        .out",
                "        .println();",
                "    System.exit(0);",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "  public static void main(String[] args) {",
                "    Sys",
                "    System.exit(0);",
                "  }",
                "}"
        ), new LogicalPosition(2, 7));
    }

    public void testDeleteCompleteTryCatch() {
        test(join("\n",
                "class Main {",
                "  public static int test() {",
                "    try {",
                "      throw new RuntimeException();",
                "    } catch (RuntimeException e) {",
                "    }",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "  public static int test() {",
                "",
                "  }",
                "}"
        ), new LogicalPosition(2, 0));
    }

    public void testDeletePartialTryCatch() {
        test(join("\n",
                "class Main {",
                "  public static int test() {",
                "    try {",
                "      throw new RuntimeException();",
                "    } catch (RuntimeException e) {",
                "    }",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "  public static int test() {",
                "    tr",
                "  }",
                "}"
        ), new LogicalPosition(2, 6));
    }

    public void testDeleteCompleteReturnStatement() {
        test(join("\n",
                "class Main {",
                "  public static int test() {",
                "    return",
                "        1;",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "  public static int test() {",
                "",
                "  }",
                "}"
        ), new LogicalPosition(2, 0));
    }

    public void testDeletePartialReturnStatement() {
        test(join("\n",
                "class Main {",
                "  public static int test() {",
                "    return",
                "        1;",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "  public static int test() {",
                "    retur",
                "  }",
                "}"
        ), new LogicalPosition(2, 9));
    }

    public void testDeleteCompleteMethod() {
        test(join("\n",
                "class Main {",
                "  public static int test() {",
                "    return 1;",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "",
                "}"
        ), new LogicalPosition(1, 0));
    }

    public void testDeletePartialMethod() {
        test(join("\n",
                "class Main {",
                "  public static int test() {",
                "    return",
                "        1;",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "  public stat",
                "}"
        ), new LogicalPosition(1, 13));
    }

    public void testDeleteCompleteArgumentList() {
        test(join("\n",
                "class Main {",
                "  void test(int a, int b, int c) {}",
                "}"
        ), join("\n",
                "class Main {",
                "  void test() {}",
                "}"
        ), new LogicalPosition(1, 12));
    }

    public void testDeletePartialArgumentList() {
        test(join("\n",
                "class Main {",
                "  void test(int a, int b, int c) {}",
                "}"
        ), join("\n",
                "class Main {",
                "  void test(int ) {}",
                "}"
        ), new LogicalPosition(1, 16));
    }

    public void testDeleteEmptyArgumentListDeleteNextBlock() {
        test(join("\n",
                "class Main {",
                "  void test() {",
                "    int i = 0;",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "  void test(",
                "}"
        ), new LogicalPosition(1, 12));
    }

    public void testDeleteCompleteConstructor() {
        test(join("\n",
                "class Main {",
                "  int i;",
                "  public Main() {",
                "    i = 1;",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "  int i;",
                "",
                "}"
        ), new LogicalPosition(2, 0));
    }

    public void testDeletePartialConstructor() {
        test(join("\n",
                "class Main {",
                "  int i;",
                "  public Main() {",
                "    i = 1;",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "  int i;",
                "  public Ma",
                "}"
        ), new LogicalPosition(2, 11));
    }

    public void testDeleteCompleteInitializer() {
        test(join("\n",
                "class Main {",
                "  int i;",
                "  {",
                "    i = 1;",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "  int i;",
                "",
                "}"
        ), new LogicalPosition(2, 0));
    }

    public void testDeletePartialInitializer() {
        test(join("\n",
                "class Main {",
                "  static int i;",
                "  static {",
                "    i = 1;",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "  static int i;",
                "  stati",
                "}"
        ), new LogicalPosition(2, 7));
    }

    public void testDeleteCompleteField() {
        test(join("\n",
                "class Main {",
                "  public static int",
                "    test = 1;",
                "}"
        ), join("\n",
                "class Main {",
                "",
                "}"
        ), new LogicalPosition(1, 0));
    }

    public void testDeletePartialField() {
        test(join("\n",
                "class Main {",
                "  public static int",
                "    test = 1;",
                "}"
        ), join("\n",
                "class Main {",
                "  public stat",
                "}"
        ), new LogicalPosition(1, 13));
    }

    public void testDeleteCompleteInnerClass() {
        test(join("\n",
                "class Main {",
                "  public static class Test {",
                "    int i;",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "",
                "}"
        ), new LogicalPosition(1, 0));
    }

    public void testDeletePartialInnerClass() {
        test(join("\n",
                "class Main {",
                "  public static class Test {",
                "    int i;",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "  public stat",
                "}"
        ), new LogicalPosition(1, 13));
    }

    public void testDeleteCompleteClass() {
        test(join("\n",
                "class Main {",
                "  public static int test = 1;",
                "}"),
                "",
                new LogicalPosition(0, 0));
    }

    public void testDeletePartialClass() {
        test(join("\n",
                "class Main {",
                "  public static int test = 1;",
                "}"
        ), join("\n",
                "class Ma"
        ), new LogicalPosition(0, 8));
    }

    public void testDeleteEmptyLine() {
        test(join("\n",
                "class Main {",
                "",
                "  public static int test = 1;",
                "}"
        ), join("\n",
                "class Main {",
                "  public static int test = 1;",
                "}"
        ), new LogicalPosition(1, 0));
    }

    public void testDeleteTextEndDoesNothing() {
        test(join("\n",
                "class Main {",
                "  public static int test = 1;",
                "}"
        ), join("\n",
                "class Main {",
                "  public static int test = 1;",
                "}"
        ), new LogicalPosition(2, 1));
    }

    public void testMultipleCursors() {
        test(join("\n",
                "class Main {",
                "  public static int test1 = 1;",
                "  public static int test2 = 2;",
                "  public static int test3() {",
                "    return 3;",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "",
                "  public static int test2 = 2;",
                "",
                "}"
        ), new LogicalPosition(1, 0), new LogicalPosition(3, 0));
    }

    public void testPaste() {
        test(true, join("\n",
                "class Main {",
                "  public static int test() {",
                "    return 1;",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "  public static int test() {",
                "    return 1;",
                "  }",
                "}"
        ), new LogicalPosition(1, 0));
    }

    public void testPasteMultipleCursors() {
        test(true, join("\n",
                "class Main {",
                "  public static int test1 = 1;",
                "  public static int test2 = 2;",
                "  public static int test3() {",
                "    return 3;",
                "  }",
                "}"
        ), join("\n",
                "class Main {",
                "  public static int test1 = 1;",
                "  public static int test2 = 2;",
                "  public static int test3() {",
                "    return 3;",
                "  }",
                "}"
        ), new LogicalPosition(1, 0), new LogicalPosition(3, 0));
    }

    private void test(
            String initialText,
            String expectedText,
            LogicalPosition... initialCaretPositions
    ) {
        test(false, initialText, expectedText, initialCaretPositions);
    }

    private void test(
            boolean paste,
            String initialText,
            String expectedText,
            LogicalPosition... initialCaretPositions
    ) {
        myFixture.configureByText(JavaFileType.INSTANCE, initialText);
        Editor editor = myFixture.getEditor();
        editor.getCaretModel().setCaretsAndSelections(
                Arrays.stream(initialCaretPositions)
                        .map(it -> new CaretState(it, it, it))
                        .collect(toList()));

        myFixture.performEditorAction("com.gitlab.lae.intellij.actions.PsiKill");
        if (paste) {
            myFixture.performEditorAction(ACTION_EDITOR_PASTE_SIMPLE);
        }
        assertEquals(expectedText, editor.getDocument().getText());
    }
}
