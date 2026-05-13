package com.xuperiptv.data

data class Channel(
    val id: Long = System.nanoTime(),
    val name: String,
    val url: String,
    val logo: String? = null,
    val group: String = "Sin grupo",
    val tvgId: String? = null,
    val tvgName: String? = null,
    var isFavorite: Boolean = false
)

data class Playlist(
    val id: Long = System.nanoTime(),
    val name: String,
    val url: String,          // URL remota o ruta local
    val isLocal: Boolean = false,
    val lastUpdated: Long = 0L
)

data class ChannelGroup(
    val name: String,
    val channels: List<Channel>
)
