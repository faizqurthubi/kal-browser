package com.faiz.kalbrowser.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DownloadTest {

    @Test
    fun `hanya http dan https yang bisa diunduh`() {
        assertThat(Download.isDownloadable("https://situs.test/berkas.pdf")).isTrue()
        assertThat(Download.isDownloadable("http://situs.test/berkas.pdf")).isTrue()
        assertThat(Download.isDownloadable("data:text/html,halo")).isFalse()
        assertThat(Download.isDownloadable("blob:https://situs.test/123")).isFalse()
        assertThat(Download.isDownloadable("javascript:void(0)")).isFalse()
        assertThat(Download.isDownloadable("https://")).isFalse()
        assertThat(Download.isDownloadable("")).isFalse()
    }

    @Test
    fun `nama dari content disposition dipakai lebih dulu`() {
        val nama = Download.fileName(
            contentDisposition = "attachment; filename=\"Laporan Tahunan 2025.pdf\"",
            url = "https://situs.test/unduh?id=9",
            fallback = "berkas",
        )

        assertThat(nama).isEqualTo("Laporan Tahunan 2025.pdf")
    }

    @Test
    fun `nama berenkode pada filename bintang dibaca`() {
        val nama = Download.fileName(
            contentDisposition = "attachment; filename*=UTF-8''Laporan%20Tahunan.pdf",
            url = "https://situs.test/x",
            fallback = "berkas",
        )

        assertThat(nama).isEqualTo("Laporan Tahunan.pdf")
    }

    @Test
    fun `tanpa header nama diambil dari potongan alamat`() {
        val nama = Download.fileName(
            contentDisposition = null,
            url = "https://situs.test/aset/dokumen/profil.pdf?v=2#halaman",
            fallback = "berkas",
        )

        assertThat(nama).isEqualTo("profil.pdf")
    }

    @Test
    fun `alamat tanpa berkas memakai nama cadangan`() {
        val nama = Download.fileName(null, "https://situs.test/", "berkas")

        assertThat(nama).isEqualTo("berkas")
    }

    @Test
    fun `pemisah jalur pada nama dibuang supaya tidak menulis ke luar folder`() {
        val nama = Download.fileName(
            contentDisposition = "attachment; filename=\"../../data/rahasia.txt\"",
            url = "https://situs.test/x",
            fallback = "berkas",
        )

        assertThat(nama).doesNotContain("/")
        assertThat(nama).doesNotContain("\\")
        assertThat(nama).doesNotContain("..")
        assertThat(nama).isNotEmpty()
    }

    @Test
    fun `nama dengan tanda plus tidak berubah jadi spasi`() {
        val nama = Download.fileName(null, "https://situs.test/a+b.pdf", "berkas")

        assertThat(nama).isEqualTo("a+b.pdf")
    }

    @Test
    fun `nama yang terlalu panjang dipotong`() {
        val panjang = "a".repeat(400) + ".pdf"
        val nama = Download.fileName(null, "https://situs.test/$panjang", "berkas")

        assertThat(nama.length).isAtMost(120)
    }

    @Test
    fun `header rusak tidak menjatuhkan pemanggil`() {
        val nama = Download.fileName("attachment; filename=\"%E0%A4%A\"", "https://situs.test/a.pdf", "berkas")

        assertThat(nama).isNotEmpty()
    }
}
