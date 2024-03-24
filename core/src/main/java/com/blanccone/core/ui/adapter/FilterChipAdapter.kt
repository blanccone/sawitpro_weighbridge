package com.blanccone.core.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blanccone.core.databinding.ItemSelectedFilterBinding

@SuppressLint("NotifyDataSetChanged")
class FilterChipAdapter: RecyclerView.Adapter<FilterChipAdapter.ViewHolder>() {

    private val filterList = arrayListOf("Terlama")

    inner class ViewHolder(
        val binding: ItemSelectedFilterBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSelectedFilterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val filter = filterList[position]
        holder.binding.tvValueFilterChip.text = filter
    }

    override fun getItemCount(): Int = filterList.size

    fun submitData(dataList: List<String>) {
        filterList.clear()
        filterList.addAll(dataList)
        notifyDataSetChanged()
    }
}