package com.gitlab.lae.intellij.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.editor.actions.TextComponentEditorAction;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;

import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE;
import static com.intellij.openapi.editor.EditorModificationUtil.deleteSelectedTextForAllCarets;
import static com.intellij.psi.util.PsiTreeUtil.getParentOfType;
import static java.lang.Character.isSpaceChar;

public final class PsiKill extends TextComponentEditorAction {

    public PsiKill() {
        super(new Handler());
    }

    private static class Handler extends EditorWriteActionHandler {
        Handler() {
            super(false);
        }

        @Override
        public void executeWriteAction(
                Editor editor,
                Caret caret,
                DataContext context
        ) {
            selectElementsUnderCarets(editor, context);
            EditorCopyPasteHelper.getInstance()
                    .copySelectionToClipboard(editor);
            deleteSelectedTextForAllCarets(editor);
        }
    }

    private static void selectElementsUnderCarets(
            Editor editor,
            DataContext context
    ) {
        PsiFile file = context.getData(PSI_FILE);
        if (file == null) {
            return;
        }
        editor.getCaretModel().getAllCarets().forEach(caret -> {
            try {
                select(editor, caret, file);
            } catch (NoClassDefFoundError e) {
                // If no PSI classes are installed, fallback to kill line
                selectToLineEnd(editor, caret);
            }
        });
    }

    private static void select(Editor editor, Caret caret, PsiFile file) {

        LogicalPosition pos = caret.getLogicalPosition();
        if (isAtEndOfLine(pos, editor)) {
            if (!isAtLastLine(pos, editor)) {
                selectToNextLineStart(editor, caret);
                return;
            }
            return;
        }

        Document doc = editor.getDocument();
        CharSequence chars = doc.getImmutableCharSequence();
        OptionalInt offset = IntStream
                .range(caret.getOffset(), doc.getTextLength())
                .filter(i -> !isSpaceChar(chars.charAt(i)))
                .findAny();

        if (!offset.isPresent()) {
            return;
        }

        PsiElement element = file.findElementAt(offset.getAsInt());
        if (element == null) {
            selectToLineEnd(editor, caret);
            return;
        }

        while (element instanceof PsiWhiteSpace) {
            element = element.getNextSibling();
        }

        while (!(element instanceof PsiStatement)
                && !(element instanceof PsiModifierListOwner)
                && !(element instanceof PsiComment)
                && !(element instanceof PsiPolyadicExpression)
                && !(element instanceof PsiArrayInitializerExpression)
                && !isStringLiteral(editor, element)) {

            if (element == null) {
                selectToLineEnd(editor, caret);
                return;
            }
            element = element.getParent();
            if (element instanceof PsiFile) {
                selectToLineEnd(editor, caret);
                return;
            }
        }

        if (element instanceof PsiParameter) {
            selectList(editor, caret, element,
                    PsiParameterList.class,
                    PsiParameterList::getParameters
            );

        } else if (element instanceof PsiTypeParameter) {
            selectList(editor, caret, element,
                    PsiTypeParameterList.class,
                    PsiTypeParameterList::getTypeParameters
            );

        } else {
            selectElement(editor, caret, element);
        }
    }

    private static boolean isStringLiteral(Editor editor, PsiElement element) {
        if (!(element instanceof PsiLiteralExpression)) {
            return false;
        }
        TextRange range = element.getTextRange();
        int endOffset = range.getEndOffset();
        return endOffset > 0 &&
                editor.getDocument()
                        .getImmutableCharSequence()
                        .charAt(endOffset - 1) == '"';
    }

    private static void selectElement(
            Editor editor,
            Caret caret,
            PsiElement element
    ) {
        Document doc = editor.getDocument();
        TextRange range = element.getTextRange();
        int endOffset = range.getEndOffset();

        if (caret.getOffset() > range.getStartOffset()) {
            // If inside pair of quotes/braces etc, kill to just before the
            // end closing char.
            if (element instanceof PsiArrayInitializerExpression
                    || isStringLiteral(editor, element)) {
                endOffset--;
            }
        }

        int endLine = doc.getLineNumber(endOffset);
        int endColumn = endOffset - doc.getLineStartOffset(endLine);
        caret.setSelection(
                caret.getVisualPosition(),
                caret.getOffset(),
                editor.logicalToVisualPosition(
                        new LogicalPosition(endLine, endColumn)),
                endOffset
        );
    }

    private static <T extends PsiElement> void selectList(
            Editor editor,
            Caret caret,
            PsiElement element,
            Class<T> parentType,
            Function<T, PsiElement[]> getParams
    ) {
        Document doc = editor.getDocument();
        T list = getParentOfType(element, parentType);
        if (list == null) {
            return;
        }
        PsiElement[] params = getParams.apply(list);
        if (params.length == 0) {
            return;
        }
        PsiElement lastParam = params[params.length - 1];
        int endOffset = lastParam.getTextRange().getEndOffset();
        int endLine = doc.getLineNumber(endOffset);
        int endLineColumn = endOffset - doc.getLineStartOffset(endLine);
        caret.setSelection(
                caret.getVisualPosition(),
                caret.getOffset(),
                editor.logicalToVisualPosition(
                        new LogicalPosition(endLine, endLineColumn)),
                endOffset
        );
    }

    private static boolean isAtEndOfLine(
            LogicalPosition pos,
            Editor editor
    ) {
        Document doc = editor.getDocument();
        return doc.getLineEndOffset(pos.line) -
                doc.getLineStartOffset(pos.line) == pos.column;
    }

    private static boolean isAtLastLine(LogicalPosition pos, Editor editor) {
        return pos.line + 1 >= editor.getDocument().getLineCount();
    }

    private static void selectToLineEnd(Editor editor, Caret caret) {

        VisualPosition visualStartPosition =
                caret.getVisualPosition();

        int logicalEndOffset =
                editor.getDocument().getLineEndOffset(
                        caret.getLogicalPosition().line);

        LogicalPosition logicalStartPosition =
                editor.visualToLogicalPosition(visualStartPosition);

        int logicalStartLineOffset =
                editor.getDocument().getLineStartOffset(
                        logicalStartPosition.line);

        LogicalPosition logicalEndPosition = new LogicalPosition(
                logicalStartPosition.line,
                logicalEndOffset - logicalStartLineOffset
        );
        caret.setSelection(
                visualStartPosition,
                caret.getOffset(),
                editor.logicalToVisualPosition(logicalEndPosition),
                logicalEndOffset
        );
    }

    private static void selectToNextLineStart(
            Editor editor,
            Caret caret
    ) {
        VisualPosition visualStartPosition =
                caret.getVisualPosition();

        VisualPosition visualEndPosition =
                new VisualPosition(visualStartPosition.line + 1, 0);

        LogicalPosition logicalEndPosition =
                editor.visualToLogicalPosition(visualEndPosition);

        int logicalEndOffset =
                editor.logicalPositionToOffset(logicalEndPosition);

        caret.setSelection(
                visualStartPosition,
                caret.getOffset(),
                visualEndPosition,
                logicalEndOffset
        );
    }
}