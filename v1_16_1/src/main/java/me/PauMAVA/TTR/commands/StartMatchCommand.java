package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StartMatchCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ttr.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar esto.");
            return true;
        }

        if (TTRCore.getInstance().getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            sender.sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.RED + "¡La partida ya está en curso!");
            return true;
        }

        sender.sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.GREEN + "Forzando inicio de partida...");

        // --- LÓGICA DE AUTO-ASIGNACIÓN DE EQUIPOS ---

        //  Convertimos el Set a ArrayList para poder usar .get() y .size()
        List<String> teamNames = new ArrayList<>(TTRCore.getInstance().getConfigManager().getTeamNames());

        if (teamNames.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "ERROR: No hay equipos configurados en config.yml");
            return true;
        }

        Random random = new Random();

        for (Player p : Bukkit.getOnlinePlayers()) {
            // Si el jugador NO tiene equipo...
            if (TTRCore.getInstance().getTeamHandler().getPlayerTeam(p) == null) {
                // Elegimos un equipo al azar de la lista
                String randomTeam = teamNames.get(random.nextInt(teamNames.size()));

                // Lo añadimos al equipo
                TTRCore.getInstance().getTeamHandler().addPlayer(randomTeam, p);
                p.sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.YELLOW + "Se te ha asignado al equipo " + randomTeam + " automáticamente.");
            }
        }
        // -------------------------------------------

        //  incia la partida
        TTRCore.getInstance().getCurrentMatch().startMatch();

        return true;
    }
}