package su.nezushin.openitems.blocks;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import su.nezushin.openitems.OpenItems;

public class BlockBreakSpeedModifiers {

    private final NamespacedKey key = new NamespacedKey(OpenItems.getInstance(), "block_break_speed_modifier");

    public AttributeModifier createModifier(double amount) {
        return new AttributeModifier(key, amount, AttributeModifier.Operation.ADD_NUMBER);
    }

    /**
     * Sets the player's {@link Attribute#BLOCK_BREAK_SPEED} so the effective
     * multiplier equals {@code amount} (vanilla default is {@code 1}).
     */
    public void apply(Player p, double amount) {
        AttributeInstance attribute = p.getAttribute(Attribute.BLOCK_BREAK_SPEED);
        if (attribute == null) {
            return;
        }
        attribute.removeModifier(key);
        // ADD_NUMBER on a base of 1: (1 + (amount - 1)) = amount
        attribute.addTransientModifier(createModifier(amount - 1));
    }

    public void remove(Player p) {
        AttributeInstance attribute = p.getAttribute(Attribute.BLOCK_BREAK_SPEED);
        if (attribute == null) {
            return;
        }
        attribute.removeModifier(key);
    }
}
