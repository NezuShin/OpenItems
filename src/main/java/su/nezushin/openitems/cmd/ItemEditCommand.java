package su.nezushin.openitems.cmd;

import com.google.common.collect.Lists;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nezushin.openitems.utils.Message;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.blocks.storage.BlockNBTUtil;

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

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            Message.oedit_help.send(p);
            return true;
        }

        try {


            if (args[0].equalsIgnoreCase("model")) {
                item.setData(DataComponentTypes.ITEM_MODEL, Key.key(args[1]));
            } else if (args[0].equalsIgnoreCase("name")) {
                var newName = new ArrayList<>(Arrays.asList(args));
                newName.removeFirst();
                item.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize(
                        Message.translateCodes(String.join(" ", newName))));
            } else if (args[0].equalsIgnoreCase("max_damage")) {
                item.setData(DataComponentTypes.MAX_DAMAGE, parseInt(args[1]));
            } else if (args[0].equalsIgnoreCase("damage")) {
                item.setData(DataComponentTypes.DAMAGE, parseInt(args[1]));
            } else if (args[0].equalsIgnoreCase("max_stack_size")) {
                item.setData(DataComponentTypes.MAX_STACK_SIZE, parseInt(args[1]));
            } else if (args[0].equalsIgnoreCase("block")) {
                var block = BlockNBTUtil.getBlockData(item);
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("model")) {
                        item = BlockNBTUtil.setBlockId(item, args[2]);
                        block = BlockNBTUtil.getBlockData(item);
                    } else if (block != null) {
                        if (args.length > 2) {
                            if (args[1].equalsIgnoreCase("drop_on_destroy")) {
                                block.setDropOnDestroy(args[2].equalsIgnoreCase("true"));
                            } else if (args[1].equalsIgnoreCase("can_be_blown")) {
                                block.setCanBeBlown(args[2].equalsIgnoreCase("true"));
                            } else if (args[1].equalsIgnoreCase("can_burn")) {
                                block.setCanBurn(args[2].equalsIgnoreCase("true"));
                            } else if (args[1].equalsIgnoreCase("can_be_replaced")) {
                                block.setCanBeReplaced(args[2].equalsIgnoreCase("true"));
                            }
                        }
                        item = block.applyData();
                    }
                }
                if (block == null) {
                    Message.err_block_id_not_set.send(p);
                    return true;
                }

                Message.current_block_data.replace("{drop-on-destroy}", "" + block.dropOnDestroy(),
                        "{can-be-blown}", "" + block.canBeBlown(), "{can-burn}", "" + block.canBurn(),
                        "{model}", block.getId(), "{can-be-replaced}", "" + block.canBeReplaced()).send(p);
            }


            p.getInventory().setItemInMainHand(item);
        } catch (CommandException ex) {
            ex.send(p);
            return true;
        }

        return true;
    }

    public int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException ex) {
            throw new CommandException(Message.err_nan.replace("{nan}", str));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return Lists.newArrayList("name", "model", "max_damage", "max_stack_size", "block", "help")
                    .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[0])).toList();
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("model") && args.length > 1) {
                return OpenItems.getInstance().getModelRegistry().getItems()
                        .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[1])).toList();
            } else if (args[0].equalsIgnoreCase("block") && args.length > 1) {
                return Lists.newArrayList("drop_on_destroy", "can_be_blown", "can_burn", "can_be_replaced",
                                "model")
                        .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[1])).toList();
            }

            return List.of();
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("block")) {
                if (args[1].equalsIgnoreCase("model"))
                    return OpenItems.getInstance().getModelRegistry().getBlockTypes().keySet().stream()
                            .filter(i -> StringUtil.startsWithIgnoreCase(i, args[2])).toList();
                else
                    return Lists.newArrayList("true", "false").stream()
                            .filter(i -> StringUtil.startsWithIgnoreCase(i, args[2])).toList();
            }
        }
        return List.of();
    }
}
