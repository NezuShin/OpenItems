package su.nezushin.openitems.cmd;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Equippable;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import io.papermc.paper.datacomponent.item.UseCooldown;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nezushin.openitems.blocks.ToolItemType;
import su.nezushin.openitems.utils.Message;
import su.nezushin.openitems.OpenItems;
import su.nezushin.openitems.utils.NBTUtil;
import su.nezushin.openitems.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class OEditCommand implements CommandExecutor, TabCompleter {
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

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            if (args.length > 1) {
                if (args[1].equalsIgnoreCase("item")) {
                    Message.oedit_help_item.send(p);
                    return true;
                } else if (args[1].equalsIgnoreCase("block")) {
                    Message.oedit_help_block.send(p);
                    return true;
                } else if (args[1].equalsIgnoreCase("equipment")) {
                    Message.oedit_help_equipment.send(p);
                    return true;
                }
            }
            Message.oedit_help_general.send(p);
            return true;
        }

        var item = p.getInventory().getItemInMainHand();

        if (item == null || item.getType().isAir()) {
            Message.err_u_should_have_item_in_hand.send(p);
            return true;
        }

        try {

            if (args[0].equalsIgnoreCase("item")) {
                if (args.length > 2) {
                    if (args[1].equalsIgnoreCase("model")) {
                        item.setData(DataComponentTypes.ITEM_MODEL, Key.key(args[2]));
                    } else if (args[1].equalsIgnoreCase("name")) {
                        var newName = new ArrayList<>(Arrays.asList(args));
                        newName.removeFirst();
                        newName.removeFirst();
                        item.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize(
                                Message.translateCodes(String.join(" ", newName))));
                    } else if (args[1].equalsIgnoreCase("custom_name")) {
                        var newName = new ArrayList<>(Arrays.asList(args));
                        newName.removeFirst();
                        newName.removeFirst();
                        item.setData(DataComponentTypes.CUSTOM_NAME, MiniMessage.miniMessage().deserialize(
                                Message.translateCodes(String.join(" ", newName))));
                    } else if (args[1].equalsIgnoreCase("max_damage")) {
                        item.setData(DataComponentTypes.MAX_DAMAGE, Utils.parseInt(args[2]));
                    } else if (args[1].equalsIgnoreCase("damage")) {
                        item.setData(DataComponentTypes.DAMAGE, Utils.parseInt(args[2]));
                    } else if (args[1].equalsIgnoreCase("max_stack_size")) {
                        item.setData(DataComponentTypes.MAX_STACK_SIZE, Utils.parseInt(args[2]));
                    } else if (args[1].equalsIgnoreCase("repair_cost")) {
                        item.setData(DataComponentTypes.REPAIR_COST, Utils.parseInt(args[2]));
                    } else if (args[1].equalsIgnoreCase("hide_tooltip")) {
                        item.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay()
                                .hideTooltip(args[2].equalsIgnoreCase("true")));
                    } else if (args.length > 3) {
                        if (args[1].equalsIgnoreCase("use_cooldown")) {
                            item.setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(Utils.parseFloat(args[2])).cooldownGroup(Key.key(args[3])));
                        }
                    }
                }

                var tooltipDisplay = item.getData(DataComponentTypes.TOOLTIP_DISPLAY);
                var repairCost = item.getData(DataComponentTypes.REPAIR_COST);


                Message.current_item_data.replace(
                                "{model}", String.valueOf(item.getData(DataComponentTypes.ITEM_MODEL)),
                                "{name}", Utils.formatMinimessage(item.getData(DataComponentTypes.ITEM_NAME)),
                                "{custom-name}", Utils.formatMinimessage(item.getData(DataComponentTypes.CUSTOM_NAME)),
                                "{max-damage}", String.valueOf(item.getData(DataComponentTypes.MAX_DAMAGE)),
                                "{damage}", String.valueOf(item.getData(DataComponentTypes.DAMAGE)),
                                "{max-stack-size}", String.valueOf(item.getData(DataComponentTypes.MAX_STACK_SIZE)),
                                "{hide-tooltip}", (tooltipDisplay != null ? String.valueOf(tooltipDisplay.hideTooltip()) : "false"),
                                "{repair-cost}", (repairCost != null ? String.format("%.2f", repairCost.floatValue()) : "0")


                        )
                        .send(p);

            } else if (args[0].equalsIgnoreCase("block")) {
                var block = NBTUtil.getBlockData(item);
                if (args.length > 1) {
                    if (args[1].equalsIgnoreCase("model")) {
                        item = NBTUtil.setBlockId(item, args[2]);
                        block = NBTUtil.getBlockData(item);
                    } else if (block != null) {
                        if (args[1].equalsIgnoreCase("break_speed_multiplier")) {
                            if (args.length >= 4) {
                                if (args[2].equalsIgnoreCase("apply_tool_grade_multiplier")) {
                                    var set = block.getToolSpeedHasGradeMultiplier();
                                    set.clear();
                                    for (var i = 3; i < args.length; i++) {
                                        set.add(ToolItemType.valueOf(args[i].toUpperCase()));
                                    }
                                } else if (args.length == 5) {
                                    if (args[2].equalsIgnoreCase("material")) {
                                        block.getMaterialSpeedMultipliers().put(Material.valueOf(args[3].toUpperCase()),
                                                (double) Utils.parseFloat(args[4]));
                                    } else if (args[2].equalsIgnoreCase("tool")) {
                                        block.getToolSpeedMultipliers().put(ToolItemType.valueOf(args[3].toUpperCase()),
                                                (double) Utils.parseFloat(args[4]));
                                    } else if (args[2].equalsIgnoreCase("model")) {
                                        block.getModelSpeedMultipliers().put(args[3],
                                                (double) Utils.parseFloat(args[4]));
                                    }
                                }
                            }
                            if (block.getMaterialSpeedMultipliers().isEmpty()
                                    && block.getToolSpeedMultipliers().isEmpty()
                                    && block.getModelSpeedMultipliers().isEmpty())
                                Message.oedit_break_speed_multiplier_none.send(p);
                            else
                                Message.oedit_break_speed_multiplier.send(p);
                            block.getMaterialSpeedMultipliers().forEach((k, v) -> {
                                if (v != -1)
                                    Message.oedit_break_speed_multiplier_material
                                            .replace("{material}", k.name().toLowerCase(),
                                                    "{multiplier}", String.format("%.2f", v)).send(p);
                            });
                            block.getToolSpeedMultipliers().forEach((k, v) -> {
                                if (v != -1)
                                    Message.oedit_break_speed_multiplier_tool
                                            .replace("{tool}", k.name().toLowerCase(),
                                                    "{multiplier}", String.format("%.2f", v)).send(p);
                            });
                            block.getModelSpeedMultipliers().forEach((k, v) -> {
                                if (v != -1)
                                    Message.oedit_break_speed_multiplier_model
                                            .replace("{model}", k,
                                                    "{multiplier}", String.format("%.2f", v)).send(p);
                            });
                            if (!block.getToolSpeedHasGradeMultiplier().isEmpty())
                                Message.oedit_break_speed_grade_list.replace("{values}",
                                                String.join(Message.oedit_break_speed_grade_delimiter.get().toString(),
                                                        block.getToolSpeedHasGradeMultiplier()
                                                                .stream().map(i -> i.name().toLowerCase()).toList()))
                                        .send(p);
                        } else if (args.length > 2) {
                            if (args[1].equalsIgnoreCase("drop_on_break")) {
                                block.setDropOnBreak(args[2].equalsIgnoreCase("true"));
                            } else if (args[1].equalsIgnoreCase("drop_on_destroy_by_liquid")) {
                                block.setDropOnDestroyByLiquid(args[2].equalsIgnoreCase("true"));
                            } else if (args[1].equalsIgnoreCase("drop_on_explosion")) {
                                block.setDropOnExplosion(args[2].equalsIgnoreCase("true"));
                            } else if (args[1].equalsIgnoreCase("drop_on_burn")) {
                                block.setDropOnBurn(args[2].equalsIgnoreCase("true"));
                            } else if (args[1].equalsIgnoreCase("can_burn")) {
                                block.setCanBurn(args[2].equalsIgnoreCase("true"));
                            } else if (args[1].equalsIgnoreCase("can_be_replaced")) {
                                block.setCanBeReplaced(args[2].equalsIgnoreCase("true"));
                            } else if (args[1].equalsIgnoreCase("can_be_destroyed_by_liquid")) {
                                block.setCanBeDestroyedByLiquid(args[2].equalsIgnoreCase("true"));
                            }
                        }
                        item = block.applyData();
                    }
                }
                if (block == null) {
                    Message.err_block_id_not_set.send(p);
                    return true;
                }

                Message.current_block_data.replace(
                        "{drop-on-break}", String.valueOf(block.dropOnBreak()),
                        "{drop-on-destroy-by-liquid}", String.valueOf(block.dropOnDestroyByLiquid()),
                        "{drop-on-explosion}", String.valueOf(block.dropOnExplosion()),
                        "{drop-on-burn}", String.valueOf(block.dropOnBurn()),
                        "{can-be-blown}", String.valueOf(block.canBeBlown()),
                        "{can-burn}", String.valueOf(block.canBurn()),
                        "{can-be-destroyed-by-liquid}", String.valueOf(block.canBeDestroyedByLiquid()),
                        "{model}", block.getId(),
                        "{can-be-replaced}", String.valueOf(block.canBeReplaced())).send(p);
            } else if (args[0].equalsIgnoreCase("equipment")) {
                var data = item.getData(DataComponentTypes.EQUIPPABLE);

                if (data == null)
                    data = Equippable.equippable(EquipmentSlot.HEAD).build();


                if (args.length > 2) {

                    //keyed
                    if (args[1].equalsIgnoreCase("model")) {
                        data = data.toBuilder().assetId(Key.key(args[2])).build();
                        item.setData(DataComponentTypes.EQUIPPABLE, data);
                    } else if (args[1].equalsIgnoreCase("camera_overlay")) {
                        data = data.toBuilder().cameraOverlay(Key.key(args[2])).build();
                        item.setData(DataComponentTypes.EQUIPPABLE, data);
                    } else if (args[1].equalsIgnoreCase("shear_sound")) {
                        data = data.toBuilder().shearSound(Key.key(args[2])).build();
                        item.setData(DataComponentTypes.EQUIPPABLE, data);
                    } else if (args[1].equalsIgnoreCase("equip_sound")) {
                        data = data.toBuilder().equipSound(Key.key(args[2])).build();
                        item.setData(DataComponentTypes.EQUIPPABLE, data);
                    }
                    /* true/false  */
                    else if (args[1].equalsIgnoreCase("equip_on_interact")) {
                        data = data.toBuilder().equipOnInteract(args[2].equalsIgnoreCase("true")).build();
                        item.setData(DataComponentTypes.EQUIPPABLE, data);
                    } else if (args[1].equalsIgnoreCase("dispensable")) {
                        data = data.toBuilder().dispensable(args[2].equalsIgnoreCase("true")).build();
                        item.setData(DataComponentTypes.EQUIPPABLE, data);
                    } else if (args[1].equalsIgnoreCase("damage_on_hurt")) {
                        data = data.toBuilder().damageOnHurt(args[2].equalsIgnoreCase("true")).build();
                        item.setData(DataComponentTypes.EQUIPPABLE, data);
                    } else if (args[1].equalsIgnoreCase("can_be_sheared")) {
                        data = data.toBuilder().canBeSheared(args[2].equalsIgnoreCase("true")).build();
                        item.setData(DataComponentTypes.EQUIPPABLE, data);
                    } else if (args[1].equalsIgnoreCase("swappable")) {
                        data = data.toBuilder().swappable(args[2].equalsIgnoreCase("true")).build();
                        item.setData(DataComponentTypes.EQUIPPABLE, data);
                    }
                    /* equippable stupid hack */
                    else if (args[1].equalsIgnoreCase("slot")) {
                        data = Equippable.equippable(EquipmentSlot.valueOf(args[2].toUpperCase()))
                                .assetId(data.assetId())
                                .allowedEntities(data.allowedEntities())
                                .cameraOverlay(data.cameraOverlay())
                                .canBeSheared(data.canBeSheared())
                                .damageOnHurt(data.damageOnHurt())
                                .shearSound(data.shearSound())
                                .swappable(data.swappable())
                                .equipSound(data.equipSound())
                                .equipOnInteract(data.equipOnInteract())
                                .dispensable(data.dispensable())
                                .build();

                        item.setData(DataComponentTypes.EQUIPPABLE, data);
                    }
                    /* custom */
                    /* not used now!!! else if (args[1].equalsIgnoreCase("ignore_damage_types")) {
                        var ignoreDamageTypes = new ArrayList<>(Arrays.asList(args));
                        ignoreDamageTypes.removeFirst();
                        ignoreDamageTypes.removeFirst();

                        item = NBTUtil.setIgnoreDamageCauses(item, ignoreDamageTypes.stream().map(i -> i.toUpperCase())
                                .toList());
                    }*/
                }

                Message.current_equipment_data.replace(
                        "{model}", String.valueOf(data.assetId()),
                        "{equip-on-interact}", String.valueOf(data.equipOnInteract()),
                        "{equip-sound}", String.valueOf(data.equipSound().toString()),
                        "{shear-sound}", String.valueOf(data.shearSound().toString()),
                        "{camera-overlay}", String.valueOf(data.cameraOverlay()),
                        "{slot}", data.slot().toString().toLowerCase(),
                        "{swappable}", String.valueOf(data.swappable()),
                        "{can-be-sheared}", String.valueOf(data.canBeSheared()),
                        "{damage-on-hurt}", String.valueOf(data.damageOnHurt()),
                        "{dispensable}", String.valueOf(data.dispensable())

                ).send(sender);
            }


            p.getInventory().setItemInMainHand(item);
        } catch (CommandException ex) {
            ex.send(p);
            return true;
        }

        return true;
    }



    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            return Lists.newArrayList("item", "block", "help", "equipment")
                    .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[0])).toList();
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("item"))
                return Lists.newArrayList("name", "custom_name", "model", "max_damage", "max_stack_size",
                                "repair_cost", "hide_tooltip")
                        .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[1])).toList();

            if (args[0].equalsIgnoreCase("block"))
                return Lists.newArrayList("drop_on_break", "drop_on_destroy_by_liquid", "drop_on_explosion",
                                "drop_on_burn", "can_be_blown", "can_burn", "can_be_replaced",
                                "model", "can_be_destroyed_by_liquid", "break_speed_multiplier")
                        .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[1])).toList();

            if (args[0].equalsIgnoreCase("equipment"))
                return Lists.newArrayList("swappable", "can_be_sheared", "damage_on_hurt", "dispensable",
                                "equip_on_interact", "equip_sound", "shear_sound", "camera_overlay", "slot")
                        .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[1])).toList();

            if (args[0].equalsIgnoreCase("help"))
                return Lists.newArrayList("item", "block", "equipment")
                        .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[1])).toList();
            return List.of();
        }

        if (args.length >= 3) {
            if (args[0].equalsIgnoreCase("item")) {
                if (args[1].equalsIgnoreCase("model"))
                    return OpenItems.getInstance().getModelRegistry().getItems()
                            .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[2])).toList();
                if (Sets.newHashSet("repair_cost", "hide_tooltip").contains(args[1]))
                    return Stream.of("true", "false")
                            .filter(i -> StringUtil.startsWithIgnoreCase(i, args[2])).toList();


            } else if (args[0].equalsIgnoreCase("block")) {
                if (args[1].equalsIgnoreCase("model"))
                    return OpenItems.getInstance().getModelRegistry().getBlockTypes().keySet().stream()
                            .filter(i -> StringUtil.startsWithIgnoreCase(i, args[2])).toList();
                else if (args.length == 3 && args[1].equalsIgnoreCase("break_speed_multiplier"))
                    return Lists.newArrayList("tool", "material", "model", "apply_tool_grade_multiplier").stream()
                            .filter(i -> StringUtil.startsWithIgnoreCase(i, args[2])).toList();
                else if (args.length == 4 && args[2].equalsIgnoreCase("tool"))
                    return Arrays.stream(ToolItemType.values()).map(i -> i.name().toLowerCase())
                            .filter(i -> StringUtil.startsWithIgnoreCase(i, args[3])).toList();
                else if (args.length == 4 && args[2].equalsIgnoreCase("material"))
                    return Arrays.stream(Material.values()).map(i -> i.name().toLowerCase())
                            .filter(i -> StringUtil.startsWithIgnoreCase(i, args[3])).toList();
                else if (args.length == 4 && args[2].equalsIgnoreCase("model"))
                    return OpenItems.getInstance().getModelRegistry().getItems()
                            .stream().filter(i -> StringUtil.startsWithIgnoreCase(i, args[3])).toList();
                else if (args[2].equalsIgnoreCase("apply_tool_grade_multiplier"))
                    return Arrays.stream(ToolItemType.values()).map(i -> i.name().toLowerCase())
                            .filter(i -> StringUtil.startsWithIgnoreCase(i, args[args.length - 1])).toList();
                else
                    return Lists.newArrayList("true", "false").stream()
                            .filter(i -> StringUtil.startsWithIgnoreCase(i, args[2])).toList();
            } else if (args[0].equalsIgnoreCase("equipment")) {
                if (args[1].equalsIgnoreCase("model"))
                    return OpenItems.getInstance().getModelRegistry().getEquipment().stream()
                            .filter(i -> StringUtil.startsWithIgnoreCase(i, args[2])).toList();
                else if (args[1].equalsIgnoreCase("slot"))
                    return Arrays.stream(EquipmentSlot.values()).map(i -> i.name().toLowerCase())
                            .filter(i -> StringUtil.startsWithIgnoreCase(i, args[2])).toList();
                else if (Sets.newHashSet("equip_on_interact", "dispensable",
                                "damage_on_hurt", "can_be_sheared", "swappable", "model")
                        .contains(args[1]))
                    return Stream.of("true", "false")
                            .filter(i -> StringUtil.startsWithIgnoreCase(i, args[2])).toList();
                else if (Sets.newHashSet("equip_sound", "shear_sound")
                        .contains(args[1]))
                    return RegistryAccess.registryAccess().getRegistry(RegistryKey.SOUND_EVENT).keyStream()
                            .map(NamespacedKey::toString).filter(i -> StringUtil.startsWithIgnoreCase(i, args[2])).toList();
                else if (args[1].equalsIgnoreCase("ignore_damage_types"))
                    return Arrays.stream(EntityDamageEvent.DamageCause.values()).map(i -> i.name().toLowerCase())
                            .filter(i -> StringUtil.startsWithIgnoreCase(i, args[args.length - 1])).toList();
            }
        }
        return List.of();
    }
}
