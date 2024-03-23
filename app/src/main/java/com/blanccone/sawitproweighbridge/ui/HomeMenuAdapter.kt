package com.blanccone.sawitproweighbridge.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.blanccone.core.model.local.Menu
import com.blanccone.sawitproweighbridge.databinding.ItemMenuBinding

class HomeMenuAdapter: RecyclerView.Adapter<HomeMenuAdapter.ViewHolder>() {

    private val menuList = arrayListOf<Menu>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMenuBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menu = menuList[position]
        with(holder.binding) {
            tvTitle.text = menu.title
            ivIcon.background = ContextCompat.getDrawable(root.context, menu.icon)

            root.setOnClickListener {
                onItemClickListener?.let { it(menu.id) }
            }
        }
    }

    override fun getItemCount(): Int = menuList.size

    fun submitData(dataList: List<Menu>) {
        menuList.apply {
            clear()
            addAll(dataList)
        }
        notifyItemRangeChanged(0, menuList.size)
    }

    inner class ViewHolder(var binding: ItemMenuBinding) : RecyclerView.ViewHolder(binding.root)

    private var onItemClickListener: ((String) -> Unit)? = null

    fun setOnItemClickListener(listener: ((String) -> Unit)) {
        onItemClickListener = listener
    }
}