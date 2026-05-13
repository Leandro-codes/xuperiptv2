package com.xuperiptv.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.xuperiptv.data.Channel
import com.xuperiptv.data.ChannelGroup
import com.xuperiptv.databinding.ItemGroupBinding

class GroupAdapter(
    private val onPlay: (Channel) -> Unit
) : RecyclerView.Adapter<GroupAdapter.VH>() {

    private var groups: List<ChannelGroup> = emptyList()
    private val expanded = mutableSetOf<String>()

    fun submitGroups(list: List<ChannelGroup>) {
        groups = list
        notifyDataSetChanged()
    }

    inner class VH(private val b: ItemGroupBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(group: ChannelGroup) {
            b.tvGroupName.text  = group.name
            b.tvGroupCount.text = "${group.channels.size}"

            val isExpanded = expanded.contains(group.name)
            b.ivArrow.rotation = if (isExpanded) 90f else 0f

            if (isExpanded) {
                b.rvGroupChannels.visibility = android.view.View.VISIBLE
                val adapter = ChannelAdapter(onPlay) { }
                b.rvGroupChannels.layoutManager = LinearLayoutManager(b.root.context)
                b.rvGroupChannels.adapter = adapter
                adapter.submitList(group.channels)
            } else {
                b.rvGroupChannels.visibility = android.view.View.GONE
            }

            b.root.setOnClickListener {
                if (isExpanded) expanded.remove(group.name) else expanded.add(group.name)
                notifyItemChanged(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(groups[position])
    override fun getItemCount() = groups.size
}
