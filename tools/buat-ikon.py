"""Membangun ikon Kal Browser sebagai vektor dari bentuk yang diukur pada gambar asli.

Semua angka posisi diukur dari berkas gambar yang dikirim pemilik, lalu dipetakan
ke kanvas 108 yang dipakai ikon adaptif Android. Huruf diletakkan satu per satu
karena jarak pada gambar asli tidak sama dengan jarak bawaan huruf monospace.
"""

import io
from fontTools.ttLib import TTFont
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.boundsPen import BoundsPen
from fontTools.pens.transformPen import TransformPen
from fontTools.misc.transform import Transform

FONT = TTFont("/path/ke/CourierPrime-Bold.ttf")  # https://github.com/google/fonts/tree/main/ofl/courierprime
GLIF = FONT.getGlyphSet()
CMAP = FONT.getBestCmap()
HMTX = FONT["hmtx"]

ORANYE = "#C24701"
BINGKAI = "#2B2B2D"
BADAN = "#0E0E0E"
PUTIH = "#FFFFFF"
LATAR = "#FFFFFF"

# Jendela pada kanvas 108, dijaga di dalam lingkaran aman berjari-jari 33 supaya
# sudutnya tidak terpotong topeng peluncur.
JX0, JY0, JLEBAR, JTINGGI = 28.0, 32.5, 52.0, 43.0
SUDUT_LUAR = 2.8
SUDUT_DALAM = 1.9
TEBAL_BINGKAI = 1.7
TINGGI_BAR = 5.3

# Kotak sasaran tiap huruf, relatif terhadap jendela, hasil pemindaian komponen
# putih pada gambar sumber.
SASARAN = {
    ">": (0.126, 0.366, 0.252, 0.521),
    "k": (0.412, 0.340, 0.545, 0.527),
    "a": (0.563, 0.390, 0.689, 0.531),
    "l": (0.714, 0.340, 0.825, 0.527),
    "_": (0.262, 0.552, 0.405, 0.578),
    "b": (0.412, 0.621, 0.543, 0.812),
    "r": (0.565, 0.673, 0.690, 0.810),
    "o": (0.707, 0.673, 0.833, 0.814),
}
URUTAN = (">", "k", "a", "l", "_", "b", "r", "o")


def f(nilai):
    """Angka pendek, tanpa ekor pecahan yang tidak berarti."""
    teks = f"{nilai:.2f}".rstrip("0").rstrip(".")
    return teks if teks else "0"


def kotak_bulat(x0, y0, x1, y1, r):
    """Satu kotak bersudut bulat, ditulis sebagai path tertutup."""
    return (
        f"M{f(x0 + r)},{f(y0)} H{f(x1 - r)}"
        f" A{f(r)},{f(r)} 0 0 1 {f(x1)},{f(y0 + r)}"
        f" V{f(y1 - r)}"
        f" A{f(r)},{f(r)} 0 0 1 {f(x1 - r)},{f(y1)}"
        f" H{f(x0 + r)}"
        f" A{f(r)},{f(r)} 0 0 1 {f(x0)},{f(y1 - r)}"
        f" V{f(y0 + r)}"
        f" A{f(r)},{f(r)} 0 0 1 {f(x0 + r)},{f(y0)} Z"
    )


def kotak_bulat_atas(x0, y0, x1, y1, r):
    """Kotak yang hanya sudut atasnya bulat, untuk bagian yang menempel ke bawah."""
    return (
        f"M{f(x0 + r)},{f(y0)} H{f(x1 - r)}"
        f" A{f(r)},{f(r)} 0 0 1 {f(x1)},{f(y0 + r)}"
        f" V{f(y1)} H{f(x0)} V{f(y0 + r)}"
        f" A{f(r)},{f(r)} 0 0 1 {f(x0 + r)},{f(y0)} Z"
    )


def kotak_bulat_bawah(x0, y0, x1, y1, r):
    """Kotak yang hanya sudut bawahnya bulat, untuk bagian di bawah bar."""
    return (
        f"M{f(x0)},{f(y0)} H{f(x1)} V{f(y1 - r)}"
        f" A{f(r)},{f(r)} 0 0 1 {f(x1 - r)},{f(y1)}"
        f" H{f(x0 + r)}"
        f" A{f(r)},{f(r)} 0 0 1 {f(x0)},{f(y1 - r)} Z"
    )


def kotak_glif(teks):
    """Kotak glif dalam satuan font, plus posisi awal tiap glif."""
    x = 0
    kotak = None
    posisi = []
    for huruf in teks:
        nama = CMAP[ord(huruf)]
        posisi.append((nama, x))
        pen = BoundsPen(GLIF)
        GLIF[nama].draw(pen)
        if pen.bounds:
            x0, y0, x1, y1 = pen.bounds
            if kotak is None:
                kotak = (x + x0, y0, x + x1, y1)
            else:
                kotak = (min(kotak[0], x + x0), min(kotak[1], y0),
                         max(kotak[2], x + x1), max(kotak[3], y1))
        x += HMTX[nama][0]
    return kotak, posisi


def jalur_huruf(huruf, sasaran):
    """Path sebuah huruf, dipetakan ke kotak sasaran di dalam jendela."""
    kotak, posisi = kotak_glif(huruf)
    rx0, ry0, rx1, ry1 = sasaran
    bx0, by0, bx1, by1 = kotak

    tx0 = JX0 + rx0 * JLEBAR
    tx1 = JX0 + rx1 * JLEBAR
    ty0 = JY0 + ry0 * JTINGGI
    ty1 = JY0 + ry1 * JTINGGI

    # Skala terkecil dari dua sumbu, supaya bentuk huruf tidak gepeng.
    skala = min((tx1 - tx0) / (bx1 - bx0), (ty1 - ty0) / (by1 - by0))
    lebar = (bx1 - bx0) * skala
    tinggi = (by1 - by0) * skala
    geser_x = tx0 + (tx1 - tx0 - lebar) / 2 - bx0 * skala
    geser_y = ty0 + (ty1 - ty0 - tinggi) / 2 + by1 * skala

    potongan = []
    for nama, x_awal in posisi:
        pen = SVGPathPen(GLIF, ntos=f)
        ubah = Transform(skala, 0, 0, -skala, geser_x + x_awal * skala, geser_y)
        GLIF[nama].draw(TransformPen(pen, ubah))
        perintah = pen.getCommands()
        if perintah:
            potongan.append(perintah)
    return " ".join(potongan)


def bentuk(latar=True, mono=False):
    """Daftar (warna, path) dari belakang ke depan."""
    x1, y1 = JX0 + JLEBAR, JY0 + JTINGGI
    dalam_x0, dalam_y0 = JX0 + TEBAL_BINGKAI, JY0 + TEBAL_BINGKAI
    dalam_x1, dalam_y1 = x1 - TEBAL_BINGKAI, y1 - TEBAL_BINGKAI
    bar_y = dalam_y0 + TINGGI_BAR

    luar = kotak_bulat(JX0, JY0, x1, y1, SUDUT_LUAR)
    dalam = kotak_bulat(dalam_x0, dalam_y0, dalam_x1, dalam_y1, SUDUT_DALAM)

    lapis = []
    if latar and not mono:
        lapis.append((LATAR, "M0,0 H108 V108 H0 Z"))

    if mono:
        # Ikon bertema diwarnai rata oleh sistem, jadi rongga jendela harus
        # berupa lubang sungguhan. Satu path berisi bentuk luar dan dalam
        # dengan aturan evenOdd menghasilkan cincin.
        lapis.append((PUTIH, luar + " " + dalam))
        for kunci in (">", "_"):
            lapis.append((PUTIH, jalur_huruf(kunci, SASARAN[kunci])))
        return lapis

    lapis.append((BINGKAI, luar))
    lapis.append((ORANYE, kotak_bulat_atas(dalam_x0, dalam_y0, dalam_x1, bar_y, SUDUT_DALAM)))
    lapis.append((BADAN, kotak_bulat_bawah(dalam_x0, bar_y, dalam_x1, dalam_y1, SUDUT_DALAM)))
    for kunci in URUTAN:
        lapis.append((PUTIH, jalur_huruf(kunci, SASARAN[kunci])))
    return lapis


def tulis_svg(path, lapis):
    isi = io.StringIO()
    isi.write('<?xml version="1.0" encoding="UTF-8"?>\n')
    isi.write('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" '
              'width="108" height="108">\n')
    for warna, d in lapis:
        isi.write(f'  <path fill="{warna}" fill-rule="evenodd" d="{d}"/>\n')
    isi.write('</svg>\n')
    io.open(path, "w", encoding="utf-8").write(isi.getvalue())


def tulis_vector(path, lapis, catatan):
    isi = io.StringIO()
    isi.write('<?xml version="1.0" encoding="utf-8"?>\n')
    isi.write(f"<!-- {catatan} -->\n")
    isi.write('<vector xmlns:android="http://schemas.android.com/apk/res/android"\n')
    isi.write('    android:width="108dp"\n    android:height="108dp"\n')
    isi.write('    android:viewportWidth="108"\n    android:viewportHeight="108">\n')
    for warna, d in lapis:
        isi.write('    <path\n')
        isi.write(f'        android:fillColor="{warna}"\n')
        isi.write('        android:fillType="evenOdd"\n')
        isi.write(f'        android:pathData="{d}" />\n')
    isi.write('</vector>\n')
    io.open(path, "w", encoding="utf-8").write(isi.getvalue())


RES = "app/src/main/res"

tulis_vector(
    f"{RES}/drawable/ic_launcher_foreground.xml",
    bentuk(latar=False),
    "Jendela terminal sebagai vektor, digambar dari bentuk yang diukur pada gambar "
    "sumber. Huruf diletakkan satu per satu mengikuti posisi aslinya.",
)

tulis_vector(
    f"{RES}/drawable/ic_launcher_background.xml",
    [(LATAR, "M0,0 H108 V108 H0 Z")],
    "Latar rata. Bentuknya dibawa lapisan depan, bukan gradien.",
)

tulis_vector(
    f"{RES}/drawable/ic_launcher_monochrome.xml",
    bentuk(latar=False, mono=True),
    "Versi monokrom untuk ikon bertema Android 13 ke atas. Hanya kerangka jendela "
    "dan penanda prompt, karena teks lengkap akan jadi gumpalan saat diwarnai rata.",
)

tulis_svg("ikon-kal-browser.svg", bentuk(latar=True))
print("berkas utama ditulis")


def svg_penuh(path, bulat=False):
    """Ikon versi lama: kanvas penuh tanpa topeng adaptif, jendela lebih besar."""
    global JLEBAR, JTINGGI, JX0, JY0, SUDUT_LUAR, SUDUT_DALAM, TEBAL_BINGKAI, TINGGI_BAR
    simpan = (JLEBAR, JTINGGI, JX0, JY0, SUDUT_LUAR, SUDUT_DALAM, TEBAL_BINGKAI, TINGGI_BAR)

    JLEBAR = 108 * 0.80
    JTINGGI = JLEBAR / 1.201
    JX0 = (108 - JLEBAR) / 2
    JY0 = (108 - JTINGGI) / 2
    SUDUT_LUAR = 4.2
    SUDUT_DALAM = 2.9
    TEBAL_BINGKAI = 2.6
    TINGGI_BAR = JTINGGI * 0.123

    isi = io.StringIO()
    isi.write('<?xml version="1.0" encoding="UTF-8"?>\n')
    isi.write('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" '
              'width="108" height="108">\n')
    if bulat:
        isi.write('  <path fill="#FFFFFF" d="M54,0 A54,54 0 1 1 53.99,0 Z"/>\n')
    for warna, d in bentuk(latar=not bulat):
        isi.write(f'  <path fill="{warna}" fill-rule="evenodd" d="{d}"/>\n')
    isi.write('</svg>\n')
    io.open(path, "w", encoding="utf-8").write(isi.getvalue())

    (JLEBAR, JTINGGI, JX0, JY0, SUDUT_LUAR, SUDUT_DALAM, TEBAL_BINGKAI, TINGGI_BAR) = simpan


svg_penuh("/tmp/ikon-penuh.svg")
svg_penuh("/tmp/ikon-bulat.svg", bulat=True)
print("svg versi lama ditulis")
