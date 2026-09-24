package me.PauMAVA.TTR.util;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.ui.BeaconShop;
import me.PauMAVA.TTR.ui.ConfigGUI;
import me.PauMAVA.TTR.listeners.TeamSelectListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import me.PauMAVA.TTR.modes.LeaderVoteManager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import me.PauMAVA.TTR.match.ChestRestockManager;
import me.PauMAVA.TTR.match.LobbyParkourManager;
import me.PauMAVA.TTR.match.ZoneWandManager;
import me.PauMAVA.TTR.ui.AuctionDraftGUI;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.Color;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Arrow;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.EventPriority;
import me.PauMAVA.TTR.web.WebStatsManager;

public class EventListener implements Listener {

    private final TTRCore plugin;
    private final int PROTECTION_RADIUS = 6;

    public EventListener(TTRCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getBlock().getWorld())) return;
        Player p = event.getPlayer();

        // Evitar romper bloques accidentalmente con la varita en modo creativo
        if (p.getInventory().getItemInMainHand() != null && ZoneWandManager.getInstance().isWand(p.getInventory().getItemInMainHand())) {
            event.setCancelled(true);
            return;
        }

        if (event.getBlock().getType() == Material.BEACON) {
            if (plugin.isBeaconShopEnabled()) {
                event.setCancelled(true);
                p.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡El Faro con tienda es indestructible!"));
                return;
            } else {
                if (plugin.getCurrentMatch() == null || plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
                    if (p.getGameMode() != GameMode.CREATIVE) {
                        event.setCancelled(true);
                        p.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡No puedes romper faros fuera de la partida!"));
                        return;
                    }
                }
            }
        }

        if (plugin.getRollbackManager() != null && plugin.getRollbackManager().isEditMode() && TTRCore.isAdmin(p)) {
            return;
        }

        if (plugin.getCurrentMatch() == null || plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
            event.setCancelled(true);
            return;
        }

        if (p.getGameMode() != GameMode.SURVIVAL) {
            event.setCancelled(true);
            return;
        }

        if (isCageZone(event.getBlock().getLocation())) {
            if (!(p.getGameMode() == GameMode.CREATIVE && TTRCore.isAdmin(p))) {
                event.setCancelled(true);
                p.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡Zona de puntuación protegida! No puedes romper el orificio de anotación."));
                return;
            }
        }

        if (isSpawnZone(event.getBlock().getLocation())) {
            if (!(p.getGameMode() == GameMode.CREATIVE && TTRCore.isAdmin(p))) {
                event.setCancelled(true);
                p.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡No puedes romper bloques en la zona de Spawn!"));
                return;
            }
        }

        if (isChestOrContainer(event.getBlock())) {
            String baseTeam = plugin.getConfigManager().getTeamForChest(event.getBlock().getLocation());
            if (baseTeam != null) {
                TTRTeam playerTeam = plugin.getTeamHandler().getPlayerTeam(p);
                if (playerTeam != null && !playerTeam.getIdentifier().equalsIgnoreCase(baseTeam)) {
                    if (!(p.getGameMode() == GameMode.CREATIVE && TTRCore.isAdmin(p))) {
                        event.setCancelled(true);
                        p.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡No puedes romper cofres del equipo " + baseTeam + "!"));
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.7f);
                        return;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getBlock().getWorld())) return;
        Player p = event.getPlayer();
        if (plugin.getRollbackManager() != null && plugin.getRollbackManager().isEditMode() && TTRCore.isAdmin(p)) {
            return;
        }

        if (plugin.getCurrentMatch() == null || plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
            event.setCancelled(true);
            return;
        }

        if (p.getGameMode() != GameMode.SURVIVAL) {
            event.setCancelled(true);
            return;
        }

        if (isCageZone(event.getBlock().getLocation()) || isCageZone(event.getBlockPlaced().getLocation())) {
            if (!(p.getGameMode() == GameMode.CREATIVE && TTRCore.isAdmin(p))) {
                event.setCancelled(true);
                showGhostProtection(p, event.getBlockPlaced().getLocation());
                p.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡Zona de puntuación protegida! No puedes construir aquí."));
                return;
            }
        }

        if (isSpawnZone(event.getBlock().getLocation()) || isSpawnZone(event.getBlockPlaced().getLocation())) {
            if (!(p.getGameMode() == GameMode.CREATIVE && TTRCore.isAdmin(p))) {
                event.setCancelled(true);
                showGhostProtection(p, event.getBlockPlaced().getLocation());
                p.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡No puedes construir en la zona de Spawn!"));
            }
        }
    }

    private void showGhostProtection(Player p, Location loc) {
        p.sendBlockChange(loc, Material.RED_STAINED_GLASS.createBlockData());
        p.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.6f);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (p.isOnline()) {
                    p.sendBlockChange(loc, loc.getBlock().getBlockData());
                }
            }
        }.runTaskLater(plugin, 10L);
    }

    @EventHandler
    public void onBucketEmpty(org.bukkit.event.player.PlayerBucketEmptyEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getBlockClicked().getWorld())) return;
        if (event.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        Location target = event.getBlockClicked().getRelative(event.getBlockFace()).getLocation();
        if (isCageZone(target) || isSpawnZone(target)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡No puedes derramar líquidos en esta zona protegida!"));
        }
    }

    @EventHandler
    public void onBlockFromTo(org.bukkit.event.block.BlockFromToEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getBlock().getWorld())) return;
        if (isCageZone(event.getToBlock().getLocation()) || isSpawnZone(event.getToBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonExtend(org.bukkit.event.block.BlockPistonExtendEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getBlock().getWorld())) return;
        for (org.bukkit.block.Block b : event.getBlocks()) {
            if (isCageZone(b.getLocation()) || isSpawnZone(b.getLocation()) ||
                isCageZone(b.getRelative(event.getDirection()).getLocation()) || isSpawnZone(b.getRelative(event.getDirection()).getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(org.bukkit.event.block.BlockPistonRetractEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getBlock().getWorld())) return;
        for (org.bukkit.block.Block b : event.getBlocks()) {
            if (isCageZone(b.getLocation()) || isSpawnZone(b.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onEntityExplode(org.bukkit.event.entity.EntityExplodeEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getLocation().getWorld())) return;
        event.blockList().removeIf(b -> isCageZone(b.getLocation()) || isSpawnZone(b.getLocation()));
    }

    @EventHandler
    public void onBlockExplode(org.bukkit.event.block.BlockExplodeEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getBlock().getWorld())) return;
        event.blockList().removeIf(b -> isCageZone(b.getLocation()) || isSpawnZone(b.getLocation()));
    }

    public boolean isCageZone(Location blockLoc) {
        if (blockLoc == null || blockLoc.getWorld() == null) return false;
        for (TTRTeam team : plugin.getTeamHandler().getTeams()) {
            List<Location> cages = plugin.getConfigManager().getTeamCages(team.getIdentifier());
            if (cages != null) {
                for (Location cage : cages) {
                    if (cage != null && cage.getWorld() != null && cage.getWorld().equals(blockLoc.getWorld())) {
                        double dx = Math.abs(cage.getX() - blockLoc.getX());
                        double dz = Math.abs(cage.getZ() - blockLoc.getZ());
                        double dy = blockLoc.getY() - cage.getY();
                        // Reducido estrictamente al orificio del punto de anotación (radio ~1.2 y altura 2.5)
                        // para que los jugadores puedan construir puentes y romper bloques alrededor de la torre
                        if (dx <= 1.2 && dz <= 1.2 && dy >= -1.0 && dy <= 2.5) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isSpawnZone(Location blockLoc) {
        if (blockLoc == null || blockLoc.getWorld() == null) return false;
        for (TTRTeam team : plugin.getTeamHandler().getTeams()) {
            Location spawn = plugin.getConfigManager().getTeamSpawn(team.getIdentifier());
            if (spawn != null && spawn.getWorld() != null && spawn.getWorld().equals(blockLoc.getWorld())) {
                double dx = Math.abs(spawn.getX() - blockLoc.getX());
                double dz = Math.abs(spawn.getZ() - blockLoc.getZ());
                double dy = blockLoc.getY() - spawn.getY();
                // Protección reducida exclusivamente a la celda de reaparición
                if (dx <= 3.5 && dz <= 3.5 && dy >= -1.0 && dy <= 5.0) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isChestOrContainer(org.bukkit.block.Block b) {
        if (b == null) return false;
        Material m = b.getType();
        return m == Material.CHEST || m == Material.TRAPPED_CHEST || m == Material.BARREL ||
               m == Material.ENDER_CHEST || m == Material.SHULKER_BOX || b.getState() instanceof Container;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getPlayer().getWorld())) return;
        Player p = event.getPlayer();

        // Control de caída rápida al vacío en partida (Y < 130)
        if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                if (p.getLocation().getY() < 130.0) {
                    p.damage(1000.0);
                    return;
                }
            }
        }

        // Control de caída al vacío en Lobby o Fuera de Partida
        if (plugin.getCurrentMatch() == null || plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
            Location lobby = plugin.getConfigManager().getLobbyLocation();

            // Mantener a todos los jugadores en el spawn durante la fase de subasta hasta que inicie la estrategia
            if (plugin.getAuctionDraftManager() != null && plugin.getAuctionDraftManager().isActive() && p.getGameMode() != GameMode.SPECTATOR) {
                if (lobby != null && lobby.getWorld() != null) {
                    if (!p.getWorld().equals(lobby.getWorld()) || p.getLocation().distanceSquared(lobby) > 1225.0) {
                        p.teleport(lobby);
                        p.setVelocity(new Vector(0, 0, 0));
                        p.setFallDistance(0f);
                        return;
                    }
                }
            }

            if (lobby != null && lobby.getWorld() != null && p.getWorld().equals(lobby.getWorld())) {
                if (p.getLocation().getY() < (lobby.getY() - 15.0) || p.getLocation().getY() < 60.0) {
                    if (LobbyParkourManager.getInstance().isDoingParkour(p)) {
                        LobbyParkourManager.getInstance().handleFall(p);
                    } else {
                        p.teleport(lobby);
                        p.setVelocity(new Vector(0, 0, 0));
                        p.setFallDistance(0f);
                    }
                    return;
                }
            }

            // Detección de placas de presión del Parkour del Lobby
            Location to = event.getTo();
            if (to != null && to.getBlock().getType().toString().endsWith("_PRESSURE_PLATE")) {
                LobbyParkourManager parkour = LobbyParkourManager.getInstance();
                Location blockLoc = to.getBlock().getLocation();
                if (parkour.getStartLocation() != null && parkour.getStartLocation().getBlock().equals(to.getBlock())) {
                    parkour.startParkour(p);
                } else if (parkour.getEndLocation() != null && parkour.getEndLocation().getBlock().equals(to.getBlock())) {
                    parkour.finishParkour(p);
                } else {
                    List<Location> cps = parkour.getCheckpoints();
                    for (int i = 0; i < cps.size(); i++) {
                        if (cps.get(i).getBlock().equals(to.getBlock())) {
                            parkour.triggerCheckpoint(p, i + 1, blockLoc);
                            break;
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getPlayer().getWorld())) return;
        Player player = event.getPlayer();

        // Herramienta Vara de Zonas (/dt wand)
        if (event.getItem() != null && ZoneWandManager.getInstance().isWand(event.getItem()) && TTRCore.isAdmin(player)) {
            if (event.getClickedBlock() != null && isChestOrContainer(event.getClickedBlock())) {
                event.setCancelled(true);
                ZoneWandManager.getInstance().handleChestClick(player, event.getClickedBlock(), player.isSneaking());
                return;
            }
            if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getClickedBlock() != null) {
                event.setCancelled(true);
                ZoneWandManager.getInstance().setPos1(player, event.getClickedBlock().getLocation());
                return;
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
                event.setCancelled(true);
                ZoneWandManager.getInstance().setPos2(player, event.getClickedBlock().getLocation());
                return;
            }
        }

        // Ítem de Subasta para Líderes (Panel de Subasta)
        if (event.getItem() != null && event.getItem().getType() == Material.GOLD_INGOT && event.getItem().hasItemMeta()) {
            if (event.getItem().getItemMeta().getDisplayName().contains(TextUtil.toTiny("Subasta"))) {
                event.setCancelled(true);
                if (plugin.getAuctionDraftManager().isActive()) {
                    AuctionDraftGUI.open(player, plugin.getAuctionDraftManager());
                } else {
                    player.sendMessage(TTRPrefix.TTR_GAME + TextUtil.toTiny("No hay subasta activa en este momento."));
                }
                return;
            }
        }

        // Ítem de Votación de Líderes
        if (event.getItem() != null && plugin.getLeaderVoteManager().isVoteItem(event.getItem())) {
            event.setCancelled(true);
            if (plugin.getLeaderVoteManager().isActive()) {
                TTRTeam team = plugin.getTeamHandler().getPlayerTeam(player);
                if (team != null) {
                    LeaderVoteManager.TeamVoteState state = plugin.getLeaderVoteManager().getState(team.getIdentifier());
                    if (state != null) {
                        me.PauMAVA.TTR.ui.LeaderVoteGUI.open(player, state);
                        return;
                    }
                }
                player.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("No estás asignado a un equipo en votación."));
            } else {
                player.sendMessage(TTRPrefix.TTR_GAME + TextUtil.toTiny("No hay votación activa en este momento."));
            }
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (isChestOrContainer(event.getClickedBlock())) {
                String baseTeam = plugin.getConfigManager().getTeamForChest(event.getClickedBlock().getLocation());
                if (baseTeam != null) {
                    TTRTeam playerTeam = plugin.getTeamHandler().getPlayerTeam(player);
                    if (playerTeam != null && !playerTeam.getIdentifier().equalsIgnoreCase(baseTeam)) {
                        if (!(player.getGameMode() == GameMode.CREATIVE && TTRCore.isAdmin(player))) {
                            event.setCancelled(true);
                            player.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡No puedes abrir cofres del equipo " + baseTeam + "!"));
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.7f);
                            return;
                        }
                    }
                }
            }

            if (event.getClickedBlock().getType() == Material.BEACON) {
                if (plugin.isBeaconShopEnabled()) {
                    event.setCancelled(true);
                    new BeaconShop().openMain(player);
                    return;
                }
                // Tienda desactivada: permitir el faro vanilla normal
                return;
            }
        }

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem() != null) {
                if (event.getItem().getType() == Material.COMPARATOR && player.getWorld().getName().equalsIgnoreCase("the-towers") && (player.hasPermission("destinytowers.admin") || player.hasPermission("ttr.admin") || player.isOp())) {
                    event.setCancelled(true);
                    ConfigGUI.open(player);
                    return;
                }
                if (event.getItem().getType() == Material.FIRE_CHARGE) {
                    // Cargas de fuego desactivadas completamente
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getPlayer().getWorld())) return;
        if (event.getInventory().getHolder() instanceof Container || event.getInventory().getHolder() instanceof DoubleChest) {
            ChestRestockManager.getInstance().purgeLiquids(event.getInventory());
        }

        if (event.getPlayer() instanceof Player player) {
            Location invLoc = event.getInventory().getLocation();
            if (invLoc != null) {
                String baseTeam = plugin.getConfigManager().getTeamForChest(invLoc);
                if (baseTeam != null) {
                    TTRTeam playerTeam = plugin.getTeamHandler().getPlayerTeam(player);
                    if (playerTeam != null && !playerTeam.getIdentifier().equalsIgnoreCase(baseTeam)) {
                        if (!(player.getGameMode() == GameMode.CREATIVE && TTRCore.isAdmin(player))) {
                            event.setCancelled(true);
                            player.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡No puedes abrir cofres del equipo " + baseTeam + "!"));
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.7f);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getWhoClicked().getWorld())) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Fase de preparación: pueden mirar los cofres pero NO agarrar ningún ítem
        if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().getStatus() == MatchStatus.PREPARATION) {
            if (event.getView().getTopInventory().getHolder() instanceof Container || event.getView().getTopInventory().getHolder() instanceof DoubleChest) {
                event.setCancelled(true);
                player.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡Fase de preparación! Solo puedes inspeccionar el cofre."));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.7f);
                return;
            }
        }

        // Prohibir agarrar o mover cualquier cubo de líquido
        ItemStack current = event.getCurrentItem();
        if (current != null && ChestRestockManager.isLiquidBucket(current.getType())) {
            event.setCurrentItem(null);
            event.setCancelled(true);
            player.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("¡Los líquidos están prohibidos en las torres!"));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.7f);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getEntity().getWorld())) return;
        event.setDroppedExp(0);

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            plugin.getCurrentMatch().playerDeath(victim, killer);

            TTRTeam victimTeam = plugin.getTeamHandler().getPlayerTeam(victim);
            String vName = (victimTeam != null ? victimTeam.getColor() : ChatColor.GRAY) + victim.getName();

            if (killer != null && !killer.equals(victim)) {
                TTRTeam killerTeam = plugin.getTeamHandler().getPlayerTeam(killer);
                String kName = (killerTeam != null ? killerTeam.getColor() : ChatColor.GRAY) + killer.getName();
                if (victim.getLastDamageCause() != null && victim.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID) {
                    event.setDeathMessage(ChatColor.DARK_GRAY + "☠ " + vName + ChatColor.GRAY + TextUtil.toTiny(" fue empujado al vacío por ") + kName + ChatColor.GRAY + ".");
                } else {
                    event.setDeathMessage(ChatColor.DARK_GRAY + "☠ " + vName + ChatColor.GRAY + TextUtil.toTiny(" fue asesinado por ") + kName + ChatColor.GRAY + ".");
                }
            } else {
                if (victim.getLastDamageCause() != null && victim.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID) {
                    event.setDeathMessage(ChatColor.DARK_GRAY + "☠ " + vName + ChatColor.GRAY + TextUtil.toTiny(" cayó al vacío."));
                } else {
                    event.setDeathMessage(ChatColor.DARK_GRAY + "☠ " + vName + ChatColor.GRAY + TextUtil.toTiny(" ha muerto."));
                }
            }

            // Registro asíncrono para la web en tiempo real
            double dist = (killer != null) ? killer.getLocation().distance(victim.getLocation()) : 0.0;
            String cause = (victim.getLastDamageCause() != null && victim.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID) ? "VACÍO" : "COMBATE";
            WebStatsManager.getInstance().recordKill(killer, victim, cause, dist);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getEntity().getWorld())) return;
        if (event.getEntity() instanceof Player victim) {
            double damage = event.getFinalDamage();
            if (damage <= 0.1) return;

            boolean isCrit = false;
            Player attacker = null;
            if (event.getDamager() instanceof Player p) {
                attacker = p;
                isCrit = p.getFallDistance() > 0.0f && !p.isOnGround() && !p.hasPotionEffect(PotionEffectType.BLINDNESS);
            } else if (event.getDamager() instanceof Arrow arrow && arrow.getShooter() instanceof Player p) {
                attacker = p;
                isCrit = arrow.isCritical();
            }

            spawnDamageIndicator(victim.getLocation(), damage, isCrit);
        }
    }

    private void spawnDamageIndicator(Location loc, double damage, boolean isCrit) {
        World world = loc.getWorld();
        if (world == null) return;

        Location spawnLoc = loc.clone().add((Math.random() - 0.5) * 0.8, 1.2 + Math.random() * 0.4, (Math.random() - 0.5) * 0.8);
        String text = isCrit ? 
                String.format("§e§l⚡ -%.1f", damage) : 
                String.format("§c-%.1f ❤", damage);

        try {
            TextDisplay display = world.spawn(spawnLoc, TextDisplay.class, entity -> {
                entity.text(net.kyori.adventure.text.Component.text(text));
                entity.setBillboard(Display.Billboard.CENTER);
                entity.setDefaultBackground(false);
                entity.setBackgroundColor(Color.fromARGB(120, 0, 0, 0));
                entity.setBrightness(new Display.Brightness(15, 15));
            });

            new BukkitRunnable() {
                int ticks = 0;
                @Override
                public void run() {
                    ticks++;
                    if (!display.isValid() || ticks > 16) {
                        display.remove();
                        this.cancel();
                        return;
                    }
                    display.teleport(display.getLocation().add(0, 0.04, 0));
                }
            }.runTaskTimer(plugin, 1L, 1L);
        } catch (Throwable ignored) {}
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpectatorDamageBlock(EntityDamageByEntityEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getEntity().getWorld())) return;

        Player attacker = null;
        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else if (event.getDamager() instanceof org.bukkit.entity.Projectile proj && proj.getShooter() instanceof Player p) {
            attacker = p;
        }

        if (attacker != null) {
            if (attacker.getGameMode() != GameMode.SURVIVAL || plugin.getTeamHandler().getPlayerTeam(attacker) == null) {
                event.setCancelled(true);
                return;
            }
        }

        if (event.getEntity() instanceof Player victim) {
            if (victim.getGameMode() != GameMode.SURVIVAL || plugin.getTeamHandler().getPlayerTeam(victim) == null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onGameModeChange(org.bukkit.event.player.PlayerGameModeChangeEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getPlayer().getWorld())) return;
        Player p = event.getPlayer();
        if (event.getNewGameMode() == GameMode.SPECTATOR) {
            p.getInventory().clear();
            p.getInventory().setArmorContents(null);
            p.getInventory().setItemInOffHand(null);
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.getGameMode() != GameMode.SPECTATOR) {
                    other.hidePlayer(plugin, p);
                }
            }
        } else if (event.getNewGameMode() == GameMode.SURVIVAL) {
            for (Player other : Bukkit.getOnlinePlayers()) {
                other.showPlayer(plugin, p);
            }
            if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
                TTRTeam team = plugin.getTeamHandler().getPlayerTeam(p);
                if (team != null) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            plugin.getCurrentMatch().equipPlayer(p, team.getIdentifier());
                        }
                    }.runTaskLater(plugin, 1L);
                }
            }
        } else {
            for (Player other : Bukkit.getOnlinePlayers()) {
                other.showPlayer(plugin, p);
            }
            p.getInventory().clear();
            p.getInventory().setArmorContents(null);
            p.getInventory().setItemInOffHand(null);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getPlayer().getWorld())) return;

        if (plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            Player player = event.getPlayer();
            TTRTeam team = plugin.getTeamHandler().getPlayerTeam(player);

            if (team != null) {
                Location teamSpawn = plugin.getConfigManager().getTeamSpawn(team.getIdentifier());
                if (teamSpawn != null) event.setRespawnLocation(teamSpawn);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        plugin.getCurrentMatch().equipPlayer(player, team.getIdentifier());
                    }
                }.runTaskLater(plugin, 1L);
            }
        } else {
            Location lobby = plugin.getConfigManager().getLobbyLocation();
            if (lobby != null) event.setRespawnLocation(lobby);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getPlayer().getWorld())) return;
        Player p = event.getPlayer();

        if (plugin.getRollbackManager() != null && plugin.getRollbackManager().isEditMode() && TTRCore.isAdmin(p)) {
            return;
        }

        if (plugin.getCurrentMatch() == null || plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME || p.getGameMode() != GameMode.SURVIVAL) {
            if (!(p.getGameMode() == GameMode.CREATIVE && TTRCore.isAdmin(p))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getEntity().getWorld())) return;
        if (!(event.getEntity() instanceof Player p)) return;

        if (plugin.getRollbackManager() != null && plugin.getRollbackManager().isEditMode() && TTRCore.isAdmin(p)) {
            return;
        }

        if (plugin.getCurrentMatch() == null || plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME || p.getGameMode() != GameMode.SURVIVAL) {
            if (!(p.getGameMode() == GameMode.CREATIVE && TTRCore.isAdmin(p))) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getEntity().getWorld())) return;

        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if (reason == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG ||
                reason == CreatureSpawnEvent.SpawnReason.COMMAND ||
                reason == CreatureSpawnEvent.SpawnReason.CUSTOM ||
                reason == CreatureSpawnEvent.SpawnReason.DEFAULT) {
            return; // Permitir invocaciones manuales con huevos, /summon o plugins
        }

        // Bloquear spawns automáticos o naturales de mobs en Hard difficulty
        event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getEntity().getWorld())) return;
        if (event.getEntity() instanceof Player player) {
            if (plugin.getCurrentMatch() == null || plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
                event.setCancelled(true);
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    if (LobbyParkourManager.getInstance().isDoingParkour(player)) {
                        LobbyParkourManager.getInstance().handleFall(player);
                    } else {
                        Location lobby = plugin.getConfigManager().getLobbyLocation();
                        if (lobby != null) {
                            player.teleport(lobby.clone().add(0, 4, 0));
                            player.setVelocity(new Vector(0, 1.2, 0));
                            player.setFallDistance(0f);
                            player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOW_FALLING, 60, 0, false, false, false));
                            if (lobby.getWorld() != null) {
                                lobby.getWorld().spawnParticle(Particle.FIREWORK, lobby, 30, 0.4, 0.4, 0.4, 0.1);
                                lobby.getWorld().spawnParticle(Particle.CLOUD, lobby, 20, 0.3, 0.2, 0.3, 0.05);
                            }
                            player.playSound(lobby, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.2f);
                        }
                    }
                }
                return;
            }

            // Muerte instantánea por vacío durante la partida
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                event.setDamage(1000.0);
                return;
            }

            // Spawn protection en partida contra spawn-killing dentro de la celda
            TTRTeam victimTeam = plugin.getTeamHandler().getPlayerTeam(player);
            if (victimTeam != null) {
                Location spawn = plugin.getConfigManager().getTeamSpawn(victimTeam.getIdentifier());
                if (spawn != null && spawn.getWorld() != null && spawn.getWorld().equals(player.getWorld())) {
                    double dx = Math.abs(spawn.getX() - player.getLocation().getX());
                    double dz = Math.abs(spawn.getZ() - player.getLocation().getZ());
                    double dy = player.getLocation().getY() - spawn.getY();
                    if (dx <= 4.0 && dz <= 4.0 && dy >= -1.0 && dy <= 5.0) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (!plugin.enabled()) return;
        if (!TTRCore.isTowersWorld(event.getEntity().getWorld())) return;
        if (plugin.getCurrentMatch().getStatus() != MatchStatus.INGAME) {
            event.setCancelled(true);
            event.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (!plugin.enabled()) return;
        if (event.getView().getPlayer() != null && !TTRCore.isTowersWorld(event.getView().getPlayer().getWorld())) return;
        if (event.getRecipe() != null && event.getRecipe().getResult().getType() == Material.SHIELD) {
            event.getInventory().setResult(new ItemStack(Material.AIR));
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.HIGHEST)
    public void onChat(org.bukkit.event.player.AsyncPlayerChatEvent event) {
        if (!plugin.enabled()) return;
        Player p = event.getPlayer();
        if (!TTRCore.isTowersWorld(p.getWorld())) {
            // No intervenir en el chat de jugadores que están en el Lobby general o en SkyWars
            return;
        }

        // Garantizar que el chat NUNCA esté cancelado para nadie en ningún momento ni fase dentro de The Towers
        event.setCancelled(false);
        // Filtrar destinatarios: solo jugadores dentro de The Towers
        event.getRecipients().removeIf(target -> !TTRCore.isTowersWorld(target.getWorld()));

        TTRTeam team = plugin.getTeamHandler().getPlayerTeam(p);
        boolean isStaff = TTRCore.isAdmin(p);

        if (team != null && team.isLeader(p.getUniqueId())) {
            String leaderBadge = ChatColor.GOLD + "★ [" + team.getColor() + TextUtil.toTiny("Líder ") + team.getColor() + TextUtil.toTiny(team.getIdentifier()) + ChatColor.GOLD + "] " + ChatColor.RESET;
            event.setFormat(leaderBadge + team.getColor() + "%1$s" + ChatColor.DARK_GRAY + " » " + ChatColor.WHITE + "%2$s");
        } else if (team != null) {
            String teamPrefix = ChatColor.DARK_GRAY + "[" + team.getColor() + TextUtil.toTiny(team.getIdentifier()) + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;
            event.setFormat(teamPrefix + team.getColor() + "%1$s" + ChatColor.DARK_GRAY + " » " + ChatColor.WHITE + "%2$s");
        } else if (p.getGameMode() == GameMode.SPECTATOR) {
            String specPrefix = ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + TextUtil.toTiny("Espectador") + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;
            event.setFormat(specPrefix + ChatColor.GRAY + "%1$s" + ChatColor.DARK_GRAY + " » " + ChatColor.WHITE + "%2$s");
        } else if (isStaff) {
            event.setFormat(ChatColor.WHITE + "%1$s" + ChatColor.DARK_GRAY + " » " + ChatColor.WHITE + "%2$s");
        } else {
            // Jugador en lobby de The Towers o sin equipo asignado
            event.setFormat(ChatColor.GRAY + "%1$s" + ChatColor.DARK_GRAY + " » " + ChatColor.WHITE + "%2$s");
        }
    }
}
