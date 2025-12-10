package me.PauMAVA.TTR.ui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeaconShop {

    public static final String TITLE_MAIN = ChatColor.DARK_PURPLE + "Tienda del Faro";
    public static final String TITLE_BLOCKS = ChatColor.DARK_BLUE + "TTR: Bloques y Armas";
    public static final String TITLE_UTILITY = ChatColor.DARK_GREEN + "TTR: Utilidad y Magia";
    public static final String TITLE_UPGRADES = ChatColor.GOLD + "TTR: Mejoras de Equipo";

    public void openMain(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE_MAIN);
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", 0, null);
        for (int i = 0; i < 27; i++) gui.setItem(i, filler);

        gui.setItem(11, createItem(Material.IRON_SWORD, ChatColor.YELLOW + "Bloques y Armas", 0, null, "Bloques, Espadas, Arcos."));
        gui.setItem(13, createItem(Material.BREWING_STAND, ChatColor.AQUA + "Utilidad y Magia", 0, null, "Pociones, Wind Charges, TNT."));
        gui.setItem(15, createItem(Material.BEACON, ChatColor.GOLD + "Mejoras de Equipo", 0, null, "Protección y Efectos Globales."));

        player.openInventory(gui);
    }

    public void openBlocks(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, TITLE_BLOCKS);
        addBackButton(gui);

        gui.setItem(10, createItem(Material.WHITE_WOOL, ChatColor.YELLOW + "Lana (x32)", 4, Material.EMERALD));
        gui.setItem(11, createItem(Material.OAK_PLANKS, ChatColor.YELLOW + "Madera (x16)", 8, Material.EMERALD));
        gui.setItem(12, createItem(Material.END_STONE, ChatColor.YELLOW + "Piedra End (x12)", 12, Material.EMERALD));
        gui.setItem(13, createItem(Material.OBSIDIAN, ChatColor.DARK_PURPLE + "Obsidiana (x4)", 4, Material.COAL));

        gui.setItem(19, createItem(Material.STONE_SWORD, ChatColor.GRAY + "Espada de Piedra", 5, Material.EMERALD));
        gui.setItem(20, createItem(Material.IRON_SWORD, ChatColor.WHITE + "Espada de Hierro", 10, Material.EMERALD));
        gui.setItem(21, createItem(Material.DIAMOND_SWORD, ChatColor.AQUA + "Espada de Diamante", 20, Material.COAL));
        gui.setItem(22, createItem(Material.SHIELD, ChatColor.YELLOW + "Escudo Frágil", 10, Material.EMERALD, "¡Cuidado! Se rompe rápido."));

        gui.setItem(23, createItem(Material.BOW, ChatColor.GREEN + "Arco", 12, Material.EMERALD));
        gui.setItem(24, createItem(Material.ARROW, ChatColor.YELLOW + "Flechas (x8)", 2, Material.EMERALD));

        gui.setItem(28, createItem(Material.SHEARS, ChatColor.WHITE + "Tijeras", 5, Material.EMERALD));
        gui.setItem(29, createItem(Material.DIAMOND_PICKAXE, ChatColor.AQUA + "Pico de Diamante", 10, Material.COAL));

        player.openInventory(gui);
    }

    public void openUtility(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, TITLE_UTILITY);
        addBackButton(gui);

        gui.setItem(10, createItem(Material.BREAD, ChatColor.YELLOW + "Pan (x4)", 2, Material.EMERALD));
        gui.setItem(11, createItem(Material.GOLDEN_APPLE, ChatColor.GOLD + "Manzana Dorada", 4, Material.COAL));

        gui.setItem(13, createItem(Material.TNT, ChatColor.RED + "TNT", 8, Material.COAL));
        gui.setItem(14, createItem(Material.FIRE_CHARGE, ChatColor.RED + "Bola de Fuego", 10, Material.COAL));
        gui.setItem(15, createItem(Material.WIND_CHARGE, ChatColor.AQUA + "Carga de Viento (x2)", 12, Material.COAL, "¡Impúlsate por los aires!"));
        gui.setItem(16, createItem(Material.ENDER_PEARL, ChatColor.DARK_AQUA + "Ender Pearl", 15, Material.COAL));

        gui.setItem(28, createItem(Material.POTION, ChatColor.BLUE + "Poción de Salto", 5, Material.EMERALD, "Salto II (45s)"));
        gui.setItem(29, createItem(Material.POTION, ChatColor.LIGHT_PURPLE + "Poción de Regeneración", 10, Material.COAL, "Regeneración II (10s)"));
        gui.setItem(30, createItem(Material.SPLASH_POTION, ChatColor.RED + "Splash de Daño", 12, Material.COAL, "Daño Instantáneo II"));

        player.openInventory(gui);
    }

    public void openUpgrades(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, TITLE_UPGRADES);
        addBackButton(gui);

        gui.setItem(11, createEnchantedItem(Material.IRON_CHESTPLATE, ChatColor.GREEN + "Team Prot I", 10, Material.COAL, "Protección I para todos."));
        gui.setItem(12, createEnchantedItem(Material.IRON_CHESTPLATE, ChatColor.GREEN + "Team Prot II", 20, Material.COAL, "Protección II para todos.", "Requiere Nivel I"));
        gui.setItem(13, createEnchantedItem(Material.DIAMOND_CHESTPLATE, ChatColor.AQUA + "Team Prot III", 30, Material.COAL, "Protección III para todos.", "Requiere Nivel II"));
        gui.setItem(14, createEnchantedItem(Material.NETHERITE_CHESTPLATE, ChatColor.LIGHT_PURPLE + "Team Prot IV", 50, Material.COAL, "Protección IV para todos.", "Requiere Nivel III"));

        gui.setItem(29, createItem(Material.SUGAR, ChatColor.AQUA + "Team Speed", 25, Material.COAL, "Velocidad I permanente."));
        gui.setItem(31, createItem(Material.GOLDEN_PICKAXE, ChatColor.GOLD + "Team Haste", 25, Material.COAL, "Prisa Minera I permanente."));

        player.openInventory(gui);
    }

    private void addBackButton(Inventory gui) {
        gui.setItem(40, createItem(Material.ARROW, ChatColor.RED + "Volver", 0, null));
    }

    private ItemStack createItem(Material mat, String name, int price, Material currency, String... loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        if (loreLines != null) lore.addAll(Arrays.asList(loreLines));

        lore.add(" ");
        if (price > 0 && currency != null) {
            String currencyName = (currency == Material.EMERALD) ? "Esmeraldas" : "Carbón";
            ChatColor currencyColor = (currency == Material.EMERALD) ? ChatColor.GREEN : ChatColor.DARK_GRAY;
            lore.add(ChatColor.GRAY + "Costo: " + currencyColor + price + " " + currencyName);
        }

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createEnchantedItem(Material mat, String name, int price, Material currency, String... loreLines) {
        ItemStack item = createItem(mat, name, price, currency, loreLines);
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(Enchantment.PROTECTION, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return item;
    }
}