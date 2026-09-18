package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.modes.AuctionDraftManager;
import me.PauMAVA.TTR.teams.TTRTeam;
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

public class AuctionDraftGUI {

    public static final String TITLE = ChatColor.DARK_GRAY + "⚖ " + ChatColor.GOLD +
            TextUtil.toTiny("Subasta de Miembros (Draft)");
    public static final NamespacedKey KEY_ACTION = new NamespacedKey(TTRCore.getInstance(), "ttr_auction_action");
    public static final NamespacedKey KEY_BID_AMOUNT = new NamespacedKey(TTRCore.getInstance(), "ttr_auction_amount");

    public static void open(Player player, AuctionDraftManager draft) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);
        render(gui, player, draft);
        player.openInventory(gui);
    }

    public static void update(Player player, AuctionDraftManager draft) {
        if (player.getOpenInventory().getTitle().equals(TITLE)) {
            render(player.getOpenInventory().getTopInventory(), player, draft);
        }
    }

    private static void render(Inventory gui, Player viewer, AuctionDraftManager draft) {
        TTRCore plugin = TTRCore.getInstance();
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        if (gm != null) {
            gm.setDisplayName(" ");
            glass.setItemMeta(gm);
        }
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, glass);
        }

        // Blue team side
        TTRTeam blue = plugin.getTeamHandler().getTeam("Blue");
        int blueCredits = draft.getTeamCredits("blue");
        boolean bluePassed = draft.hasPassed("blue");
        ItemStack blueHead = createTeamHead(blue, blueCredits, bluePassed);
        gui.setItem(0, blueHead);

        // Red team side
        TTRTeam red = plugin.getTeamHandler().getTeam("Red");
        int redCredits = draft.getTeamCredits("red");
        boolean redPassed = draft.hasPassed("red");
        ItemStack redHead = createTeamHead(red, redCredits, redPassed);
        gui.setItem(8, redHead);

        // Timer
        ItemStack timer = new ItemStack(Material.CLOCK);
        ItemMeta tm = timer.getItemMeta();
        if (tm != null) {
            tm.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "⏱ " +
                    TextUtil.toTiny("Tiempo restante: ") + ChatColor.WHITE + draft.getSecondsRemaining() + "s");
            timer.setItemMeta(tm);
        }
        gui.setItem(4, timer);

        // Candidate in Center (slot 13)
        UUID candUuid = draft.getCurrentCandidate();
        if (candUuid != null) {
            OfflinePlayer off = Bukkit.getOfflinePlayer(candUuid);
            String name = (off.getName() != null) ? off.getName() : "Jugador";

            ItemStack skull = SkullUtil.getPlayerHead(off);
            SkullMeta sm = (SkullMeta) skull.getItemMeta();
            if (sm != null) {
                sm.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "★ " + name);

                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + TextUtil.toTiny("Puja más alta: ") +
                        ChatColor.GREEN + draft.getCurrentBid() + " créditos");

                String leaderTeam = draft.getHighestBidderTeam();
                if (leaderTeam != null) {
                    ChatColor col = leaderTeam.equalsIgnoreCase("red") ? ChatColor.RED : ChatColor.BLUE;
                    lore.add(ChatColor.GRAY + TextUtil.toTiny("Ganando actualmente: ") +
                            col + TextUtil.toTiny(leaderTeam.toUpperCase()));
                } else {
                    lore.add(ChatColor.GRAY + TextUtil.toTiny("Ganando actualmente: ") +
                            ChatColor.DARK_GRAY + TextUtil.toTiny("Sin pujas"));
                }
                lore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
                lore.add(ChatColor.WHITE + TextUtil.toTiny("¡Capitanes, pujen con los botones inferiores!"));

                sm.setLore(lore);
                skull.setItemMeta(sm);
            }
            gui.setItem(13, skull);
        }

        // Bidding controls (Slots 18 to 26)
        TTRTeam viewerTeam = plugin.getTeamHandler().getPlayerTeam(viewer);
        boolean isCaptain = viewerTeam != null && viewerTeam.isLeader(viewer.getUniqueId());
        boolean isAdmin = TTRCore.isAdmin(viewer);

        if (isCaptain || isAdmin) {
            gui.setItem(18, createActionItem(Material.LIME_DYE, ChatColor.GREEN + "+1 " + TextUtil.toTiny("Crédito"), "bid_1", 1));
            gui.setItem(19, createActionItem(Material.EMERALD, ChatColor.GREEN + "+5 " + TextUtil.toTiny("Créditos"), "bid_5", 5));
            gui.setItem(20, createActionItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "" + ChatColor.BOLD + "+10 " + TextUtil.toTiny("Créditos"), "bid_10", 10));
            gui.setItem(21, createActionItem(Material.GOLD_INGOT, ChatColor.GOLD + "+25 " + TextUtil.toTiny("Créditos"), "bid_25", 25));
            gui.setItem(22, createActionItem(Material.GOLD_BLOCK, ChatColor.GOLD + "" + ChatColor.BOLD + "+50 " + TextUtil.toTiny("Créditos"), "bid_50", 50));
            gui.setItem(23, createActionItem(Material.DIAMOND, ChatColor.AQUA + "" + ChatColor.BOLD + "+100 " + TextUtil.toTiny("Créditos"), "bid_100", 100));

            gui.setItem(24, createActionItem(Material.NAME_TAG, ChatColor.YELLOW + "" + ChatColor.BOLD + TextUtil.toTiny("Pujar Cifra"), "custom_bid", 0));
            gui.setItem(25, createActionItem(Material.BARRIER, ChatColor.RED + "" + ChatColor.BOLD + TextUtil.toTiny("Pasar Turno"), "pass", 0));
            gui.setItem(26, createActionItem(Material.HOPPER, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + TextUtil.toTiny("Saltar Jugador"), "skip", 0));
        } else {
            ItemStack spectatorItem = new ItemStack(Material.PAPER);
            ItemMeta spMeta = spectatorItem.getItemMeta();
            if (spMeta != null) {
                spMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "👁 " + TextUtil.toTiny("Modo Espectador"));
                List<String> sl = new ArrayList<>();
                sl.add(ChatColor.GRAY + TextUtil.toTiny("Solo los líderes de equipo pueden"));
                sl.add(ChatColor.GRAY + TextUtil.toTiny("pujar con créditos por los jugadores."));
                sl.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
                sl.add(ChatColor.AQUA + TextUtil.toTiny("Observa la subasta en vivo."));
                spMeta.setLore(sl);
                spectatorItem.setItemMeta(spMeta);
            }
            gui.setItem(22, spectatorItem);
        }
    }

    private static ItemStack createTeamHead(TTRTeam team, int credits, boolean passed) {
        ItemStack item = new ItemStack(team.getIdentifier().equalsIgnoreCase("red") ? Material.RED_BANNER : Material.BLUE_BANNER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(team.getColor() + "" + ChatColor.BOLD + TextUtil.toTiny("Equipo " + team.getIdentifier()));
            List<String> lore = new ArrayList<>();
            UUID leaderUuid = team.getLeader();
            String leaderName = "Sin Líder";
            if (leaderUuid != null) {
                OfflinePlayer leader = Bukkit.getOfflinePlayer(leaderUuid);
                if (leader.getName() != null) leaderName = leader.getName();
            }
            lore.add(ChatColor.GRAY + TextUtil.toTiny("Capitán: ") + ChatColor.YELLOW + leaderName);
            lore.add(ChatColor.GRAY + TextUtil.toTiny("Créditos disponibles: ") + ChatColor.GREEN + credits);
            lore.add(ChatColor.GRAY + TextUtil.toTiny("Estado: ") + (passed ? ChatColor.RED + TextUtil.toTiny("Pasó") : ChatColor.AQUA + TextUtil.toTiny("Pujando")));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createActionItem(Material mat, String name, String action, int amount) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.getPersistentDataContainer().set(KEY_ACTION, PersistentDataType.STRING, action);
            if (amount > 0) {
                meta.getPersistentDataContainer().set(KEY_BID_AMOUNT, PersistentDataType.INTEGER, amount);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
