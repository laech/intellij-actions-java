package com.gitlab.lae.intellij.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.editor.actions.TextComponentEditorAction;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE;
import static com.intellij.openapi.editor.EditorModificationUtil.deleteSelectedTextForAllCarets;
import static java.lang.Character.isSpaceChar;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

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
                && !(element instanceof PsiLiteralExpression)
                && !(element instanceof PsiArrayInitializerExpression)
                && !(element instanceof PsiParameterList)
                && !(element instanceof PsiParenthesizedExpression)
                && !(element instanceof PsiCodeBlock)
                && !isParentPolyadic(element)
                && !isParentExpressionList(element)
                && !isParentTypeParameterList(element)
                && !isStringLiteral(element)) {

            if (element == null || element instanceof PsiFile) {
                selectToLineEnd(editor, caret);
                return;
            }

            element = element.getParent();
        }

        selectElement(editor, caret, element);
    }

    private static boolean isParentTypeParameterList(PsiElement element) {
        return element != null &&
                element.getParent() instanceof PsiTypeParameterList;
    }

    private static boolean isParentExpressionList(PsiElement element) {
        return element != null &&
                element.getParent() instanceof PsiExpressionList;
    }

    private static boolean isParentPolyadic(PsiElement element) {
        return element != null &&
                element.getParent() instanceof PsiPolyadicExpression;
    }

    private static boolean isStringLiteral(PsiElement element) {
        return element instanceof PsiLiteralValue &&
                ((PsiLiteralValue) element).getValue() instanceof String;
    }

    private static boolean isCharLiteral(PsiElement element) {
        return element instanceof PsiLiteralValue &&
                ((PsiLiteralValue) element).getValue() instanceof Character;
    }

    private static boolean isCompleteCodeBlock(PsiElement element) {
        return element instanceof PsiCodeBlock &&
                ((PsiCodeBlock) element).getLBrace() != null &&
                ((PsiCodeBlock) element).getRBrace() != null;
    }

    private static boolean isCompleteClass(PsiElement element) {
        return element instanceof PsiClass &&
                ((PsiClass) element).getLBrace() != null &&
                ((PsiClass) element).getRBrace() != null;
    }

    private static void selectElement(
            Editor editor,
            Caret caret,
            PsiElement element
    ) {
        Document doc = editor.getDocument();
        TextRange elementTextRange = element.getTextRange();
        int selectionEndOffset = elementTextRange.getEndOffset();

        PsiElement parent = element.getParent();
        if (caret.getOffset() > elementTextRange.getStartOffset()) {
            // If inside pair of quotes/braces etc, kill to just before the
            // end closing char.
            if (isStringLiteral(element)
                    || isCharLiteral(element)
                    || element instanceof PsiArrayInitializerExpression
                    || element instanceof PsiParameterList
                    || element instanceof PsiParenthesizedExpression
                    || isCompleteCodeBlock(element)
                    || isCaretBetweenClassBraces(caret, element)) {
                selectionEndOffset--;
            }

        } else if (parent instanceof PsiPolyadicExpression) {
            // If in polyadic expression (e.g. a && b && c), kill single
            // element and operator
            selectionEndOffset =
                    getNextElementOffset(
                            ((PsiPolyadicExpression) parent).getOperands(),
                            element
                    ).orElse(selectionEndOffset);

        } else if (parent instanceof PsiArrayInitializerExpression) {
            // If in array, kill single element + separator
            selectionEndOffset =
                    getListOrNextElementOffset(
                            parent,
                            element,
                            ((PsiArrayInitializerExpression) parent)::getInitializers
                    ).orElseGet(() -> parent.getTextRange().getEndOffset() - 1);

        } else {
            Optional<PsiElement[]> elements = getListElements(parent);
            selectionEndOffset = elements
                    .map(es -> getListOrNextElementOffset(
                            parent,
                            element,
                            () -> es
                    ).orElseGet(() -> parent.getTextRange().getEndOffset() - 1))
                    .orElse(selectionEndOffset);
        }

        int endLine = doc.getLineNumber(selectionEndOffset);
        int endColumn = selectionEndOffset - doc.getLineStartOffset(endLine);
        caret.setSelection(
                caret.getVisualPosition(),
                caret.getOffset(),
                editor.logicalToVisualPosition(
                        new LogicalPosition(endLine, endColumn)),
                selectionEndOffset
        );
    }

    private static OptionalInt getListOrNextElementOffset(
            PsiElement parent,
            PsiElement element,
            Supplier<PsiElement[]> getChildren
    ) {
        TextRange elementRange = element.getTextRange();
        TextRange parentTextRange = parent.getTextRange();
        return elementRange.getStartOffset() ==
                parentTextRange.getStartOffset()
                ? OptionalInt.of(parentTextRange.getEndOffset())
                : getNextElementOffset(getChildren.get(), element);
    }

    private static OptionalInt getNextElementOffset(
            PsiElement[] elements,
            PsiElement element
    ) {
        int index = asList(elements).indexOf(element);
        OptionalInt offset = index < 0 || index >= elements.length - 1
                ? OptionalInt.empty()
                : OptionalInt.of(elements[index + 1]
                        .getTextRange()
                        .getStartOffset());

        return offset.isPresent() ? offset : stream(elements)
                .map(PsiElement::getTextRange)
                .filter(o -> o.getStartOffset() >=
                        element.getTextRange().getEndOffset())
                .mapToInt(TextRange::getEndOffset)
                .findFirst();
    }

    private static Optional<PsiElement[]> getListElements(PsiElement parent) {
        if (parent instanceof PsiExpressionList) {
            return Optional.of(((PsiExpressionList) parent).getExpressions());
        }
        if (parent instanceof PsiParameterList) {
            return Optional.of(((PsiParameterList) parent).getParameters());
        }
        if (parent instanceof PsiTypeParameterList) {
            return Optional.of(((PsiTypeParameterList) parent).getTypeParameters());
        }
        return Optional.empty();
    }

    private static boolean isCaretBetweenClassBraces(
            Caret caret,
            PsiElement element
    ) {
        return isCompleteClass(element) &&
                requireNonNull(((PsiClass) element).getLBrace()).getTextRange()
                        .getEndOffset() <= caret.getOffset();
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