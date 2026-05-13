package com.xuperiptv.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xuperiptv.R
import com.xuperiptv.data.Channel
import com.xuperiptv.databinding.ItemChannelBinding

class ChannelAdapter(
    private val onPlay: (Channel) -> Unit,
    private val onFavorite: (Channel) -> Unit
) : ListAdapter<Channel, ChannelAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemChannelBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(channel: Channel) {
            b.tvChannelName.text  = channel.name
            b.tvChannelGroup.text = channel.group

            // Logo con Glide
            if (!channel.logo.isNullOrBlank()) {
                Glide.with(b.ivLogo)
                    .load(channel.logo)
                    .placeholder(R.drawable.ic_channel_placeholder)
                    .error(R.drawable.ic_channel_placeholder)
                    .into(b.ivLogo)
            } else {
                b.ivLogo.setImageResource(R.drawable.ic_channel_placeholder)
            }

            // Favorito
            b.btnFavorite.setImageResource(
                if (channel.isFavorite) R.drawable.ic_star_filled
                else R.drawable.ic_star_outline
            )

            b.root.setOnClickListener    { onPlay(channel) }
            b.root.setOnLongClickListener { onFavorite(channel); true }
            b.btnFavorite.setOnClickListener { onFavorite(channel) }

            // Accesibilidad TV: foco visual
            b.root.isFocusable = true
            b.root.isFocusableInTouchMode = false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Channel>() {
            override fun areItemsTheSame(a: Channel, b: Channel) = a.url == b.url
            override fun areContentsTheSame(a: Channel, b: Channel) = a == b
        }
    }
}
