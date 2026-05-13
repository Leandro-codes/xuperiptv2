package com.xuperiptv.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.xuperiptv.data.Channel
import com.xuperiptv.data.ChannelGroup
import com.xuperiptv.data.Playlist
import com.xuperiptv.utils.M3UParser
import com.xuperiptv.utils.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.TimeUnit

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _groups     = MutableLiveData<List<ChannelGroup>>()
    val groups: LiveData<List<ChannelGroup>> = _groups

    private val _allChannels = MutableLiveData<List<Channel>>()
    val allChannels: LiveData<List<Channel>> = _allChannels

    private val _favorites  = MutableLiveData<List<Channel>>()
    val favorites: LiveData<List<Channel>> = _favorites

    private val _playlists  = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    private val _loading    = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error      = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var rawChannels: List<Channel> = emptyList()
    private var favoriteUrls: MutableSet<String> = mutableSetOf()

    init {
        loadPlaylists()
    }

    fun loadPlaylists() {
        val ctx = getApplication<Application>()
        val saved = PreferencesManager.loadPlaylists(ctx)
        favoriteUrls = PreferencesManager.loadFavorites(ctx)
        _playlists.value = saved
        if (saved.isNotEmpty()) {
            loadAllChannels(saved)
        }
    }

    fun addPlaylist(playlist: Playlist) {
        val current = PreferencesManager.loadPlaylists(getApplication())
        current.add(playlist)
        PreferencesManager.savePlaylists(getApplication(), current)
        _playlists.value = current
        loadAllChannels(current)
    }

    fun removePlaylist(playlist: Playlist) {
        val current = PreferencesManager.loadPlaylists(getApplication())
        current.removeAll { it.id == playlist.id }
        PreferencesManager.savePlaylists(getApplication(), current)
        _playlists.value = current
        loadAllChannels(current)
    }

    private fun loadAllChannels(playlists: List<Playlist>) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val channels = withContext(Dispatchers.IO) {
                    val result = mutableListOf<Channel>()
                    playlists.forEach { pl ->
                        try {
                            val stream = if (pl.isLocal) {
                                FileInputStream(File(pl.url))
                            } else {
                                val req = Request.Builder().url(pl.url).build()
                                client.newCall(req).execute().body!!.byteStream()
                            }
                            result.addAll(M3UParser.parse(stream))
                        } catch (e: Exception) {
                            // Ignorar errores individuales de playlist
                        }
                    }
                    result
                }

                rawChannels = channels.map { ch ->
                    ch.copy(isFavorite = favoriteUrls.contains(ch.url))
                }

                buildGroupsAndFavorites()

            } catch (e: Exception) {
                _error.value = "Error al cargar canales: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    private fun buildGroupsAndFavorites() {
        val grouped = rawChannels
            .groupBy { it.group }
            .map { (g, chs) -> ChannelGroup(g, chs) }
            .sortedBy { it.name }

        _groups.value    = grouped
        _allChannels.value = rawChannels
        _favorites.value = rawChannels.filter { it.isFavorite }
    }

    fun toggleFavorite(channel: Channel) {
        if (favoriteUrls.contains(channel.url)) {
            favoriteUrls.remove(channel.url)
        } else {
            favoriteUrls.add(channel.url)
        }
        PreferencesManager.saveFavorites(getApplication(), favoriteUrls)
        rawChannels = rawChannels.map { ch ->
            if (ch.url == channel.url) ch.copy(isFavorite = favoriteUrls.contains(ch.url))
            else ch
        }
        buildGroupsAndFavorites()
    }

    fun search(query: String): List<Channel> {
        if (query.isBlank()) return rawChannels
        return rawChannels.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.group.contains(query, ignoreCase = true)
        }
    }
}
