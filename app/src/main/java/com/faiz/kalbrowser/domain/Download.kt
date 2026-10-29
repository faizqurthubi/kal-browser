package com.faiz.kalbrowser.domain

import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLDecoder

/**
 * Aturan penamaan berkas unduhan.
 *
 * Dipisahkan dari Activity karena nama berkas yang tidak sah membuat unduhan
 * gagal tanpa pesan apa pun: DownloadManager hanya menolak, dan pengguna
 * melihat tidak terjadi apa-apa. Nama dari situs juga tidak selalu masuk akal,
 * jadi semua jalur masuk dilewatkan sini lebih dulu.
 */
object Download {

    private const val PANJANG_MAKS = 120
    private const val NAMA_CADANGAN = "unduhan"

    /** Karakter yang dilarang sistem berkas, ditambah pemisah jalur. */
    private const val TERLARANG = "/\\:*?\"<>|"

    /**
     * Hanya http dan https yang bisa diserahkan ke DownloadManager. Alamat
     * `data:` dan `blob:` berisi halaman itu sendiri, bukan berkas yang bisa
     * diambil aplikasi lain.
     */
    fun isDownloadable(url: String): Boolean {
        val bersih = url.trim()
        val skema = bersih.substringBefore(':', "").lowercase()
        if (skema != "http" && skema != "https") return false
        // Skema saja tanpa alamat tetap tidak bisa diunduh.
        return bersih.substringAfter(':').trimStart('/').isNotBlank()
    }

    /**
     * Nama berkas diambil dari Content-Disposition lebih dulu, karena itulah
     * nama yang dimaksud situs. Kalau tidak ada, potongan terakhir alamat
     * dipakai. Nama cadangan hanya dipakai bila keduanya kosong.
     */
    fun fileName(contentDisposition: String?, url: String, fallback: String): String {
        val kandidat = fromContentDisposition(contentDisposition)
            ?: fromUrl(url)
            ?: fallback
        val aman = sanitize(kandidat)
        if (aman.isNotEmpty()) return aman

        val cadangan = sanitize(fallback)
        return if (cadangan.isNotEmpty()) cadangan else NAMA_CADANGAN
    }

    private fun fromContentDisposition(header: String?): String? {
        if (header.isNullOrBlank()) return null
        val bagian = header.split(';')

        // Bentuk filename* memuat nama asli berenkode, jadi didahulukan.
        for (b in bagian) {
            val teks = b.trim()
            if (!teks.startsWith("filename*=", ignoreCase = true)) continue
            val nilai = teks.substringAfter('=').trim().trim('"')
            // Bentuk RFC 5987: charset'bahasa'nilai
            val isi = nilai.split('\'').lastOrNull().orEmpty()
            val terbaca = decode(isi)
            if (terbaca.isNotBlank()) return terbaca
        }

        for (b in bagian) {
            val teks = b.trim()
            if (!teks.startsWith("filename=", ignoreCase = true)) continue
            val nilai = teks.substringAfter('=').trim().trim('"')
            val terbaca = decode(nilai)
            if (terbaca.isNotBlank()) return terbaca
        }
        return null
    }

    private fun fromUrl(url: String): String? {
        val bersih = url.trim()
        // Jalur diambil terpisah dari nama host, karena potongan terakhir
        // "https://situs.test/" adalah nama host, bukan nama berkas.
        val jalur = try {
            URL(bersih).path.orEmpty()
        } catch (e: MalformedURLException) {
            bersih.substringBefore('#').substringBefore('?').substringAfter("://", bersih)
        }
        val potongan = jalur.trimEnd('/').substringAfterLast('/')
        return decode(potongan).takeIf { it.isNotBlank() }
    }

    private fun decode(teks: String): String {
        if (teks.isBlank()) return teks
        return try {
            // Tanda plus di nama berkas adalah huruf biasa, bukan spasi, jadi
            // dilindungi dulu sebelum dekode bentuk form.
            URLDecoder.decode(teks.replace("+", "%2B"), "UTF-8")
        } catch (e: IllegalArgumentException) {
            teks
        } catch (e: UnsupportedEncodingException) {
            teks
        }
    }

    private fun sanitize(nama: String): String {
        val bersih = nama
            .map { huruf -> if (huruf in TERLARANG || huruf.code < 32) '_' else huruf }
            .joinToString("")
            .trim()
            .trim('.')
        // Titik berderet tidak berguna di nama berkas dan bisa terbaca sebagai
        // upaya keluar folder, jadi dirapatkan jadi satu.
        val rapat = bersih.replace(Regex("\\.{2,}"), ".")
        return if (rapat.length > PANJANG_MAKS) rapat.take(PANJANG_MAKS) else rapat
    }
}
