package com.gitlab.lae.intellij.actions.java;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.editor.actions.TextComponentEditorAction;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.function.ToIntBiFunction;
import java.util.stream.IntStream;

import static com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE;
import static com.intellij.openapi.editor.EditorModificationUtil.deleteSelectedTextForAllCarets;
import static java.lang.Character.isSpaceChar;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;

public final class KillToCodeEnd extends TextComponentEditorAction {

    private static final EndOffsetGetter[] getters = EndOffsetGetter.values();

    public KillToCodeEnd() {
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

        while (true) {

            if (element == null || element instanceof PsiFile) {
                selectToLineEnd(editor, caret);
                return;
            }

            Optional<EndOffsetGetter> getter = getter(element, caret);
            if (!getter.isPresent()) {
                element = element.getParent();
                continue;
            }

            int endOffset = getter.get().applyAsInt(element, caret);
            int endLine = doc.getLineNumber(endOffset);
            int endColumn = endOffset - doc.getLineStartOffset(endLine);
            caret.setSelection(
                    caret.getVisualPosition(),
                    caret.getOffset(),
                    editor.logicalToVisualPosition(new LogicalPosition(
                            endLine, endColumn
                    )),
                    endOffset
            );
            return;
        }
    }

    private static Optional<EndOffsetGetter> getter(
            PsiElement element,
            Caret caret
    ) {
        return stream(getters)
                .filter(getter -> getter.test(element, caret))
                .findFirst();
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
        if (index >= 0 && index < elements.length - 1) {
            return OptionalInt.of(
                    elements[index + 1]
                            .getTextRange()
                            .getStartOffset()
            );
        }
        int elementEndOffset = element.getTextRange().getEndOffset();
        return stream(elements)
                .map(PsiElement::getTextRange)
                .filter(range -> range.getStartOffset() >= elementEndOffset)
                .mapToInt(TextRange::getEndOffset)
                .findFirst();
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

    private enum EndOffsetGetter
            implements
            BiPredicate<PsiElement, Caret>,
            ToIntBiFunction<PsiElement, Caret> {

        ENCLOSURE_PARENT {
            @Override
            public boolean test(PsiElement element, Caret caret) {
                return element instanceof PsiParenthesizedExpression
                        || element instanceof PsiArrayInitializerExpression
                        || element instanceof PsiParameterList
                        || element instanceof PsiCodeBlock
                        || isCharLiteral(element)
                        || isStringLiteral(element)
                        || isCompleteCodeBlock(element)
                        || isCaretBetweenClassBraces(caret, element);
            }

            @Override
            public int applyAsInt(PsiElement element, Caret caret) {
                TextRange range = element.getTextRange();
                return range.getEndOffset() -
                        (caret.getOffset() > range.getStartOffset() ? 1 : 0);
            }

            private boolean isStringLiteral(PsiElement element) {
                return element instanceof PsiLiteralValue &&
                        ((PsiLiteralValue) element).getValue() instanceof String;
            }

            private boolean isCharLiteral(PsiElement element) {
                return element instanceof PsiLiteralValue &&
                        ((PsiLiteralValue) element).getValue() instanceof Character;
            }

            private boolean isCompleteCodeBlock(PsiElement element) {
                return element instanceof PsiCodeBlock &&
                        ((PsiCodeBlock) element).getLBrace() != null &&
                        ((PsiCodeBlock) element).getRBrace() != null;
            }

            private boolean isCompleteClass(PsiElement element) {
                return element instanceof PsiClass &&
                        ((PsiClass) element).getLBrace() != null &&
                        ((PsiClass) element).getRBrace() != null;
            }

            private boolean isCaretBetweenClassBraces(
                    Caret caret,
                    PsiElement element
            ) {
                return isCompleteClass(element) &&
                        requireNonNull(((PsiClass) element).getLBrace())
                                .getTextRange()
                                .getEndOffset()
                                <= caret.getOffset();
            }
        },

        POLYADIC_CHILD {
            @Override
            public boolean test(PsiElement element, Caret caret) {
                return element.getParent() instanceof PsiPolyadicExpression;
            }

            @Override
            public int applyAsInt(PsiElement element, Caret caret) {
                PsiPolyadicExpression parent =
                        (PsiPolyadicExpression) element.getParent();
                return getNextElementOffset(parent.getOperands(), element)
                        .orElseGet(() -> element.getTextRange().getEndOffset());
            }
        },

        ARRAY_INITIALIZER_CHILD {
            @Override
            public boolean test(PsiElement element, Caret caret) {
                return element.getParent() instanceof PsiArrayInitializerExpression;
            }

            @Override
            public int applyAsInt(PsiElement element, Caret caret) {
                PsiArrayInitializerExpression parent =
                        (PsiArrayInitializerExpression) element.getParent();
                return getListOrNextElementOffset(
                        parent,
                        element,
                        parent::getInitializers
                ).orElseGet(() -> parent.getTextRange().getEndOffset() - 1);
            }
        },

        ENCLOSURE_CHILD {
            @Override
            public boolean test(PsiElement element, Caret caret) {
                return getChildren(element.getParent()).isPresent();
            }

            @Override
            public int applyAsInt(PsiElement element, Caret caret) {
                PsiElement parent = element.getParent();
                return getChildren(parent)
                        .map(es -> getListOrNextElementOffset(
                                parent,
                                element,
                                () -> es
                        ).orElseGet(() ->
                                parent.getTextRange().getEndOffset() - 1))
                        .orElseGet(() -> element.getTextRange().getEndOffset());
            }

            private Optional<PsiElement[]> getChildren(PsiElement parent) {
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
        },

        GENERAL {
            @Override
            public boolean test(PsiElement element, Caret caret) {
                return (element instanceof PsiStatement)
                        || (element instanceof PsiModifierListOwner)
                        || (element instanceof PsiComment)
                        || (element instanceof PsiLiteralExpression);
            }

            @Override
            public int applyAsInt(PsiElement element, Caret caret) {
                return element.getTextRange().getEndOffset();
            }
        }
    }
}