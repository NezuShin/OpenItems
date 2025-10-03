package su.nezushin.openitems.events;

import org.bukkit.block.Block;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;
import org.jetbrains.annotations.NotNull;
import su.nezushin.openitems.blocks.storage.BlockLocationStore;

public class CustomBlockUnloadEvent extends BlockEvent {
    private static final HandlerList handlerList = new HandlerList();

    private BlockLocationStore placedBlock;

    public CustomBlockUnloadEvent(@NotNull Block block, BlockLocationStore placedBlock) {
        super(block);
        this.placedBlock = placedBlock;
    }

    public BlockLocationStore getPlacedBlock() {
        return placedBlock;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
