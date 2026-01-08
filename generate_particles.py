#!/usr/bin/env python3
"""
Generate custom particle textures for Demon Slayer mod
Creates 8x8 pixel particle textures for breathing effects
"""

from PIL import Image, ImageDraw
import os

OUTPUT_DIR = "src/main/resources/assets/demonslayer/textures/particle"

# Particle definitions: name -> (color_rgb, style)
PARTICLES = {
    # Breathing effects
    "water_breathing": ((64, 156, 255), "circle"),
    "flame_breathing": ((255, 100, 30), "flame"),
    "thunder_breathing": ((255, 255, 100), "spark"),
    "wind_breathing": ((200, 255, 200), "swirl"),
    "mist_breathing": ((220, 220, 240), "cloud"),
    "love_breathing": ((255, 150, 200), "heart"),
    "sun_breathing": ((255, 200, 50), "sun"),
    "beast_breathing": ((150, 100, 50), "claw"),
    
    # Demon effects
    "demon_blood": ((180, 20, 20), "drip"),
    "muzan_aura": ((100, 0, 50), "dark"),
    
    # Slayer effects
    "slayer_mark": ((100, 200, 255), "mark"),
    "hashira_aura": ((255, 215, 0), "aura"),
}

def create_circle_particle(size, color):
    """Create circular particle"""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    margin = 1
    draw.ellipse([margin, margin, size-margin-1, size-margin-1], fill=(*color, 255))
    # Add glow
    for i in range(margin):
        alpha = 100 - i * 30
        draw.ellipse([i, i, size-i-1, size-i-1], outline=(*color, alpha))
    return img

def create_flame_particle(size, color):
    """Create flame-like particle"""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Flame shape
    points = [
        (size//2, 1),
        (size-2, size//2),
        (size//2+1, size-2),
        (size//2, size-3),
        (size//2-1, size-2),
        (2, size//2),
    ]
    draw.polygon(points, fill=(*color, 255))
    # Inner highlight
    inner_color = tuple(min(255, c + 50) for c in color)
    draw.ellipse([size//4, size//4, size*3//4, size*3//4], fill=(*inner_color, 200))
    return img

def create_spark_particle(size, color):
    """Create spark/lightning particle"""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Cross shape for spark
    center = size // 2
    draw.line([(center, 0), (center, size-1)], fill=(*color, 255), width=2)
    draw.line([(0, center), (size-1, center)], fill=(*color, 255), width=2)
    # Diagonal
    draw.line([(1, 1), (size-2, size-2)], fill=(*color, 180), width=1)
    draw.line([(1, size-2), (size-2, 1)], fill=(*color, 180), width=1)
    return img

def create_swirl_particle(size, color):
    """Create swirl/wind particle"""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Arc for swirl effect
    draw.arc([1, 1, size-2, size-2], 0, 270, fill=(*color, 255), width=2)
    draw.ellipse([size//3, size//3, size*2//3, size*2//3], fill=(*color, 150))
    return img

def create_cloud_particle(size, color):
    """Create cloud/mist particle"""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Multiple overlapping circles
    draw.ellipse([0, 2, size//2, size-2], fill=(*color, 150))
    draw.ellipse([size//4, 0, size*3//4, size//2+2], fill=(*color, 180))
    draw.ellipse([size//2, 2, size-1, size-2], fill=(*color, 150))
    return img

def create_heart_particle(size, color):
    """Create heart particle"""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Simple heart shape
    draw.ellipse([0, 1, size//2, size//2+1], fill=(*color, 255))
    draw.ellipse([size//2-1, 1, size-1, size//2+1], fill=(*color, 255))
    draw.polygon([(0, size//3), (size//2, size-1), (size-1, size//3)], fill=(*color, 255))
    return img

def create_sun_particle(size, color):
    """Create sun/radiant particle"""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    center = size // 2
    # Center circle
    draw.ellipse([2, 2, size-3, size-3], fill=(*color, 255))
    # Rays
    for i in range(8):
        import math
        angle = i * math.pi / 4
        x1 = center + int(math.cos(angle) * 2)
        y1 = center + int(math.sin(angle) * 2)
        x2 = center + int(math.cos(angle) * (size//2-1))
        y2 = center + int(math.sin(angle) * (size//2-1))
        draw.line([(x1, y1), (x2, y2)], fill=(*color, 200), width=1)
    return img

def create_claw_particle(size, color):
    """Create claw/slash particle"""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Three diagonal lines
    draw.line([(1, 1), (size-2, size-2)], fill=(*color, 255), width=2)
    draw.line([(size//3, 0), (size-1, size*2//3)], fill=(*color, 220), width=1)
    draw.line([(0, size//3), (size*2//3, size-1)], fill=(*color, 220), width=1)
    return img

def create_drip_particle(size, color):
    """Create blood drip particle"""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Teardrop shape
    draw.ellipse([1, size//3, size-2, size-1], fill=(*color, 255))
    draw.polygon([(size//2, 0), (1, size//2), (size-2, size//2)], fill=(*color, 255))
    return img

def create_dark_particle(size, color):
    """Create dark/evil aura particle"""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Irregular dark shape
    draw.ellipse([1, 1, size-2, size-2], fill=(*color, 200))
    # Dark center
    draw.ellipse([2, 2, size-3, size-3], fill=(30, 0, 30, 255))
    return img

def create_mark_particle(size, color):
    """Create slayer mark particle"""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Diamond shape
    center = size // 2
    points = [(center, 0), (size-1, center), (center, size-1), (0, center)]
    draw.polygon(points, fill=(*color, 255))
    return img

def create_aura_particle(size, color):
    """Create golden aura particle"""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Glowing circle with gradient-like effect
    for i in range(size//2):
        alpha = 255 - i * 40
        if alpha < 0: alpha = 0
        draw.ellipse([i, i, size-i-1, size-i-1], outline=(*color, alpha))
    draw.ellipse([size//4, size//4, size*3//4, size*3//4], fill=(*color, 255))
    return img

# Map styles to functions
STYLE_FUNCS = {
    "circle": create_circle_particle,
    "flame": create_flame_particle,
    "spark": create_spark_particle,
    "swirl": create_swirl_particle,
    "cloud": create_cloud_particle,
    "heart": create_heart_particle,
    "sun": create_sun_particle,
    "claw": create_claw_particle,
    "drip": create_drip_particle,
    "dark": create_dark_particle,
    "mark": create_mark_particle,
    "aura": create_aura_particle,
}

def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    
    for name, (color, style) in PARTICLES.items():
        func = STYLE_FUNCS.get(style, create_circle_particle)
        
        # Create 8x8 texture
        img = func(8, color)
        filepath = os.path.join(OUTPUT_DIR, f"{name}.png")
        img.save(filepath)
        print(f"Created: {filepath}")
    
    print(f"\nGenerated {len(PARTICLES)} particle textures!")

if __name__ == "__main__":
    main()
