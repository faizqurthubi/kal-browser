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
     * Dua hal yang mudah salah di sini:
     *
     * 1. Tanpa `initial-scale`. Kalau ikut ditulis, browser memaksa skala 1:1 dan
     *    halaman selebar 1920 CSS px hanya terlihat potongan kirinya.
     * 2. Semua meta viewport harus ditulis, bukan hanya yang pertama. Script ini
     *    berjalan sebelum parser sempat membaca <head>, jadi meta milik situs
     *    selalu muncul setelah meta buatan kita. Browser memakai meta yang
     *    terakhir diproses, dan itu meta situs, sehingga tata letak tetap mengikuti
     *    device-width.
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

                var daftar = document.querySelectorAll('meta[name=viewport]');
                if (daftar.length === 0) {
                  var baru = document.createElement('meta');
                  baru.name = 'viewport';
                  baru.content = ISI;
                  (document.head || akar).appendChild(baru);
                  return true;
                }

                for (var i = 0; i < daftar.length; i++) {
                  if (daftar[i].content !== ISI) daftar[i].content = ISI;
                }
                return true;
              }

              // Document-start kadang berjalan sebelum <html> terbentuk. Satu
              // percobaan gagal berarti halaman terlanjur disusun selebar ponsel.
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
                    attributeFilter: ['content', 'name']
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

    /**
     * Membaca angka yang benar-benar dilihat halaman saat ini, lalu menyusunnya
     * sebagai teks biasa. Dipakai untuk memeriksa apakah lebar virtual benar
     * sampai ke halaman, dan untuk membedakan halaman yang memang bisa digeser
     * ke samping dari halaman yang tata letaknya rusak.
     */
    fun diagnosticsScript(): String = """
        (function () {
          try {
            var meta = [];
            var daftar = document.querySelectorAll('meta[name=viewport]');
            for (var i = 0; i < daftar.length; i++) {
              meta.push('"' + daftar[i].content + '"');
            }
            var vv = window.visualViewport;
            var akar = document.documentElement;
            var lebarHalaman = window.innerWidth;
            var lebarDokumen = akar ? akar.scrollWidth : 0;

            // Elemen yang lebih lebar dari layar adalah tersangka utama kalau
            // seluruh halaman bisa digeser ke samping.
            var terlebar = null;
            var semua = document.querySelectorAll('body *');
            var batas = Math.min(semua.length, 4000);
            for (var j = 0; j < batas; j++) {
              var el = semua[j];
              var w = el.scrollWidth;
              if (w > lebarHalaman + 1 && (!terlebar || w > terlebar.w)) {
                terlebar = { w: w, el: el };
              }
            }

            var nama = 'tidak ada';
            if (terlebar) {
              var el = terlebar.el;
              nama = el.tagName.toLowerCase();
              if (el.id) nama += '#' + el.id;
              if (el.className && typeof el.className === 'string') {
                nama += '.' + el.className.trim().split(/\s+/).slice(0, 2).join('.');
              }
              nama += ' (' + terlebar.w + ' px)';
            }

            var baris = [
              'lebar halaman: ' + lebarHalaman + ' px',
              'tinggi halaman: ' + window.innerHeight + ' px',
              'lebar dokumen: ' + lebarDokumen + ' px',
              'beda: ' + (lebarDokumen - lebarHalaman) + ' px',
              'geser horizontal: ' + Math.round(window.scrollX || 0) + ' px',
              'skala tampilan: ' + (vv ? vv.scale.toFixed(3) : 'tidak tersedia'),
              'lebar visual: ' + (vv ? Math.round(vv.width) + ' px' : 'tidak tersedia'),
              'rasio piksel: ' + window.devicePixelRatio,
              'layar fisik: ' + screen.width + ' x ' + screen.height,
              'meta viewport: ' + (meta.length ? meta.join(' lalu ') : 'tidak ada'),
              'elemen terlebar: ' + nama,
              'platform: ' + navigator.platform,
              'titik sentuh: ' + navigator.maxTouchPoints,
              'mobile menurut hint: ' + (navigator.userAgentData
                ? navigator.userAgentData.mobile
                : 'tidak tersedia'),
              'ontouchstart: ' + ('ontouchstart' in window),
              'user agent: ' + navigator.userAgent
            ];
            return baris.join('\n');
          } catch (e) {
            return 'pembacaan gagal: ' + e;
          }
        })();
    """.trimIndent()

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
