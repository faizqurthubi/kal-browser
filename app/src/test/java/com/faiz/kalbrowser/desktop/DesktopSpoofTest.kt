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
    fun `script penyamaran menimpa platform maxTouchPoints dan mobile`() {
        val script = DesktopSpoof.spoofScript(DesktopSpoof.desktopUserAgent(realUserAgent))

        assertThat(script).contains("'platform', 'Win32'")
        assertThat(script).contains("'maxTouchPoints', 0")
        assertThat(script).contains("'mobile', false")
        assertThat(script).contains("delete Window.prototype.ontouchstart")
    }

    @Test
    fun `script penyamaran memuat UA desktop sebagai string JavaScript`() {
        val desktop = DesktopSpoof.desktopUserAgent(realUserAgent)
        val script = DesktopSpoof.spoofScript(desktop)

        assertThat(script).contains("'$desktop'")
    }

    @Test
    fun `script viewport memakai lebar yang diminta`() {
        val script = DesktopSpoof.viewportScript(1280)

        assertThat(script).contains("var W = 1280;")
        assertThat(script).contains("'width=' + W")
    }

    @Test
    fun `meta viewport tidak memaksa skala satu banding satu`() {
        // initial-scale=1 membuat halaman selebar 1920 hanya terlihat potongan
        // kirinya, karena skala tidak pernah dikecilkan agar muat di kotak.
        assertThat(DesktopSpoof.viewportScript(1920)).doesNotContain("initial-scale")
    }

    @Test
    fun `script viewport mencoba ulang kalau html belum terbentuk`() {
        val script = DesktopSpoof.viewportScript(1024)

        assertThat(script).contains("if (!pasang())")
        assertThat(script).contains("setInterval")
        assertThat(script).contains("clearInterval")
    }

    @Test
    fun `pengamat dipasang setelah html ada`() {
        val script = DesktopSpoof.viewportScript(1024)

        assertThat(script).contains("function amati()")
        assertThat(script).contains("DOMContentLoaded")
    }

    @Test
    fun `lebar nol dijepit jadi satu supaya width tidak kosong`() {
        assertThat(DesktopSpoof.viewportScript(0)).contains("var W = 1;")
        assertThat(DesktopSpoof.viewportScript(-5)).contains("var W = 1;")
    }

    @Test
    fun `script viewport mengamati perubahan dom`() {
        val script = DesktopSpoof.viewportScript(1024)

        assertThat(script).contains("new MutationObserver(jadwalkan)")
        assertThat(script).contains("attributeFilter: ['content']")
    }

    @Test
    fun `script gabungan berisi penyamaran dan viewport`() {
        val script = DesktopSpoof.startScript(DesktopSpoof.desktopUserAgent(realUserAgent), 1440)

        assertThat(script).contains("'platform', 'Win32'")
        assertThat(script).contains("var W = 1440;")
    }

    @Test
    fun `UA dengan tanda kutip tidak disusupkan ke script`() {
        val script = DesktopSpoof.spoofScript("Mozilla/5.0'); alert(1); //")

        assertThat(script).doesNotContain("alert(1)")
        assertThat(script).contains("'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'")
    }

    @Test
    fun `script penyamaran dibungkus fungsi yang langsung jalan`() {
        val script = DesktopSpoof.spoofScript("Mozilla/5.0 (Windows NT 10.0)")

        assertThat(script).startsWith("(function () {")
        assertThat(script).endsWith("})();")
    }
}
