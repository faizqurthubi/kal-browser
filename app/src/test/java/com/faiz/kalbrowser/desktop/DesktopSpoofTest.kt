package com.faiz.kalbrowser.desktop

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DesktopSpoofTest {

    // UA asli WebView di Android, dipakai sebagai bahan.
    private val realUserAgent =
        "Mozilla/5.0 (Linux; Android 14; Pixel 7 Build/AP1A.240505.004; wv) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/141.0.7390.122 " +
            "Mobile Safari/537.36"

    @Test
    fun `versi chrome diambil dari UA asli`() {
        assertThat(DesktopSpoof.chromeVersion(realUserAgent)).isEqualTo("141.0.7390.122")
    }

    @Test
    fun `UA desktop memakai versi chrome yang sama dengan WebView`() {
        val desktop = DesktopSpoof.desktopUserAgent(realUserAgent)

        assertThat(desktop).contains("Chrome/141.0.7390.122")
        assertThat(desktop).contains("Windows NT 10.0; Win64; x64")
        assertThat(desktop).doesNotContain("Android")
        assertThat(desktop).doesNotContain("Mobile")
        assertThat(desktop).doesNotContain("wv")
    }

    @Test
    fun `UA tanpa versi chrome memakai versi cadangan`() {
        val desktop = DesktopSpoof.desktopUserAgent("Mozilla/5.0 (Linux; Android 10)")

        assertThat(desktop).contains("Chrome/${DesktopSpoof.FALLBACK_CHROME_VERSION}")
    }

    @Test
    fun `versi mayor dipisah dari versi penuh`() {
        assertThat(DesktopSpoof.majorVersion("141.0.7390.122")).isEqualTo("141")
        assertThat(DesktopSpoof.majorVersion("130")).isEqualTo("130")
    }

    @Test
    fun `script menimpa platform maxTouchPoints dan mobile`() {
        val script = DesktopSpoof.documentStartScript(DesktopSpoof.desktopUserAgent(realUserAgent))

        assertThat(script).contains("'platform', 'Win32'")
        assertThat(script).contains("'maxTouchPoints', 0")
        assertThat(script).contains("'mobile', false")
        assertThat(script).contains("delete Window.prototype.ontouchstart")
    }

    @Test
    fun `script memuat UA desktop sebagai string JavaScript`() {
        val desktop = DesktopSpoof.desktopUserAgent(realUserAgent)
        val script = DesktopSpoof.documentStartScript(desktop)

        assertThat(script).contains("'$desktop'")
    }

    @Test
    fun `UA dengan tanda kutip tidak disusupkan ke script`() {
        val script = DesktopSpoof.documentStartScript("Mozilla/5.0'); alert(1); //")

        assertThat(script).doesNotContain("alert(1)")
        assertThat(script).contains("'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'")
    }

    @Test
    fun `script dibungkus fungsi yang langsung jalan`() {
        val script = DesktopSpoof.documentStartScript("Mozilla/5.0 (Windows NT 10.0)")

        assertThat(script).startsWith("(function () {")
        assertThat(script).endsWith("})();")
    }
}
