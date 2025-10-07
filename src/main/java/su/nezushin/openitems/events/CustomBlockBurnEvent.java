package su.nezushin.openitems.events;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;

/**
 * Called when a block is destroyed as a result of being burnt by fire.
 * This event called only if can_burn property set to true
 * <p>
 * You can get original BlockBurnEvent using getSource() method.
 */
public class CustomBlockBurnEvent extends BlockEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private BlockLocationStore brokenBlock;

    private BlockBurnEvent source;

    private boolean cancelled = false;

    public CustomBlockBurnEvent(@NotNull Block block, BlockLocationStore brokenBlock, BlockBurnEvent source) {
        super(block);
        this.brokenBlock = brokenBlock;
        this.source = source;
    }

    public BlockLocationStore getBrokenBlock() {
        return brokenBlock;
    }

    public BlockBurnEvent getSource() {
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
