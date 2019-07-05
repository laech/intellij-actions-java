package com.gitlab.lae.intellij.actions;

import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.CaretState;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import kotlin.Triple;

import java.util.List;

import static com.intellij.openapi.fileTypes.FileTypes.PLAIN_TEXT;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public final class RectangleCreateTest
        extends LightPlatformCodeInsightFixtureTestCase {

    public void testSingleLineSelectionRemainsUnchanged() {
        test("hello", singletonList(
                new CaretState(
                        new LogicalPosition(0, 1),
                        new LogicalPosition(0, 1),
                        new LogicalPosition(0, 2)
                )
        ), singletonList(
                new CaretState(
                        new LogicalPosition(0, 1),
                        new LogicalPosition(0, 1, true),
                        new LogicalPosition(0, 2)
                )
        ));
    }

    public void testSelectsContentInRectangle() {

        // h[e]llo
        // w[o]rld
        test("hello\nworld", singletonList(
                new CaretState(
                        new LogicalPosition(0, 1),
                        new LogicalPosition(0, 1),
                        new LogicalPosition(1, 2)
                )
        ), asList(
                new CaretState(
                        new LogicalPosition(0, 1),
                        new LogicalPosition(0, 1, true),
                        new LogicalPosition(0, 2)
                ),
                new CaretState(
                        new LogicalPosition(1, 1),
                        new LogicalPosition(1, 1, true),
                        new LogicalPosition(1, 2)
                )
        ));
    }

    public void testSelectsShortLineInMiddleOfRectangle() {

        // h[ell]o
        // a[a]
        // w[orl]d
        test("hello\naa\nworld", singletonList(
                new CaretState(
                        new LogicalPosition(0, 1),
                        new LogicalPosition(0, 1),
                        new LogicalPosition(2, 4)
                )
        ), asList(
                new CaretState(
                        new LogicalPosition(0, 1),
                        new LogicalPosition(0, 1, true),
                        new LogicalPosition(0, 4)
                ),
                new CaretState(
                        new LogicalPosition(1, 1),
                        new LogicalPosition(1, 1, true),
                        new LogicalPosition(1, 2)
                ),
                new CaretState(
                        new LogicalPosition(2, 1),
                        new LogicalPosition(2, 1, true),
                        new LogicalPosition(2, 4)
                )
        ));
    }

    public void testSkipsLineIfLineHasNoContentInRectangle() {

        // h[ell]o
        // a
        // w[orl]d
        test("hello\na\nworld", singletonList(
                new CaretState(
                        new LogicalPosition(0, 1),
                        new LogicalPosition(0, 1),
                        new LogicalPosition(2, 4)
                )
        ), asList(
                new CaretState(
                        new LogicalPosition(0, 1),
                        new LogicalPosition(0, 1, true),
                        new LogicalPosition(0, 4)
                ),
                new CaretState(
                        new LogicalPosition(2, 1),
                        new LogicalPosition(2, 1, true),
                        new LogicalPosition(2, 4)
                )
        ));
    }

    public void testNegativeSelection() {

        // h[ell]o
        // a
        // w[orl]d
        test("hello\na\nworld", singletonList(
                new CaretState(
                        new LogicalPosition(0, 4),
                        new LogicalPosition(0, 4),
                        new LogicalPosition(2, 1)
                )
        ), asList(
                new CaretState(
                        new LogicalPosition(0, 4),
                        new LogicalPosition(0, 1),
                        new LogicalPosition(0, 4)
                ),
                new CaretState(
                        new LogicalPosition(2, 4),
                        new LogicalPosition(2, 1),
                        new LogicalPosition(2, 4)
                )
        ));
    }

    public void testWorksWithMultipleCursors() {
        test("hello world\nhi how\nare you today", asList(
                new CaretState(
                        new LogicalPosition(0, 1),
                        new LogicalPosition(0, 1),
                        new LogicalPosition(1, 3)
                ),
                new CaretState(
                        new LogicalPosition(1, 5),
                        new LogicalPosition(1, 5),
                        new LogicalPosition(2, 9)
                )
        ), asList(
                new CaretState(
                        new LogicalPosition(0, 1),
                        new LogicalPosition(0, 1),
                        new LogicalPosition(0, 3)
                ),
                new CaretState(
                        new LogicalPosition(1, 1),
                        new LogicalPosition(1, 1),
                        new LogicalPosition(1, 3)
                ),
                new CaretState(
                        new LogicalPosition(1, 5),
                        new LogicalPosition(1, 5),
                        new LogicalPosition(1, 6)
                ),
                new CaretState(
                        new LogicalPosition(2, 5),
                        new LogicalPosition(2, 5),
                        new LogicalPosition(2, 9)
                )
        ));
    }

    private void test(
            String text,
            List<CaretState> initialSelections,
            List<CaretState> expectedSelections
    ) {
        myFixture.configureByText(PLAIN_TEXT, text);
        CaretModel caretModel = myFixture.getEditor().getCaretModel();
        caretModel.setCaretsAndSelections(initialSelections);
        myFixture.performEditorAction("com.gitlab.lae.intellij.actions.CreateRectangularSelectionFromMultiLineSelection");
        assertEquals(
                positions(expectedSelections),
                positions(myFixture.getEditor().getCaretModel().getCaretsAndSelections()));
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
}
