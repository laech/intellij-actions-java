package com.gitlab.lae.intellij.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.util.TextRange;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.function.Function;

import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;
import static java.lang.Math.max;
import static java.lang.Math.min;

@FunctionalInterface
interface Edit {

    void edit(Editor editor, Caret caret, @Nullable DataContext context);

    default EditorActionHandler toHandler() {
        return new EditorActionHandler(true) {
            @Override
            protected void doExecute(
                    @Nonnull Editor editor,
                    @Nullable Caret caret,
                    @Nullable DataContext context
            ) {
                if (caret != null) {
                    edit(editor, caret, context);
                }
            }
        };
    }

    default EditorWriteActionHandler toWriteHandler() {
        return new EditorWriteActionHandler(true) {
            @Override
            public void executeWriteAction(
                    @Nonnull Editor editor,
                    @Nullable Caret caret,
                    @Nullable DataContext context
            ) {
                if (caret != null) {
                    edit(editor, caret, context);
                }
            }
        };
    }

    default Edit ifNoSelection(Edit other) {
        return (editor, caret, context) ->
                (caret.hasSelection() ? this : other)
                        .edit(editor, caret, context);
    }

    static Edit replacingSelection(Function<String, String> replace) {
        return (editor, caret, __) -> {
            if (!caret.hasSelection()) {
                return;
            }
            int start = caret.getSelectionStart();
            int end = caret.getSelectionEnd();
            Document doc = editor.getDocument();
            String replacement = replace.apply(doc.getText(new TextRange(start, end)));
            caret.removeSelection();
            doc.replaceString(start, end, replacement);
            caret.setSelection(start, start + replacement.length());
        };
    }

    static Edit replacingFromCaret(
            String id,
            Function<String, String> replace
    ) {
        return (editor, caret, context) -> {
            caret.removeSelection();
            Entry<Integer, Integer> entry =
                    moveAndGetRegion(id, editor, caret, context);
            int start = entry.getKey();
            int end = entry.getValue();
            if (start == end) {
                return;
            }
            Document doc = editor.getDocument();
            String text = doc.getText(new TextRange(start, end));
            String replacement = replace.apply(text);
            doc.replaceString(start, end, replacement);
        };
    }

    /*private*/
    static Entry<Integer, Integer> moveAndGetRegion(
            String id,
            Editor editor,
            Caret caret,
            DataContext context
    ) {
        int offset1 = caret.getOffset();
        int offset2 = offset1;
        while (offset2 < editor.getDocument().getTextLength()) {
            offset2 = executeMoveAction(id, editor, caret, context);
            if (editor.getDocument().getCharsSequence()
                    .subSequence(
                            min(offset1, offset2),
                            max(offset1, offset2))
                    .codePoints()
                    .anyMatch(p -> isUpperCase(p) != isLowerCase(p))) {
                break;
            }
        }
        return new SimpleImmutableEntry<>(
                min(offset1, offset2),
                max(offset1, offset2));
    }

    /*private*/
    static int executeMoveAction(
            String id,
            Editor editor,
            Caret caret,
            DataContext context
    ) {
        EditorActionManager
                .getInstance()
                .getActionHandler(id)
                .execute(editor, caret, context);
        return caret.getOffset();
    }
}
