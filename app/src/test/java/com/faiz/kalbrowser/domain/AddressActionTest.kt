package com.faiz.kalbrowser.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AddressActionTest {

    @Test
    fun `kotak kosong tidak menampilkan panah`() {
        assertThat(AddressAction.shouldShowGo(focused = true, typed = "", currentUrl = "https://a.test"))
            .isFalse()
        assertThat(AddressAction.shouldShowGo(focused = true, typed = "   ", currentUrl = ""))
            .isFalse()
    }

    @Test
    fun `kotak yang difokuskan dan berisi teks menampilkan panah`() {
        assertThat(AddressAction.shouldShowGo(true, "contoh.test", "https://a.test")).isTrue()
    }

    @Test
    fun `kotak yang sama dengan alamat halaman menyembunyikan panah saat tidak fokus`() {
        val url = "https://contoh.test/halaman"

        assertThat(AddressAction.shouldShowGo(focused = false, typed = url, currentUrl = url))
            .isFalse()
    }

    @Test
    fun `ketikan yang belum dikirim tetap menampilkan panah setelah fokus hilang`() {
        // Inilah lubang yang ditutup: pengguna mengetik, lalu mengetuk di luar
        // kotak tanpa menekan Go. Tanpa aturan ini panah ikut hilang dan teks
        // yang sudah diketik tidak punya jalan keluar.
        assertThat(
            AddressAction.shouldShowGo(
                focused = false,
                typed = "jadwal sholat jakarta",
                currentUrl = "https://a.test",
            ),
        ).isTrue()
    }

    @Test
    fun `spasi di ujung tidak dianggap alamat berbeda`() {
        val url = "https://contoh.test/"

        assertThat(AddressAction.shouldShowGo(focused = false, typed = "  $url  ", currentUrl = url))
            .isFalse()
    }
}
