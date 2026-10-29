package com.faiz.kalbrowser.ui

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class BrowserViewModelTest {

    @Test
    fun `memuat halaman menyalakan keadaan memuat`() {
        val viewModel = BrowserViewModel()

        viewModel.onPageStarted("https://example.com")

        val state = viewModel.uiState.value
        assertThat(state.address).isEqualTo("https://example.com")
        assertThat(state.isLoading).isTrue()
    }

    @Test
    fun `progres seratus mematikan keadaan memuat`() {
        val viewModel = BrowserViewModel()
        viewModel.onPageStarted("https://example.com")

        viewModel.onProgressChanged(100)

        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `progres di luar rentang dipotong`() {
        val viewModel = BrowserViewModel()

        viewModel.onProgressChanged(140)

        assertThat(viewModel.uiState.value.progress).isEqualTo(100)
    }

    @Test
    fun `kolom alamat kosong tidak mengirim perintah muat`() = runTest {
        val viewModel = BrowserViewModel()

        viewModel.submitAddress("   ")

        assertThat(viewModel.uiState.value.address).isEmpty()
    }

    @Test
    fun `menekan go mengirim alamat hasil normalisasi`() = runTest {
        val viewModel = BrowserViewModel()

        viewModel.submitAddress("example.com")

        val command = viewModel.commands.first()
        assertThat(command).isEqualTo(BrowserCommand.LoadUrl("https://example.com"))
    }

    @Test
    fun `tombol muat ulang menjadi hentikan saat memuat`() = runTest {
        val viewModel = BrowserViewModel()
        viewModel.onPageStarted("https://example.com")

        viewModel.reloadOrStop()

        assertThat(viewModel.commands.first()).isEqualTo(BrowserCommand.Stop)
    }

    @Test
    fun `tombol muat ulang memuat ulang saat halaman diam`() = runTest {
        val viewModel = BrowserViewModel()

        viewModel.reloadOrStop()

        assertThat(viewModel.commands.first()).isEqualTo(BrowserCommand.Reload)
    }
}
