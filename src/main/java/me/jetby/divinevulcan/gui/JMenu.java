package me.jetby.divinevulcan.gui;

import com.jodexindustries.jguiwrapper.api.item.ItemWrapper;
import com.jodexindustries.jguiwrapper.api.text.SerializerType;
import com.jodexindustries.jguiwrapper.gui.advanced.AdvancedGui;
import com.jodexindustries.jguiwrapper.gui.advanced.AdvancedGuiClickHandler;
import lombok.Getter;
import me.jetby.divinevulcan.Main;
import me.jetby.divinevulcan.Vulcan;
import me.jetby.divinevulcan.configurations.Items;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class JMenu extends AdvancedGui implements Listener {

    private final MiniMessage mm;

    private final Main plugin;
    public JMenu(Player player, Vulcan vulcan, Main plugin, Items items) {
        super("&0&lDivineVulcan");
        this.plugin = plugin;
        var mm = MiniMessage.miniMessage();
        this.mm = mm;

        if (!plugin.isPluginEnabled()) {
            title("<red>Ошибка лицензии!");
            return;
        }

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
            player.sendMessage(msg);

            controller.updateItemWrappers(itemWrapper -> {
                itemWrapper.lore(List.of(
                        mm.deserialize("<!i><#FB430A><st>=                                   ="),
                        mm.deserialize("<!i>"+checkColor(vulcan.isStaticLocation())+"● <white>Сейчас: "+check(vulcan.isStaticLocation())),
                        mm.deserialize(""),
                        mm.deserialize("<!i><#FB430A><b>▶</b> <white>Нажмите чтобы переключить"),
                        mm.deserialize("<!i><#FB430A><st>=                                   =")
                ));
            });
        };

        registerItem("info", builder -> {

            ItemWrapper itemWrapper;




            itemWrapper = ItemWrapper
                    .builder(Material.KNOWLEDGE_BOOK)
                    .displayName(mm.deserialize("<!i><#FB430A><b>⭐</b> <white>Информация"))
                    .lore(List.of(
                            mm.deserialize("<!i>"+checkColor(vulcan.isStarted())+"● <white>Ивент запущен: "+check(vulcan.isStarted())),
                            mm.deserialize("<!i>"+checkColor(vulcan.isActivation())+"● <white>Ивент активируется: "+check(vulcan.isActivation())),
                            mm.deserialize("<!i><#FB430A><st>=                                   ="),
                            mm.deserialize("<!i><#FB430A><b>ЛКМ</b> <gray>- <white>Запустить/Остановить ивент"),
                            mm.deserialize("<!i><#FB430A><b>ПКМ</b> <gray>- <white>Активировать ивент"),
                            mm.deserialize("<!i><#FB430A><b>СКМ</b> <gray>- <white>Телепорт к ивенту"),
                            mm.deserialize("<!i><#FB430A><st>=                                   =")
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
                                    player.sendMessage("§cИвент не запущен");
                                }

                            }
                            case MIDDLE -> {
                                if (vulcan.isStarted()) {
                                    if (vulcan.getLocation()!=null) player.teleport(vulcan.getLocation());
                                } else {
                                    player.sendMessage("§cИвент не запущен");
                                }


                            }
                        }

                        getController("info").get().updateItemWrappers(wrapper -> {
                            wrapper.lore(List.of(
                                    mm.deserialize("<!i><white>Ивент запущен: "+check(vulcan.isStarted())),
                                    mm.deserialize("<!i><white>Ивент активируется: "+check(vulcan.isActivation())),
                                    mm.deserialize("<!i><#FB430A><st>=                                   ="),
                                    mm.deserialize("<!i><#FB430A><b>ЛКМ</b> <gray>- <white>Запустить/Остановить ивент"),
                                    mm.deserialize("<!i><#FB430A><b>ПКМ</b> <gray>- <white>Активировать ивент"),
                                    mm.deserialize("<!i><#FB430A><b>СКМ</b> <gray>- <white>Телепорт к ивенту"),
                                    mm.deserialize("<!i><#FB430A><st>=                                   =")
                            ));
                        });
                    });

            builder.defaultItem(itemWrapper);
        });
        registerItem("static", builder -> {
            ItemWrapper itemWrapper;

            itemWrapper = ItemWrapper
                    .builder(Material.BEDROCK)
                    .displayName(mm.deserialize("<!i><#FB430A><b>⭐</b> <white>Статическая локация"))
                    .lore(List.of(
                            mm.deserialize("<!i><#FB430A><st>=                                   ="),
                            mm.deserialize("<!i>"+checkColor(vulcan.isStaticLocation())+"● <white>Сейчас: "+check(vulcan.isStaticLocation())),
                            mm.deserialize(""),
                            mm.deserialize("<!i><#FB430A><b>▶</b> <white>Нажмите чтобы переключить"),
                            mm.deserialize("<!i><#FB430A><st>=                                   =")
                    ))
                    .build();

            builder.slots(20)
                    .defaultClickHandler(staticLocation);

            builder.defaultItem(itemWrapper);
        });
        registerItem("edit", builder -> {
            builder.slots(21)
                    .defaultItem(ItemWrapper.builder(Material.CHEST, SerializerType.MINI_MESSAGE)
                            .displayName("<!i><#FB430A><b>⭐</b> <white>Настроить все предметы")
                            .lore(List.of(
                                    "<!i><#FB430A><st>=                                   =",
                                    "",
                                    "<!i><#FB430A><b>▶</b> <white>Нажмите чтобы настроить",
                                    "<!i><#FB430A><st>=                                   ="
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
                            .displayName(mm.deserialize("<!i><#FB430A><b>⭐</b> <white>Изменить мир"))
                            .lore(List.of(
                                    mm.deserialize("<!i><#FB430A><st>=                                   ="),
                                    mm.deserialize("<!i><red>ℹ <gray><i>Изменить мир в котором будет спавнится вулкан"),
                                    mm.deserialize(""),
                                    mm.deserialize("<!i><green>● <white>Текущий мир: <green>"+vulcan.getSpawnWorld()),
                                    mm.deserialize(""),
                                    mm.deserialize("<!i><#FB430A><b>▶</b> <white>Нажмите чтобы настроить"),
                                    mm.deserialize("<!i><#FB430A><st>=                                   =")
                            ))
                            .build())
                    .defaultClickHandler((event, controller) -> {
                        event.setCancelled(true);

                       new WorldMenu(player, vulcan, plugin).open(player);

                    });
        });

        registerItem("pregenerated", builder -> {
            builder.slots(24).defaultItem(ItemWrapper.builder(Material.GRASS_BLOCK)
                    .displayName(mm.deserialize("<!i><#FB430A><b>⭐</b> <white>Заранее сгенерированные локации"))
                    .lore(List.of(
                            mm.deserialize("<#FB430A><st>=                                   ="),
                            mm.deserialize("<!i>"+checkColor(vulcan.isUsePreGeneratedLocations())+"● <white>Использовать: "+check(vulcan.isUsePreGeneratedLocations())),
                            mm.deserialize(""),
                            mm.deserialize("<!i><#FB430A><b>●</b> <white>Сгенерировано локаций: <#FB430A>"+plugin.getPreLocationGenerator().getLocationsAmount(vulcan.getType())),
                            mm.deserialize(""),
                            mm.deserialize("<!i><#FB430A><b>ЛКМ</b> <gray>- <white>Начать поиск 50 локаций"),
                            mm.deserialize("<!i><#FB430A><b>ПКМ</b> <gray>- <white>Отменить поиск"),
                            mm.deserialize("<!i><#FB430A><b>СКМ</b> <gray>- <white>Переключить <green>true/<red>false"),
                            mm.deserialize("<!i><#FB430A><st>=                                   =")

                    ))
                    .build());
            builder.defaultClickHandler((event, controller) -> {

                event.setCancelled(true);

                switch (event.getClick()) {
                    case LEFT -> {
                        if (!plugin.getPreLocationGenerator().getSessions().contains(player.getUniqueId())) {
                            player.sendMessage(mm.deserialize("<green>Начинаю искать локации..."));
                            plugin.getPreLocationGenerator().getSessions().add(player.getUniqueId());
                            plugin.getPreLocationGenerator().startSearch(player, vulcan.getType(), 50, this);
                        } else {
                            player.sendMessage(mm.deserialize("<red>У вас уже есть активная сессия поиска локаций"));
                        }

                    }
                    case RIGHT -> {
                        if (plugin.getPreLocationGenerator().getSessions().contains(player.getUniqueId())) {
                            plugin.getPreLocationGenerator().getSessions().remove(player.getUniqueId());
                        } else {
                            player.sendMessage(mm.deserialize("<red>Нечего отменять :/"));
                        }
                    }
                    case MIDDLE -> {
                        vulcan.setUsePreGeneratedLocations(!vulcan.isUsePreGeneratedLocations());
                    }
                }

                controller.updateItems(wrapper -> {
                    wrapper.lore(List.of(
                            mm.deserialize("<#FB430A><st>=                                   ="),
                            mm.deserialize("<!i>"+checkColor(vulcan.isUsePreGeneratedLocations())+"● <!i><white>Использовать: "+check(vulcan.isUsePreGeneratedLocations())),
                            mm.deserialize(""),
                            mm.deserialize("<!i><#FB430A><b>●</b> <white>Сгенерировано локаций: <#FB430A>"+plugin.getPreLocationGenerator().getLocationsAmount(vulcan.getType())),
                            mm.deserialize(""),
                            mm.deserialize("<!i><#FB430A><b>ЛКМ</b> <gray>- <white>Начать поиск 50 локаций"),
                            mm.deserialize("<!i><#FB430A><b>ПКМ</b> <gray>- <white>Отменить поиск"),
                            mm.deserialize("<!i><#FB430A><b>СКМ</b> <gray>- <white>Переключить <green>true/<red>false"),
                            mm.deserialize("<!i><#FB430A><st>=                                   =")
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
                player.sendMessage(mm.deserialize("<gray>→ <white>Введите значение в чат <gray>[Текущее значение: "+vulcan.getMinPlayers()+"]"));
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
                player.sendMessage(mm.deserialize("<gray>→ <white>Введите значение в чат <gray>[Текущее значение: "+vulcan.getSpawnRadiusMin()+"]"));
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
                player.sendMessage(mm.deserialize("<gray>→ <white>Введите значение в чат <gray>[Текущее значение: "+vulcan.getSpawnRadiusMax()+"]"));
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
                player.sendMessage(mm.deserialize("<gray>→ <white>Введите значение в чат <gray>[Текущее значение: "+vulcan.getActivation()+"]"));
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
                player.sendMessage(mm.deserialize("<gray>→ <white>Введите значение в чат <gray>[Текущее значение: "+vulcan.getDuration()+"]"));
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
                player.sendMessage(mm.deserialize("<gray>→ <white>Введите значение в чат <gray>[Текущее значение: "+vulcan.getRegionSize()+"]"));
                region.put(player.getUniqueId(), vulcan);
                close(player);
            });
        });


    }


    public String check(boolean status){
        return status ? "<green>true" : "<red>false";
    }
    public String checkColor(boolean status){
        return status ? "<green>" : "<red>";
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
                player.sendMessage("Возникла ошибка, значение должно быть числом");
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
                player.sendMessage("Возникла ошибка, значение должно быть числом");
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
                player.sendMessage("Возникла ошибка, значение должно быть числом");
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
                player.sendMessage("Возникла ошибка, значение должно быть числом");
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
                player.sendMessage("Возникла ошибка, значение должно быть числом");
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
                player.sendMessage("Возникла ошибка, значение должно быть числом");
            }
            maxRadiusEditing.remove(player.getUniqueId());
            plugin.getVulcans().save(true);
            open(player);
            return;
        }
    }
}
