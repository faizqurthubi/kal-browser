package com.faiz.kalbrowser.ui

/**
 * Menentukan berapa jauh panah pita harus digeser dari tengah.
 *
 * Pita menempati tepi paling atas layar saat bilah alamat terlipat, tepat di
 * tempat lubang kamera depan pada ponsel berpunch hole. Kalau lubangnya menutupi
 * titik tengah, panahnya digeser ke sisi lubang supaya tetap terlihat.
 *
 * Dipisah dari View supaya bisa diuji tanpa perangkat: masukannya hanya angka.
 */
object FoldArrow {

    /**
     * @param lebarPita lebar pita yang bisa dipakai, tanpa padding.
     * @param lebarPanah lebar ikon panah.
     * @param lubangKiri tepi kiri lubang kamera, atau [TIDAK_ADA] kalau tidak ada.
     * @param lubangKanan tepi kanan lubang kamera.
     * @param lubangAtas tepi atas lubang kamera.
     * @param lubangBawah tepi bawah lubang kamera.
     * @param tinggiPita tinggi pita, dipakai untuk tahu apakah lubangnya mengenai pita.
     * @param jarak jarak minimum panah dari tepi lubang.
     */
    fun geser(
        lebarPita: Float,
        lebarPanah: Float,
        lubangKiri: Float = TIDAK_ADA,
        lubangKanan: Float = TIDAK_ADA,
        lubangAtas: Float = 0f,
        lubangBawah: Float = 0f,
        tinggiPita: Float,
        jarak: Float,
    ): Float {
        if (lebarPita <= 0f) return 0f

        // Hanya lubang yang benar-benar mengenai pita yang diurus. Lubang di
        // bawah pita tidak akan menutupi panah.
        val mengenaiPita = lubangKiri != TIDAK_ADA && lubangAtas < tinggiPita && lubangBawah > 0f
        val tengah = lebarPita / 2f
        val menutupiTengah = mengenaiPita && tengah >= lubangKiri && tengah <= lubangKanan
        if (!menutupiTengah) return 0f

        // Panah dipindah ke kanan lubang. Kalau tidak muat, hasilnya dipotong
        // supaya panah tetap berada di dalam pita.
        val tujuan = lubangKanan + jarak + lebarPanah / 2f
        val batas = lebarPita - jarak - lebarPanah / 2f
        return (tujuan - tengah).coerceIn(-batas + tengah, batas - tengah)
    }

    /** Penanda "tidak ada lubang", karena fungsi ini tidak memakai nullable. */
    const val TIDAK_ADA = Float.NaN
}
