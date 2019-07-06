package com.gitlab.lae.intellij.actions;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.CaretState;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import kotlin.Triple;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.openapi.fileTypes.FileTypes.PLAIN_TEXT;
import static java.util.stream.Collectors.toList;

public final class RectangleCreateTest
        extends LightPlatformCodeInsightFixtureTestCase {

    public void testSingleLineSelectionRemainsUnchanged() {
        new Tester()
                .text("hello")
                .addInitialSelection(from(0, 1), to(0, 2))
                .addExpectedSelection(from(0, 1, true), to(0, 2))
                .run();
    }

    public void testSelectsContentInRectangle() {
        // h[e]llo
        // w[o]rld
        new Tester()
                .text(
                        "hello",
                        "world"
                )
                .addInitialSelection(from(0, 1), to(1, 2))
                .addExpectedSelection(from(0, 1, true), to(0, 2))
                .addExpectedSelection(from(1, 1, true), to(1, 2))
                .run();
    }

    public void testSelectsShortLineInMiddleOfRectangle() {
        // h[ell]o
        // a[a]
        // w[orl]d
        new Tester()
                .text(
                        "hello",
                        "aa",
                        "world"
                )
                .addInitialSelection(from(0, 1), to(2, 4))
                .addExpectedSelection(from(0, 1, true), to(0, 4))
                .addExpectedSelection(from(1, 1, true), to(1, 2))
                .addExpectedSelection(from(2, 1, true), to(2, 4))
                .run();
    }

    public void testSkipsLineIfLineHasNoContentInRectangle() {
        // h[ell]o
        // a
        // w[orl]d
        new Tester()
                .text(
                        "hello",
                        "a",
                        "world"
                )
                .addInitialSelection(from(0, 1), to(2, 4))
                .addExpectedSelection(from(0, 1, true), to(0, 4))
                .addExpectedSelection(from(2, 1, true), to(2, 4))
                .run();
    }

    public void testNegativeSelection() {
        // h[ell]o
        // a
        // w[orl]d
        new Tester()
                .text(
                        "hello",
                        "a",
                        "world"
                )
                .addInitialSelection(from(0, 4), to(2, 1))
                .addExpectedSelection(new CaretState(
                        new LogicalPosition(0, 4), // caret position
                        new LogicalPosition(0, 1), // from
                        new LogicalPosition(0, 4)  // to
                ))
                .addExpectedSelection(new CaretState(
                        new LogicalPosition(2, 4), // caret position
                        new LogicalPosition(2, 1), // from
                        new LogicalPosition(2, 4)  // to
                ))
                .run();
    }

    public void testWorksWithMultipleCursors() {
        new Tester()
                .text(
                        "hello world",
                        "hi how",
                        "are you today"
                )
                .addInitialSelection(from(0, 1), to(1, 3))
                .addInitialSelection(from(1, 5), to(2, 9))
                .addExpectedSelection(from(0, 1), to(0, 3))
                .addExpectedSelection(from(1, 1), to(1, 3))
                .addExpectedSelection(from(1, 5), to(1, 6))
                .addExpectedSelection(from(2, 5), to(2, 9))
                .run();
    }

    private List<Triple<LogicalPosition, LogicalPosition, LogicalPosition>>
    positions(List<CaretState> caretStates) {
        return caretStates.stream()
                .map(it -> new Triple<>(
                        it.getCaretPosition(),
                        it.getSelectionStart(),
                        it.getSelectionEnd()))
                .collect(toList());
    }

    private static LogicalPosition from(int line, int column) {
        return from(line, column, false);
    }

    private static LogicalPosition from(
            int line,
            int column,
            boolean leanForward
    ) {
        return new LogicalPosition(line, column, leanForward);
    }

    private static LogicalPosition to(int line, int column) {
        return new LogicalPosition(line, column);
    }

    private class Tester implements Runnable {

        private String text;

        private final List<CaretState> initialSelections =
                new ArrayList<>();

        private final List<CaretState> expectedSelections =
                new ArrayList<>();

        Tester text(String... lines) {
            text = String.join("\n", lines);
            return this;
        }

        Tester addInitialSelection(
                LogicalPosition start,
                LogicalPosition end
        ) {
            initialSelections.add(new CaretState(start, start, end));
            return this;
        }

        Tester addExpectedSelection(CaretState selection) {
            expectedSelections.add(selection);
            return this;
        }

        Tester addExpectedSelection(
                LogicalPosition start,
                LogicalPosition end
        ) {
            return addExpectedSelection(
                    new CaretState(start, start, end));
        }

        @Override
        public void run() {
            myFixture.configureByText(PLAIN_TEXT, text);
            CaretModel caretModel = myFixture.getEditor().getCaretModel();
            caretModel.setCaretsAndSelections(initialSelections);
            myFixture.performEditorAction("com.gitlab.lae.intellij.actions.CreateRectangularSelectionFromMultiLineSelection");
            assertEquals(
                    positions(expectedSelections),
                    positions(caretModel.getCaretsAndSelections()));
        }

    }
}
