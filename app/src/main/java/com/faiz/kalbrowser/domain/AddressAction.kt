package com.faiz.kalbrowser.domain

/**
 * Kapan tombol panah di dalam kotak alamat perlu terlihat.
 *
 * Aturannya tidak sesederhana "saat kotak difokuskan". Kalau begitu, pengguna
 * yang mengetik lalu mengetuk di luar kotak kehilangan panah, padahal teks yang
 * sudah diketik belum dikirim. Karena itu panah juga tampil saat isi kotak
 * berbeda dari alamat halaman yang sedang terbuka.
 */
object AddressAction {

    fun shouldShowGo(focused: Boolean, typed: String, currentUrl: String): Boolean {
        val teks = typed.trim()
        if (teks.isEmpty()) return false

        // Saat fokus, kotak sedang dipakai mengetik, jadi panah selalu tersedia.
        if (focused) return true

        // Saat fokus hilang, kotak hanya menampilkan alamat halaman. Kalau
        // isinya berbeda, itu berarti ada ketikan yang belum dikirim.
        return teks != currentUrl.trim()
    }
}
