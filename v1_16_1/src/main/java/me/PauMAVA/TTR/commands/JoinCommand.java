package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        if (TTRCore.getInstance().getTeamHandler().getPlayerTeam(p) != null) {
            p.sendMessage(ChatColor.RED + "¡Ya estás en un equipo!");
            return true;
        }

        p.setGameMode(GameMode.ADVENTURE);
        p.getInventory().clear();

        TTRCore.getInstance().getCurrentMatch().giveLobbyItems(p);

        Location lobby = TTRCore.getInstance().getConfigManager().getLobbyLocation();
        if (lobby != null) p.teleport(lobby);

        p.sendMessage(ChatColor.GREEN + "Has salido del modo espectador. ¡Elige un equipo!");

        return true;
    }
}