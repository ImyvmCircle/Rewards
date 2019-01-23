package com.imyvm.Rewards;

import com.imyvm.Rewards.Commands.Commands;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Reward extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Acquisition");
    private FileConfiguration config = getConfig();
    private static List<String> rewards = new ArrayList<>();
    private static int time;
    private static int range;
    private static int mini;

    @Override
    public void onDisable() {
        log.info(String.format("[%s] Disabled Version %s", getDescription().getName(),
                getDescription().getVersion()));
    }

    @Override
    public void onEnable() {
        log.info(String.format("[%s] Enabled Version %s", getDescription().getName(), getDescription().getVersion()));

        config.addDefault("Rewards", rewards);
        config.addDefault("Life Time", 30000);
        config.addDefault("Range", 300);
        config.addDefault("Mini", 50);
        config.options().copyDefaults(true);
        saveConfig();

        rewards = config.getStringList("Rewards");
        time = config.getInt("Life Time");
        range = config.getInt("Range");
        mini = config.getInt("Mini");

        getCommand("rw").setExecutor(new Commands(this));

    }

    public static List<String> getRewards() {
        return rewards;
    }

    public static int getTime(){
        return time;
    }

    public static int getRange(){
        return range;
    }

    public static int getMini(){
        return mini;
    }


}
