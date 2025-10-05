package su.nezushin.openitems.events;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;

/**
 * Called when player places custom block in world
 *
 * You can get original BlockPlaceEvent using getSource() method.
 *
 * Better to modify custom block after one tick after event called
 */
public class CustomBlockPlaceEvent extends BlockEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private BlockLocationStore placedBlock;

    private BlockPlaceEvent source;

    private boolean cancelled = false;

    public CustomBlockPlaceEvent(@NotNull Block block, BlockLocationStore placedBlock, BlockPlaceEvent source) {
        super(block);
        this.placedBlock = placedBlock;
        this.source = source;
    }

    public BlockLocationStore getPlacedBlock() {
        return placedBlock;
    }

    public BlockPlaceEvent getSource() {
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
