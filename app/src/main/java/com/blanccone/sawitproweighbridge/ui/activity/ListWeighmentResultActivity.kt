package com.blanccone.sawitproweighbridge.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.blanccone.core.model.local.Ticket
import com.blanccone.core.ui.activity.CoreActivity
import com.blanccone.core.ui.adapter.FilterChipAdapter
import com.blanccone.core.ui.widget.FilterBottomSheet
import com.blanccone.core.ui.widget.LoadingDialog
import com.blanccone.core.util.Utils
import com.blanccone.sawitproweighbridge.databinding.ActivityListWeighmentResultBinding
import com.blanccone.sawitproweighbridge.ui.adapter.WeighmentTicketAdapter
import com.blanccone.sawitproweighbridge.ui.viewmodel.WeighmentViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ListWeighmentResultActivity : CoreActivity<ActivityListWeighmentResultBinding>() {

    private val viewModel: WeighmentViewModel by viewModels()

    private val filterAdapter by lazy { FilterChipAdapter() }
    private val ticketAdapter by lazy { WeighmentTicketAdapter() }

    @Inject
    internal lateinit var firebaseDb: DatabaseReference

    private val tickets = arrayListOf<Ticket>()
    private var selectedFilter = "Terlama"

    override fun inflateLayout(inflater: LayoutInflater): ActivityListWeighmentResultBinding {
        return ActivityListWeighmentResultBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Weighment Results"
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        setTicketListView()
        setFilterListView()
        setEvent()
        setObserves()
        setFirebaseObserves()
        fetchFromLocal()
    }

    private fun setObserves() {
        viewModel.isLoading.observe(this) {
            it?.let { isLoading ->
                showLoading(isLoading)
            }
        }
        viewModel.tickets.observe(this) {
            val dataList = it.filter { ticket ->
                ticket.status == "Done"
            }
            tickets.addAll(dataList)
            updateTicketList(tickets)
        }
    }

    private fun setFirebaseObserves() {
        firebaseDb.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tickets.clear()
                for (ticketSnapshot in snapshot.children) {
                    val ticket = ticketSnapshot.getValue(Ticket::class.java)
                    ticket?.let {
                        if (ticket.status == "Done") {
                            tickets.add(it)
                        }
                    }
                }
                updateTicketsToLocal(tickets)
            }

            override fun onCancelled(error: DatabaseError) {
                if (!Utils.isConnected(this@ListWeighmentResultActivity)) {
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
        binding.layoutListWeighment.rvFilter.isVisible = dataList.isNotEmpty()
        ticketAdapter.updateTickets(dataList)
    }

    private fun updateFilterList() {
        filterAdapter.submitData(arrayListOf(selectedFilter))
    }

    private fun setFilterListView() {
        binding.layoutListWeighment.rvFilter.adapter = filterAdapter
    }

    private fun setTicketListView() {
        binding.layoutListWeighment.rvTicket.adapter = ticketAdapter
    }

    private fun setEvent() {
        with(binding.layoutListWeighment) {
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
            if (it.action == WeighmentTicketAdapter.ACTION_ITEM_CLICK) {
                EFormWeighmentActivity.newInstance(
                    context = this,
                    status = EFormWeighmentActivity.DONE,
                    data = it.ticket,
                    isPreviewOnly = true
                )
            }
        }
    }

    private fun showFilterBottomSheet() {
        FilterBottomSheet
            .showDialog(
                selectedFilter,
                supportFragmentManager
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
            LoadingDialog.showDialog(supportFragmentManager)
        } else {
            LoadingDialog.dismissDialog(supportFragmentManager)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        fun newInstance(context: Context) {
            val intent = Intent(context, ListWeighmentResultActivity::class.java)
            context.startActivity(intent)
        }
    }
}