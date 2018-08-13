package com.gitlab.lae.intellij.actions

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.IdeActions.ACTION_EDITOR_NEXT_WORD
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.util.TextRange
import java.lang.Character.isLowerCase
import java.lang.Character.isUpperCase
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.math.min
import kotlin.text.toLowerCase
import kotlin.text.toUpperCase

class UpcaseRegionOrToWordEnd : TextAction(true, upcaseRegionOrToWordEnd)
class DowncaseRegionOrToWordEnd : TextAction(true, downcaseRegionOrToWordEnd)
class CapitalizeRegionOrToWordEnd : TextAction(true, capitalizeRegionOrToWordEnd)

private val upcaseRegion = region(String::toUpperCase)
private val downcaseRegion = region(String::toLowerCase)
private val capitalizeRegion = region(::toCap)

private val upcaseToWordEnd = move(ACTION_EDITOR_NEXT_WORD, String::toUpperCase)
private val downcaseToWordEnd = move(ACTION_EDITOR_NEXT_WORD, String::toLowerCase)
private val capitalizeToWordEnd = move(ACTION_EDITOR_NEXT_WORD, ::toCap)

private val upcaseRegionOrToWordEnd = regionOr(upcaseRegion, upcaseToWordEnd)
private val downcaseRegionOrToWordEnd = regionOr(downcaseRegion, downcaseToWordEnd)
private val capitalizeRegionOrToWordEnd = regionOr(capitalizeRegion, capitalizeToWordEnd)

private val wordPattern = Pattern.compile("\\w+")

private fun toCap(str: String): String {
    val matcher = wordPattern.matcher(str)
    var builder: StringBuffer? = null
    while (matcher.find()) {
        if (builder == null) {
            builder = StringBuffer(str.length)
        }
        matcher.appendReplacement(builder, matcher.group().capitalize())
    }
    return when (builder) {
        null -> str
        else -> matcher.appendTail(builder).toString()
    }
}

private fun regionOr(region: Edit, other: Edit): Edit = { editor, caret, ctx ->
    (if (caret.hasSelection()) region else other)(editor, caret, ctx)
}

private fun region(f: (String) -> String): Edit = g@{ editor, caret, _ ->
    if (!caret.hasSelection()) {
        return@g
    }
    val start = caret.selectionStart
    val end = caret.selectionEnd
    val doc = editor.document
    val replacement = f(doc.getText(TextRange(start, end)))
    caret.removeSelection()
    doc.replaceString(start, end, replacement)
    caret.setSelection(start, start + replacement.length)
}

private fun move(id: String, f: (String) -> String): Edit = { editor, caret, ctx ->
    caret.removeSelection()
    val (start, end) = moveAndGetRegion(id, editor, caret, ctx)
    val doc = editor.document
    val replacement = f(doc.getText(TextRange(start, end)))
    doc.replaceString(start, end, replacement)
}

private fun moveAndGetRegion(
        id: String,
        editor: Editor,
        caret: Caret,
        ctx: DataContext?
): Pair<Int, Int> {

    fun executeMoveAction(): Int {
        EditorActionManager.getInstance()
                .getActionHandler(id)
                .execute(editor, caret, ctx)
        return caret.offset
    }

    val offset1 = caret.offset
    var offset2: Int
    while (true) {
        offset2 = executeMoveAction()
        if (editor.document.charsSequence
                        .subSequence(min(offset1, offset2), max(offset1, offset2))
                        .codePoints()
                        .anyMatch { p -> isUpperCase(p) != isLowerCase(p) }) {
            break
        }
    }
    return Pair(min(offset1, offset2), max(offset1, offset2))
}