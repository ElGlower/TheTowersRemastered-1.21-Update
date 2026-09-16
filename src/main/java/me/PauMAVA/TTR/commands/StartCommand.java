package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StartCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ttr.admin")) {
            sender.sendMessage(TTRPrefix.TTR_ADMIN + " No tienes permisos.");
            return true;
        }

        if (TTRCore.getInstance().getCurrentMatch() == null) {
            sender.sendMessage(ChatColor.RED + "El sistema de juego está desactivado. Usa /ttrenable primero.");
            return true;
        }

        if (TTRCore.getInstance().getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            sender.sendMessage(ChatColor.RED + "¡La partida ya está en curso!");
            return true;
        }

        TTRCore.getInstance().getCurrentMatch().startMatch();
        sender.sendMessage(TTRPrefix.TTR_ADMIN + ChatColor.GREEN + " ¡Partida iniciada forzosamente!");
        return true;
    }
}