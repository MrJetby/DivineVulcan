package me.jetby.divinevulcan.utils;


import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.jetby.divinevulcan.Main;
import me.jetby.divinevulcan.Vulcan;
import org.antlr.v4.runtime.misc.NotNull;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import static me.jetby.divinevulcan.Main.activeVulcans;

@RequiredArgsConstructor
public class Placeholders extends PlaceholderExpansion {

    private final Main plugin;

    @Override
    public @NotNull String getAuthor() {
        return "treexstudio";
    }
    @Override
    public boolean persist() {
        return true;
    }
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
    @Override
    public String getIdentifier() {

        return "divinevulcan";
    }
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {


        if (identifier.equalsIgnoreCase("time_to_start")) {
            return String.valueOf(plugin.getAutoStart().getTime());
        }
        if (identifier.equalsIgnoreCase("time_to_start_string") || identifier.equalsIgnoreCase("time_to_start_format")) {
            return String.valueOf(plugin.getFormatTime().stringFormat(plugin.getAutoStart().getTime()));
        }

        if (identifier.startsWith("check_")) {
            String key = identifier.replace("check_", "");
            if (activeVulcans.containsKey(key)) {
                return "true";
            }
            return "false";
        }

        if (identifier.startsWith("x_")) {
            String key = identifier.replace("x_", "");
            Vulcan vulcan = activeVulcans.get(key);
            if (vulcan != null) {
                if (vulcan.getLocation() != null) {
                    Location location = vulcan.getLocation();
                    return String.valueOf(location.getBlockX());
                } else {
                    return "локация не найдена";
                }

            } else {
                return "none";

            }
        }
        if (identifier.startsWith("y_")) {
            String key = identifier.replace("y_", "");
            Vulcan vulcan = activeVulcans.get(key);
            if (vulcan != null) {
                if (vulcan.getLocation() != null) {
                    Location location = vulcan.getLocation();
                    return String.valueOf(location.getBlockY());
                } else {
                    return "локация не найдена";
                }

            } else {
                return "none";

            }
        }
        if (identifier.startsWith("z_")) {
            String key = identifier.replace("z_", "");
            Vulcan vulcan = activeVulcans.get(key);
            if (vulcan != null) {
                if (vulcan.getLocation() != null) {
                    Location location = vulcan.getLocation();
                    return String.valueOf(location.getBlockZ());
                } else {
                    return "локация не найдена";
                }

            } else {
                return "none";

            }
        }



            return null;
    }
}
