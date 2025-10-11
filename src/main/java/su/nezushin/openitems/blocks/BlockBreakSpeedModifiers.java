package su.nezushin.openitems.blocks;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import su.nezushin.openitems.OpenItems;

public class BlockBreakSpeedModifiers {

    private final NamespacedKey key = new NamespacedKey(OpenItems.getInstance(), "block_break_speed_modifier");

    public AttributeModifier createModifier(double amount) {
        return new AttributeModifier(key, amount, AttributeModifier.Operation.ADD_NUMBER);
    }


    public void apply(Player p, double amount) {
        var attribute = p.getAttribute(Attribute.BLOCK_BREAK_SPEED);
        if (attribute.getModifier(key) == null)
            attribute.addTransientModifier(createModifier(amount - 1));
    }

    public void remove(Player p) {
        p.getAttribute(Attribute.BLOCK_BREAK_SPEED).removeModifier(key);
    }


}
