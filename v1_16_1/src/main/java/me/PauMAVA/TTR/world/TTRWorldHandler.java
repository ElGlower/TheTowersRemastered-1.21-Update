/*
 * TheTowersRemastered (TTR) - 1.21 Updated Version
 * Copyright (c) 2019-2021  Pau Machetti Vallverdú (Author Original)
 * Copyright (c) 2025       @StartCes, @Ripkyng1, @ElGlower (Updates)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.PauMAVA.TTR.world;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;

public class TTRWorldHandler {

    private TTRCore plugin;

    private World matchWorld;

    private Difficulty originalDifficulty;

    public TTRWorldHandler(TTRCore plugin, World matchWorld) {
        this.plugin = plugin;
        this.matchWorld = matchWorld;
        this.originalDifficulty = matchWorld.getDifficulty();
    }

    public void setUpWorld() {
        // --- CORRECCIÓN ANTI-CRASH ---
        Location lobbyLoc = plugin.getConfigManager().getLobbyLocation();

        if (lobbyLoc != null && lobbyLoc.getWorld() != null) {
            this.matchWorld.setSpawnLocation(lobbyLoc);
        } else {
            plugin.getLogger().warning("----------------------------------------------------");
            plugin.getLogger().warning("¡AVISO DE CONFIGURACIÓN!");
            plugin.getLogger().warning("La ubicación 'Lobby' en config.yml no es válida o es NULL.");
            plugin.getLogger().warning("Se usará el spawn por defecto del mundo para evitar el crash.");
            plugin.getLogger().warning("----------------------------------------------------");

            // Usamos el spawn actual del mundo como respaldo seguro
            this.matchWorld.setSpawnLocation(this.matchWorld.getSpawnLocation());
        }
    }

    public void configureWeather() {
        setWeatherCycle(false);
        String weatherType = plugin.getConfigManager().getWeather();

        // Protección simple por si weatherType es null
        if (weatherType == null) weatherType = "clear";

        if (weatherType.equalsIgnoreCase("rain") || weatherType.equalsIgnoreCase("thunder")) {
            this.matchWorld.setStorm(true);
            if (weatherType.equalsIgnoreCase("thunder")) {
                this.matchWorld.setThundering(true);
            }
        } else if (weatherType.equalsIgnoreCase("clear")) {
            this.matchWorld.setStorm(false);
            this.matchWorld.setThundering(false);
        }
    }

    public void configureTime() {
        setDayLightCycle(false);
        this.matchWorld.setTime(plugin.getConfigManager().getTime());
    }

    public void enableDayLightCycle() {
        setDayLightCycle(true);
    }

    public void enableWeatherCycle() {
        setWeatherCycle(true);
    }

    public void restoreDifficulty() {
        matchWorld.setDifficulty(originalDifficulty);
    }

    public void setWorldDifficulty(Difficulty difficulty) {
        matchWorld.setDifficulty(difficulty);
    }

    private void setWeatherCycle(boolean value) {
        this.matchWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, value);
    }

    private void setDayLightCycle(boolean value) {
        this.matchWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, value);
    }
}