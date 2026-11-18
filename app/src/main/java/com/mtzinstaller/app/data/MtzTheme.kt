package com.mtzinstaller.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class MtzTheme(
    val id: String,
    val title: String,
    val author: String,
    val version: String,
    val description: String,
    val sizeBytes: Long,
    val filePath: String,
    val previewPath: String?,
    val addedAt: Long,
    val lastInstalledAt: Long? = null,
    val installCount: Int = 0,
    val favorite: Boolean = false,
    /** id item katalog jika diunduh dari tab Jelajah */
    val sourceId: String? = null,
)

/** Penyimpanan sederhana berbasis SharedPreferences + JSON. */
class ThemeStore(context: Context) {
    private val prefs = context.getSharedPreferences("mtz_store", Context.MODE_PRIVATE)

    var onboardingDone: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING, value).apply()

    var catalogUrl: String
        get() = prefs.getString(KEY_CATALOG, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_CATALOG, value).apply()

    fun load(): List<MtzTheme> {
        val raw = prefs.getString(KEY_THEMES, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { fromJson(arr.getJSONObject(it)) }
        }.getOrDefault(emptyList())
    }

    fun save(themes: List<MtzTheme>) {
        val arr = JSONArray()
        themes.forEach { arr.put(toJson(it)) }
        prefs.edit().putString(KEY_THEMES, arr.toString()).apply()
    }

    private fun toJson(t: MtzTheme) = JSONObject().apply {
        put("id", t.id)
        put("title", t.title)
        put("author", t.author)
        put("version", t.version)
        put("description", t.description)
        put("sizeBytes", t.sizeBytes)
        put("filePath", t.filePath)
        put("previewPath", t.previewPath ?: JSONObject.NULL)
        put("addedAt", t.addedAt)
        put("lastInstalledAt", t.lastInstalledAt ?: JSONObject.NULL)
        put("installCount", t.installCount)
        put("favorite", t.favorite)
        put("sourceId", t.sourceId ?: JSONObject.NULL)
    }

    private fun fromJson(o: JSONObject) = MtzTheme(
        id = o.getString("id"),
        title = o.optString("title"),
        author = o.optString("author"),
        version = o.optString("version"),
        description = o.optString("description"),
        sizeBytes = o.optLong("sizeBytes"),
        filePath = o.getString("filePath"),
        previewPath = if (o.isNull("previewPath")) null else o.optString("previewPath"),
        addedAt = o.optLong("addedAt"),
        lastInstalledAt = if (o.isNull("lastInstalledAt")) null else o.optLong("lastInstalledAt"),
        installCount = o.optInt("installCount"),
        favorite = o.optBoolean("favorite"),
        sourceId = if (o.isNull("sourceId")) null else o.optString("sourceId"),
    )

    private companion object {
        const val KEY_THEMES = "themes"
        const val KEY_ONBOARDING = "onboarding_done"
        const val KEY_CATALOG = "catalog_url"
    }
}
