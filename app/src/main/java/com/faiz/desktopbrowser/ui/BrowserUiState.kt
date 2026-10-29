package com.faiz.desktopbrowser.ui

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
)
