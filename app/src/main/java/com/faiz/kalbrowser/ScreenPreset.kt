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
