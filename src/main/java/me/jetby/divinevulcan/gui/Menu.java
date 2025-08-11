package me.jetby.divinevulcan.gui;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.gui.advanced.AdvancedGui;
import me.jetby.divinevulcan.Main;
import me.jetby.divinevulcan.Vulcan;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.*;


public class Menu extends AdvancedGui {

    public static final NamespacedKey CHANCE = new NamespacedKey("vulcanevent", "chance");
    private static final NamespacedKey KEY = new NamespacedKey("vulcanevent", "key");


    public Menu(Main plugin) {
        super("DivineVulcan by TreexStudio");
        var mm = MiniMessage.miniMessage();

        int slot = 0;
        for (String type : plugin.getVulcans().getVulcans().keySet()) {
            int finalSlot = slot;
            registerItem(type, builder -> {
                builder.slots(finalSlot);

                builder.defaultItem(ItemWrapper.builder(Material.BARREL)
                        .displayName(mm.deserialize("<!i><#FB430A><bold>⭐</bold> <white>Вулкан: <yellow>" + type))
                        .lore(List.of(
                                mm.deserialize("<!i><#FB430A><bold><st>=<#FB4C0E>                                   <st>="),
                                mm.deserialize(""),
                                mm.deserialize("<!i> <#FB430A><bold>ЛКМ</bold> <gray>- <white>Настроить вулкан "),
                                mm.deserialize(""),
                                mm.deserialize("<!i><#FB430A><bold><st>=<#FB4C0E>                                   <st>=")
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