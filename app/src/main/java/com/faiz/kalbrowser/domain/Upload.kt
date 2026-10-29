package com.faiz.kalbrowser.domain

/**
 * Menerjemahkan atribut accept pada input file HTML menjadi tipe yang
 * dimengerti pemilih berkas Android.
 *
 * Nilai yang tidak sah dibuang, bukan diteruskan. Intent dengan tipe tidak
 * dikenal membuat pemilih berkas menolak terbuka, sehingga tombol unggah di
 * situs tampak mati padahal aplikasinya tidak salah.
 */
object Upload {

    const val SEMUA = "*/*"

    private val EKSTENSI = mapOf(
        ".pdf" to "application/pdf",
        ".jpg" to "image/jpeg",
        ".jpeg" to "image/jpeg",
        ".png" to "image/png",
        ".gif" to "image/gif",
        ".webp" to "image/webp",
        ".heic" to "image/heic",
        ".svg" to "image/svg+xml",
        ".txt" to "text/plain",
        ".csv" to "text/csv",
        ".zip" to "application/zip",
        ".doc" to "application/msword",
        ".docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        ".xls" to "application/vnd.ms-excel",
        ".xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        ".ppt" to "application/vnd.ms-powerpoint",
        ".pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        ".mp4" to "video/mp4",
        ".mp3" to "audio/mpeg",
    )

    /**
     * Daftar tipe yang boleh dipilih. Satu atribut accept bisa berisi beberapa
     * nilai dipisah koma, dan bisa berupa ekstensi seperti ".pdf".
     */
    fun typesFor(accept: Array<String>?): List<String> {
        val hasil = mutableListOf<String>()
        accept?.forEach { bagian ->
            bagian.split(',').forEach { kandidat ->
                val teks = kandidat.trim().lowercase()
                when {
                    teks.isEmpty() -> Unit
                    teks.startsWith(".") -> EKSTENSI[teks]?.let { hasil += it }
                    teks.contains('/') -> hasil += teks
                }
            }
        }
        return hasil.distinct().ifEmpty { listOf(SEMUA) }
    }

    /**
     * Tipe untuk setType. Tipe campuran tidak sah di posisi ini, jadi yang
     * dipakai hanya kelompok besar yang seragam, atau semua tipe.
     */
    fun primaryType(types: List<String>): String {
        val pertama = types.firstOrNull().orEmpty()
        if (types.size == 1 && pertama != SEMUA) return pertama
        return when {
            types.all { it.startsWith("image/") } -> "image/*"
            types.all { it.startsWith("video/") } -> "video/*"
            types.all { it.startsWith("audio/") } -> "audio/*"
            types.all { it.startsWith("text/") } -> "text/*"
            else -> SEMUA
        }
    }
}
