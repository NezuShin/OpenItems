package su.nezushin.openitems.events;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.jetbrains.annotations.NotNull;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;

/**
 * Called when a block is destroyed by liquid.
 * This event called only if can_be_destroyed_by_liquid property set to true
 * <p>
 * You can get original BlockFromToEvent using getSource() method.
 */
public class CustomBlockDestroyedByLiquidEvent extends BlockEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private BlockLocationStore brokenBlock;

    private BlockFromToEvent source;

    private boolean cancelled = false;

    public CustomBlockDestroyedByLiquidEvent(@NotNull Block block, BlockLocationStore brokenBlock, BlockFromToEvent source) {
        super(block);
        this.brokenBlock = brokenBlock;
        this.source = source;
    }

    public BlockLocationStore getBrokenBlock() {
        return brokenBlock;
    }

    public BlockFromToEvent getSource() {
        return source;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
