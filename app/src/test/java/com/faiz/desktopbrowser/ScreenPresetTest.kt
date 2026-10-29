package com.faiz.desktopbrowser

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Label dipakai di dua tempat (dropdown dan label ukuran), jadi salah ketik di
 * satu preset akan terbaca sebagai dua ukuran berbeda oleh pengguna.
 */
class ScreenPresetTest {

    @Test
    fun `label setiap preset cocok dengan ukurannya`() {
        PRESETS.forEach { preset ->
            assertThat(preset.label).isEqualTo("${preset.w} × ${preset.h}")
        }
    }

    @Test
    fun `preset wajib dari panduan tersedia lengkap`() {
        val sizes = PRESETS.map { it.w to it.h }

        assertThat(sizes).containsExactly(
            1024 to 768,
            1280 to 720,
            1366 to 768,
            1440 to 900,
            1920 to 1080,
        ).inOrder()
    }

    @Test
    fun `tidak ada preset dengan ukuran nol atau negatif`() {
        PRESETS.forEach { preset ->
            assertThat(preset.w).isGreaterThan(0)
            assertThat(preset.h).isGreaterThan(0)
        }
    }
}
