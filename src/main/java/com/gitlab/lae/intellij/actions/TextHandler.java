package com.gitlab.lae.intellij.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction;
import static java.util.Objects.requireNonNull;

final class TextHandler extends EditorActionHandler {

    private final String actionId;
    private final Function<String, String> stringF;

    TextHandler(String actionId, Function<String, String> stringF) {
        super(true);
        this.actionId = actionId;
        this.stringF = requireNonNull(stringF);
    }

    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, DataContext context) {
        if (caret == null) {
            return;
        }
        int start = caret.getOffset();
        EditorActionHandler handler = EditorActionManager.getInstance().getActionHandler(actionId);
        if (handler == null) {
            return;
        }
        handler.execute(editor, caret, context);
        int end = caret.getOffset();
        if (start == end) {
            return;
        }
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        replace(editor, start, end);
    }

    private void replace(Editor editor, int startOffset, int endOffset) {
        Document doc = editor.getDocument();
        String replacement = stringF.apply(doc.getText(new TextRange(startOffset, endOffset)));
        runWriteCommandAction(editor.getProject(), () -> doc.replaceString(startOffset, endOffset, replacement));
    }
}
