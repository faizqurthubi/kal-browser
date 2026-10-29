package com.faiz.desktopbrowser.ui

/**
 * Perintah satu kali dari ViewModel ke WebView. Dipisahkan dari
 * [BrowserUiState] karena memuat ulang halaman bukan keadaan, melainkan
 * tindakan yang hanya boleh terjadi sekali.
 */
sealed interface BrowserCommand {

    data class LoadUrl(val url: String) : BrowserCommand

    data object GoBack : BrowserCommand

    data object GoForward : BrowserCommand

    data object Reload : BrowserCommand

    data object Stop : BrowserCommand
}
