package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.ui.ConfigGUI;
import me.PauMAVA.TTR.ui.ConfigScreen;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfigCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ttr.admin")) {
            sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("No tienes permiso."));
            return true;
        }

        if (args.length == 0) {
            if (sender instanceof Player) {
                ConfigGUI.open((Player) sender);
                return true;
            }
            sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Uso correcto en consola: /ttr config <time/points/autostart/gui/screen> <valor>"));
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("gui") || sub.equals("menu")) {
            if (sender instanceof Player) {
                ConfigGUI.open((Player) sender);
            } else {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Este comando solo puede ejecutarse en el juego."));
            }
            return true;
        }

        if (sub.equals("screen") || sub.equals("pantalla") || sub.equals("book")) {
            if (sender instanceof Player) {
                ConfigScreen.openScreen((Player) sender);
            } else {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Este comando solo puede ejecutarse en el juego."));
            }
            return true;
        }

        if (sub.equals("time") || sub.equals("duration") || sub.equals("tiempo")) {
            if (args.length < 2) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Uso: /ttr config time <segundos>"));
                return true;
            }
            int value;
            try {
                value = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("El valor debe ser un número entero válido."));
                return true;
            }
            TTRCore.getInstance().getConfig().set("match.duration", value);
            TTRCore.getInstance().saveConfig();

            if (TTRCore.getInstance().getCurrentMatch() != null) {
                TTRCore.getInstance().getCurrentMatch().setRemainingTime(value);
            }
            sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Duración establecida a: ") + ChatColor.YELLOW + value + "s (" + (value / 60) + "m)");
            return true;
        }

        if (sub.equals("points") || sub.equals("puntos") || sub.equals("maxpoints")) {
            if (args.length < 2) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Uso: /ttr config points <puntos>"));
                return true;
            }
            int value;
            try {
                value = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("El valor debe ser un número entero válido."));
                return true;
            }
            TTRCore.getInstance().getConfig().set("match.maxpoints", value);
            TTRCore.getInstance().saveConfig();

            if (TTRCore.getInstance().getCurrentMatch() != null) {
                TTRCore.getInstance().getCurrentMatch().setMaxPointsToWin(value);
            }
            sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Puntos para ganar establecidos a: ") + ChatColor.YELLOW + value);
            return true;
        }

        if (sub.equals("autostart")) {
            if (args.length >= 2 && args[1].equalsIgnoreCase("toggle")) {
                boolean cur = TTRCore.getInstance().getConfig().getBoolean("autostart.enabled", true);
                TTRCore.getInstance().getConfig().set("autostart.enabled", !cur);
                TTRCore.getInstance().saveConfig();
                sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Auto-inicio: ") + (!cur ? ChatColor.GREEN + TextUtil.toTiny("Activado") : ChatColor.RED + TextUtil.toTiny("Desactivado")));
                return true;
            }
            if (args.length >= 3 && args[1].equalsIgnoreCase("count")) {
                int cnt = Integer.parseInt(args[2]);
                TTRCore.getInstance().getConfig().set("autostart.count", cnt);
                TTRCore.getInstance().saveConfig();
                sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Jugadores mínimos: ") + ChatColor.YELLOW + cnt);
                return true;
            }
        }

        sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Opción desconocida. Usa /ttr config <gui/screen/time/points/autostart>."));
        return true;
    }
}
