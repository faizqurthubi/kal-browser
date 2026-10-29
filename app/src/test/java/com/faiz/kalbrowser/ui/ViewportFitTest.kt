package com.faiz.kalbrowser.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ViewportFitTest {

    // HP dari laporan pemilik: layar 393 x 873 dp, kerapatan 2.75.
    private val density = 2.75f
    private val boxWidth = 1080

    @Test
    fun `1920 dengan layar 393 dp memakai transformasi`() {
        // 393 / 1920 = 0.205, di bawah batas 0.25, jadi jalur biasa mustahil.
        assertThat(ViewportFit.neededScale(boxWidth, density, 1920)).isWithin(0.001).of(0.2045)
        assertThat(ViewportFit.modeFor(boxWidth, density, 1920))
            .isEqualTo(ViewportFit.Mode.Transform)
    }

    @Test
    fun `1280 masih memakai jalur biasa`() {
        // 393 / 1280 = 0.307, masih di atas batas ditambah jarak aman.
        assertThat(ViewportFit.modeFor(boxWidth, density, 1280))
            .isEqualTo(ViewportFit.Mode.Native)
    }

    @Test
    fun `1366 masih memakai jalur biasa`() {
        assertThat(ViewportFit.modeFor(boxWidth, density, 1366))
            .isEqualTo(ViewportFit.Mode.Native)
    }

    @Test
    fun `1440 jatuh ke transformasi karena terlalu dekat dengan batas`() {
        // 393 / 1440 = 0.273. Masih di atas 0.25, tapi jarak 0.023 lebih kecil
        // dari jarak aman, jadi tidak dipertaruhkan.
        assertThat(ViewportFit.neededScale(boxWidth, density, 1440)).isWithin(0.001).of(0.2730)
        assertThat(ViewportFit.modeFor(boxWidth, density, 1440))
            .isEqualTo(ViewportFit.Mode.Transform)
    }

    @Test
    fun `layar lebar memakai jalur biasa untuk semua preset`() {
        // Tablet 1200 dp: skala untuk 1920 adalah 0.625.
        assertThat(ViewportFit.modeFor(3300, 2.75f, 1920))
            .isEqualTo(ViewportFit.Mode.Native)
    }

    @Test
    fun `faktor transformasi membuat kotak selebar mungkin`() {
        val skala = ViewportFit.transformScale(boxWidth, 1920)

        assertThat(skala).isWithin(0.0001f).of(0.5625f)
        // Lebar virtual dikali faktor harus kembali menjadi lebar kotak.
        assertThat(1920 * skala).isWithin(0.01f).of(boxWidth.toFloat())
    }

    @Test
    fun `masukan tidak masuk akal tidak menghasilkan pembagian nol`() {
        assertThat(ViewportFit.neededScale(0, 0f, 1920)).isEqualTo(0.0)
        assertThat(ViewportFit.transformScale(1080, 0)).isEqualTo(1f)
    }
}
