package me.PauMAVA.TTR.listeners;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class GameJoinListener implements Listener {

    private final TTRCore plugin;

    public GameJoinListener(TTRCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                MatchStatus status = plugin.getCurrentMatch().getStatus();
                TTRTeam team = plugin.getTeamHandler().getPlayerTeam(player);

                // Caso 1: Reconexión en partida activa con equipo asignado
                if (status == MatchStatus.INGAME && team != null) {
                    player.setGameMode(GameMode.SURVIVAL);
                    if (plugin.getCurrentMatch().getBossBar() != null) {
                        plugin.getCurrentMatch().getBossBar().addPlayer(player);
                    }
                    player.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("¡Reconectado a la partida! Continuando..."));
                    return;
                }

                // Caso 2: Jugador en Lobby o Late-Join sin equipo
                player.setGameMode(GameMode.ADVENTURE);
                if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
                    player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
                }
                player.setHealth(20);
                player.setFoodLevel(20);
                player.setSaturation(20);
                player.setExp(0);
                player.setLevel(0);
                player.setFireTicks(0);
                player.setFlying(false);
                player.setAllowFlight(false);

                // Limpiar inventario
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }

                // Teletransportar al Lobby (unificado mediante ConfigManager)
                Location lobby = plugin.getConfigManager().getLobbyLocation();
                if (lobby != null) {
                    player.teleport(lobby);
                }

                // Dar estrella para selector de equipo
                if (status == MatchStatus.LOBBY || status == MatchStatus.INGAME) {
                    giveTeamSelector(player);
                    if (status == MatchStatus.INGAME) {
                        player.sendMessage(TTRPrefix.TTR_GAME + ChatColor.YELLOW + TextUtil.toTiny("Partida en curso. Usa la Estrella para unirte."));
                    }
                }

                // Verificar autostart
                plugin.getAutoStarter().addPlayerToQueue(player);
            }
        }.runTaskLater(plugin, 2L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getAutoStarter().removePlayerFromQueue(event.getPlayer());
        plugin.getScoreboard().removePlayer(event.getPlayer());
    }

    private void giveTeamSelector(Player player) {
        ItemStack star = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = star.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + TextUtil.toTiny("Elegir Equipo") + ChatColor.GRAY + " (Click Derecho)");
            star.setItemMeta(meta);
        }
        player.getInventory().setItem(4, star);
    }
}
