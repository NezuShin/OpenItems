package su.nezushin.openitems.hooks.worldedit;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.utils.NBTUtil;

import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Parses {@code oi:hand} and {@code oi:slot:<0-8>} WorldEdit block patterns.
 */
public final class OpenItemsBlockParser extends InputParser<BaseBlock> {

    private static final String PREFIX = "oi:";

    private final BooleanSupplier active;

    public OpenItemsBlockParser(WorldEdit worldEdit, BooleanSupplier active) {
        super(worldEdit);
        this.active = active;
    }

    @Override
    public BaseBlock parseFromInput(String input, ParserContext context) throws InputParseException {
        if (!active.getAsBoolean())
            return null;

        if (!input.startsWith(PREFIX))
            return null;

        String rest = input.substring(PREFIX.length());
        if (rest.isEmpty())
            throw parseError("Incomplete OpenItems pattern: " + input);

        ItemStack item = switch (getForm(rest)) {
            case HAND -> resolveHand(context);
            case SLOT -> resolveSlot(context, rest.substring("slot:".length()));
            case PRESET -> throw parseError("oi:preset is not supported yet");
            case MODEL -> throw parseError("oi:model is not supported yet");
            case UNKNOWN -> throw parseError("Unknown OpenItems pattern: " + input);
        };

        validateCustomBlockItem(item);
        return OpenItemsWorldEditTag.tag(WorldEditHostStates.forItem(item), item);
    }

    @Override
    public Stream<String> getSuggestions(String input, ParserContext context) {
        if (!active.getAsBoolean())
            return Stream.empty();

        if (input.isEmpty())
            return Stream.of("oi:hand", "oi:slot:0");

        if ("oi:".equals(input))
            return Stream.of("oi:hand", "oi:slot:0");

        if (input.startsWith(PREFIX)) {
            String rest = input.substring(PREFIX.length());
            if (rest.isEmpty() || "hand".startsWith(rest))
                return Stream.of("oi:hand");
            if ("slot:".startsWith(rest) || rest.startsWith("slot:"))
                return IntStream.range(0, 9).mapToObj(i -> "oi:slot:" + i);
        }

        return Stream.empty();
    }

    private enum Form {
        HAND, SLOT, PRESET, MODEL, UNKNOWN
    }

    private static Form getForm(String rest) {
        if ("hand".equals(rest))
            return Form.HAND;
        if (rest.startsWith("slot:"))
            return Form.SLOT;
        if (rest.startsWith("preset:"))
            return Form.PRESET;
        if (rest.startsWith("model:"))
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
}
