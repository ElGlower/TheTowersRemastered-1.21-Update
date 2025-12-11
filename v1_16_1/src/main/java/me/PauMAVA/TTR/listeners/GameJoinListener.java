package me.PauMAVA.TTR.listeners;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
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

        // Ejecutamos con un pequeño retraso para asegurar que cargue bien
        new BukkitRunnable() {
            @Override
            public void run() {
                MatchStatus status = plugin.getCurrentMatch().getStatus();
                TTRTeam team = plugin.getTeamHandler().getPlayerTeam(player);

                // --- CASO 1: RECONEXIÓN EN PARTIDA (El jugador ya tiene equipo y es INGAME) ---
                if (status == MatchStatus.INGAME && team != null) {
                    // No limpiamos inventario, no teletransportamos.
                    // Solo nos aseguramos de que esté en Survival y vea la BossBar.
                    player.setGameMode(GameMode.SURVIVAL);

                    if (plugin.getCurrentMatch().getBossBar() != null) {
                        plugin.getCurrentMatch().getBossBar().addPlayer(player);
                    }

                    player.sendMessage(ChatColor.GREEN + "¡Has vuelto a la partida! Continuas donde lo dejaste.");
                    return; // ¡IMPORTANTE! Aquí terminamos para no mandarlo al Lobby
                }

                // --- CASO 2: NUEVO JUGADOR O PARTIDA EN LOBBY (Reset Normal) ---

                // 1. Forzar Modo Aventura y Restaurar Stats
                player.setGameMode(GameMode.ADVENTURE);
                if (player.getAttribute(Attribute.GENERIC_MAX_HEALTH) != null) {
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
                }
                player.setHealth(20);
                player.setFoodLevel(20);
                player.setSaturation(20);
                player.setExp(0);
                player.setLevel(0);
                player.setFireTicks(0);
                player.setFlying(false);
                player.setAllowFlight(false);

                // 2. Limpiar Inventario
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }

                // 3. Teletransportar al Lobby
                if (plugin.getConfig().contains("lobby")) {
                    Location lobby = plugin.getConfig().getLocation("lobby");
                    if (lobby != null) player.teleport(lobby);
                } else {
                    player.teleport(player.getWorld().getSpawnLocation());
                }

                // 4. Dar Selector (Si es Lobby o Late Join sin equipo)
                if (status == MatchStatus.LOBBY || status == MatchStatus.INGAME) {
                    giveTeamSelector(player);
                    if (status == MatchStatus.INGAME) {
                        player.sendMessage(ChatColor.YELLOW + "La partida está en curso.");
                        player.sendMessage(ChatColor.GREEN + "¡Usa la Estrella para unirte y jugar!");
                    }
                }
            }
        }.runTaskLater(plugin, 2L);
    }

    private void giveTeamSelector(Player player) {
        ItemStack star = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = star.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Elegir Equipo " + ChatColor.GRAY + "(Click Derecho)");
        star.setItemMeta(meta);
        player.getInventory().setItem(4, star);
    }
}