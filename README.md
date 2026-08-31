# OpenItems

OpenItems is a plugin for PaperMC that allows you to add new items, blocks, and font images with the ability to edit them directly in-game with minimal config file editing.

Unlike [craftengine](https://modrinth.com/plugin/craftengine), OpenItems not manipulating PaperMCs registry since there is no pre-defined blocks, everything goes in runtime. 
## Features
- [Automatic resource pack generation](#automatic-model-generator-and-content-creation)
- [Custom blocks](#blocks) with [custom hardness](#understanding-block-hardness) — note blocks, chorus plants, tripwires, [stairs](#stairs-itemdisplay), and [slabs](#slabs-itemdisplay)
- [WorldEdit / FAWE](#worldedit-support) — `//set`, clipboard, presets (`oi:hand`, `oi:preset`, …)
- [Custom armor models](#equipment)
- [Custom font images](#font-images-and-placeholders)
- Edit models of items or blocks, configure their behavior in-game

## Dependencies
- [**NBTAPI**](https://modrinth.com/plugin/nbtapi)
- [**PaperMC**](https://papermc.io/) - based server (Are someone still using spigot? why?)

## Soft Dependencies
- [**PlaceholderAPI**](https://www.spigotmc.org/resources/placeholderapi.6245/) - font image placeholders and text offsets 
- [**WorldEdit**](https://enginehub.org/worldedit/) / [**FastAsyncWorldEdit**](https://intellectualsites.com/fastasyncworldedit/) - bulk edits with custom blocks ([details](#worldedit-support))

## Working with plugin 

### Commands
- `/openitems` - reloading plugin and building resource pack
- `/oedit` - main command for item editing

### Suggested Setup
- [**PaperMC**](https://papermc.io/) as server core
- [**OpenItems**](https://github.com/NezuShin/OpenItems) - for custom items and blocks
- [**CustomRecipes**](https://www.spigotmc.org/resources/%E2%96%BA%E2%96%BA-customrecipes-1-8-x-1-21-x-advanced-recipes-made-easy-%E2%97%84%E2%97%84.36925/) - for custom crafting
- [**BetterHud**](https://www.spigotmc.org/resources/%E2%AD%90betterhud%E2%AD%90a-beautiful-hud-plugin-you-havent-seen-before%E2%9C%85auto-resource-pack-build%E2%9C%85.115559/) - for custom huds
- [**BetterModel**](https://www.spigotmc.org/resources/bettermodel-modern-blockbench-model-engine-folia-supported.121561/) - for custom mobs and player animations

### Automatic model generator and content creation

To reduce boilerplate, plugin offers automatic model generation. \
Generator never modifies contents data. It's only making changes in build directory. 

Generator also copies full `OpenItems/contents/<namespace>/` directory to `/build/assets/<namespace>/`. It as last action, so if yours files have more priority then auto-generated and replaces it on conflict. 
Even with `minecraft/blockstates/` directory, so be careful and keep it in mind. 

#### Ids for blocks and font images

Ids stored in `OpenItems/font-images-cache.json` and `OpenItems/block-id-cache.json`. 
Plugin automatically assigns id for new blocks and font images.  
You may edit these files manually if you want to reassign id of deleted block model or font image.
No need to edit fields other than `noteblockIds`, `tripwireIds`, `chorusIds` and `charIds`. Other ones will be replaced by generator.


#### Handheld and generated textures
Plugin can automatically create models with `"partent": "handheld"` and `"parent:"generated"` and links to them.

You just need to put your texture to one of two directories:
- `OpenItems/contents/<namespace>/textures/item/generated/`
- `OpenItems/contents/<namespace>/textures/item/handheld/`

When building a resource pack, models appear in the plugin's registry and can be set to item using command `/oedit item model <namespace>:<path>`

#### Equipment
Directory scan format is `OpenItems/contents/<namespace>/textures/entity/equipment/<layer>/<model_name>`

`<layer>` may be `wolf_body`, `horse_body`, `llama_body`, `humanoid`, `humanoid_leggings`, `wings`, `pig_saddle`,
`strider_saddle`, `camel_saddle`, `horse_saddle`, `donkey_saddle`, `mule_saddle`, `skeleton_horse_saddle`, `zombie_horse_saddle`,
`happy_ghast_body`.

To create regular armor model, just put your armor textures with same `name.png` to `.../equipment/humanoid` and `.../equipment/humanoid_leggings`.
Plugin will generate `<namespace>:name` model. You can set model using command `/oedit equipment model <namespace>:name`.

#### Font images and Placeholders

Use command `/oitems font print_image <emoji>` to available font images. \
Use command `/oitems font print_path <path>` to available paths to font images. \
Use command `/oitems font print_offset_sequence <offset in pixels>` to prepare [text offset](https://minecraft.wiki/w/Font#Space_provider) sequence.

Font images stored in directory `OpenItems/contents/<namespace>/textures/font/`.

**Named fonts (default):** first-level folders under `textures/font/` become font names (written to `assets/<namespace>/font/<name>.json`). Loose PNGs directly under `textures/font/` belong to the `default` font.

**Legacy mode:** set `legacy-mode: true` to merge every PNG under `textures/font/` into `minecraft:default` and `minecraft:uniform` (previous behavior).

```yaml
fonts:
  # Default is false. Set true to keep the old “everything in vanilla default/uniform” behavior
  legacy-mode: false
  settings:
    default:
      # Also inject providers into these fonts (chat uses minecraft:default / minecraft:uniform)
      merge-into: [minecraft:default, minecraft:uniform]
      append-negative-spaces: true
    hud:
      merge-into: []
      append-negative-spaces: true
```

Missing `settings` for a font: `default` merges into both vanilla fonts with spaces; any other name merges nowhere and still gets spaces.
Custom fonts (e.g. `hud`) need the text component `font` set to `<namespace>:hud` (or use the font placeholder below). Merging into `minecraft:default` / `minecraft:uniform` is what makes glyphs work in normal chat without setting a custom font.

There is two ways to configure font image size:
- Using file name. For example, file with name `my_awesome_texture_h20_a8.png` will have height 20 and ascent 8. Texture in registry will have name `<namespace>:font/my_awesome_texture`.
- Using yaml config files in `OpenItems/contents/<namespace>/configs/` directory. Example with same values as above: 
```yaml  
 font-images:
  random-useless-unique-name:
    path: '<namespace>:font/my_awesome_texture'
    height: 20
    ascent: 8
```
Unicode symbol assigns automatically, but you can [manually change it](#ids-for-blocks-and-font-images) at any time.
Plugin uses [Unicode private area](https://en.wikipedia.org/wiki/Private_Use_Areas) range (U+E000-U+F8FF).

PAPI placeholders:
 - Font image: `%openitems_emoji_<namespace>:<path>%` \
    Example: `%openitems_emoji_my_awesome_namespace:font/my_awesome_texture%`.
 - Font id for a glyph: `%openitems_font_<namespace>:<path>%` \
    Example: `%openitems_font_my_awesome_namespace:font/hud/icon%` → `my_awesome_namespace:hud`.
 - Text offset: `%openitems_offset_<offset_in_pixels>%` \
   Examples: `%openitems_offset_-10%`, `%openitems_offset_+10%`.

#### Custom model templates

You can drop your model to directory `OpenItems/contents/<namespace>/model_templates/` and specify in config 
(in the `OpenItems/contents/<namespace>/configs/` directory) where to use it as template. Model should have `{path}` 
placeholder where the texture path should be placed.

\
Example config:
```yaml
model-templates:
  random-useless-unique-name: 
    path: 'item/my_textures_with_custom_template_model/'
    # This string is a prefix ^^^
    # item/my_textures_with_custom_template_model/my_pic.png will pass the filter
    # item/my_textures_with_custom_template_model/another_dir/my_pic.png will also pass the filter
    # item/my_textures_with_another_template_model/my_pic.png will not pass the filter
    template: 'model_in_model_templates_dir'
```

Example model:
```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "{path}"
  }
}
```

#### Custom item templates

You can drop your [item model link](https://minecraft.wiki/w/Items_model_definition)
to directory `OpenItems/contents/<namespace>/item_templates/` and specify in config
(in the `OpenItems/contents/<namespace>/configs/` directory) where to use it as template. Model should have `{path}`
placeholder where the model path should be placed.

\
Example config:
```yaml
item-templates:
  oversized_in_gui:
    path: 'item/menus'
    # This string is a prefix ^^^. Works as described above
    template: 'oversized_in_gui.json'
  another_item_model:
    path: 'item/generated/textures_with_custom_link'
    template: 'oversized_in_gui.json'
```

Example model:
```json
{
  "model": {
    "type": "model",
    "model": "{path}"
  },
  "oversized_in_gui": true
}

```

#### Blocks

The plugin scans content directories and registers block models automatically. Set the item look with `/oedit item model <your_model_path>`, then apply block behaviour with `/oedit block model <your_model_path>` so the item places as a custom block.

For note-block, tripwire, and chorus hosts the generator also writes `minecraft/blockstates/` (`note_block.json`, `tripwire.json`, `chorus_plant.json`). Stairs and slabs use a different approach (see below).

##### Note blocks

Textures: `OpenItems/contents/<namespace>/textures/block/note_block/`

Note blocks are opaque cubes — custom JSON models are not supported for this host. Define a block with textures only:

| Layout | Files | Registry id |
|--------|-------|-------------|
| Single texture | `my_block.png` | `<namespace>:block/note_block/my_block` |
| Top / bottom / side | `my_block_up.png`, `my_block_down.png`, `my_block_side.png` | `<namespace>:block/note_block/my_block` |
| Per face | `my_block_up.png`, `_down`, `_east`, `_west`, `_south`, `_north` | `<namespace>:block/note_block/my_block` |

Subdirectories under `note_block/` are allowed; all faces of one block must sit in the same folder.

##### Tripwire and chorus plant

Models: `OpenItems/contents/<namespace>/models/block/tripwire/` and `.../models/block/chorus_plant/`

Only custom models are supported (copied as-is into the build). Registry ids:

- `<namespace>:block/tripwire/<model>`
- `<namespace>:block/chorus_plant/<model>`

Subdirectories are allowed.

##### Stairs (ItemDisplay)

Textures: `OpenItems/contents/<namespace>/textures/block/item_display/stairs/`

Stairs are **not** multiplexed through note-block / tripwire / chorus blockstates. The host is a real vanilla stairs block (collision, facing, half, and corner shape stay vanilla). The custom look is an `ItemDisplay` entity synced to that host.

| Layout | Files | Registry id |
|--------|-------|-------------|
| Single texture | `my_stairs.png` | `<namespace>:block/item_display/stairs/my_stairs` |
| Bottom / side / top | `my_stairs_bottom.png`, `my_stairs_side.png`, `my_stairs_top.png` | `<namespace>:block/item_display/stairs/my_stairs` |

The generator builds three shape models (`straight`, `inner`, `outer`) plus an inventory item model. Facing and half are applied as `ItemDisplay` rotation at runtime — you do not author every BlockData variant by hand.

**Placement:** use a vanilla stairs item as the base (e.g. `birch_stairs`, `stone_stairs`), then set `/oedit item model` and `/oedit block model` to `<namespace>:block/item_display/stairs/my_stairs`. The item’s material becomes the host block type; OpenItems only overlays the display and stores metadata.

Stairs do not consume note-block / tripwire / chorus blockstate IDs.

##### Slabs (ItemDisplay + note-block double)

Textures: `OpenItems/contents/<namespace>/textures/block/item_display/slabs/`

Half slabs use a vanilla slab host plus an `ItemDisplay` (`bottom` / `top`). When two matching halves merge into a double, the host becomes a **note block** and uses a normal note-block model (correct block lighting).

| Layout | Files | Registry id (half) | Double note-block model |
|--------|-------|--------------------|-------------------------|
| Single texture | `my_slab.png` | `<namespace>:block/item_display/slabs/my_slab` | `<namespace>:block/note_block/double_slabs/my_slab` |
| Top / bottom / side | `my_slab_up.png`, `_down`, `_side` | same | same double path |
| Per face | `_up`, `_down`, `_east`, `_west`, `_south`, `_north` | same | same double path |

The generator builds ItemDisplay models for `bottom` and `top`, an inventory item model, and a note-block cube model under `models/block/note_block/double_slabs/`. Doubles consume a note-block blockstate ID.

**Placement:** use a vanilla slab item as the base (e.g. `birch_slab`), then set `/oedit item model` and `/oedit block model` to `<namespace>:block/item_display/slabs/my_slab`.

**Merging:** two half slabs may form a double only when both are the same custom id and the same host material (`itemToDrop` type). On a valid merge the host becomes a note block and the placed-block registry entry switches to `<namespace>:block/note_block/double_slabs/...` (`CustomNoteblockModel`). The drop item stays the original slab item.

#### Understanding block hardness

Plugin cannot change real block hardness (note-block / chorus hosts stay those materials; stairs and half slabs stay their vanilla hosts; merged double slabs become note blocks). It sets a player `block_break_speed` attribute so mining *feels* like a chosen hardness.

Typical setup (stone-like ore mined with pickaxes):

```text
/oedit block hardness 1.5
/oedit block preferred_tool pickaxe
```

Preferred tools get vanilla tool grade and Efficiency (wood/stone/iron/… as on a matching vanilla block). Other tools mine at hand speed for that hardness. Same hardness feels the same on note-block and chorus hosts — the host’s own axe preference is cancelled in the attribute math.

- `/oedit block hardness <value>` — felt hardness (e.g. `1.5` like stone, `50` like obsidian). `clear` / `none` removes it.
- `/oedit block preferred_tool [type] [type…]` — tools that get grade (e.g. `pickaxe`, or `pickaxe shovel`). No args clears the list.

Overrides (flat, no grade) still work and win over hardness when the held item matches:

```text
/oedit block break_speed_multiplier material stone 10
/oedit block break_speed_multiplier model ns:item/handheld/drill 100
```

If hardness is **unset**, mining uses vanilla host speed unless a material/model override matches. 

Custom break speed only works for note-block and chorus-based blocks; vanilla tripwire breaks instantly.

## WorldEdit support

OpenItems can integrate with [WorldEdit](https://enginehub.org/worldedit/) and [FastAsyncWorldEdit](https://intellectualsites.com/fastasyncworldedit/). OpenItems loads the hook automatically when present.

### Why it works this way

OpenItems does not register new block types in Minecraft’s global block registry (unlike CraftEngine) and does not assign one block state to one specific configured block (like Nexo or IA). Custom blocks are configured at runtime: a vanilla host block (note block, tripwire, chorus, stairs, slab, …) plus OpenItems metadata in a per-chunk runtime registry. There is no `minecraft:my_custom_ore` id WorldEdit could target natively.

Because of that:

- WorldEdit only ever sees vanilla host block states unless OpenItems injects its own payload.
- Every custom placement must update the runtime registry, apply the model (and sometimes spawn an `ItemDisplay`), and may save chunk data — work that normal `//set stone` does not do.
- Large edits such as `//set oi:hand` over a big selection are much slower than filling with a vanilla block and can stress the server on huge regions (especially display stairs/slabs).


Also, because of [lack of WorldEdit's api](https://github.com/EngineHub/WorldEdit/pull/2544), there is no normal way to 
make snapshot reads (`//copy`, schematic saves). To achieve this functionality, OI inject its own code to WorldEdit 
(when `worldedit.enable-extended-support` is enabled). This may break on any version change, so integration has been split into `basic-support` (to correctly `//set` custom blocks, allow to use `//set oi:hand`) and `extended-support` (with clipboard support. Loading/saving schematics). 

FAWE use is highly recommbended because it does not have these problems and does not need additional code injections. 

If both FAWE and extended support are enabled, extended mode is disabled automatically. With FAWE installed but `enable-fawe: false`, only basic support runs unless you enable extended (not recommended alongside FAWE).

### Block patterns (selectors)

Patterns are used as the replacement argument (e.g. `//set <pattern>`). They require a player context for hand/slot/preset forms.

| Pattern | Meaning |
|---------|---------|
| `oi:hand` | Main-hand item if it is an OpenItems custom block |
| `oi:hand[type=top]` | Same, with host `BlockData` properties (slabs/stairs facing, half, …) |
| `oi:slot:0` … `oi:slot:8` | Hotbar slot `0`–`8` |
| `oi:preset:<name>` | Saved preset (see below) |
| `stone_slab[type=top]` | If main hand holds a matching OpenItems slab/stair **item material**, same as `oi:hand[type=top]` |


**Presets** are per-player, in-memory templates for WorldEdit (lost on restart/reload):

```text
/oi we preset save <name>    # save main-hand custom block item
/oi we preset list
/oi we preset delete <name>
//set oi:preset:<name>
```

### Commands — what works

**Works well (basic tier):**

- `//set oi:hand`, `//set oi:slot:3`, `//set oi:preset:foo` — places custom blocks; registry + model updated
- `//set air`, `//set stone`, brushes, `//replace <vanilla_host> …` when overwriting custom blocks — OpenItems removes stale metadata/display (no extra drops)
- `//replace note_block air` — matches all note blocks in the selection (both vanilla and OpenItems), OI's metadata will be cleaned gracefully anyway.

**Needs extended or FAWE tier:**

- `//copy`, `//paste`, `//cut`, `//schem save` / `//schem load` — custom id and item payload must be captured on read and restored on paste

**Works with caveats:**

- `//replace oi:hand oi:slot:2` — the replacement (`oi:slot:2`) works; the filter (`oi:hand`) usually does not match blocks already in the world. You cannot separate “custom” vs “vanilla” on the same host. There is no `//replace` mask by custom id yet.
- `//replace oi:hand …` as “only my custom blocks” — **not supported**
- Large `//set oi:…` regions — correct but slow;

## Plugin API

Javadocs available [**here**](http://nezushin.su/javadocs/openitems/).

Currently, installation via a Maven repository is not available. To use this library, download the JAR file from the releases page or build it yourself using Gradle. Then add it as a local dependency by including the following code snippet in your `build.gradle` file:

```groovy
dependencies {
    compileOnly(files('C:/your-dev-server/plugins/OpenItems.jar'))
}
```

### Resource pack related events:
- AsyncBuildDoneEvent - when done building resource pack
- AsyncRegistryLoadedEvent - Called when plugin has finished populating internal model registry and other plugins may access it

### Custom block related events:
- CustomBlockBreakEvent - Called when player brakes custom block in world
- CustomBlockBurnEvent - Called when a block is destroyed as a result of being burnt by fire
- CustomBlockDropItemEvent - Called after CustomBlockBreakEvent if dropOnDestroy set to true in BlockLocationStore
- CustomBlockExplodeEvent - Called when block is affected by explosion in world
- CustomBlockLoadEvent -  Called for every custom block when loading chunk
- CustomBlockUnloadEvent - Called for every custom block when loading chunk
- CustomBlockPlaceEvent - Called when player places custom block in world
- CustomBlockSpeedModifierSetEvent - Called after calculations of custom block break speed multiplier

### Useful API Features

Access the API via `OpenItems.getInstance()`. Wait for `AsyncRegistryLoadedEvent` before reading the model registry (font images, block models).

#### 1. Place a custom block

```java
ItemStack item = /* item with openitems_custom_block NBT */;
Block block = player.getTargetBlockExact(5);

BlockLocationStore placed = OpenItems.getInstance().getBlocks().placeBlock(block, item);
// placed is also available later via getBlocks().getPlacedBlocks().get(block)
```

#### 2. Remove a custom block

```java
CustomBlocks blocks = OpenItems.getInstance().getBlocks();

// Loaded chunk: dropItem / setAir control drop and whether the world block becomes air
blocks.destroyBlock(block, true, true);

// Unloaded chunk (or unknown): destroys when the chunk loads
blocks.destroyBlockOnLoad(block, false, true, () -> {
    // runs after destroy
});
```

#### 3. Store per-block extras

Plugin-owned data that does not belong in item NBT lives in **extras** — one map for all value types (primitives, collections, nested maps/lists, and Bukkit `ConfigurationSerializable` types such as `ItemStack` or `Location`). After changing extras, save the chunk.

Use namespaced keys (e.g. `myplugin:counter`) to avoid collisions between addons.

```java
BlockLocationStore store = OpenItems.getInstance().getBlocks().getPlacedBlocks().get(block);
if (store == null) return;

store.putExtra("myplugin:owner", player.getUniqueId().toString());
store.putExtra("myplugin:energy", 42);
store.putExtra("myplugin:reward", new ItemStack(Material.DIAMOND, 3));
store.putExtra("myplugin:home", player.getLocation());

OpenItems.getInstance().getBlocks().saveChunk(block.getChunk());

// Read back
String owner = store.getExtra("myplugin:owner", String.class);
ItemStack reward = store.getExtra("myplugin:reward", ItemStack.class);

// Or use the live map
store.getExtras().put("myplugin:flags", List.of("a", "b"));
store.removeExtra("myplugin:energy");
```

Extras are persisted in chunk data separately from the item palette (optional per block). They are not written into `openitems_custom_block` item NBT.

#### 4. Change block model (including host type)

`changeBlockModel` updates the stored id, applies the new blockstate, and saves the chunk. The new path can be a different host type (chorus → note block, tripwire → chorus, etc.) as long as that model exists in the registry.

```java
CustomBlocks blocks = OpenItems.getInstance().getBlocks();

// Same host type
blocks.changeBlockModel(block, "myns:block/chorus_plant/ore_stage_2");
 
// Switch host type, e.g. chorus plant → note block
blocks.changeBlockModel(block, "myns:block/note_block/deep_slate_variant");
```

Check available models with `OpenItems.getInstance().getModelRegistry().getBlockTypes()`.

#### 5. Font images and text offsets

```java
ModelRegistry registry = OpenItems.getInstance().getModelRegistry();

// Glyph character for a font image id (same keys as %openitems_emoji_<id>%)
String glyph = registry.getFontImages().get("myns:font/my_awesome_texture");

// Font id for that glyph (e.g. myns:hud or minecraft:default)
String fontId = registry.getFontImageFonts().get("myns:font/hud/icon");

// Build a space/offset sequence (positive = right, negative = left), same as %openitems_offset_<n>%
String moveRight = Utils.getOffset(10);
String moveLeft = Utils.getOffset(-10);

// Or compose manually from registry spaces (keys are pixel sizes, values are the space glyphs)
Map<Integer, String> spaces = registry.getFontSpaces();
String plus8 = spaces.get(8);   // +8px
String minus4 = spaces.get(-4); // -4px
```

## Inspirations
- https://github.com/MMonkeyKiller/CustomBlocks
- https://github.com/Xiao-MoMi/craft-engine