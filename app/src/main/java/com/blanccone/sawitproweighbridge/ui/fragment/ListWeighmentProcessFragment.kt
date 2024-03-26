package com.blanccone.sawitproweighbridge.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import com.blanccone.core.model.local.Ticket
import com.blanccone.core.ui.adapter.FilterChipAdapter
import com.blanccone.core.ui.fragment.CoreFragment
import com.blanccone.core.ui.widget.ActionBottomSheetDialog
import com.blanccone.core.ui.widget.FilterBottomSheet
import com.blanccone.core.ui.widget.LoadingDialog
import com.blanccone.core.util.Utils
import com.blanccone.core.util.Utils.toast
import com.blanccone.sawitproweighbridge.databinding.LayoutListWeighmentTicketBinding
import com.blanccone.sawitproweighbridge.ui.activity.EFormWeighmentActivity
import com.blanccone.sawitproweighbridge.ui.activity.EFormWeighmentActivity.Companion.FIRST_WEIGHT
import com.blanccone.sawitproweighbridge.ui.adapter.WeighmentTicketAdapter
import com.blanccone.sawitproweighbridge.ui.viewmodel.WeighmentViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ListWeighmentProcessFragment : CoreFragment<LayoutListWeighmentTicketBinding>() {

    private val viewModel: WeighmentViewModel by activityViewModels()

    private val filterAdapter by lazy { FilterChipAdapter() }
    private val ticketAdapter by lazy { WeighmentTicketAdapter() }

    @Inject
    internal lateinit var firebaseDb: DatabaseReference

    @Inject
    internal lateinit var storageDb: StorageReference

    private val tickets = arrayListOf<Ticket>()
    private var selectedFilter = "Terlama"

    private var validatedTicket = Ticket()

    private lateinit var ticketStatus: String

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
        ticketStatus = arguments?.getString("ticketStatus") ?: ""
        setTicketListView()
        setFilterListView()
        setEvent()
        setObserves()
        setFirebaseObserves()
        fetchFromLocal()
    }

    private fun setObserves() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            it?.let { isLoading ->
                showLoading(isLoading)
            }
        }

        viewModel.tickets.observe(viewLifecycleOwner) {
            val status = if (ticketStatus == FIRST_WEIGHT) "Inbound" else "Outbound"
            val dataList = it.filter { ticket ->
                ticket.status == status
            }
            tickets.apply {
                clear()
                addAll(dataList)
            }
            updateTicketList(tickets)
        }

        viewModel.updateTicketSuccessful.observe(viewLifecycleOwner) {
            it?.let { isSuccessful ->
                if (isSuccessful) {
                    if (ticketStatus == FIRST_WEIGHT) {
                        toast("Data berhasil tersimpan ke Second Weight")
                    } else {
                        requireActivity().apply {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun setFirebaseObserves() {
        firebaseDb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tickets.clear()
                val status = if (ticketStatus == FIRST_WEIGHT) "Inbound" else "Outbound"
                for (ticketSnapshot in snapshot.children) {
                    val ticket = ticketSnapshot.getValue(Ticket::class.java)
                    ticket?.let {
                        if (ticket.status == status) {
                            tickets.add(it)
                        }
                    }
                }
                if (tickets.isNotEmpty()) {
                    updateTicketsToLocal(tickets)
                    updateTicketList(tickets)
                } else {
                    fetchFromLocal()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!Utils.isConnected(requireContext())) {
                    fetchFromLocal()
                }
            }
        })
    }

    private fun fetchFromLocal() {
        viewModel.getTickets()
    }

    private fun updateTicketsToLocal(dataList: List<Ticket>) {
        viewModel.insertTickets(dataList)
    }

    private fun updateTicketList(dataList: List<Ticket>) {
        binding.rvFilter.isVisible = dataList.isNotEmpty()
        ticketAdapter.updateTickets(dataList)
    }

    private fun updateFilterList() {
        filterAdapter.submitData(arrayListOf(selectedFilter))
    }

    private fun setFilterListView() {
        binding.rvFilter.adapter = filterAdapter
    }

    private fun setTicketListView() {
        binding.rvTicket.adapter = ticketAdapter
    }

    private fun setEvent() {
        with(binding) {
            srlRefresh.setOnRefreshListener {
                srlRefresh.isRefreshing = false
                fetchFromLocal()
            }
            with(layoutSearch) {
                etSearch.doAfterTextChanged {
                    searchData(it.toString())
                }
                btnFilter.setOnClickListener {
                    showFilterBottomSheet()
                }
            }
        }
        ticketAdapter.setOnItemClickListener {
            when (it.action) {
                WeighmentTicketAdapter.ACTION_ITEM_CLICK -> EFormWeighmentActivity.newInstance(
                    context = requireContext(),
                    status = ticketStatus,
                    data = it.ticket,
                    isPreviewOnly = true
                )

                WeighmentTicketAdapter.ACTION_EDIT -> EFormWeighmentActivity.newInstance(
                    context = requireContext(),
                    status = ticketStatus,
                    data = it.ticket,
                    isRequested = true
                )

                WeighmentTicketAdapter.ACTION_SUBMIT -> {
                    ActionBottomSheetDialog.showDialog(childFragmentManager, "Submit").apply {
                        setOnItemClickListener {
                            setData(it.ticket)
                        }
                    }
                }
            }
        }
    }

    private fun setData(ticket: Ticket) {
        validatedTicket = ticket.apply {
            status = if (ticketStatus == FIRST_WEIGHT) "Outbound" else "Done"
        }
        storeDataToFirebase(validatedTicket.toMap())
    }

    private fun storeDataToFirebase(ticket: Map<String, Any?>) {
        showLoading(true)
        firebaseDb
            .child("${validatedTicket.id}")
            .setValue(ticket)
            .addOnSuccessListener {
                updateDataToLocal()
            }.addOnFailureListener {
                showLoading(false)
                toast("Gagal menyimpan data")
            }
    }

    private fun updateDataToLocal() {
        viewModel.updateTicket(validatedTicket)
    }

    private fun showFilterBottomSheet() {
        FilterBottomSheet
            .showDialog(
                selectedFilter,
                childFragmentManager
            ).apply {
                setOnSelectListener {
                    selectedFilter = it
                    filterData()
                }
            }
    }

    private fun searchData(text: String) {
        val searchedTickets = arrayListOf<Ticket>()
        if (text.isEmpty()) {
            searchedTickets.addAll(tickets)
        } else {
            if (tickets.isNotEmpty()) {
                tickets.forEach {
                    if ((it.driverName != null && it.driverName!!.contains(text, true)) ||
                        (it.licenseNumber != null && it.licenseNumber!!.contains(text, true))
                    ) {
                        searchedTickets.add(it)
                    }
                }
                updateTicketList(searchedTickets)
            }
        }
    }

    private fun filterData() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val sortedTickets = when (selectedFilter) {
            FilterBottomSheet.URUTKAN_NAMA_AZ -> tickets.sortedByDescending { it.driverName }
            FilterBottomSheet.URUTKAN_NAMA_ZA -> tickets.sortedBy { it.driverName }
            FilterBottomSheet.URUTKAN_TERBARU -> tickets.sortedByDescending { ticket ->
                ticket.firstWeighedOn?.let {
                    calendar.time = dateFormat.parse(it) ?: Date(0)
                    calendar.time
                }
            }

            else -> tickets.sortedBy { ticket ->
                ticket.firstWeighedOn?.let {
                    calendar.time = dateFormat.parse(it) ?: Date(0)
                    calendar.time
                }
            }
        }
        updateFilterList()
        updateTicketList(sortedTickets)
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            LoadingDialog.showDialog(childFragmentManager)
        } else {
            LoadingDialog.dismissDialog(childFragmentManager)
        }
    }

    companion object {
        fun newInstance(status: String): ListWeighmentProcessFragment {
            val args = Bundle().apply {
                putString("ticketStatus", status)
            }
            return ListWeighmentProcessFragment().apply {
                arguments = args
            }
        }
    }
}