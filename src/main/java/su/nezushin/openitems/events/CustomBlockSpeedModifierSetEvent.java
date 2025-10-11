package su.nezushin.openitems.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;

/**
 * Called after calculations of custom block break speed multiplier
 */
public class CustomBlockSpeedModifierSetEvent extends BlockEvent {
    private static final HandlerList handlerList = new HandlerList();

    private BlockLocationStore placedBlock;

    private Player player;

    private ItemStack itemInHand;

    private double modifier;

    public CustomBlockSpeedModifierSetEvent(@NotNull Block block, BlockLocationStore placedBlock, Player player,
                                            ItemStack itemInHand, double modifier) {
        super(block);
        this.placedBlock = placedBlock;
        this.player = player;
        this.itemInHand = itemInHand;
        this.modifier = modifier;
    }

    public ItemStack getItemInHand() {
        return itemInHand.clone();
    }

    public double getModifier() {
        return modifier;
    }

    /**
     * Set resulting speed multiplier. Settings this value to zero will make block unbreakable.
     * @param modifier multiplier to set
     */
    public void setModifier(double modifier) {
        this.modifier = modifier;
    }

    public BlockLocationStore getPlacedBlock() {
        return placedBlock;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}
