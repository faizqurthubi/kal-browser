package com.faiz.kalbrowser.desktop

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.webkit.ScriptHandler
import androidx.webkit.UserAgentMetadata
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

/**
 * Menerapkan penyamaran desktop ke satu WebView.
 *
 * Setiap fitur androidx.webkit diperiksa dulu. Di perangkat dengan WebView lama,
 * fiturnya tidak tersedia dan aplikasi harus tetap jalan dengan UA dasar saja,
 * bukan berhenti dengan error.
 */
class DesktopMode(private val webView: WebView) {

    private var scriptHandler: ScriptHandler? = null

    /** Versi desktop yang terpasang, dipakai juga untuk Client Hints. */
    var desktopUserAgent: String = ""
        private set

    fun apply() {
        applySettings()
        applyUserAgent()
        applyClientHints()
        applyDocumentStartScript()
    }

    // JavaScript wajib: tanpa itu meta viewport dan script penyamaran tidak
    // pernah jalan, dan situs akan tetap memakai tata letak mobile.
    @SuppressLint("SetJavaScriptEnabled")
    private fun applySettings() {
        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            mediaPlaybackRequiresUserGesture = false
            javaScriptCanOpenWindowsAutomatically = true
            // Popup dibuka di WebView yang sama, karena aplikasi ini hanya punya
            // satu kotak halaman dan tidak ada tab.
            setSupportMultipleWindows(false)
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
    }

    private fun applyUserAgent() {
        desktopUserAgent = DesktopSpoof.desktopUserAgent(webView.settings.userAgentString.orEmpty())
        webView.settings.userAgentString = desktopUserAgent
    }

    private fun applyClientHints() {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.USER_AGENT_METADATA)) return

        val version = DesktopSpoof.chromeVersion(desktopUserAgent)
        val major = DesktopSpoof.majorVersion(version)

        val metadata = UserAgentMetadata.Builder()
            .setBrandVersionList(
                listOf(
                    brand("Chromium", major, version),
                    brand("Google Chrome", major, version),
                ),
            )
            .setPlatform("Windows")
            .setPlatformVersion("10.0.0")
            .setMobile(false)
            .setModel("")
            .setArchitecture("x86")
            .setBitness(64)
            .build()

        WebSettingsCompat.setUserAgentMetadata(webView.settings, metadata)
    }

    private fun brand(name: String, major: String, full: String): UserAgentMetadata.BrandVersion =
        UserAgentMetadata.BrandVersion.Builder()
            .setBrand(name)
            .setMajorVersion(major)
            .setFullVersion(full)
            .build()

    private fun applyDocumentStartScript() {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) return

        // Daftar ulang harus menghapus handler lama, kalau tidak script yang
        // sudah usang ikut jalan di setiap halaman berikutnya.
        scriptHandler?.remove()
        scriptHandler = WebViewCompat.addDocumentStartJavaScript(
            webView,
            DesktopSpoof.documentStartScript(desktopUserAgent),
            setOf("*"),
        )
    }
}
