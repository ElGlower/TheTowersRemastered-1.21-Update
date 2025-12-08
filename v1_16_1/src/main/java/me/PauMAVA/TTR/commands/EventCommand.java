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
        if (!sender.hasPermission("ttr.admin")) return true;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "/ttrevent <jump|speed|blind|slow|size|meteors|fall>");
            sender.sendMessage(ChatColor.YELLOW + "/ttrevent auto <on|off>");
            return true;
        }

        String arg = args[0].toLowerCase();

        if (arg.equals("auto")) {
            if (args.length > 1 && args[1].equals("off")) {
                TTRCore.getInstance().getEventManager().setAutoEvents(false);
                sender.sendMessage(TTRPrefix.TTR_GAME + " Auto-Eventos OFF.");
            } else {
                TTRCore.getInstance().getEventManager().setAutoEvents(true);
                sender.sendMessage(TTRPrefix.TTR_GAME + " Auto-Eventos ON.");
            }
            return true;
        }

        switch (arg) {
            case "jump":
                TTRCore.getInstance().getEventManager().triggerPotionEvent("Gravedad Lunar", PotionEffectType.JUMP_BOOST, 2);
                break;
            case "speed":
                TTRCore.getInstance().getEventManager().triggerPotionEvent("Flash", PotionEffectType.SPEED, 2);
                break;
            case "blind":
                TTRCore.getInstance().getEventManager().triggerPotionEvent("Tinieblas", PotionEffectType.BLINDNESS, 0);
                break;
            case "slow":
                TTRCore.getInstance().getEventManager().triggerPotionEvent("Pesadez", PotionEffectType.SLOWNESS, 1);
                break;
            // --- NUEVOS ---
            case "size":
                TTRCore.getInstance().getEventManager().triggerManual("size");
                break;
            case "meteors":
                TTRCore.getInstance().getEventManager().triggerManual("meteors");
                break;
            case "fall":
                TTRCore.getInstance().getEventManager().triggerManual("fall");
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Evento no encontrado.");
        }
        return true;
    }
}