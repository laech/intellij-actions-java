package com.gitlab.lae.intellij.actions;

import com.google.common.base.Strings;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;

import javax.annotation.Nullable;

final class NSpace implements Edit {

    private final int count;

    NSpace(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count=" + count);
        }
        this.count = count;
    }

    @Override
    public void edit(Editor editor, Caret caret, @Nullable DataContext context) {

        Document doc = editor.getDocument();
        CharSequence chars = doc.getImmutableCharSequence();
        int offset = caret.getOffset();
        if (offset >= chars.length() || !isSpaceOrTab(chars, offset)) {
            return;
        }

        int start = offset;
        int end = offset;
        while (start > 0 && isSpaceOrTab(chars, start - 1)) start--;
        while (end < chars.length() && isSpaceOrTab(chars, end)) end++;
        if (start < end) {
            doc.replaceString(start, end, Strings.repeat(" ", count));
        }
    }

    private static boolean isSpaceOrTab(CharSequence chars, int offset) {
        char c = chars.charAt(offset);
        return c == ' ' || c == '\t';
    }
}
