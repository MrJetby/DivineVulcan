package me.jetby.divinevulcan;

import com.jodexindustries.jguiwrapper.common.JGuiInitializer;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import lombok.Getter;
import lombok.Setter;
import me.jetby.divinevulcan.commands.AdminCommands;
import me.jetby.divinevulcan.configurations.Config;
import me.jetby.divinevulcan.configurations.Items;
import me.jetby.divinevulcan.configurations.Vulcans;
import me.jetby.divinevulcan.gui.Menu;
import me.jetby.divinevulcan.listeners.ItemPickup;
import me.jetby.divinevulcan.utils.*;
import me.jetby.divinevulcan.worldGuardHook.Schematic;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;


@Getter
public final class Main extends JavaPlugin {

    private Placeholders placeholderExpansion;
    @Setter private Vulcans vulcans = null;
    public static Map<String, Vulcan> activeVulcans = new HashMap<>();
    @Setter private Config cfg;
    private Menu menu;
    @Setter private Items items;
    private Schematic schematic;
    private WorldEditPlugin worldEditPlugin;
    private WorldEdit worldEdit;
    @Setter private FormatTime formatTime;
    @Setter private AutoStart autoStart;
    private final Actions actions = new Actions(this);
    @Setter private LocationGenerator locationGenerator;
    @Setter private PreLocationGenerator preLocationGenerator;
    @Setter private BossBar bossBar;
    @Setter private boolean pluginEnabled = false;

    @Override
    public void onEnable() {

        pluginEnabled = false;

        JGuiInitializer.init(this);

        cfg = new Config();
        cfg.load(getFileConfiguration( "config.yml"));

        items = new Items(getFile("items.yml"));
        items.load();

            if (!checkDependencies()) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            preLocationGenerator = new PreLocationGenerator(this, getFile("locations.yml"));
            preLocationGenerator.load();

            locationGenerator = new LocationGenerator(this, preLocationGenerator);

            new Metrics(this, 25221);

            getCommand("divinevulcan").setExecutor(new AdminCommands(this));


            getServer().getPluginManager().registerEvents(new ItemPickup(), this);

            File types = new File(getDataFolder(), "types");
            vulcans = new Vulcans(this, types);
            vulcans.load();

            menu = new Menu(this);

            File schematics = new File(getDataFolder(), "schematics");

            if (!schematics.exists())  {
                schematics.mkdirs();
                saveDefaultSchematic("schematics/default.schem");
            }

            autoStart = new AutoStart(this);

            formatTime = new FormatTime(this);

            bossBar = new BossBar(this);
    }

    public FileConfiguration getFileConfiguration(String fileName) {
        File file = new File(getDataFolder().getAbsolutePath(), fileName);
        if (!file.exists()) {
            saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public File getFile(String fileName) {
        File file = new File(getDataFolder().getAbsolutePath(), fileName);
        if (!file.exists()) {
            saveResource(fileName, false);
        }
        return file;
    }


    private boolean checkDependencies() {

        int dependencyCount = 0;

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled()) {
            placeholderExpansion = new Placeholders(this);
            placeholderExpansion.register();
            Logger.success("✔ PlaceholderAPI подключён!");
            dependencyCount++;
        } else {
            Logger.warn("❌ PlaceholderAPI не найден!");
        }
        if (Bukkit.getPluginManager().getPlugin("WorldEdit") != null && Bukkit.getPluginManager().getPlugin("WorldEdit").isEnabled()) {
            worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
            worldEdit = worldEditPlugin.getWorldEdit();
            schematic = new Schematic(this);
            Logger.success("✔ WorldEdit подключён!");
            dependencyCount++;
        } else {
            Logger.warn("❌ WorldEdit не найден!");
        }
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            Logger.success("✔ WorldGuard подключён!");
            dependencyCount++;
        } else {
            Logger.warn("❌ WorldGuard не найден!");
        }
        if (Bukkit.getPluginManager().getPlugin("DecentHolograms") != null) {
            Logger.success("✔ DecentHolograms подключён!");
            dependencyCount++;
        } else {
            Logger.warn("❌ DecentHolograms не найден!");
        }

        if (dependencyCount == 4) {
            pluginEnabled = true;
            Logger.success("✔ Все зависимости подключены!");
        } else {
            Logger.warn("");
            Logger.warn("❌ Некоторые зависимости не найдены! Плагин не может быть запущен без них.");
            Logger.warn("");
        }

        return dependencyCount == 4;
    }

    @Override
    public void onDisable() {
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }
        activeVulcans.values().forEach(Vulcan::stop);
        if (items != null) {
            items.save();
        }
        if (preLocationGenerator!=null) {
            preLocationGenerator.getSessions().clear();
            preLocationGenerator.save();
        }

        Logger.warn("Плагин отключён");
    }

    public void saveDefaultSchematic(String path) {
        File schemFile = new File(getDataFolder(), path);

        if (!schemFile.exists()) {
            getDataFolder().mkdirs();

            try (InputStream in = getResource(path)) {
                if (in == null) {
                    getLogger().warning("Не найден " + path + " в jar плагина!");
                    return;
                }
                Files.copy(in, schemFile.toPath());
                getLogger().info("Схематика " + path + " скопирована в папку плагина.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
