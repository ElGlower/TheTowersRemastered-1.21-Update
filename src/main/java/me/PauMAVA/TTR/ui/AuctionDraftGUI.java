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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.format.TextDecoration;

public class AuctionDraftGUI {

    public static final String TITLE = ChatColor.DARK_GRAY + "⚖ " + ChatColor.GOLD +
            TextUtil.toTiny("Subasta de Miembros (Draft)");
    public static final NamespacedKey KEY_ACTION = new NamespacedKey(TTRCore.getInstance(), "ttr_auction_action");
    public static final NamespacedKey KEY_BID_AMOUNT = new NamespacedKey(TTRCore.getInstance(), "ttr_auction_amount");

    public static class AuctionDraftHolder implements InventoryHolder {
        private Inventory inventory;

        @Override
        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }

    public static void open(Player player, AuctionDraftManager draft) {
        AuctionDraftHolder holder = new AuctionDraftHolder();
        Inventory gui = Bukkit.createInventory(holder, 27, TITLE);
        holder.setInventory(gui);
        render(gui, player, draft);
        player.openInventory(gui);
    }

    public static void update(Player player, AuctionDraftManager draft) {
        if (player.getOpenInventory().getTopInventory().getHolder() instanceof AuctionDraftHolder) {
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
                lore.add(ChatColor.WHITE + TextUtil.toTiny("¡Capitanes, decidan el destino de su equipo!"));

                sm.setLore(lore);
                skull.setItemMeta(sm);
            }
            gui.setItem(13, skull);
        }

        // Controls (Slots 18 to 26)
        TTRTeam viewerTeam = plugin.getTeamHandler().getPlayerTeam(viewer);
        boolean isCaptain = viewerTeam != null && viewerTeam.isLeader(viewer.getUniqueId());
        boolean isCandidate = draft.getCurrentCandidate() != null && draft.getCurrentCandidate().equals(viewer.getUniqueId());
        boolean inPool = draft.getPool().contains(viewer.getUniqueId());
        boolean isAdmin = TTRCore.isAdmin(viewer) && !isCandidate && !inPool;

        if (isCaptain) {
            gui.setItem(18, createActionItem(Material.LIME_DYE, ChatColor.GREEN + "+1 " + TextUtil.toTiny("Crédito"), "bid_1", 1, null));
            gui.setItem(19, createActionItem(Material.EMERALD, ChatColor.GREEN + "+5 " + TextUtil.toTiny("Créditos"), "bid_5", 5, null));
            gui.setItem(20, createActionItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "" + ChatColor.BOLD + "+10 " + TextUtil.toTiny("Créditos"), "bid_10", 10, null));
            gui.setItem(21, createActionItem(Material.GOLD_INGOT, ChatColor.GOLD + "+25 " + TextUtil.toTiny("Créditos"), "bid_25", 25, null));
            gui.setItem(22, createActionItem(Material.GOLD_BLOCK, ChatColor.GOLD + "" + ChatColor.BOLD + "+50 " + TextUtil.toTiny("Créditos"), "bid_50", 50, null));
            gui.setItem(23, createActionItem(Material.DIAMOND, ChatColor.AQUA + "" + ChatColor.BOLD + "+100 " + TextUtil.toTiny("Créditos"), "bid_100", 100, null));

            gui.setItem(24, createActionItem(Material.NAME_TAG, ChatColor.YELLOW + "" + ChatColor.BOLD + TextUtil.toTiny("Pujar Cifra"), "custom_bid", 0, List.of(ChatColor.GRAY + TextUtil.toTiny("Usa /bid <cantidad>"))));
            gui.setItem(25, createActionItem(Material.BARRIER, ChatColor.RED + "" + ChatColor.BOLD + TextUtil.toTiny("Pasar Turno"), "pass", 0, null));
            gui.setItem(26, createActionItem(Material.IRON_DOOR, ChatColor.GRAY + TextUtil.toTiny("Cerrar (ESC)"), "close_gui", 0, null));
        } else if (isAdmin) {
            // Panel de Control y Monitoreo Administrativo
            gui.setItem(18, createActionItem(Material.CLOCK, ChatColor.YELLOW + "" + ChatColor.BOLD + "⏱ +10s " + TextUtil.toTiny("Tiempo"), "admin_add_time_10", 0, List.of(ChatColor.GRAY + TextUtil.toTiny("Extender turno actual"))));
            gui.setItem(19, createActionItem(Material.RECOVERY_COMPASS, ChatColor.GOLD + "" + ChatColor.BOLD + "⏱ +30s " + TextUtil.toTiny("Tiempo"), "admin_add_time_30", 0, List.of(ChatColor.GRAY + TextUtil.toTiny("Añadir 30s de subasta"))));
            gui.setItem(20, createActionItem(Material.RED_DYE, ChatColor.RED + "" + ChatColor.BOLD + "+25c " + TextUtil.toTiny("Rojo"), "admin_add_red_credits", 0, List.of(ChatColor.GRAY + TextUtil.toTiny("Sumar 25 créditos al equipo rojo"))));
            gui.setItem(21, createActionItem(Material.LAPIS_LAZULI, ChatColor.BLUE + "" + ChatColor.BOLD + "+25c " + TextUtil.toTiny("Azul"), "admin_add_blue_credits", 0, List.of(ChatColor.GRAY + TextUtil.toTiny("Sumar 25 créditos al equipo azul"))));
            gui.setItem(22, createActionItem(Material.HOPPER, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "⏭ " + TextUtil.toTiny("Saltar Jugador"), "admin_skip_candidate", 0, List.of(ChatColor.GRAY + TextUtil.toTiny("Asigna equitativamente y pasa al siguiente"))));
            gui.setItem(23, createActionItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "" + ChatColor.BOLD + "⚡ " + TextUtil.toTiny("Auto-Balancear"), "admin_conclude_draft", 0, List.of(ChatColor.GRAY + TextUtil.toTiny("Finalizar subasta y repartir miembros"))));
            gui.setItem(24, createActionItem(Material.BARRIER, ChatColor.RED + "" + ChatColor.BOLD + "✖ " + TextUtil.toTiny("Cancelar Subasta"), "admin_cancel_draft", 0, List.of(ChatColor.GRAY + TextUtil.toTiny("Detener subasta inmediatamente"))));
            gui.setItem(26, createActionItem(Material.IRON_DOOR, ChatColor.WHITE + "" + ChatColor.BOLD + "🚪 " + TextUtil.toTiny("Cerrar Screen"), "close_gui", 0, List.of(ChatColor.GRAY + TextUtil.toTiny("Puedes volver a abrir con /dt auction gui"))));
        } else {
            // Modo Espectador para Miembros y Visitantes (Monitoreo en Vivo)
            ItemStack liveStatus = new ItemStack(Material.EMERALD);
            ItemMeta lsMeta = liveStatus.getItemMeta();
            if (lsMeta != null) {
                lsMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "💰 " + TextUtil.toTiny("Oferta Actual: ") + draft.getCurrentBid() + "c");
                String winning = draft.getHighestBidderTeam();
                List<String> lsl = new ArrayList<>();
                lsl.add(ChatColor.GRAY + TextUtil.toTiny("Líder actual: ") + (winning != null ? (winning.equalsIgnoreCase("red") ? ChatColor.RED : ChatColor.BLUE) + winning.toUpperCase() : ChatColor.DARK_GRAY + "Ninguno"));
                lsMeta.setLore(lsl);
                liveStatus.setItemMeta(lsMeta);
            }
            gui.setItem(20, liveStatus);

            ItemStack spectatorItem = new ItemStack(isCandidate ? Material.TOTEM_OF_UNDYING : Material.ENDER_EYE);
            ItemMeta spMeta = spectatorItem.getItemMeta();
            if (spMeta != null) {
                if (isCandidate) {
                    spMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "★ " + TextUtil.toTiny("¡Estás en la Mesa de Subasta!"));
                    List<String> sl = new ArrayList<>();
                    sl.add(ChatColor.YELLOW + TextUtil.toTiny("Los capitanes están decidiendo tu destino"));
                    sl.add(ChatColor.YELLOW + TextUtil.toTiny("pujando con créditos de su equipo."));
                    sl.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
                    sl.add(ChatColor.GREEN + TextUtil.toTiny("¡Pronto sabrás tu equipo!"));
                    spMeta.setLore(sl);
                } else if (inPool) {
                    spMeta.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "⏳ " + TextUtil.toTiny("En Cola de Subasta"));
                    List<String> sl = new ArrayList<>();
                    sl.add(ChatColor.GRAY + TextUtil.toTiny("Estás en lista para ser subastado"));
                    sl.add(ChatColor.GRAY + TextUtil.toTiny("en los siguientes turnos."));
                    sl.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
                    sl.add(ChatColor.YELLOW + TextUtil.toTiny("Observa las pujas actuales."));
                    spMeta.setLore(sl);
                } else {
                    spMeta.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "👁 " + TextUtil.toTiny("Modo Espectador"));
                    List<String> sl = new ArrayList<>();
                    sl.add(ChatColor.GRAY + TextUtil.toTiny("Solo los líderes de equipo pueden"));
                    sl.add(ChatColor.GRAY + TextUtil.toTiny("pujar con créditos por los jugadores."));
                    sl.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
                    sl.add(ChatColor.AQUA + TextUtil.toTiny("Observa la subasta en vivo."));
                    spMeta.setLore(sl);
                }
                spectatorItem.setItemMeta(spMeta);
            }
            gui.setItem(22, spectatorItem);

            ItemStack teamInfo = new ItemStack(Material.PAPER);
            ItemMeta tiMeta = teamInfo.getItemMeta();
            if (tiMeta != null) {
                tiMeta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "👥 " + TextUtil.toTiny("Estado Equipos"));
                int rCount = red != null ? red.getPlayers().size() : 0;
                int bCount = blue != null ? blue.getPlayers().size() : 0;
                List<String> til = new ArrayList<>();
                til.add(ChatColor.RED + "Rojo: " + ChatColor.WHITE + rCount + " miembros " + ChatColor.GRAY + "(" + redCredits + "c)");
                til.add(ChatColor.BLUE + "Azul: " + ChatColor.WHITE + bCount + " miembros " + ChatColor.GRAY + "(" + blueCredits + "c)");
                tiMeta.setLore(til);
                teamInfo.setItemMeta(tiMeta);
            }
            gui.setItem(24, teamInfo);

            gui.setItem(26, createActionItem(Material.IRON_DOOR, ChatColor.GRAY + TextUtil.toTiny("Cerrar (ESC)"), "close_gui", 0, null));
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

    private static ItemStack createActionItem(Material mat, String name, String action, int amount, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(LegacyComponentSerializer.legacySection().deserialize(name).decoration(TextDecoration.ITALIC, false));
            if (lore != null) {
                meta.lore(lore.stream().map(l -> LegacyComponentSerializer.legacySection().deserialize(l).decoration(TextDecoration.ITALIC, false)).toList());
            }
            meta.getPersistentDataContainer().set(KEY_ACTION, PersistentDataType.STRING, action);
            if (amount > 0) {
                meta.getPersistentDataContainer().set(KEY_BID_AMOUNT, PersistentDataType.INTEGER, amount);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
