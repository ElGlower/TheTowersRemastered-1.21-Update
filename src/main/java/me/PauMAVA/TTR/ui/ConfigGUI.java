package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.match.TTRMatch;
import me.PauMAVA.TTR.rollback.RollbackManager;
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

public class ConfigGUI {

    public static final String TITLE = ChatColor.DARK_GRAY + "⚙ " + ChatColor.DARK_AQUA + TextUtil.toTiny("Destiny Towers - Configuración");
    public static final NamespacedKey KEY_ACTION = new NamespacedKey(TTRCore.getInstance(), "ttr_gui_action");

    public static void open(Player player) {
        TTRCore plugin = TTRCore.getInstance();
        Inventory gui = Bukkit.createInventory(null, 54, TITLE);

        ItemStack borderGray = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        ItemStack borderCyan = createItem(Material.CYAN_STAINED_GLASS_PANE, " ", null);

        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                gui.setItem(i, (i % 2 == 0) ? borderCyan : borderGray);
            }
        }

        // 1. Duracion
        int duration = plugin.getConfig().getInt("match.duration", 1200);
        List<String> durLore = new ArrayList<>();
        durLore.add(ChatColor.GRAY + TextUtil.toTiny("Actual: ") + ChatColor.YELLOW + TextUtil.toTiny((duration / 60) + "m (" + duration + "s)"));
        durLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        durLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Izquierdo: ") + ChatColor.GREEN + TextUtil.toTiny("+1m (+60s)"));
        durLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Derecho: ") + ChatColor.RED + TextUtil.toTiny("-1m (-60s)"));
        durLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Shift + Clic: ") + ChatColor.GOLD + TextUtil.toTiny("+5m (+300s)"));
        gui.setItem(11, createActionItem(Material.CLOCK, ChatColor.YELLOW + "" + ChatColor.BOLD + "⏱ " + TextUtil.toTiny("Duración de Partida"), durLore, "duration"));

        // 2. Puntos
        int maxPoints = plugin.getConfig().getInt("match.maxpoints", 10);
        List<String> ptsLore = new ArrayList<>();
        ptsLore.add(ChatColor.GRAY + TextUtil.toTiny("Puntos para ganar: ") + ChatColor.YELLOW + TextUtil.toTiny(String.valueOf(maxPoints)));
        ptsLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        ptsLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Izquierdo: ") + ChatColor.GREEN + TextUtil.toTiny("+1 punto"));
        ptsLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Derecho: ") + ChatColor.RED + TextUtil.toTiny("-1 punto"));
        gui.setItem(13, createActionItem(Material.TARGET, ChatColor.GOLD + "" + ChatColor.BOLD + "🎯 " + TextUtil.toTiny("Puntos de Victoria"), ptsLore, "points"));

        // 3. Auto-Inicio
        boolean autoEnabled = plugin.getConfig().getBoolean("autostart.enabled", true);
        int autoCount = plugin.getConfig().getInt("autostart.count", 4);
        int autoCountdown = plugin.getConfig().getInt("autostart.countdown", 10);
        List<String> autoLore = new ArrayList<>();
        autoLore.add(ChatColor.GRAY + TextUtil.toTiny("Estado: ") + (autoEnabled ? ChatColor.GREEN + TextUtil.toTiny("Activado") : ChatColor.RED + TextUtil.toTiny("Desactivado")));
        autoLore.add(ChatColor.GRAY + TextUtil.toTiny("Jugadores requeridos: ") + ChatColor.YELLOW + TextUtil.toTiny(String.valueOf(autoCount)));
        autoLore.add(ChatColor.GRAY + TextUtil.toTiny("Cuenta regresiva: ") + ChatColor.YELLOW + TextUtil.toTiny(autoCountdown + "s"));
        autoLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        autoLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Izquierdo: ") + ChatColor.AQUA + TextUtil.toTiny("Alternar ON/OFF"));
        autoLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Derecho: ") + ChatColor.GREEN + TextUtil.toTiny("+1 Jugador requerido"));
        autoLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Shift + Clic: ") + ChatColor.RED + TextUtil.toTiny("-1 Jugador requerido"));
        gui.setItem(15, createActionItem(Material.REPEATER, ChatColor.AQUA + "" + ChatColor.BOLD + "🚀 " + TextUtil.toTiny("Auto-Inicio"), autoLore, "autostart"));

        // 4. Regeneracion de Mapa
        RollbackManager rm = plugin.getRollbackManager();
        int recorded = (rm != null) ? rm.getRecordedBlockModifications() : 0;
        boolean autoRollback = plugin.getConfig().getBoolean("rollback.auto_restore_on_end", true);
        List<String> rollLore = new ArrayList<>();
        rollLore.add(ChatColor.GRAY + TextUtil.toTiny("Bloques modificados: ") + ChatColor.YELLOW + TextUtil.toTiny(String.valueOf(recorded)));
        rollLore.add(ChatColor.GRAY + TextUtil.toTiny("Auto-restaurar al finalizar: ") + (autoRollback ? ChatColor.GREEN + TextUtil.toTiny("Sí") : ChatColor.RED + TextUtil.toTiny("No")));
        rollLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        rollLore.add(ChatColor.YELLOW + "» " + ChatColor.GREEN + TextUtil.toTiny("Clic Izquierdo: ") + ChatColor.WHITE + TextUtil.toTiny("Regenerar Mapa Ahora"));
        rollLore.add(ChatColor.YELLOW + "» " + ChatColor.AQUA + TextUtil.toTiny("Clic Derecho: ") + ChatColor.WHITE + TextUtil.toTiny("Alternar Auto-Restaurar"));
        gui.setItem(29, createActionItem(Material.NETHER_STAR, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "⚡ " + TextUtil.toTiny("Regeneración de Mapa"), rollLore, "rollback"));

        // 5. Control de Partida
        TTRMatch match = plugin.getCurrentMatch();
        MatchStatus status = (match != null) ? match.getStatus() : MatchStatus.STOPPED;
        List<String> matchLore = new ArrayList<>();
        matchLore.add(ChatColor.GRAY + TextUtil.toTiny("Estado: ") + ChatColor.YELLOW + TextUtil.toTiny(status.name()));
        matchLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        matchLore.add(ChatColor.GREEN + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Izquierdo: ") + ChatColor.GREEN + TextUtil.toTiny("Iniciar Partida (/dt start)"));
        matchLore.add(ChatColor.RED + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Derecho: ") + ChatColor.RED + TextUtil.toTiny("Detener Partida (/dt stop)"));
        gui.setItem(31, createActionItem(Material.NETHERITE_SWORD, ChatColor.GREEN + "" + ChatColor.BOLD + "🎮 " + TextUtil.toTiny("Control de Partida"), matchLore, "matchcontrol"));

        // 6. Spawns y Jaulas
        List<String> spawnLore = new ArrayList<>();
        spawnLore.add(ChatColor.GRAY + TextUtil.toTiny("Configurar posiciones según tu ubicación:"));
        spawnLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        spawnLore.add(ChatColor.RED + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Izquierdo: ") + ChatColor.RED + TextUtil.toTiny("Fijar Spawn Rojo"));
        spawnLore.add(ChatColor.BLUE + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic Derecho: ") + ChatColor.BLUE + TextUtil.toTiny("Fijar Spawn Azul"));
        spawnLore.add(ChatColor.DARK_RED + "» " + ChatColor.WHITE + TextUtil.toTiny("Shift + Clic Izq: ") + ChatColor.DARK_RED + TextUtil.toTiny("Fijar Jaula Roja"));
        spawnLore.add(ChatColor.DARK_BLUE + "» " + ChatColor.WHITE + TextUtil.toTiny("Shift + Clic Der: ") + ChatColor.DARK_BLUE + TextUtil.toTiny("Fijar Jaula Azul"));
        gui.setItem(33, createActionItem(Material.ARMOR_STAND, ChatColor.GOLD + "" + ChatColor.BOLD + "🚩 " + TextUtil.toTiny("Spawns y Jaulas"), spawnLore, "spawns"));

        // 7. Establecer Lobby
        List<String> lobbyLore = new ArrayList<>();
        lobbyLore.add(ChatColor.GRAY + TextUtil.toTiny("Fija el punto de spawn de espera central"));
        lobbyLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        lobbyLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic: ") + ChatColor.GREEN + TextUtil.toTiny("Establecer Lobby en tu posición"));
        gui.setItem(22, createActionItem(Material.COMPASS, ChatColor.YELLOW + "" + ChatColor.BOLD + "📍 " + TextUtil.toTiny("Establecer Lobby"), lobbyLore, "lobby"));

        // 8. Modo Pantalla (Libro interactivo)
        List<String> screenLore = new ArrayList<>();
        screenLore.add(ChatColor.GRAY + TextUtil.toTiny("Abre el panel en una pantalla completa"));
        screenLore.add(ChatColor.GRAY + TextUtil.toTiny("de libro interactivo con botones de texto."));
        screenLore.add(ChatColor.DARK_GRAY + "§m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        screenLore.add(ChatColor.YELLOW + "» " + ChatColor.WHITE + TextUtil.toTiny("Clic: ") + ChatColor.GOLD + TextUtil.toTiny("Abrir Modo Pantalla"));
        gui.setItem(40, createActionItem(Material.WRITTEN_BOOK, ChatColor.GOLD + "" + ChatColor.BOLD + "📖 " + TextUtil.toTiny("Modo Pantalla (Screen)"), screenLore, "screen"));

        // 9. Salir
        List<String> closeLore = new ArrayList<>();
        closeLore.add(ChatColor.GRAY + TextUtil.toTiny("Cerrar este panel de configuración."));
        gui.setItem(49, createActionItem(Material.BARRIER, ChatColor.RED + "" + ChatColor.BOLD + "✖ " + TextUtil.toTiny("Cerrar"), closeLore, "close"));

        player.openInventory(gui);
    }

    private static ItemStack createItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
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
