package me.jetby.divinevulcan.gui;


import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.AdvancedGui;
import me.jetby.divinevulcan.Main;
import me.jetby.divinevulcan.configurations.Items;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class InvEdit extends AdvancedGui {

    private final Items items;
    public InvEdit(Player player, String type, Items items, Main plugin) {
        super("Выбор инвентаря");

        this.items = items;

        if (!plugin.isPluginEnabled()) {
            title("Ошибка лицензии!");
            return;
        }

        Set<String> invs = new HashSet<>();
        List<Items.ItemsData> itemsData = items.getData().get(type);
        if (itemsData != null) {
            for (Items.ItemsData data : itemsData) {
                invs.add(data.inv());
            }
        }

        int slot = 0;
        for (String inv : invs) {
            int currentSlot = slot;
            registerItem(inv + "_" + slot, builder -> {
                builder.slots(currentSlot);
                builder.defaultItem(ItemWrapper.builder(Material.CHEST)
                        .lore(List.of(
                                Component.text("§6§l§m=                                   ="),
                                Component.text(""),
                                Component.text(" §6§lЛКМ §7- §fНастроить предметы "),
                                Component.text(" §6§lПКМ §7- §fНастроить шансы предметов "),
                                Component.text(""),
                                Component.text("§6§l§m=                                   =")
                        ))
                        .displayName("&#FB430A&l⭐ &fИнвентарь: &e" + inv)
                        .build());

                builder.defaultClickHandler((event, controller) -> {
                    event.setCancelled(true);

                    switch (event.getClick()) {
                        case LEFT -> {
                            new ItemsEditor(player, inv, type, items, plugin).open(player);
                        }
                        case RIGHT -> {
                            new ChanceEdit(player, type, inv, items, plugin).open(player);
                        }
                    }
                });
            });
            slot++;
        }

        registerItem("add_button", builder -> {
            builder.slots(53);
            builder.defaultItem(ItemWrapper.builder(Material.EMERALD)
                    .displayName("&a[+] Добавить новый инвентарь")
                    .build());

            builder.defaultClickHandler((event, controller) -> {
                event.setCancelled(true);
                String newInvName = "inv_" + System.currentTimeMillis();
                ItemStack dirt = new ItemStack(Material.DIRT);
                items.saveItem(type, newInvName, dirt, 0, 100);

                InvEdit newGui = new InvEdit(player, type, items, plugin);
                newGui.open(player);
            });
        });

//        onClose(event -> {
//            checkAndRemoveEmptyInvs(type);
//        });
    }



    private void checkAndRemoveEmptyInvs(String type) {
        List<Items.ItemsData> itemsData = items.getData().get(type);
        if (itemsData == null) return;

        Map<String, List<Items.ItemsData>> invMap = new HashMap<>();
        for (Items.ItemsData data : itemsData) {
            invMap.computeIfAbsent(data.inv(), k -> new ArrayList<>()).add(data);
        }

        for (Map.Entry<String, List<Items.ItemsData>> entry : invMap.entrySet()) {
            boolean isEmpty = true;
            for (Items.ItemsData data : entry.getValue()) {
                if (data.itemStack() != null) {
                    isEmpty = false;
                    break;
                }
            }
            if (isEmpty) {
                for (Items.ItemsData data : entry.getValue()) {
                    items.removeItem(type, entry.getKey(), data.slot());
                }
            }
        }
    }
}