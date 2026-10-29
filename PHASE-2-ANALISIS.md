# Analisis fase 2: tata letak

Tanggal: 2026-10-29
Status: usulan, belum dikerjakan
Dasar: delapan permintaan pemilik, diukur terhadap kode `v1.9.1`

## Ringkasan angka

Chrome sekarang memakan ruang vertikal:

| Bagian | Tinggi |
|---|---|
| Bilah status sistem | 24 dp |
| Bilah atas | 48 dp |
| Garis pemisah | 1 dp |
| Baris kedua | 48 dp |
| Garis pemisah | 1 dp |
| **Jumlah** | **122 dp** |

Usulan memakan:

| Bagian | Tinggi |
|---|---|
| Bilah status sistem | 0 dp, disembunyikan |
| Bilah atas | 48 dp |
| Garis pemisah | 1 dp |
| Bilah progres | 2 dp, hanya saat memuat |
| **Jumlah** | **49 dp** |

Selisihnya **73 dp**. Pada ponsel 873 x 393 dp dalam keadaan mendatar, ruang untuk halaman naik dari
sekitar 247 dp menjadi sekitar 320 dp. Itu **tambah 30 persen tinggi yang bisa dipakai**. Ini angka
yang membuat permintaan nomor 5 dan 8 masuk akal, bukan sekadar selera.

## Per permintaan

### 1. Ikon stop diganti X

Permintaan ini benar dan bisa diukur. Panjang garis tiap ikon sekarang:

| Ikon | Panjang garis | Bentuk |
|---|---|---|
| Chevron mundur dan maju | 19,8 satuan | garis 2 dp |
| Busur muat ulang | 36,6 satuan | garis 2 dp |
| Kotak stop | 0 satuan | **bidang penuh 10 x 10, luas 100 satuan persegi** |

Stop adalah satu-satunya ikon yang bukan garis. Ia bidang pejal tanpa rongga, sementara tiga ikon lain
berupa garis 2 dp dengan ruang kosong di dalamnya. Itu sebabnya ia terlihat berat, bukan karena
ukurannya, tapi karena jenisnya berbeda.

Usulan: X digambar sebagai dua garis 2 dp melintang kotak 10 dp, panjang garisnya 28,3 satuan. Angka
itu berada di antara chevron 19,8 dan busur 36,6, jadi beratnya duduk di tengah himpunan, bukan di
luarnya. Aturan yang berlaku untuk semua ikon: satu kisi 24, tebal garis 2, ujung membulat, tanpa
bidang penuh, dan panjang garis antara 18 dan 38 satuan.

### 2. Panah masuk ke dalam kotak alamat

Bisa, dan lebih baik daripada tombol Buka sekarang. Teknisnya kotak alamat dibungkus wadah, panah
diletakkan di dalam di sisi kanan, dan teks diberi jarak kanan 36 dp supaya tidak menabrak panah.

Dua hal yang perlu diketahui:

- Panah hanya muncul saat kotak difokuskan, jadi ia muncul persis ketika papan ketik muncul. Saat itu
  tombol Go di papan ketik juga tersedia. Dua jalan untuk hal yang sama, dan itu tidak apa-apa karena
  satu terlihat dan satu bawaan sistem.
- **Lubang yang perlu ditutup:** kalau pengguna mengetik lalu mengetuk di luar kotak, fokus hilang,
  panah ikut hilang, dan tidak ada lagi cara mengirim selain mengetuk kotak dulu. Usulan saya: panah
  tampil saat kotak difokuskan **atau** saat isi kotak berbeda dari alamat halaman yang sedang
  terbuka. Dengan begitu alamat yang sudah diketik tidak pernah tidak punya jalan keluar.

### 3. Jarak ikon ke kotak alamat dikurangi setengah

Terukur sekarang 16 dp, terdiri dari 12 dp ruang kosong ikon di dalam tombol 48 dp, ditambah 4 dp
margin kotak alamat. Dikurangi setengah menjadi **8 dp**.

Cara mencapainya tanpa mengecilkan sasaran sentuh: margin kotak alamat menjadi 0, dan padding sisi
dalam tombol muat ulang dipotong dari 8 dp ke 4 dp. Tombol tetap 48 dp, jadi jari tetap punya sasaran
sebesar sekarang. Yang menyusut hanya ruang kosongnya.

### 4. Kebab menggantikan tombol Buka

Lebar tombol kebab 48 dp, sama dengan tombol ikon lain, dengan tiga titik 24 dp di tengah. Permintaan
lebar yang sama terpenuhi dengan sendirinya karena memakai gaya tombol yang sama.

### 5. Baris kedua masuk ke kebab

Ini perubahan yang paling berani, dan ada satu kerugian yang wajib disebut: **label ukuran aktif akan
hilang dari layar.** Sekarang pengguna selalu bisa melihat `1920 × 1080` tanpa membuka apa pun.
Kalau semuanya masuk menu, ukuran aktif hanya terlihat setelah menu dibuka.

Tiga pilihan, dan ini yang saya perlu putuskan bersamamu:

| Pilihan | Cara | Kerugian |
|---|---|---|
| A | Label hanya di dalam menu, ditandai centang pada ukuran aktif | Ukuran aktif tidak terlihat tanpa membuka menu |
| B | Label kecil 11 sp di tepi hitam letterbox, pojok kiri atas | Bergantung pada ada tidaknya tepi hitam. Di layar mendatar, tepinya di kiri dan kanan, sempit tapi cukup |
| C | Chip kecil di dalam kotak alamat saat kosong, misalnya `1920 × 1080` sebagai petunjuk | Hanya terlihat saat kotak kosong, dan menambah isi di dalam kotak |

Isi menu yang saya usulkan: judul `Ukuran layar` beserta nilai aktif, lima ukuran dengan tanda centang,
garis pemisah, `Tukar lebar dan tinggi`, dan `Info halaman`.

Bilah progres **tidak** masuk menu. Ia penanda keadaan yang sedang berjalan, bukan pilihan, jadi tetap
berupa garis 2 dp di bawah bilah atas dan hanya memakan ruang saat halaman dimuat.

### 6. Ikon tukar menjadi rotate-cw-square

Bisa. Satu catatan makna: ikon bujur sangkar dengan panah putar bisa terbaca sebagai "putar layar",
dan memang itulah yang dilakukannya, jadi tidak ada masalah.

Catatan ukuran: bujur sangkar keliling penuh ditambah busur panjang akan menjadi ikon terberat di
himpunan. Karena itu digambar dengan satu sudut terbuka dan busur pendek yang menyambung ke situ.
Hasil akhirnya 34,7 satuan garis, masih di dalam rentang 18 sampai 38 yang dipakai semua ikon lain.

### 7. kal:info masuk kebab, dengan tombol salin

Bisa. Dialognya mendapat tiga tombol: `Salin`, `Tutup`, dan isi teksnya tetap apa adanya.

Rincian kecil yang saya usulkan: pesan `Tersalin` hanya muncul di Android 12 ke bawah, karena Android
13 ke atas sudah menampilkan sendiri penanda salin di layar. Menampilkan keduanya jadi berlebihan.

Kata kunci `kal:info` di kolom alamat tetap dipertahankan, supaya cara lama yang sudah tertulis di
README tidak mendadak mati.

### 8. Sembunyikan bilah status

Bisa, dan inilah yang memberi 24 dp paling murah. Tiga pilihan pemicu:

| Pilihan | Perilaku | Kapan cocok |
|---|---|---|
| A | Sembunyi hanya saat mendatar | Paling sesuai alasanmu: mendatar untuk meniru desktop, tegak tetap normal |
| B | Sembunyi selalu saat menjelajah, geser dari atas untuk memunculkan | Tinggi maksimal di semua keadaan, tapi jam dan baterai hilang |
| C | Sembunyi saat halaman digulir ke bawah, muncul saat digulir ke atas | Jam tetap mudah dijangkau, tapi perilakunya berubah-ubah |

Tiga hal teknis yang perlu ditangani, dan saya sudah punya tempatnya karena mekanisme ini dipakai video
fullscreen:

1. **Lubang kamera.** Saat bilah status disembunyikan mendatar, isi layar bisa masuk ke area kamera
   depan. Perlu diatur supaya halaman tidak tertutup lubang itu.
2. **Bilah navigasi bawah.** Di mendatar, bilah gestur bawah juga memakan sekitar 24 dp. Kamu tidak
   memintanya, tapi kalau tujuannya tinggi maksimal, ini sumber ruang berikutnya.
3. **Hitung ulang kotak.** Setiap kali bilah muncul atau hilang, ukuran kotak letterbox berubah, dan
   perhitungannya sudah berjalan lewat pemantau ukuran yang ada.

## Yang harus diputuskan sebelum saya menulis kode

1. Label ukuran aktif: pilihan A, B, atau C di atas.
2. Tombol tukar: tetap terlihat di bilah atas, atau hanya di dalam menu.
3. Pemicu sembunyi bilah status: A, B, atau C.
4. Bilah navigasi bawah ikut disembunyikan atau tidak.

## Biaya yang perlu diterima

Dengan empat tombol di bilah atas, pada ponsel 360 dp dalam keadaan tegak kotak alamat hanya kebagian
sekitar 152 dp. Kalau tombol tukar tetap terlihat, sisanya sekitar 104 dp, dan itu sudah sempit untuk
membaca alamat. Dalam keadaan mendatar tidak ada masalah sama sekali. Karena pemakaian utamamu
mendatar, saya tidak menolak permintaan ini, tapi angkanya perlu kamu tahu.

## Keputusan pemilik dan hasilnya

| Yang diputuskan | Pilihan |
|---|---|
| Label ukuran aktif | Di dalam menu saja, ditandai centang |
| Tombol tukar orientasi | Hanya di dalam menu kebab |
| Bilah status | Selalu disembunyikan saat menjelajah, muncul sementara dengan geser dari tepi atas |
| Bilah navigasi | Tetap terlihat, mengikuti bawaan ponsel |

Ukuran garis semua ikon setelah dikerjakan:

| Ikon | Panjang garis |
|---|---|
| Chevron mundur dan maju | 19,8 satuan |
| Silang hentikan | 28,3 satuan |
| Panah kirim di kotak alamat | 32,4 satuan |
| Bujur sangkar putar | 34,7 satuan |
| Busur muat ulang | 36,7 satuan |

Semuanya berada di dalam rentang yang sama. Ikon stop yang dulu bidang penuh tanpa rongga sudah tidak
ada.
