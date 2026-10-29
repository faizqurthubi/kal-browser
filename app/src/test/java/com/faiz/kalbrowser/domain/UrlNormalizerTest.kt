package com.faiz.kalbrowser.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UrlNormalizerTest {

    @Test
    fun `alamat tanpa skema diberi https`() {
        assertThat(UrlNormalizer.toLoadableUrl("example.com"))
            .isEqualTo("https://example.com")
    }

    @Test
    fun `alamat dengan skema dibiarkan apa adanya`() {
        assertThat(UrlNormalizer.toLoadableUrl("http://example.com/a?b=1"))
            .isEqualTo("http://example.com/a?b=1")
    }

    @Test
    fun `localhost dengan skema tidak dianggap pencarian`() {
        assertThat(UrlNormalizer.toLoadableUrl("http://localhost:8080"))
            .isEqualTo("http://localhost:8080")
    }

    @Test
    fun `kata tanpa titik dijadikan pencarian`() {
        assertThat(UrlNormalizer.toLoadableUrl("android"))
            .isEqualTo("https://www.google.com/search?q=android")
    }

    @Test
    fun `kalimat dengan spasi dijadikan pencarian`() {
        assertThat(UrlNormalizer.toLoadableUrl("cara membuat kopi"))
            .isEqualTo("https://www.google.com/search?q=cara+membuat+kopi")
    }

    @Test
    fun `spasi di awal dan akhir dibuang`() {
        assertThat(UrlNormalizer.toLoadableUrl("  example.com  "))
            .isEqualTo("https://example.com")
    }

    @Test
    fun `masukan kosong tidak menghasilkan alamat`() {
        assertThat(UrlNormalizer.toLoadableUrl("   ")).isEmpty()
    }
}
