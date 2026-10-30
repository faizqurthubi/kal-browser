package com.faiz.kalbrowser.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.faiz.kalbrowser.PRESETS
import com.faiz.kalbrowser.data.BrowserSettings
import com.faiz.kalbrowser.data.SettingsRepository
import com.faiz.kalbrowser.domain.Diagnostics
import com.faiz.kalbrowser.domain.UrlNormalizer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

/**
 * Menyimpan satu-satunya sumber kebenaran untuk keadaan browser. WebView tidak
 * menyimpan keputusan apa pun: ia melaporkan apa yang terjadi lewat [onPageStarted]
 * dan kawan-kawan, lalu menjalankan perintah yang datang dari [commands].
 */
class BrowserViewModel(
    private val settings: SettingsRepository,
) : ViewModel() {

    // Keadaan awal diambil dari pengaturan tersimpan, supaya ukuran yang dipilih
    // terakhir tidak perlu dipilih ulang setiap aplikasi dibuka.
    private val _uiState = MutableStateFlow(stateAwal())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    // Channel, bukan StateFlow: perintah yang terlewat tidak boleh diputar ulang
    // saat layar dibuat ulang, karena memuat ulang halaman dua kali terlihat
    // seperti halaman berkedip.
    private val _commands = Channel<BrowserCommand>(Channel.BUFFERED)
    val commands: Flow<BrowserCommand> = _commands.receiveAsFlow()

    fun submitAddress(input: String) {
        if (Diagnostics.isKeyword(input)) {
            _commands.trySend(BrowserCommand.CollectDiagnostics)
            return
        }

        val url = UrlNormalizer.toLoadableUrl(input)
        if (url.isEmpty()) return

        _uiState.update { it.copy(address = url) }
        _commands.trySend(BrowserCommand.LoadUrl(url))
    }

    fun goBack() {
        _commands.trySend(BrowserCommand.GoBack)
    }

    fun goForward() {
        _commands.trySend(BrowserCommand.GoForward)
    }

    /** Satu tombol, dua tindakan: menghentikan saat memuat, memuat ulang saat diam. */
    fun reloadOrStop() {
        val command = if (_uiState.value.isLoading) BrowserCommand.Stop else BrowserCommand.Reload
        _commands.trySend(command)
    }

    /**
     * Memilih ukuran dan orientasi sekaligus. Sidebar menampilkan sepuluh kartu,
     * jadi memilih kartu berarti menentukan keduanya dalam satu ketukan. Kalau
     * orientasi diubah lewat jalur terpisah, kartu yang ditandai bisa berbeda
     * dari ukuran yang benar-benar dipakai halaman.
     */
    fun selectSize(index: Int, portrait: Boolean) {
        if (index !in PRESETS.indices) return
        val state = _uiState.value
        if (index == state.selectedPresetIndex && portrait == state.isPortrait) return

        _uiState.update { it.copy(selectedPresetIndex = index, isPortrait = portrait) }
        simpanPengaturan()
        _commands.trySend(BrowserCommand.ApplyViewport(_uiState.value.viewportWidth))
    }

    fun onPageStarted(url: String) {
        _uiState.update { it.copy(address = url, isLoading = true, progress = 0) }
    }

    fun onPageFinished(url: String) {
        _uiState.update { it.copy(address = url, isLoading = false, progress = 100) }
    }

    fun onUrlChanged(url: String) {
        _uiState.update { it.copy(address = url) }
    }

    fun onProgressChanged(progress: Int) {
        val clamped = progress.coerceIn(0, 100)
        _uiState.update { it.copy(progress = clamped, isLoading = clamped < 100) }
    }

    fun onHistoryChanged(canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.update { it.copy(canGoBack = canGoBack, canGoForward = canGoForward) }
    }

    private fun stateAwal(): BrowserUiState {
        val tersimpan = settings.load().sanitized()
        return BrowserUiState(
            selectedPresetIndex = tersimpan.presetIndex,
            isPortrait = tersimpan.isPortrait,
        )
    }

    private fun simpanPengaturan() {
        val state = _uiState.value
        settings.save(BrowserSettings(state.selectedPresetIndex, state.isPortrait))
    }

    companion object {
        fun factory(settings: SettingsRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { BrowserViewModel(settings) }
        }
    }
}
