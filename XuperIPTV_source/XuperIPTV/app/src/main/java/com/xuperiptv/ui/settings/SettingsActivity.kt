package com.xuperiptv.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.xuperiptv.R
import com.xuperiptv.data.Playlist
import com.xuperiptv.databinding.ActivitySettingsBinding
import com.xuperiptv.utils.PreferencesManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var adapter: PlaylistSettingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupList()
        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddPlaylist.setOnClickListener { showAddDialog() }
    }

    private fun setupList() {
        adapter = PlaylistSettingsAdapter { pl -> confirmDelete(pl) }
        binding.rvPlaylists.layoutManager = LinearLayoutManager(this)
        binding.rvPlaylists.adapter = adapter
        refreshList()
    }

    private fun refreshList() {
        adapter.submitList(PreferencesManager.loadPlaylists(this))
    }

    private fun showAddDialog() {
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
                    val list = PreferencesManager.loadPlaylists(this)
                    list.add(Playlist(name = name, url = url))
                    PreferencesManager.savePlaylists(this, list)
                    refreshList()
                    Toast.makeText(this, "Lista agregada", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmDelete(playlist: Playlist) {
        AlertDialog.Builder(this, R.style.DarkDialog)
            .setTitle("Eliminar lista")
            .setMessage("¿Eliminar \"${playlist.name}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                val list = PreferencesManager.loadPlaylists(this)
                list.removeAll { it.id == playlist.id }
                PreferencesManager.savePlaylists(this, list)
                refreshList()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
