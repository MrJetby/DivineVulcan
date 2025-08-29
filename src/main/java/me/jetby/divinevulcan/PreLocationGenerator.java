package me.jetby.divinevulcan;

import lombok.Getter;
import me.jetby.divinevulcan.gui.JMenu;
import me.jetby.divinevulcan.utils.LocationHandler;
import me.jetby.divinevulcan.utils.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static me.jetby.divinevulcan.utils.Hex.hex;

@Getter
public class PreLocationGenerator {

    private final Main plugin;
    private final YamlConfiguration configuration;
    private final File file;

    public PreLocationGenerator(Main plugin, File file) {
        this.plugin = plugin;
        this.file = file;
        this.configuration = YamlConfiguration.loadConfiguration(file);
    }

    private final Map<String, List<Location>> locations = new HashMap<>();
    private final Set<UUID> sessions = new HashSet<>();

    public int getLocationsAmount(String type) {
        if (locations.get(type)==null || locations.get(type).isEmpty()) {
            return 0;
        }
        return locations.get(type).size();
    }

    //
    // крч, это для поиска локаций, чтобы засунуть их в locations
    //
    public void startSearch(Player player, String type, int amount, JMenu jMenu) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
        List<Location> locationList;
        if (locations.isEmpty()) {
            locationList = new ArrayList<>();
        } else {
            locationList = locations.get(type);
        }


        for (int i = 1; i <= amount; i++) {

            if (!sessions.contains(player.getUniqueId())) {
                player.sendMessage(hex("&cПоиск локаций отменена"));
                break;
            }

            Vulcan vulcan = plugin.getVulcans().getVulcans().get(type);
                Location location = plugin.getLocationGenerator().
                        getRandomLocation(vulcan);

                if (locationList.contains(location)) {
                    player.sendMessage(hex("&cНеудачно. (Локация уже сохранена) "+i));
                    continue;
                }
                if (location==null) {
                    player.sendMessage(hex("&cНеудачно. (Локация вернула null) "+i));
                    continue;
                }
                for (Location loc : locationList) {
                    if (location.distance(loc)>vulcan.getRegionSize()) continue;
                    player.sendMessage(hex("&cНеудачно. (Дистанция локации задевает одну из существующих)"+i));
                }

                locationList.add(location);
                player.sendMessage(hex("&aУдачно. "+i));
                jMenu.getController("pregenerated").get().updateItemWrappers(wrapper -> {
                    wrapper.lore(List.of(
                            "<#FB430A><st>=                                   =",
                            "<!i>"+jMenu.checkColor(vulcan.isUsePreGeneratedLocations())+"● <white>Использовать: "+jMenu.check(vulcan.isUsePreGeneratedLocations()),
                            "",
                            "<!i><#FB430A><b>●</b> <white>Сгенерировано локаций: <#FB430A>"+getLocationsAmount(vulcan.getType()),
                            "",
                            "<!i><#FB430A><b>ЛКМ</b> <gray>- <white>Начать поиск 50 локаций",
                            "<!i><#FB430A><b>ПКМ</b> <gray>- <white>Отменить поиск",
                            "<!i><#FB430A><b>СКМ</b> <gray>- <white>Переключить <green>true/<red>false",
                            "<!i><#FB430A><st>=                                   ="
                    ));
                });
        }
        sessions.remove(player.getUniqueId());
        locations.put(type, locationList);
        });

    }


    public void load() {
        try {
            ConfigurationSection allLocations = configuration.getConfigurationSection("locations");
            if (allLocations==null) return;
            for (String type : allLocations.getKeys(false)) {
                ConfigurationSection typeSection = allLocations.getConfigurationSection(type);
                List<Location> locationList = new ArrayList<>();
                if (typeSection==null) continue;
                for (String xyz : typeSection.getKeys(false)) {
                    ConfigurationSection location = typeSection.getConfigurationSection(xyz);
                    if (location==null) continue;
                    if (location.getLocation("location")==null) continue;
                    locationList.add(location.getLocation("location"));
                }
                locations.put(type, locationList);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void save() {

        for (String type : locations.keySet()) {
            List<Location> locationList = locations.get(type);

            for (Location location : locationList) {
                String locationStr = LocationHandler.serializeLocation(location);
                configuration.set("locations."+type+"."+locationStr+".location", location);
            }
        }
        try {
            configuration.save(file);
        } catch (IOException e) {
            Logger.warn("Не удалось сохранить файл (items.yml)\n" + e);
        }
    }
}
