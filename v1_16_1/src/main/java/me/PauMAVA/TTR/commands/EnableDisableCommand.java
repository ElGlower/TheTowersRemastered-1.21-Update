package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EnableDisableCommand implements CommandExecutor {

    private final TTRCore plugin;

    public EnableDisableCommand(TTRCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ttr.admin")) {
            sender.sendMessage(TTRPrefix.TTR_ADMIN + " No tienes permisos.");
            return true;
        }

        if (label.equalsIgnoreCase("ttrenable")) {
            plugin.getConfigManager().setEnableOnStart(true);
            sender.sendMessage(TTRPrefix.TTR_ADMIN + ChatColor.GREEN + " Juego habilitado (se activará al reiniciar).");
        }
        else if (label.equalsIgnoreCase("ttrdisable")) {
            plugin.getConfigManager().setEnableOnStart(false);
            sender.sendMessage(TTRPrefix.TTR_ADMIN + ChatColor.RED + " Juego deshabilitado (se desactivará al reiniciar).");
        }

        return true;
    }
}