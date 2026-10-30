package com.faiz.kalbrowser.ui

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import com.faiz.kalbrowser.R
import com.faiz.kalbrowser.SIZE_OPTIONS
import com.faiz.kalbrowser.SizeOption
import com.faiz.kalbrowser.cardSize

/**
 * Mengisi dua kisi sidebar dengan kartu ukuran.
 *
 * Tiap bagian adalah kisi 4 x 2 dengan sel yang lebarnya dan tingginya sama
 * semua. Label ukuran duduk di dasar sel, dan sisa ruang di atasnya dipakai
 * kotak yang bentuknya mengikuti rasio ukuran aslinya. Kotak itu diletakkan di
 * tengah ruang tersebut, kiri kanan dan atas bawah, jadi perbedaan bentuk
 * terbaca dari posisinya juga.
 *
 * Semua kartu memakai satu skala, sehingga kartu 1920 benar-benar tampil lebih
 * besar daripada kartu 1024 dan bukan hanya berbeda bentuk.
 */
class SizeGrid(
    private val context: Context,
    private val onPilih: (SizeOption) -> Unit,
) {

    /** Kartu yang sudah terpasang, dipakai untuk menandai pilihan aktif. */
    private val kartu = mutableListOf<Triple<SizeOption, View, TextView>>()

    fun pasang(kisiLandscape: GridLayout, kisiPortrait: GridLayout) {
        val sisiTerpanjang = context.resources.getDimension(R.dimen.size_card_longest)
        val lebarSel = context.resources.getDimensionPixelSize(R.dimen.size_cell_width)
        val tinggiSel = context.resources.getDimensionPixelSize(R.dimen.size_cell_height)
        // Label mendapat sisa tinggi sel setelah kartu tertinggi, bukan tinggi
        // bungkusannya, supaya ruang gambar kartu sama untuk semua sel.
        val ruangLabel = (tinggiSel - sisiTerpanjang).toInt()
        val jarakBaris = (10 * context.resources.displayMetrics.density).toInt()

        SIZE_OPTIONS.forEach { opsi ->
            val kisi = if (opsi.portrait) kisiPortrait else kisiLandscape
            kisi.addView(buatSel(opsi, lebarSel, tinggiSel, ruangLabel, jarakBaris, sisiTerpanjang))
        }

        // Dua bagian berisi lima ukuran, sedangkan kisinya empat kolom dua baris.
        // Sisa tiga slot diisi ruang kosong supaya baris kedua tetap setinggi
        // baris pertama, bukan menciut karena hanya punya satu anak.
        repeat(SLOT_KOSONG) { ke ->
            kisiLandscape.addView(buatSlotKosong(lebarSel, tinggiSel, jarakBaris, ke))
            kisiPortrait.addView(buatSlotKosong(lebarSel, tinggiSel, jarakBaris, ke))
        }
    }

    /** Menandai kartu yang sedang dipakai halaman. */
    fun tandaiAktif(presetIndex: Int, portrait: Boolean) {
        kartu.forEach { (opsi, kotak, label) ->
            val aktif = opsi.presetIndex == presetIndex && opsi.portrait == portrait
            kotak.isSelected = aktif
            label.isSelected = aktif
        }
    }

    private fun buatSel(
        opsi: SizeOption,
        lebarSel: Int,
        tinggiSel: Int,
        ruangLabel: Int,
        jarakBaris: Int,
        sisiTerpanjang: Float,
    ): LinearLayout {
        val (lebarKotakDp, tinggiKotakDp) = opsi.cardSize(sisiTerpanjang)
        // Paling kecil 2 dp, karena kartu 720 x 1280 hanya selebar 18 dp.
        val lebarKotak = lebarKotakDp.toInt().coerceAtLeast((2 * context.resources.displayMetrics.density).toInt())
        val tinggiKotak = tinggiKotakDp.toInt().coerceAtLeast((2 * context.resources.displayMetrics.density).toInt())

        val sel = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = paramSel(lebarSel, tinggiSel, jarakBaris)
            isClickable = true
            isFocusable = true
            // Latar ripple diberi bentuk kotak supaya efeknya tidak melebar ke
            // sel sebelah.
            setBackgroundResource(R.drawable.bg_size_cell)
            contentDescription = opsi.label
        }

        val wadahKotak = FrameLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        }

        val kotak = View(context).apply {
            layoutParams = FrameLayout.LayoutParams(lebarKotak, tinggiKotak).apply {
                gravity = Gravity.CENTER
            }
            setBackgroundResource(R.drawable.bg_size_card)
        }

        val label = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ruangLabel)
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f)
            setTextColor(context.getColorStateList(R.color.size_label))
            maxLines = 1
            text = opsi.label
            // Label "1920 × 1080" selebar 63 dp bisa meluber di ukuran 8 sp,
            // jadi ukurannya dibiarkan menyusut sendiri sampai 6 sp.
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this, 6, 8, 1, TypedValue.COMPLEX_UNIT_SP,
            )
        }

        wadahKotak.addView(kotak)
        sel.addView(wadahKotak)
        sel.addView(label)
        sel.setOnClickListener { onPilih(opsi) }
        kartu += Triple(opsi, kotak, label)
        return sel
    }

    /**
     * Slot yang belum berisi ukuran. Ia tidak bisa diketuk dan tidak
     * menggambar apa pun, tugasnya hanya menjaga bentuk kisi tetap 4 x 2.
     */
    private fun buatSlotKosong(
        lebarSel: Int,
        tinggiSel: Int,
        jarakBaris: Int,
        urutan: Int,
    ): View = View(context).apply {
        layoutParams = paramSel(lebarSel, tinggiSel, jarakBaris)
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        contentDescription = null
        tag = "slot_kosong_$urutan"
    }

    private fun paramSel(lebarSel: Int, tinggiSel: Int, jarakBaris: Int) =
        GridLayout.LayoutParams().apply {
            width = lebarSel
            height = tinggiSel
            // Empat sel kali 63 dp sama dengan 252 dp, tepat selebar isi sidebar.
            setMargins(0, 0, 0, jarakBaris)
            setGravity(Gravity.CENTER_HORIZONTAL)
        }

    private companion object {
        /** Empat kolom dua baris dikurangi lima ukuran yang ada sekarang. */
        const val SLOT_KOSONG = 3
    }
}
