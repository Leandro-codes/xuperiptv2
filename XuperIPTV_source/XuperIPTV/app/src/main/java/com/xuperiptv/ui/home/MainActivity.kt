package com.xuperiptv.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuperiptv.R
import com.xuperiptv.data.Channel
import com.xuperiptv.data.Playlist
import com.xuperiptv.databinding.ActivityMainBinding
import com.xuperiptv.ui.player.PlayerActivity
import com.xuperiptv.ui.settings.SettingsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private lateinit var groupAdapter: GroupAdapter
    private lateinit var channelAdapter: ChannelAdapter

    private var currentTab = Tab.CHANNELS

    enum class Tab { CHANNELS, FAVORITES, SEARCH, SETTINGS }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSidebar()
        setupChannelList()
        setupObservers()
        setupSearch()
        selectTab(Tab.CHANNELS)
    }

    private fun setupSidebar() {
        binding.navChannels.setOnClickListener  { selectTab(Tab.CHANNELS) }
        binding.navFavorites.setOnClickListener { selectTab(Tab.FAVORITES) }
        binding.navSearch.setOnClickListener    { selectTab(Tab.SEARCH) }
        binding.navSettings.setOnClickListener  {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.navAddPlaylist.setOnClickListener { showAddPlaylistDialog() }
    }

    private fun setupChannelList() {
        channelAdapter = ChannelAdapter(
            onPlay      = { ch -> openPlayer(ch) },
            onFavorite  = { ch -> viewModel.toggleFavorite(ch) }
        )

        groupAdapter = GroupAdapter { ch -> openPlayer(ch) }

        binding.rvChannels.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter        = channelAdapter
        }
    }

    private fun setupObservers() {
        viewModel.loading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { err ->
            err?.let { Toast.makeText(this, it, Toast.LENGTH_LONG).show() }
        }

        viewModel.allChannels.observe(this) { channels ->
            if (currentTab == Tab.CHANNELS) channelAdapter.submitList(channels)
        }

        viewModel.favorites.observe(this) { favs ->
            if (currentTab == Tab.FAVORITES) channelAdapter.submitList(favs)
        }

        viewModel.groups.observe(this) { groups ->
            binding.tvChannelCount.text = "${viewModel.allChannels.value?.size ?: 0} canales"
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { editable ->
            val q = editable.toString()
            val results = viewModel.search(q)
            channelAdapter.submitList(results)
        }
    }

    private fun selectTab(tab: Tab) {
        currentTab = tab
        // Reset UI
        binding.navChannels.isSelected  = tab == Tab.CHANNELS
        binding.navFavorites.isSelected = tab == Tab.FAVORITES
        binding.navSearch.isSelected    = tab == Tab.SEARCH

        // Mostrar/ocultar barra de búsqueda
        binding.searchBar.visibility = if (tab == Tab.SEARCH) View.VISIBLE else View.GONE

        // Actualizar lista
        when (tab) {
            Tab.CHANNELS  -> channelAdapter.submitList(viewModel.allChannels.value ?: emptyList())
            Tab.FAVORITES -> channelAdapter.submitList(viewModel.favorites.value ?: emptyList())
            Tab.SEARCH    -> {
                channelAdapter.submitList(viewModel.allChannels.value ?: emptyList())
                binding.etSearch.requestFocus()
            }
            Tab.SETTINGS  -> {}
        }
        binding.rvChannels.adapter = channelAdapter
        binding.tvTabTitle.text = when (tab) {
            Tab.CHANNELS  -> "Todos los canales"
            Tab.FAVORITES -> "Favoritos"
            Tab.SEARCH    -> "Buscar"
            Tab.SETTINGS  -> "Ajustes"
        }
    }

    private fun openPlayer(channel: Channel) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_URL,  channel.url)
            putExtra(PlayerActivity.EXTRA_NAME, channel.name)
            putExtra(PlayerActivity.EXTRA_LOGO, channel.logo)
        }
        startActivity(intent)
    }

    private fun showAddPlaylistDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_playlist, null)
        val etName = view.findViewById<android.widget.EditText>(R.id.etPlaylistName)
        val etUrl  = view.findViewById<android.widget.EditText>(R.id.etPlaylistUrl)

        AlertDialog.Builder(this, R.style.DarkDialog)
            .setTitle("Agregar lista M3U")
            .setView(view)
            .setPositiveButton("Agregar") { _, _ ->
                val name = etName.text.toString().trim()
                val url  = etUrl.text.toString().trim()
                if (name.isNotEmpty() && url.isNotEmpty()) {
                    viewModel.addPlaylist(Playlist(name = name, url = url))
                    Toast.makeText(this, "Lista agregada", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_MENU -> {
                showAddPlaylistDialog()
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}
