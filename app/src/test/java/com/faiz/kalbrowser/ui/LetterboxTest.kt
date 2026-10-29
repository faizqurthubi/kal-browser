package com.faiz.kalbrowser.ui

import com.google.common.truth.Truth.assertThat
import kotlin.math.abs
import org.junit.Test

class LetterboxTest {

    private val phonePortrait = 1080 to 1920

    @Test
    fun `container yang belum diukur tidak menghasilkan kotak`() {
        assertThat(Letterbox.boxFor(0, 1920, 1280, 720)).isNull()
        assertThat(Letterbox.boxFor(1080, 0, 1280, 720)).isNull()
    }

    @Test
    fun `ukuran virtual nol tidak menghasilkan kotak`() {
        assertThat(Letterbox.boxFor(1080, 1920, 0, 720)).isNull()
        assertThat(Letterbox.boxFor(1080, 1920, 1280, 0)).isNull()
    }

    @Test
    fun `layar lebar di ponsel portrait menghasilkan bilah hitam atas dan bawah`() {
        val box = Letterbox.boxFor(phonePortrait.first, phonePortrait.second, 1280, 720)

        assertThat(box).isNotNull()
        assertThat(box!!.width).isEqualTo(1080)
        assertThat(box.height).isLessThan(phonePortrait.second)
        // 720 dari 1280 lebar, dibulatkan.
        assertThat(box.height).isEqualTo(608)
    }

    @Test
    fun `layar portrait mengisi tinggi dan menyisakan bilah hitam kiri kanan`() {
        val box = Letterbox.boxFor(phonePortrait.first, phonePortrait.second, 720, 1280)

        assertThat(box).isNotNull()
        assertThat(box!!.width).isEqualTo(1080)
        assertThat(box.height).isEqualTo(1920)
    }

    @Test
    fun `rasio kotak tidak pernah melebar dari rasio virtual`() {
        val kasus = listOf(
            Triple(1080, 1920, 1280 to 720),
            Triple(1080, 2340, 1920 to 1080),
            Triple(720, 1280, 1024 to 768),
            Triple(1440, 3200, 1366 to 768),
            Triple(1000, 1000, 1440 to 900),
        )

        kasus.forEach { (cw, ch, virtual) ->
            val box = Letterbox.boxFor(cw, ch, virtual.first, virtual.second)!!
            val rasiVirtual = virtual.first.toDouble() / virtual.second
            val rasiKotak = box.width.toDouble() / box.height

            // Selisih hanya boleh datang dari pembulatan piksel.
            assertThat(abs(rasiKotak - rasiVirtual)).isLessThan(0.02)
        }
    }

    @Test
    fun `kotak tidak pernah lebih besar dari container`() {
        val kasus = listOf(
            Triple(1080, 1920, 1280 to 720),
            Triple(1080, 2340, 1920 to 1080),
            Triple(720, 1280, 1024 to 768),
            Triple(1440, 3200, 1366 to 768),
            Triple(1000, 1000, 1440 to 900),
        )

        kasus.forEach { (cw, ch, virtual) ->
            val box = Letterbox.boxFor(cw, ch, virtual.first, virtual.second)!!

            assertThat(box.width).isAtMost(cw)
            assertThat(box.height).isAtMost(ch)
        }
    }

    @Test
    fun `container yang rasionya sama persis terisi penuh`() {
        val box = Letterbox.boxFor(1280, 720, 1280, 720)!!

        assertThat(box.width).isEqualTo(1280)
        assertThat(box.height).isEqualTo(720)
    }

    @Test
    fun `container sangat sempit tetap menghasilkan kotak minimal satu piksel`() {
        val box = Letterbox.boxFor(1, 1, 1920, 1080)!!

        assertThat(box.width).isAtLeast(1)
        assertThat(box.height).isAtLeast(1)
    }
}
