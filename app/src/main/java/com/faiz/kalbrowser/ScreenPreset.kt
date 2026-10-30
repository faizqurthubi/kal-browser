package com.faiz.kalbrowser

/**
 * Satu ukuran layar virtual. [w] dan [h] adalah piksel CSS yang dipakai halaman
 * web sebagai acuan media query, bukan piksel fisik layar Android.
 */
data class ScreenPreset(val label: String, val w: Int, val h: Int)

/**
 * Urutan dari yang paling kecil ke paling besar, supaya daftar di dropdown
 * terbaca sebagai tangga dan bukan kumpulan acak.
 */
val PRESETS = listOf(
    ScreenPreset("1024 × 768", 1024, 768),
    ScreenPreset("1280 × 720", 1280, 720),
    ScreenPreset("1366 × 768", 1366, 768),
    ScreenPreset("1440 × 900", 1440, 900),
    ScreenPreset("1920 × 1080", 1920, 1080),
)

/**
 * Satu pilihan ukuran sebagaimana dilihat pengguna di sidebar.
 *
 * Lima preset mendatar dan lima preset tegak adalah hal yang sama dipandang
 * dari dua arah, jadi daftar tegak diturunkan dari daftar mendatar, bukan
 * ditulis ulang. Dengan begitu mustahil ada ukuran yang hanya muncul di satu
 * arah saja.
 */
data class SizeOption(
    val presetIndex: Int,
    val portrait: Boolean,
    val width: Int,
    val height: Int,
) {
    /** Rasio untuk menggambar kartu, dihitung dari ukuran sebenarnya. */
    val ratio: Float
        get() = height.toFloat() / width.toFloat()

    val label: String
        get() = "$width × $height"
}

/** Lima mendatar lebih dulu, lalu lima tegak, supaya urutan di layar sama dengan urutan di sini. */
val SIZE_OPTIONS: List<SizeOption> = buildList {
    PRESETS.forEachIndexed { index, preset ->
        add(SizeOption(index, portrait = false, width = preset.w, height = preset.h))
    }
    PRESETS.forEachIndexed { index, preset ->
        add(SizeOption(index, portrait = true, width = preset.h, height = preset.w))
    }
}

/**
 * Sisi terpanjang di antara semua kartu, dalam piksel virtual.
 *
 * Semua kartu digambar dengan satu skala terhadap angka ini, bukan dengan lebar
 * yang sama. Akibatnya kartu 1920 tampil dua kali lebih besar daripada kartu
 * 1024, dan perbedaan ukuran terbaca sebelum labelnya dibaca.
 */
val SIZE_SCALE_BASE: Int = SIZE_OPTIONS.maxOf { maxOf(it.width, it.height) }

/**
 * Ukuran gambar kartu bila sisi terpanjangnya [sisi] dp. Perbandingan lebar dan
 * tinggi tetap sama dengan ukuran sebenarnya, karena keduanya dibagi angka yang
 * sama.
 */
fun SizeOption.cardSize(sisi: Float): Pair<Float, Float> =
    (width.toFloat() / SIZE_SCALE_BASE * sisi) to (height.toFloat() / SIZE_SCALE_BASE * sisi)

/** Kartu yang sedang aktif, dipakai untuk menandainya di sidebar. */
fun optionFor(presetIndex: Int, portrait: Boolean): SizeOption? =
    SIZE_OPTIONS.firstOrNull { it.presetIndex == presetIndex && it.portrait == portrait }
