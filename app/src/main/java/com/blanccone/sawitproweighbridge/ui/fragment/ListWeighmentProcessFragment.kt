package com.blanccone.sawitproweighbridge.ui.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.blanccone.core.model.local.Ticket
import com.blanccone.core.model.local.WeightImage
import com.blanccone.core.ui.adapter.FilterChipAdapter
import com.blanccone.core.ui.fragment.CoreFragment
import com.blanccone.core.ui.widget.FilterBottomSheet
import com.blanccone.core.util.FileUtils
import com.blanccone.core.util.Utils
import com.blanccone.core.util.Utils.toast
import com.blanccone.core.util.ViewUtils.stopRefresh
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
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ListWeighmentProcessFragment(
    private val ticketStatus: String
): CoreFragment<LayoutListWeighmentTicketBinding>() {

    private val viewModel: WeighmentViewModel by viewModels()

    private val filterAdapter by lazy { FilterChipAdapter() }
    private val ticketAdapter by lazy { WeighmentTicketAdapter() }

    @Inject
    internal lateinit var firebaseDb: DatabaseReference

    @Inject
    internal lateinit var storageDb: StorageReference

    private val tickets = arrayListOf<Ticket>()
    private var selectedFilter = "Terlama"

    private var validatedTicket = Ticket()
    private var validatedImage = WeightImage()

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            getActivityResult(it)
        }

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
            val status = if (ticketStatus == FIRST_WEIGHT) "Inbound" else "Outbound"
            val dataList = it.filter { ticket ->
                ticket.status == status
            }
            tickets.apply {
                clear()
                addAll(dataList)
            }
            binding.rvFilter.isVisible = tickets.isNotEmpty()
            updateTicketList(tickets)
        }
        viewModel.images.observe(viewLifecycleOwner) {
            val status = if (ticketStatus == FIRST_WEIGHT) "first" else "second"
            for (image in it) {
                if (image.id == "${image.ticketId}_$status") {
                    validatedImage = image
                    storeImageToFirebase()
                }
                break
            }
        }
        viewModel.insertTicketSuccessful.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) storeImageToLocal(it)
        }
        viewModel.insertImageSuccessful.observe(viewLifecycleOwner) {
            if (it) {
                toast("Data berhasil tersimpan")
                fetchFromLocal()
            }
        }
    }

    private fun fetchFromFirebase() {
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
                binding.rvFilter.isVisible = tickets.isNotEmpty()
                if (tickets.isNotEmpty()) {
                    updateTicketsToLocal(tickets)
                    binding.srlRefresh.stopRefresh()
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
            srlRefresh.setOnRefreshListener {
                if (!Utils.isConnected(requireContext())) {
                    fetchFromLocal()
                } else {
                    fetchFromFirebase()
                }
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

                WeighmentTicketAdapter.ACTION_EDIT -> {
                    val intent = EFormWeighmentActivity.resultInstance(
                        context = requireContext(),
                        status = ticketStatus,
                        data = it.ticket,
                        isRequested = true
                    )
                    resultLauncher.launch(intent)
                }

                WeighmentTicketAdapter.ACTION_SUBMIT -> setData(it.ticket)
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
            .child("${validatedTicket.id}_${ticketStatus}")
            .setValue(ticket)
            .addOnSuccessListener {
                viewModel.getimages("${validatedTicket.id}")
            }.addOnFailureListener {
                toast("Gagal menyimpan data")
            }
    }

    private fun storeImageToFirebase() {
        val fileName = "${validatedTicket.id}_$ticketStatus"
        val imageUri = File("${validatedImage.imagePath}").toUri()
        storageDb
            .child("${validatedTicket.id}")
            .child(fileName)
            .putFile(imageUri)
            .addOnSuccessListener {
                toast("Data berhasil tersimpan")
                storeDataToLocal()
            }
            .addOnFailureListener {
                showLoading(false)
                toast("Gagal menyimpan data")
            }
    }

    private fun storeDataToLocal() {
        viewModel.insertTicket(validatedTicket)
    }

    private fun storeImageToLocal(ticketId: String) {
        val image = WeightImage(
            id = "${validatedTicket.id}_$ticketStatus",
            ticketId = ticketId,
            image = validatedImage.image,
            imageName = validatedImage.imageName,
            imagePath = validatedImage.imagePath
        )
        viewModel.insertImage(image)
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
        binding.srlRefresh.isRefreshing = isLoading
    }

    private fun getActivityResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            fetchFromLocal()
        }
    }
}