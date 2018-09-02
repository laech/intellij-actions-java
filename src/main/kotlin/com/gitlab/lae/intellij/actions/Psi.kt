package com.gitlab.lae.intellij.actions

import com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction
import com.intellij.psi.*
import com.intellij.psi.util.parentOfType

class PsiDelete : TextComponentEditorAction(object : EditorWriteActionHandler(true) {

    override fun executeWriteAction(editor: Editor, caret: Caret?, ctx: DataContext) {
        caret ?: return

        val doc = editor.document
        if (!doc.isWritable) {
            return
        }

        val line = doc.getLineNumber(caret.offset)
        if (doc.getLineEndOffset(line) == caret.offset) {
            if (line + 1 < doc.lineCount) {
                doc.deleteString(caret.offset, doc.getLineStartOffset(line + 1))
            }
            return
        }

        var element = ctx.getData(PSI_FILE)?.findElementAt(caret.offset)
                ?: return

        while (element is PsiWhiteSpace) {
            element = element.nextSibling
        }

        while (element !is PsiStatement && element !is PsiModifierListOwner) {
            element = element.parent
            if (element is PsiFile) {
                return
            }
        }

        when (element) {
            is PsiParameter -> deleteList(doc, caret.offset, element, PsiParameterList::getParameters)
            is PsiTypeParameter -> deleteList(doc, caret.offset, element, PsiTypeParameterList::getTypeParameters)
            else -> doc.deleteString(caret.offset, element.textRange.endOffset)
        }
    }

    private inline fun <reified T : PsiElement> deleteList(
            doc: Document,
            startOffset: Int,
            element: PsiElement,
            getParameters: (T) -> Array<out PsiElement>
    ) {
        val list = element.parentOfType<T>() ?: return
        val last = getParameters(list).lastOrNull() ?: return
        doc.deleteString(startOffset, last.textRange.endOffset)
    }

})
