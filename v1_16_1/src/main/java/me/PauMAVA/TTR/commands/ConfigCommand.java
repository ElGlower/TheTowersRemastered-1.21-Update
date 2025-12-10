package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ConfigCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ttr.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.GOLD + "--- Configuración TTR ---");
            sender.sendMessage(ChatColor.YELLOW + "/ttrconfig time <segundos>");
            sender.sendMessage(ChatColor.YELLOW + "/ttrconfig points <cantidad>");
            return true;
        }

        String type = args[0].toLowerCase();
        int value;

        try {
            value = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "El valor debe ser un número.");
            return true;
        }

        if (type.equals("time") || type.equals("tiempo")) {
            TTRCore.getInstance().getConfig().set("match.duration", value);
            TTRCore.getInstance().saveConfig();

            if (TTRCore.getInstance().getCurrentMatch() != null) {
                TTRCore.getInstance().getCurrentMatch().setRemainingTime(value);
            }
            sender.sendMessage(TTRPrefix.TTR_GAME + " " + ChatColor.GREEN + "Tiempo establecido a " + value + " segundos.");

        } else if (type.equals("points") || type.equals("puntos")) {
            TTRCore.getInstance().getConfig().set("match.maxpoints", value);
            TTRCore.getInstance().saveConfig();

            if (TTRCore.getInstance().getCurrentMatch() != null) {
                TTRCore.getInstance().getCurrentMatch().setMaxPointsToWin(value);
            }
            sender.sendMessage(TTRPrefix.TTR_GAME + " " + ChatColor.GREEN + "Puntos para ganar: " + value);

        } else {
            sender.sendMessage(ChatColor.RED + "Opción desconocida.");
            return true;
        }

        TTRCore.getInstance().getScoreboard().refreshScoreboard();

        return true;
    }
}