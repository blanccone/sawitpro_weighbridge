package com.blanccone.sawitproweighbridge.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.blanccone.core.model.local.Ticket
import com.blanccone.core.ui.adapter.FilterChipAdapter
import com.blanccone.core.ui.fragment.CoreFragment
import com.blanccone.core.ui.widget.FilterBottomSheet
import com.blanccone.core.ui.widget.FilterBottomSheet.Companion.URUTKAN_NAMA_AZ
import com.blanccone.core.ui.widget.FilterBottomSheet.Companion.URUTKAN_NAMA_ZA
import com.blanccone.core.ui.widget.FilterBottomSheet.Companion.URUTKAN_TERBARU
import com.blanccone.core.ui.widget.FilterBottomSheet.Companion.URUTKAN_TERlAMA
import com.blanccone.sawitproweighbridge.databinding.LayoutListWeighmentTicketBinding
import com.blanccone.sawitproweighbridge.ui.adapter.WeighmentTicketAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ListFirstWeightFragment: CoreFragment<LayoutListWeighmentTicketBinding>() {

    private val filterAdapter by lazy { FilterChipAdapter() }
    private val ticketAdapter by lazy { WeighmentTicketAdapter() }

    private lateinit var firebaseDb: DatabaseReference

    private val tickets = arrayListOf<Ticket>()
    private var selectedFilter = "Terlama"

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
        firebaseDb = FirebaseDatabase.getInstance().reference
        setTicketListView()
        setFilterListView()
        setEvent()
        fetchDataFromFirebase()
    }

    private fun fetchDataFromFirebase() {
        firebaseDb
            .child("tickets")
            .addValueEventListener(object : ValueEventListener {
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
                    updateTickets(tickets)
                }

                override fun onCancelled(error: DatabaseError) { }
            })
    }

    private fun updateTickets(dataList: List<Ticket>) {
        ticketAdapter.updateTickets(dataList)
    }

    private fun updateFilter() {
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
                        (it.licenseNumber != null && it.licenseNumber!!.contains(text, true))) {
                        searchedTickets.add(it)
                    }
                }
                updateTickets(searchedTickets)
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
                ticket.weighedOn ?.let {
                    calendar.time = dateFormat.parse(it) ?: Date(0)
                    calendar.time
                }
            }
            else -> tickets.sortedBy { ticket ->
                ticket.weighedOn ?.let {
                    calendar.time = dateFormat.parse(it) ?: Date(0)
                    calendar.time
                }
            }
        }
        updateFilter()
        updateTickets(sortedTickets)
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}