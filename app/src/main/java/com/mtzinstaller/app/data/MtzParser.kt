package com.mtzinstaller.app.data

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.InputStream
import java.util.zip.ZipFile

/**
 * File .mtz adalah arsip ZIP. Di dalamnya biasanya ada:
 *  - description.xml  (judul, desainer, versi, deskripsi)
 *  - preview/         (gambar pratinjau)
 *  - framework-res, com.android.systemui, icons, wallpaper/, lockscreen, dll.
 */
object MtzParser {

    class Meta(
        val title: String?,
        val author: String?,
        val version: String?,
        val description: String?,
        val preview: ByteArray?,
    )

    class InvalidMtzException(message: String) : Exception(message)

    private val knownEntries = listOf(
        "description.xml", "framework-res", "framework-miui-res", "com.android.systemui",
        "com.android.settings", "com.miui.home", "icons", "lockscreen", "wallpaper/",
        "preview/", "fonts/", "boots/", "ringtones/",
    )
    private val imageExt = listOf(".jpg", ".jpeg", ".png", ".webp")

    fun parse(file: File): Meta {
        val zip = try {
            ZipFile(file)
        } catch (e: Exception) {
            throw InvalidMtzException("File ini bukan arsip .mtz yang valid")
        }
        return zip.use { z ->
            val entries = z.entries().asSequence().toList()
            val looksLikeTheme = entries.any { e ->
                knownEntries.any { k -> e.name.startsWith(k, ignoreCase = true) }
            }
            if (!looksLikeTheme) {
                throw InvalidMtzException("Arsip ini tidak berisi struktur tema MIUI/HyperOS")
            }

            val descEntry = entries.firstOrNull { it.name.equals("description.xml", ignoreCase = true) }
            val fields = descEntry?.let { e ->
                runCatching { z.getInputStream(e).use { readXml(it) } }.getOrDefault(emptyMap())
            } ?: emptyMap()

            val previewEntry = entries
                .filter { e ->
                    !e.isDirectory &&
                        e.name.startsWith("preview/", ignoreCase = true) &&
                        imageExt.any { e.name.endsWith(it, ignoreCase = true) }
                }
                .minByOrNull { previewRank(it.name) }
            val preview = previewEntry?.let { e -> z.getInputStream(e).use { it.readBytes() } }

            Meta(
                title = fields["title"],
                author = fields["designer"] ?: fields["author"],
                version = fields["version"],
                description = fields["description"],
                preview = preview,
            )
        }
    }

    private fun previewRank(name: String): String {
        val n = name.lowercase()
        val priority = when {
            "lockscreen" in n -> 0
            "home" in n || "desktop" in n || "launcher" in n -> 1
            "icon" in n -> 2
            else -> 3
        }
        return "$priority$n"
    }

    private fun readXml(input: InputStream): Map<String, String> {
        val out = mutableMapOf<String, String>()
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(input, null)
        var tag: String? = null
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> tag = parser.name?.lowercase()
                XmlPullParser.TEXT -> {
                    val text = parser.text?.trim()
                    val key = tag
                    if (!text.isNullOrEmpty() && key != null && key !in out) out[key] = text
                }
                XmlPullParser.END_TAG -> tag = null
            }
            event = parser.next()
        }
        return out
    }
}
