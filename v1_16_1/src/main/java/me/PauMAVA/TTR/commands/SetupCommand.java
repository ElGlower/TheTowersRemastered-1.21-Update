package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetupCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        if (!p.hasPermission("ttr.admin")) {
            p.sendMessage(TTRPrefix.TTR_ADMIN + " No tienes permiso.");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage("§cUso: /ttrset <lobby|redspawn|bluespawn|iron|xp|coal|emerald>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "lobby":
                TTRCore.getInstance().getConfigManager().setLobby(p.getLocation());
                p.sendMessage(TTRPrefix.TTR_ADMIN + " Lobby establecido.");
                break;
            case "redspawn":
                TTRCore.getInstance().getConfigManager().setTeamSpawn("Red", p.getLocation());
                p.sendMessage(TTRPrefix.TTR_ADMIN + " Spawn Rojo establecido.");
                break;
            case "bluespawn":
                TTRCore.getInstance().getConfigManager().setTeamSpawn("Blue", p.getLocation());
                p.sendMessage(TTRPrefix.TTR_ADMIN + " Spawn Azul establecido.");
                break;
            case "iron":
                TTRCore.getInstance().getConfigManager().addSpawn("iron", p.getLocation());
                p.sendMessage(TTRPrefix.TTR_ADMIN + " Generador de Hierro añadido.");
                break;
            case "xp":
                TTRCore.getInstance().getConfigManager().addSpawn("xp", p.getLocation());
                p.sendMessage(TTRPrefix.TTR_ADMIN + " Generador de XP añadido.");
                break;
            // --- NUEVOS ---
            case "coal":
                TTRCore.getInstance().getConfigManager().addSpawn("coal", p.getLocation());
                p.sendMessage(TTRPrefix.TTR_ADMIN + " Generador de Carbón añadido.");
                break;
            case "emerald":
                TTRCore.getInstance().getConfigManager().addSpawn("emerald", p.getLocation());
                p.sendMessage(TTRPrefix.TTR_ADMIN + " Generador de Esmeralda añadido.");
                break;
            default:
                p.sendMessage("§cUso: /ttrset <lobby|redspawn|bluespawn|iron|xp|coal|emerald>");
                break;
        }
        return true;
    }
}