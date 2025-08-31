package me.jetby.divinevulcan.utils;

import me.jetby.divinevulcan.Main;
import me.jetby.divinevulcan.Vulcan;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

import static me.jetby.divinevulcan.Main.activeVulcans;

public class Actions {
    private final Main plugin;
    public Actions(Main plugin) {
        this.plugin = plugin;
    }


    public void execute(String id, Location location, List<String> commands, Vulcan vulcan) {
        executeWithDelay(id, location, commands, 0, vulcan);
    }
    private void executeWithDelay(String id, Location location, List<String> commands, int index, Vulcan vulcan) {
        if (index >= commands.size()) return;

        String command = commands.get(index);
        String[] args = command.split(" ");
        String withoutCMD = command
                .replace(args[0] + " ", "")
                .replace("{world}", vulcan.getSpawnLocation().getWorld().getName())
                .replace("{x}", String.valueOf(vulcan.getSpawnLocation().getBlockX()))
                .replace("{y}", String.valueOf(vulcan.getSpawnLocation().getBlockY()))
                .replace("{z}", String.valueOf(vulcan.getSpawnLocation().getBlockZ()));

        if (args[0].equalsIgnoreCase("[DELAY]")) {
            int delayTicks = Integer.parseInt(args[1]);

            Bukkit.getScheduler().runTaskLater(
                    plugin, () -> executeWithDelay(id, location, commands, index+1, vulcan), delayTicks
            );
            return;
        }

        switch (args[0].toUpperCase()) {
            case "[CREATE_BOSSBAR]":
                plugin.getBossBar().createBossBar(withoutCMD, plugin.getVulcans().getVulcans().get(id));
                break;
            case "[SHOW_BOSSBAR]": {
                List<Audience> audiences = new ArrayList<>(Bukkit.getOnlinePlayers());
                plugin.getBossBar().show(withoutCMD, audiences);
                break;
            }
            case "[SHOW_BOSSBAR_NEAR]": {
                if (activeVulcans.containsKey(id)) {
                    String bossbarId = args[1];
                    int radius = Integer.parseInt(args[2]);
                    plugin.getBossBar().show(bossbarId, location, radius);
                } else {
                    Logger.warn("Нету активного ивента чтобы показать [SHOW_BOSSBAR_NEAR]");
                }
                break;
            }
            case "[REMOVE_BOSSBAR_NEAR]": {
                if (activeVulcans.containsKey(id)) {
                    String bossbarId = args[1];
                    Bukkit.getScheduler().cancelTask(plugin.getBossBar().getDatas().get(bossbarId).getNearTask());
                } else {
                    Logger.warn("Нету активного ивента чтобы показать [SHOW_BOSSBAR_NEAR]");
                }
                break;
            }


            case "[REMOVE_BOSSBAR]": {
                List<Audience> audiences = new ArrayList<>(Bukkit.getOnlinePlayers());
                plugin.getBossBar().remove(withoutCMD, audiences);
                break;
            }

            case "[DELETE_BOSSBAR]":
                plugin.getBossBar().deleteBossBar(withoutCMD);
                break;
            case "[MSG]", "[MESSAGE]", "[MESSAGE_ALL]", "[MSG_ALL]":
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendMessage(TextUtil.colorize(withoutCMD));
                }
                break;

            case "[MSG_NEAR]":
                double msgRadius = getRadiusFromArgs(args);
                for (Player player : location.getWorld().getPlayers()) {
                    if (player.getLocation().distance(location) <= msgRadius) {
                        player.sendMessage(TextUtil.colorize(withoutCMD.replace("--radius:" + (int) msgRadius, "").trim()));
                    }
                }
                break;

            case "[TITLE]":
                for (Player player : Bukkit.getOnlinePlayers()) {

                    sendTitleToPlayers(player, withoutCMD, args);
                }
                break;

            case "[TITLE_NEAR]":
                double titleRadius = getRadiusFromArgs(args);
                for (Player player : location.getWorld().getPlayers()) {
                    if (player.getLocation().distance(location) <= titleRadius) {
                        sendTitleToPlayers(player, withoutCMD.replace("--radius:" + (int) titleRadius, "").trim(), args);
                    }
                }
                break;

            case "[CONSOLE]":
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), TextUtil.colorize(withoutCMD));
                break;

            case "[DROP]":
                plugin.getVulcans().getVulcans().get(id).dropItem(Integer.parseInt(withoutCMD));
                break;

            case "[DROP_CLEAR]":
                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (entity instanceof Item item && item.hasMetadata("originalItem")) {
                            item.remove();
                        }
                    }
                }
                break;

            case "[SOUND]":
                for (Player player : Bukkit.getOnlinePlayers()) {
                    playSound(player, location, withoutCMD);
                }
                break;

            case "[SOUND_NEAR]":
                String[] soundArgs = withoutCMD.split(" ");
                double soundRadius = Double.parseDouble(soundArgs[soundArgs.length - 1]);
                String soundParams = withoutCMD.replace(" " + soundArgs[soundArgs.length - 1], "");
                for (Player player : location.getWorld().getPlayers()) {
                    if (player.getLocation().distance(location) <= soundRadius) {
                        playSound(player, location, soundParams);
                    }
                }
                break;

            case "[EFFECT]":
                for (Player player : Bukkit.getOnlinePlayers()) {
                    applyEffect(player, withoutCMD);
                }
                break;

            case "[EFFECT_NEAR]":
                String[] effArgs = withoutCMD.split(" ");
                double effRadius = Double.parseDouble(effArgs[effArgs.length - 1]);
                String effParams = withoutCMD.replace(" " + effArgs[effArgs.length - 1], "");
                for (Player player : location.getWorld().getPlayers()) {
                    if (player.getLocation().distance(location) <= effRadius) {
                        applyEffect(player, effParams);
                    }
                }
                break;

            case "[DAMAGE_NEAR]":
                if (args.length >= 3) {
                    try {
                        double damage = Double.parseDouble(args[1]);
                        double radius = Double.parseDouble(args[2]);
                        for (Player player : location.getWorld().getPlayers()) {
                            if (player.getLocation().distance(location) <= radius) {
                                player.damage(damage);
                            }
                        }
                    } catch (NumberFormatException ignored) {}
                }
                break;
        }
        Bukkit.getScheduler().runTask(plugin, ()-> executeWithDelay(id, location, commands, index+1, vulcan));
    }

    private void playSound(Player player, Location location, String raw) {
        String[] parts = raw.split(";");
        try {
            Sound sound = Sound.valueOf(parts[0].toUpperCase());
            float volume = parts.length > 1 ? Float.parseFloat(parts[1]) : 1;
            float pitch = parts.length > 2 ? Float.parseFloat(parts[2]) : 1;
            player.playSound(location, sound, volume, pitch);
        } catch (Exception ignored) {}
    }

    private void applyEffect(Player player, String raw) {
        String[] parts = raw.split(";");
        try {
            PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
            int strength = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
            int duration = parts.length > 2 ? Integer.parseInt(parts[2]) : 1;
            if (type != null) player.addPotionEffect(new PotionEffect(type, duration * 20, strength));
        } catch (Exception ignored) {}
    }

    private void sendTitleToPlayers(Player player, String message, String[] args) {
        int fadeIn = 1;
        int stay = 3;
        int fadeOut = 1;
        for (String arg : args) {
            if (arg.startsWith("-fadeIn:")) {
                fadeIn = Integer.parseInt(arg.replace("-fadeIn:", ""));
            } else if (arg.startsWith("-stay:")) {
                stay = Integer.parseInt(arg.replace("-stay:", ""));
            } else if (arg.startsWith("-fadeOut:")) {
                fadeOut = Integer.parseInt(arg.replace("-fadeOut:", ""));
            }
        }
        String[] parts = TextUtil.colorize(message).split(";");
        String title = parts.length > 0 ? parts[0] : "";
        String sub = parts.length > 1 ? parts[1] : "";
        player.sendTitle(title, sub, fadeIn * 20, stay * 20, fadeOut * 20);
    }

    private static double getRadiusFromArgs(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--radius:")) {
                try {
                    return Double.parseDouble(arg.replace("--radius:", ""));
                } catch (NumberFormatException ignored) {}
            }
        }
        return 5;
    }
}
