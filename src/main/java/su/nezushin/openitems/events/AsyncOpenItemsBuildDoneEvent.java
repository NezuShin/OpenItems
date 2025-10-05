package su.nezushin.openitems.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when plugin done building resource pack
 */
public class AsyncOpenItemsBuildDoneEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    public AsyncOpenItemsBuildDoneEvent(boolean async) {
        super(async);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
