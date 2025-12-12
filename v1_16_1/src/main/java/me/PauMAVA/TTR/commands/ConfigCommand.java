package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ConfigCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 1. Verificar Permisos
        if (!sender.hasPermission("ttr.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return true;
        }

        // 2. Verificar Argumentos
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso correcto: /ttrconfig <time/points> <valor>");
            return true;
        }

        String sub = args[0].toLowerCase();
        int value;

        // 3. Intentar leer el número
        try {
            value = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Error: '" + args[1] + "' no es un número válido.");
            return true;
        }

        // 4. Lógica de Configuración
        if (sub.equals("time") || sub.equals("tiempo")) {
            // Guardar en config.yml
            TTRCore.getInstance().getConfig().set("match.duration", value);
            TTRCore.getInstance().saveConfig();

            // Si la partida está activa, actualizar el tiempo en vivo
            if (TTRCore.getInstance().getCurrentMatch() != null) {
                TTRCore.getInstance().getCurrentMatch().setRemainingTime(value);
            }
            sender.sendMessage(ChatColor.GREEN + "✔ Tiempo de partida establecido a: " + ChatColor.YELLOW + value + " segundos.");

        } else if (sub.equals("points") || sub.equals("puntos") || sub.equals("maxpoints")) {
            // Guardar en config.yml
            TTRCore.getInstance().getConfig().set("match.maxpoints", value);
            TTRCore.getInstance().saveConfig();

            // Si la partida está activa, actualizar la meta en vivo
            if (TTRCore.getInstance().getCurrentMatch() != null) {
                TTRCore.getInstance().getCurrentMatch().setMaxPointsToWin(value);
            }
            sender.sendMessage(ChatColor.GREEN + "✔ Puntos para ganar establecidos a: " + ChatColor.YELLOW + value);

        } else {
            sender.sendMessage(ChatColor.RED + "Opción desconocida. Usa 'time' o 'points'.");
        }

        return true;
    }
}