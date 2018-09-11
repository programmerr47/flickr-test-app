package com.github.programmerr47.flickrawesomeclient.util.sugar

import android.text.Editable
import android.text.TextWatcher

abstract class AbstractTextWatcher : TextWatcher {
    override fun afterTextChanged(s: Editable) {}
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
}

class SimpleTextWatcher : TextWatcher {
    private var after: (Editable) -> Unit = {}
    private var before: (CharSequence, Int, Int, Int) -> Unit = { _, _, _, _ -> }
    private var on: (CharSequence, Int, Int, Int) -> Unit = { _, _, _, _ -> }

    override fun afterTextChanged(s: Editable) = after(s)
    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) =
            before(s, start, count, after)
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) =
            on(s, start, before, count)

    fun after(after: (Editable) -> Unit) = apply { this.after = after }
    fun before(before: (CharSequence, Int, Int, Int) -> Unit) = apply { this.before = before }
    fun on(on: (CharSequence, Int, Int, Int) -> Unit) = apply { this.on = on }
}

fun textWatcher() = SimpleTextWatcher()