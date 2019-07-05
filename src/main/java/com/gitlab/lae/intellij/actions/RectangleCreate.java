package com.gitlab.lae.intellij.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actions.TextComponentEditorAction;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class RectangleCreate
        extends TextComponentEditorAction {

    public RectangleCreate() {
        super(new Handler());
    }

    private static class Handler extends EditorActionHandler {
        Handler() {
            super(false);
        }

        @Override
        protected void doExecute(
                Editor editor,
                Caret caret,
                DataContext context
        ) {
            CaretModel caretModel = editor.getCaretModel();
            if (!caretModel.supportsMultipleCarets()) {
                return;
            }

            caretModel.setCaretsAndSelections(caretModel
                    .getCaretsAndSelections()
                    .stream()
                    .flatMap(o -> toRectangle(o, editor.getDocument()))
                    .collect(toList()));
        }
    }

    private static Stream<CaretState> toRectangle(CaretState cs, Document doc) {

        LogicalPosition selectionStart = cs.getSelectionStart();
        LogicalPosition selectionEnd = cs.getSelectionEnd();
        if (selectionStart == null || selectionEnd == null) {
            return Stream.empty();
        }

        if (selectionStart.line == selectionEnd.line) {
            return Stream.of(cs);
        }

        return IntStream
                .rangeClosed(selectionStart.line, selectionEnd.line)

                .filter(line -> hasEnoughColumns(
                        selectionStart, selectionEnd, line, doc))

                .mapToObj(line -> toSelection(
                        selectionStart, selectionEnd, line));
    }

    private static boolean hasEnoughColumns(
            LogicalPosition selectionStart,
            LogicalPosition selectionEnd,
            int line,
            Document doc
    ) {
        int columns = doc.getLineEndOffset(line)
                - doc.getLineStartOffset(line);

        return selectionStart.column < columns
                || selectionEnd.column < columns;
    }

    private static CaretState toSelection(
            LogicalPosition selectionStart,
            LogicalPosition selectionEnd,
            int line
    ) {
        LogicalPosition lineSelectionStart =
                new LogicalPosition(line, selectionStart.column);

        LogicalPosition lineSelectionEnd =
                new LogicalPosition(line, selectionEnd.column);

        return new CaretState(
                lineSelectionStart,
                lineSelectionStart,
                lineSelectionEnd);
    }
}
