package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ttr.admin")) {
            sender.sendMessage(TTRPrefix.TTR_ADMIN + " No tienes permisos.");
            return true;
        }

        if (TTRCore.getInstance().getCurrentMatch() != null) {
            // Forzamos el final de la partida (pasamos null para que no gane nadie)
            TTRCore.getInstance().getCurrentMatch().endMatch(null);
            sender.sendMessage(TTRPrefix.TTR_ADMIN + ChatColor.RED + " Partida detenida forzosamente.");
        } else {
            sender.sendMessage(ChatColor.RED + "No hay partida activa.");
        }
        return true;
    }
}