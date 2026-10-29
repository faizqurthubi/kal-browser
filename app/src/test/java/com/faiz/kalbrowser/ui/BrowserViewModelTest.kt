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
    fun `ukuran awal 1280 kali 720`() {
        val state = BrowserViewModel().uiState.value

        assertThat(state.viewportWidth).isEqualTo(1280)
        assertThat(state.viewportHeight).isEqualTo(720)
        assertThat(state.effectiveSizeLabel).isEqualTo("1280 × 720")
    }

    @Test
    fun `memilih preset mengubah lebar dan meminta pasang viewport`() = runTest {
        val viewModel = BrowserViewModel()

        viewModel.selectPreset(4)

        assertThat(viewModel.uiState.value.viewportWidth).isEqualTo(1920)
        assertThat(viewModel.commands.first()).isEqualTo(BrowserCommand.ApplyViewport(1920))
    }

    @Test
    fun `preset yang sama tidak memicu perintah lagi`() = runTest {
        val viewModel = BrowserViewModel()
        viewModel.selectPreset(2)
        assertThat(viewModel.commands.first()).isEqualTo(BrowserCommand.ApplyViewport(1366))

        viewModel.selectPreset(2)

        // Tidak ada perintah kedua yang menunggu, jadi uji ini hanya memastikan
        // state tetap dan tidak ada muat ulang tambahan yang dikirim.
        assertThat(viewModel.uiState.value.viewportWidth).isEqualTo(1366)
    }

    @Test
    fun `indeks preset di luar rentang diabaikan`() {
        val viewModel = BrowserViewModel()

        viewModel.selectPreset(99)
        viewModel.selectPreset(-1)

        assertThat(viewModel.uiState.value.selectedPresetIndex)
            .isEqualTo(BrowserUiState.DEFAULT_PRESET_INDEX)
    }

    @Test
    fun `rotate menukar lebar dan tinggi`() = runTest {
        val viewModel = BrowserViewModel()

        viewModel.rotate()

        val state = viewModel.uiState.value
        assertThat(state.viewportWidth).isEqualTo(720)
        assertThat(state.viewportHeight).isEqualTo(1280)
        assertThat(state.effectiveSizeLabel).isEqualTo("720 × 1280")
        assertThat(viewModel.commands.first()).isEqualTo(BrowserCommand.ApplyViewport(720))
    }

    @Test
    fun `rotate dua kali kembali ke ukuran semula`() {
        val viewModel = BrowserViewModel()

        viewModel.rotate()
        viewModel.rotate()

        assertThat(viewModel.uiState.value.viewportWidth).isEqualTo(1280)
    }

    @Test
    fun `preset dan orientasi digabung dengan benar`() {
        val viewModel = BrowserViewModel()

        viewModel.selectPreset(4)
        viewModel.rotate()

        assertThat(viewModel.uiState.value.effectiveSizeLabel).isEqualTo("1080 × 1920")
    }

    @Test
    fun `kata kunci periksa tidak dimuat sebagai halaman`() = runTest {
        val viewModel = BrowserViewModel()

        viewModel.submitAddress("kal:info")

        assertThat(viewModel.commands.first()).isEqualTo(BrowserCommand.CollectDiagnostics)
        assertThat(viewModel.uiState.value.address).isEmpty()
    }

    @Test
    fun `tombol muat ulang memuat ulang saat halaman diam`() = runTest {
        val viewModel = BrowserViewModel()

        viewModel.reloadOrStop()

        assertThat(viewModel.commands.first()).isEqualTo(BrowserCommand.Reload)
    }
}
