# OpenItems

OpenItems is a plugin for PaperMC that allows you to add new items, blocks, and font images with the ability to edit them directly in-game with minimal config file editing.
If you have questions, want ask for a feature or report a bug - feel free to open issue, mail me at nezushin@ya.ru or dm in discord @nezushin or matrix @nezushin:matrix.nezushin.ru.

Unlike [craftengine](https://modrinth.com/plugin/craftengine), OpenItems not manipulating PaperMCs registry since there is no pre-defined blocks, everything goes in runtime. 
## Features
- Automatic resource pack generation - handheld, generated, cube_all child models, etc
- Custom blocks with [custom hardness](#understanding-block-hardness) based on note blocks, chorus plants or tripwires
- Custom armor models
- Custom font images
- Edit models of items or blocks, configure their behavior in-game

## Dependencies
- [**NBTAPI**](https://modrinth.com/plugin/nbtapi)
- [**PaperMC**](https://papermc.io/) - based server (Are someone still using spigot? why?)

## Soft Dependencies
- [**PlaceholderAPI**](https://www.spigotmc.org/resources/placeholderapi.6245/) - if you need font image placeholders

## Commands
- `/openitems` - reloading plugin and building resource pack
- `/oedit` - main command for item editing

## Suggested Setup
- [**PaperMC**](https://papermc.io/) as server core
- OpenItems - for custom items and blocks
- [**CustomRecipes**](https://www.spigotmc.org/resources/%E2%96%BA%E2%96%BA-customrecipes-1-8-x-1-21-x-advanced-recipes-made-easy-%E2%97%84%E2%97%84.36925/) - for custom crafting
- [**BetterHud**](https://www.spigotmc.org/resources/%E2%AD%90betterhud%E2%AD%90a-beautiful-hud-plugin-you-havent-seen-before%E2%9C%85auto-resource-pack-build%E2%9C%85.115559/) - for custom huds
- [**BetterModel**](https://www.spigotmc.org/resources/bettermodel-modern-blockbench-model-engine-folia-supported.121561/) - for custom mobs and player animations


## Working with plugin 

### Automatic model generator

To reduce boilerplate, plugin offers automatic model generation. \
Generator never modifies contents data. It's only making changes in build directory.
What generator do:
- Assigns id to blocks and font images - You can view or manually change ids in font-images-cache.json or block-id-cache.json but usually this is not necessary
- Performing scan for textures in `OpenItems/contents/<namespace>/textures/item/generated/` directory and automatically adds model with parent `minecraft:generated` 
to `/build/assets/<namespace>/models/item` directory. Also, its creates link to model in `/build/assets/<namespace>/items/`. 
- Doing same for `OpenItems/contents/<namespace>/textures/item/handheld/` directory but with parent `minecraft:handheld`.
- Performing scan for item and block models add adds link in `/build/assets/<namespace>/items/` directory.
- Searching for `OpenItems/contents/<namespace>/textures/entity/equipment/` directories and prepares models in `/build/assets/<namespace>/equipment/`. \
 `.../equipment/humanoid` and `.../equipment/humanoid_leggings` is most used directories (layers). Just put your armor textures with same `name.png` to these directories. 
Machine will generate `<namespace>:name` model. You can set model using command `/oedit equipment model <namespace>:name`. \
Other layers also supported: `wings`, `pig_saddle`, and any others.
- Scanning for block models and textures:
- - For note block based textures in directory `OpenItems/contents/<namespace>/textures/block/note_block`.
As note block is plane minecraft block, and it is not supports "transparency", plugin gives no option to provide custom model with this base-block type. \
There is three ways to define block: 
- - - For single texture block - (e.g. `minecraft:diamond_ore`) just put `.png` texture to `.../block/note_block` directory. \
After building resource pack in registry will appear new block model - `<namespace>:block/note_block/<your_png_file>`
- - - For block with three textures: up, down and side - put images with postfix `_up`, `_down`, `_side` to directory. For example `my_block_up.png`, `my_block_down.png`,`my_block_up.png`.   
If done correctly, registry will have model `<namespace>:block/note_block/my_block`
- - - For block with own texture in every side - same as above, but you need names ends with `_up`, `_down`, `_east`, `_west`, `_south` and `_west`.
You may put textures to every subdirectory in `.../block/note_block` but png files for one block should be in same directory.
- - Tripwire and chorus plant based models is located in `OpenItems/contents/<namespace>/models/block/tripwire` and `chorus_plant` directories respectively. 
As there is no reason to use tripwires and chorus plants as plain blocks, so only custom models supported. \
Models appears in registry as `<namespace>:block/tripwire/<model>` and `<namespace>:block/chorus_plant/<model>`. Any subdirectory is allowed. Models just being copied to build directory as is.
- - For every block type generator creates its `/build/assets/<namespace>/items/` link, so you can use command `/oedit item model <your_model_path>` to set block model to item. \
Also, it generates `minecraft:/blockstates/` for `note_block.json`, `tripwire.json` and `chorus_plant.json`
- Copies full `OpenItems/contents/<namespace>` directory to `/build/assets/<namespace>`. Generator does it as last action, so if yours files have more priority then auto-generated and replaces it on conflict. 
Even with `minecraft:/blockstates/` directory, so be careful and keep it in mind. 

### Font images and Placeholders

Use command `/oitems emoji <emoji>` to view all available font images.

Font image offset and size configuration: WIP

PAPI placeholder: WIP


### Understanding block hardness

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