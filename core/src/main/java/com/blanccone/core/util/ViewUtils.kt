package com.blanccone.core.util

import android.app.Activity
import android.app.Dialog
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.blanccone.core.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout

object ViewUtils {
    /**
     * Use this when set View to Visible
     */
    fun View.show(): View {
        if (visibility != View.VISIBLE) {
            visibility = View.VISIBLE
        }
        return this
    }

    /**
     * Use this when set View to Gone
     */
    fun View.hide(): View {
        if (visibility != View.GONE) {
            visibility = View.GONE
        }
        return this
    }

    fun View.invisible(): View {
        if (visibility != View.INVISIBLE) {
            visibility = View.INVISIBLE
        }
        return this
    }

    fun unknownMsg(): String = "Oops...Something went wrong"

    fun View.hideKeyboard() {
        val imm = this.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.windowToken, 0)
    }

    fun Dialog?.setHeightSetState(
        height: Int = ViewGroup.LayoutParams.MATCH_PARENT,
        state: Int = BottomSheetBehavior.STATE_EXPANDED
    ) {
        val d = this as BottomSheetDialog
        val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
        bottomSheet.layoutParams.height = height
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = state
    }

    fun TextInputLayout.stateError(string: String? = null) {
        this.isErrorEnabled = true
        this.error = string
        this.parent.requestChildFocus(this, this)
        if (string.isNullOrEmpty()) {
            this.error = String.format(this.context.resources.getString(R.string.error_input_message), this.hint)
        } else this.error = string
    }

    fun View.backgroundTint(color: Int){
        this.background.colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(this.context,color),
            PorterDuff.Mode.SRC_IN
        )
    }
}