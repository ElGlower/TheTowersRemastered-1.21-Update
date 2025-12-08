package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SetupCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Solo jugadores.");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("ttr.admin")) {
            player.sendMessage(ChatColor.RED + "No tienes permiso.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.GOLD + "--- Comandos de Setup TTR ---");
            player.sendMessage(ChatColor.YELLOW + "/ttrset lobby " + ChatColor.GRAY + "- Define el Lobby");
            player.sendMessage(ChatColor.YELLOW + "/ttrset spawn <Equipo> " + ChatColor.GRAY + "- Define Spawn");
            player.sendMessage(ChatColor.YELLOW + "/ttrset cage <Equipo> " + ChatColor.GRAY + "- Define Jaula");
            return true;
        }

        FileConfiguration config = TTRCore.getInstance().getConfig();
        Location loc = player.getLocation();
        String action = args[0].toLowerCase();

        if (action.equals("lobby")) {
            saveLocation(config, "map.lobby", loc);
            player.sendMessage(TTRPrefix.TTR_GAME + " " + ChatColor.GREEN + "Lobby establecido.");
            TTRCore.getInstance().saveConfig();
            TTRCore.getInstance().getConfigManager().resetFile();
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Falta el nombre del equipo.");
            return true;
        }

        String teamArg = args[1];
        String realTeamName = null;
        for (String team : TTRCore.getInstance().getConfigManager().getTeamNames()) {
            if (team.equalsIgnoreCase(teamArg) || team.replace(" ", "").equalsIgnoreCase(teamArg)) {
                realTeamName = team;
                break;
            }
        }

        if (realTeamName == null) {
            player.sendMessage(ChatColor.RED + "Equipo no encontrado.");
            return true;
        }

        if (action.equals("spawn")) {
            saveLocation(config, "teams." + realTeamName + ".spawn", loc);
            player.sendMessage(TTRPrefix.TTR_GAME + " " + ChatColor.GREEN + "Spawn de " + realTeamName + " guardado.");
        }
        else if (action.equals("cage")) {
            loc.setX(loc.getBlockX() + 0.5);
            loc.setZ(loc.getBlockZ() + 0.5);
            saveLocation(config, "teams." + realTeamName + ".cage", loc);
            player.sendMessage(TTRPrefix.TTR_GAME + " " + ChatColor.GREEN + "Jaula de " + realTeamName + " guardada.");
        }

        TTRCore.getInstance().saveConfig();
        TTRCore.getInstance().getConfigManager().resetFile();
        return true;
    }

    private void saveLocation(FileConfiguration config, String path, Location loc) {
        if (loc.getWorld() != null) config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());
    }
}