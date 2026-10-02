package su.nezushin.openitems.hooks.worldedit;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;
import su.nezushin.openitems.blocks.types.CustomBlockModel;
import su.nezushin.openitems.blocks.types.CustomSlabBlockModel;
import su.nezushin.openitems.blocks.types.CustomStairsBlockModel;
import su.nezushin.openitems.utils.NBTUtil;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Parses OpenItems WorldEdit block patterns:
 * <ul>
 *   <li>{@code oi:hand} / {@code oi:hand[type=top]} — main-hand item with optional host properties</li>
 *   <li>{@code oi:slot:<0-8>} / {@code oi:slot:3[facing=north,half=bottom]} — hotbar slot</li>
 *   <li>{@code oi:preset:<name>} / {@code oi:preset:<name>[type=top]} — saved runtime preset</li>
 *   <li>{@code stone_slab[type=top]} — vanilla host syntax when main hand holds a matching OpenItems slab/stair item</li>
 * </ul>
 */
public final class OpenItemsBlockParser extends InputParser<BaseBlock> {

    private static final String PREFIX = "oi:";

    private final BooleanSupplier active;
    private final WorldEditPresetStore presetStore;

    public OpenItemsBlockParser(
            WorldEdit worldEdit,
            BooleanSupplier active,
            WorldEditPresetStore presetStore
    ) {
        super(worldEdit);
        this.active = active;
        this.presetStore = presetStore;
    }

    @Override
    public BaseBlock parseFromInput(String input, ParserContext context) throws InputParseException {
        if (!active.getAsBoolean())
            return null;

        if (input.startsWith(PREFIX))
            return parseOpenItemsPattern(input, context);

        return tryParseHostItemPattern(input, context);
    }

    private BaseBlock parseOpenItemsPattern(String input, ParserContext context) throws InputParseException {
        String rest = input.substring(PREFIX.length());
        if (rest.isEmpty())
            throw parseError("Incomplete OpenItems pattern: " + input);

        ParsedPattern pattern = splitHostProperties(rest, input);
        ItemStack item = switch (getForm(pattern.base())) {
            case HAND -> resolveHand(context);
            case SLOT -> resolveSlot(context, pattern.base().substring("slot:".length()));
            case PRESET -> resolvePreset(context, pattern.base().substring("preset:".length()));
            case MODEL -> throw parseError("oi:model is not supported yet");
            case UNKNOWN -> throw parseError("Unknown OpenItems pattern: " + input);
        };

        validateCustomBlockItem(item);
        try {
            return OpenItemsWorldEditTag.tag(WorldEditHostStates.forItem(item, pattern.properties()), item);
        } catch (IllegalArgumentException e) {
            throw parseError("Invalid block properties: " + pattern.properties());
        }
    }

    /**
     * When the player holds an OpenItems slab/stair item whose material matches the vanilla host pattern,
     * treat {@code stone_slab[type=top]} like {@code oi:hand[type=top]}.
     */
    private BaseBlock tryParseHostItemPattern(String input, ParserContext context) throws InputParseException {
        BlockData hostData;
        try {
            hostData = Bukkit.createBlockData(input.toLowerCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }

        Material hostMaterial = hostData.getMaterial();
        if (!Tag.SLABS.isTagged(hostMaterial) && !Tag.STAIRS.isTagged(hostMaterial))
            return null;

        ItemStack item;
        try {
            item = resolveHand(context);
        } catch (InputParseException e) {
            return null;
        }

        if (!isSlabOrStairCustomBlock(item) || item.getType() != hostMaterial)
            return null;

        validateCustomBlockItem(item);
        try {
            return OpenItemsWorldEditTag.tag(WorldEditHostStates.fromHostBlockData(item, hostData), item);
        } catch (IllegalArgumentException e) {
            throw parseError(e.getMessage());
        }
    }

    @Override
    public Stream<String> getSuggestions(String input, ParserContext context) {
        if (!active.getAsBoolean())
            return Stream.empty();

        if (input.isEmpty())
            return Stream.of("oi:hand", "oi:slot:", "oi:preset:");

        if (input.startsWith(PREFIX)) {
            String rest = input.substring(PREFIX.length());
            int propertiesStart = rest.indexOf('[');
            if (propertiesStart >= 0) {
                String base = rest.substring(0, propertiesStart);
                String properties = rest.substring(propertiesStart + 1);
                if (properties.endsWith("]"))
                    properties = properties.substring(0, properties.length() - 1);
                return propertySuggestions(context, base, properties);
            }
            if (rest.isEmpty() || "hand".startsWith(rest))
                return Stream.of( "oi:hand", "oi:slot:", "oi:preset:");
            if ("slot:".startsWith(rest) || rest.startsWith("slot:"))
                return IntStream.range(0, 9).mapToObj(i -> "oi:slot:" + i);
            if ("preset:".startsWith(rest) || rest.startsWith("preset:"))
                return presetSuggestions(context, rest);
        }

        return Stream.empty();
    }

    private Stream<String> propertySuggestions(ParserContext context, String base, String properties) {
        ItemStack item = resolveSuggestionItem(context, base);
        if (item == null)
            return Stream.empty();

        String modelId = NBTUtil.getBlockId(item);
        CustomBlockModel model = modelId == null
                ? null
                : OpenItems.getInstance().getModelRegistry().getBlockTypes().get(modelId);
        if (model == null)
            return Stream.empty();

        BlockType type = BlockTypes.get(model.resolveHostMaterial(item).getKey().toString());
        if (type == null)
            return Stream.empty();

        return OpenItemsPropertySuggestions.suggest(PREFIX + base, type, properties);
    }

    private ItemStack resolveSuggestionItem(ParserContext context, String base) {
        try {
            ItemStack item = switch (getForm(base)) {
                case HAND -> resolveHand(context);
                case SLOT -> resolveSlot(context, base.substring("slot:".length()));
                case PRESET -> resolvePreset(context, base.substring("preset:".length()));
                default -> null;
            };
            if (item == null || item.getType().isAir() || item.getAmount() <= 0 || NBTUtil.getBlockId(item) == null)
                return null;
            return item;
        } catch (InputParseException e) {
            return null;
        }
    }

    private Stream<String> presetSuggestions(ParserContext context, String rest) {
        var actor = context.getActor();
        if (!(actor instanceof com.sk89q.worldedit.entity.Player wePlayer))
            return Stream.of("oi:preset:");

        var player = Bukkit.getPlayer(wePlayer.getUniqueId());
        if (player == null)
            return Stream.of("oi:preset:");

        String prefix = rest.startsWith("preset:") ? rest.substring("preset:".length()) : "";
        return presetStore.getNames(player.getUniqueId()).stream()
                .filter(name -> name.startsWith(prefix))
                .map(name -> "oi:preset:" + name);
    }

    private record ParsedPattern(String base, String properties) {
    }

    private static ParsedPattern splitHostProperties(String rest, String originalInput) throws InputParseException {
        int bracketStart = rest.indexOf('[');
        if (bracketStart < 0)
            return new ParsedPattern(rest, null);

        if (!rest.endsWith("]"))
            throw parseError("Unclosed block properties in pattern: " + originalInput);

        String base = rest.substring(0, bracketStart);
        String properties = rest.substring(bracketStart + 1, rest.length() - 1);
        if (base.isEmpty())
            throw parseError("Missing pattern before block properties: " + originalInput);

        return new ParsedPattern(base, properties);
    }

    private enum Form {
        HAND, SLOT, PRESET, MODEL, UNKNOWN
    }

    private static Form getForm(String base) {
        if ("hand".equals(base))
            return Form.HAND;
        if (base.startsWith("slot:"))
            return Form.SLOT;
        if (base.startsWith("preset:"))
            return Form.PRESET;
        if (base.startsWith("model:"))
            return Form.MODEL;
        return Form.UNKNOWN;
    }

    private static ItemStack resolveHand(ParserContext context) throws InputParseException {
        var player = requireBukkitPlayer(context);
        return player.getInventory().getItemInMainHand();
    }

    private static ItemStack resolveSlot(ParserContext context, String slotText) throws InputParseException {
        var player = requireBukkitPlayer(context);

        int slot;
        try {
            slot = Integer.parseInt(slotText);
        } catch (NumberFormatException e) {
            throw parseError("Invalid hotbar slot: " + slotText);
        }

        if (slot < 0 || slot > 8)
            throw parseError("Hotbar slot must be between 0 and 8");

        return player.getInventory().getItem(slot);
    }

    private ItemStack resolvePreset(ParserContext context, String name) throws InputParseException {
        if (name.isEmpty())
            throw parseError("Missing preset name");

        var player = requireBukkitPlayer(context);
        ItemStack item = presetStore.get(player.getUniqueId(), name);
        if (item == null)
            throw parseError("Unknown WorldEdit preset: " + name);
        return item;
    }

    private static Player requireBukkitPlayer(ParserContext context) throws InputParseException {
        var actor = context.getActor();
        if (!(actor instanceof com.sk89q.worldedit.entity.Player))
            throw parseError("This OpenItems pattern requires a player");

        // WorldEdit 7.4 wraps actors in PlayerProxy; BukkitAdapter.adapt(Player) expects BukkitPlayer.
        var player = Bukkit.getPlayer(actor.getUniqueId());
        if (player == null)
            throw parseError("This OpenItems pattern requires an online player");
        return player;
    }

    private static boolean isSlabOrStairCustomBlock(ItemStack item) {
        if (item == null || item.getType().isAir())
            return false;

        String id = NBTUtil.getBlockId(item);
        if (id == null)
            return false;

        CustomBlockModel model = OpenItems.getInstance().getModelRegistry().getBlockTypes().get(id);
        return model instanceof CustomSlabBlockModel || model instanceof CustomStairsBlockModel;
    }

    private static void validateCustomBlockItem(ItemStack item) throws InputParseException {
        if (item == null || item.getType().isAir())
            throw parseError("Item is empty or not an OpenItems custom block");

        String id = NBTUtil.getBlockId(item);
        if (id == null)
            throw parseError("Item is empty or not an OpenItems custom block");

        if (!OpenItems.getInstance().getModelRegistry().getBlockTypes().containsKey(id))
            throw parseError("Unknown OpenItems block model: " + id);
    }

    private static InputParseException parseError(String message) {
        return new InputParseException(TextComponent.of(message));
    }

    static BaseBlock enrichFullBlock(World world, BlockVector3 position, BaseBlock block) {
        Block bukkitBlock = world.getBlockAt(position.x(), position.y(), position.z());
        BlockLocationStore store = OpenItems.getInstance().getBlocks().getPlacedBlocks().get(bukkitBlock);
        if (store == null)
            return block;
        return OpenItemsWorldEditTag.tag(block, store.getCurrentItem());
    }
}
