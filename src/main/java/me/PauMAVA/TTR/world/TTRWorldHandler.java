package me.PauMAVA.TTR.world;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;

public class TTRWorldHandler {

    private final TTRCore plugin;
    private final World world;

    public TTRWorldHandler(TTRCore plugin, World world) {
        this.plugin = plugin;
        this.world = world;
    }

    public void setUpWorld() {
        if (world == null) return;

        // Reglas básicas para que no se haga de noche ni llueva solo
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);

        // Dificultad Normal para que haya hambre
        world.setDifficulty(Difficulty.NORMAL);

        // Configurar tiempo y clima inicial usando tu config
        configureTime();
        configureWeather();
    }

    public void configureTime() {
        if (world == null) return;
        // Leemos la hora del ConfigManager
        int time = plugin.getConfigManager().getMatchTime();
        world.setTime(time);
    }

    public void configureWeather() {
        if (world == null) return;
        // Leemos el clima del ConfigManager
        String weather = plugin.getConfigManager().getWeather();

        if (weather.equalsIgnoreCase("RAIN")) {
            world.setStorm(true);
            world.setThundering(false);
        } else if (weather.equalsIgnoreCase("THUNDER")) {
            world.setStorm(true);
            world.setThundering(true);
        } else {
            // CLEAR (Despejado)
            world.setStorm(false);
            world.setThundering(false);
        }
    }

    public void restoreDifficulty() {
        if (world != null) {
            // Al terminar la partida, lo ponemos en pacífico
            world.setDifficulty(Difficulty.PEACEFUL);
        }
    }
}