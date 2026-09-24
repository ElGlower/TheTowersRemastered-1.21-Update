package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.modes.LeaderVoteManager;
import me.PauMAVA.TTR.util.SkullUtil;
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

public class LeaderVoteGUI {

    public static final NamespacedKey KEY_CANDIDATE = new NamespacedKey(TTRCore.getInstance(), "ttr_vote_candidate");

    public static void open(Player player, LeaderVoteManager.TeamVoteState state) {
        String title = ChatColor.DARK_GRAY + "★ " + ChatColor.GOLD +
                TextUtil.toTiny("Votación de Líder - Ronda ") + state.getRound();
        Inventory gui = Bukkit.createInventory(null, 54, title);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bm = border.getItemMeta();
        if (bm != null) {
            bm.setDisplayName(" ");
            border.setItemMeta(bm);
        }

        // Borde exterior del inventario de 54 slots
        for (int i = 0; i < 9; i++) gui.setItem(i, border);
        for (int i = 45; i < 54; i++) gui.setItem(i, border);
        gui.setItem(9, border);
        gui.setItem(17, border);
        gui.setItem(18, border);
        gui.setItem(26, border);
        gui.setItem(27, border);
        gui.setItem(35, border);
        gui.setItem(36, border);
        gui.setItem(44, border);

        // Reloj central en la fila inferior (slot 49)
        ItemStack timer = new ItemStack(Material.CLOCK);
        ItemMeta tm = timer.getItemMeta();
        if (tm != null) {
            tm.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "⏱ " +
                    TextUtil.toTiny("Tiempo restante: ") + ChatColor.WHITE +
                    TTRCore.getInstance().getLeaderVoteManager().getSecondsRemaining() + "s");
            timer.setItemMeta(tm);
        }
        gui.setItem(49, timer);

        // Slots centrales para cabezas de candidatos (hasta 28 candidatos)
        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        List<UUID> candidates = state.getCandidates();
        UUID votedFor = state.getVotes().get(player.getUniqueId());

        for (int i = 0; i < candidates.size() && i < slots.length; i++) {
            UUID candUuid = candidates.get(i);
            OfflinePlayer off = Bukkit.getOfflinePlayer(candUuid);
            String name = (off.getName() != null) ? off.getName() : "Jugador";

            ItemStack skull = SkullUtil.getPlayerHead(off);
            SkullMeta sm = (SkullMeta) skull.getItemMeta();
            if (sm != null) {
                sm.setDisplayName(state.getTeam().getColor() + "" + ChatColor.BOLD + name);

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + TextUtil.toTiny("Candidato a líder de tu equipo"));
                lore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");

                int currentVotes = state.getVotesFor(candUuid);
                lore.add(ChatColor.GOLD + "★ " + TextUtil.toTiny("Votos actuales: ") + ChatColor.WHITE + "" + ChatColor.BOLD + currentVotes);

                if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR) {
                    lore.add(ChatColor.GRAY + TextUtil.toTiny("Espectador (No votas)"));
                } else if (candUuid.equals(votedFor)) {
                    lore.add(ChatColor.GREEN + "✔ " + TextUtil.toTiny("¡Tu voto emitido!"));
                } else {
                    lore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic para votar a este líder"));
                }

                sm.getPersistentDataContainer().set(KEY_CANDIDATE, PersistentDataType.STRING, candUuid.toString());
                sm.setLore(lore);
                skull.setItemMeta(sm);
            }
            gui.setItem(slots[i], skull);
        }

        player.openInventory(gui);
    }

    public static void updateTimer(Player player, int seconds) {
        if (player.getOpenInventory() != null && player.getOpenInventory().getTitle().contains(TextUtil.toTiny("Votación de Líder"))) {
            Inventory inv = player.getOpenInventory().getTopInventory();
            ItemStack timer = new ItemStack(Material.CLOCK);
            ItemMeta tm = timer.getItemMeta();
            if (tm != null) {
                tm.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "⏱ " +
                        TextUtil.toTiny("Tiempo restante: ") + ChatColor.WHITE + seconds + "s");
                timer.setItemMeta(tm);
            }
            inv.setItem(49, timer);
        }
    }
}
