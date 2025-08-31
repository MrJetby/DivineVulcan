package me.jetby.divinevulcan.gui;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.api.text.SerializerType;
import com.jodexindustries.jguiwrapper.gui.advanced.AdvancedGui;
import com.jodexindustries.jguiwrapper.gui.advanced.AdvancedGuiClickHandler;
import lombok.Getter;
import me.jetby.divinevulcan.Main;
import me.jetby.divinevulcan.Vulcan;
import me.jetby.divinevulcan.configurations.Items;
import me.jetby.divinevulcan.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

import java.util.*;

@Getter
public class JMenu extends AdvancedGui implements Listener {

    private final Main plugin;
    
    public JMenu(Player player, Vulcan vulcan, Main plugin, Items items) {
        super("&0&lDivineVulcan");
        this.plugin = plugin;
        defaultSerializer = SerializerType.LEGACY_AMPERSAND;


        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        registerItem("red_glass", builder -> {
            builder.slots(0, 1, 7, 8, 9, 17, 36, 44, 45,46, 52, 53)
                    .defaultItem(ItemWrapper.builder(Material.RED_STAINED_GLASS_PANE).build())
                    .defaultClickHandler((event, controller) ->  {
                        event.setCancelled(true);
                    });
        });

        AdvancedGuiClickHandler staticLocation = (event, controller) -> {
            event.setCancelled(true);

            vulcan.setStaticLocation(!vulcan.isStaticLocation());
            String msg = vulcan.isStaticLocation() ? "§aСтатическая локация теперь включена" : "§cСтатическая локация теперь отключена";
            player.sendMessage(TextUtil.colorize(msg));

            controller.updateItemWrappers(itemWrapper -> {
                itemWrapper.lore(List.of(
                        "&#FB430A&m=                                   =",
                        ""+checkColor(vulcan.isStaticLocation())+"● &fСейчас: "+check(vulcan.isStaticLocation()),
                        "",
                        "&#FB430A&l▶ &fНажмите чтобы переключить",
                        "&#FB430A&m=                                   ="
                ));
            });
        };

        registerItem("info", builder -> {

            ItemWrapper itemWrapper;




            itemWrapper = ItemWrapper
                    .builder(Material.KNOWLEDGE_BOOK)
                    .displayName("&#FB430A&l⭐ &fИнформация")
                    .lore(List.of(
                            ""+checkColor(vulcan.isStarted())+"● &fИвент запущен: "+check(vulcan.isStarted()),
                            ""+checkColor(vulcan.isActivation())+"● &fИвент активируется: "+check(vulcan.isActivation()),
                            "&#FB430A&m=                                   =",
                            "&#FB430A&lЛКМ &7- &fЗапустить/Остановить ивент",
                            "&#FB430A&lПКМ &7- &fАктивировать ивент",
                            "&#FB430A&lСКМ &7- &fТелепорт к ивенту",
                            "&#FB430A&m=                                   ="
                    ))
                    .build();

            builder.slots(13)
                    .defaultClickHandler((event, controller) -> {
                        event.setCancelled(true);



                        switch (event.getClick()) {
                            case LEFT -> {
                                if (!vulcan.isStarted()) {
                                    vulcan.start();
                                } else {
                                    vulcan.stop();
                                }

                            }
                            case RIGHT -> {
                                if (vulcan.isStarted()) {
                                    if (!vulcan.isActivation()) {
                                        vulcan.activation();
                                    }
                                } else {
                                    player.sendMessage(TextUtil.colorize("§cИвент не запущен"));
                                }

                            }
                            case MIDDLE -> {
                                if (vulcan.isStarted()) {
                                    if (vulcan.getLocation()!=null) player.teleport(vulcan.getLocation());
                                } else {
                                    player.sendMessage(TextUtil.colorize("§cИвент не запущен"));
                                }


                            }
                        }

                        getController("info").get().updateItemWrappers(wrapper -> {
                            wrapper.lore(List.of(
                                    "&fИвент запущен: "+check(vulcan.isStarted()),
                                    "&fИвент активируется: "+check(vulcan.isActivation()),
                                    "&#FB430A&m=                                   =",
                                    "&#FB430A&lЛКМ &7- &fЗапустить/Остановить ивент",
                                    "&#FB430A&lПКМ &7- &fАктивировать ивент",
                                    "&#FB430A&lСКМ &7- &fТелепорт к ивенту",
                                    "&#FB430A&m=                                   ="
                            ));
                        });
                    });

            builder.defaultItem(itemWrapper);
        });
        registerItem("static", builder -> {
            ItemWrapper itemWrapper;

            itemWrapper = ItemWrapper
                    .builder(Material.BEDROCK)
                    .displayName("&#FB430A&l⭐ &fСтатическая локация")
                    .lore(List.of(
                            "&#FB430A&m=                                   =",
                            checkColor(vulcan.isStaticLocation())+"● &fСейчас: "+check(vulcan.isStaticLocation()),
                            "",
                            "&#FB430A&l▶ &fНажмите чтобы переключить",
                            "&#FB430A&m=                                   ="
                    ))
                    .build();

            builder.slots(20)
                    .defaultClickHandler(staticLocation);

            builder.defaultItem(itemWrapper);
        });
        registerItem("edit", builder -> {
            builder.slots(21)
                    .defaultItem(ItemWrapper.builder(Material.CHEST)
                            .displayName("&#FB430A&l⭐ &fНастроить все предметы")
                            .lore(List.of(
                                    "&#FB430A&m=                                   =",
                                    "",
                                    "&#FB430A&l▶ &fНажмите чтобы настроить",
                                    "&#FB430A&m=                                   ="
                            ))
                            .build())
                    .defaultClickHandler((event, controller) -> {
                        event.setCancelled(true);
                        InvEdit invEdit = new InvEdit(player, vulcan.getType(), items, plugin);
                        invEdit.open(player);
                    });
        });

        registerItem("world", builder -> {
            builder.slots(23)
                    .defaultItem(ItemWrapper.builder(Material.PLAYER_HEAD)
                            .displayName("&#FB430A&l⭐ &fИзменить мир")
                            .lore(List.of(
                                    "&#FB430A&m=                                   =",
                                    "&cℹ &7&oИзменить мир в котором будет спавнится вулкан",
                                    "",
                                    "&a● &fТекущий мир: &a"+vulcan.getSpawnWorld().getName(),
                                    "",
                                    "&#FB430A&l▶ &fНажмите чтобы настроить",
                                    "&#FB430A&m=                                   ="
                            ))
                            .build())
                    .defaultClickHandler((event, controller) -> {
                        event.setCancelled(true);

                       new WorldMenu(player, vulcan, plugin).open(player);

                    });
        });

        registerItem("pregenerated", builder -> {
            builder.slots(24).defaultItem(ItemWrapper.builder(Material.GRASS_BLOCK)
                    .displayName("&#FB430A&l⭐ &fЗаранее сгенерированные локации")
                    .lore(List.of(
                            "&#FB430A&m=                                   =",
                            ""+checkColor(vulcan.isUsePreGeneratedLocations())+"● &fИспользовать: "+check(vulcan.isUsePreGeneratedLocations()),
                            "",
                            "&#FB430A&l● &fСгенерировано локаций: &#FB430A"+plugin.getPreLocationGenerator().getLocationsAmount(vulcan.getType()),
                            "",
                            "&#FB430A&lЛКМ &7- &fНачать поиск 50 локаций",
                            "&#FB430A&lПКМ &7- &fОтменить поиск",
                            "&#FB430A&lСКМ &7- &fПереключить &atrue/&cfalse",
                            "&#FB430A&m=                                   ="

                    ))
                    .build());
            builder.defaultClickHandler((event, controller) -> {

                event.setCancelled(true);

                switch (event.getClick()) {
                    case LEFT -> {
                        if (!plugin.getPreLocationGenerator().getSessions().contains(player.getUniqueId())) {
                            player.sendMessage(TextUtil.colorize("&aНачинаю искать локации..."));
                            plugin.getPreLocationGenerator().getSessions().add(player.getUniqueId());
                            plugin.getPreLocationGenerator().startSearch(player, vulcan.getType(), 50, this);
                        } else {
                            player.sendMessage(TextUtil.colorize("&cУ вас уже есть активная сессия поиска локаций"));
                        }

                    }
                    case RIGHT -> {
                        if (plugin.getPreLocationGenerator().getSessions().contains(player.getUniqueId())) {
                            plugin.getPreLocationGenerator().getSessions().remove(player.getUniqueId());
                        } else {
                            player.sendMessage(TextUtil.colorize("&cНечего отменять :/"));
                        }
                    }
                    case MIDDLE -> {
                        vulcan.setUsePreGeneratedLocations(!vulcan.isUsePreGeneratedLocations());
                    }
                }

                controller.updateItems(wrapper -> {
                    wrapper.lore(List.of(
                            "&#FB430A&m=                                   =",
                            checkColor(vulcan.isUsePreGeneratedLocations())+"● &fИспользовать: "+check(vulcan.isUsePreGeneratedLocations()),
                            "",
                            "&#FB430A&l● &fСгенерировано локаций: &#FB430A"+plugin.getPreLocationGenerator().getLocationsAmount(vulcan.getType()),
                            "",
                            "&#FB430A&lЛКМ &7- &fНачать поиск 50 локаций",
                            "&#FB430A&lПКМ &7- &fОтменить поиск",
                            "&#FB430A&lСКМ &7- &fПереключить &atrue/&cfalse",
                            "&#FB430A&m=                                   ="
                    ));
                });
            });
        });


        registerItem("online", builder -> {
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text("§aНажмите чтобы изменить"));
            builder.slots(29).defaultItem(ItemWrapper.builder(Material.NAME_TAG)
                    .displayName(LEGACY_AMPERSAND.deserialize("&rМинимальный онлайн: &e"+vulcan.getMinPlayers()))
                    .lore(lore)
                    .build());
            builder.defaultClickHandler((event, controller) -> {
                event.setCancelled(true);
                player.sendMessage(TextUtil.colorize("&7→ &fВведите значение в чат &7[Текущее значение: " + vulcan.getMinPlayers( ) + "]"));
                onlineEditing.put(player.getUniqueId(), vulcan);
                close(player);
            });
        });

        registerItem("minRadius", builder -> {
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text("§aНажмите чтобы изменить"));
            builder.slots(30).defaultItem(ItemWrapper.builder(Material.NAME_TAG)
                    .displayName(LEGACY_AMPERSAND.deserialize("&rМинимальный радиус: &e"+vulcan.getSpawnRadiusMin()))
                    .lore(lore)
                    .build());
            builder.defaultClickHandler((event, controller) -> {
                event.setCancelled(true);
                player.sendMessage(TextUtil.colorize("&7→ &fВведите значение в чат &7[Текущее значение: " + vulcan.getSpawnRadiusMin( ) + "]"));
                minRadiusEditing.put(player.getUniqueId(), vulcan);
                close(player);
            });
        });

        registerItem("maxRadius", builder -> {
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text("§aНажмите чтобы изменить"));
            builder.slots(31).defaultItem(ItemWrapper.builder(Material.NAME_TAG)
                    .displayName(LEGACY_AMPERSAND.deserialize("&rМаксимальный радиус: &e"+vulcan.getSpawnRadiusMax()))
                    .lore(lore)
                    .build());
            builder.defaultClickHandler((event, controller) -> {
                event.setCancelled(true);
                player.sendMessage(TextUtil.colorize("&7→ &fВведите значение в чат &7[Текущее значение: " + vulcan.getSpawnRadiusMax( ) + "]"));
                maxRadiusEditing.put(player.getUniqueId(), vulcan);
                close(player);
            });
        });

        registerItem("activation", builder -> {
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text("§aНажмите чтобы изменить"));
            builder.slots(32).defaultItem(ItemWrapper.builder(Material.NAME_TAG)
                    .displayName(LEGACY_AMPERSAND.deserialize("&rДлительность активации: &e"+vulcan.getActivation()))
                    .lore(lore)
                    .build());
            builder.defaultClickHandler((event, controller) -> {
                event.setCancelled(true);
                player.sendMessage(TextUtil.colorize("&7→ &fВведите значение в чат &7[Текущее значение: " + vulcan.getActivation( ) + "]"));
                activation.put(player.getUniqueId(), vulcan);
                close(player);
            });
        });
        registerItem("duration", builder -> {
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text("§aНажмите чтобы изменить"));
            builder.slots(33).defaultItem(ItemWrapper.builder(Material.NAME_TAG)
                    .displayName(LEGACY_AMPERSAND.deserialize("&rДлительность вулкана: &e"+vulcan.getDuration()))
                    .lore(lore)
                    .build());
            builder.defaultClickHandler((event, controller) -> {
                event.setCancelled(true);
                player.sendMessage(TextUtil.colorize("&7→ &fВведите значение в чат &7[Текущее значение: " + vulcan.getDuration( ) + "]"));
                duration.put(player.getUniqueId(), vulcan);
                close(player);
            });
        });
        registerItem("region", builder -> {
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(""));
            lore.add(Component.text("§aНажмите чтобы изменить"));
            builder.slots(40).defaultItem(ItemWrapper.builder(Material.NAME_TAG)
                    .displayName(LEGACY_AMPERSAND.deserialize("&rРазмер привата: &e"+vulcan.getRegionSize()))
                    .lore(lore)
                    .build());
            builder.defaultClickHandler((event, controller) -> {
                event.setCancelled(true);
                player.sendMessage(TextUtil.colorize("&7→ &fВведите значение в чат &7[Текущее значение: " + vulcan.getRegionSize( ) + "]"));
                region.put(player.getUniqueId(), vulcan);
                close(player);
            });
        });


    }


    public String check(boolean status){
        return status ? "&atrue" : "&cfalse";
    }
    public String checkColor(boolean status){
        return status ? "&a" : "&c";
    }


    Map<UUID, Vulcan> onlineEditing = new HashMap<>();
    Map<UUID, Vulcan> minRadiusEditing = new HashMap<>();
    Map<UUID, Vulcan> maxRadiusEditing = new HashMap<>();
    Map<UUID, Vulcan> activation = new HashMap<>();
    Map<UUID, Vulcan> duration = new HashMap<>();
    Map<UUID, Vulcan> region = new HashMap<>();

    @EventHandler
    public void onChat(PlayerChatEvent e) {

        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (region.containsKey(uuid)) {
            e.setCancelled(true);
            String msg = e.getMessage();
            try {
                Vulcan vulcan = region.get(uuid);
                int number = Integer.parseInt(msg);
                vulcan.setRegionSize(number);
                getController("region").get().updateItemWrappers(itemWrapper -> {
                    itemWrapper.displayName(LEGACY_AMPERSAND.deserialize("&rРазмер привата: &e"+vulcan.getRegionSize()));
                });
            } catch (NumberFormatException ex) {
                player.sendMessage(TextUtil.colorize("Возникла ошибка, значение должно быть числом"));
            }
            region.remove(player.getUniqueId());
            plugin.getVulcans().save(true);
            open(player);
            return;
        }
        if (duration.containsKey(uuid)) {
            e.setCancelled(true);
            String msg = e.getMessage();
            try {
                Vulcan vulcan = duration.get(uuid);
                int number = Integer.parseInt(msg);
                vulcan.setDuration(number);
                getController("duration").get().updateItemWrappers(itemWrapper -> {
                    itemWrapper.displayName(LEGACY_AMPERSAND.deserialize("&rДлительность вулкана: &e"+vulcan.getDuration()));
                });
            } catch (NumberFormatException ex) {
                player.sendMessage(TextUtil.colorize("Возникла ошибка, значение должно быть числом"));
            }
            duration.remove(player.getUniqueId());
            plugin.getVulcans().save(true);
            open(player);
            return;
        }
        if (activation.containsKey(uuid)) {
            e.setCancelled(true);
            String msg = e.getMessage();
            try {
                Vulcan vulcan = activation.get(uuid);
                int number = Integer.parseInt(msg);
                vulcan.setActivation(number);
                getController("activation").get().updateItemWrappers(itemWrapper -> {
                    itemWrapper.displayName(LEGACY_AMPERSAND.deserialize("&rДлительность активации: &e"+vulcan.getActivation()));
                });
            } catch (NumberFormatException ex) {
                player.sendMessage(TextUtil.colorize("Возникла ошибка, значение должно быть числом"));
            }
            activation.remove(player.getUniqueId());
            plugin.getVulcans().save(true);
            open(player);
            return;
        }
        if (onlineEditing.containsKey(uuid)) {
            e.setCancelled(true);
            String msg = e.getMessage();
            try {
                Vulcan vulcan = onlineEditing.get(uuid);
                int number = Integer.parseInt(msg);
                vulcan.setMinPlayers(number);
                getController("online").get().updateItemWrappers(itemWrapper -> {
                    itemWrapper.displayName(LEGACY_AMPERSAND.deserialize("&rМинимальный онлайн: &e"+vulcan.getMinPlayers()));
                });
            } catch (NumberFormatException ex) {
                player.sendMessage(TextUtil.colorize("Возникла ошибка, значение должно быть числом"));
            }
            onlineEditing.remove(player.getUniqueId());
            plugin.getVulcans().save(true);
            open(player);
            return;
        }

        if (minRadiusEditing.containsKey(uuid)) {
            e.setCancelled(true);
            String msg = e.getMessage();
            try {
                Vulcan vulcan = minRadiusEditing.get(uuid);
                int number = Integer.parseInt(msg);
                vulcan.setSpawnRadiusMin(number);
                getController("minRadius").get().updateItemWrappers(itemWrapper -> {
                    itemWrapper.displayName(LEGACY_AMPERSAND.deserialize("&rМинимальный радиус: &e"+vulcan.getSpawnRadiusMin()));
                });
            } catch (NumberFormatException ex) {
                player.sendMessage(TextUtil.colorize("Возникла ошибка, значение должно быть числом"));
            }
            minRadiusEditing.remove(player.getUniqueId());
            plugin.getVulcans().save(true);
            open(player);
            return;
        }
        if (maxRadiusEditing.containsKey(uuid)) {
            e.setCancelled(true);
            String msg = e.getMessage();
            try {
                Vulcan vulcan = maxRadiusEditing.get(uuid);
                int number = Integer.parseInt(msg);
                vulcan.setSpawnRadiusMax(number);
                getController("maxRadius").get().updateItemWrappers(itemWrapper -> {
                    itemWrapper.displayName(LEGACY_AMPERSAND.deserialize("&rМаксимальный радиус: &e"+vulcan.getSpawnRadiusMax()));
                });
            } catch (NumberFormatException ex) {
                player.sendMessage(TextUtil.colorize("Возникла ошибка, значение должно быть числом"));
            }
            maxRadiusEditing.remove(player.getUniqueId());
            plugin.getVulcans().save(true);
            open(player);
            return;
        }
    }
}
