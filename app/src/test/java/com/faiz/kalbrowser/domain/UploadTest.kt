package com.faiz.kalbrowser.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UploadTest {

    @Test
    fun `tanpa accept semua tipe diizinkan`() {
        assertThat(Upload.typesFor(null)).containsExactly("*/*")
        assertThat(Upload.typesFor(emptyArray())).containsExactly("*/*")
        assertThat(Upload.typesFor(arrayOf(""))).containsExactly("*/*")
    }

    @Test
    fun `ekstensi diterjemahkan menjadi tipe`() {
        assertThat(Upload.typesFor(arrayOf(".pdf")))
            .containsExactly("application/pdf")
        assertThat(Upload.typesFor(arrayOf(".jpg", ".png")))
            .containsExactly("image/jpeg", "image/png")
    }

    @Test
    fun `beberapa nilai dalam satu accept dipisah`() {
        assertThat(Upload.typesFor(arrayOf("image/*, .pdf")))
            .containsExactly("image/*", "application/pdf")
    }

    @Test
    fun `nilai tidak dikenal dibuang`() {
        // Situs kadang menulis accept yang bukan tipe dan bukan ekstensi.
        assertThat(Upload.typesFor(arrayOf("berkas", ".tidakdikenal", "image/png")))
            .containsExactly("image/png")
    }

    @Test
    fun `tipe ganda dihapus`() {
        assertThat(Upload.typesFor(arrayOf("image/png, image/png")))
            .containsExactly("image/png")
    }

    @Test
    fun `tipe utama mengikuti kelompok yang seragam`() {
        assertThat(Upload.primaryType(listOf("image/jpeg", "image/png"))).isEqualTo("image/*")
        assertThat(Upload.primaryType(listOf("application/pdf"))).isEqualTo("application/pdf")
        assertThat(Upload.primaryType(listOf("video/mp4", "audio/mpeg"))).isEqualTo("*/*")
        assertThat(Upload.primaryType(listOf("*/*"))).isEqualTo("*/*")
    }
}
