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
    fun documentStartScript(desktopUserAgent: String): String {
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
