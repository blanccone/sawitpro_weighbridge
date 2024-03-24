package com.blanccone.core.ui.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.blanccone.core.R
import com.blanccone.core.databinding.BottomSheetFilterBinding
import com.blanccone.core.util.ViewUtils.setHeightSetState
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.radiobutton.MaterialRadioButton

class FilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterBinding? = null
    val binding
        get() = requireNotNull(_binding)

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterBinding.inflate(
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
        setView()
        setEvent()
    }

    private fun setView() {
        with(binding) {
            when (selectedFilter) {
                URUTKAN_TERBARU -> rbTerbaru.isChecked = true
                URUTKAN_TERlAMA -> rbTerlama.isChecked = true
                URUTKAN_NAMA_AZ -> rbSortAz.isChecked = true
                URUTKAN_NAMA_ZA -> rbSortZa.isChecked = true
                else -> (rgUrutan.getChildAt(0) as MaterialRadioButton).isChecked = true
            }
        }
    }

    private fun setEvent() {
        with(binding) {
            btnPilih.setOnClickListener {
                onSelectListener?.let {
                    it(getSelectedFilter())
                }
                dismiss()
            }

            btnReset.setOnClickListener {
                rgUrutan.clearCheck()
                (rgUrutan.getChildAt(0) as MaterialRadioButton).isChecked = true
            }
        }
    }

    private fun getSelectedFilter(): String {
        with(binding) {
            selectedFilter = when (rgUrutan.checkedRadioButtonId) {
                rbSortZa.id -> URUTKAN_NAMA_ZA
                rbSortAz.id -> URUTKAN_NAMA_AZ
                rbTerlama.id -> URUTKAN_TERlAMA
                else -> URUTKAN_TERBARU
            }
            return selectedFilter
        }
    }

    private var onSelectListener: ((String) -> Unit)? = null

    fun setOnSelectListener(listener: ((String) -> Unit)?) {
        onSelectListener = listener
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val TAG = "filter_bottom_sheet"
        const val URUTKAN_TERBARU = "Terbaru"
        const val URUTKAN_TERlAMA = "Terlama"
        const val URUTKAN_NAMA_AZ = "Nama A-Z"
        const val URUTKAN_NAMA_ZA = "Nama Z-A"
        private var selectedFilter = ""

        fun showDialog(
            selectedFilter: String,
            fragmentManager: FragmentManager
        ): FilterBottomSheet {
            this.selectedFilter = selectedFilter
            val dialog = FilterBottomSheet()
            dialog.show(fragmentManager, TAG)
            return dialog
        }
    }
}