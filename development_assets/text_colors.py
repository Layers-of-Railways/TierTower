#  Tier Tower
#  Copyright (c) 2025 The Tier Tower Team
#
#  This program is free software: you can redistribute it and/or modify
#  it under the terms of the GNU Lesser General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
#  GNU Lesser General Public License for more details.
#
#  You should have received a copy of the GNU Lesser General Public License
#  along with this program. If not, see <https://www.gnu.org/licenses/>.

import os
import json

import numpy as np
import colour

while not os.path.exists("src/main/resources/assets"):
    os.chdir("..")
    if os.getcwd() == "/":
        print("Run this script from the project root.")
        exit(1)


def flip_brightness(l: float) -> float:
    if l < 0.5:
        l = min(l + 0.5, 1.0)
    else:
        l = max(0.0, l - 0.5)

    return 0.1 + (l * 0.8) # [0.1, 0.9] range


def calculate_text_color(namespace: str, path: str) -> tuple[int, int, int]:
    rgba = colour.io.read_image(f"src/main/resources/assets/{namespace}/textures/tier_tower/badge/{path}.png", method="Imageio")
    rgb = rgba[rgba[:, :, 3] > 0][:, :3]
    xyz = colour.sRGB_to_XYZ(rgb)
    ok_lab = colour.XYZ_to_Oklab(xyz)

    avg_ok_lab = ok_lab.mean(axis=0)
    avg_xyz = colour.Oklab_to_XYZ(avg_ok_lab)
    avg_rgb = colour.XYZ_to_sRGB(avg_xyz).clip(0, 1)

    l_, a_, b_ = avg_ok_lab
    inv_ok_lab = np.array([flip_brightness(l_), a_, b_])
    inv_xyz = colour.Oklab_to_XYZ(inv_ok_lab)
    inv_rgb = colour.XYZ_to_sRGB(inv_xyz).clip(0, 1)

    avg_swatch = colour.plotting.ColourSwatch(avg_rgb, "Average")
    inv_swatch = colour.plotting.ColourSwatch(inv_rgb, "Inverted Average")
    colour.plotting.plot_multi_colour_swatches([avg_swatch, inv_swatch], title=f"Colors for {namespace}:{path}")

    r, g, b = (inv_rgb * 255).round()
    return int(r), int(g), int(b)


def write_meta(namespace: str, path: str, text_color: tuple[int, int, int]) -> None:
    r, g, b = text_color
    data = {
        "tier_tower:chat_badge": {
            "text_color": {
                "r": r,
                "g": g,
                "b": b
            },
        },
    }
    output_path = f"src/main/resources/assets/{namespace}/textures/tier_tower/badge/{path}.png.mcmeta"
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2)

def main():
    tiers = """
    ae2:fluix
    create:brass
    create:rose_quartz
    create:zinc
    minecraft:amethyst
    minecraft:copper
    minecraft:diamond
    minecraft:echo
    minecraft:emerald
    minecraft:gold
    minecraft:iron
    minecraft:netherite
    minecraft:prismarine
    minecraft:quartz
    minecraft:redstone"""

    for tier in tiers.strip().splitlines():
        namespace, path = tier.strip().split(":")
        text_color = calculate_text_color(namespace, path)
        write_meta(namespace, path, text_color)


if __name__ == "__main__":
    main()