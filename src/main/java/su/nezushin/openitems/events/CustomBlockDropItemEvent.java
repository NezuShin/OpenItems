package su.nezushin.openitems.events;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;

/**
 * Called after CustomBlockBreakEvent if dropOnDestroy set to true in BlockLocationStore.
 * If the block break is cancelled, this event won't be called.
 */
public class CustomBlockDropItemEvent extends BlockEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private BlockLocationStore placedBlock;

    private BlockDropItemEvent source;

    private boolean cancelled = false;

    public CustomBlockDropItemEvent(@NotNull Block block, BlockLocationStore placedBlock, BlockDropItemEvent source) {
        super(block);
        this.placedBlock = placedBlock;
        this.source = source;
    }

    public BlockLocationStore getPlacedBlock() {
        return placedBlock;
    }

    public BlockDropItemEvent getSource() {
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
