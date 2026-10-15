# Green Lantern (Fabric, Minecraft 1.21.1)

A Green Lantern mod inspired by HBO's *Lanterns*: Hal Jordan, the veteran test pilot, and John Stewart,
the rookie architect. Hal's half of the ring is flight and raw force. John's half is hard-light construction.
There's also a detective mode, because the show is a murder mystery.

> Fan project. "Green Lantern" and related names are DC trademarks. All art here is original,
> procedurally generated pixel art, and the Lantern oath text is deliberately not included.

## What's in it

| Thing | What it does |
|---|---|
| **Power Ring** | The main item. Eight modes, a charge meter, and a HUD readout while held. |
| **Power Battery** | Lantern-shaped block. Right-click it to fully recharge every ring in your inventory. |
| **Hard-Light Construct** | The temporary green block the ring creates. Solid, glowing, flickers for 3 s, then vanishes (30 s total). No item form. |
| **Lantern suit** (mask, jacket, trousers, boots) | Cheap wool armour with a **set bonus**: casts cost 25% less, passive recharge is 3x faster, flight drains half as fast. |

### Ring controls
- **Right-click** - use the selected mode
- **Sneak + right-click** - cycle to the next mode
- **Right-click a Power Battery** - recharge (sneak while clicking to *not* do this)
- The ring trickles back +1 charge every 3 s while it's in your inventory (every 1 s in the full suit)

### Modes (charge cost)
| Mode | Cost | Effect |
|---|---|---|
| Hard-Light Bridge | 10 | 3-wide, 16-long road in the direction you face. Look up/down more than 15 degrees and it ramps. |
| Hard-Light Wall | 12 | 5x4 barrier three blocks ahead. |
| Hard-Light Shelter | 20 | Hollow 5x5x5 bunker around you with a doorway on the side you face. |
| Energy Bolt | 6 | Hitscan beam, 48 blocks, 10 damage + knockback. |
| Ring Shield | 12 | Resistance II + Absorption III for 12 s. |
| Restoration | 15 | Heals 4 hearts, restores a little hunger, strips harmful potion effects. |
| Ring Scan | 8 | Everything alive within 24 blocks glows for 10 s. |
| Flight | 0 up front | Toggles flight. Double-tap jump to take off. Drains 1 charge / 2 s while actually flying; out of charge = soft landing (Slow Falling). |

Constructs only replace air and soft blocks like grass and snow - never solid blocks or fluids.
Creative-mode players cast for free.

### Recipes
- **Power Ring** - 4 gold ingots (corners), 4 emeralds (edges), 1 diamond (centre)
- **Power Battery** - 6 iron ingots (top and bottom rows), 2 emeralds, 1 lantern (middle)
- **Mask / Jacket / Trousers / Boots** - leather-armour shapes using green wool (and black wool for trim)

## Building

Requirements: **JDK 21**, internet access (Gradle downloads Minecraft + mappings on the first build).

This zip does not include the Gradle wrapper binaries. Do one of:
1. `gradle wrapper --gradle-version 8.10.2` (if you have Gradle installed), or
2. copy `gradlew`, `gradlew.bat` and `gradle/wrapper/gradle-wrapper.jar` from the official
   [Fabric example mod](https://github.com/FabricMC/fabric-example-mod) (1.21.1 branch), or
3. open the folder in IntelliJ IDEA - it will import the Gradle project.

Then:

```
./gradlew build          # jar ends up in build/libs/greenlantern-1.0.0.jar
./gradlew runClient      # launch a dev client to test
```

### No local setup? Build it on GitHub
Push this folder to a new GitHub repository (the web uploader works). The included workflow
(`.github/workflows/build.yml`) builds automatically. Open the **Actions** tab, click the latest run, and download
the **greenlantern-jar** artifact. Use `greenlantern-1.0.0.jar`, not the `-sources` one. If the build fails,
the log shows the exact compile error.

To play: install Fabric Loader 0.16+ and **Fabric API for 1.21.1**, drop the jar in `mods/`.
In creative, open the **Green Lantern** tab.

## Tuning

| Want to change | Where |
|---|---|
| Max charge, bolt damage/range, bridge length, scan radius, recharge speed | top of `item/PowerRingItem.java` |
| Mode costs | `item/RingMode.java` |
| How long constructs last | `ConstructBlock.LIFETIME_TICKS` / `FADE_TICKS` in `block/ConstructBlock.java` |
| Flight speed and drain | `item/RingFlight.java` |
| Suit stats | `registry/ModArmorMaterials.java` |
| Recipes | `src/main/resources/data/greenlantern/recipe/` (plain JSON; a datapack can override them) |
| Text | `src/main/resources/assets/greenlantern/lang/en_us.json` |
| Art | everything is under `assets/greenlantern/textures/`; `tools/make_textures.py` regenerated the originals |

## Honest status

This was written without a Minecraft toolchain available, so it has been syntax-checked but **not compiled
against the real 1.21.1 sources or play-tested**. The code sticks to Yarn `1.21.1+build.3` names, but if the
compiler objects, these are the spots most likely to need a one-line tweak:

- `item/RingFx.java` - `DustParticleEffect` constructor (swap in `ParticleTypes.HAPPY_VILLAGER` if needed)
- `registry/ModArmorMaterials.java` - `ArmorMaterial` record argument order / equip-sound type
- `block/ConstructBlock.java` - `TransparentBlock` as a base class (can extend `Block` instead)
- `client/RingHud.java` - `HudRenderCallback` signature (the HUD is optional; remove `RingHud.register()` to disable)

Mapping names drift slightly between Yarn builds. `fabric_version` / `loader_version` live in `gradle.properties`.
