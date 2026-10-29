package com.faiz.kalbrowser.domain

/**
 * Kapan bilah alamat perlu disembunyikan atau dimunculkan.
 *
 * Aturannya dibuat di sini, bukan di dalam Activity, karena salah satu
 * keputusannya halus: bilah tidak boleh disembunyikan saat pengguna sedang
 * mengetik alamat. Kalau ia hilang di tengah pengetikan, papan ketik menutup
 * kolom yang sedang dipakai.
 */
object ChromeVisibility {

    enum class Aksi {
        TidakBerubah,
        Sembunyikan,
        Tampilkan,
    }

    /**
     * @param tersembunyi keadaan sekarang, supaya aksi yang tidak mengubah apa
     * pun tidak dikirim berulang setiap kali halaman bergulir.
     * @param geser piksel perubahan posisi gulir. Positif berarti halaman
     * bergerak ke atas, isinya bergerak naik, dan itulah saat bilah menyingkir.
     * @param posisiGulir posisi gulir sekarang. Di puncak halaman bilah selalu
     * ditampilkan, karena menyembunyikannya di puncak membuat pengguna tidak
     * punya jalan kembali selain menggulir naik yang mustahil.
     * @param ambang getaran sekecil ini tidak dihitung sebagai niat menggulir.
     * @param alamatDifokuskan selama kolom alamat dipakai, bilah tidak pernah
     * disembunyikan.
     */
    fun putuskan(
        tersembunyi: Boolean,
        geser: Int,
        posisiGulir: Int,
        ambang: Int,
        alamatDifokuskan: Boolean,
    ): Aksi {
        if (alamatDifokuskan) {
            return if (tersembunyi) Aksi.Tampilkan else Aksi.TidakBerubah
        }

        if (posisiGulir <= 0) {
            return if (tersembunyi) Aksi.Tampilkan else Aksi.TidakBerubah
        }

        if (geser > ambang) {
            return if (tersembunyi) Aksi.TidakBerubah else Aksi.Sembunyikan
        }

        if (geser < -ambang) {
            return if (tersembunyi) Aksi.Tampilkan else Aksi.TidakBerubah
        }

        return Aksi.TidakBerubah
    }
}
