package com.blanccone.sawitproweighbridge.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

abstract class TestWatcher(
    private val et: EditText
) : TextWatcher {
    val max = 100
    var isUpdate = false
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        if (!isUpdate) {
            isUpdate = true
            if (s.toString().isEmpty() || s.toString() == "0" || s.toString() == "%") {
                val text = "0%"
                et.apply {
                    setText(text)
                    setSelection(text.length - 1)
                }
            }
            if (!s.toString().endsWith("%")) {
                val text = s.toString().replace("%", "") + "%"
                et.apply {
                    setText(text)
                    setSelection(text.length - 1)
                }
            }
            if (s.toString().length > 2 && s?.startsWith("0") == true) {
                val text = s.toString()
                    .replaceFirst("0", "")
                    .replace("%", "") + "%"
                et.apply {
                    setText(text)
                    setSelection(text.length - 1)
                }
            }
            val numberInput = s.toString().replace("%", "")
            if (s.toString().length > 2 && numberInput.toInt() + getCurrentTotal() > max) {
                val maxInput = max - getCurrentTotal()
                val text = "$maxInput%"
                et.apply {
                    setText(text)
                    setSelection(text.length - 1)
                }
            }
            doAfterTextChanged()
            isUpdate = false
        }
    }

    protected abstract fun getCurrentTotal(): Int

    protected abstract fun doAfterTextChanged()

}