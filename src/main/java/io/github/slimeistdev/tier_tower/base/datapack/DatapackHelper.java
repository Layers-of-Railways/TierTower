/*
 * Tier Tower
 * Copyright (c) 2025 The Tier Tower Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.slimeistdev.tier_tower.base.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatapackHelper {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Load JSON files from a directory in the resource pack and parse them using the provided codec.
     *<p>
     *     Will warn if any file fails to parse, but will continue processing other files.
     *     Outputs the top successfully parsed resource for each location.
     *</p>
     */
    public static <T> void scanDirectory(ResourceManager resourceManager, String name, Gson gson, String descriptor, Codec<T> codec, Map<ResourceLocation, T> output) {
        scanDirectory(resourceManager, name, gson, descriptor, codec, output, LOGGER);
    }

    /**
     * Load JSON files from a directory in the resource pack and parse them using the provided codec.
     *<p>
     *     Will warn if any file fails to parse, but will continue processing other files.
     *     Outputs the top successfully parsed resource for each location.
     *</p>
     */
    public static <T> void scanDirectory(ResourceManager resourceManager, String name, Gson gson, String descriptor, Codec<T> codec, Map<ResourceLocation, T> output, Logger logger) {
        FileToIdConverter fileToIdConverter = FileToIdConverter.json(name);

        List<Pair<String, JsonElement>> elements = new ArrayList<>();

        for (Map.Entry<ResourceLocation, List<Resource>> entry : fileToIdConverter.listMatchingResourceStacks(resourceManager).entrySet()) {
            ResourceLocation fileLoc = entry.getKey();
            ResourceLocation resourceLoc = fileToIdConverter.fileToId(fileLoc);

            for (Resource resource : entry.getValue()) {
                String packId = resource.sourcePackId();
                try {
                    Reader reader = resource.openAsReader();

                    try {
                        JsonElement raw = GsonHelper.fromJson(gson, reader, JsonElement.class);
                        elements.add(new Pair<>(packId, raw));
                    } catch (Throwable parseError) {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (Throwable closeError) {
                                parseError.addSuppressed(closeError);
                            }
                        }

                        throw parseError;
                    }

                    if (reader != null) {
                        reader.close();
                    }
                } catch (IllegalArgumentException | IOException | JsonParseException e) {
                    logger.error("Couldn't parse data file {} from {} in {}", resourceLoc, fileLoc, packId, e);
                }
            }

            for (int i = elements.size() - 1; i >= 0; i--) {
                var element = elements.get(i);
                String packId = element.getFirst();
                JsonElement raw = element.getSecond();

                try {
                    T parsed = codec.parse(JsonOps.INSTANCE, raw).getOrThrow(false, logger::error);
                    output.put(resourceLoc, parsed);
                    break;
                } catch (Exception e) {
                    logger.error("Couldn't read {} {} from {} in data pack {}", descriptor, resourceLoc, fileLoc, packId, e);
                }
            }

            elements.clear();
        }
    }
}
