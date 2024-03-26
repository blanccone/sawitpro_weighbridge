package com.blanccone.core.ui.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import com.blanccone.core.R
import com.blanccone.core.databinding.BottomSheetDialogActionBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ActionBottomSheetDialog: BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetDialogActionBinding
    
    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetDialogActionBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeightState()
        with(binding) {
            tvAction.apply {
                text = title
                setOnClickListener {
                    onItemClickListener?.let { save ->
                        save()
                    }
                    dismiss()
                }
            }
        }
    }

    private fun setHeightState() {
        val dialog = dialog as BottomSheetDialog
        val bottomSheet = dialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) as FrameLayout
        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private var onItemClickListener: (() -> Unit)? = null

    fun setOnItemClickListener(listener: (() -> Unit)) {
        onItemClickListener = listener
    }

    companion object {
        private const val TAG = "BOTTOM_SHEET_DIALOG"
        private var title = ""

        fun showDialog(fragmentManager: FragmentManager, title: String): ActionBottomSheetDialog {
            this.title = title
            val dialog = ActionBottomSheetDialog()
            dialog.show(fragmentManager, TAG)
            return dialog
        }
    }
}