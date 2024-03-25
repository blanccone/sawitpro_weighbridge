package com.blanccone.sawitproweighbridge.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.blanccone.core.model.local.Ticket
import com.blanccone.core.ui.adapter.FilterChipAdapter
import com.blanccone.core.ui.fragment.CoreFragment
import com.blanccone.core.ui.widget.FilterBottomSheet
import com.blanccone.core.ui.widget.FilterBottomSheet.Companion.URUTKAN_NAMA_AZ
import com.blanccone.core.ui.widget.FilterBottomSheet.Companion.URUTKAN_NAMA_ZA
import com.blanccone.core.ui.widget.FilterBottomSheet.Companion.URUTKAN_TERBARU
import com.blanccone.core.ui.widget.LoadingDialog
import com.blanccone.core.util.Utils
import com.blanccone.core.util.Utils.toast
import com.blanccone.sawitproweighbridge.databinding.LayoutListWeighmentTicketBinding
import com.blanccone.sawitproweighbridge.ui.activity.EFormWeighmentActivity
import com.blanccone.sawitproweighbridge.ui.adapter.WeighmentTicketAdapter
import com.blanccone.sawitproweighbridge.ui.adapter.WeighmentTicketAdapter.Companion.ACTION_EDIT
import com.blanccone.sawitproweighbridge.ui.adapter.WeighmentTicketAdapter.Companion.ACTION_ITEM_CLICK
import com.blanccone.sawitproweighbridge.ui.adapter.WeighmentTicketAdapter.Companion.ACTION_SUBMIT
import com.blanccone.sawitproweighbridge.ui.viewmodel.WeighmentViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ListFirstWeightFragment : CoreFragment<LayoutListWeighmentTicketBinding>() {

    private val viewModel: WeighmentViewModel by viewModels()

    private val filterAdapter by lazy { FilterChipAdapter() }
    private val ticketAdapter by lazy { WeighmentTicketAdapter() }

    @Inject
    internal lateinit var firebaseDb: DatabaseReference

    @Inject
    internal lateinit var storageDb: StorageReference

    private val tickets = arrayListOf<Ticket>()
    private var selectedFilter = "Terlama"
    private var imagePath = ""

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
        setTicketListView()
        setFilterListView()
        setEvent()
        setObserves()
        fetchFromFirebase()
    }

    private fun setObserves() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            it?.let { isLoading ->
                showLoading(isLoading)
            }
        }
        viewModel.tickets.observe(viewLifecycleOwner) {
            val dataList = it.filter { ticket ->
                ticket.status == "Inbound"
            }
            updateTicketList(dataList)
        }
        viewModel.images.observe(viewLifecycleOwner) {
            for (image in it) {
                if (image.imageName!!.contains("first", true)) {
                    imagePath = image.imagePath!!
                    submitImage(image.ticketId!!)
                }
                break
            }
        }
    }

    private fun fetchFromFirebase() {
        firebaseDb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tickets.clear()
                for (ticketSnapshot in snapshot.children) {
                    val ticket = ticketSnapshot.getValue(Ticket::class.java)
                    ticket?.let {
                        if (ticket.status == "Inbound") {
                            tickets.add(it)
                        }
                    }
                }
                binding.rvFilter.isVisible = tickets.isNotEmpty()
                updateTicketsToLocal(tickets)
            }

            override fun onCancelled(error: DatabaseError) {
                if (!Utils.isConnected(requireContext())) {
                    fetchFromLocal()
                }
            }
        })
    }

    private fun fetchFromLocal() {
        viewModel.gettickets()
    }

    private fun updateTicketsToLocal(dataList: List<Ticket>) {
        viewModel.insertTickets(dataList)
    }

    private fun updateTicketList(dataList: List<Ticket>) {
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
                ACTION_ITEM_CLICK -> EFormWeighmentActivity.newInstance(
                    context = requireContext(),
                    status = EFormWeighmentActivity.FIRST_WEIGHT,
                    data = it.ticket
                )

                ACTION_EDIT -> EFormWeighmentActivity.newInstance(
                    context = requireContext(),
                    status = EFormWeighmentActivity.FIRST_WEIGHT,
                    data = it.ticket,
                    isRequested = true
                )

                ACTION_SUBMIT -> submitData(it.ticket)
            }
        }
    }

    private fun submitData(ticket: Ticket) {
        showLoading(true)
        firebaseDb
            .push()
            .setValue(ticket.toMap())
            .addOnSuccessListener {
                viewModel.getimages("${ticket.id}")
            }
            .addOnFailureListener {
                showLoading(false)
                toast("Gagal menyimpan data")
            }
    }

    private fun submitImage(ticketId: String) {
        val imageUri = File(imagePath).toUri()
        storageDb
            .child(ticketId)
            .putFile(imageUri)
            .addOnSuccessListener {
                toast("Data berhasil tersimpan")
                showLoading(false)
                fetchFromLocal()
            }
            .addOnFailureListener {
                showLoading(false)
                toast("Gagal menyimpan data")
            }
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
            URUTKAN_NAMA_AZ -> tickets.sortedByDescending { it.driverName }
            URUTKAN_NAMA_ZA -> tickets.sortedBy { it.driverName }
            URUTKAN_TERBARU -> tickets.sortedByDescending { ticket ->
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
}