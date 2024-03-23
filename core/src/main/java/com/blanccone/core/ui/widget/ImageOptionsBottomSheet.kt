package com.blanccone.core.ui.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.blanccone.core.R
import com.blanccone.core.databinding.BottomSheetImageOptionsBinding
import com.blanccone.core.util.ViewUtils.setHeightSetState
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ImageOptionsBottomSheet : BottomSheetDialogFragment() {

    private var _binding : BottomSheetImageOptionsBinding? = null
    val binding
        get() = requireNotNull(_binding)

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetImageOptionsBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setHeightSetState(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            BottomSheetBehavior.STATE_EXPANDED
        )
        setEvent()
    }

    private fun setEvent() {
        with(binding) {
            llImageOptionsOpen.setOnClickListener {
                onOpenFileClickListener?.let { it() }
                dismiss()
            }

            llImageOptionsRemove.setOnClickListener {
                onRemoveClickListener?.let { it() }
                dismiss()
            }

            btnDismiss.setOnClickListener { dismiss() }
        }
    }

    private var onOpenFileClickListener: (() -> Unit)? = null
    private var onRemoveClickListener: (() -> Unit)? = null

    fun setOnOpenFileListener(listener: (() -> Unit)?) {
        onOpenFileClickListener = listener
    }

    fun setOnRemoveListener(listener: (() -> Unit)?) {
        onRemoveClickListener = listener
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val TAG = "image_options"

        fun showDialog(fragmentManager: FragmentManager) : ImageOptionsBottomSheet {
            val dialog = ImageOptionsBottomSheet()
            dialog.show(fragmentManager, TAG)
            return dialog
        }

        fun showDialog(fragmentActivity: FragmentActivity) : ImageOptionsBottomSheet {
            val dialog = ImageOptionsBottomSheet()
            dialog.show(fragmentActivity.supportFragmentManager, TAG)
            return dialog
        }
    }
}