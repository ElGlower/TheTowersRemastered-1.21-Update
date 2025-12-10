/*
 * TheTowersRemastered (TTR)
 * Copyright (c) 2019-2021  Pau Machetti Vallverdú
 * [Licencia...]
 */

package me.PauMAVA.TTR.util;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PacketInterceptor implements Listener {

    private final TTRCore plugin;

    public PacketInterceptor(TTRCore plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void addPlayer(Player player) {
    }

    public void removePlayer(Player player) {
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.enabled() || plugin.getCurrentMatch() == null || plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
            return;
        }

        Player player = event.getPlayer();
        String message = event.getMessage();
        TTRTeam team = plugin.getTeamHandler().getPlayerTeam(player);

        event.setCancelled(true);

        if (team != null) {
            ChatColor teamColor = plugin.getConfigManager().getTeamColor(team.getIdentifier());
            String format = ChatColor.GRAY + "[" + teamColor + team.getIdentifier() + ChatColor.GRAY + "] "
                    + teamColor + player.getName() + ChatColor.WHITE + ": " + message;

            Bukkit.broadcastMessage(format);
        } else {
            String format = ChatColor.GRAY + "[Espectador] " + player.getName() + ": " + message;
            Bukkit.broadcastMessage(format);
        }
    }
}