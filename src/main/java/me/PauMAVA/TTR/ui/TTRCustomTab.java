package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TTRCustomTab extends BukkitRunnable {

    private final TTRCore plugin;
    private int animStep = 0;

    // Efecto de brillo animado en la cabecera
    private static final String[] SHINE_FRAMES = {
        "§6§l◆ §e§lᴅᴇsᴛɪɴʏ ᴛᴏᴡᴇʀs §6§l◆",
        "§e§l◆ §f§lᴅ§e§lsᴛɪɴʏ ᴛᴏᴡᴇʀs §6§l◆",
        "§6§l◆ §e§lᴅ§f§lᴇ§e§lsᴛɪɴʏ ᴛᴏᴡᴇʀs §6§l◆",
        "§6§l◆ §e§lᴅᴇ§f§ls§e§lᴛɪɴʏ ᴛᴏᴡᴇʀs §6§l◆",
        "§6§l◆ §e§lᴅᴇs§f§lᴛ§e§lɪɴʏ ᴛᴏᴡᴇʀs §6§l◆",
        "§6§l◆ §e§lᴅᴇsᴛ§f§lɪ§e§lɴʏ ᴛᴏᴡᴇʀs §6§l◆",
        "§6§l◆ §e§lᴅᴇsᴛɪ§f§lɴ§e§lʏ ᴛᴏᴡᴇʀs §6§l◆",
        "§6§l◆ §e§lᴅᴇsᴛɪɴ§f§lʏ§e§l ᴛᴏᴡᴇʀs §6§l◆",
        "§6§l◆ §e§lᴅᴇsᴛɪɴʏ §f§lᴛ§e§lᴏᴡᴇʀs §6§l◆",
        "§6§l◆ §e§lᴅᴇsᴛɪɴʏ ᴛ§f§lᴏ§e§lᴡᴇʀs §6§l◆",
        "§6§l◆ §e§lᴅᴇsᴛɪɴʏ ᴛᴏ§f§lᴡ§e§lᴇʀs §6§l◆",
        "§6§l◆ §e§lᴅᴇsᴛɪɴʏ ᴛᴏᴡ§f§lᴇ§e§lʀs §6§l◆",
        "§6§l◆ §e§lᴅᴇsᴛɪɴʏ ᴛᴏᴡᴇ§f§lʀ§e§ls §6§l◆",
        "§6§l◆ §e§lᴅᴇsᴛɪɴʏ ᴛᴏᴡᴇʀ§f§ls §6§l◆",
        "§e§l◆ §f§l◆ §e§lᴅᴇsᴛɪɴʏ ᴛᴏᴡᴇʀs §f§l◆ §e§l◆"
    };

    private static final String[] FOOTER_FRAMES = {
        "§e● §6§lᴅᴇsᴛɪɴʏᴏᴡɴᴇʀs §e●",
        "§6● §e§lᴅᴇsᴛɪɴʏᴏᴡɴᴇʀs §6●",
        "§f● §e§lᴅᴇsᴛɪɴʏᴏᴡɴᴇʀs §f●",
        "§e● §f§lᴅᴇsᴛɪɴʏᴏᴡɴᴇʀs §e●"
    };

    public TTRCustomTab(TTRCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.enabled()) return;

        animStep++;
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTabList(player);
        }
    }

    private void updateTabList(Player player) {
        String animatedTitle = SHINE_FRAMES[animStep % SHINE_FRAMES.length];
        String animatedFooter = FOOTER_FRAMES[(animStep / 2) % FOOTER_FRAMES.length];

        String header = "\n" +
                animatedTitle + "\n" +
                ChatColor.DARK_GRAY + "§m                             \n" +
                getMatchStatus() + "\n";

        int online = Bukkit.getOnlinePlayers().size();
        int ping = player.getPing();

        String footer = "\n" +
                ChatColor.GRAY + TextUtil.toTiny("Jugadores: ") + ChatColor.AQUA + TextUtil.toTiny(String.valueOf(online)) +
                ChatColor.DARK_GRAY + "  ▪  " + ChatColor.GRAY + TextUtil.toTiny("Ping: ") + getPingDisplay(ping) + "\n" +
                ChatColor.DARK_GRAY + "§m                             \n" +
                animatedFooter + "\n";

        player.setPlayerListHeaderFooter(header, footer);
        updatePlayerName(player);
    }

    private void updatePlayerName(Player player) {
        TTRTeam team = plugin.getTeamHandler().getPlayerTeam(player);
        String formattedName;

        String killsInfo = "";
        if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            int kills = plugin.getCurrentMatch().getKills(player);
            killsInfo = ChatColor.DARK_GRAY + " [" + ChatColor.YELLOW + TextUtil.toTiny(String.valueOf(kills)) + ChatColor.DARK_GRAY + "]";
        }

        if (team != null) {
            boolean isLeader = team.isLeader(player.getUniqueId());
            String leaderBadge = isLeader ? ChatColor.GOLD + "★" + TextUtil.toTiny("Líder ") : "";
            String teamPrefix = TextUtil.toTiny(team.getIdentifier().substring(0, 1).toUpperCase());
            ChatColor color = team.getColor();
            formattedName = color + " ▪ " + leaderBadge + color + "" + ChatColor.BOLD + teamPrefix + ChatColor.DARK_GRAY + " | " + color + player.getName() + killsInfo;
        } else {
            if (player.isOp() || player.hasPermission("destinytowers.admin") || player.hasPermission("ttr.admin")) {
                formattedName = ChatColor.GOLD + " ▪ " + ChatColor.GOLD + "" + ChatColor.BOLD + TextUtil.toTiny("Destiny") + ChatColor.DARK_GRAY + " | " + ChatColor.WHITE + player.getName();
            } else {
                formattedName = ChatColor.GRAY + " ▪ " + ChatColor.WHITE + player.getName();
            }
        }

        player.setPlayerListName(formattedName);
    }

    private String getMatchStatus() {
        if (plugin.getCurrentMatch() == null) return ChatColor.RED + TextUtil.toTiny("Offline");

        MatchStatus status = plugin.getCurrentMatch().getStatus();
        if (status == MatchStatus.INGAME) {
            String time = plugin.getCurrentMatch().getFormattedTime();
            return ChatColor.GREEN + TextUtil.toTiny("En Partida ") + ChatColor.DARK_GRAY + "» " + ChatColor.WHITE + TextUtil.toTiny(time);
        } else if (status == MatchStatus.LOBBY) {
            return ChatColor.YELLOW + TextUtil.toTiny("Esperando jugadores...");
        } else if (status == MatchStatus.STARTING) {
            return ChatColor.GOLD + TextUtil.toTiny("Iniciando...");
        } else {
            return ChatColor.RED + TextUtil.toTiny("Terminado");
        }
    }

    private String getPingDisplay(int ping) {
        if (ping < 60) return ChatColor.GREEN + TextUtil.toTiny(ping + "ms") + ChatColor.DARK_GRAY + " ▂▃▅";
        if (ping < 120) return ChatColor.YELLOW + TextUtil.toTiny(ping + "ms") + ChatColor.DARK_GRAY + " ▂▃";
        return ChatColor.RED + TextUtil.toTiny(ping + "ms") + ChatColor.DARK_GRAY + " ▂";
    }
}
