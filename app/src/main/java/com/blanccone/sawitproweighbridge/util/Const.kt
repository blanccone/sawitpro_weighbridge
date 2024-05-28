package com.blanccone.sawitproweighbridge.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.blanccone.core.model.local.Menu
import com.blanccone.core.R
import com.blanccone.sawitproweighbridge.util.Const.percentWatcher

object Const {
    fun homeMenu() : List<Menu> {
        return listOf(
            Menu("tickets123","Tickets", R.drawable.ic_ticket_24),
            Menu("results456","Weighment Results", R.drawable.ic_result_24)
        )
    }

    fun EditText.percentWatcher(){
        val editText = this
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }
            override fun afterTextChanged(p0: Editable?) { }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                editText.removeTextChangedListener(this)
                p0?.let {
                    val text = it.toString().replace("[%]".toRegex(), "")
                    val textPercent = "${text.toInt()}%"
                    editText.apply {
                        setText(textPercent)
                        setSelection(textPercent.length)
                    }
                    editText.addTextChangedListener(this)
                }
            }
        }
        editText.addTextChangedListener(watcher)
    }
}