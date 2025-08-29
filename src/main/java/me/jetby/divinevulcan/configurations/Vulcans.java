package me.jetby.divinevulcan.configurations;

import lombok.Getter;
import me.jetby.divinevulcan.Main;
import me.jetby.divinevulcan.Vulcan;
import me.jetby.divinevulcan.utils.Logger;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Getter
public class Vulcans {

    private final Map<String, Vulcan> vulcans = new HashMap<>();
    private final Main plugin;
    private final File file;
    public Vulcans(Main plugin, File file) {
        this.plugin = plugin;
        this.file = file;
    }

    public void load() {

        masks.clear();

        File[] files = file.listFiles();
        if (!file.exists()) {
            if (file.mkdirs()) {
                File defaultFile = new File(file, "default.yml");
                if (!defaultFile.exists()) {
                    plugin.saveResource("types/default.yml", false);
                }
                FileConfiguration config = YamlConfiguration.loadConfiguration(defaultFile);
                loadVulcan(config, config.getString("id"));
                Logger.info("Файл types/"+config.getString("id")+".yml создан");
                return;
            }
        }

        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            loadVulcan(config, config.getString("id"));
            Logger.info("Файл types/"+config.getString("id")+".yml загружен");
        }



    }
    public record Mask(
            String id,
            Material material,
            String name,
            boolean enchanted
    ){}


    public void save(boolean async) {

        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, ()-> {
                for (String type : vulcans.keySet()) {
                    File typeFile = plugin.getFile("types/"+type+".yml");
                    Vulcan vulcan = vulcans.get(type);
                    FileConfiguration config = YamlConfiguration.loadConfiguration(typeFile);

                    try { config.save(typeFile); } catch (IOException e) { Logger.warn("Не удалось сохранить +"+type+"\n" + e); }

                    config.set("static-location.enable", vulcan.isStaticLocation());
                    config.set("spawn-world", vulcan.getSpawnWorld());
                    config.set("minPlayers", vulcan.getMinPlayers());
                    config.set("spawn-radius-min", vulcan.getSpawnRadiusMin());
                    config.set("spawn-radius-max", vulcan.getSpawnRadiusMax());
                    config.set("activation", vulcan.getActivation());
                    config.set("duration", vulcan.getDuration());
                    config.set("region.size", vulcan.getRegionSize());
                    config.set("use-pre-generated-locations", vulcan.isUsePreGeneratedLocations());

                    try { config.save(typeFile); } catch (IOException e) { Logger.warn("Не удалось сохранить +"+type+"\n" + e); }

                    Logger.info("Файл types/"+config.getString("id")+".yml сохранен");
                }
            });
        } else {
            for (String type : vulcans.keySet()) {
                File typeFile = plugin.getFile("types/"+type+".yml");
                Vulcan vulcan = vulcans.get(type);
                FileConfiguration config = YamlConfiguration.loadConfiguration(typeFile);

                try { config.save(typeFile); } catch (IOException e) { Logger.warn("Не удалось сохранить +"+type+"\n" + e); }

                config.set("static-location.enable", vulcan.isStaticLocation());
                config.set("spawn-world", vulcan.getSpawnWorld());
                config.set("minPlayers", vulcan.getMinPlayers());
                config.set("spawn-radius-min", vulcan.getSpawnRadiusMin());
                config.set("spawn-radius-max", vulcan.getSpawnRadiusMax());
                config.set("activation", vulcan.getActivation());
                config.set("duration", vulcan.getDuration());
                config.set("region.size", vulcan.getRegionSize());
                config.set("use-pre-generated-locations", vulcan.isUsePreGeneratedLocations());

                try { config.save(typeFile); } catch (IOException e) { Logger.warn("Не удалось сохранить +"+type+"\n" + e); }

                Logger.info("Файл types/"+config.getString("id")+".yml сохранен");
            }
        }
    }
    private final Map<String, Mask> masks = new HashMap<>();

    public void loadVulcan(FileConfiguration config, String id) {

        try {

            boolean mask = config.getBoolean("mask.enable", false);

            ConfigurationSection maskSection = config.getConfigurationSection("mask.items");
            if (maskSection == null) {
                mask = false;
            } else {
                for (String maskId : maskSection.getKeys(false)) {
                    ConfigurationSection mask2 = maskSection.getConfigurationSection(maskId);
                    if (mask2 == null) continue;

                    Material material = Material.valueOf(mask2.getString("material", "STONE"));
                    String name = mask2.getString("name", "Default Mask");
                    boolean enchanted = mask2.getBoolean("enchanted", false);
                    masks.put(maskId, new Mask(maskId, material, name, enchanted));
                }
            }

            int minPlayers = config.getInt("minPlayers");
            int activation = config.getInt("activation");
            int duration = config.getInt("duration");
            Set<Material> materialSet = EnumSet.noneOf(Material.class);
            for (String materialName : config.getStringList("blacklist-materials")) {
                materialSet.add(Material.getMaterial(materialName));
            }

            ConfigurationSection events = config.getConfigurationSection("events");
            List<String> onSpawnEvents = events.getStringList("onSpawn");
            List<String> onStartEvents = events.getStringList("onStart");
            List<String> onEndEvents = events.getStringList("onEnd");

            List<String> hologramLines = config.getStringList("hologram");
            List<String> hologramActivatedLines = config.getStringList("hologram-activated");
            String spawnWorld = config.getString("spawn-world");
            int spawnRadiusMin = config.getInt("spawn-radius-min");
            int spawnRadiusMax = config.getInt("spawn-radius-max");
            double radius = config.getDouble("radius");
            int period = config.getInt("period");
            int pickupDelay = config.getInt("pickup-delay");

            Map<String, BossBars> bossBarsMap = new HashMap<>();

            ConfigurationSection bossBars = config.getConfigurationSection("bossBars");
            for (String bossBarId : bossBars.getKeys(false)) {
                ConfigurationSection bossBar = bossBars.getConfigurationSection(bossBarId);
                String bossBarTitle = bossBar.getString("title", "");
                int bossBarDuration = bossBar.getInt("duration", -1);

                BossBar.Color bossBarColor = BossBar.Color.valueOf(bossBar.getString("Color", "BLUE"));
                BossBar.Overlay bossBarStyle = BossBar.Overlay.valueOf(bossBar.getString("Style", "PROGRESS"));

                bossBarsMap.put(bossBarId, new BossBars(bossBarId, bossBarTitle, bossBarColor, bossBarStyle, bossBarDuration ));
                Logger.info("true -"+bossBarId);
            }


            ConfigurationSection particle = config.getConfigurationSection("particle");
            boolean particleEnabled = particle.getBoolean("enable", false);
            boolean fired = particle.getBoolean("fired", false);
            String sound = particle.getString("sound").toUpperCase();
            double soundPitch = particle.getDouble("soundPitch");
            double soundVolume = particle.getDouble("soundVolume");
            String particleType = particle.getString("particle-type");
            int particleAmount = particle.getInt("particle-amount");
            double minY = particle.getDouble("min-y");
            double maxY = particle.getDouble("max-y");
            double minHorizontalSpeed = particle.getDouble("min-horizontal-speed");
            double maxHorizontalSpeed = particle.getDouble("max-horizontal-speed");

            boolean region = config.getBoolean("region.enable", false);
            int regionSize = config.getInt("region.size", 0);
            List<String> flags = config.getStringList("region.flags");

            boolean schem = config.getBoolean("schem.enable", false);
            String schemFile = config.getString("schem.file");
            boolean schemIgnoreAirBlocks = config.getBoolean("schem.ignore-air-blocks", false);
            int schemOffsetX = config.getInt("schem.offsets-x", 0);
            int schemOffsetY = config.getInt("schem.offsets-y", 0);
            int schemOffsetZ = config.getInt("schem.offsets-z", 0);

            double holoOffsetX = config.getDouble("holo-offsets.x", 0);
            double holoOffsetY = config.getDouble("holo-offsets.y", 0);
            double holoOffsetZ = config.getDouble("holo-offsets.z", 0);

            boolean staticLocation = config.getBoolean("static-location.enable", false);
            String staticLocationWorld = config.getString("static-location.world");
            int staticLocationX = config.getInt("static-location.x");
            int staticLocationY = config.getInt("static-location.y");
            int staticLocationZ = config.getInt("static-location.z");

            boolean usePreGeneratedLocations = config.getBoolean("use-pre-generated-locations", false);

            boolean isPhase = config.getBoolean("phase.enable", false);
            ConfigurationSection phases = config.getConfigurationSection("phase.phases");
            Map<Integer, List<String>> phase = new HashMap<>();

            if (isPhase) {
                if (phases==null) {
                    Logger.error("Секция phase.phases не была найдена.");
                    return;
                } else{
                    for (String p : phases.getKeys(false)) {
                        phase.put(Integer.parseInt(p), phases.getStringList(p));
                    }
                }
            }



            Vulcan vulcan = new Vulcan(
                    plugin,
                    mask,
                    masks,
                    id,
                    minPlayers,
                    duration,
                    activation,
                    materialSet,
                    onSpawnEvents,
                    onStartEvents,
                    onEndEvents,
                    hologramLines,
                    hologramActivatedLines,
                    spawnWorld,
                    spawnRadiusMin,
                    spawnRadiusMax,
                    radius,
                    period,
                    pickupDelay,
                    particleEnabled,
                    fired,
                    sound,
                    soundPitch,
                    soundVolume,
                    particleType,
                    particleAmount,
                    minY,
                    maxY,
                    minHorizontalSpeed,
                    maxHorizontalSpeed,
                    region,
                    regionSize,
                    flags,
                    schem,
                    schemFile,
                    schemIgnoreAirBlocks,
                    schemOffsetX,
                    schemOffsetY,
                    schemOffsetZ,
                    holoOffsetX,
                    holoOffsetY,
                    holoOffsetZ,
                    staticLocation,
                    staticLocationWorld,
                    staticLocationX,
                    staticLocationY,
                    staticLocationZ,
                    usePreGeneratedLocations,
                    isPhase,
                    phase,
                    bossBarsMap,
                    null
            );

            vulcans.put(id, vulcan);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
