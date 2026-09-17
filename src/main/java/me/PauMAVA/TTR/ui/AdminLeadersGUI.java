package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.modes.TeamSelectionMode;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminLeadersGUI {

    public static final String TITLE = ChatColor.DARK_GRAY + "⚙ " + ChatColor.DARK_AQUA +
            TextUtil.toTiny("Gestor de Equipos y Líderes");

    public static final NamespacedKey KEY_ACTION = new NamespacedKey(TTRCore.getInstance(), "ttr_adm_action");
    public static final NamespacedKey KEY_PLAYER_UUID = new NamespacedKey(TTRCore.getInstance(), "ttr_adm_target");

    public static void open(Player player) {
        TTRCore plugin = TTRCore.getInstance();
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bm = border.getItemMeta();
        if (bm != null) {
            bm.setDisplayName(" ");
            border.setItemMeta(bm);
        }

        // Center separator
        for (int row = 0; row < 6; row++) {
            gui.setItem(row * 9 + 4, border);
        }

        // 1. Selector de Modalidad (Slot 2)
        TeamSelectionMode mode = plugin.getCurrentSelectionMode();
        List<String> modeLore = new ArrayList<>();
        modeLore.add(ChatColor.GRAY + TextUtil.toTiny("Modalidad activa: ") + mode.getDisplayName());
        modeLore.add(ChatColor.DARK_GRAY + TextUtil.toTiny(mode.getDescription()));
        modeLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        modeLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic: Rotar modalidad"));
        gui.setItem(2, createActionItem(Material.COMPASS, ChatColor.GOLD + "" + ChatColor.BOLD + "🎲 " + TextUtil.toTiny("Modo de Selección"), modeLore, "cycle_mode"));

        // 2. Control de Fase (Slot 4)
        boolean voteActive = plugin.getLeaderVoteManager().isActive();
        boolean draftActive = plugin.getAuctionDraftManager().isActive();

        if (voteActive || draftActive) {
            List<String> stopLore = new ArrayList<>();
            stopLore.add(ChatColor.GRAY + TextUtil.toTiny("Hay una fase activa en curso."));
            stopLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            stopLore.add(ChatColor.RED + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic: Detener fase activa"));
            gui.setItem(4, createActionItem(Material.BARRIER, ChatColor.RED + "" + ChatColor.BOLD + "✖ " + TextUtil.toTiny("Detener Fase Activa"), stopLore, "stop_phase"));
        } else if (mode == TeamSelectionMode.LEADER_VOTING) {
            List<String> startLore = new ArrayList<>();
            startLore.add(ChatColor.GRAY + TextUtil.toTiny("Inicia la votación por rondas"));
            startLore.add(ChatColor.GRAY + TextUtil.toTiny("en ambos equipos inmediatamente."));
            startLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            startLore.add(ChatColor.GREEN + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic: Iniciar Votación de Líder"));
            gui.setItem(4, createActionItem(Material.GOLDEN_HELMET, ChatColor.GREEN + "" + ChatColor.BOLD + "★ " + TextUtil.toTiny("Iniciar Votación"), startLore, "start_voting"));
        } else if (mode == TeamSelectionMode.AUCTION_DRAFT) {
            List<String> draftLore = new ArrayList<>();
            draftLore.add(ChatColor.GRAY + TextUtil.toTiny("Inicia la subasta interactiva"));
            draftLore.add(ChatColor.GRAY + TextUtil.toTiny("para fichar jugadores con créditos."));
            draftLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
            draftLore.add(ChatColor.GREEN + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic: Iniciar Subasta (Draft)"));
            gui.setItem(4, createActionItem(Material.CHEST_MINECART, ChatColor.GREEN + "" + ChatColor.BOLD + "⚖ " + TextUtil.toTiny("Iniciar Subasta"), draftLore, "start_draft"));
        } else {
            List<String> stdLore = new ArrayList<>();
            stdLore.add(ChatColor.GRAY + TextUtil.toTiny("En modo estándar los jugadores eligen"));
            stdLore.add(ChatColor.GRAY + TextUtil.toTiny("su equipo libremente o por auto-inicio."));
            gui.setItem(4, createActionItem(Material.IRON_SWORD, ChatColor.AQUA + "" + ChatColor.BOLD + "🎮 " + TextUtil.toTiny("Modo Estándar Activo"), stdLore, "none"));
        }

        // 3. Volver a Config (Slot 6)
        List<String> backLore = new ArrayList<>();
        backLore.add(ChatColor.GRAY + TextUtil.toTiny("Regresar al panel de configuración principal."));
        gui.setItem(6, createActionItem(Material.ARROW, ChatColor.YELLOW + "" + ChatColor.BOLD + "« " + TextUtil.toTiny("Volver"), backLore, "back"));

        // Red Team Members (Left columns: 0, 1, 2, 3)
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

        int[] redSlots = {9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39, 45, 46, 47, 48};
        if (red != null) {
            populateTeamMembers(gui, red, redSlots);
        }

        // Blue Team Members (Right columns: 5, 6, 7, 8)
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

        int[] blueSlots = {14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35, 41, 42, 43, 44, 50, 51, 52, 53};
        if (blue != null) {
            populateTeamMembers(gui, blue, blueSlots);
        }

        player.openInventory(gui);
    }

    private static void populateTeamMembers(Inventory gui, TTRTeam team, int[] slots) {
        List<UUID> members = team.getPlayers();
        for (int i = 0; i < members.size() && i < slots.length; i++) {
            UUID memberUuid = members.get(i);
            OfflinePlayer off = Bukkit.getOfflinePlayer(memberUuid);
            String name = (off.getName() != null) ? off.getName() : "Jugador";
            boolean isLeader = team.isLeader(memberUuid);

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) skull.getItemMeta();
            if (sm != null) {
                sm.setOwningPlayer(off);
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
            meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            meta.getPersistentDataContainer().set(KEY_ACTION, PersistentDataType.STRING, action);
            item.setItemMeta(meta);
        }
        return item;
    }
}
