package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class BeaconShop {

    public static final String TITLE_MAIN = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + TextUtil.toTiny("Tienda del Faro");
    public static final String TITLE_BLOCKS = ChatColor.DARK_BLUE + "" + ChatColor.BOLD + TextUtil.toTiny("Bloques y Armas");
    public static final String TITLE_UTILITY = ChatColor.DARK_GREEN + "" + ChatColor.BOLD + TextUtil.toTiny("Utilidad y Magia");
    public static final String TITLE_UPGRADES = ChatColor.GOLD + "" + ChatColor.BOLD + TextUtil.toTiny("Mejoras de Equipo");

    public static NamespacedKey KEY_PRICE;
    public static NamespacedKey KEY_CURRENCY;
    public static NamespacedKey KEY_UPGRADE;

    static {
        TTRCore core = TTRCore.getInstance();
        KEY_PRICE = new NamespacedKey(core, "shop_price");
        KEY_CURRENCY = new NamespacedKey(core, "shop_currency");
        KEY_UPGRADE = new NamespacedKey(core, "shop_upgrade");
    }

    public void openMain(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, TITLE_MAIN);
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", 0, null);
        for (int i = 0; i < 27; i++) gui.setItem(i, filler);

        gui.setItem(11, createCategoryItem(Material.IRON_SWORD, 
                ChatColor.YELLOW + "" + ChatColor.BOLD + TextUtil.toTiny("Bloques y Armas"), 
                ChatColor.GRAY + TextUtil.toTiny("Bloques, espadas, arcos y herramientas.")));

        gui.setItem(13, createCategoryItem(Material.BREWING_STAND, 
                ChatColor.AQUA + "" + ChatColor.BOLD + TextUtil.toTiny("Utilidad y Magia"), 
                ChatColor.GRAY + TextUtil.toTiny("Pociones, Cargas de Viento 1.21 y TNT.")));

        gui.setItem(15, createCategoryItem(Material.BEACON, 
                ChatColor.GOLD + "" + ChatColor.BOLD + TextUtil.toTiny("Mejoras de Equipo"), 
                ChatColor.GRAY + TextUtil.toTiny("Protección y efectos permanentes para el equipo.")));

        player.openInventory(gui);
    }

    public void openBlocks(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, TITLE_BLOCKS);
        addBackButton(gui);

        // Bloques
        gui.setItem(10, createItem(Material.WHITE_WOOL, 32, ChatColor.YELLOW + TextUtil.toTiny("Lana (x32)"), 4, Material.EMERALD));
        gui.setItem(11, createItem(Material.OAK_PLANKS, 16, ChatColor.YELLOW + TextUtil.toTiny("Madera (x16)"), 8, Material.EMERALD));
        gui.setItem(12, createItem(Material.END_STONE, 12, ChatColor.YELLOW + TextUtil.toTiny("Piedra End (x12)"), 12, Material.EMERALD));
        gui.setItem(13, createItem(Material.OBSIDIAN, 4, ChatColor.DARK_PURPLE + TextUtil.toTiny("Obsidiana (x4)"), 4, Material.COAL));

        // Armas
        gui.setItem(19, createItem(Material.STONE_SWORD, 1, ChatColor.GRAY + TextUtil.toTiny("Espada de Piedra"), 5, Material.EMERALD));
        gui.setItem(20, createItem(Material.IRON_SWORD, 1, ChatColor.WHITE + TextUtil.toTiny("Espada de Hierro"), 10, Material.EMERALD));
        gui.setItem(21, createItem(Material.DIAMOND_SWORD, 1, ChatColor.AQUA + TextUtil.toTiny("Espada de Diamante"), 20, Material.COAL));
        gui.setItem(22, createItem(Material.SHIELD, 1, ChatColor.YELLOW + TextUtil.toTiny("Escudo Táctico"), 10, Material.EMERALD, 
                ChatColor.GRAY + TextUtil.toTiny("Se rompe con facilidad.")));

        // Arcos
        gui.setItem(23, createItem(Material.BOW, 1, ChatColor.GREEN + TextUtil.toTiny("Arco"), 12, Material.EMERALD));
        gui.setItem(24, createItem(Material.ARROW, 8, ChatColor.YELLOW + TextUtil.toTiny("Flechas (x8)"), 2, Material.EMERALD));

        // Herramientas
        gui.setItem(28, createItem(Material.SHEARS, 1, ChatColor.WHITE + TextUtil.toTiny("Tijeras"), 5, Material.EMERALD));
        gui.setItem(29, createItem(Material.DIAMOND_PICKAXE, 1, ChatColor.AQUA + TextUtil.toTiny("Pico de Diamante"), 10, Material.COAL));

        player.openInventory(gui);
    }

    public void openUtility(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, TITLE_UTILITY);
        addBackButton(gui);

        // Comida
        gui.setItem(10, createItem(Material.BREAD, 4, ChatColor.YELLOW + TextUtil.toTiny("Pan (x4)"), 2, Material.EMERALD));
        gui.setItem(11, createItem(Material.GOLDEN_APPLE, 1, ChatColor.GOLD + TextUtil.toTiny("Manzana Dorada"), 4, Material.COAL));

        // Explosivos y Cargas de Viento (1.21)
        gui.setItem(13, createItem(Material.TNT, 1, ChatColor.RED + TextUtil.toTiny("TNT"), 8, Material.COAL));
        gui.setItem(14, createItem(Material.COBWEB, 4, ChatColor.WHITE + TextUtil.toTiny("Telarañas Tácticas (x4)"), 8, Material.COAL, 
                ChatColor.GRAY + TextUtil.toTiny("Ralentiza a los invasores enemigos.")));
        gui.setItem(15, createItem(Material.WIND_CHARGE, 2, ChatColor.AQUA + TextUtil.toTiny("Carga de Viento (x2)"), 12, Material.COAL, 
                ChatColor.GRAY + TextUtil.toTiny("¡Impulso aéreo explosivo de la 1.21!")));
        gui.setItem(16, createItem(Material.ENDER_PEARL, 1, ChatColor.DARK_AQUA + TextUtil.toTiny("Ender Pearl"), 15, Material.COAL));

        // Pociones
        gui.setItem(28, createItem(Material.POTION, 1, ChatColor.BLUE + TextUtil.toTiny("Poción de Salto"), 5, Material.EMERALD, 
                ChatColor.GRAY + TextUtil.toTiny("Salto II (45s)")));
        gui.setItem(29, createItem(Material.POTION, 1, ChatColor.LIGHT_PURPLE + TextUtil.toTiny("Poción de Regeneración"), 10, Material.COAL, 
                ChatColor.GRAY + TextUtil.toTiny("Regeneración II (10s)")));
        gui.setItem(30, createItem(Material.SPLASH_POTION, 1, ChatColor.RED + TextUtil.toTiny("Splash de Daño"), 12, Material.COAL, 
                ChatColor.GRAY + TextUtil.toTiny("Daño Instantáneo II")));

        player.openInventory(gui);
    }

    public void openUpgrades(Player player) {
        Inventory gui = Bukkit.createInventory(null, 45, TITLE_UPGRADES);
        addBackButton(gui);

        // Armadura de Equipo (Protección I a IV)
        gui.setItem(10, createUpgradeItem(Material.IRON_CHESTPLATE, ChatColor.GREEN + TextUtil.toTiny("Protección I"), 10, Material.COAL, "prot_1", 
                ChatColor.GRAY + TextUtil.toTiny("Protección I para todo el equipo.")));
        gui.setItem(11, createUpgradeItem(Material.IRON_CHESTPLATE, ChatColor.GREEN + TextUtil.toTiny("Protección II"), 20, Material.COAL, "prot_2", 
                ChatColor.GRAY + TextUtil.toTiny("Protección II para todo el equipo."), ChatColor.RED + TextUtil.toTiny("Requiere Nivel I")));
        gui.setItem(12, createUpgradeItem(Material.DIAMOND_CHESTPLATE, ChatColor.AQUA + TextUtil.toTiny("Protección III"), 30, Material.COAL, "prot_3", 
                ChatColor.GRAY + TextUtil.toTiny("Protección III para todo el equipo."), ChatColor.RED + TextUtil.toTiny("Requiere Nivel II")));
        gui.setItem(13, createUpgradeItem(Material.NETHERITE_CHESTPLATE, ChatColor.LIGHT_PURPLE + TextUtil.toTiny("Protección IV"), 50, Material.COAL, "prot_4", 
                ChatColor.GRAY + TextUtil.toTiny("Protección IV para todo el equipo."), ChatColor.RED + TextUtil.toTiny("Requiere Nivel III")));

        // Efectos Permanentes de Equipo
        gui.setItem(29, createUpgradeItem(Material.SUGAR, ChatColor.AQUA + TextUtil.toTiny("Velocidad de Equipo"), 25, Material.COAL, "speed", 
                ChatColor.GRAY + TextUtil.toTiny("Velocidad I permanente para todos.")));
        gui.setItem(31, createUpgradeItem(Material.GOLDEN_PICKAXE, ChatColor.GOLD + TextUtil.toTiny("Prisa Minera de Equipo"), 25, Material.COAL, "haste", 
                ChatColor.GRAY + TextUtil.toTiny("Prisa Minera I permanente para todos.")));

        player.openInventory(gui);
    }

    private void addBackButton(Inventory gui) {
        gui.setItem(40, createCategoryItem(Material.ARROW, ChatColor.RED + "" + ChatColor.BOLD + TextUtil.toTiny("Volver"), 
                ChatColor.GRAY + TextUtil.toTiny("Regresar al menú principal")));
    }

    private ItemStack createCategoryItem(Material mat, String name, String loreLine) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add(loreLine);
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createItem(Material mat, String name, int price, Material currency, String... loreLines) {
        return createItem(mat, 1, name, price, currency, loreLines);
    }

    public ItemStack createItem(Material mat, int amount, String name, int price, Material currency, String... loreLines) {
        ItemStack item = new ItemStack(mat, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            if (loreLines != null) {
                for (String line : loreLines) lore.add(line);
            }

            if (price > 0 && currency != null) {
                lore.add(" ");
                String currencyName = (currency == Material.EMERALD) ? "Esmeraldas" : "Carbón";
                ChatColor currencyColor = (currency == Material.EMERALD) ? ChatColor.GREEN : ChatColor.DARK_GRAY;
                lore.add(ChatColor.GRAY + TextUtil.toTiny("Costo: ") + currencyColor + price + " " + TextUtil.toTiny(currencyName));

                meta.getPersistentDataContainer().set(KEY_PRICE, PersistentDataType.INTEGER, price);
                meta.getPersistentDataContainer().set(KEY_CURRENCY, PersistentDataType.STRING, currency.name());
            }

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createUpgradeItem(Material mat, String name, int price, Material currency, String upgradeKey, String... loreLines) {
        ItemStack item = createItem(mat, 1, name, price, currency, loreLines);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.PROTECTION, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.getPersistentDataContainer().set(KEY_UPGRADE, PersistentDataType.STRING, upgradeKey);
            item.setItemMeta(meta);
        }
        return item;
    }
}
