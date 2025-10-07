package su.nezushin.openitems.events;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jetbrains.annotations.NotNull;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;

/**
 * Called when block is affected by explosion in world. Cancelling this event will prevent block destruction.
 * This event called only if can_be_blown property set to false
 * <p>
 * You can get original EntityExplodeEvent or BlockExplodeEvent using getSource() method.
 */
public class CustomBlockExplodeEvent extends BlockEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();

    private BlockLocationStore affectedBlock;

    private Event source;

    private boolean cancelled = false;

    public CustomBlockExplodeEvent(@NotNull Block block, BlockLocationStore affectedBlock, Event source) {
        super(block);
        this.affectedBlock = affectedBlock;
        this.source = source;
    }

    public BlockLocationStore getAffectedBlock() {
        return affectedBlock;
    }

    /**
     * @return EntityExplodeEvent or BlockExplodeEvent
     */
    public Event getSource() {
        return source;
    }

    public ExplosionEventType getSourceEventType() {
        return this.source instanceof EntityExplodeEvent ?
                ExplosionEventType.ENTITY_EXPLOSION : ExplosionEventType.BLOCK_EXPLOSION;
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


    public static enum ExplosionEventType {
        ENTITY_EXPLOSION, BLOCK_EXPLOSION
    }
}
