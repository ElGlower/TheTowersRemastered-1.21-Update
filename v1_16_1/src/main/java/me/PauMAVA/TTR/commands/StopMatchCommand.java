package me.PauMAVA.TTR.commands;
import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class StopMatchCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ttr.admin")) return true;
        Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + "" + ChatColor.RED + " ¡Partida detenida!");
        TTRCore.getInstance().resetMatchLogic();
        return true;
    }
}