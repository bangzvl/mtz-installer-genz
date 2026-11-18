package com.mtzinstaller.app.data

import android.content.ClipData
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File

/**
 * Memasang tema lewat aplikasi Tema bawaan Xiaomi (com.android.thememanager).
 *
 * Xiaomi sering mengubah/menghapus "pintu masuk" untuk tema dari luar, jadi beberapa rute
 * dicoba berurutan dan setiap rute DICEK dulu lewat PackageManager sebelum dipanggil:
 *
 *  1. Tester lama  : ApplyThemeForScreenshot + action "support3.0" (Tema Global 2.15.x–3.0.5.x).
 *  2. Buka file    : ACTION_VIEW file .mtz langsung ke aplikasi Tema (jika versi itu mendukung).
 *  3. File Manager : serahkan file ke File Manager Xiaomi, yang meneruskannya ke importer Tema.
 *  4. Manual       : buka layar tema lokal / aplikasi Tema + panduan.
 */
object ThemeInstaller {

    const val THEME_MANAGER = "com.android.thememanager"
    private const val TESTER_ACTIVITY = "com.android.thememanager.ApplyThemeForScreenshot"
    private const val TESTER_ACTION = "com.android.thememanager.support3.0"
    private const val RESOURCE_TAB_ACTIVITY = "com.android.thememanager.ThemeResourceTabActivity"
    private const val EXPORT_FOLDER = "MtzInstaller"
    private val MTZ_MIME_TYPES = listOf(
        "application/vnd.miui.mtz",
        "application/x-mtz",
        "application/zip",
        "application/octet-stream",
    )

    data class Exported(val path: String, val uri: Uri)

    sealed interface LaunchResult {
        /** Aplikasi Tema langsung dibuka dengan tema ini. */
        data object Applied : LaunchResult
        /** File diserahkan ke aplikasi lain (Tema/File Manager); pengguna tinggal konfirmasi. */
        data class HandedOff(val via: String) : LaunchResult
        /** Tidak ada rute otomatis di versi Tema ini. */
        data class ManualImportNeeded(val path: String, val themeAppVersion: String?) : LaunchResult
    }

    fun isThemeManagerAvailable(context: Context): Boolean = themeManagerVersion(context) != null

    fun themeManagerVersion(context: Context): String? = try {
        context.packageManager.getPackageInfo(THEME_MANAGER, 0).versionName ?: "?"
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }

    /** True jika versi Tema yang terpasang masih punya jalur pemasangan otomatis (tester lama). */
    fun hasAutoApplyRoute(context: Context): Boolean =
        legacyTesterIntents(context, "/x.mtz").any { it.resolveActivity(context.packageManager) != null }

    /**
     * Aplikasi Tema tidak bisa membaca folder privat aplikasi ini, jadi file disalin ke
     * Download/MtzInstaller lewat MediaStore (tanpa izin tambahan di Android 10+).
     */
    fun exportToPublic(context: Context, theme: MtzTheme): Exported {
        val resolver = context.contentResolver
        val relativePath = "${Environment.DIRECTORY_DOWNLOADS}/$EXPORT_FOLDER/"
        val name = "${safeName(theme.title)}_${theme.id.take(6)}.mtz"
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI

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
        return Exported(File(File(downloads, EXPORT_FOLDER), finalName).absolutePath, uri)
    }

    fun launchApply(context: Context, exported: Exported): LaunchResult {
        val pm = context.packageManager

        // 1) Tester lama — kontrak persis yang terverifikasi di Tema Global 2.15.x / 3.0.x
        legacyTesterIntents(context, exported.path).firstOrNull { it.resolveActivity(pm) != null }?.let {
            if (start(context, it)) return LaunchResult.Applied
        }

        // 2) Versi Tema yang masih menerima "buka file .mtz" secara langsung
        MTZ_MIME_TYPES.forEach { mime ->
            val open = viewIntent(exported.uri, mime).setPackage(THEME_MANAGER)
            if (open.resolveActivity(pm) != null && start(context, open)) {
                return LaunchResult.HandedOff("Tema")
            }
        }

        // 3) File Manager Xiaomi bisa meneruskan file .mtz ke importer Tema
        val fm = viewIntent(exported.uri, "application/vnd.miui.mtz")
        val fmTargets = pm.queryIntentActivities(fm, 0).filter { it.activityInfo.packageName != THEME_MANAGER }
        if (fmTargets.isNotEmpty()) {
            val target = fmTargets.first().activityInfo
            if (fmTargets.size == 1) {
                fm.setClassName(target.packageName, target.name)
                if (start(context, fm)) return LaunchResult.HandedOff("File Manager")
            } else if (start(context, Intent.createChooser(fm, "Buka tema dengan").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))) {
                return LaunchResult.HandedOff("File Manager")
            }
        }

        // 4) Tidak ada rute otomatis
        return LaunchResult.ManualImportNeeded(exported.path, themeManagerVersion(context))
    }

    /** Buka layar "tema saya" (lokal) jika ada, kalau tidak buka aplikasi Tema biasa. */
    fun openThemeManager(context: Context): Boolean {
        val local = Intent().apply {
            component = ComponentName(THEME_MANAGER, RESOURCE_TAB_ACTIVITY)
            putExtra("extra_outer_tab_id", "mine")
            putExtra("EXTRA_TAB_ID", "theme")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (local.resolveActivity(context.packageManager) != null && start(context, local)) return true
        val launch = context.packageManager.getLaunchIntentForPackage(THEME_MANAGER) ?: return false
        return start(context, launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** Membuka halaman info aplikasi Tema (untuk "Copot pemasangan pembaruan"). */
    fun openThemeManagerAppInfo(context: Context): Boolean = start(
        context,
        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$THEME_MANAGER"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )

    /** Membuka File Manager di folder Download/MtzInstaller (best effort). */
    fun openExportFolder(context: Context): Boolean {
        val folder = Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload%2F$EXPORT_FOLDER")
        val viaFm = Intent(Intent.ACTION_VIEW).setDataAndType(folder, "vnd.android.document/directory")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (start(context, viaFm)) return true
        val miFm = context.packageManager.getLaunchIntentForPackage("com.mi.android.globalFileexplorer")
            ?: context.packageManager.getLaunchIntentForPackage("com.android.fileexplorer")
            ?: return false
        return start(context, miFm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun legacyTesterIntents(context: Context, path: String): List<Intent> = listOf(
        // Kontrak lengkap (action support3.0 + 4 extras)
        Intent(TESTER_ACTION).apply {
            component = ComponentName(THEME_MANAGER, TESTER_ACTIVITY)
            putExtra("theme_file_path", path)
            putExtra("api_called_from", context.packageName)
            putExtra("theme_apply_flags", -1L)
            putExtra("theme_remove_flags", -1L)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
        // Varian lama tanpa action (MIUI 12–14)
        Intent(Intent.ACTION_MAIN).apply {
            component = ComponentName(THEME_MANAGER, TESTER_ACTIVITY)
            putExtra("theme_file_path", path)
            putExtra("api_called_from", "ThemeEditor")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        },
    )

    private fun viewIntent(uri: Uri, mime: String) = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mime)
        clipData = ClipData.newRawUri("MTZ", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun start(context: Context, intent: Intent): Boolean = runCatching {
        context.startActivity(intent)
    }.isSuccess

    private fun safeName(s: String): String =
        s.replace(Regex("[^A-Za-z0-9._-]+"), "_").trim('_').take(40).ifEmpty { "theme" }
}
