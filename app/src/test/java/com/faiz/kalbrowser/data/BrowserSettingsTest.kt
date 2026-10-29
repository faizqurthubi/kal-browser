package com.faiz.kalbrowser.data

import com.faiz.kalbrowser.PRESETS
import com.faiz.kalbrowser.ui.BrowserUiState
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BrowserSettingsTest {

    @Test
    fun `indeks sah dibiarkan apa adanya`() {
        val settings = BrowserSettings(presetIndex = 4, isPortrait = true).sanitized()

        assertThat(settings.presetIndex).isEqualTo(4)
        assertThat(settings.isPortrait).isTrue()
    }

    @Test
    fun `indeks terlalu besar dikembalikan ke preset bawaan`() {
        val settings = BrowserSettings(presetIndex = 99).sanitized()

        assertThat(settings.presetIndex).isEqualTo(BrowserUiState.DEFAULT_PRESET_INDEX)
    }

    @Test
    fun `indeks negatif dikembalikan ke preset bawaan`() {
        val settings = BrowserSettings(presetIndex = -3).sanitized()

        assertThat(settings.presetIndex).isEqualTo(BrowserUiState.DEFAULT_PRESET_INDEX)
    }

    @Test
    fun `preset terakhir yang sah tidak dianggap rusak`() {
        val settings = BrowserSettings(presetIndex = PRESETS.lastIndex).sanitized()

        assertThat(settings.presetIndex).isEqualTo(PRESETS.lastIndex)
    }

    @Test
    fun `nilai bawaan adalah 1280 kali 720 mendatar`() {
        val settings = BrowserSettings()

        assertThat(settings.presetIndex).isEqualTo(BrowserUiState.DEFAULT_PRESET_INDEX)
        assertThat(settings.isPortrait).isFalse()
    }
}
