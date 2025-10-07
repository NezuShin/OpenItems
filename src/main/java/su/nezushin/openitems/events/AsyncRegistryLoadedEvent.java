package su.nezushin.openitems.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when plugin has finished populating  model registry
 */
public class AsyncRegistryLoadedEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();

    public AsyncRegistryLoadedEvent() {
        super(true);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
