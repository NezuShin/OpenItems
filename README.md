# OpenItems

OpenItems is a plugin for PaperMC that allows you to add new items, blocks, and font images with the ability to edit them directly in-game with minimal config file editing.
If you have questions, want ask for a feature or report a bug - feel free to open issue, mail me at nezushin@ya.ru or dm in discord @nezushin or matrix @nezushin:matrix.nezushin.ru.

Unlike [craftengine](https://modrinth.com/plugin/craftengine), OpenItems not manipulating PaperMCs registry since there is no pre-defined blocks, everything goes in runtime. 
## Features
- [Automatic resource pack generation](#automatic-model-generator-and-content-creation)
- [Custom blocks](#blocks) with [custom hardness](#understanding-block-hardness) based on note blocks, chorus plants or tripwires
- [Custom armor models](#equipment)
- [Custom font images](#font-images-and-placeholders)
- Edit models of items or blocks, configure their behavior in-game

## Dependencies
- [**NBTAPI**](https://modrinth.com/plugin/nbtapi)
- [**PaperMC**](https://papermc.io/) - based server (Are someone still using spigot? why?)

## Soft Dependencies
- [**PlaceholderAPI**](https://www.spigotmc.org/resources/placeholderapi.6245/) - if you need font image placeholders

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
Use command `/oitems print_offset_sequence <offset in pixels>` to prepare [text offset](https://minecraft.wiki/w/Font#Space_provider) sequence.

Font images stored in directory `OpenItems/contents/<namespace>/textures/font/`.

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
```yaml
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "{path}"
  }
}
```


#### Blocks
How plugin scanning for block models and textures:
- For note block based textures in directory `OpenItems/contents/<namespace>/textures/block/note_block/`.
  As note block is plane minecraft block, and it is not supports "transparency", plugin gives no option to provide custom model with this base-block type. \
  There is three ways to define block:
- - For single texture block - (e.g. `minecraft:diamond_ore`) just put `.png` texture to `.../block/note_block/` directory. \
    After building resource pack in registry will appear new block model - `<namespace>:block/note_block/<your_png_file>`
- - For block with three textures: up, down and side - put images with postfix `_up`, `_down`, `_side` to directory. For example `my_block_up.png`, `my_block_down.png`,`my_block_up.png`.   
    If done correctly, registry will have model `<namespace>:block/note_block/my_block`
- - For block with own texture in every side - same as above, but you need names ends with `_up`, `_down`, `_east`, `_west`, `_south` and `_north`.
    You may put textures to every subdirectory in `.../block/note_block` but png files for one block should be in same directory.
- Tripwire and chorus plant based models is located in `OpenItems/contents/<namespace>/models/block/tripwire/` and `.../chorus_plant/` directories respectively.
  As there is no reason to use tripwires and chorus plants as plain blocks, so only custom models supported. \
  Models appears in registry as `<namespace>:block/tripwire/<model>` and `<namespace>:block/chorus_plant/<model>`. Any subdirectory is allowed. Models just being copied to build directory as is.
- For every block type generator creates its `/build/assets/<namespace>/items/` link, so you can use command `/oedit item model <your_model_path>` to set block model to item. \
    Also, it generates `minecraft:/blockstates/` for `note_block.json`, `tripwire.json` and `chorus_plant.json`



#### Understanding block hardness

Plugin cannot manipulate real block hardness but lets you configure player break speed multiplier. \
For example, you want block to be mined by pickaxes - you should perform next commands:
 - `/oedit block break_speed_multiplier tool pickaxe 0.6` - set multiplier 0.6 for pickaxes. Value more than 1 - faster than in vanilla tool speed,  
 - `/oedit block break_speed_multiplier apply_tool_grade_multiplier pickaxe` - set list of tools to apply [tool grade multiplier](https://minecraft.wiki/w/Breaking#Mining_efficiency), separated by space. Example with two tools: `pickaxe shovel`.
 - `/oedit block break_speed_multiplier tool axe 0.005` - note that in vanilla axe is preferred tool to break note blocks, so you need to set smaller multiplier to decrease breaking speed. 
 - `/oedit block break_speed_multiplier tool shovel 0.05` - set same multipliers for other tools and hand
 - `/oedit block break_speed_multiplier tool hand 0.05` 
 - `/oedit block break_speed_multiplier tool shears 0.05`
  

And now, according to [digging speed table](https://minecraft.wiki/w/Breaking#Mining_efficiency), breaking multipliers for tools are:

|                          | Wood  | Stone | Diamond | Netherite |
|--------------------------|-------|-------|---------|-----------|
| base tool grade modifier | 2     | 4     | 8       | 9         |
| calculations             | 0.6*2 | 0.6*4 | 0.6*8   | 0.6*9     |
| breaking multiplier      | 1.2   | 2.4   | 4.8     | 5.4       |

You also can set breaking speed using item model or [Bukkit's material](https://jd.papermc.io/paper/1.21.10/org/bukkit/Material.html):
 - `/oedit block break_speed_multiplier material stone 10`
 - `/oedit block break_speed_multiplier model ns:item/handheld/drill 100`

In these cases, tool grade modifiers cannot be applied. \
Also note that custom break speed can be set only for note blocks and chorus plant based blocks as vanilla tripwire breaks immediately.



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
- Store any arbitrary information in blocks using [`BlockLocationStore.getArbitratyData()`](http://nezushin.su/javadocs/openitems/su/nezushin/openitems/blocks/storage/BlockLocationStore.html#getArbitraryData())
- To be done

## Inspirations
- https://github.com/MMonkeyKiller/CustomBlocks
- https://github.com/Xiao-MoMi/craft-engine