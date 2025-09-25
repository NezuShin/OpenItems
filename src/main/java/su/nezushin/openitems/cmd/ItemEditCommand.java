package su.nezushin.openitems.cmd;

import com.google.common.collect.Lists;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nezushin.openitems.Message;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.BlockNBTUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemEditCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {


        if (!(sender.hasPermission("nezu.openitems.oedit"))) {
            Message.err_u_dont_have_permission.send(sender);
            return true;
        }


        if (!(sender instanceof Player p)) {
            Message.err_player_only.send(sender);
            return true;
        }

        var item = p.getInventory().getItemInMainHand();

        if (item == null || item.getType().isAir()) {
            Message.err_u_should_have_item_in_hand.send(p);
            return true;
        }


        if (args[0].equalsIgnoreCase("model")) {
            item.setData(DataComponentTypes.ITEM_MODEL, Key.key(args[1]));
        } else if (args[0].equalsIgnoreCase("name")) {
            var newName = new ArrayList<>(Arrays.asList(args));
            newName.removeFirst();
            item.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize(Message.translateCodes(String.join(" ", newName))));
        } else if (args[0].equalsIgnoreCase("max_damage")) {
            item.setData(DataComponentTypes.MAX_DAMAGE, Integer.parseInt(args[1]));
        } else if (args[0].equalsIgnoreCase("damage")) {
            item.setData(DataComponentTypes.DAMAGE, Integer.parseInt(args[1]));
        } else if (args[0].equalsIgnoreCase("max_stack_size")) {
            item.setData(DataComponentTypes.MAX_STACK_SIZE, Integer.parseInt(args[1]));
        } else if (args[0].equalsIgnoreCase("block")) {
            item = BlockNBTUtil.setBlockId(item, args[1]);
        }


        p.getInventory().setItemInMainHand(item);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return Lists.newArrayList("name", "model", "max_damage", "max_stack_size", "block").stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[0])).toList();
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("model") && args.length > 1) {
                return OpenItems.getInstance().getModelRegistry().getItems().stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[1])).toList();
            }

            return List.of();
        }
        return List.of();
    }
}
