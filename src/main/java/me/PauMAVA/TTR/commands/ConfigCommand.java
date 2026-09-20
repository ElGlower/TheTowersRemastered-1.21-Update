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
            TTRCore.getInstance().getConfigManager().reload();

            if (TTRCore.getInstance().getCurrentMatch() != null) {
                TTRCore.getInstance().getCurrentMatch().setRemainingTime(value);
            }
            sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Duración de partida establecida a: ") + ChatColor.YELLOW + value + "s (" + (value / 60) + "m)");
            return true;
        }

        if (sub.equals("prep") || sub.equals("prestart") || sub.equals("preparacion")) {
            if (args.length < 2) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Uso: /ttr config prep <segundos>"));
                return true;
            }
            int value;
            try {
                value = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("El valor debe ser un número entero válido."));
                return true;
            }
            TTRCore.getInstance().getConfig().set("match.prestart_countdown", value);
            TTRCore.getInstance().saveConfig();
            TTRCore.getInstance().getConfigManager().reload();

            if (TTRCore.getInstance().getCurrentMatch() != null) {
                TTRCore.getInstance().getCurrentMatch().setPrepRemaining(value);
            }
            sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Tiempo de preparación establecido a: ") + ChatColor.YELLOW + value + "s");
            return true;
        }

        if (sub.equals("autostart-time") || sub.equals("autocountdown") || sub.equals("lobby-time")) {
            if (args.length < 2) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Uso: /ttr config autostart-time <segundos>"));
                return true;
            }
            int value;
            try {
                value = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("El valor debe ser un número entero válido."));
                return true;
            }
            TTRCore.getInstance().getConfig().set("autostart.countdown", value);
            TTRCore.getInstance().saveConfig();
            TTRCore.getInstance().getConfigManager().reload();

            sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Conteo de auto-inicio establecido a: ") + ChatColor.YELLOW + value + "s");
            return true;
        }

        if (sub.equals("autostart-players") || sub.equals("minplayers") || sub.equals("jugadores")) {
            if (args.length < 2) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Uso: /ttr config autostart-players <cantidad>"));
                return true;
            }
            int value;
            try {
                value = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("El valor debe ser un número entero válido."));
                return true;
            }
            TTRCore.getInstance().getConfig().set("autostart.count", value);
            TTRCore.getInstance().saveConfig();
            TTRCore.getInstance().getConfigManager().reload();

            sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Jugadores mínimos para auto-inicio: ") + ChatColor.YELLOW + value);
            return true;
        }

        if (sub.equals("vote-time") || sub.equals("votacion")) {
            if (args.length < 2) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Uso: /ttr config vote-time <segundos>"));
                return true;
            }
            int value;
            try {
                value = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("El valor debe ser un número entero válido."));
                return true;
            }
            TTRCore.getInstance().getConfig().set("voting.round_seconds", value);
            TTRCore.getInstance().saveConfig();
            TTRCore.getInstance().getConfigManager().reload();

            sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Ronda de votación establecida a: ") + ChatColor.YELLOW + value + "s");
            return true;
        }

        if (sub.equals("rules-time") || sub.equals("reglas") || sub.equals("explanation")) {
            if (args.length < 2) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Uso: /ttr config rules-time <segundos>"));
                return true;
            }
            int value;
            try {
                value = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("El valor debe ser un número entero válido."));
                return true;
            }
            TTRCore.getInstance().getConfig().set("modes.explanation_seconds", value);
            TTRCore.getInstance().saveConfig();
            TTRCore.getInstance().getConfigManager().reload();

            sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Tiempo para leer reglas establecido a: ") + ChatColor.YELLOW + value + "s");
            return true;
        }

        if (sub.equals("auction-time") || sub.equals("subasta")) {
            if (args.length < 2) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Uso: /ttr config auction-time <segundos>"));
                return true;
            }
            int value;
            try {
                value = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("El valor debe ser un número entero válido."));
                return true;
            }
            TTRCore.getInstance().getConfig().set("auction.turn_seconds", value);
            TTRCore.getInstance().saveConfig();
            TTRCore.getInstance().getConfigManager().reload();

            sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Turno de subasta establecido a: ") + ChatColor.YELLOW + value + "s");
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
            TTRCore.getInstance().getConfigManager().reload();

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
                TTRCore.getInstance().getConfigManager().reload();
                sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Auto-inicio: ") + (!cur ? ChatColor.GREEN + TextUtil.toTiny("Activado") : ChatColor.RED + TextUtil.toTiny("Desactivado")));
                return true;
            }
            if (args.length >= 3 && args[1].equalsIgnoreCase("count")) {
                int cnt = Integer.parseInt(args[2]);
                TTRCore.getInstance().getConfig().set("autostart.count", cnt);
                TTRCore.getInstance().saveConfig();
                TTRCore.getInstance().getConfigManager().reload();
                sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Jugadores mínimos: ") + ChatColor.YELLOW + cnt);
                return true;
            }
        }

        if (sub.equals("credits") || sub.equals("creditos")) {
            if (args.length < 2) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Uso: /ttr config credits <monto>"));
                return true;
            }
            int value;
            try {
                value = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("El valor debe ser un número entero válido."));
                return true;
            }
            TTRCore.getInstance().getConfig().set("auction.initial_credits", value);
            TTRCore.getInstance().saveConfig();
            TTRCore.getInstance().getConfigManager().reload();

            sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Créditos de subasta establecidos a: ") + ChatColor.YELLOW + value);
            return true;
        }

        if (sub.equals("bid-increment") || sub.equals("incremento")) {
            if (args.length < 2) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Uso: /ttr config bid-increment <monto>"));
                return true;
            }
            int value;
            try {
                value = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("El valor debe ser un número entero válido."));
                return true;
            }
            TTRCore.getInstance().getConfig().set("auction.bid_increment", value);
            TTRCore.getInstance().saveConfig();
            TTRCore.getInstance().getConfigManager().reload();

            sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Incremento de puja establecido a: ") + ChatColor.YELLOW + value);
            return true;
        }

        sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Opciones: time, prep, autostart-time, autostart-players, vote-time, rules-time, auction-time, credits, bid-increment, points, gui, screen."));
        return true;
    }
}
