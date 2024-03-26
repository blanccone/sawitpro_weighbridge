package com.blanccone.sawitproweighbridge.ui.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blanccone.core.model.local.Ticket
import com.blanccone.core.R
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
            tvStatus.text = ticket.status
            ivStatusIcon.background = getStatusIcon(this.root, ticket.status)

            tvBeratKeluarLabel.isVisible = ticket.status != INBOUND
            tvBeratKeluarColon.isVisible = ticket.status != INBOUND
            tvBeratKeluar.apply {
                isVisible = ticket.status != INBOUND
                text = ticket.secondWeight.toString()
            }
            tvBeratBersihLabel.isVisible = isEditedOutbound(ticket) || ticket.status == DONE
            tvBeratBersihColon.isVisible = isEditedOutbound(ticket) || ticket.status == DONE
            tvBeratBersih.apply {
                isVisible = isEditedOutbound(ticket) || ticket.status == DONE
                text = getNettWeight(ticket)
            }
            cslBtn.isVisible = ticket.status != DONE

            btnSubmit.apply {
                isVisible = isEditedOutbound(ticket) || ticket.status == INBOUND
                setOnClickListener {
                    onItemClickListener?.let { it(ItemData(ACTION_SUBMIT, ticket)) }
                }
            }

            btnEdit.setOnClickListener {
                onItemClickListener?.let { it(ItemData(ACTION_EDIT, ticket)) }
            }

            root.setOnClickListener {
                onItemClickListener?.let { it(ItemData(ACTION_ITEM_CLICK, ticket)) }
            }
        }
    }

    private fun getNettWeight(ticket: Ticket): String {
        var nettWeight = 0
        with(ticket) {
            if (isEditedOutbound(this) || status == DONE) {
                nettWeight = firstWeight.toString().toInt() - secondWeight.toString().toInt()
            }
        }
        return nettWeight.toString()
    }

    private fun getStatusIcon(view: View, status: String?): Drawable? {
        return ContextCompat.getDrawable(
            view.context,
            when (status) {
                INBOUND -> R.drawable.ic_inbound_24
                OUTBOUND -> R.drawable.ic_outbound_24
                else -> R.drawable.ic_check_24
            }
        )
    }

    private fun isEditedOutbound(ticket: Ticket): Boolean {
        return ticket.status == OUTBOUND && ticket.secondWeight != NOT_EDITED
    }

    override fun getItemCount(): Int = tickets.size


    @SuppressLint("NotifyDataSetChanged")
    fun updateTickets(dataList: List<Ticket>) {
        tickets.clear()
        tickets.addAll(dataList)
        notifyDataSetChanged()
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
        private const val INBOUND = "Inbound"
        private const val OUTBOUND = "Outbound"
        private const val DONE = "Done"
    }
}