# Audit checklist penerimaan, panduan bagian 11

Tanggal: 2026-10-29
Versi terakhir yang diperiksa: `v1.9.0`

Tiga status dipakai, dan bedanya sengaja dijaga:

- **LULUS**: sudah dibuktikan, dengan bukti yang ditulis.
- **BELUM DIUJI**: kode dan test sudah ada, tapi belum pernah dijalankan di HP. Tidak saya sebut lulus.
- **GAGAL**: dibuktikan tidak memenuhi.

## Hasil

| No | Butir | Status | Bukti |
|---|---|---|---|
| 1 | Workflow jalan pada tag `v*` dan melampirkan APK ke Release | LULUS | 10 tag rilis, semuanya sukses, unduhan publik tanpa login menjawab HTTP 200 |
| 2 | Aplikasi terbuka, address bar memuat URL dan pencarian | LULUS | Pemilik menguji pencarian tanpa titik dan hasilnya terbuka |
| 3 | Situs dengan tata letak mobile dan desktop menampilkan desktop | LULUS | `tbkholding.co.id/id` berubah ke tata letak desktop pada 1920. Laporan `whatismybrowser.com/w/BJYLG28` menyebut Chrome 156 di Windows 10 dan Hardware Type Computer, dan tangkapan layar pemilik menunjukkan tata letak desktop penuh |
| 4 | Situs pendeteksi melaporkan Windows dan Chrome, `userAgentData.mobile` false | LULUS | `kal:info` pada v1.9.1 di `whatismybrowser.com`: `platform` Win32, `titik sentuh` 0, `mobile menurut hint` false, `ontouchstart` false, `typeof ontouchstart` undefined, dan user agent Windows NT 10.0 Chrome 156 |
| 5 | Mengganti ukuran mengubah lebar CSS yang dilihat halaman | LULUS | Pemilik memastikan `kal:info` pada 1920 × 1080 melaporkan lebar dan tinggi sesuai |
| 6 | Tombol tukar menukar W dan H, rasio berubah, tanpa stretch, sisa hitam | LULUS | Pemilik memastikan `1080 × 1920` berhasil dan kotak tetap bertepi hitam |
| 7 | `100vh` sesuai tinggi virtual | LULUS | `tinggi halaman` pada `kal:info` sesuai tinggi virtual |
| 8 | Sentuhan akurat pada semua ukuran | LULUS untuk 1920 | Pemilik menguji ketukan menu pada jalur transformasi dan hasilnya tepat |
| 9 | Video HTML5 bisa diputar dan fullscreen berfungsi | LULUS | Dua tangkapan layar pemilik menunjukkan pemutar situs memenuhi layar penuh |
| 10 | Unduh dan unggah berfungsi | LULUS | Pemilik menyatakan butir 2, 3, dan 4 berhasil |
| 11 | Tombol kembali, maju, muat ulang, dan tombol kembali sistem | LULUS | Pemilik menguji mundur, maju, lalu muat ulang |
| 12 | Memutar HP tidak merusak letterbox dan tidak mereset halaman | LULUS | Pemilik memutar HP saat halaman terbuka, kotak menyesuaikan dan halaman tidak termuat ulang |
| 13 | Ukuran dan orientasi tersimpan setelah aplikasi ditutup | LULUS | Pemilik memastikan `1080 × 1920` bertahan setelah ditutup |
| 14 | Tidak ada crash saat fitur WebView tidak didukung | LULUS | Setiap `WebViewFeature` diperiksa dulu sebelum dipakai, di dua tempat |

Ringkasan akhir: 14 lulus, 0 sebagian, 0 belum diuji, 0 gagal.

## Delivery Gate

Laporan per blok, dengan bukti.

### Blok 1: Hard Gate

- **R-02 PASS:** em dash dan en dash nol di seluruh teks, komentar, dan dokumen, diperiksa dengan pencarian karakter.
- **R-03 PASS:** chrome tidak bisa meluber. Luberan horizontal yang pernah ada berasal dari batas skala Chromium dan sudah diperbaiki.
- **R-17 PASS:** tidak ada statistik di antarmuka.
- **R-18 PASS:** tidak ada testimoni.
- **R-23 PASS dengan catatan:** satu-satunya aset visual buatan sendiri adalah ikon peluncur, dan itu tercatat sebagai temuan LOW di audit anti-slop nomor 3.
- **R-24 PASS:** tidak ada tautan navigasi, karena aplikasi tidak punya menu.
- **R-25 PASS:** kontras terukur 17.00:1, 6.21:1, 8.67:1, dan 5.54:1. Semua di atas 4.5:1.
- **R-26 PASS:** semua tombol punya perilaku nyata.
- **R-27 FAIL:** tidak ada keadaan error saat halaman gagal dimuat. Tercatat sebagai temuan HIGH di audit anti-slop nomor 1.
- **R-28 PASS:** tidak ada FAQ.
- **R-32 PASS:** semua tombol punya nama aksesibilitas.
- **R-33 PASS:** tidak ada fitur yang ditambahkan lewat skrip tempelan. Script yang disuntikkan ke halaman adalah fitur inti yang diminta panduan.
- **R-34 PASS:** tidak ada tombol tema, dan kedua mode sistem sudah diperiksa.
- **R-35 PASS:** setiap versi dibangun lewat Actions dan dipasang di HP pemilik.
- **R-36 PASS:** tidak ada klaim yang tidak diukur.
- **R-37 PASS:** arah desain ada di `DESIGN.md` dan disetujui pemilik lewat urutan kerja yang disepakati.
- **R-38 PASS:** tidak ada konten karangan.
- **R-39 PASS:** kontrol memakai elemen asli, punya nama aksesibilitas, dan tidak memblokir zoom halaman.
- **R-40 PASS:** tidak ada media atau skrip pihak ketiga di aplikasi.

### Blok 2: Purpose-Gate

- **R-01, R-06, R-07, R-09, R-10, R-12, R-13, R-14, R-19, R-22 PASS:** tidak ada permukaan yang memakai teknik ini. Chrome terdiri dari satu bilah, satu baris ukuran, dan satu kotak hitam.
- **R-04 PASS:** ikon digambar sendiri dan relevan.
- **R-08 PASS:** tidak ada panah dekoratif.

### Blok 3 dan 4

Dial dan penguncian konsistensi tidak punya objek di aplikasi ini. Yang bisa diperiksa, konsistensi, sudah: satu warna aksen dipakai di satu tempat, satu keluarga tipografi, dan tidak ada dua gaya untuk hal yang sama.

## Bukti dari laporan pihak ketiga

Laporan `whatismybrowser.com/w/BJYLG28` pada 1920 × 1080 mencatat:

| Yang diperiksa | Hasil |
|---|---|
| Browser | Chrome 156 di Windows 10 |
| Hardware Type | Computer |
| Navigator Platform | Win32 |
| Maximum Touch Points | 0 |
| JavaScript dan cookie | aktif, termasuk cookie pihak ketiga |
| User agent | Windows NT 10.0, Win64, Chrome 156 |

Tiga yang pertama adalah tiga sinyal yang paling sering dipakai situs untuk memilih tata letak, dan ketiganya sudah desktop.

Laporan yang sama juga membocorkan hal yang tidak disamarkan, dan itu perlu dicatat dengan jujur: WebGL renderer `Adreno 618`, ukuran layar fisik `786 x 1746`, dan IP di Jakarta. Situs yang memeriksa GPU atau ukuran layar fisik masih bisa menyimpulkan ini perangkat Android. Menyamarkan WebGL bisa dilakukan, tapi berisiko merusak situs peta dan 3D, jadi belum dipakai.

Angka lengkap dari `kal:info` pada `1280 × 720`, situs `whatismybrowser.com`:

```
lebar halaman: 1280 px        skala tampilan: 0.307
tinggi halaman: 720 px        lebar visual: 1280 px
lebar dokumen: 1280 px        meta viewport: "width=1280" lalu "width=1280"
beda: 0 px                    elemen terlebar: tidak ada
geser horizontal: 0 px        platform: Win32
titik sentuh: 0               mobile menurut hint: false
ontouchstart: false           typeof ontouchstart: undefined
```

`beda: 0` dan `elemen terlebar: tidak ada` sekaligus membuktikan halaman ini tidak bisa digeser ke samping pada 1280.
