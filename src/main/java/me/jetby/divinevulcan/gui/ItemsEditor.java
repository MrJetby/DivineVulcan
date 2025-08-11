package me.jetby.divinevulcan.gui;


import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.AdvancedGui;
import me.jetby.divinevulcan.Main;
import me.jetby.divinevulcan.configurations.Items;
import me.jetby.divinevulcan.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

import static me.jetby.divinevulcan.gui.Menu.CHANCE;

public class ItemsEditor extends AdvancedGui {

    private final Items items;

    public ItemsEditor(Player player, String inv, String type, Items items, Main plugin) {
        super("Редактор предметов");
        this.items = items;


        setCancelEmptySlots(false);
        onDrag(inventoryDragEvent -> {
            inventoryDragEvent.setCancelled(false);
        });

        List<Items.ItemsData> map = items.getData().get(type);
        for (Items.ItemsData itemData : map) {
            if (!itemData.inv().equals(inv)) continue;
            if (itemData.itemStack() == null) continue;
            registerItem(itemData.slot().toString()+"-"+itemData.inv(), builder -> {
                builder.slots(itemData.slot());
                builder.defaultItem(new ItemWrapper(itemData.itemStack()));
                builder.defaultClickHandler((event, controller) -> {
                    event.setCancelled(false);
                });
            });
            Logger.info(itemData.slot() + " был зареган, материал "+itemData.itemStack().getType());
        }

        onClose(event -> {
            saveInv(event.getInventory(), type, inv);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                new InvEdit(player, type, items, plugin).open(player);
            }, 1L);
        });
    }

    private void saveInv(Inventory inventory, String type, String inv) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null) {
                items.removeItem(type, inv, slot);
                continue;
            }
            int chance = item.getItemMeta().getPersistentDataContainer()
                    .getOrDefault(CHANCE, PersistentDataType.INTEGER, 100);
            items.saveItem(type, inv, item, slot, chance);
        }
    }
}