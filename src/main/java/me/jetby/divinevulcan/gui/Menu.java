package me.jetby.divinevulcan.gui;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.api.text.SerializerType;
import com.jodexindustries.jguiwrapper.gui.advanced.AdvancedGui;
import me.jetby.divinevulcan.Main;
import me.jetby.divinevulcan.Vulcan;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.*;


public class Menu extends AdvancedGui {

    public static final NamespacedKey CHANCE = new NamespacedKey("vulcanevent", "chance");
    private static final NamespacedKey KEY = new NamespacedKey("vulcanevent", "key");


    public Menu(Main plugin) {
        super("DivineVulcan by TreexStudio");

        defaultSerializer = SerializerType.LEGACY_AMPERSAND;

        int slot = 0;
        for (String type : plugin.getVulcans().getVulcans().keySet()) {
            int finalSlot = slot;
            registerItem(type, builder -> {
                builder.slots(finalSlot);

                builder.defaultItem(ItemWrapper.builder(Material.BARREL)
                        .displayName("&#FB430A&l⭐ &fВулкан: &e" + type)
                        .lore(List.of(
                                "&#FB430A&m=                                   &m=",
                                "",
                                "&#FB430A&lЛКМ &7- &fНастроить вулкан ",
                                "",
                                "&#FB430A&m=                                   &m="
                                ))
                        .build());


            builder.defaultClickHandler((event, controller) ->  {
                event.setCancelled(true);

                if (event.getWhoClicked() instanceof Player player) {
                    Vulcan vulcan = plugin.getVulcans().getVulcans().get(type);
                    new JMenu(player, vulcan, plugin, plugin.getItems()).open(player);
                }
                });
            });
            slot++;
        }

    }
}