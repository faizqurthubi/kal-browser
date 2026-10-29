package com.faiz.kalbrowser.data

import android.content.Context
import android.content.SharedPreferences
import com.faiz.kalbrowser.PRESETS
import com.faiz.kalbrowser.ui.BrowserUiState

/**
 * Pengaturan yang perlu bertahan setelah aplikasi ditutup.
 *
 * Hanya pilihan yang disengaja pengguna yang disimpan. Alamat terakhir tidak
 * ikut, karena membuka aplikasi lalu langsung memuat halaman lama terasa
 * seperti aplikasi yang tidak pernah benar-benar ditutup.
 */
data class BrowserSettings(
    val presetIndex: Int = BrowserUiState.DEFAULT_PRESET_INDEX,
    val isPortrait: Boolean = false,
) {

    /**
     * Indeks disimpan sebagai angka, dan daftar preset bisa berubah di versi
     * berikutnya. Indeks yang tidak lagi ada dikembalikan ke preset bawaan,
     * bukan dipotong ke preset terdekat, karena memotong diam-diam mengubah
     * pilihan pengguna menjadi ukuran lain yang tidak pernah ia pilih.
     */
    fun sanitized(): BrowserSettings =
        if (presetIndex in PRESETS.indices) this
        else copy(presetIndex = BrowserUiState.DEFAULT_PRESET_INDEX)
}

interface SettingsRepository {
    fun load(): BrowserSettings
    fun save(settings: BrowserSettings)
}

class SharedPreferencesSettings(
    private val prefs: SharedPreferences,
) : SettingsRepository {

    override fun load(): BrowserSettings = BrowserSettings(
        presetIndex = prefs.getInt(KEY_PRESET, BrowserUiState.DEFAULT_PRESET_INDEX),
        isPortrait = prefs.getBoolean(KEY_PORTRAIT, false),
    )

    override fun save(settings: BrowserSettings) {
        prefs.edit()
            .putInt(KEY_PRESET, settings.presetIndex)
            .putBoolean(KEY_PORTRAIT, settings.isPortrait)
            .apply()
    }

    companion object {
        private const val NAMA = "kal_browser_pengaturan"
        private const val KEY_PRESET = "preset_index"
        private const val KEY_PORTRAIT = "portrait"

        fun from(context: Context): SettingsRepository =
            SharedPreferencesSettings(context.getSharedPreferences(NAMA, Context.MODE_PRIVATE))
    }
}
