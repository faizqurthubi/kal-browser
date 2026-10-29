package com.faiz.kalbrowser.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DiagnosticsTest {

    @Test
    fun `kata kunci dikenali`() {
        assertThat(Diagnostics.isKeyword("kal:info")).isTrue()
    }

    @Test
    fun `spasi dan huruf besar diabaikan`() {
        assertThat(Diagnostics.isKeyword("  KAL:INFO  ")).isTrue()
    }

    @Test
    fun `alamat biasa bukan kata kunci`() {
        assertThat(Diagnostics.isKeyword("example.com")).isFalse()
        assertThat(Diagnostics.isKeyword("kal:info.co.id")).isFalse()
        assertThat(Diagnostics.isKeyword("")).isFalse()
    }
}
