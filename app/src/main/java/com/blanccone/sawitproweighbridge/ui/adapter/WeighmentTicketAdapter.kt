package com.blanccone.sawitproweighbridge.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blanccone.core.model.local.Ticket
import com.blanccone.sawitproweighbridge.databinding.ItemWeighmentTicketBinding
import com.blanccone.sawitproweighbridge.ui.activity.EFormWeighmentActivity.Companion.NOT_EDITED

class WeighmentTicketAdapter: RecyclerView.Adapter<WeighmentTicketAdapter.ViewHolder>() {

    private val tickets = arrayListOf<Ticket>()

    inner class ViewHolder(
        val binding: ItemWeighmentTicketBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWeighmentTicketBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ticket = tickets[position]
        with(holder.binding) {
            tvTiket.text = ticket.id
            tvNoPol.text = ticket.licenseNumber?.uppercase()
            tvNama.text = ticket.driverName
            tvBeratMasuk.text = ticket.firstWeight.toString()
            tvBeratKeluarLabel.isVisible = ticket.status != "Inbound"
            tvBeratKeluarColon.isVisible = ticket.status != "Inbound"
            tvBeratKeluar.apply {
                isVisible = ticket.status != "Inbound"
                text = ticket.secondWeight.toString()
            }
            tvStatus.text = ticket.status
            cslBtn.isVisible = ticket.status != "Done"

            btnEdit.setOnClickListener {
                onItemClickListener?.let { it(ItemData(ACTION_EDIT, ticket)) }
            }

            btnSubmit.apply {
                isVisible = ticket.status == "Inbound" ||
                        (ticket.status == "Outbound" && tvBeratKeluar.text.toString() != NOT_EDITED)
                setOnClickListener {
                    onItemClickListener?.let { it(ItemData(ACTION_SUBMIT, ticket)) }
                }
            }

            root.setOnClickListener {
                onItemClickListener?.let { it(ItemData(ACTION_ITEM_CLICK, ticket)) }
            }
        }
    }

    override fun getItemCount(): Int = tickets.size


    fun updateTickets(dataList: List<Ticket>) {
        tickets.clear()
        tickets.addAll(dataList)
        notifyItemRangeChanged(0, tickets.size)
    }

    private var onItemClickListener: ((ItemData) -> Unit)? = null

    fun setOnItemClickListener(listener: ((ItemData) -> Unit)) {
        onItemClickListener = listener
    }

    data class ItemData(val action: String, val ticket: Ticket)

    companion object {
        const val ACTION_ITEM_CLICK = "item_click"
        const val ACTION_SUBMIT = "submit"
        const val ACTION_EDIT = "edit"
    }
}