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
        // Sifatnya: properti sentuh dihapus dari window sendiri dan dari
        // prototipenya, karena di WebView Android keduanya berbeda tempat.
        assertThat(script).contains("delete sasaranSentuh[t][namaSentuh[n]]")
        assertThat(script).contains("var sasaranSentuh = [window, Window.prototype")
        assertThat(script).contains("HTMLElement.prototype")
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
    fun `semua meta viewport ditulis bukan hanya yang pertama`() {
        // Situlah bug yang membuat situs TBK tetap mobile: meta milik situs
        // ditambahkan setelah script berjalan, dan browser memakai yang terakhir.
        val script = DesktopSpoof.viewportScript(1920)

        assertThat(script).contains("querySelectorAll('meta[name=viewport]')")
        assertThat(script).contains("for (var i = 0; i < daftar.length; i++)")
        assertThat(script).doesNotContain("querySelector('meta[name=viewport]')")
    }

    @Test
    fun `membuat meta baru saat halaman belum punya`() {
        val script = DesktopSpoof.viewportScript(1280)

        assertThat(script).contains("daftar.length === 0")
        assertThat(script).contains("document.createElement('meta')")
    }

    @Test
    fun `script periksa mengembalikan teks biasa`() {
        val script = DesktopSpoof.diagnosticsScript()

        assertThat(script).contains("window.innerWidth")
        assertThat(script).contains("visualViewport")
        assertThat(script).contains("meta[name=viewport]")
        assertThat(script).contains("navigator.platform")
    }

    @Test
    fun `script periksa membedakan halaman bergeser dari tata letak rusak`() {
        val script = DesktopSpoof.diagnosticsScript()

        assertThat(script).contains("lebar dokumen")
        assertThat(script).contains("geser horizontal")
        assertThat(script).contains("elemen terlebar")
    }

    @Test
    fun `properti sentuh yang tidak bisa dihapus ditutupi nilai undefined`() {
        val script = DesktopSpoof.spoofScript("Mozilla/5.0")

        // Sebagian properti bawaan tidak bisa dihapus sama sekali. Menutupinya
        // dengan undefined menyelamatkan pemeriksaan yang memakai typeof.
        assertThat(script).contains("Object.defineProperty(window, 'ontouchstart'")
        assertThat(script).contains("return undefined")
    }

    @Test
    fun `script periksa dapat menjelaskan kenapa sentuhan masih terdeteksi`() {
        val script = DesktopSpoof.diagnosticsScript()

        assertThat(script).contains("ontouchstart milik window")
        assertThat(script).contains("ontouchstart bisa dihapus")
    }

    @Test
    fun `script periksa menampilkan bukti penyamaran desktop`() {
        // Checklist penerimaan meminta dua hal ini dibuktikan dari dalam aplikasi,
        // tanpa perlu membuka situs pendeteksi.
        val script = DesktopSpoof.diagnosticsScript()

        assertThat(script).contains("mobile menurut hint")
        assertThat(script).contains("userAgentData")
        assertThat(script).contains("ontouchstart")
        assertThat(script).contains("navigator.userAgent")
    }

    @Test
    fun `pencarian elemen terlebar dibatasi supaya halaman besar tidak membeku`() {
        val script = DesktopSpoof.diagnosticsScript()

        assertThat(script).contains("Math.min(semua.length, 4000)")
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
        assertThat(script).contains("attributeFilter: ['content', 'name']")
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
