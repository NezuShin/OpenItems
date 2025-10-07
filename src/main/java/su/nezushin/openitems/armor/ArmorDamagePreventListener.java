package su.nezushin.openitems.armor;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import su.nezushin.openitems.utils.NBTUtil;

import java.util.HashMap;
import java.util.Map;


/**
 * Not used now.
 * Need this to prevent custom helmet durability decrease on player fall damage
 */
public class ArmorDamagePreventListener implements Listener {

    Map<Player, EntityDamageEvent.DamageCause> lastDamage = new HashMap<>();
    Map<Player, Integer> lastDamageTick = new HashMap<>();

    @EventHandler
    public void itemDamage(PlayerItemDamageEvent e) {
        var p = e.getPlayer();
        var item = e.getItem();

        if (lastDamageTick.getOrDefault(p, 0) != p.getTicksLived()) //ensure events happen in same tick
            return;
        var cause = lastDamage.get(p);
        if (cause == null)
            return;

        if (!NBTUtil.hasIgnoreDamageCause(item, cause.name()))
            return;

        e.setCancelled(true);
    }

    @EventHandler
    public void playerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p))
            return;

        lastDamage.put(p, e.getCause());
        lastDamageTick.put(p, p.getTicksLived());
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        var p = e.getPlayer();
        lastDamage.remove(p);
        lastDamageTick.remove(p);
    }
}
