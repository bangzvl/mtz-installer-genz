package com.mtzinstaller.app.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mtzinstaller.app.ui.components.CircleIconButton
import com.mtzinstaller.app.ui.theme.MtzColors
import com.mtzinstaller.app.ui.theme.MtzType

const val MIUITHEMEZ_URL = "https://www.miuithemez.com/search"

/**
 * Browser di dalam aplikasi untuk miuithemez.com. Situs ditampilkan apa adanya (termasuk iklannya);
 * saat pengguna menekan tombol unduh dan file .mtz mulai diunduh (biasanya lewat Mediafire/Drive),
 * unduhan ditangkap lalu otomatis diimpor ke Koleksi.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MiuiThemezBrowser(
    downloads: Map<String, Float>,
    onDownload: (url: String, fileName: String, userAgent: String?, cookie: String?, referer: String?) -> Unit,
    header: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val currentOnDownload by rememberUpdatedState(onDownload)
    var host by remember { mutableStateOf("miuithemez.com") }
    var progress by remember { mutableIntStateOf(0) }
    var canGoBack by remember { mutableStateOf(false) }

    val webView = remember {
        WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

            webViewClient = object : WebViewClient() {
                override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
                    canGoBack = view.canGoBack()
                    host = url?.let { Uri.parse(it).host?.removePrefix("www.") } ?: host
                }

                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val scheme = request.url.scheme
                    if (scheme == "http" || scheme == "https") return false
                    // tg://, intent:// dll. → serahkan ke aplikasi lain
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, request.url).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                    return true
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    progress = newProgress
                }
            }
            setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                var name = URLUtil.guessFileName(url, contentDisposition, mimeType)
                if (!name.endsWith(".mtz", true) && url.contains(".mtz", true)) {
                    name = name.substringBeforeLast('.') + ".mtz"
                }
                val looksLikeTheme = name.endsWith(".mtz", true) ||
                    mimeType == "application/octet-stream" || mimeType == "application/zip"
                if (looksLikeTheme) {
                    Toast.makeText(context, "Mengunduh $name…", Toast.LENGTH_SHORT).show()
                    currentOnDownload(url, name, userAgent, CookieManager.getInstance().getCookie(url), this.url)
                } else {
                    Toast.makeText(context, "File ini bukan tema .mtz", Toast.LENGTH_SHORT).show()
                }
            }
            loadUrl(MIUITHEMEZ_URL)
        }
    }

    DisposableEffect(Unit) { onDispose { webView.destroy() } }
    BackHandler(enabled = canGoBack) { webView.goBack() }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.padding(horizontal = 20.dp)) { header() }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", { if (webView.canGoBack()) webView.goBack() }, size = 38.dp)
            Spacer(Modifier.width(8.dp))
            Row(
                Modifier.weight(1f).clip(CircleShape).background(Color.White).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(host, style = MtzType.Body, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.width(8.dp))
            CircleIconButton(Icons.Filled.Refresh, "Muat ulang", { webView.reload() }, size = 38.dp)
            Spacer(Modifier.width(8.dp))
            CircleIconButton(
                Icons.AutoMirrored.Filled.ExitToApp, "Buka di browser",
                {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webView.url ?: MIUITHEMEZ_URL)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                },
                size = 38.dp,
            )
        }
        if (progress in 1..99) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = MtzColors.Coral,
                trackColor = Color.Transparent,
            )
        }
        Box(Modifier.weight(1f).fillMaxWidth()) {
            AndroidView(
                factory = { webView },
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(start = 12.dp, end = 12.dp, bottom = 92.dp)
                    .clip(RoundedCornerShape(22.dp)),
            )
        }
    }
}
