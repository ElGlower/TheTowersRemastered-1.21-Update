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

        int prepTime = TTRCore.getInstance().getConfig().getInt("match.prestart_countdown", 15);
        if (args.length > 0 && args[0].equalsIgnoreCase("now")) {
            TTRCore.getInstance().getCurrentMatch().startMatch();
            sender.sendMessage(TTRPrefix.TTR_ADMIN + ChatColor.GREEN + " ¡Partida iniciada instantáneamente!");
        } else {
            TTRCore.getInstance().getCurrentMatch().startPreparationPhase(prepTime);
            sender.sendMessage(TTRPrefix.TTR_ADMIN + ChatColor.GREEN + " ¡Fase de preparación (" + prepTime + "s) iniciada!");
        }
        return true;
    }
}