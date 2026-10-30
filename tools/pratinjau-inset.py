from PIL import Image, ImageDraw, ImageFont
import glob

# Membandingkan keadaan sebelum dan sesudah perbaikan 1.10.7, pada layar
# mendatar dengan lubang kamera di kiri. Skala dp sebenarnya.
S = 2
W, H = 400, 200

REG = "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
BOLD = "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf"
if not glob.glob(REG):
    REG = BOLD = None

paper = "#FBFBF9"; ink = "#17191C"; muted = "#5A5F66"; divider = "#D6D8DB"
hitam = "#000000"; halaman = "#2F3B33"; sinyal = "#B4441F"; kamera = "#6E767E"

PITA = 14
BAR = 48
KAMERA_LEBAR = 37      # area aman lubang kamera di sisi kiri saat mendatar
NAV = 24               # bilah navigasi di sisi kanan

TINGGI_BLOK = PITA + 100 + NAV + 40
TOTAL = 40 + 2 * TINGGI_BLOK
img = Image.new("RGB", (W * S, TOTAL * S), "#FFFFFF")
d = ImageDraw.Draw(img)


def F(px, bold=False):
    path = BOLD if bold else REG
    return ImageFont.truetype(path, int(px * S)) if path else ImageFont.load_default()


def teks(x, y, s, size, fill, anchor="lm", bold=False):
    d.text((x * S, y * S), s, font=F(size, bold), fill=fill, anchor=anchor)


def kotak(x, y, w, h, fill=None, outline=None, width=1, radius=0):
    box = [(x * S), (y * S), ((x + w) * S), ((y + h) * S)]
    if radius:
        d.rounded_rectangle(box, radius=radius * S, fill=fill, outline=outline,
                            width=max(1, int(width * S)))
    else:
        d.rectangle(box, fill=fill, outline=outline, width=max(1, int(width * S)))


def panah(cx, cy, ke_atas=True):
    u = 5
    tanda = -1 if ke_atas else 1
    d.line([((cx - u) * S), ((cy - tanda * u / 2) * S), (cx * S), ((cy + tanda * u / 2) * S)],
           fill=muted, width=int(1.6 * S))
    d.line([((cx + u) * S), ((cy - tanda * u / 2) * S), (cx * S), ((cy + tanda * u / 2) * S)],
           fill=muted, width=int(1.6 * S))


def layar(y, sebelum):
    if sebelum:
        # 1.10.6: pita menumpang, padding diwarnai kertas, halaman mulai dari 0.
        kotak(0, y, W, PITA, fill=paper)
        d.line([(0, y * S), (W * S, y * S)], fill=divider, width=int(1 * S))
        panah(W / 2, y + PITA / 2, ke_atas=False)
        kotak(0, y + PITA, W, 100, fill=halaman)
        # halaman ikut tergambar di bawah pita, jadi kepala situsnya hilang
        teks(W / 2, y + PITA / 2 + 1, "", 8, hitam)
        kotak(0, y, KAMERA_LEBAR, PITA + 100 + NAV, fill=paper)
        kotak(0, y + PITA + 100, W, NAV, fill=paper)
        kotak(0, y, KAMERA_LEBAR, 10, fill=kamera)
        teks(KAMERA_LEBAR + 6, y + 4, "pita menutupi kepala situs", 7.5, muted)
        teks(KAMERA_LEBAR + 6, y + PITA + 12, "kepala situs tertutup", 8.5, "#F1F3F4")
        teks(KAMERA_LEBAR + 6, y + PITA + 30, "halaman", 8.5, "#F1F3F4")
        teks(6, y + PITA + 60, "putih", 8, hitam)
        teks(6, y + PITA + 100 + 8, "putih", 8, hitam)
    else:
        # 1.10.7: pita ikut tata letak, inset memakai wadah hitam.
        kotak(0, y, KAMERA_LEBAR, BAR + PITA + 100 + NAV, fill=hitam)
        kotak(W - NAV, y, NAV, BAR + PITA + 100 + NAV, fill=hitam)
        kotak(KAMERA_LEBAR, y, W - KAMERA_LEBAR - NAV, PITA, fill=paper)
        d.line([(KAMERA_LEBAR, y * S), ((W - NAV) * S, y * S)], fill=divider, width=int(1 * S))
        d.line([(KAMERA_LEBAR, (y + PITA) * S), ((W - NAV) * S, (y + PITA) * S)],
               fill=divider, width=int(1 * S))
        panah(KAMERA_LEBAR + (W - KAMERA_LEBAR - NAV) / 2, y + PITA / 2, ke_atas=False)
        kotak(KAMERA_LEBAR, y + PITA, W - KAMERA_LEBAR - NAV, 100, fill=halaman)
        teks(KAMERA_LEBAR + 6, y + PITA + 12, "kepala situs utuh", 8.5, "#F1F3F4")
        teks(KAMERA_LEBAR + 6, y + PITA + 30, "halaman", 8.5, "#F1F3F4")
        kotak(KAMERA_LEBAR, y + PITA + 100, W - KAMERA_LEBAR - NAV, NAV, fill=hitam)
        kotak(0, y, KAMERA_LEBAR, 10, fill=kamera)
    # Lubang kamera
    kotak(8, y + BAR + PITA + 40, 16, 16, fill=kamera, radius=8)


teks(16, 20, "Mendatar, bilah terlipat: sebelum dan sesudah 1.10.7", 12, ink, bold=True)

y = 40
teks(16, y + 8, "1.10.6: pita menumpang, inset diwarnai kertas", 11, ink, bold=True)
layar(y + 22, sebelum=True)
y += TINGGI_BLOK

teks(16, y + 8, "1.10.7: pita di tata letak, inset di wadah hitam", 11, ink, bold=True)
layar(y + 22, sebelum=False)

img.save("/home/user/DesktopBrowser/pratinjau-inset.png")
print("gambar dibuat", img.size)
