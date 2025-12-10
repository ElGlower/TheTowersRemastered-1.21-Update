package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpectateCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ttr.admin")) {
            sender.sendMessage(TTRPrefix.TTR_ADMIN + " No tienes permisos.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Uso: /ttrspectate <jugador>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Jugador no encontrado.");
            return true;
        }

        TTRCore.getInstance().getTeamHandler().removePlayer(target);

        target.setGameMode(GameMode.SPECTATOR);
        target.getInventory().clear();

        target.sendMessage(ChatColor.GRAY + "Has sido puesto en modo espectador por un administrador.");
        sender.sendMessage(TTRPrefix.TTR_ADMIN + "Has puesto a " + target.getName() + " en espectador.");

        return true;
    }
}