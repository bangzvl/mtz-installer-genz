package com.mtzinstaller.app.data

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class CatalogItem(
    val id: String,
    val title: String,
    val author: String,
    val version: String,
    val category: String,
    val description: String,
    val sizeBytes: Long,
    val previewUrl: String?,
    val downloadUrl: String,
)

/**
 * Membaca katalog tema dari file JSON online. Format yang didukung:
 * { "themes": [ { "id", "title", "author", "version", "category", "description",
 *                 "size", "preview", "download" } ] }
 * atau langsung array [ {...}, {...} ].
 */
object CatalogRepository {

    fun fetch(url: String): List<CatalogItem> {
        val text = open(url).use { it.inputStream.bufferedReader().readText() }.trim()
        val arr = if (text.startsWith("[")) JSONArray(text) else JSONObject(text).getJSONArray("themes")
        return (0 until arr.length()).mapNotNull { i ->
            val o = arr.optJSONObject(i) ?: return@mapNotNull null
            val download = o.optString("download").ifBlank { o.optString("url") }
            if (download.isBlank()) return@mapNotNull null
            CatalogItem(
                id = o.optString("id").ifBlank { download.hashCode().toString() },
                title = o.optString("title", "Tanpa nama"),
                author = o.optString("author", "Tidak diketahui"),
                version = o.optString("version", "-"),
                category = o.optString("category", "Lainnya"),
                description = o.optString("description"),
                sizeBytes = o.optLong("size", 0L),
                previewUrl = o.optString("preview").ifBlank { null },
                downloadUrl = download,
            )
        }
    }

    /** Unduh file ke [dest]. [onProgress] dipanggil dengan nilai 0..1 (atau -1 jika ukuran tidak diketahui). */
    fun download(url: String, dest: File, headers: Map<String, String> = emptyMap(), onProgress: (Float) -> Unit) {
        open(url, headers).use { conn ->
            val total = conn.contentLengthLong
            var done = 0L
            var lastReported = -1
            conn.inputStream.use { input ->
                dest.outputStream().use { out ->
                    val buf = ByteArray(64 * 1024)
                    while (true) {
                        val n = input.read(buf)
                        if (n < 0) break
                        out.write(buf, 0, n)
                        done += n
                        if (total > 0) {
                            val pct = (done * 100 / total).toInt()
                            if (pct != lastReported) {
                                lastReported = pct
                                onProgress(pct / 100f)
                            }
                        } else if (lastReported != -2) {
                            lastReported = -2
                            onProgress(-1f)
                        }
                    }
                }
            }
        }
    }

    fun downloadBytes(url: String): ByteArray? = runCatching {
        open(url).use { it.inputStream.readBytes() }
    }.getOrNull()

    private class Conn(val c: HttpURLConnection) : AutoCloseable {
        val inputStream get() = c.inputStream
        val contentLengthLong get() = c.contentLengthLong
        override fun close() = c.disconnect()
    }

    private fun open(url: String, headers: Map<String, String> = emptyMap()): Conn {
        var current = URL(url)
        repeat(5) {
            val c = (current.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15_000
                readTimeout = 30_000
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", "MtzInstaller/1.0 (Android)")
                headers.forEach { (k, v) -> setRequestProperty(k, v) }
            }
            when (val code = c.responseCode) {
                in 200..299 -> return Conn(c)
                301, 302, 303, 307, 308 -> {
                    val loc = c.getHeaderField("Location") ?: throw IllegalStateException("Redirect tanpa tujuan")
                    c.disconnect()
                    current = URL(current, loc)
                }
                else -> {
                    c.disconnect()
                    throw IllegalStateException("Server membalas HTTP $code")
                }
            }
        }
        throw IllegalStateException("Terlalu banyak redirect")
    }
}
