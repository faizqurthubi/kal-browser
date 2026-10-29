package com.faiz.kalbrowser.domain

import java.net.URLEncoder

/**
 * Mengubah apa pun yang diketik di kolom alamat menjadi URL yang bisa dimuat
 * WebView.
 */
object UrlNormalizer {

    // data: dan about: ikut diterima karena berguna untuk menempel halaman uji
    // kecil langsung dari kolom alamat, tanpa perlu situs.
    private val KNOWN_SCHEMES = setOf("http", "https", "file", "about", "data")

    fun toLoadableUrl(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        // Skema diperiksa lebih dulu dari aturan titik, karena alamat seperti
        // http://localhost tidak punya titik dan bukan berarti kata kunci pencarian.
        // data: dan about: tidak memakai "://", jadi yang diperiksa bagian
        // sebelum titik dua pertama.
        val scheme = trimmed.substringBefore(':').lowercase()
        if (trimmed.contains(':') && scheme in KNOWN_SCHEMES) return trimmed

        // Aturan panduan 6.6: spasi atau tanpa titik berarti pencarian.
        val looksLikeHost = !trimmed.contains(' ') && trimmed.contains('.')
        return if (looksLikeHost) "https://$trimmed" else searchUrl(trimmed)
    }

    private fun searchUrl(query: String): String =
        "https://www.google.com/search?q=" + URLEncoder.encode(query, Charsets.UTF_8.name())
}
