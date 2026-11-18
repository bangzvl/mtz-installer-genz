package com.mtzinstaller.app.data

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.provider.MediaStore
import java.io.File

object ThemeInstaller {

    const val THEME_MANAGER = "com.android.thememanager"
    private const val APPLY_ACTIVITY = "com.android.thememanager.ApplyThemeForScreenshot"
    private const val EXPORT_FOLDER = "MtzInstaller"

    enum class LaunchResult { Launched, ManualImportNeeded }

    fun isThemeManagerAvailable(context: Context): Boolean = try {
        context.packageManager.getPackageInfo(THEME_MANAGER, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    /**
     * Aplikasi Tema MIUI tidak bisa membaca folder privat aplikasi ini, jadi file
     * disalin ke Download/MtzInstaller lewat MediaStore (tanpa izin tambahan di Android 10+).
     * @return path absolut file yang bisa dibaca oleh aplikasi Tema.
     */
    fun exportToPublic(context: Context, theme: MtzTheme): String {
        val resolver = context.contentResolver
        val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/$EXPORT_FOLDER/"
        val name = "${safeName(theme.title)}_${theme.id.take(6)}.mtz"
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI

        // Hapus salinan lama milik aplikasi ini agar tidak menumpuk "(1)", "(2)", dst.
        runCatching {
            resolver.delete(
                collection,
                "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?",
                arrayOf(name, relativePath),
            )
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(collection, values)
            ?: throw IllegalStateException("Tidak bisa menulis ke folder Download")

        resolver.openOutputStream(uri)?.use { out ->
            File(theme.filePath).inputStream().use { it.copyTo(out) }
        } ?: throw IllegalStateException("Tidak bisa menulis file tema")

        values.clear()
        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        val finalName = resolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)
            ?.use { c -> if (c.moveToFirst()) c.getString(0) else null } ?: name

        @Suppress("DEPRECATION")
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(File(downloads, EXPORT_FOLDER), finalName).absolutePath
    }

    /**
     * Memanggil activity tersembunyi milik aplikasi Tema yang menerapkan file .mtz
     * dari path tertentu (cara yang sama dipakai oleh "MIUI Theme Editor").
     */
    fun launchApply(context: Context, path: String): LaunchResult {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            component = ComponentName(THEME_MANAGER, APPLY_ACTIVITY)
            putExtra("theme_file_path", path)
            putExtra("api_called_from", "ThemeEditor")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            LaunchResult.Launched
        } catch (e: ActivityNotFoundException) {
            openThemeManager(context)
            LaunchResult.ManualImportNeeded
        } catch (e: SecurityException) {
            openThemeManager(context)
            LaunchResult.ManualImportNeeded
        }
    }

    fun openThemeManager(context: Context): Boolean {
        val launch = context.packageManager.getLaunchIntentForPackage(THEME_MANAGER) ?: return false
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(launch) }.isSuccess
    }

    private fun safeName(s: String): String =
        s.replace(Regex("[^A-Za-z0-9._-]+"), "_").trim('_').take(40).ifEmpty { "theme" }
}
