package me.PauMAVA.TTR.ui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class TeamSelector {

    public void open(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, ChatColor.DARK_AQUA + "Seleccionar Equipo");

        // --- EQUIPO ROJO ---
        ItemStack red = new ItemStack(Material.RED_WOOL);
        ItemMeta redMeta = red.getItemMeta();
        redMeta.setDisplayName(ChatColor.RED + "Equipo Rojo");
        List<String> redLore = new ArrayList<>();
        redLore.add(ChatColor.GRAY + "Click para unirte al");
        redLore.add(ChatColor.GRAY + "equipo " + ChatColor.RED + "ROJO");
        redMeta.setLore(redLore);
        red.setItemMeta(redMeta);

        // --- EQUIPO AZUL ---
        ItemStack blue = new ItemStack(Material.BLUE_WOOL);
        ItemMeta blueMeta = blue.getItemMeta();
        blueMeta.setDisplayName(ChatColor.BLUE + "Equipo Azul");
        List<String> blueLore = new ArrayList<>();
        blueLore.add(ChatColor.GRAY + "Click para unirte al");
        blueLore.add(ChatColor.GRAY + "equipo " + ChatColor.BLUE + "AZUL");
        blueMeta.setLore(blueLore);
        blue.setItemMeta(blueMeta);

        // --- ESPECTADOR (NUEVO) ---
        ItemStack spec = new ItemStack(Material.ENDER_EYE);
        ItemMeta specMeta = spec.getItemMeta();
        specMeta.setDisplayName(ChatColor.GRAY + "Espectador");
        List<String> specLore = new ArrayList<>();
        specLore.add(ChatColor.GRAY + "Click para observar");
        specLore.add(ChatColor.GRAY + "la partida sin jugar.");
        specMeta.setLore(specLore);
        spec.setItemMeta(specMeta);

        // Colocamos los items
        gui.setItem(3, red);
        gui.setItem(4, spec);
        gui.setItem(5, blue);

        player.openInventory(gui);
    }
}