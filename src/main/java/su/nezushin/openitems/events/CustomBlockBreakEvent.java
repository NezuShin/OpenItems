package su.nezushin.openitems.events;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;

/**
 * Called when player brakes custom block in world
 */
public class CustomBlockBreakEvent extends BlockEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private BlockLocationStore placedBlock;

    private BlockBreakEvent source;

    private boolean cancelled = false;

    public CustomBlockBreakEvent(@NotNull Block block, BlockLocationStore placedBlock, BlockBreakEvent source) {
        super(block);
        this.placedBlock = placedBlock;
        this.source = source;
    }

    public BlockLocationStore getPlacedBlock() {
        return placedBlock;
    }

    public BlockBreakEvent getSource() {
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
