package com.faiz.kalbrowser.desktop

/**
 * Bahan-bahan palsu untuk membuat situs melihat desktop Windows, bukan Android.
 *
 * Kelas ini sengaja tidak menyentuh WebView sama sekali supaya isinya bisa diuji
 * di JVM biasa. Yang menerapkan ke WebView adalah [DesktopMode].
 */
object DesktopSpoof {

    /** Dipakai hanya kalau UA asli tidak menyebut versi Chrome yang bisa dibaca. */
    const val FALLBACK_CHROME_VERSION = "130.0.0.0"

    private val CHROME_VERSION = Regex("""Chrome/(\d+(?:\.\d+)*)""")
    private val UNSAFE_FOR_JS = Regex("""['\\\r\n]""")

    /**
     * Merakit UA desktop memakai versi Chrome milik WebView yang sebenarnya.
     *
     * Kalau versinya dikarang, situs bisa melihat UA Chrome 130 padahal Client
     * Hints-nya melaporkan versi lain, dan ketidakcocokan itu sendiri sudah jadi
     * tanda bahwa ada yang dipalsukan.
     */
    fun desktopUserAgent(realUserAgent: String): String {
        val version = chromeVersion(realUserAgent)
        return "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/$version Safari/537.36"
    }

    /** Versi Chrome penuh, misalnya 141.0.7390.122. */
    fun chromeVersion(realUserAgent: String): String =
        CHROME_VERSION.find(realUserAgent)?.groupValues?.get(1) ?: FALLBACK_CHROME_VERSION

    fun majorVersion(chromeVersion: String): String = chromeVersion.substringBefore('.')

    /**
     * Script yang jalan sebelum halaman punya kesempatan membaca apa pun.
     *
     * Setiap penulisan dibungkus try, karena halaman bisa membekukan prototipe
     * lebih dulu atau mematikan defineProperty, dan satu error di sini akan
     * mematikan sisa script.
     */
    fun spoofScript(desktopUserAgent: String): String {
        val userAgent = jsString(desktopUserAgent)
        return """
            (function () {
              function override(target, prop, value) {
                try {
                  Object.defineProperty(target, prop, {
                    get: function () { return value; },
                    configurable: true
                  });
                } catch (e) {}
              }

              override(Navigator.prototype, 'platform', 'Win32');
              override(Navigator.prototype, 'maxTouchPoints', 0);
              override(Navigator.prototype, 'userAgent', $userAgent);

              try {
                if (navigator.userAgentData) {
                  override(Object.getPrototypeOf(navigator.userAgentData), 'mobile', false);
                }
              } catch (e) {}

              // Touch event hanya ada di perangkat sentuh. Menghapus propertinya
              // membuat 'ontouchstart' in window bernilai false, sama seperti desktop.
              try { delete Window.prototype.ontouchstart; } catch (e) {}
              try { delete Document.prototype.ontouchstart; } catch (e) {}
            })();
        """.trimIndent()
    }

    /**
     * Memaksa meta viewport memakai lebar layar virtual, sehingga media query CSS
     * dan satuan vh mengikuti ukuran palsu itu, bukan lebar layar ponsel.
     *
     * Isinya sengaja hanya `width=W`, tanpa `initial-scale`. Kalau `initial-scale=1`
     * ikut ditulis, browser memaksa skala 1:1 sehingga halaman selebar 1920 CSS px
     * hanya terlihat potongan kirinya. Tanpa `initial-scale`, browser menghitung
     * sendiri skala yang membuat seluruh lebar itu muat di kotak WebView.
     *
     * Pengamat DOM ikut dipasang karena banyak situs menulis ulang meta viewport
     * setelah halaman dimuat. Tanpa pengamat, nilai yang sudah benar akan ditimpa
     * kembali beberapa ratus milidetik kemudian.
     */
    fun viewportScript(width: Int): String {
        val safeWidth = width.coerceAtLeast(1)
        return """
            (function () {
              var W = $safeWidth;
              var ISI = 'width=' + W;

              function pasang() {
                var akar = document.documentElement;
                if (!akar) return false;
                var m = document.querySelector('meta[name=viewport]');
                if (!m) {
                  m = document.createElement('meta');
                  m.name = 'viewport';
                  (document.head || akar).appendChild(m);
                }
                if (m.content !== ISI) m.content = ISI;
                return true;
              }

              // Document-start kadang berjalan sebelum <html> terbentuk. Satu
              // percobaan gagal berarti halaman terlanjur disusun selebar ponsel,
              // jadi dicoba berulang selama frame-frame pertama.
              if (!pasang()) {
                var coba = 0;
                var timer = setInterval(function () {
                  if (pasang() || ++coba > 120) clearInterval(timer);
                }, 16);
              }

              // Halaman yang terus berubah memicu pengamat ini terus-menerus.
              // Pekerjaannya dijadwalkan sekali per putaran, bukan setiap mutasi.
              var terjadwal = false;
              function jadwalkan() {
                if (terjadwal) return;
                terjadwal = true;
                setTimeout(function () { terjadwal = false; pasang(); }, 0);
              }
              function amati() {
                try {
                  if (!document.documentElement) return false;
                  new MutationObserver(jadwalkan).observe(document.documentElement, {
                    childList: true,
                    subtree: true,
                    attributes: true,
                    attributeFilter: ['content']
                  });
                  return true;
                } catch (e) {
                  return false;
                }
              }
              if (!amati()) {
                document.addEventListener('DOMContentLoaded', amati);
                document.addEventListener('readystatechange', amati);
              }
            })();
        """.trimIndent()
    }

    /** Script lengkap untuk document-start: penyamaran perangkat lalu viewport. */
    fun startScript(desktopUserAgent: String, viewportWidth: Int): String =
        spoofScript(desktopUserAgent) + "\n" + viewportScript(viewportWidth)

    /**
     * UA asli tidak pernah memuat tanda kutip atau baris baru, jadi nilai seperti
     * itu dibuang saja. Membiarkan string apa pun masuk ke script berarti
     * bergantung pada escaping yang benar; menolaknya lebih mudah diaudit.
     */
    private fun jsString(value: String): String {
        val safe = if (value.isEmpty() || UNSAFE_FOR_JS.containsMatchIn(value)) {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
        } else {
            value
        }
        return "'" + safe + "'"
    }
}
