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

package io.github.slimeistdev.tier_tower.base.data;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import io.github.fabricators_of_create.porting_lib.data.SoundDefinition;
import io.github.fabricators_of_create.porting_lib.data.SoundDefinitionsProvider;
import io.github.slimeistdev.tier_tower.TierTower;
import io.github.slimeistdev.tier_tower.registry.TierTowerSoundEvents;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

public class TierTowerSoundDefinitionsProvider extends SoundDefinitionsProvider {
    public TierTowerSoundDefinitionsProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, TierTower.MOD_ID, helper);
    }

    protected void addRedirect(Holder.Reference<SoundEvent> event, @Nullable String subtitle, ResourceLocation target) {
        addRedirect(event.value(), subtitle, target);
    }

    protected void addRedirect(SoundEvent event, @Nullable String subtitle, ResourceLocation target) {
        add(event, definition()
            .with(sound(target, SoundDefinition.SoundType.EVENT))
            .subtitle(subtitle == null ? null : "subtitles." + TierTower.MOD_ID + "." + subtitle));
    }

    private static ResourceLocation l(SoundEvent event) {
        return event.getLocation();
    }

    public static ResourceLocation l(Holder.Reference<SoundEvent> event) {
        return event.key().location();
    }

    @Override
    public void registerSounds() {
        addRedirect(TierTowerSoundEvents.EMINENT_SNAKE_AMBIENT, "eminent_snake.ambient", l(SoundEvents.PHANTOM_AMBIENT));
        addRedirect(TierTowerSoundEvents.EMINENT_SNAKE_SWOOP, "eminent_snake.swoop", l(SoundEvents.PHANTOM_SWOOP));
        addRedirect(TierTowerSoundEvents.EMINENT_SNAKE_STRIKE, "eminent_snake.strike", l(SoundEvents.PHANTOM_BITE));
        addRedirect(TierTowerSoundEvents.EMINENCE_PICKUP, "eminence.pickup", l(SoundEvents.EXPERIENCE_ORB_PICKUP));
        addRedirect(TierTowerSoundEvents.LEVEL_UP, "progression.level_up", l(SoundEvents.PLAYER_LEVELUP));
        addRedirect(TierTowerSoundEvents.TIER_UP, "progression.tier_up", l(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE));
        addRedirect(TierTowerSoundEvents.PRESTIGE_THUNDER, "progression.prestige", l(SoundEvents.LIGHTNING_BOLT_THUNDER));
        addRedirect(TierTowerSoundEvents.PRESTIGE_POWER_DOWN, null, l(SoundEvents.RESPAWN_ANCHOR_DEPLETE));
    }
}
