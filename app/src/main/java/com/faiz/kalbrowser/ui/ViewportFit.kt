package com.faiz.kalbrowser.ui

/**
 * Memilih cara menampilkan halaman: dibiarkan diskalakan sendiri oleh WebView,
 * atau WebView diletakkan sebesar layar virtual lalu dikecilkan dengan
 * transformasi View.
 *
 * Alasannya, Chromium menolak mengecilkan halaman di bawah 25%. Pada layar
 * 393 dp, lebar virtual 1920 px butuh skala 0.205, dan skala itu tidak pernah
 * tercapai. Akibatnya halaman tersangkut di 25%, hanya 1571 px yang terlihat,
 * dan sisa 349 px bisa digeser ke samping.
 *
 * Transformasi View tidak punya batas seperti itu, jadi dipakai saat skala yang
 * dibutuhkan terlalu kecil. Skala yang masih aman tetap memakai jalur biasa
 * karena hasil teksnya lebih tajam.
 */
object ViewportFit {

    /** Batas bawah skala halaman di Chromium. Di bawah ini tidak mungkin tercapai. */
    const val MIN_PAGE_SCALE = 0.25

    /** Jarak aman dari batas, supaya pembulatan perangkat tidak menjatuhkan ke batas. */
    const val SAFE_MARGIN = 0.03

    enum class Mode {
        /** WebView menghitung skalanya sendiri. Teks paling tajam. */
        Native,

        /** WebView diletakkan seukuran virtual lalu diskalakan dengan matriks View. */
        Transform,
    }

    /** Skala yang dibutuhkan agar lebar virtual muat di kotak, dalam satuan dp per CSS px. */
    fun neededScale(boxWidthPx: Int, density: Float, virtualWidth: Int): Double {
        if (virtualWidth <= 0 || density <= 0f) return 0.0
        val boxWidthDp = boxWidthPx / density
        return boxWidthDp / virtualWidth.toDouble()
    }

    fun modeFor(boxWidthPx: Int, density: Float, virtualWidth: Int): Mode {
        val needed = neededScale(boxWidthPx, density, virtualWidth)
        return if (needed >= MIN_PAGE_SCALE + SAFE_MARGIN) Mode.Native else Mode.Transform
    }

    /** Faktor pengecil View pada jalur transformasi: kotak dibagi lebar virtual. */
    fun transformScale(boxWidthPx: Int, virtualWidth: Int): Float {
        if (virtualWidth <= 0) return 1f
        return boxWidthPx.toFloat() / virtualWidth
    }
}
