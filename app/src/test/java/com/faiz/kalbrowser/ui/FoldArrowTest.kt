package com.faiz.kalbrowser.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Panah pita menempati tepi paling atas layar saat bilah alamat terlipat, dan di
 * sana ada lubang kamera depan. Salah hitung di sini membuat panahnya tidak
 * terlihat sama sekali, jadi pergeserannya diuji terpisah dari View.
 */
class FoldArrowTest {

    @Test
    fun `tanpa lubang kamera panah tetap di tengah`() {
        val geser = FoldArrow.geser(
            lebarPita = 393f,
            lebarPanah = 12f,
            tinggiPita = 14f,
            jarak = 6f,
        )

        assertThat(geser).isEqualTo(0f)
    }

    @Test
    fun `lubang di tengah menggeser panah ke kanan lubang`() {
        val lebar = 393f
        val geser = FoldArrow.geser(
            lebarPita = lebar,
            lebarPanah = 12f,
            lubangKiri = 185f,
            lubangKanan = 205f,
            lubangAtas = 0f,
            lubangBawah = 20f,
            tinggiPita = 14f,
            jarak = 6f,
        )

        // Panah harus berada sesudah lubang, bukan di bawahnya.
        val posisiPanah = lebar / 2f + geser
        assertThat(posisiPanah).isGreaterThan(205f)
        assertThat(posisiPanah + 6f).isAtMost(lebar)
    }

    @Test
    fun `lubang yang tidak mengenai pita diabaikan`() {
        val geser = FoldArrow.geser(
            lebarPita = 393f,
            lebarPanah = 12f,
            lubangKiri = 185f,
            lubangKanan = 205f,
            lubangAtas = 40f,
            lubangBawah = 60f,
            tinggiPita = 14f,
            jarak = 6f,
        )

        assertThat(geser).isEqualTo(0f)
    }

    @Test
    fun `lubang di luar titik tengah tidak menggeser apa pun`() {
        val geser = FoldArrow.geser(
            lebarPita = 393f,
            lebarPanah = 12f,
            lubangKiri = 20f,
            lubangKanan = 40f,
            lubangAtas = 0f,
            lubangBawah = 14f,
            tinggiPita = 14f,
            jarak = 6f,
        )

        assertThat(geser).isEqualTo(0f)
    }

    @Test
    fun `geseran tidak pernah mendorong panah keluar pita`() {
        val lebar = 393f
        val geser = FoldArrow.geser(
            lebarPita = lebar,
            lebarPanah = 12f,
            lubangKiri = 0f,
            lubangKanan = 380f,
            lubangAtas = 0f,
            lubangBawah = 14f,
            tinggiPita = 14f,
            jarak = 6f,
        )

        val posisiPanah = lebar / 2f + geser
        assertThat(posisiPanah - 6f).isAtLeast(0f)
        assertThat(posisiPanah + 6f).isAtMost(lebar)
    }

    @Test
    fun `lebar pita nol tidak menghasilkan geseran`() {
        assertThat(
            FoldArrow.geser(
                lebarPita = 0f,
                lebarPanah = 12f,
                lubangKiri = 0f,
                lubangKanan = 10f,
                lubangAtas = 0f,
                lubangBawah = 14f,
                tinggiPita = 14f,
                jarak = 6f,
            ),
        ).isEqualTo(0f)
    }
}
