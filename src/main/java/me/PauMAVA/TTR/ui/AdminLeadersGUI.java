package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.modes.TeamSelectionMode;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.SkullUtil;
import me.PauMAVA.TTR.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.format.TextDecoration;

public class AdminLeadersGUI {

    public static final String TITLE = ChatColor.DARK_GRAY + "⚙ " + ChatColor.DARK_AQUA +
            TextUtil.toTiny("Gestor de Equipos y Líderes");

    public static final NamespacedKey KEY_ACTION = new NamespacedKey(TTRCore.getInstance(), "ttr_adm_action");
    public static final NamespacedKey KEY_PLAYER_UUID = new NamespacedKey(TTRCore.getInstance(), "ttr_adm_target");

    private static BukkitTask liveTask = null;

    public static void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);
        render(gui);
        player.openInventory(gui);
        startLiveUpdater();
    }

    public static void startLiveUpdater() {
        if (liveTask != null && !liveTask.isCancelled()) return;
        liveTask = Bukkit.getScheduler().runTaskTimer(TTRCore.getInstance(), () -> {
            boolean anyViewer = false;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getOpenInventory() != null && p.getOpenInventory().getTitle().equals(TITLE)) {
                    anyViewer = true;
                    updateLiveControls(p.getOpenInventory().getTopInventory());
                }
            }
            if (!anyViewer && liveTask != null) {
                liveTask.cancel();
                liveTask = null;
            }
        }, 20L, 20L);
    }

    private static void updateLiveControls(Inventory gui) {
        TTRCore plugin = TTRCore.getInstance();
        TeamSelectionMode mode = plugin.getCurrentSelectionMode();

        boolean voteActive = plugin.getLeaderVoteManager().isActive();
        boolean draftActive = plugin.getAuctionDraftManager().isActive();
        boolean announcing = plugin.getModeAnnouncementManager().isAnnouncing();
        boolean counting = plugin.isCounting();
        boolean prepActive = plugin.getCurrentMatch() != null && plugin.getCurrentMatch().isPreparing();

        if (announcing || voteActive || draftActive || counting || prepActive) {
            List<String> stopLore = new ArrayList<>();
            String currentPhaseName = "Fase Activa";
            if (announcing) {
                currentPhaseName = "Lectura de Reglas (" + plugin.getModeAnnouncementManager().getSecondsRemaining() + "s)";
            } else if (voteActive) {
                currentPhaseName = "Votación de Líder (" + plugin.getLeaderVoteManager().getSecondsRemaining() + "s)";
            } else if (draftActive) {
                currentPhaseName = "Subasta de Miembros (" + plugin.getAuctionDraftManager().getSecondsRemaining() + "s)";
            } else if (prepActive) {
                currentPhaseName = "Preparación en Bases (" + plugin.getCurrentMatch().getPrepRemaining() + "s)";
            } else if (counting) {
                currentPhaseName = "Conteo de Inicio (" + plugin.getAutoStarter().getCountdown() + "s)";
            }

            stopLore.add(ChatColor.GRAY + TextUtil.toTiny("Estado: ") + ChatColor.YELLOW + currentPhaseName);
            stopLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            stopLore.add(ChatColor.RED + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic: Cancelar fase y restablecer"));
            gui.setItem(4, createActionItem(Material.BARRIER, ChatColor.RED + "" + ChatColor.BOLD + "✖ " + TextUtil.toTiny("Detener / Cancelar Fase"), stopLore, "stop_phase"));

            List<String> skipLore = new ArrayList<>();
            skipLore.add(ChatColor.GRAY + TextUtil.toTiny("Fase: ") + ChatColor.YELLOW + currentPhaseName);
            skipLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            skipLore.add(ChatColor.AQUA + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic: Saltar fase / Iniciar de inmediato"));
            gui.setItem(5, createActionItem(Material.BEACON, ChatColor.AQUA + "" + ChatColor.BOLD + "⚡ " + TextUtil.toTiny("Saltar Fase"), skipLore, "skip_phase"));

            List<String> addTimeLore = new ArrayList<>();
            addTimeLore.add(ChatColor.GRAY + TextUtil.toTiny("Añade 15 segundos al cronómetro de la fase activa."));
            addTimeLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            addTimeLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic: +15 segundos"));
            gui.setItem(8, createActionItem(Material.CLOCK, ChatColor.YELLOW + "" + ChatColor.BOLD + "⏱ +15s " + TextUtil.toTiny("Tiempo"), addTimeLore, "add_time_15"));
        } else {
            ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta bm = border.getItemMeta();
            if (bm != null) {
                bm.setDisplayName(" ");
                border.setItemMeta(bm);
            }
            gui.setItem(8, border);

            int playingCount = 0;
            int adminCount = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!TTRCore.isTowersWorld(p.getWorld())) continue;
                if (p.getGameMode() == GameMode.SPECTATOR) adminCount++;
                else playingCount++;
            }
            int required = plugin.getConfig().getInt("autostart.count", 4);
            boolean ready = playingCount >= required;

            List<String> countLore = new ArrayList<>();
            countLore.add(ChatColor.GRAY + TextUtil.toTiny("Participantes: ") + (ready ? ChatColor.GREEN : ChatColor.RED) + playingCount + ChatColor.DARK_GRAY + " / " + ChatColor.WHITE + required);
            if (adminCount > 0) {
                countLore.add(ChatColor.GRAY + TextUtil.toTiny("Administradores: ") + ChatColor.YELLOW + adminCount + ChatColor.GRAY + " (Destiny)");
            }
            countLore.add(ChatColor.GRAY + TextUtil.toTiny("Mínimo recomendado para Subasta: 4"));
            countLore.add(ChatColor.GRAY + TextUtil.toTiny("Mínimo absoluto para 1v1: 2"));
            countLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            countLore.add(ready ? ChatColor.GREEN + "✔ " + TextUtil.toTiny("Cantidad suficiente para iniciar.") : ChatColor.RED + "✖ " + TextUtil.toTiny("Faltan jugadores para auto-inicio."));

            Material countMat = ready ? Material.LIME_DYE : Material.RED_DYE;
            gui.setItem(5, createActionItem(countMat, (ready ? ChatColor.GREEN : ChatColor.YELLOW) + "" + ChatColor.BOLD + "👥 " + TextUtil.toTiny("Jugadores: ") + playingCount + "/" + required, countLore, "none"));
        }
    }

    public static void render(Inventory gui) {
        TTRCore plugin = TTRCore.getInstance();

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bm = border.getItemMeta();
        if (bm != null) {
            bm.setDisplayName(" ");
            border.setItemMeta(bm);
        }

        // Top border items
        if (plugin.getAuctionDraftManager().isActive()) {
            List<String> credLore = new ArrayList<>();
            credLore.add(ChatColor.RED + "Rojo: " + ChatColor.GREEN + plugin.getAuctionDraftManager().getTeamCredits("red") + "c");
            credLore.add(ChatColor.BLUE + "Azul: " + ChatColor.GREEN + plugin.getAuctionDraftManager().getTeamCredits("blue") + "c");
            credLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            credLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Izq: +25 a Rojo | Der: +25 a Azul"));
            gui.setItem(1, createActionItem(Material.GOLD_INGOT, ChatColor.YELLOW + "" + ChatColor.BOLD + "💰 " + TextUtil.toTiny("Añadir Créditos"), credLore, "add_credits_quick"));
        } else {
            gui.setItem(1, border);
        }

        // Slot 7: Re-Roll de Equipos
        List<String> rerollLore = new ArrayList<>();
        rerollLore.add(ChatColor.GRAY + TextUtil.toTiny("Baraja aleatoriamente a todos"));
        rerollLore.add(ChatColor.GRAY + TextUtil.toTiny("los jugadores entre Rojo y Azul."));
        rerollLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        rerollLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic: Re-roll aleatorio"));
        gui.setItem(7, createActionItem(Material.DISPENSER, ChatColor.GOLD + "" + ChatColor.BOLD + "🎲 " + TextUtil.toTiny("Re-Roll de Equipos"), rerollLore, "reroll_teams"));

        // Center separator (rows 1 to 4)
        for (int row = 1; row < 5; row++) {
            gui.setItem(row * 9 + 4, border);
        }

        // 1. Selector de Modalidad (Slot 2)
        TeamSelectionMode mode = plugin.getCurrentSelectionMode();
        List<String> modeLore = new ArrayList<>();
        modeLore.add(ChatColor.GRAY + TextUtil.toTiny("Modalidad activa: ") + mode.getDisplayName());
        modeLore.add(ChatColor.DARK_GRAY + TextUtil.toTiny(mode.getDescription()));
        modeLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        modeLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic: Rotar y anunciar modalidad"));
        gui.setItem(2, createActionItem(Material.COMPASS, ChatColor.GOLD + "" + ChatColor.BOLD + "🎲 " + TextUtil.toTiny("Modo de Selección"), modeLore, "cycle_mode"));

        // 2. Ajustes de Tiempos y Créditos (Slot 3)
        List<String> setLore = new ArrayList<>();
        setLore.add(ChatColor.GRAY + TextUtil.toTiny("Configura segundos de votación, subasta,"));
        setLore.add(ChatColor.GRAY + TextUtil.toTiny("créditos de capitanes y tiempo de lectura."));
        setLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        setLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic: Abrir menú de tiempos"));
        gui.setItem(3, createActionItem(Material.CLOCK, ChatColor.AQUA + "" + ChatColor.BOLD + "⚙ " + TextUtil.toTiny("Ajustes de Tiempos"), setLore, "open_settings"));

        // 3. Control de Fase & Cancelación Rápida (Slot 4)
        boolean voteActive = plugin.getLeaderVoteManager().isActive();
        boolean draftActive = plugin.getAuctionDraftManager().isActive();
        boolean announcing = plugin.getModeAnnouncementManager().isAnnouncing();
        boolean counting = plugin.isCounting();

        if (announcing || voteActive || draftActive || counting) {
            List<String> stopLore = new ArrayList<>();
            String currentPhaseName = "Fase Activa";
            if (announcing) {
                currentPhaseName = "Lectura de Reglas (" + plugin.getModeAnnouncementManager().getSecondsRemaining() + "s)";
            } else if (voteActive) {
                currentPhaseName = "Votación de Líder (" + plugin.getLeaderVoteManager().getSecondsRemaining() + "s)";
            } else if (draftActive) {
                currentPhaseName = "Subasta de Miembros (" + plugin.getAuctionDraftManager().getSecondsRemaining() + "s)";
            } else if (counting) {
                currentPhaseName = "Conteo de Inicio (" + plugin.getAutoStarter().getCountdown() + "s)";
            }

            stopLore.add(ChatColor.GRAY + TextUtil.toTiny("Estado: ") + ChatColor.YELLOW + currentPhaseName);
            stopLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            stopLore.add(ChatColor.RED + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic: Cancelar fase y restablecer"));
            gui.setItem(4, createActionItem(Material.BARRIER, ChatColor.RED + "" + ChatColor.BOLD + "✖ " + TextUtil.toTiny("Detener / Cancelar Fase"), stopLore, "stop_phase"));
        } else if (mode == TeamSelectionMode.AUCTION_DRAFT) {
            List<String> draftLore = new ArrayList<>();
            draftLore.add(ChatColor.GRAY + TextUtil.toTiny("Inicia la fase completa de Subasta:"));
            draftLore.add(ChatColor.WHITE + "1. " + TextUtil.toTiny("Anuncio y barra de experiencia."));
            draftLore.add(ChatColor.WHITE + "2. " + TextUtil.toTiny("Votación democrática de líderes."));
            draftLore.add(ChatColor.WHITE + "3. " + TextUtil.toTiny("Subasta interactiva de miembros."));
            draftLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            draftLore.add(ChatColor.GREEN + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic: Iniciar Modo Puja / Subasta"));
            gui.setItem(4, createActionItem(Material.CHEST_MINECART, ChatColor.GREEN + "" + ChatColor.BOLD + "⚖ " + TextUtil.toTiny("Iniciar Modo Puja"), draftLore, "start_draft"));
        } else {
            List<String> stdLore = new ArrayList<>();
            stdLore.add(ChatColor.GRAY + TextUtil.toTiny("Inicia el modo clásico de selección"));
            stdLore.add(ChatColor.GRAY + TextUtil.toTiny("libre por menú o auto-equilibrio."));
            stdLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            stdLore.add(ChatColor.GREEN + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic: Iniciar Modo Estándar"));
            gui.setItem(4, createActionItem(Material.IRON_SWORD, ChatColor.AQUA + "" + ChatColor.BOLD + "🎮 " + TextUtil.toTiny("Iniciar Modo Estándar"), stdLore, "start_standard"));
        }

        // Slot 5: Omitir espera de anuncio O Indicador de Jugadores Requeridos
        if (announcing) {
            List<String> skipLore = new ArrayList<>();
            skipLore.add(ChatColor.GRAY + TextUtil.toTiny("Los jugadores están leyendo las reglas."));
            skipLore.add(ChatColor.GRAY + TextUtil.toTiny("Tiempo restante: ") + ChatColor.YELLOW + plugin.getModeAnnouncementManager().getSecondsRemaining() + "s");
            skipLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            skipLore.add(ChatColor.AQUA + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic: Comenzar ahora (Omitir espera)"));
            gui.setItem(5, createActionItem(Material.BEACON, ChatColor.AQUA + "" + ChatColor.BOLD + "⚡ " + TextUtil.toTiny("Comenzar Ahora"), skipLore, "skip_announcement"));
        } else {
            int playingCount = 0;
            int adminCount = 0;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!TTRCore.isTowersWorld(p.getWorld())) continue;
                if (p.getGameMode() == GameMode.SPECTATOR) adminCount++;
                else playingCount++;
            }
            int required = plugin.getConfig().getInt("autostart.count", 4);
            boolean ready = playingCount >= required;

            List<String> countLore = new ArrayList<>();
            countLore.add(ChatColor.GRAY + TextUtil.toTiny("Participantes: ") + (ready ? ChatColor.GREEN : ChatColor.RED) + playingCount + ChatColor.DARK_GRAY + " / " + ChatColor.WHITE + required);
            if (adminCount > 0) {
                countLore.add(ChatColor.GRAY + TextUtil.toTiny("Administradores: ") + ChatColor.YELLOW + adminCount + ChatColor.GRAY + " (Destiny)");
            }
            countLore.add(ChatColor.GRAY + TextUtil.toTiny("Mínimo recomendado para Subasta: 4"));
            countLore.add(ChatColor.GRAY + TextUtil.toTiny("Mínimo absoluto para 1v1: 2"));
            countLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            countLore.add(ready ? ChatColor.GREEN + "✔ " + TextUtil.toTiny("Cantidad suficiente para iniciar.") : ChatColor.RED + "✖ " + TextUtil.toTiny("Faltan jugadores para auto-inicio."));

            Material countMat = ready ? Material.LIME_DYE : Material.RED_DYE;
            gui.setItem(5, createActionItem(countMat, (ready ? ChatColor.GREEN : ChatColor.YELLOW) + "" + ChatColor.BOLD + "👥 " + TextUtil.toTiny("Jugadores: ") + playingCount + "/" + required, countLore, "none"));
        }

        // 4. Volver a Config (Slot 6)
        List<String> backLore = new ArrayList<>();
        backLore.add(ChatColor.GRAY + TextUtil.toTiny("Regresar al panel de configuración principal."));
        gui.setItem(6, createActionItem(Material.ARROW, ChatColor.YELLOW + "" + ChatColor.BOLD + "« " + TextUtil.toTiny("Volver"), backLore, "back"));

        // Red Team Members (Left columns: 0, 1, 2, 3 in rows 1 to 4)
        TTRTeam red = plugin.getTeamHandler().getTeam("Red");
        ItemStack redHeader = new ItemStack(Material.RED_BANNER);
        ItemMeta rm = redHeader.getItemMeta();
        if (rm != null) {
            rm.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + TextUtil.toTiny("Equipo Rojo"));
            List<String> hl = new ArrayList<>();
            hl.add(ChatColor.GRAY + TextUtil.toTiny("Miembros: ") + ChatColor.WHITE + ((red != null) ? red.getPlayers().size() : 0));
            rm.setLore(hl);
            redHeader.setItemMeta(rm);
        }
        gui.setItem(0, redHeader);

        int[] redSlots = {9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39};
        if (red != null) {
            populateTeamMembers(gui, red, redSlots);
        }

        // Blue Team Members (Right columns: 5, 6, 7, 8 in rows 1 to 4)
        TTRTeam blue = plugin.getTeamHandler().getTeam("Blue");
        ItemStack blueHeader = new ItemStack(Material.BLUE_BANNER);
        ItemMeta bm2 = blueHeader.getItemMeta();
        if (bm2 != null) {
            bm2.setDisplayName(ChatColor.BLUE + "" + ChatColor.BOLD + TextUtil.toTiny("Equipo Azul"));
            List<String> hl = new ArrayList<>();
            hl.add(ChatColor.GRAY + TextUtil.toTiny("Miembros: ") + ChatColor.WHITE + ((blue != null) ? blue.getPlayers().size() : 0));
            bm2.setLore(hl);
            blueHeader.setItemMeta(bm2);
        }
        gui.setItem(8, blueHeader);

        int[] blueSlots = {14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35, 41, 42, 43, 44};
        if (blue != null) {
            populateTeamMembers(gui, blue, blueSlots);
        }

        // Unassigned Players Shelf (Row 5: slots 45 - 53) - solo jugadores activos (no admins)
        List<Player> unassigned = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (TTRCore.isTowersWorld(p.getWorld()) && p.getGameMode() != GameMode.SPECTATOR && plugin.getTeamHandler().getPlayerTeam(p) == null) {
                unassigned.add(p);
            }
        }

        ItemStack unassignedHeader = new ItemStack(Material.NAME_TAG);
        ItemMeta uhMeta = unassignedHeader.getItemMeta();
        if (uhMeta != null) {
            uhMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "⚡ " + TextUtil.toTiny("Sin Asignar"));
            List<String> uhLore = new ArrayList<>();
            uhLore.add(ChatColor.GRAY + TextUtil.toTiny("Jugadores conectados sin equipo: ") + ChatColor.WHITE + unassigned.size());
            uhLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            uhLore.add(ChatColor.YELLOW + TextUtil.toTiny("Haz clic en una cabeza para asignarlo."));
            uhMeta.setLore(uhLore);
            unassignedHeader.setItemMeta(uhMeta);
        }
        gui.setItem(45, unassignedHeader);

        int[] unassignedSlots = {46, 47, 48, 49, 50, 51, 52, 53};
        for (int i = 0; i < unassignedSlots.length; i++) {
            int slot = unassignedSlots[i];
            if (i < unassigned.size()) {
                Player target = unassigned.get(i);
                ItemStack skull = SkullUtil.getPlayerHead(target);
                SkullMeta sm = (SkullMeta) skull.getItemMeta();
                if (sm != null) {
                    sm.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + target.getName());
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + TextUtil.toTiny("Estado: Sin equipo asignado"));
                    lore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
                    lore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Izquierdo: ") +
                            ChatColor.RED + TextUtil.toTiny("Asignar a Rojo"));
                    lore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Derecho: ") +
                            ChatColor.BLUE + TextUtil.toTiny("Asignar a Azul"));

                    sm.getPersistentDataContainer().set(KEY_ACTION, PersistentDataType.STRING, "player_assign_unassigned");
                    sm.getPersistentDataContainer().set(KEY_PLAYER_UUID, PersistentDataType.STRING, target.getUniqueId().toString());
                    sm.setLore(lore);
                    skull.setItemMeta(sm);
                }
                gui.setItem(slot, skull);
            } else {
                gui.setItem(slot, border);
            }
        }
    }

    private static void populateTeamMembers(Inventory gui, TTRTeam team, int[] slots) {
        List<UUID> members = team.getPlayers();
        for (int i = 0; i < members.size() && i < slots.length; i++) {
            UUID memberUuid = members.get(i);
            OfflinePlayer off = Bukkit.getOfflinePlayer(memberUuid);
            String name = (off.getName() != null) ? off.getName() : "Jugador";
            boolean isLeader = team.isLeader(memberUuid);

            ItemStack skull = SkullUtil.getPlayerHead(off);
            SkullMeta sm = (SkullMeta) skull.getItemMeta();
            if (sm != null) {
                String badge = isLeader ? ChatColor.GOLD + "★ " : "";
                sm.setDisplayName(badge + team.getColor() + "" + ChatColor.BOLD + name);

                List<String> lore = new ArrayList<>();
                if (isLeader) {
                    lore.add(ChatColor.GOLD + "" + ChatColor.BOLD + "★ " +
                            TextUtil.toTiny("LÍDER DEL EQUIPO ") + team.getColor() + TextUtil.toTiny(team.getIdentifier().toUpperCase()));
                } else {
                    lore.add(ChatColor.GRAY + TextUtil.toTiny("Rango: Miembro"));
                }
                lore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
                lore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Izquierdo: ") +
                        (isLeader ? ChatColor.RED + TextUtil.toTiny("Quitar Líder") : ChatColor.GOLD + TextUtil.toTiny("Nombrar Líder")));
                lore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Derecho: ") +
                        ChatColor.AQUA + TextUtil.toTiny("Mover al otro equipo"));
                lore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Shift + Clic: ") +
                        ChatColor.RED + TextUtil.toTiny("Expulsar"));

                sm.getPersistentDataContainer().set(KEY_ACTION, PersistentDataType.STRING, "player_manage");
                sm.getPersistentDataContainer().set(KEY_PLAYER_UUID, PersistentDataType.STRING, memberUuid.toString());
                sm.setLore(lore);
                skull.setItemMeta(sm);
            }
            gui.setItem(slots[i], skull);
        }
    }

    private static ItemStack createActionItem(Material mat, String name, List<String> lore, String action) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacySection().deserialize(name).decoration(TextDecoration.ITALIC, false));
            if (lore != null) {
                meta.lore(lore.stream().map(l -> LegacyComponentSerializer.legacySection().deserialize(l).decoration(TextDecoration.ITALIC, false)).toList());
            }
            meta.getPersistentDataContainer().set(KEY_ACTION, PersistentDataType.STRING, action);
            item.setItemMeta(meta);
        }
        return item;
    }
}
