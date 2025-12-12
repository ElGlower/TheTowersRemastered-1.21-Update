package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TTRCustomTab extends BukkitRunnable {

    private final TTRCore plugin;

    public TTRCustomTab(TTRCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.enabled()) return;

        // Actualizamos para todos los jugadores conectados
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTabList(player);
        }
    }

    private void updateTabList(Player player) {
        // --- HEADER: Información del Servidor y Estado ---
        String header = "\n" +
                ChatColor.GOLD + "" + ChatColor.BOLD + " THE TOWERS " + "\n" +
                ChatColor.GRAY + "Remastered 1.21" + "\n" +
                "\n" +
                getMatchStatus() + "\n";

        // --- FOOTER: Ping y Jugadores ---
        String footer = "\n" +
                ChatColor.GRAY + "Jugadores: " + ChatColor.AQUA + Bukkit.getOnlinePlayers().size() +
                ChatColor.GRAY + "  |  Ping: " + getPingColor(player.getPing()) + player.getPing() + "ms" + "\n" +
                "\n" +
                ChatColor.YELLOW + "DESTINY OWNERS HOST" + "\n";

        player.setPlayerListHeaderFooter(header, footer);
        updatePlayerName(player);
    }

    private void updatePlayerName(Player player) {
        TTRTeam team = plugin.getTeamHandler().getPlayerTeam(player);
        String formattedName;

        // Obtenemos kills si la partida está en curso
        String killsInfo = "";
        if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            int kills = plugin.getCurrentMatch().getKills(player);
            killsInfo = ChatColor.GRAY + " [" + ChatColor.WHITE + kills + ChatColor.GRAY + "]";
        }

        if (team != null) {
            // --- AQUÍ ESTÁ EL TRUCO DEL ORDEN ---
            // Al poner el color primero, Minecraft agrupa los colores iguales.
            // §9 (Azul) viene antes que §c (Rojo) en la lista de códigos de Minecraft.
            // Así que los Azules saldrán arriba y los Rojos abajo (o viceversa), pero JUNTOS.

            String teamPrefix = team.getIdentifier().substring(0, 1).toUpperCase(); // "R" o "B"
            ChatColor color = team.getColor();

            // Formato: " R | Nombre [0]"
            formattedName = color + " " + teamPrefix + " | " + player.getName() + killsInfo;

        } else {
            // Espectadores o gente sin equipo
            if (player.isOp()) {
                formattedName = ChatColor.RED + " ADMIN " + ChatColor.WHITE + player.getName();
            } else {
                formattedName = ChatColor.GRAY + " " + player.getName();
            }
        }

        // Aplicamos el nombre en el TAB
        player.setPlayerListName(formattedName);
    }

    private String getMatchStatus() {
        if (plugin.getCurrentMatch() == null) return ChatColor.RED + "Offline";

        MatchStatus status = plugin.getCurrentMatch().getStatus();

        if (status == MatchStatus.INGAME) {
            String time = plugin.getCurrentMatch().getFormattedTime();
            return ChatColor.GREEN + "En Partida " + ChatColor.DARK_GRAY + "» " + ChatColor.WHITE + time;
        } else if (status == MatchStatus.LOBBY) {
            return ChatColor.YELLOW + "Esperando...";
        } else if (status == MatchStatus.STARTING) {
            return ChatColor.GOLD + "Iniciando...";
        } else {
            return ChatColor.RED + "Terminado";
        }
    }

    private ChatColor getPingColor(int ping) {
        if (ping < 60) return ChatColor.GREEN;
        if (ping < 150) return ChatColor.YELLOW;
        return ChatColor.RED;
    }
}