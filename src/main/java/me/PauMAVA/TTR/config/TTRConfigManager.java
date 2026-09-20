package me.PauMAVA.TTR.config;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TTRConfigManager {

    private FileConfiguration config;

    public TTRConfigManager(FileConfiguration config) {
        this.config = config;
    }

    public void reload() {
        TTRCore.getInstance().reloadConfig();
        this.config = TTRCore.getInstance().getConfig();
    }

    public String getLocale() {
        return config.getString("locale", "es");
    }

    public void setEnableOnStart(boolean enable) {
        config.set("enable_on_start", enable);
        TTRCore.getInstance().saveConfig();
    }

    public Location getLocationSafe(String path) {
        if (!config.contains(path)) return null;

        // Si ya está serializado como objeto Location directo de Bukkit
        if (config.get(path) instanceof Location) {
            return config.getLocation(path);
        }

        String worldName = config.getString(path + ".world");
        if (worldName == null) return null;

        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw", 0.0);
        float pitch = (float) config.getDouble(path + ".pitch", 0.0);

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            try {
                world = Bukkit.createWorld(new WorldCreator(worldName));
            } catch (Exception e) {
                return null;
            }
        }

        if (world == null) return null;
        return new Location(world, x, y, z, yaw, pitch);
    }

    public Location getLobbyLocation() {
        Location loc = getLocationSafe("map.lobby");
        if (loc == null && !Bukkit.getWorlds().isEmpty()) {
            return Bukkit.getWorlds().get(0).getSpawnLocation();
        }
        return loc;
    }

    public void setLobby(Location location) {
        saveLocation("map.lobby", location);
    }

    public Set<String> getTeamNames() {
        ConfigurationSection section = config.getConfigurationSection("teams");
        return (section != null) ? section.getKeys(false) : Set.of("Red", "Blue");
    }

    public ChatColor getTeamColor(String teamIdentifier) {
        String colorName = config.getString("teams." + teamIdentifier + ".color");
        try {
            return ChatColor.valueOf(colorName);
        } catch (Exception e) {
            return teamIdentifier.equalsIgnoreCase("Red") ? ChatColor.RED : ChatColor.BLUE;
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

    public void setTeamBaseRegion(String teamIdentifier, Location pos1, Location pos2) {
        if (pos1 == null || pos2 == null || pos1.getWorld() == null) return;
        String path = "teams." + teamIdentifier + ".base";
        config.set(path + ".world", pos1.getWorld().getName());
        config.set(path + ".minX", Math.min(pos1.getX(), pos2.getX()));
        config.set(path + ".maxX", Math.max(pos1.getX(), pos2.getX()));
        config.set(path + ".minY", Math.min(pos1.getY(), pos2.getY()));
        config.set(path + ".maxY", Math.max(pos1.getY(), pos2.getY()));
        config.set(path + ".minZ", Math.min(pos1.getZ(), pos2.getZ()));
        config.set(path + ".maxZ", Math.max(pos1.getZ(), pos2.getZ()));
        TTRCore.getInstance().saveConfig();
    }

    public boolean isInsideTeamBase(String teamIdentifier, Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        String path = "teams." + teamIdentifier + ".base";
        if (config.contains(path + ".world")) {
            String wName = config.getString(path + ".world");
            if (wName != null && wName.equals(loc.getWorld().getName())) {
                double minX = config.getDouble(path + ".minX");
                double maxX = config.getDouble(path + ".maxX");
                double minY = config.getDouble(path + ".minY");
                double maxY = config.getDouble(path + ".maxY");
                double minZ = config.getDouble(path + ".minZ");
                double maxZ = config.getDouble(path + ".maxZ");
                return loc.getX() >= minX && loc.getX() <= maxX &&
                       loc.getY() >= minY && loc.getY() <= maxY &&
                       loc.getZ() >= minZ && loc.getZ() <= maxZ;
            }
        }

        // Fallback dinámico basado en la distancia al spawn de la isla del equipo
        Location spawn = getTeamSpawn(teamIdentifier);
        if (spawn != null && spawn.getWorld() != null && spawn.getWorld().equals(loc.getWorld())) {
            double baseRadius = config.getDouble("match.team_base_radius", 48.0);
            double dx = Math.abs(spawn.getX() - loc.getX());
            double dz = Math.abs(spawn.getZ() - loc.getZ());
            double dy = Math.abs(spawn.getY() - loc.getY());
            return dx <= baseRadius && dz <= baseRadius && dy <= 35.0;
        }
        return false;
    }

    public String getTeamBaseAt(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        Set<String> names = getTeamNames();
        if (names != null) {
            for (String team : names) {
                if (isInsideTeamBase(team, loc)) {
                    return team;
                }
            }
        }
        return null;
    }

    public String getTeamForChest(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        String key = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();

        // 1. Comprobar etiqueta explícita de Neutro
        List<String> neutralList = config.getStringList("tagged_neutral_chests");
        if (neutralList != null && neutralList.contains(key)) {
            return "Neutral";
        }

        // 2. Comprobar etiquetas explícitas por equipo
        Set<String> names = getTeamNames();
        if (names != null) {
            for (String team : names) {
                List<String> list = config.getStringList("teams." + team + ".tagged_chests");
                if (list != null && list.contains(key)) {
                    return team;
                }
            }
        }

        // 3. Si no tiene etiqueta explícita, se usa la base
        return getTeamBaseAt(loc);
    }

    public void setTeamChest(String team, Location loc) {
        if (loc == null || loc.getWorld() == null) return;
        String key = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();

        Set<String> names = getTeamNames();
        if (names != null) {
            for (String other : names) {
                List<String> otherList = new ArrayList<>(config.getStringList("teams." + other + ".tagged_chests"));
                if (otherList.remove(key)) {
                    config.set("teams." + other + ".tagged_chests", otherList);
                }
            }
        }

        List<String> neutralList = new ArrayList<>(config.getStringList("tagged_neutral_chests"));
        neutralList.remove(key);

        if (team != null && !team.equalsIgnoreCase("Neutral") && !team.equalsIgnoreCase("None")) {
            List<String> teamList = new ArrayList<>(config.getStringList("teams." + team + ".tagged_chests"));
            if (!teamList.contains(key)) {
                teamList.add(key);
            }
            config.set("teams." + team + ".tagged_chests", teamList);
        } else {
            neutralList.add(key);
        }

        config.set("tagged_neutral_chests", neutralList);
        TTRCore.getInstance().saveConfig();
    }

    public boolean toggleTeamChest(String team, Location loc) {
        if (loc == null || loc.getWorld() == null || team == null) return false;
        String key = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
        List<String> list = new ArrayList<>(config.getStringList("teams." + team + ".tagged_chests"));
        boolean added;
        if (list.contains(key)) {
            list.remove(key);
            added = false;
        } else {
            Set<String> names = getTeamNames();
            if (names != null) {
                for (String other : names) {
                    if (!other.equalsIgnoreCase(team)) {
                        List<String> otherList = new ArrayList<>(config.getStringList("teams." + other + ".tagged_chests"));
                        if (otherList.remove(key)) {
                            config.set("teams." + other + ".tagged_chests", otherList);
                        }
                    }
                }
            }
            list.add(key);
            added = true;
        }
        config.set("teams." + team + ".tagged_chests", list);
        TTRCore.getInstance().saveConfig();
        return added;
    }

    public List<Location> getTeamCages() {
        List<Location> all = new ArrayList<>();
        Set<String> names = getTeamNames();
        if (names != null) {
            for (String team : names) {
                all.addAll(getTeamCages(team));
            }
        }
        return all;
    }

    public List<Location> getSpawns(String type) {
        List<?> rawList = config.getList("spawns." + type);
        List<Location> locations = new ArrayList<>();

        if (rawList != null) {
            for (Object obj : rawList) {
                if (obj instanceof Location) {
                    locations.add((Location) obj);
                } else if (obj instanceof ConfigurationSection) {
                    ConfigurationSection sec = (ConfigurationSection) obj;
                    String wName = sec.getString("world");
                    World w = Bukkit.getWorld(wName);
                    if (w == null && wName != null) w = Bukkit.createWorld(new WorldCreator(wName));
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

    public int getMatchDuration() {
        return config.getInt("match.duration", 1200);
    }

    public int getMatchTime() {
        return config.getInt("match.time", 6000);
    }

    public String getWeather() {
        return config.getString("match.weather", "CLEAR");
    }

    public boolean isAutoStartEnabled() {
        return config.getBoolean("autostart.enabled", true);
    }

    public int getAutoStartPlayers() {
        return config.getInt("autostart.count", 4);
    }

    public int getAutoStartCountdown() {
        return config.getInt("autostart.countdown", 10);
    }

    public boolean isAutoRestoreMap() {
        return config.getBoolean("rollback.auto_restore_on_end", true);
    }

    public List<Location> getAllConfiguredChests() {
        List<Location> locs = new ArrayList<>();
        List<String> neutral = config.getStringList("tagged_neutral_chests");
        if (neutral != null) {
            for (String s : neutral) {
                Location l = parseLocationString(s);
                if (l != null) locs.add(l);
            }
        }
        Set<String> names = getTeamNames();
        if (names != null) {
            for (String team : names) {
                List<String> list = config.getStringList("teams." + team + ".tagged_chests");
                if (list != null) {
                    for (String s : list) {
                        Location l = parseLocationString(s);
                        if (l != null) locs.add(l);
                    }
                }
            }
        }
        return locs;
    }

    private Location parseLocationString(String s) {
        try {
            String[] parts = s.split(",");
            if (parts.length >= 4) {
                World w = Bukkit.getWorld(parts[0]);
                if (w != null) {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    int z = Integer.parseInt(parts[3]);
                    return new Location(w, x, y, z);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
}
