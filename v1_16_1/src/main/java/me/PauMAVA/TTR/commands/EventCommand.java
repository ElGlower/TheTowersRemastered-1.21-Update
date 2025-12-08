package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;

public class EventCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permiso de admin requerido
        if (!sender.hasPermission("ttr.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso.");
            return true;
        }

        // Si escribe solo /ttrevent, le mostramos la ayuda
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "--- Eventos TheTowers ---");
            sender.sendMessage(ChatColor.YELLOW + "/ttrevent jump" + ChatColor.GRAY + " - Gravedad Lunar");
            sender.sendMessage(ChatColor.YELLOW + "/ttrevent speed" + ChatColor.GRAY + " - Flash");
            sender.sendMessage(ChatColor.YELLOW + "/ttrevent blind" + ChatColor.GRAY + " - Tinieblas");
            sender.sendMessage(ChatColor.YELLOW + "/ttrevent slow" + ChatColor.GRAY + " - Pesadez");
            sender.sendMessage(ChatColor.YELLOW + "/ttrevent auto <on|off>" + ChatColor.GRAY + " - Control automático");
            return true;
        }

        String arg = args[0].toLowerCase();

        // Control del modo automático
        if (arg.equals("auto")) {
            if (args.length > 1 && args[1].equals("off")) {
                TTRCore.getInstance().getEventManager().setAutoEvents(false);
                sender.sendMessage(TTRPrefix.TTR_GAME + " " + ChatColor.RED + "Eventos automáticos DESACTIVADOS.");
            } else {
                TTRCore.getInstance().getEventManager().setAutoEvents(true);
                sender.sendMessage(TTRPrefix.TTR_GAME + " " + ChatColor.GREEN + "Eventos automáticos ACTIVADOS.");
            }
            return true;
        }

        // Activación manual de eventos
        switch (arg) {
            case "jump":
                TTRCore.getInstance().getEventManager().triggerEvent("Gravedad Lunar", PotionEffectType.JUMP_BOOST, 2);
                sender.sendMessage(ChatColor.GREEN + "Evento 'Gravedad Lunar' iniciado.");
                break;
            case "speed":
                TTRCore.getInstance().getEventManager().triggerEvent("Flash", PotionEffectType.SPEED, 2);
                sender.sendMessage(ChatColor.GREEN + "Evento 'Flash' iniciado.");
                break;
            case "blind":
                TTRCore.getInstance().getEventManager().triggerEvent("Tinieblas", PotionEffectType.BLINDNESS, 0);
                sender.sendMessage(ChatColor.GREEN + "Evento 'Tinieblas' iniciado.");
                break;
            case "slow":
                TTRCore.getInstance().getEventManager().triggerEvent("Pesadez", PotionEffectType.SLOWNESS, 1);
                sender.sendMessage(ChatColor.GREEN + "Evento 'Pesadez' iniciado.");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Evento no encontrado. Usa /ttrevent para ver la lista.");
        }
        return true;
    }
}