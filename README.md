<!--suppress HtmlDeprecatedTag, XmlDeprecatedElement, HtmlDeprecatedAttribute -->
<p align="center">
<img src="https://cdn.modrinth.com/data/6KkN9umD/images/b87781a78d889924e7ac9912b0f0c577542a785e.png" width="512" alt="Tier Tower Logo"/>
</p>

<p align="center">
A data-driven cosmetic leveling system.
</p>

---

Tier Tower adds chat badge and nameplate cosmetics that players can unlock by sacrificing items.
Several new decorative blocks are also added.

- Drop items into a Vortex to sacrifice them and gain eminence. (EMI integration shows valid items.)
- Jump down into a Vortex to sacrifice your current eminence to prestige, speeding up future leveling.
- The Vortex is uncraftable by default since servers may want to restrict it to certain areas.
- The Obelisk display's offset can be set by using an Obelisk item on the top, bottom, or sides of the front face of the block.

---

![Slimeist with an echo nameplate](https://cdn.modrinth.com/data/6KkN9umD/images/64c40ba48340fcfceb2e98aa8b761c616ea0989b.png)

---

## Datapack Format

### Overview

Tier Tower is based around sequences of tiers, with progression based on custom mathematical formulae.

The sequence `tier_tower:main` is special, as players will start at tier 0 of this sequence.
Other sequences can be created and assigned to players using commands or through sequence progression.

#### Prestige System

Players acquire prestige points per-sequence, according to the `prestige_value_function` defined in the sequence JSON.
When a player prestiges, their prestige points increase by this amount.
The prestige multiplier is then recalculated using the `prestige_multiplier_function`, which affects future leveling costs.

### Sequence Format

```json5
// data/tier_tower/tier_tower/sequence/main.json
{
  // if the first tier doesn't specify a base leveling cost, this value is used instead
  "default_base_leveling_cost": 1,

  /*
  The prestige value function determines the incremental prestige points gained when prestiging.
  Available variables:
  - points: the total points in the current sequence
  - tier: the (0-based) index of the current tier
  - level: the (0-based) index of the current level within the current tier
  - level_points: the number of points within the current level
  */
  "prestige_value_function": "(points - 5000.0)",

  /*
  The prestige multiplier function determines the maximum multiplier applied during leveling.
  The actual multiplier for each item will be a random value between 1.0 and this value.
  Available variables:
  - prestige_points: the total accumulated prestige points in the current sequence
  */
  "prestige_multiplier_function": "min(8.0, (1.0 + (prestige_points / 5000.0)))",

  // the tiers that must be progressed through, in order
  "tiers": [
    "minecraft:copper",
    /* ... */
    "minecraft:netherite"
  ],

  // optional next sequence to switch to after completing this one
  "next_sequence": "tier_tower:example"
}
```

### Tier Format

```json5
// data/minecraft/tier_tower/tier/netherite.json
{
  // the number of levels in this tier that must be completed to reach the next tier
  "level_count": 10,

  /*
  The leveling cost for level index 0 in this tier.
  Available variables:
  - prev: the hypothetical leveling cost of the one beyond the highest level in the previous tier, or `default_base_leveling_cost` if this is the first tier
  */
  "base_leveling_cost": "(prev * 0.75)",
  
  /*
  The leveling cost for the remaining levels in this tier.
  Available variables:
  - levels: the value of `level_count`, provided for convenience
  - base: the computed value of `base_leveling_cost`
  - prev: the leveling cost of the previous level in this tier
  - level: the index of the level for which the cost is being computed
  */
  "leveling_cost_function": "((((5.0 * level) + 610.0) * 32.0) + base)",

  // optional: eminence points will be lost over time while in this tier (useful for making an 'infinite' highest tier)
  "erosion_rate": {
    // the number eminence points lost per interval
    "loss": 1,
    // the number of ticks between each erosion event
    "interval": 20,
  },
}
```

### Expression Language

Formulae can be written with standard mathematical operators (`+`, `-`, `*`, `/`, `^`), parentheses for grouping, and the following functions:
- `min(a, b, ...)`: returns the minimum of the arguments
- `max(a, b, ...)`: returns the maximum of the arguments
- `sqrt(x)`: returns the square root of `x`
- `log(x)`: returns the natural logarithm of `x`
- `exp(x)`: returns e raised to the power of `x`
- `e()`: returns the value of e (2.71828...)
- `pow(x, y)`: returns `x` raised to the power of `y`
- `abs(x)`: returns the absolute value of `x`
- `floor(x)`: returns the largest integer less than or equal to `x`
- `ceil(x)`: returns the smallest integer greater than or equal to `x`
- `round(x)`: returns the nearest integer to `x`
- `pi()`: returns the value of π (3.14159...)
- `sin(x)`, `cos(x)`, `tan(x)`: trigonometric functions (x in radians)

---

## Resourcepack Format

In addition to the datapack entry, several textures must be provided.
We will use the tier `minecraft:copper` for example paths.

### Badge Texture
`assets/minecraft/textures/tier_tower/badge/copper.png` provides the chat badge.
```json5
// assets/minecraft/textures/tier_tower/badge/copper.png.mcmeta
{
  "tier_tower:chat_badge": {
    // the color used for level numbering on the badge
    "text_color": {
      "r": 236,
      "g": 221,
      "b": 255
    }
  }
}
```

### Frame Texture
`assets/minecraft/textures/tier_tower/frame/copper.png` provides the nameplate frame.
It is made up of three 16x16 sections, that are aligned horizontally to the left side, center, and right side of the nameplate respectively.

### Obelisk Texture
`assets/minecraft/textures/tier_tower/obelisk/copper.png` provides the outline texture for the Obelisk.

### Tower Texture
`assets/minecraft/textures/tier_tower/tower/copper.png` provides the tower slice texture used in the Obelisk progress display.

---

## License
Tier Tower is licensed under the LGPL-3.0 license. See [LICENSE.LESSER](LICENSE.LESSER) for more details.

A modified version of the [Kiwi Soda](https://www.dafont.com/kiwisoda.font) font was used in the icon.

Sections of [Create's](https://github.com/Creators-of-Create/Create) creative tab and virtual fluid code were used under the MIT license.

### Credits
- Code by Slimeist
- Textures and design by
  - Charon
  - Luna
  - pinkmachine
  - Vivian