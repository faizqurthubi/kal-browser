package com.faiz.kalbrowser.domain

/**
 * Kata kunci di kolom alamat untuk menampilkan angka yang dilihat halaman.
 *
 * Ini alat periksa, bukan fitur: bentuknya sengaja bukan tombol atau menu, supaya
 * tidak menambah satu piksel pun chrome browser.
 */
object Diagnostics {

    const val KEYWORD = "kal:info"

    fun isKeyword(input: String): Boolean = input.trim().equals(KEYWORD, ignoreCase = true)
}
