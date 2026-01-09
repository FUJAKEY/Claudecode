#!/usr/bin/env python3
"""
ArcaneMagic Texture Generator
Generates 16x16 pixel art textures for the mod using PIL
"""

from PIL import Image, ImageDraw, ImageFilter
import os
import math
import random

# Output directory
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEXTURES_DIR = os.path.join(BASE_DIR, "src/main/resources/assets/arcanemagic/textures")
ITEM_DIR = os.path.join(TEXTURES_DIR, "item")
BLOCK_DIR = os.path.join(TEXTURES_DIR, "block")
GUI_DIR = os.path.join(TEXTURES_DIR, "gui")

# Ensure directories exist
for d in [ITEM_DIR, BLOCK_DIR, GUI_DIR]:
    os.makedirs(d, exist_ok=True)

# Color palettes
WAND_COLORS = {
    'apprentice': [(139, 90, 43), (101, 67, 33), (180, 130, 70)],  # Brown wooden
    'adept': [(0, 128, 0), (34, 139, 34), (50, 205, 50)],  # Green nature
    'master': [(65, 105, 225), (0, 0, 205), (135, 206, 250)],  # Blue arcane
    'archmage': [(218, 165, 32), (255, 215, 0), (255, 255, 200)]  # Gold legendary
}

CRYSTAL_COLORS = [(138, 43, 226), (148, 0, 211), (186, 85, 211), (200, 150, 255)]
MANA_BLUE = [(0, 100, 255), (0, 150, 255), (100, 200, 255), (180, 220, 255)]


def create_wand_texture(tier, colors):
    """Generate a magical wand texture"""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    base, dark, bright = colors
    
    # Wand handle (diagonal line from bottom-left to top-right)
    for i in range(10):
        x = 3 + i
        y = 12 - i
        # Main handle
        col = base if i % 2 == 0 else dark
        draw.point((x, y), fill=col + (255,))
        draw.point((x, y+1), fill=dark + (255,))
    
    # Crystal tip
    tip_colors = {
        'apprentice': (200, 200, 200, 255),
        'adept': (0, 255, 128, 255),
        'master': (100, 150, 255, 255),
        'archmage': (255, 200, 50, 255)
    }
    tip_color = tip_colors.get(tier, (255, 255, 255, 255))
    
    # Draw crystal tip (top right area)
    draw.point((13, 2), fill=tip_color)
    draw.point((14, 2), fill=tip_color)
    draw.point((13, 3), fill=tip_color)
    draw.point((14, 3), fill=tip_color)
    draw.point((12, 3), fill=bright + (255,))
    
    # Add glow effect for higher tiers
    if tier in ['master', 'archmage']:
        glow_color = (*bright[:3], 100)
        draw.point((12, 2), fill=glow_color)
        draw.point((14, 4), fill=glow_color)
        draw.point((11, 3), fill=glow_color)
    
    # Archmage special effects
    if tier == 'archmage':
        for _ in range(3):
            x = random.randint(11, 15)
            y = random.randint(1, 4)
            draw.point((x, y), fill=(255, 255, 200, 150))
    
    return img


def create_crystal_texture():
    """Generate a glowing mana crystal texture"""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Crystal shape (hexagonal faceted gem)
    # Outer edges
    outline = CRYSTAL_COLORS[0]
    inner = CRYSTAL_COLORS[2]
    bright = CRYSTAL_COLORS[3]
    
    # Draw faceted crystal shape
    points = [
        (8, 1),   # top
        (12, 4),  # top right
        (12, 11), # bottom right
        (8, 14),  # bottom
        (4, 11),  # bottom left
        (4, 4),   # top left
    ]
    
    # Fill crystal body
    draw.polygon(points, fill=CRYSTAL_COLORS[1] + (255,), outline=outline + (255,))
    
    # Add bright facets
    draw.line([(8, 1), (8, 14)], fill=inner + (200,))
    draw.line([(5, 5), (11, 5)], fill=bright + (180,))
    
    # Inner glow
    for y in range(5, 11):
        for x in range(6, 11):
            if random.random() > 0.7:
                draw.point((x, y), fill=bright + (120,))
    
    # Bright highlight
    draw.point((6, 4), fill=(255, 255, 255, 255))
    draw.point((7, 3), fill=(255, 255, 255, 200))
    
    return img


def create_shard_texture():
    """Generate a small mana crystal shard"""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Small triangular shard
    points = [(8, 4), (11, 10), (5, 10)]
    draw.polygon(points, fill=CRYSTAL_COLORS[1] + (255,), outline=CRYSTAL_COLORS[0] + (255,))
    
    # Highlight
    draw.point((7, 5), fill=CRYSTAL_COLORS[3] + (255,))
    draw.point((8, 6), fill=CRYSTAL_COLORS[2] + (200,))
    
    return img


def create_core_texture(tier, color):
    """Generate wand core item texture"""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    base, dark, bright = color
    
    # Circular core with magical glow
    for y in range(4, 13):
        for x in range(4, 13):
            dx = x - 8
            dy = y - 8
            dist = math.sqrt(dx*dx + dy*dy)
            
            if dist < 4:
                if dist < 2:
                    draw.point((x, y), fill=bright + (255,))
                elif dist < 3:
                    draw.point((x, y), fill=base + (255,))
                else:
                    draw.point((x, y), fill=dark + (255,))
    
    # Central bright spot
    draw.point((7, 6), fill=(255, 255, 255, 255))
    draw.point((8, 7), fill=bright + (255,))
    
    return img


def create_tome_texture():
    """Generate spell tome texture"""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Book base
    book_color = (100, 50, 20)
    draw.rectangle([(2, 3), (13, 13)], fill=book_color + (255,))
    
    # Pages
    draw.rectangle([(4, 4), (12, 12)], fill=(240, 230, 200, 255))
    
    # Spine
    draw.line([(2, 3), (2, 13)], fill=(60, 30, 10, 255), width=1)
    
    # Magic symbol on cover (simplified)
    draw.ellipse([(6, 6), (10, 10)], outline=(200, 150, 255, 255))
    draw.point((8, 5), fill=(200, 150, 255, 255))
    draw.point((8, 11), fill=(200, 150, 255, 255))
    
    # Glow effect
    draw.point((7, 7), fill=(255, 200, 255, 150))
    
    return img


def create_block_texture(name, base_color, accent_color, has_runes=False):
    """Generate a block texture"""
    img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Fill with base stone pattern
    for y in range(16):
        for x in range(16):
            noise = random.randint(-15, 15)
            r = max(0, min(255, base_color[0] + noise))
            g = max(0, min(255, base_color[1] + noise))
            b = max(0, min(255, base_color[2] + noise))
            draw.point((x, y), fill=(r, g, b, 255))
    
    # Add cracks/texture
    for _ in range(3):
        x1 = random.randint(0, 15)
        y1 = random.randint(0, 15)
        x2 = x1 + random.randint(-3, 3)
        y2 = y1 + random.randint(-3, 3)
        draw.line([(x1, y1), (x2, y2)], 
                  fill=(base_color[0]-30, base_color[1]-30, base_color[2]-30, 255))
    
    # Add accent elements
    if has_runes:
        # Draw magical runes
        rune_positions = [(4, 4), (11, 4), (4, 11), (11, 11), (8, 8)]
        for rx, ry in rune_positions:
            draw.point((rx, ry), fill=accent_color + (255,))
            draw.point((rx+1, ry), fill=accent_color + (200,))
    else:
        # Crystal embedded in ore
        cx, cy = 8, 8
        for dy in range(-2, 3):
            for dx in range(-2, 3):
                if abs(dx) + abs(dy) < 3:
                    alpha = 255 - abs(dx)*40 - abs(dy)*40
                    draw.point((cx+dx, cy+dy), fill=accent_color + (alpha,))
    
    return img


def create_mana_bar_texture():
    """Generate mana bar GUI texture"""
    img = Image.new('RGBA', (128, 32), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Empty bar background (top row)
    draw.rectangle([(0, 0), (79, 7)], fill=(40, 40, 60, 200))
    draw.rectangle([(1, 1), (78, 6)], fill=(20, 20, 40, 255))
    
    # Filled bar (bottom row) - gradient blue
    draw.rectangle([(0, 8), (79, 15)], fill=(0, 100, 180, 255))
    for x in range(1, 79):
        brightness = int(50 + (x / 79) * 100)
        draw.line([(x, 9), (x, 14)], fill=(0, 100 + brightness//2, 180 + brightness//3, 255))
    
    # Border
    draw.rectangle([(0, 8), (79, 15)], outline=(100, 200, 255, 255))
    
    # Add shimmer effect
    for i in range(0, 80, 8):
        draw.point((i, 10), fill=(200, 255, 255, 150))
    
    return img


def main():
    print("Generating ArcaneMagic textures...")
    
    # Generate wand textures
    for tier, colors in WAND_COLORS.items():
        img = create_wand_texture(tier, colors)
        img.save(os.path.join(ITEM_DIR, f"{tier}_wand.png"))
        print(f"  Created {tier}_wand.png")
    
    # Generate core textures
    for tier, colors in WAND_COLORS.items():
        img = create_core_texture(tier, colors)
        img.save(os.path.join(ITEM_DIR, f"{tier}_core.png"))
        print(f"  Created {tier}_core.png")
    
    # Generate crystal textures
    crystal = create_crystal_texture()
    crystal.save(os.path.join(ITEM_DIR, "mana_crystal.png"))
    print("  Created mana_crystal.png")
    
    shard = create_shard_texture()
    shard.save(os.path.join(ITEM_DIR, "mana_crystal_shard.png"))
    print("  Created mana_crystal_shard.png")
    
    # Generate tome textures
    for spell in ['fireball', 'ice_shard', 'lightning', 'heal', 'teleport', 'meteor']:
        tome = create_tome_texture()
        tome.save(os.path.join(ITEM_DIR, f"tome_{spell}.png"))
        print(f"  Created tome_{spell}.png")
    
    # Generate block textures
    blocks = [
        ("mana_altar", (80, 60, 100), (180, 100, 255), True),
        ("arcane_pedestal", (60, 60, 70), (200, 180, 100), True),
        ("magic_ore", (100, 100, 100), (160, 80, 220), False),
        ("rune_stone", (70, 70, 80), (150, 200, 255), True),
    ]
    
    for name, base, accent, runes in blocks:
        block = create_block_texture(name, base, accent, runes)
        block.save(os.path.join(BLOCK_DIR, f"{name}.png"))
        print(f"  Created {name}.png")
    
    # Generate GUI textures
    mana_bar = create_mana_bar_texture()
    mana_bar.save(os.path.join(GUI_DIR, "mana_bar.png"))
    print("  Created mana_bar.png")
    
    print(f"\nAll textures generated successfully in {TEXTURES_DIR}")


if __name__ == "__main__":
    main()
