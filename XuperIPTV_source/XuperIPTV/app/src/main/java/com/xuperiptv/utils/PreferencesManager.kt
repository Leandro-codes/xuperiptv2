package com.xuperiptv.utils

import android.content.Context
import android.content.SharedPreferences
import com.xuperiptv.data.Playlist
import org.json.JSONArray
import org.json.JSONObject

object PreferencesManager {

    private const val PREFS_NAME      = "xuperiptv_prefs"
    private const val KEY_PLAYLISTS   = "playlists"
    private const val KEY_FAVORITES   = "favorites"
    private const val KEY_LAST_CHANNEL = "last_channel_url"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Playlists ──────────────────────────────────────────────────────────

    fun savePlaylists(context: Context, playlists: List<Playlist>) {
        val array = JSONArray()
        playlists.forEach { p ->
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("name", p.name)
            obj.put("url", p.url)
            obj.put("isLocal", p.isLocal)
            obj.put("lastUpdated", p.lastUpdated)
            array.put(obj)
        }
        prefs(context).edit().putString(KEY_PLAYLISTS, array.toString()).apply()
    }

    fun loadPlaylists(context: Context): MutableList<Playlist> {
        val json = prefs(context).getString(KEY_PLAYLISTS, "[]") ?: "[]"
        val array = JSONArray(json)
        val list = mutableListOf<Playlist>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                Playlist(
                    id          = obj.getLong("id"),
                    name        = obj.getString("name"),
                    url         = obj.getString("url"),
                    isLocal     = obj.getBoolean("isLocal"),
                    lastUpdated = obj.getLong("lastUpdated")
                )
            )
        }
        return list
    }

    // ── Favoritos ──────────────────────────────────────────────────────────

    fun saveFavorites(context: Context, urls: Set<String>) {
        prefs(context).edit().putStringSet(KEY_FAVORITES, urls).apply()
    }

    fun loadFavorites(context: Context): MutableSet<String> =
        prefs(context).getStringSet(KEY_FAVORITES, mutableSetOf())?.toMutableSet()
            ?: mutableSetOf()

    // ── Último canal ───────────────────────────────────────────────────────

    fun saveLastChannel(context: Context, url: String) {
        prefs(context).edit().putString(KEY_LAST_CHANNEL, url).apply()
    }

    fun loadLastChannel(context: Context): String? =
        prefs(context).getString(KEY_LAST_CHANNEL, null)
}
