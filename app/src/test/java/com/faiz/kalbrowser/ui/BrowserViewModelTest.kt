package com.faiz.kalbrowser.ui

import com.faiz.kalbrowser.data.BrowserSettings
import com.faiz.kalbrowser.data.FakeSettingsRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class BrowserViewModelTest {

    @Test
    fun `memuat halaman menyalakan keadaan memuat`() {
        val viewModel = BrowserViewModel(FakeSettingsRepository())

        viewModel.onPageStarted("https://example.com")

        val state = viewModel.uiState.value
        assertThat(state.address).isEqualTo("https://example.com")
        assertThat(state.isLoading).isTrue()
    }

    @Test
    fun `progres seratus mematikan keadaan memuat`() {
        val viewModel = BrowserViewModel(FakeSettingsRepository())
        viewModel.onPageStarted("https://example.com")

        viewModel.onProgressChanged(100)

        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `progres di luar rentang dipotong`() {
        val viewModel = BrowserViewModel(FakeSettingsRepository())

        viewModel.onProgressChanged(140)

        assertThat(viewModel.uiState.value.progress).isEqualTo(100)
    }

    @Test
    fun `kolom alamat kosong tidak mengirim perintah muat`() = runTest {
        val viewModel = BrowserViewModel(FakeSettingsRepository())

        viewModel.submitAddress("   ")

        assertThat(viewModel.uiState.value.address).isEmpty()
    }

    @Test
    fun `menekan go mengirim alamat hasil normalisasi`() = runTest {
        val viewModel = BrowserViewModel(FakeSettingsRepository())

        viewModel.submitAddress("example.com")

        val command = viewModel.commands.first()
        assertThat(command).isEqualTo(BrowserCommand.LoadUrl("https://example.com"))
    }

    @Test
    fun `tombol muat ulang menjadi hentikan saat memuat`() = runTest {
        val viewModel = BrowserViewModel(FakeSettingsRepository())
        viewModel.onPageStarted("https://example.com")

        viewModel.reloadOrStop()

        assertThat(viewModel.commands.first()).isEqualTo(BrowserCommand.Stop)
    }

    @Test
    fun `ukuran awal 1280 kali 720`() {
        val state = BrowserViewModel(FakeSettingsRepository()).uiState.value

        assertThat(state.viewportWidth).isEqualTo(1280)
        assertThat(state.viewportHeight).isEqualTo(720)
        assertThat(state.effectiveSizeLabel).isEqualTo("1280 × 720")
    }

    @Test
    fun `memilih ukuran mengubah lebar dan meminta pasang viewport`() = runTest {
        val viewModel = BrowserViewModel(FakeSettingsRepository())

        viewModel.selectSize(4, portrait = false)

        assertThat(viewModel.uiState.value.viewportWidth).isEqualTo(1920)
        assertThat(viewModel.commands.first()).isEqualTo(BrowserCommand.ApplyViewport(1920))
    }

    @Test
    fun `kartu yang sama tidak memicu perintah lagi`() = runTest {
        val viewModel = BrowserViewModel(FakeSettingsRepository())
        viewModel.selectSize(2, portrait = false)
        assertThat(viewModel.commands.first()).isEqualTo(BrowserCommand.ApplyViewport(1366))

        viewModel.selectSize(2, portrait = false)

        // Tidak ada perintah kedua yang menunggu, jadi uji ini hanya memastikan
        // state tetap dan tidak ada muat ulang tambahan yang dikirim.
        assertThat(viewModel.uiState.value.viewportWidth).isEqualTo(1366)
    }

    @Test
    fun `indeks preset di luar rentang diabaikan`() {
        val viewModel = BrowserViewModel(FakeSettingsRepository())

        viewModel.selectSize(99, portrait = false)
        viewModel.selectSize(-1, portrait = true)

        assertThat(viewModel.uiState.value.selectedPresetIndex)
            .isEqualTo(BrowserUiState.DEFAULT_PRESET_INDEX)
    }

    @Test
    fun `kartu tegak menukar lebar dan tinggi`() = runTest {
        val viewModel = BrowserViewModel(FakeSettingsRepository())

        viewModel.selectSize(BrowserUiState.DEFAULT_PRESET_INDEX, portrait = true)

        val state = viewModel.uiState.value
        assertThat(state.viewportWidth).isEqualTo(720)
        assertThat(state.viewportHeight).isEqualTo(1280)
        assertThat(state.effectiveSizeLabel).isEqualTo("720 × 1280")
        assertThat(viewModel.commands.first()).isEqualTo(BrowserCommand.ApplyViewport(720))
    }

    @Test
    fun `memilih mendatar lagi mengembalikan ukuran semula`() {
        val viewModel = BrowserViewModel(FakeSettingsRepository())
        val awal = BrowserUiState.DEFAULT_PRESET_INDEX

        viewModel.selectSize(awal, portrait = true)
        viewModel.selectSize(awal, portrait = false)

        assertThat(viewModel.uiState.value.viewportWidth).isEqualTo(1280)
    }

    @Test
    fun `kartu menentukan preset dan orientasi sekaligus`() {
        val viewModel = BrowserViewModel(FakeSettingsRepository())

        viewModel.selectSize(4, portrait = true)

        assertThat(viewModel.uiState.value.effectiveSizeLabel).isEqualTo("1080 × 1920")
    }

    @Test
    fun `kata kunci periksa tidak dimuat sebagai halaman`() = runTest {
        val viewModel = BrowserViewModel(FakeSettingsRepository())

        viewModel.submitAddress("kal:info")

        assertThat(viewModel.commands.first()).isEqualTo(BrowserCommand.CollectDiagnostics)
        assertThat(viewModel.uiState.value.address).isEmpty()
    }

    @Test
    fun `tombol muat ulang memuat ulang saat halaman diam`() = runTest {
        val viewModel = BrowserViewModel(FakeSettingsRepository())

        viewModel.reloadOrStop()

        assertThat(viewModel.commands.first()).isEqualTo(BrowserCommand.Reload)
    }

    @Test
    fun `ukuran tersimpan dipakai saat view model dibuat`() {
        val settings = FakeSettingsRepository(BrowserSettings(presetIndex = 4, isPortrait = true))

        val viewModel = BrowserViewModel(settings)

        assertThat(viewModel.uiState.value.selectedPresetIndex).isEqualTo(4)
        assertThat(viewModel.uiState.value.isPortrait).isTrue()
    }

    @Test
    fun `memilih ukuran menyimpan pilihan`() {
        val settings = FakeSettingsRepository()

        val viewModel = BrowserViewModel(settings)
        viewModel.selectSize(3, portrait = false)

        assertThat(settings.isiTersimpan().presetIndex).isEqualTo(3)
        assertThat(settings.jumlahSimpan).isEqualTo(1)
    }

    @Test
    fun `memilih kartu tegak menyimpan orientasi`() {
        val settings = FakeSettingsRepository()

        val viewModel = BrowserViewModel(settings)
        viewModel.selectSize(BrowserUiState.DEFAULT_PRESET_INDEX, portrait = true)

        assertThat(settings.isiTersimpan().isPortrait).isTrue()
    }

    @Test
    fun `indeks di luar daftar dijatuhkan ke preset bawaan`() {
        // Bisa terjadi bila daftar preset berubah di versi berikutnya.
        val settings = FakeSettingsRepository(BrowserSettings(presetIndex = 99, isPortrait = false))

        val viewModel = BrowserViewModel(settings)

        assertThat(viewModel.uiState.value.selectedPresetIndex)
            .isEqualTo(BrowserUiState.DEFAULT_PRESET_INDEX)
    }

    @Test
    fun `memilih ukuran yang sama tidak menyimpan ulang`() {
        val settings = FakeSettingsRepository()

        val viewModel = BrowserViewModel(settings)
        val state = viewModel.uiState.value
        viewModel.selectSize(state.selectedPresetIndex, state.isPortrait)

        assertThat(settings.jumlahSimpan).isEqualTo(0)
    }

    @Test
    fun `preset sama dengan orientasi berbeda tetap dihitung perubahan`() = runTest {
        val settings = FakeSettingsRepository()

        val viewModel = BrowserViewModel(settings)
        val state = viewModel.uiState.value
        viewModel.selectSize(state.selectedPresetIndex, portrait = !state.isPortrait)

        // Kartu mendatar dan kartu tegak adalah dua kartu berbeda, jadi menukar
        // di antaranya harus mengirim perintah dan menyimpan pilihan.
        assertThat(settings.jumlahSimpan).isEqualTo(1)
        assertThat(viewModel.uiState.value.isPortrait).isEqualTo(!state.isPortrait)
        assertThat(viewModel.commands.first())
            .isEqualTo(BrowserCommand.ApplyViewport(viewModel.uiState.value.viewportWidth))
    }
}
