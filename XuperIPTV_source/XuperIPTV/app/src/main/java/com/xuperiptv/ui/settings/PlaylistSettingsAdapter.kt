package com.xuperiptv.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xuperiptv.data.Playlist
import com.xuperiptv.databinding.ItemPlaylistSettingBinding

class PlaylistSettingsAdapter(
    private val onDelete: (Playlist) -> Unit
) : ListAdapter<Playlist, PlaylistSettingsAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemPlaylistSettingBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(pl: Playlist) {
            b.tvPlaylistName.text = pl.name
            b.tvPlaylistUrl.text  = pl.url
            b.btnDelete.setOnClickListener { onDelete(pl) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemPlaylistSettingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Playlist>() {
            override fun areItemsTheSame(a: Playlist, b: Playlist) = a.id == b.id
            override fun areContentsTheSame(a: Playlist, b: Playlist) = a == b
        }
    }
}
