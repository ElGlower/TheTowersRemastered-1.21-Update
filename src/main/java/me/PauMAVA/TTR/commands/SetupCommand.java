package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TextUtil;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage(TTRPrefix.TTR_ERROR + "Solo jugadores.");
            return true;
        }
        Player p = (Player) sender;

        if (!p.hasPermission("ttr.admin")) {
            p.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("No tienes permisos."));
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + TextUtil.toTiny("CONFIGURACIÓN DE PUNTOS Y SPAWNS"));
            p.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/ttr set lobby" + ChatColor.GRAY + " - " + TextUtil.toTiny("Establecer el Lobby principal"));
            p.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/ttr set redspawn / bluespawn" + ChatColor.GRAY + " - " + TextUtil.toTiny("Spawns de equipo"));
            p.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/ttr set redcage / bluecage" + ChatColor.GRAY + " - " + TextUtil.toTiny("Jaulas de puntos"));
            p.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/ttr set iron / coal / emerald / xp" + ChatColor.GRAY + " - " + TextUtil.toTiny("Añadir generadores"));
            p.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            return true;
        }

        String sub = args[0].toLowerCase();
        Location loc = p.getLocation();

        loc.setX(loc.getBlockX() + 0.5);
        loc.setY(loc.getBlockY());
        loc.setZ(loc.getBlockZ() + 0.5);

        switch (sub) {
            case "lobby":
                TTRCore.getInstance().getConfigManager().setLobby(p.getLocation());
                p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Lobby establecido con éxito."));
                break;
            case "redspawn":
                TTRCore.getInstance().getConfigManager().setTeamSpawn("Red", p.getLocation());
                p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Spawn ROJO establecido."));
                break;
            case "bluespawn":
                TTRCore.getInstance().getConfigManager().setTeamSpawn("Blue", p.getLocation());
                p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Spawn AZUL establecido."));
                break;
            case "redcage":
                TTRCore.getInstance().getConfigManager().setTeamCage("Red", loc);
                p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Jaula ROJA establecida."));
                break;
            case "bluecage":
                TTRCore.getInstance().getConfigManager().setTeamCage("Blue", loc);
                p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Jaula AZUL establecida."));
                break;
            case "iron":
                TTRCore.getInstance().getConfigManager().addSpawn("iron", loc);
                p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Generador de ") + ChatColor.WHITE + TextUtil.toTiny("HIERRO") + TextUtil.toTiny(" añadido."));
                break;
            case "coal":
                TTRCore.getInstance().getConfigManager().addSpawn("coal", loc);
                p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Generador de ") + ChatColor.DARK_GRAY + TextUtil.toTiny("CARBÓN") + TextUtil.toTiny(" añadido."));
                break;
            case "emerald":
                TTRCore.getInstance().getConfigManager().addSpawn("emerald", loc);
                p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Generador de ") + ChatColor.GREEN + TextUtil.toTiny("ESMERALDA") + TextUtil.toTiny(" añadido."));
                break;
            case "xp":
                TTRCore.getInstance().getConfigManager().addSpawn("xp", loc);
                p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Generador de ") + ChatColor.AQUA + TextUtil.toTiny("EXPERIENCIA") + TextUtil.toTiny(" añadido."));
                break;
            default:
                p.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Opción desconocida."));
                break;
        }
        return true;
    }
}
