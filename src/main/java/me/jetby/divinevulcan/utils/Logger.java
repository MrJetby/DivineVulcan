package me.jetby.divinevulcan.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

@UtilityClass
public class Logger {

    public void warn(String message) {
        Bukkit.getConsoleSender().sendMessage("§7[§eDivineVulcan§7] §e"+ message);
    }
    public void info(String message) {
        Bukkit.getConsoleSender().sendMessage("§7[§aDivineVulcan§7] §f"+ message);
    }
    public void success(String message) {
        Bukkit.getConsoleSender().sendMessage("§7[§aDivineVulcan§7] §a"+ message);
    }
    public void error(String message) {
        Bukkit.getConsoleSender().sendMessage("§7[§cDivineVulcan§7] §c"+ message);
    }
    public void msg(String message) {
        Bukkit.getConsoleSender().sendMessage("§7[§6DivineVulcan§7] §f"+ message);
    }
}