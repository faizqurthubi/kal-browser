package com.faiz.kalbrowser.domain

import com.faiz.kalbrowser.domain.ChromeVisibility.Aksi
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ChromeVisibilityTest {

    private val ambang = 8

    private fun putuskan(
        tersembunyi: Boolean = false,
        geser: Int,
        posisiGulir: Int = 500,
        alamatDifokuskan: Boolean = false,
    ) = ChromeVisibility.putuskan(tersembunyi, geser, posisiGulir, ambang, alamatDifokuskan)

    @Test
    fun `menggulir ke bawah menyembunyikan bilah`() {
        assertThat(putuskan(geser = 40)).isEqualTo(Aksi.Sembunyikan)
    }

    @Test
    fun `menggulir ke atas memunculkan bilah`() {
        assertThat(putuskan(tersembunyi = true, geser = -40)).isEqualTo(Aksi.Tampilkan)
    }

    @Test
    fun `getaran kecil di bawah ambang tidak mengubah apa pun`() {
        assertThat(putuskan(geser = 3)).isEqualTo(Aksi.TidakBerubah)
        assertThat(putuskan(tersembunyi = true, geser = -3)).isEqualTo(Aksi.TidakBerubah)
    }

    @Test
    fun `di puncak halaman bilah selalu tampil`() {
        // Menyembunyikan bilah di puncak membuat pengguna tidak bisa
        // memunculkannya lagi, karena menggulir naik sudah tidak mungkin.
        assertThat(putuskan(tersembunyi = true, geser = 40, posisiGulir = 0))
            .isEqualTo(Aksi.Tampilkan)
    }

    @Test
    fun `kolom alamat yang sedang dipakai tidak pernah disembunyikan`() {
        assertThat(putuskan(geser = 200, alamatDifokuskan = true)).isEqualTo(Aksi.TidakBerubah)
        assertThat(putuskan(tersembunyi = true, geser = 0, alamatDifokuskan = true))
            .isEqualTo(Aksi.Tampilkan)
    }

    @Test
    fun `aksi yang sama tidak diulang`() {
        assertThat(putuskan(tersembunyi = true, geser = 40)).isEqualTo(Aksi.TidakBerubah)
        assertThat(putuskan(tersembunyi = false, geser = -40)).isEqualTo(Aksi.TidakBerubah)
    }
}
