# DESIGN.md: Kal Browser

Dokumen arah desain. Isinya keputusan, bukan saran. Setiap keputusan punya alasan satu baris (R-31).

## 1. Aplikasi ini apa

Pembungkus WebView yang memaksa halaman web tampil dalam mode desktop, dengan pengaturan ukuran layar virtual, orientasi virtual, dan URL terakhir yang disimpan.

Dibaca sebagai: alat kerja harian untuk satu orang di ponsel, dengan bahasa visual netral seperti alat sistem bawaan, dial **ENERGY 1 / RHYTHM 1 / MOTION 1**.

## 2. Batas karakter

Aplikasi ini alat, bukan produk yang dipamerkan.

- Tidak ada elemen dekoratif: tanpa gradien, glow, glass, ilustrasi, maskot, emoji, badge, atau kartu yang mengambang.
- Tidak ada angka atau klaim yang tidak bisa dibuktikan: tanpa statistik, tanpa logo pelanggan, tanpa testimoni, tanpa label "cepat" atau "aman" di UI.
- Chrome browser sebesar kebutuhan. Setiap piksel tetap yang dipakai chrome adalah piksel yang hilang dari halaman web, dan halaman web adalah alasan aplikasi ini ada.
- Satu layar. Tidak ada onboarding, tidak ada tab, tidak ada beranda, tidak ada carousel.
- Mode desktop tidak menambah elemen visual apa pun. Tidak ada lencana "Desktop" atau ikon
  monitor, karena status itu tidak bisa dipastikan dari sisi aplikasi: situs tetap bisa
  mendeteksi WebView lewat sinyal lain (panduan bagian 12). Lencana yang mengklaim sesuatu
  yang tidak bisa dibuktikan melanggar aturan no-claims yang sama dengan statistik palsu.

Alasan: pengguna membuka aplikasi ini untuk melihat satu situs, bukan untuk melihat aplikasi ini.

## 3. Warna

Palet achromatic plus satu aksen. Total 2 inti (tinta, kertas) + 1 aksen (sinyal).

| Peran | Terang | Gelap | Dipakai untuk |
|---|---|---|---|
| Kertas | `#FBFBF9` | `#101214` | Latar aplikasi dan seluruh chrome |
| Tinta | `#17191C` | `#ECEDEE` | Teks utama, ikon aktif, tombol Buka |
| Tinta redup | `#5A5F66` | `#A2A7AD` | Teks sekunder, ikon nonaktif |
| Garis kendali | `#7C838B` | `#666D75` | Batas kolom URL |
| Garis pemisah | `#D6D8DB` | `#2A2E33` | Garis tipis antar baris chrome |
| Sinyal | `#B4441F` | `#E8763F` | Satu-satunya aksen |
| Letterbox | `#000000` | `#000000` | Sisa ruang di luar kotak WebView |

Kontras terukur (WCAG AA, dihitung dengan `contrast-check.py`):

- Tinta pada kertas: 17.00:1 terang, 16.01:1 gelap.
- Tinta redup pada permukaan: 6.43:1 terang, 7.28:1 gelap.
- Sinyal pada kertas: 5.35:1 terang, 6.34:1 gelap.
- Garis kendali pada kertas: 3.70:1 terang, 3.58:1 gelap (ambang 3:1 untuk batas kontrol).
- Garis pemisah tidak memikul informasi, jadi tidak dihitung sebagai batas kontrol.

Kenapa aksen sinyal, bukan biru: biru adalah warna bawaan setiap browser dan setiap produk teknologi, sehingga tidak membedakan apa pun. Oranye tanah juga tidak bersaing dengan warna tautan bawaan halaman web.

Letterbox tetap hitam di tema terang maupun gelap, sesuai panduan. Alasannya bukan selera: kotak hitam itu meniru tepi monitor virtual yang sedang ditiru, jadi warnanya tidak boleh ikut tema aplikasi. Kalau letterbox ikut menjadi putih di tema terang, batas antara halaman web dan chrome aplikasi hilang.

**Aksen hanya muncul di tiga tempat, tidak lebih:**

1. Bilah progres muat halaman (2dp, di bawah toolbar).
2. Kolom URL saat difokuskan: garis tepinya dan kursor ketik di dalamnya.
3. Ikon peringatan saat halaman gagal dimuat.

Alasan: aksen yang muncul di setiap tombol berhenti menjadi aksen. Ikon peluncur di home screen memakai warna sinyal juga, tapi itu di luar layar aplikasi dan tidak dihitung dalam anggaran ini.

Tema mengikuti pengaturan terang/gelap sistem, tanpa tombol di dalam aplikasi. Alasan: chrome yang mengikuti sistem tidak pernah bertabrakan dengan halaman web yang sedang dibaca, dan tombol tema di dalam browser adalah kontrol yang tidak diminta siapa pun. Kedua mode dirancang dan diverifikasi, bukan salah satunya.

## 4. Tipografi

Roboto lewat font sistem, ukuran 14sp untuk kolom URL, 13sp untuk label, 16sp untuk teks pesan status.

Alasan: font platform tidak menambah unduhan, tidak menambah jeda render, dan tidak menyisipkan karakter merek ke alat yang tugasnya menghilang di belakang halaman web. Tidak ada monospace, tidak ada huruf kapital berjarak lebar: URL sudah terbaca jelas di Roboto, dan monospace di sini hanya kostum.

## 5. Chrome browser

Dua baris di atas, tidak ada apa pun di bawah. Susunan ini mengikuti panduan bagian 6.1.

```
[ kembali ] [ maju ] [ muat ulang ] [ kolom URL .......... ] [ Buka ]
---------------------------------------------------------- garis 1dp
[ ukuran layar ▾ ]                        [ 1280 × 720 ] [ ⇄ ]
---------------------------------------------------------- garis 1dp
================== bilah progres 2dp (hanya saat memuat)
[                                                     kotak hitam    ]
[                          kotak WebView rasio W:H                  ]
[                                                     kotak hitam    ]
```

- Tinggi tiap baris 48dp. Setiap tombol ikon berukuran 48x48dp.
- Tombol tanpa label teks memakai `contentDescription` berbahasa Indonesia: "Kembali", "Maju", "Muat ulang", "Tukar lebar dan tinggi".
- Kolom URL menampilkan alamat apa adanya setelah dimuat, karena pengguna perlu membaca alamat yang sedang dipalsukan, bukan versi yang sudah dirapikan.
- Tombol kembali dan maju nonaktif saat riwayat habis. Nonaktif berarti tidak bisa ditekan dan ikonnya ikut memudar, bukan hanya berubah warna.
- Tombol muat ulang berubah menjadi tombol hentikan selama halaman dimuat. Satu tombol, dua keadaan, karena keduanya tidak pernah dibutuhkan bersamaan.
- Label ukuran menunjukkan ukuran efektif setelah orientasi virtual diterapkan. Saat belum diputar angkanya sama dengan isi dropdown. Saat diputar, label menjadi satu-satunya tempat yang menunjukkan bahwa lebar dan tinggi sudah bertukar.
- Tombol Buka memakai tinta biasa, bukan warna aksen. Menekan tombol itu tidak lebih penting daripada menekan tombol Go di papan ketik, jadi memberinya aksen akan memindahkan fokus dari halaman web.
- Tidak ada bilah alamat bawah, tidak ada tombol berbagi, tidak ada bookmark, tidak ada tab, tidak ada menu tiga titik. Yang tidak ada di panduan tidak dibuat.
- Tidak ada bayangan. Dua garis pemisah 1dp sudah memisahkan chrome dari halaman, dan bayangan akan terlihat seperti bagian dari halaman.
- Tidak ada baris judul halaman. Panduan menyebut WebChromeClient menerima judul, tapi tidak menyediakan tempat untuk menampilkannya, dan menambah satu baris lagi berarti mengambil tinggi dari halaman web. Alamat yang sedang dibuka sudah menjawab pertanyaan yang sama.

Alasan susunan: urutan kiri ke kanan mengikuti kebiasaan browser dan arah membaca, jadi tidak perlu dipelajari; baris kedua memisahkan kontrol yang jarang dipakai dari kontrol yang dipakai setiap menit.

## 6. Motion

Hanya dua, keduanya menjawab sesuatu:

- Bilah progres terisi dari kiri selama halaman dimuat, lalu hilang saat selesai. Sifatnya determinate, bukan animasi berulang, karena angka progresnya memang datang dari WebView. Alasan: satu-satunya tanda bahwa aplikasi masih bekerja ketika halaman belum tampil.
- Perubahan status tombol (aktif/nonaktif) tanpa animasi, hanya pergantian warna.

Tidak ada animasi masuk, tidak ada transisi halaman, tidak ada efek tekan yang memantul.

## 7. State yang wajib ada

Setiap layar harus punya ini sebelum dianggap selesai (R-27):

- **Kosong (pertama buka):** kolom URL kosong, area halaman hitam sesuai letterbox, satu baris teks terang di tengah: "Ketik alamat situs di kolom atas". Teks itu hilang begitu ada halaman yang dimuat.
- **Memuat:** bilah progres berjalan, kolom URL tetap bisa dibaca.
- **Sukses:** halaman tampil, chrome menyusut ke satu toolbar.
- **Gagal:** halaman menjelaskan apa yang gagal (tidak ada koneksi, alamat tidak valid, server menolak) dan satu tombol untuk mencoba lagi.

Pesan gagal menyebut penyebab dan tindakan berikutnya, bukan "Terjadi kesalahan".

## 8. Suara teks

- Bahasa Indonesia, kalimat perintah, tanpa basa-basi.
- Label tombol menyebut aksinya: "Muat ulang", bukan "Segarkan halaman ini".
- Placeholder kolom URL: `Alamat situs`, bukan `Ketik URL di sini...`.
- Tanpa tanda pisah panjang, tanpa tanda seru, tanpa huruf kapital untuk menekankan.
- Tanpa kata "seamless", "powerful", "canggih", "revolusioner", atau padanannya dalam bahasa Indonesia.

## 9. Nilai yang dipakai

Diambil dari panduan, bukan dikarang di sini:

- Nama tampilan: Kal Browser.
- Nama paket: `com.faiz.kalbrowser`.
- Ukuran layar virtual: 1024 × 768, 1280 × 720, 1366 × 768, 1440 × 900, 1920 × 1080.
- Orientasi virtual: tombol tukar, yang menukar lebar dan tinggi tanpa memutar layar fisik.

Nama tampilan dan nama paket mengikuti nama repo Kal Browser. Panduan menetapkan nama Desktop Browser dan paket `com.faiz.desktopbrowser`, tapi juga membolehkan paket diganti bila pemilik meminta, dan pemilik meminta namanya Kal Browser.

## 10. Yang tidak dijanjikan

Aplikasi ini tidak menampilkan klaim apa pun tentang seberapa besar kemungkinan sebuah situs mendeteksi mode desktop. Panduan bagian 12 menyebut bahwa sebagian situs tetap bisa mendeteksi WebView lewat sinyal lain. Karena itu tidak ada label "anti deteksi", tidak ada persentase keberhasilan, dan tidak ada daftar situs yang "didukung". Yang bisa dibuktikan hanya perilaku aplikasi, dan itu yang ditampilkan.
