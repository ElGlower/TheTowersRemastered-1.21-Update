package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ModeSettingsGUI {

    public static final String TITLE = ChatColor.DARK_GRAY + "⚙ " +
            TextUtil.color("&#FFFFFF§l" + TextUtil.toTiny("Ajustes de Tiempos y Créditos"));

    public static final NamespacedKey KEY_ACTION = new NamespacedKey(TTRCore.getInstance(), "ttr_timing_action");

    public static void open(Player player) {
        TTRCore plugin = TTRCore.getInstance();
        Inventory gui = Bukkit.createInventory(null, 27, TITLE);

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta bm = border.getItemMeta();
        if (bm != null) {
            bm.setDisplayName(" ");
            border.setItemMeta(bm);
        }
        for (int i = 0; i < 27; i++) {
            gui.setItem(i, border);
        }

        // 1. Slot 10: Tiempo de Lectura de Reglas de Modo
        int readSecs = plugin.getConfig().getInt("modes.explanation_seconds", 60);
        List<String> readLore = new ArrayList<>();
        readLore.add(ChatColor.GRAY + TextUtil.toTiny("Tiempo de espera para leer las reglas antes de iniciar."));
        readLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        readLore.add(ChatColor.WHITE + TextUtil.toTiny("Actual: ") + (readSecs == 0 ? ChatColor.RED + TextUtil.toTiny("Inmediato (0s)") : ChatColor.YELLOW + String.valueOf(readSecs) + "s"));
        readLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Izq: Ciclar") + ChatColor.GRAY + " | " + ChatColor.AQUA + TextUtil.toTiny("Clic Der: Personalizado"));
        gui.setItem(10, createActionItem(Material.BOOK, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "📖 " + TextUtil.toTiny("Tiempo de Lectura"), readLore, "cycle_read_secs"));

        // 2. Slot 11: Tiempo por Ronda de Votación
        int voteSecs = plugin.getConfig().getInt("voting.round_seconds", 20);
        List<String> voteLore = new ArrayList<>();
        voteLore.add(ChatColor.GRAY + TextUtil.toTiny("Duración de cada ronda eliminatoria de líderes."));
        voteLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        voteLore.add(ChatColor.WHITE + TextUtil.toTiny("Actual: ") + ChatColor.YELLOW + voteSecs + "s");
        voteLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Izq: Ciclar") + ChatColor.GRAY + " | " + ChatColor.AQUA + TextUtil.toTiny("Clic Der: Personalizado"));
        gui.setItem(11, createActionItem(Material.CLOCK, ChatColor.GOLD + "" + ChatColor.BOLD + "⏱ " + TextUtil.toTiny("Ronda de Votación"), voteLore, "cycle_vote_secs"));

        // 3. Slot 12: Tiempo por Turno de Subasta
        int turnSecs = plugin.getConfig().getInt("auction.turn_seconds", 15);
        List<String> turnLore = new ArrayList<>();
        turnLore.add(ChatColor.GRAY + TextUtil.toTiny("Tiempo para que los capitanes pujen por un jugador."));
        turnLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        turnLore.add(ChatColor.WHITE + TextUtil.toTiny("Actual: ") + ChatColor.YELLOW + turnSecs + "s");
        turnLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Izq: Ciclar") + ChatColor.GRAY + " | " + ChatColor.AQUA + TextUtil.toTiny("Clic Der: Personalizado"));
        gui.setItem(12, createActionItem(Material.COMPASS, ChatColor.AQUA + "" + ChatColor.BOLD + "⏱ " + TextUtil.toTiny("Turno de Subasta"), turnLore, "cycle_turn_secs"));

        // 4. Slot 14: Conteo Pre-Partida
        int prestart = plugin.getConfig().getInt("match.prestart_countdown", 10);
        List<String> prestartLore = new ArrayList<>();
        prestartLore.add(ChatColor.GRAY + TextUtil.toTiny("Segundos de cuenta atrás antes de soltar a los jugadores."));
        prestartLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        prestartLore.add(ChatColor.WHITE + TextUtil.toTiny("Actual: ") + ChatColor.YELLOW + prestart + "s");
        prestartLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Izq: Ciclar") + ChatColor.GRAY + " | " + ChatColor.AQUA + TextUtil.toTiny("Clic Der: Personalizado"));
        gui.setItem(14, createActionItem(Material.RECOVERY_COMPASS, ChatColor.YELLOW + "" + ChatColor.BOLD + "⏳ " + TextUtil.toTiny("Conteo Pre-Partida"), prestartLore, "cycle_prestart_secs"));

        // 5. Slot 15: Créditos Iniciales de Subasta
        int credits = plugin.getConfig().getInt("auction.initial_credits", 100);
        List<String> credLore = new ArrayList<>();
        credLore.add(ChatColor.GRAY + TextUtil.toTiny("Bolsa de créditos inicial para cada capitán."));
        credLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        credLore.add(ChatColor.WHITE + TextUtil.toTiny("Actual: ") + ChatColor.GREEN + credits + " créditos");
        credLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Izq: Ciclar") + ChatColor.GRAY + " | " + ChatColor.AQUA + TextUtil.toTiny("Clic Der: Personalizado"));
        gui.setItem(15, createActionItem(Material.EMERALD, ChatColor.GREEN + "" + ChatColor.BOLD + "⚖ " + TextUtil.toTiny("Créditos de Subasta"), credLore, "cycle_credits"));

        // 6. Slot 16: Incremento Base de Puja
        int bidInc = plugin.getConfig().getInt("auction.bid_increment", 10);
        List<String> incLore = new ArrayList<>();
        incLore.add(ChatColor.GRAY + TextUtil.toTiny("Valor base para los botones de puja en la subasta."));
        incLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        incLore.add(ChatColor.WHITE + TextUtil.toTiny("Actual: ") + ChatColor.AQUA + "+" + bidInc + " créditos");
        incLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Izq: Ciclar") + ChatColor.GRAY + " | " + ChatColor.AQUA + TextUtil.toTiny("Clic Der: Personalizado"));
        gui.setItem(16, createActionItem(Material.GOLD_INGOT, ChatColor.GOLD + "" + ChatColor.BOLD + "💵 " + TextUtil.toTiny("Incremento de Puja"), incLore, "cycle_bid_increment"));

        // 7. Slot 22: Volver
        List<String> backLore = new ArrayList<>();
        backLore.add(ChatColor.GRAY + TextUtil.toTiny("Volver al gestor de equipos y líderes."));
        gui.setItem(22, createActionItem(Material.ARROW, ChatColor.YELLOW + "" + ChatColor.BOLD + "« " + TextUtil.toTiny("Volver"), backLore, "back_leaders"));

        player.openInventory(gui);
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
