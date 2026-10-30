package com.faiz.kalbrowser

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Label dipakai di dua tempat (dropdown dan label ukuran), jadi salah ketik di
 * satu preset akan terbaca sebagai dua ukuran berbeda oleh pengguna.
 */
class ScreenPresetTest {

    @Test
    fun `label setiap preset cocok dengan ukurannya`() {
        PRESETS.forEach { preset ->
            assertThat(preset.label).isEqualTo("${preset.w} × ${preset.h}")
        }
    }

    @Test
    fun `preset wajib dari panduan tersedia lengkap`() {
        val sizes = PRESETS.map { it.w to it.h }

        assertThat(sizes).containsExactly(
            1024 to 768,
            1280 to 720,
            1366 to 768,
            1440 to 900,
            1920 to 1080,
        ).inOrder()
    }

    @Test
    fun `tidak ada preset dengan ukuran nol atau negatif`() {
        PRESETS.forEach { preset ->
            assertThat(preset.w).isGreaterThan(0)
            assertThat(preset.h).isGreaterThan(0)
        }
    }

    @Test
    fun `sidebar berisi lima kartu mendatar lalu lima kartu tegak`() {
        assertThat(SIZE_OPTIONS).hasSize(10)
        assertThat(SIZE_OPTIONS.count { it.portrait }).isEqualTo(5)
        assertThat(SIZE_OPTIONS.take(5).all { !it.portrait }).isTrue()
        assertThat(SIZE_OPTIONS.drop(5).all { it.portrait }).isTrue()
    }

    @Test
    fun `kartu tegak adalah kebalikan kartu mendatar`() {
        SIZE_OPTIONS.take(5).zip(SIZE_OPTIONS.drop(5)).forEach { (mendatar, tegak) ->
            assertThat(tegak.presetIndex).isEqualTo(mendatar.presetIndex)
            assertThat(tegak.width).isEqualTo(mendatar.height)
            assertThat(tegak.height).isEqualTo(mendatar.width)
        }
    }

    @Test
    fun `setiap kartu punya pasangan indeks dan orientasi yang unik`() {
        val pasangan = SIZE_OPTIONS.map { it.presetIndex to it.portrait }

        assertThat(pasangan).hasSize(pasangan.toSet().size)
        assertThat(SIZE_OPTIONS.map { it.label }).hasSize(SIZE_OPTIONS.toSet().size)
    }

    @Test
    fun `label kartu cocok dengan ukurannya`() {
        SIZE_OPTIONS.forEach { opsi ->
            assertThat(opsi.label).isEqualTo("${opsi.width} × ${opsi.height}")
        }
    }

    @Test
    fun `rasio kartu mengikuti ukuran sebenarnya`() {
        SIZE_OPTIONS.forEach { opsi ->
            assertThat(opsi.ratio).isWithin(0.0001f).of(opsi.height.toFloat() / opsi.width)
        }

        // Kartu mendatar lebih lebar daripada tinggi, kartu tegak sebaliknya,
        // karena itu keduanya bisa dipisah tanpa membaca label.
        assertThat(SIZE_OPTIONS.filter { !it.portrait }.all { it.ratio < 1f }).isTrue()
        assertThat(SIZE_OPTIONS.filter { it.portrait }.all { it.ratio > 1f }).isTrue()
    }

    @Test
    fun `skala kartu memakai ukuran terbesar sebagai acuan`() {
        assertThat(SIZE_SCALE_BASE).isEqualTo(1920)
    }

    @Test
    fun `kartu terbesar mengisi sisi terpanjang dan yang lain lebih kecil`() {
        val besar = optionFor(4, portrait = false)!!.cardSize(48f)
        assertThat(besar.first).isWithin(0.001f).of(48f)
        assertThat(besar.second).isWithin(0.001f).of(27f)

        // 1024 adalah separuh dari 1920 lebih sedikit, jadi kartunya juga harus
        // lebih kecil, bukan sama lebar.
        val kecil = optionFor(0, portrait = false)!!.cardSize(48f)
        assertThat(kecil.first).isWithin(0.001f).of(25.6f)
        assertThat(kecil.first).isLessThan(besar.first)

        SIZE_OPTIONS.forEach { opsi ->
            val (lebar, tinggi) = opsi.cardSize(48f)
            assertThat(maxOf(lebar, tinggi)).isAtMost(48.001f)
        }
    }

    @Test
    fun `perbandingan sisi kartu sama dengan ukuran sebenarnya`() {
        SIZE_OPTIONS.forEach { opsi ->
            val (lebar, tinggi) = opsi.cardSize(48f)
            assertThat(lebar / tinggi)
                .isWithin(0.0001f)
                .of(opsi.width.toFloat() / opsi.height)
        }
    }

    @Test
    fun `optionFor menemukan kartu yang sedang aktif`() {
        assertThat(optionFor(2, portrait = false)?.label).isEqualTo("1366 × 768")
        assertThat(optionFor(2, portrait = true)?.label).isEqualTo("768 × 1366")
        assertThat(optionFor(9, portrait = false)).isNull()
    }
}
