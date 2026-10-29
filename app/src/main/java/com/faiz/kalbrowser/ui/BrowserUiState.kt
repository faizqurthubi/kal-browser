package com.faiz.kalbrowser.ui

import com.faiz.kalbrowser.PRESETS
import com.faiz.kalbrowser.ScreenPreset

/**
 * Seluruh keadaan yang dibaca layar browser. Satu data class, bukan beberapa
 * flow terpisah, supaya mustahil menampilkan kombinasi yang tidak pernah ada,
 * misalnya tombol maju aktif padahal riwayat masih kosong.
 */
data class BrowserUiState(
    val address: String = "",
    val isLoading: Boolean = false,
    val progress: Int = 0,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val selectedPresetIndex: Int = DEFAULT_PRESET_INDEX,
    val isPortrait: Boolean = false,
) {

    val preset: ScreenPreset
        get() = PRESETS[selectedPresetIndex.coerceIn(PRESETS.indices)]

    // Ukuran efektif diturunkan, bukan disimpan, supaya tidak mungkin ada dua
    // angka berbeda untuk hal yang sama di dalam satu state.
    val viewportWidth: Int
        get() = if (isPortrait) preset.h else preset.w

    val viewportHeight: Int
        get() = if (isPortrait) preset.w else preset.h

    val effectiveSizeLabel: String
        get() = "$viewportWidth × $viewportHeight"

    companion object {
        /** 1280 × 720: ukuran yang paling sering dipakai situs sebagai desktop. */
        const val DEFAULT_PRESET_INDEX = 1
    }
}
