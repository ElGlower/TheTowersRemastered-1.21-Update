package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EventCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ttr.admin")) {
            sender.sendMessage(TTRPrefix.TTR_ADMIN + " No tienes permisos.");
            return true;
        }
         if (args.length == 0) {
            sender.sendMessage(ChatColor.GREEN + "Forzando evento aleatorio...");
            TTRCore.getInstance().getEventManager().triggerRandomEvent();
            return true;
        }

        String arg = args[0].toLowerCase();


        if (arg.equals("stop")) {
            TTRCore.getInstance().getEventManager().stopCurrentEvent();
            sender.sendMessage(ChatColor.GREEN + "Evento detenido manualmente.");
            return true;
        }

        if (arg.equals("auto")) {
            boolean current = TTRCore.getInstance().getEventManager().isAutoMode();
            TTRCore.getInstance().getEventManager().toggleAutoMode(!current);
            return true;
        }

        TTRCore.getInstance().getEventManager().triggerEvent(arg);
        sender.sendMessage(ChatColor.GREEN + "Iniciando evento: " + arg);
        return true;
    }
}