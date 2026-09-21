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

        // Enlace nativo al Discord oficial en el menú de pausa (Esc)
        try {
            org.bukkit.ServerLinks links = org.bukkit.Bukkit.getServer().getServerLinks().copy();
            links.addLink(org.bukkit.ServerLinks.Type.COMMUNITY, java.net.URI.create("https://discord.gg/destinyowners"));
            player.sendLinks(links);
        } catch (Throwable ignored) {}

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                MatchStatus status = plugin.getCurrentMatch() != null ? plugin.getCurrentMatch().getStatus() : MatchStatus.STOPPED;
                TTRTeam team = plugin.getTeamHandler() != null ? plugin.getTeamHandler().getPlayerTeam(player) : null;
                boolean isMatchActive = (status == MatchStatus.INGAME || status == MatchStatus.PREPARATION);

                // CASO 1: Jugador desconectado que se RECONECTA durante partida activa con equipo asignado
                // NO se toca su inventario ni su posición; aparece exactamente donde se desconectó y sigue jugando
                if (isMatchActive && team != null) {
                    player.setGameMode(GameMode.SURVIVAL);
                    if (plugin.getCurrentMatch().getBossBar() != null) {
                        plugin.getCurrentMatch().getBossBar().addPlayer(player);
                    }
                    if (plugin.getCurrentMatch().getPrepBar() != null) {
                        plugin.getCurrentMatch().getPrepBar().addPlayer(player);
                    }
                    me.PauMAVA.TTR.voice.VoiceChatManager.getInstance().assignPlayerToTeamVoice(player, team.getIdentifier());

                    // Actualizar scoreboard
                    if (plugin.getScoreboard() != null) {
                        plugin.getScoreboard().update(player);
                    }

                    player.sendMessage(TTRPrefix.TTR_SUCCESS + ChatColor.GREEN + "" + ChatColor.BOLD + 
                            TextUtil.toTiny("¡Reconectado con éxito! Continuando en tu posición con el equipo ") + 
                            team.getColor() + team.getIdentifier() + ".");
                    return;
                }

                // CASO 2: Jugador que entra cuando la partida ya está en curso pero no tiene equipo asignado (Late-Join)
                if (isMatchActive && team == null) {
                    player.setGameMode(GameMode.ADVENTURE);
                    player.getInventory().clear();
                    player.getInventory().setArmorContents(null);
                    player.getInventory().setItemInOffHand(null);

                    Location lobby = plugin.getConfigManager().getLobbyLocation();
                    if (lobby != null) {
                        player.teleport(lobby);
                    }

                    // En el lobby durante partida activa NO recibe estrella ni nada
                    player.sendMessage(TTRPrefix.TTR_GAME + ChatColor.YELLOW + 
                            TextUtil.toTiny("Hay una partida en curso. Puedes observar la partida con ") + 
                            ChatColor.AQUA + "/dt spectate" + ChatColor.YELLOW + ".");

                    if (plugin.getScoreboard() != null) {
                        plugin.getScoreboard().update(player);
                    }
                    return;
                }

                // CASO 3: Jugador en Lobby normal / Sala de espera (NO hay partida en curso)
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

                // Limpiar inventario completamente
                player.getInventory().clear();
                player.getInventory().setArmorContents(null);
                player.getInventory().setItemInOffHand(null);
                for (PotionEffect effect : player.getActivePotionEffects()) {
                    player.removePotionEffect(effect.getType());
                }

                // Teletransportar al Lobby
                Location lobby = plugin.getConfigManager().getLobbyLocation();
                if (lobby != null) {
                    player.teleport(lobby);
                }

                // Si la subasta está activa, dar ítem de subasta
                if (plugin.getAuctionDraftManager() != null && plugin.getAuctionDraftManager().isActive()) {
                    plugin.getAuctionDraftManager().giveViewerAuctionItem(player);
                    me.PauMAVA.TTR.ui.AuctionDraftGUI.open(player, plugin.getAuctionDraftManager());
                } else if (TTRCore.isAdmin(player)) {
                    // Ítem de configuración únicamente para administradores
                    ItemStack config = new ItemStack(Material.COMPARATOR);
                    ItemMeta cMeta = config.getItemMeta();
                    if (cMeta != null) {
                        cMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "⚙ " + TextUtil.toTiny("Configuración") + ChatColor.GRAY + " (" + TextUtil.toTiny("Clic Derecho") + ")");
                        config.setItemMeta(cMeta);
                    }
                    player.getInventory().setItem(0, config);
                }
                // ¡YA NO SE ENTREGA LA ESTRELLA DEL NETHER!

                // Cola de inicio automático
                plugin.getAutoStarter().addPlayerToQueue(player);

                // Scoreboard inicial
                if (plugin.getScoreboard() != null) {
                    plugin.getScoreboard().update(player);
                }
            }
        }.runTaskLater(plugin, 2L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getAutoStarter().removePlayerFromQueue(event.getPlayer());
        plugin.getScoreboard().removePlayer(event.getPlayer());
    }
}
