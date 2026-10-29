package com.faiz.kalbrowser.ui

import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Menghitung kotak WebView di dalam container hitam.
 *
 * Kotak mengikuti rasio layar virtual dan diperkecil sampai muat, lalu sisanya
 * dibiarkan hitam. Halaman tidak pernah diregangkan: satu-satunya cara memenuhi
 * container adalah memilih rasio yang memang sama.
 */
object Letterbox {

    data class Box(val width: Int, val height: Int)

    /**
     * Null berarti belum bisa dihitung, misalnya karena container belum diukur.
     * Pemanggil cukup menunggu dan mencoba lagi, bukan memakai angka tebakan.
     */
    fun boxFor(
        containerWidth: Int,
        containerHeight: Int,
        virtualWidth: Int,
        virtualHeight: Int,
    ): Box? {
        if (containerWidth <= 0 || containerHeight <= 0) return null
        if (virtualWidth <= 0 || virtualHeight <= 0) return null

        val scale = min(
            containerWidth.toDouble() / virtualWidth,
            containerHeight.toDouble() / virtualHeight,
        )

        return Box(
            width = (virtualWidth * scale).roundToInt().coerceAtLeast(1),
            height = (virtualHeight * scale).roundToInt().coerceAtLeast(1),
        )
    }
}
