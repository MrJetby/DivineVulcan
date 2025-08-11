package me.jetby.divinevulcan.commands;

import me.jetby.divinevulcan.LocationGenerator;
import me.jetby.divinevulcan.Main;
import me.jetby.divinevulcan.PreLocationGenerator;
import me.jetby.divinevulcan.Vulcan;
import me.jetby.divinevulcan.configurations.Config;
import me.jetby.divinevulcan.configurations.Items;
import me.jetby.divinevulcan.configurations.Vulcans;
import me.jetby.divinevulcan.utils.AutoStart;
import me.jetby.divinevulcan.utils.BossBar;
import me.jetby.divinevulcan.utils.FormatTime;
import me.jetby.divinevulcan.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static me.jetby.divinevulcan.Main.activeVulcans;
import static me.jetby.divinevulcan.utils.Hex.hex;

public class AdminCommands implements CommandExecutor, TabCompleter {

    private final Main plugin;
    public AdminCommands(Main plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        if (!plugin.getLicense().getMashonka().equalsIgnoreCase("JUST_BELIEVE_ME_THAT_WORKS_BRO")) {
            return true;
        }

        if (args.length==0) {


                if (sender instanceof Player p) {
                    p.sendMessage(hex("&#FB3D14&lDivineVulcan &7» &6/" + cmd.getName() + " menu &7- &fНастроить предметы"));
                    p.sendMessage(hex("&#FB3D14&lDivineVulcan &7» &6/" + cmd.getName() + " start <id> &7- &fНачать ивент"));
                    p.sendMessage(hex("&#FB3D14&lDivineVulcan &7» &6/" + cmd.getName() + " tp <id> &7- &fТелепорт на активный ивент"));
                    p.sendMessage(hex("&#FB3D14&lDivineVulcan &7» &6/" + cmd.getName() + " stop <id> &7- &fЗавершить активный ивент"));
                }



            return true;
        }

        if (args[0].equalsIgnoreCase("test")) {
            plugin.getBossBar().createBossBar(args[1], plugin.getVulcans().getVulcans().get(args[2]));
            plugin.getBossBar().show(args[1], new ArrayList<>(List.of( sender )));
            return true;
        }
        if (!plugin.getLicense().getMashonka().equalsIgnoreCase("JUST_BELIEVE_ME_THAT_WORKS_BRO")) {
            return true;
        }
        if (args[0].equalsIgnoreCase("tp")) {
                if (sender instanceof Player player) {
                    if (!player.hasPermission("divinevulcan.admin")) return true;

                    if (args.length != 2) {
                        sender.sendMessage(hex("&#FB3D14&lDivineVulcan &7» &fУкажите имя &6/" + cmd.getName() + " tp <id>"));
                        return true;
                    }
                    String name = args[1];
                    Vulcan block = activeVulcans.get(name);


                    if (block == null) {
                        sender.sendMessage(hex("&#FB3D14&lDivineVulcan &7» &7Данный тип не найден"));
                        return true;
                    }
                    if (block.getLocation() == null) {
                        sender.sendMessage(hex("&#FB3D14&lDivineVulcan &7» &fПодождите, я ищу локацию..."));
                        return true;
                    }

                    player.teleport(block.getLocation());

                } else {
                    sender.sendMessage("Команда только для игрока!");
                }


        }
        if (args[0].equalsIgnoreCase("start")) {
                if (!sender.hasPermission("divinevulcan.admin")) return true;
                if (args.length < 2) {
                    sender.sendMessage(hex("&#FB3D14&lDivineVulcan &7» &fУкажите имя &6/" + cmd.getName() + " start <id>"));
                    return true;
                }

                String name = args[1];
                if (!plugin.getVulcans().getVulcans().containsKey(name)) {
                    sender.sendMessage(hex("&#FB3D14&lDivineVulcan &7» &7Ивент с таким названием не найден."));
                    return true;
                }
                if (activeVulcans.containsKey(name)) {
                    sender.sendMessage(hex("&#FB3D14&lDivineVulcan &7» &7Ивент уже активен."));
                    return true;
                }
                Vulcan block = activeVulcans.get(name);
                if (block!=null && block.getLocation() == null) {
                    sender.sendMessage(hex("&#FB3D14&lDivineVulcan &7» &fПодождите, я ищу локацию..."));
                    return true;
                }


                sender.sendMessage(hex("&#FB3D14&lDivineVulcan &7» &7Попытаюсь вызвать ивент..."));
                Vulcan vulcan = plugin.getVulcans().getVulcans().get(name);
                activeVulcans.put(name, vulcan);

                return true;


        }
        if (args[0].equalsIgnoreCase("menu")) {
                if (!sender.hasPermission("divinevulcan.admin")) return true;
                if (sender instanceof Player player) plugin.getMenu().open(player);


            return true;
        }

        if (!plugin.getLicense().getMashonka().equalsIgnoreCase("JUST_BELIEVE_ME_THAT_WORKS_BRO")) {
            return true;
        }
        if (args[0].equals("stop")) {
                if (!sender.hasPermission("divinevulcan.admin")) return true;
                if (args.length != 2) {
                    sender.sendMessage(hex("&#FB3D14&lDivineVulcan &7» &fУкажите имя &6/" + cmd.getName() + " stop <id>"));
                    return true;
                }
                String name = args[1];
                Vulcan block = activeVulcans.get(name);
                if (block == null) {
                    sender.sendMessage(hex("&#FB3D14&lDivineVulcan &7» &7Данный тип не найден"));
                    return true;
                }
                block.stop();
            return true;


        }
        if (args[0].equals("reload")) {
                if (!sender.hasPermission("divinevulcan.admin")) return true;

            long startTime = System.currentTimeMillis();
            try {
                if (plugin.getItems() != null) {
                    plugin.getItems().save();
                }
                if (plugin.getPreLocationGenerator()!=null) {
                    plugin.getPreLocationGenerator().getSessions().clear();
                    plugin.getPreLocationGenerator().save();
                }

                Config cfg = new Config();
                cfg.load(plugin.getFileConfiguration( "config.yml"));
                plugin.setCfg(cfg);

                Items items = new Items(plugin.getFile("items.yml"));
                items.load();
                plugin.setItems(items);

                if (Bukkit.getPluginManager().getPlugin("TreexStudio") == null) {
                    Logger.error("Плагин TreexStudio не был найден, плагин не может без него работать");
                    plugin.setPluginEnabled(false);
                    return true;
                }
                PreLocationGenerator preLocationGenerator = new PreLocationGenerator(plugin, plugin.getFile("locations.yml"));
                preLocationGenerator.load();
                plugin.setPreLocationGenerator(preLocationGenerator);

                LocationGenerator locationGenerator = new LocationGenerator(plugin, preLocationGenerator);
                plugin.setLocationGenerator(locationGenerator);

                File types = new File(plugin.getDataFolder(), "types");
                Vulcans vulcans = new Vulcans(plugin, types);
                vulcans.load();
                plugin.setVulcans(vulcans);

                File schematics = new File(plugin.getDataFolder(), "schematics");

                if (!schematics.exists())  {
                    schematics.mkdirs();
                    plugin.saveDefaultSchematic("schematics/default.schem");

                }

                plugin.setAutoStart(new AutoStart(plugin));
                plugin.setFormatTime(new FormatTime(plugin));

                long elapsed = System.currentTimeMillis() - startTime;
                sender.sendMessage("§aУспешная перезагрузка, всего заняло "+elapsed+"мс");
            } catch (Exception e) {
                sender.sendMessage("§cПроизошла ошибка "+e.getMessage());
            }

                return true;



        }
        return false;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("goldblock.admin")) {
            return null;
        }

        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("divinevulcan.admin")) return null;
        if (args.length == 1) {
            completions.add("reload");
            completions.add("start");
            completions.add("menu");
            completions.add("tp");
            completions.add("stop");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("start")) {
                if (plugin.getVulcans().getVulcans() != null) {
                    completions.addAll(plugin.getVulcans().getVulcans().keySet());
                }
            }
            if (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("stop")) {
                completions.addAll(activeVulcans.keySet());
            }
        }

        String input = args[args.length - 1].toLowerCase();
        completions.removeIf(option -> !option.toLowerCase().startsWith(input));

        return completions;
    }
}
