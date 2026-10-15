"""Generates all pixel-art for the Green Lantern mod. Original art, drawn procedurally."""
from PIL import Image, ImageDraw, ImageFilter
import math, random, os

ROOT = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources", "assets", "greenlantern"))
TEX = f"{ROOT}/textures"

# palette
OUTLINE = (6, 26, 14, 255)
G_DARK  = (10, 96, 40, 255)
G_MID   = (28, 178, 76, 255)
G_LIGHT = (110, 245, 150, 255)
G_GLOW  = (60, 235, 120, 255)
WHITE   = (232, 255, 238, 255)
BLACK   = (22, 26, 24, 255)
BLACK_L = (46, 52, 48, 255)
METAL_D = (38, 46, 42, 255)
METAL_M = (72, 84, 78, 255)
METAL_L = (118, 132, 124, 255)

def new(w, h):
    return Image.new("RGBA", (w, h), (0, 0, 0, 0))

def rect(img, x0, y0, x1, y1, c):
    px = img.load()
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if 0 <= x < img.width and 0 <= y < img.height:
                px[x, y] = c

def outline(img, color=OUTLINE):
    """Add a 1px outline around opaque pixels (4-neighbour)."""
    px = img.load()
    w, h = img.size
    marks = []
    for y in range(h):
        for x in range(w):
            if px[x, y][3] == 0:
                for dx, dy in ((1,0),(-1,0),(0,1),(0,-1)):
                    nx, ny = x+dx, y+dy
                    if 0 <= nx < w and 0 <= ny < h and px[nx, ny][3] > 200:
                        marks.append((x, y)); break
    for x, y in marks:
        px[x, y] = color

def lerp(a, b, t):
    return tuple(int(a[i] + (b[i]-a[i]) * t) for i in range(4))

def save(img, path):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)

# ---------------------------------------------------------------- items
def ring_icon():
    img = new(16, 16); px = img.load()
    cx, cy = 7.5, 9.2
    for y in range(16):
        for x in range(16):
            dx = (x - cx) / 6.1
            dy = (y - cy) / 4.9
            r = math.hypot(dx, dy)
            if 0.60 <= r <= 1.0:
                light = -(dx * 0.6 + dy * 0.8)          # light from the top-left
                t = max(0.0, min(1.0, 0.5 + light * 0.5))
                c = lerp(G_DARK, G_LIGHT, t)
                if r > 0.90:
                    c = lerp(c, G_DARK, 0.55)
                px[x, y] = c
    # gem on top
    rect(img, 6, 2, 9, 4, G_MID)
    rect(img, 7, 2, 8, 3, WHITE)
    px[6, 2] = (0,0,0,0); px[9, 2] = (0,0,0,0)
    outline(img)
    return img

def mask_icon():
    img = new(16, 16); px = img.load()
    for y in range(16):
        for x in range(16):
            inL = ((x - 4.5) / 4.0) ** 2 + ((y - 8) / 3.3) ** 2 <= 1
            inR = ((x - 10.5) / 4.0) ** 2 + ((y - 8) / 3.3) ** 2 <= 1
            bridge = 6 <= x <= 9 and 6 <= y <= 8
            strap = (x <= 1 or x >= 14) and 7 <= y <= 8
            if inL or inR or bridge or strap:
                t = (y - 4.5) / 7.0
                px[x, y] = lerp(G_MID, G_DARK, max(0, min(1, t)))
    # lenses
    for cx in (4.5, 10.5):
        for y in range(16):
            for x in range(16):
                if ((x - cx) / 2.3) ** 2 + ((y - 8.2) / 1.5) ** 2 <= 1:
                    px[x, y] = WHITE
    outline(img)
    return img

def jacket_icon():
    img = new(16, 16)
    rect(img, 4, 3, 11, 14, G_MID)       # torso
    rect(img, 0, 3, 3, 10, G_MID)        # sleeves
    rect(img, 12, 3, 15, 10, G_MID)
    rect(img, 6, 3, 9, 4, (0, 0, 0, 0))  # neck notch
    px = img.load()
    px[5, 3] = lerp(G_MID, G_DARK, 0.2); px[10, 3] = lerp(G_MID, G_DARK, 0.2)
    # shading on the right side
    rect(img, 10, 5, 11, 14, G_DARK)
    rect(img, 2, 5, 3, 10, lerp(G_MID, G_DARK, 0.4)); rect(img, 14, 5, 15, 10, G_DARK)
    # black shoulders / sleeve bands
    rect(img, 0, 3, 3, 4, BLACK); rect(img, 12, 3, 15, 4, BLACK)
    rect(img, 4, 3, 5, 4, BLACK); rect(img, 10, 3, 11, 4, BLACK)
    # white gloves
    rect(img, 0, 9, 3, 10, WHITE); rect(img, 12, 9, 15, 10, WHITE)
    # belt
    rect(img, 4, 13, 11, 14, BLACK)
    # lantern emblem
    for p in [(7,6),(8,6),(6,7),(9,7),(6,8),(9,8),(7,9),(8,9)]:
        px[p] = WHITE
    for p in [(7,7),(8,7),(7,8),(8,8)]:
        px[p] = G_LIGHT
    outline(img)
    return img

def trousers_icon():
    img = new(16, 16)
    rect(img, 4, 2, 11, 7, BLACK)        # waist / hips
    rect(img, 4, 8, 6, 14, BLACK)        # left leg
    rect(img, 9, 8, 11, 14, BLACK)       # right leg
    rect(img, 4, 2, 11, 3, G_MID)        # green belt
    px = img.load()
    px[7, 2] = WHITE; px[8, 2] = WHITE; px[7, 3] = G_LIGHT; px[8, 3] = G_LIGHT
    rect(img, 4, 4, 4, 14, G_MID); rect(img, 11, 4, 11, 14, G_DARK)   # side stripes
    rect(img, 5, 5, 5, 13, BLACK_L); rect(img, 9, 8, 9, 13, BLACK_L)  # highlights
    outline(img)
    return img

def boots_icon():
    img = new(16, 16)
    # left boot (toe points left), right boot (toe points right)
    rect(img, 3, 4, 6, 11, BLACK); rect(img, 1, 11, 6, 13, BLACK)
    rect(img, 9, 4, 12, 11, BLACK); rect(img, 9, 11, 14, 13, BLACK)
    rect(img, 3, 4, 6, 5, G_MID); rect(img, 9, 4, 12, 5, G_MID)       # green cuffs
    rect(img, 3, 6, 3, 10, BLACK_L); rect(img, 9, 6, 9, 10, BLACK_L)   # highlights
    rect(img, 1, 13, 6, 13, (10, 12, 11, 255)); rect(img, 9, 13, 14, 13, (10, 12, 11, 255))  # soles
    outline(img)
    return img

# ---------------------------------------------------------------- blocks
def battery_side():
    img = new(16, 16)
    rect(img, 0, 0, 15, 1, METAL_M)                 # handle (y14..16)
    rect(img, 6, 0, 9, 0, METAL_L)
    rect(img, 0, 2, 15, 2, METAL_L)                 # top cap (y12..14)
    rect(img, 0, 3, 15, 3, METAL_D)
    rect(img, 0, 4, 15, 13, METAL_D)                # body frame (y2..12)
    px = img.load()
    for y in range(5, 13):                           # glowing glass
        for x in range(5, 11):
            d = math.hypot(x - 7.5, y - 8.5) / 5.0
            px[x, y] = lerp(G_LIGHT, G_DARK, min(1.0, d))
    # lantern emblem
    for p in [(7,7),(8,7),(6,8),(9,8),(6,9),(9,9),(7,10),(8,10)]:
        px[p] = WHITE
    for p in [(7,8),(8,8),(7,9),(8,9)]:
        px[p] = (200, 255, 215, 255)
    rect(img, 4, 4, 11, 4, METAL_M); rect(img, 4, 13, 11, 13, METAL_M)   # frame rims
    rect(img, 4, 5, 4, 12, METAL_M); rect(img, 11, 5, 11, 12, METAL_M)
    rect(img, 0, 14, 15, 15, METAL_M)               # base (y0..2)
    rect(img, 0, 15, 15, 15, METAL_D)
    return img

def battery_top():
    img = new(16, 16)
    rect(img, 0, 0, 15, 15, METAL_D)
    rnd = random.Random(7)
    px = img.load()
    for _ in range(40):
        x, y = rnd.randrange(16), rnd.randrange(16)
        px[x, y] = METAL_M
    rect(img, 5, 5, 10, 10, G_DARK)
    rect(img, 6, 6, 9, 9, G_GLOW)
    rect(img, 7, 7, 8, 8, G_LIGHT)
    return img

def construct(fading=False):
    img = new(16, 16); px = img.load()
    for y in range(16):
        for x in range(16):
            edge = x in (0, 15) or y in (0, 15)
            diag = (x + y) % 8 == 7
            cross = x in (7, 8) or y in (7, 8)
            if not fading:
                c = (40, 230, 100, 112)
                if diag: c = (80, 255, 140, 135)
                if cross: c = (90, 255, 150, 150)
                if edge: c = (140, 255, 185, 215)
            else:
                c = (70, 235, 120, 52)
                if diag and (x + y) % 2 == 0: c = (200, 255, 220, 100)
                if edge and (x + y) % 2 == 0: c = (210, 255, 225, 150)
                elif edge: c = (60, 220, 110, 60)
            px[x, y] = c
    return img

# ---------------------------------------------------------------- armor layers (64x32)
def armor_layers():
    # --- layer 1: helmet (mask), chestplate (jacket + sleeves), boots
    l1 = new(64, 32)
    # mask band across the eyes on all four sides of the head; head front is (8,8)-(15,15)
    band = [(0, 7), (8, 15), (16, 23), (24, 31)]
    for x0, x1 in band:
        rect(l1, x0, 11, x1, 13, (12, 74, 36, 255))
        rect(l1, x0, 11, x1, 11, (30, 150, 70, 255))
    # white lenses on the front face
    rect(l1, 9, 12, 10, 13, WHITE)
    rect(l1, 13, 12, 14, 13, WHITE)

    # body (16,16): top 20-27 y16-19, bottom 28-35 y16-19, right 16-19, front 20-27, left 28-31, back 32-39 (y20-31)
    rect(l1, 20, 16, 27, 19, BLACK); rect(l1, 28, 16, 35, 19, BLACK)
    for x0, x1 in [(16, 19), (20, 27), (28, 31), (32, 39)]:
        rect(l1, x0, 20, x1, 31, G_MID)
        rect(l1, x0, 20, x1, 21, BLACK)               # black collar / yoke
        rect(l1, x0, 30, x1, 31, BLACK)               # belt line
    rect(l1, 20, 22, 20, 29, G_DARK); rect(l1, 27, 22, 27, 29, G_DARK)   # front edge shading
    rect(l1, 35, 22, 36, 29, BLACK)                   # back seam
    # chest emblem on the front: 4x4 circle at x22..25, y23..26
    for p in [(23,23),(24,23),(22,24),(25,24),(22,25),(25,25),(23,26),(24,26)]:
        l1.putpixel(p, WHITE)
    for p in [(23,24),(24,24),(23,25),(24,25)]:
        l1.putpixel(p, G_LIGHT)

    # right arm (40,16): top 44-47 y16-19, bottom 48-51, outer 40-43, front 44-47, inner 48-51, back 52-55 (y20-31)
    rect(l1, 44, 16, 47, 19, BLACK); rect(l1, 48, 16, 51, 19, G_MID)
    for x0, x1 in [(40, 43), (44, 47), (48, 51), (52, 55)]:
        rect(l1, x0, 20, x1, 31, G_MID)
        rect(l1, x0, 20, x1, 22, BLACK)               # black shoulder band
        rect(l1, x0, 28, x1, 31, WHITE)               # white glove
        rect(l1, x0, 27, x1, 27, G_DARK)              # cuff shadow

    # boots live in the lower rows of the leg area (0,16): faces x0-3, 4-7, 8-11, 12-15 (y20-31)
    for x0, x1 in [(0, 3), (4, 7), (8, 11), (12, 15)]:
        rect(l1, x0, 27, x1, 27, G_MID)               # green cuff
        rect(l1, x0, 28, x1, 31, BLACK)
        rect(l1, x0, 31, x1, 31, (10, 12, 11, 255))   # sole
    rect(l1, 4, 16, 7, 19, (0, 0, 0, 0))

    # --- layer 2: leggings (belt on the body region + full legs)
    l2 = new(64, 32)
    for x0, x1 in [(16, 19), (20, 27), (28, 31), (32, 39)]:
        rect(l2, x0, 20, x1, 22, BLACK)               # waistband
        rect(l2, x0, 22, x1, 22, G_MID)               # green trim
    rect(l2, 23, 20, 24, 21, WHITE)                   # buckle (front)
    rect(l2, 20, 16, 27, 19, (0, 0, 0, 0))
    # legs at (0,16)
    rect(l2, 4, 16, 7, 19, BLACK); rect(l2, 8, 16, 11, 19, BLACK)
    for x0, x1 in [(0, 3), (4, 7), (8, 11), (12, 15)]:
        rect(l2, x0, 20, x1, 31, BLACK)
        rect(l2, x0, 20, x1, 21, BLACK_L)
    rect(l2, 1, 22, 2, 31, G_MID)                     # outer-side green stripes
    rect(l2, 9, 22, 10, 31, G_MID)
    rect(l2, 5, 22, 6, 23, BLACK_L); rect(l2, 13, 22, 14, 23, BLACK_L)
    return l1, l2

# ---------------------------------------------------------------- mod icon
def mod_icon(size=128):
    S = size
    bg = Image.new("RGBA", (S, S), (6, 18, 11, 255))
    d = ImageDraw.Draw(bg)
    # vignette
    for i in range(S // 2, 0, -2):
        t = i / (S / 2)
        c = lerp((5, 14, 9, 255), (16, 62, 34, 255), 1 - t)
        d.ellipse([S/2 - i, S/2 - i, S/2 + i, S/2 + i], fill=c)
    glow = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    # ring, drawn on a transparent layer then blurred for a glow
    box = [S*0.16, S*0.30, S*0.84, S*0.84]
    gd.ellipse(box, outline=(80, 255, 140, 255), width=int(S*0.11))
    glow_blur = glow.filter(ImageFilter.GaussianBlur(S * 0.05))
    bg = Image.alpha_composite(bg, glow_blur)
    ring = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    rd = ImageDraw.Draw(ring)
    rd.ellipse(box, outline=(30, 190, 85, 255), width=int(S*0.09))
    inner = [box[0] + S*0.025, box[1] + S*0.025, box[2] - S*0.025, box[3] - S*0.025]
    rd.arc(inner, 180, 300, fill=(150, 255, 190, 255), width=int(S*0.03))
    rd.ellipse([S*0.42, S*0.12, S*0.58, S*0.30], fill=(30, 190, 85, 255), outline=(6, 26, 14, 255), width=2)
    rd.ellipse([S*0.46, S*0.16, S*0.54, S*0.25], fill=(232, 255, 238, 255))
    bg = Image.alpha_composite(bg, ring)
    return bg

# ---------------------------------------------------------------- write everything
if __name__ == "__main__":
    save(ring_icon(),    f"{TEX}/item/power_ring.png")
    save(mask_icon(),    f"{TEX}/item/lantern_mask.png")
    save(jacket_icon(),  f"{TEX}/item/lantern_jacket.png")
    save(trousers_icon(),f"{TEX}/item/lantern_trousers.png")
    save(boots_icon(),   f"{TEX}/item/lantern_boots.png")
    save(battery_side(), f"{TEX}/block/power_battery_side.png")
    save(battery_top(),  f"{TEX}/block/power_battery_top.png")
    save(construct(False), f"{TEX}/block/hard_light_construct.png")
    save(construct(True),  f"{TEX}/block/hard_light_construct_fading.png")
    l1, l2 = armor_layers()
    save(l1, f"{TEX}/models/armor/lantern_layer_1.png")
    save(l2, f"{TEX}/models/armor/lantern_layer_2.png")
    save(mod_icon(128), f"{ROOT}/icon.png")
    print("textures written")
