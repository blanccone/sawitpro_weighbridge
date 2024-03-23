package com.blanccone.sawitproweighbridge.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blanccone.core.ui.fragment.CoreFragment
import com.blanccone.sawitproweighbridge.databinding.LayoutListWeighmentTicketBinding

class ListSecondWeightFragment: CoreFragment<LayoutListWeighmentTicketBinding>() {

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): LayoutListWeighmentTicketBinding {
        return LayoutListWeighmentTicketBinding.inflate(
            inflater,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFilterView()
        setTicketListView()
        setEvent()
    }

    private fun setFilterView() {
        TODO("Not yet implemented")
    }

    private fun setTicketListView() {
        TODO("Not yet implemented")
    }

    private fun setEvent() {

    }
}