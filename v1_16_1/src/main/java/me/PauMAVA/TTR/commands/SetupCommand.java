package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
            p.sendMessage(TTRPrefix.TTR_ADMIN + " No tienes permisos.");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(ChatColor.GOLD + "--- Configuración TTR ---");
            p.sendMessage(ChatColor.YELLOW + "/ttrset lobby" + ChatColor.GRAY + " - Setea el Lobby");
            p.sendMessage(ChatColor.YELLOW + "/ttrset redspawn / bluespawn" + ChatColor.GRAY + " - Spawns de equipo");
            p.sendMessage(ChatColor.YELLOW + "/ttrset redcage / bluecage" + ChatColor.GRAY + " - Jaulas de equipo");
            p.sendMessage(ChatColor.AQUA + "/ttrset iron / coal / emerald / xp" + ChatColor.GRAY + " - Añadir generadores");
            return true;
        }

        String sub = args[0].toLowerCase();
        Location loc = p.getLocation();

        loc.setX(loc.getBlockX() + 0.5);
        loc.setY(loc.getBlockY());
        loc.setZ(loc.getBlockZ() + 0.5);

        switch (sub) {
            case "lobby":
                TTRCore.getInstance().getConfigManager().setLobby(p.getLocation()); // Lobby usa rotación del jugador
                p.sendMessage(TTRPrefix.TTR_ADMIN + "Lobby establecido.");
                break;
            case "redspawn":
                TTRCore.getInstance().getConfigManager().setTeamSpawn("Red", p.getLocation());
                p.sendMessage(TTRPrefix.TTR_ADMIN + "Spawn ROJO establecido.");
                break;
            case "bluespawn":
                TTRCore.getInstance().getConfigManager().setTeamSpawn("Blue", p.getLocation());
                p.sendMessage(TTRPrefix.TTR_ADMIN + "Spawn AZUL establecido.");
                break;
            case "redcage":
                TTRCore.getInstance().getConfigManager().setTeamCage("Red", loc);
                p.sendMessage(TTRPrefix.TTR_ADMIN + "Jaula ROJA establecida.");
                break;
            case "bluecage":
                TTRCore.getInstance().getConfigManager().setTeamCage("Blue", loc);
                p.sendMessage(TTRPrefix.TTR_ADMIN + "Jaula AZUL establecida.");
                break;

            // --- NUEVOS GENERADORES ---
            case "iron":
                TTRCore.getInstance().getConfigManager().addSpawn("iron", loc);
                p.sendMessage(TTRPrefix.TTR_ADMIN + "Generador de " + ChatColor.WHITE + "HIERRO" + ChatColor.GRAY + " añadido.");
                break;
            case "coal":
                TTRCore.getInstance().getConfigManager().addSpawn("coal", loc);
                p.sendMessage(TTRPrefix.TTR_ADMIN + "Generador de " + ChatColor.DARK_GRAY + "CARBÓN" + ChatColor.GRAY + " añadido.");
                break;
            case "emerald":
                TTRCore.getInstance().getConfigManager().addSpawn("emerald", loc);
                p.sendMessage(TTRPrefix.TTR_ADMIN + "Generador de " + ChatColor.GREEN + "ESMERALDA" + ChatColor.GRAY + " añadido.");
                break;
            case "xp":
                TTRCore.getInstance().getConfigManager().addSpawn("xp", loc);
                p.sendMessage(TTRPrefix.TTR_ADMIN + "Generador de " + ChatColor.AQUA + "EXPERIENCIA" + ChatColor.GRAY + " añadido.");
                break;
            default:
                p.sendMessage(ChatColor.RED + "Opción desconocida.");
                break;
        }
        return true;
    }
}