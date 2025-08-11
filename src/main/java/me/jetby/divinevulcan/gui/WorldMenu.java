package me.jetby.divinevulcan.gui;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.AdvancedGui;
import com.jodexindustries.jguiwrapper.gui.advanced.GuiItemController;
import me.jetby.divinevulcan.Main;
import me.jetby.divinevulcan.Vulcan;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

public class WorldMenu extends AdvancedGui {

    public WorldMenu(Player player, Vulcan vulcan, Main plugin) {

        super("&0Выбор мира");

        onClose(event -> {
            JMenu menu = new JMenu(player, vulcan, plugin, plugin.getItems());
            Bukkit.getScheduler().runTaskLater(vulcan.getPlugin(), ()-> {menu.open(player);}, 1L);
        });

        int i = 0;
        for (World world : Bukkit.getWorlds()) {
            int finalI = i;

            registerItem(world.getName(), builder -> {

                ItemWrapper item = ItemWrapper.builder(getMaterial(world))
                        .displayName(world.getName())
                        .enchanted(vulcan.getSpawnWorld().equalsIgnoreCase(world.getName()))
                        .build();

                item.itemStack().addItemFlags(ItemFlag.HIDE_ENCHANTS);
                builder.defaultItem(item);
                builder.slots(finalI);
                builder.defaultClickHandler((event, controller) -> {
                    event.setCancelled(true);
                    if (!vulcan.getSpawnWorld().equalsIgnoreCase(world.getName())) {
                        vulcan.setSpawnWorld(world.getName());
                        controller.updateItemWrappers(updater -> {
                            item.enchanted(true);
                            item.itemStack().addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        });
                    }
                    for (GuiItemController guiItemController : getControllers()) {
                        guiItemController.updateItemWrappers(itemWrapper -> {
                            TextComponent textComponent = (TextComponent) itemWrapper.displayName();

                            if (!vulcan.getSpawnWorld().equalsIgnoreCase(textComponent.content())) {
                                itemWrapper.enchanted(false);
                            }
                        });
                    }
                });
            });
            i++;
        }
    }

    private static @NotNull Material getMaterial(World world) {
        Material block = Material.DIRT;
        switch (world.getEnvironment()) {
            case THE_END -> {
                block = Material.END_STONE;
            }
            case NORMAL -> {
                block = Material.GRASS_BLOCK;
            }
            case NETHER -> {
                block = Material.NETHERRACK;
            }
            case CUSTOM -> {
                block = Material.QUARTZ_BLOCK;
            }
        }

        return block;
    }

}