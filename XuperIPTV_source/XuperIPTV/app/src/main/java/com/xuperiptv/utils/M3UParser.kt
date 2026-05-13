package com.xuperiptv.utils

import com.xuperiptv.data.Channel
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object M3UParser {

    /**
     * Parsea un InputStream M3U y devuelve la lista de canales.
     */
    fun parse(inputStream: InputStream): List<Channel> {
        val channels = mutableListOf<Channel>()
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))

        var currentName = ""
        var currentLogo: String? = null
        var currentGroup = "Sin grupo"
        var currentTvgId: String? = null
        var currentTvgName: String? = null

        reader.forEachLine { rawLine ->
            val line = rawLine.trim()
            when {
                line.startsWith("#EXTINF:") -> {
                    // Extraer atributos
                    currentTvgId    = extractAttr(line, "tvg-id")
                    currentTvgName  = extractAttr(line, "tvg-name")
                    currentLogo     = extractAttr(line, "tvg-logo")
                    currentGroup    = extractAttr(line, "group-title") ?: "Sin grupo"
                    // Nombre al final de la línea (después de la última coma)
                    currentName     = line.substringAfterLast(",").trim()
                }
                line.isNotEmpty() && !line.startsWith("#") -> {
                    if (currentName.isNotEmpty()) {
                        channels.add(
                            Channel(
                                name      = currentName,
                                url       = line,
                                logo      = currentLogo,
                                group     = currentGroup,
                                tvgId     = currentTvgId,
                                tvgName   = currentTvgName
                            )
                        )
                    }
                    // Reset
                    currentName    = ""
                    currentLogo    = null
                    currentGroup   = "Sin grupo"
                    currentTvgId   = null
                    currentTvgName = null
                }
            }
        }
        return channels
    }

    private fun extractAttr(line: String, attr: String): String? {
        val regex = Regex("""$attr="([^"]*?)"""")
        return regex.find(line)?.groupValues?.get(1)?.takeIf { it.isNotBlank() }
    }
}
