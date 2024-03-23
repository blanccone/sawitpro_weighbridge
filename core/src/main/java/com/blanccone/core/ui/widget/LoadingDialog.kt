package com.blanccone.core.ui.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.blanccone.core.R
import com.blanccone.core.databinding.DialogLoadingBinding

class LoadingDialog: DialogFragment() {

    private lateinit var binding: DialogLoadingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogLoadingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false
        binding.piLoading.isIndeterminate = true
    }

    override fun getTheme(): Int = R.style.RoundedAlertDialog

    override fun onResume() {
        super.onResume()
        binding.piLoading.isIndeterminate = true
    }

    override fun onPause() {
        super.onPause()
        binding.piLoading.isIndeterminate = false
    }

    companion object {
        private const val TAG = "LOADING_DIALOG"

        fun showDialog(fragmentManager: FragmentManager) {
            val dialog = LoadingDialog()
            dialog.show(fragmentManager, TAG)
        }

        fun dismissDialog(fragmentManager: FragmentManager){
            try {
                val dialog =  fragmentManager.findFragmentByTag(TAG)
                if (dialog is DialogFragment) {
                    dialog.dismiss()
                }
            } catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
}