package com.blanccone.sawitproweighbridge.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blanccone.core.model.local.Ticket
import com.blanccone.sawitproweighbridge.databinding.ItemWeighmentTicketBinding


@SuppressLint("NotifyDataSetChanged")
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
            tvBeratMuatan.text = ticket.weight.toString()
            tvStatus.text = ticket.status

            btnSubmit.setOnClickListener {
                onSubmitClickListener?.let { it(ticket) }
            }
        }
    }

    override fun getItemCount(): Int = tickets.size

    fun updateTickets(dataList: List<Ticket>) {
        tickets.clear()
        tickets.addAll(dataList)
        notifyDataSetChanged()
    }

    private var onSubmitClickListener: ((Ticket) -> Unit)? = null

    fun setOnSubmitClickListener(listener: ((Ticket) -> Unit)) {
        onSubmitClickListener = listener
    }

    private var onEditClickListener: ((Ticket) -> Unit)? = null

    fun setOnEditClickListener(listener: ((Ticket) -> Unit)) {
        onSubmitClickListener = listener
    }
}