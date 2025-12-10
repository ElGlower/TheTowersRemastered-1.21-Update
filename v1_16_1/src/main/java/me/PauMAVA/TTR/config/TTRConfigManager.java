package me.PauMAVA.TTR.config;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator; // IMPORTANTE
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TTRConfigManager {

    private final FileConfiguration config;

    public TTRConfigManager(FileConfiguration config) {
        this.config = config;
    }

    public String getLocale() {
        return config.getString("locale", "en");
    }

    public void setEnableOnStart(boolean enable) {
        config.set("enable_on_start", enable);
        TTRCore.getInstance().saveConfig();
    }

    public Location getLocationSafe(String path) {
        if (!config.contains(path)) return null;

        String worldName = config.getString(path + ".world");
        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw");
        float pitch = (float) config.getDouble(path + ".pitch");

        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            System.out.println("[TTR] El mundo '" + worldName + "' no está cargado. Intentando cargar...");
            try {
                world = Bukkit.createWorld(new WorldCreator(worldName));
            } catch (Exception e) {
                System.out.println("[TTR] ERROR FATAL: No se pudo cargar el mundo '" + worldName + "'. ¿Existe la carpeta?");
                return null;
            }
        }

        if (world == null) {
            return null;
        }

        return new Location(world, x, y, z, yaw, pitch);
    }

    public Location getLobbyLocation() {
        return getLocationSafe("map.lobby");
    }

    public void setLobby(Location location) {
        saveLocation("map.lobby", location);
    }

    public Set<String> getTeamNames() {
        ConfigurationSection section = config.getConfigurationSection("teams");
        return (section != null) ? section.getKeys(false) : null;
    }

    public ChatColor getTeamColor(String teamIdentifier) {
        String colorName = config.getString("teams." + teamIdentifier + ".color");
        try {
            return ChatColor.valueOf(colorName);
        } catch (Exception e) {
            return ChatColor.WHITE;
        }
    }

    public Location getTeamSpawn(String teamIdentifier) {
        return getLocationSafe("teams." + teamIdentifier + ".spawn");
    }

    public void setTeamSpawn(String teamIdentifier, Location location) {
        saveLocation("teams." + teamIdentifier + ".spawn", location);
    }

    public List<Location> getTeamCages(String teamIdentifier) {
        List<Location> cages = new ArrayList<>();
        Location singleCage = getLocationSafe("teams." + teamIdentifier + ".cage");
        if (singleCage != null) cages.add(singleCage);
        return cages;
    }

    public void setTeamCage(String teamIdentifier, Location location) {
        saveLocation("teams." + teamIdentifier + ".cage", location);
    }

    public List<Location> getTeamCages() {
        List<Location> all = new ArrayList<>();
        if (getTeamNames() != null) {
            for (String team : getTeamNames()) {
                all.addAll(getTeamCages(team));
            }
        }
        return all;
    }

    @SuppressWarnings("unchecked")
    public List<Location> getSpawns(String type) {
        List<?> rawList = config.getList("spawns." + type);
        List<Location> locations = new ArrayList<>();

        if (rawList != null) {
            for (Object obj : rawList) {
                if (obj instanceof Location) {
                    locations.add((Location) obj);
                }
                else if (obj instanceof org.bukkit.configuration.ConfigurationSection) {
                    ConfigurationSection sec = (ConfigurationSection) obj;
                    String wName = sec.getString("world");
                    World w = Bukkit.getWorld(wName);
                    if (w == null) w = Bukkit.createWorld(new WorldCreator(wName));

                    if (w != null) {
                        locations.add(new Location(w, sec.getDouble("x"), sec.getDouble("y"), sec.getDouble("z")));
                    }
                }
            }
        }
        return locations;
    }

    public void addSpawn(String type, Location loc) {
        List<Location> list = getSpawns(type);
        list.add(loc);
        config.set("spawns." + type, list);
        TTRCore.getInstance().saveConfig();
    }

    private void saveLocation(String path, Location loc) {
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());
        TTRCore.getInstance().saveConfig();
    }

    public int getMaxPoints() {
        return config.getInt("match.maxpoints", 10);
    }

    public String getWeather() {
        return config.getString("match.weather", "CLEAR");
    }

    public int getMatchTime() {
        return config.getInt("match.time", 1200);
    }
}