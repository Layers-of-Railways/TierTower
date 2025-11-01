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

package io.github.slimeistdev.tier_tower.base.data.lang;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import io.github.slimeistdev.tier_tower.registry.TierTowerSequences;
import io.github.slimeistdev.tier_tower.registry.TierTowerTiers;
import io.github.slimeistdev.tier_tower.utils.FilesHelper;

import java.util.Map;
import java.util.function.BiConsumer;

public class LangGen {
    public static void generate(RegistrateLangProvider provider) {
        BiConsumer<String, String> langConsumer = provider::add;

        provideDefaultLang("interface", langConsumer);
        provideDefaultLang("commands", langConsumer);
        TierTowerTiers.provideLang(langConsumer);
        TierTowerSequences.provideLang(langConsumer);
        provider.add("tier_tower.special.badge", "!"); // just to have a fallback
    }

    private static void provideDefaultLang(String fileName, BiConsumer<String, String> consumer) {
        String path = "assets/tier_tower/lang/default/" + fileName + ".json";
        JsonElement jsonElement = FilesHelper.loadJsonResource(path);
        if (jsonElement == null) {
            throw new IllegalStateException(String.format("Could not find default lang file: %s", path));
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue().getAsString();
            consumer.accept(key, value);
        }
    }
}
