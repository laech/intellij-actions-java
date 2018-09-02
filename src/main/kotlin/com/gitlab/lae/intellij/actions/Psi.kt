package com.gitlab.lae.intellij.actions

import com.intellij.openapi.actionSystem.CommonDataKeys.PSI_FILE
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.editor.actions.TextComponentEditorAction
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiStatement
import com.intellij.psi.PsiWhiteSpace

class PsiDelete : TextComponentEditorAction(object : EditorWriteActionHandler(true) {

    override fun executeWriteAction(editor: Editor, caret: Caret?, ctx: DataContext) {
        caret ?: return

        var element = ctx.getData(PSI_FILE)?.findElementAt(caret.offset)
                ?: return

        while (element is PsiWhiteSpace) {
            element = element.nextSibling
        }

        while (element !is PsiStatement) {
            element = element.parent
            if (element is PsiFile) {
                return
            }
        }

        editor.document.deleteString(caret.offset, element.textRange.endOffset)
    }

})
