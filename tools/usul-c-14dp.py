from PIL import Image, ImageDraw, ImageFont
import glob

# Dua tafsir untuk pegangan 14 dp, digambar dalam keadaan terlipat dan terbuka.
# Yang dibandingkan: apakah titik ketuknya berpindah atau tidak.
S = 2
W = 393
MARGIN = 16

REG = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
if not glob.glob(REG):
    REG = BOLD = None

paper = "#FBFBF9"; ink = "#17191C"; muted = "#5A5F66"; divider = "#D6D8DB"
signal = "#B4441F"; page = "#EDEEF0"; pita = "#FAEDE8"; kepala = "#DDE0E3"

PITA = 14
BAR = 48
HALAMAN = 56


def F(px, bold=False):
    path = BOLD if bold else REG
    return ImageFont.truetype(path, int(px * S)) if path else ImageFont.load_default()


# Tinggi satu pasang tahap: judul tahap 14 + tutup (pita+hal) + 8 + buka (pita+bar+hal)
PASANG = 14 + (PITA + HALAMAN) + 10 + (PITA + BAR + HALAMAN)
JUDUL_PANEL = 24
CATATAN = 40
JEDA = 26

PANELS = [
    ("Pegangan ikut bergerak bersama bilah", True,
     ["Pegangan menempel di bawah bilah, jadi ikut naik saat bilah menutup.",
      "Titik ketuknya berpindah 48 dp antara buka dan tutup."]),
    ("Pegangan tetap di posisi yang sama", False,
     ["Pita 14 dp menetap di tepi atas layar. Bilah meluncur di bawahnya.",
      "Titik ketuknya tidak pernah berpindah."]),
]

TOTAL = 42 + len(PANELS) * (JUDUL_PANEL + PASANG + CATATAN + JEDA)
img = Image.new("RGB", (W * S, TOTAL * S), "#FFFFFF")
d = ImageDraw.Draw(img)


def teks(x, y, s, size, fill, anchor="lm", bold=False):
    d.text((x * S, y * S), s, font=F(size, bold), fill=fill, anchor=anchor)


def kotak(x, y, w, h, fill=None, outline=None, width=1, radius=0):
    box = [(x * S), (y * S), ((x + w) * S), ((y + h) * S)]
    if radius:
        d.rounded_rectangle(box, radius=radius * S, fill=fill, outline=outline,
                            width=max(1, int(width * S)))
    else:
        d.rectangle(box, fill=fill, outline=outline, width=max(1, int(width * S)))


def panah(cx, cy, ke_atas, ukuran=5):
    tanda = -1 if ke_atas else 1
    d.line([((cx - ukuran) * S), ((cy - tanda * ukuran / 2) * S), (cx * S), ((cy + tanda * ukuran / 2) * S)],
           fill=signal, width=int(1.6 * S))
    d.line([((cx + ukuran) * S), ((cy - tanda * ukuran / 2) * S), (cx * S), ((cy + tanda * ukuran / 2) * S)],
           fill=signal, width=int(1.6 * S))


def titik_ketuk(cx, cy):
    d.ellipse([((cx - 4) * S), ((cy - 4) * S), ((cx + 4) * S), ((cy + 4) * S)],
              outline=signal, width=int(1.4 * S))


def halaman(y, judul_kepala=True):
    kotak(0, y, W, HALAMAN, fill=page)
    if judul_kepala:
        kotak(MARGIN, y + 8, W - 2 * MARGIN, 18, fill=kepala)
        teks(MARGIN + 6, y + 17, "kepala halaman situs", 8, muted)
    teks(MARGIN, y + 38, "halaman web", 8.5, muted)


teks(MARGIN, 20, "Dua tafsir pegangan 14 dp", 13, ink, bold=True)

y = 42
for judul, ikut_bergerak, catatan in PANELS:
    teks(MARGIN, y + 8, judul, 11.5, ink, bold=True)
    y += JUDUL_PANEL

    # --- terlipat ---
    teks(MARGIN, y + 7, "terlipat", 9, muted)
    y_tutup = y + 14
    kotak(0, y_tutup, W, PITA, fill=pita)
    d.line([(0, y_tutup * S), (W * S, y_tutup * S)], fill=divider, width=int(1 * S))
    d.line([(0, (y_tutup + PITA) * S), (W * S, (y_tutup + PITA) * S)], fill=divider, width=int(1 * S))
    panah(W / 2, y_tutup + PITA / 2, ke_atas=False)
    titik_ketuk(W / 2, y_tutup + PITA / 2)
    halaman(y_tutup + PITA, judul_kepala=not ikut_bergerak)
    y_buka = y_tutup + PITA + HALAMAN + 10

    # --- terbuka ---
    teks(MARGIN, y_buka + 7, "terbuka", 9, muted)
    y_bar = y_buka + 14
    if ikut_bergerak:
        # Pegangan menempel di bawah bilah: bilah di atas, pegangan ikut turun.
        kotak(0, y_bar, W, BAR, fill=paper)
        teks(MARGIN, y_bar + BAR / 2, "bilah alamat 48 dp", 9, muted)
        kotak(0, y_bar + BAR, W, PITA, fill=pita)
        panah(W / 2, y_bar + BAR + PITA / 2, ke_atas=True)
        titik_ketuk(W / 2, y_bar + BAR + PITA / 2)
        halaman(y_bar + BAR + PITA)
    else:
        # Pegangan menetap di tepi atas, bilah meluncur di bawahnya.
        kotak(0, y_bar, W, PITA, fill=pita)
        d.line([(0, (y_bar + PITA) * S), (W * S, (y_bar + PITA) * S)], fill=divider, width=int(1 * S))
        panah(W / 2, y_bar + PITA / 2, ke_atas=True)
        titik_ketuk(W / 2, y_bar + PITA / 2)
        kotak(0, y_bar + PITA, W, BAR, fill=paper)
        teks(MARGIN, y_bar + PITA + BAR / 2, "bilah alamat 48 dp", 9, muted)
        d.line([(0, (y_bar + PITA + BAR) * S), (W * S, (y_bar + PITA + BAR) * S)],
               fill=divider, width=int(1 * S))
        halaman(y_bar + PITA + BAR)

    # Garis bantu: apakah titik ketuk pindah antar keadaan
    y1 = y_tutup + PITA / 2
    y2 = (y_bar + BAR + PITA / 2) if ikut_bergerak else (y_bar + PITA / 2)
    d.line([(W - MARGIN - 40) * S, y1 * S, (W - MARGIN - 40) * S, y2 * S],
           fill=signal, width=int(1 * S))
    teks(W - MARGIN - 44, (y1 + y2) / 2, "48 dp" if ikut_bergerak else "0", 9.5, signal, anchor="rm")

    y = y_buka + 14 + PITA + BAR + HALAMAN
    for i, isi in enumerate(catatan):
        teks(MARGIN, y + 14 + i * 14, isi, 9.5, muted)
    y += CATATAN + JEDA

img.save("/home/user/DesktopBrowser/usul-c-14dp.png")
print("gambar dibuat", img.size)
