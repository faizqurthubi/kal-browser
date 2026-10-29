package com.faiz.desktopbrowser.ui

import androidx.lifecycle.ViewModel
import com.faiz.desktopbrowser.domain.UrlNormalizer
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
class BrowserViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()

    // Channel, bukan StateFlow: perintah yang terlewat tidak boleh diputar ulang
    // saat layar dibuat ulang, karena memuat ulang halaman dua kali terlihat
    // seperti halaman berkedip.
    private val _commands = Channel<BrowserCommand>(Channel.BUFFERED)
    val commands: Flow<BrowserCommand> = _commands.receiveAsFlow()

    fun submitAddress(input: String) {
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
}
