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
import json
import os
from typing import Literal
import zipfile

PATH_BASE = os.path.dirname(os.path.abspath(__file__))
PATH_EXPORT_BASE = os.path.join(PATH_BASE, "scratch")

class Exporter:
    def __init__(self, pack_name: str, pack_version: int, pack_description: str, locations: list[str], pack_type: Literal["assets", "data"]):
        self.path_export = os.path.join(PATH_EXPORT_BASE, pack_name + ".zip")
        self.pack_version = pack_version
        self.pack_description = pack_description
        self.locations = locations
        self.pack_type = pack_type
        self.path_gen_vals = os.path.join(PATH_BASE, "..", "src", "generated", "resources", pack_type)
        self.path_main_vals = os.path.join(PATH_BASE, "..", "src", "main", "resources", pack_type)

    def resource_exists(self, namespace: str, path: str) -> bool:
        return (os.path.exists(os.path.join(self.path_gen_vals, namespace, path)) or
                os.path.exists(os.path.join(self.path_main_vals, namespace, path)))

    def discover_paths(self) -> list[str]:
        discovered_paths = []

        mods = list(
            set(filter(lambda p: os.path.isdir(os.path.join(self.path_gen_vals, p)), os.listdir(self.path_gen_vals))) |
            set(filter(lambda p: os.path.isdir(os.path.join(self.path_main_vals, p)), os.listdir(self.path_main_vals)))
        )

        for path_pattern in self.locations:
            if ":" not in path_pattern:
                raise ValueError(f"Invalid path pattern: {path_pattern}")

            mod, relative_path = path_pattern.split(":", 1)
            if mod == "*":
                for mod_name in mods:
                    if self.resource_exists(mod_name, relative_path):
                        discovered_paths.append(f"{mod_name}:{relative_path}")
            elif mod in mods:
                if self.resource_exists(mod, relative_path):
                    discovered_paths.append(f"{mod}:{relative_path}")
            else:
                print(f"Warning: Mod '{mod}' not found in generated data.")

        return discovered_paths

    def export(self):
        paths = self.discover_paths()
        with zipfile.ZipFile(self.path_export, "w", zipfile.ZIP_DEFLATED) as zip:
            zip.writestr("pack.mcmeta", json.dumps({
                "pack": {
                    "pack_format": self.pack_version,
                    "description": self.pack_description
                }
            }))

            for path in paths:
                mod, relative_path = path.split(":", 1)
                source_dirs = [
                    os.path.join(self.path_gen_vals, mod, relative_path),
                    os.path.join(self.path_main_vals, mod, relative_path)
                ]

                for source_dir in source_dirs:
                    if os.path.exists(source_dir):
                        for root, dirs, files in os.walk(source_dir):
                            relative_root = os.path.relpath(root, source_dir)
                            for file in files:
                                source_file = os.path.join(root, file)
                                arcname = os.path.join(self.pack_type, mod, relative_path, relative_root, file)
                                zip.write(str(source_file), arcname)

if __name__ == "__main__":
    Exporter(
        pack_name="builtin_tier_data",
        pack_version=15,
        pack_description="Built-in Tier Tower tier data",
        locations=["*:tier_tower/tier", "tier_tower:tier_tower/sequence"],
        pack_type="data"
    ).export()
    print("Default datapack exported successfully.")

    Exporter(
        pack_name="builtin_tier_resources",
        pack_version=15,
        pack_description="Built-in Tier Tower tier assets",
        locations=["*:textures/tier_tower"],
        pack_type="assets"
    ).export()
    print("Default resourcepack exported successfully.")