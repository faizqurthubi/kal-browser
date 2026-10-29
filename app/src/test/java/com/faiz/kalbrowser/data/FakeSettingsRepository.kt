package com.faiz.kalbrowser.data

import com.faiz.kalbrowser.ui.BrowserUiState

/**
 * Pengganti penyimpanan sungguhan untuk test. Menyimpan di memori supaya
 * perilaku simpan dan muat bisa diperiksa tanpa Android.
 */
class FakeSettingsRepository(
    private var tersimpan: BrowserSettings = BrowserSettings(),
) : SettingsRepository {

    var jumlahSimpan = 0
        private set

    override fun load(): BrowserSettings = tersimpan

    override fun save(settings: BrowserSettings) {
        tersimpan = settings
        jumlahSimpan++
    }

    /** Membaca langsung isi penyimpanan, tidak lewat pemanggil. */
    fun isiTersimpan(): BrowserSettings = tersimpan

    companion object {
        /** Bawaan yang sama dengan keadaan awal aplikasi. */
        val BAWAAN = BrowserSettings(BrowserUiState.DEFAULT_PRESET_INDEX, false)
    }
}
