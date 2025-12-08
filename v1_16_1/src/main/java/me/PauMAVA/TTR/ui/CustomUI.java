/*
 * TheTowersRemastered (TTR)
 * Copyright (c) 2019-2021  Pau Machetti Vallverdú
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
//Modificado por CeStart,Ripkyng,ElGlower en 2025

package me.PauMAVA.TTR.ui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class CustomUI implements Listener {

    private Inventory inventory;
    private String title;
    private int size;

    CustomUI(int size, String title) {
        // En 1.21, se recomienda usar Componentes de chat, pero String sigue funcionando por compatibilidad
        this.inventory = Bukkit.getServer().createInventory(null, size, title);
        this.title = title;
        this.size = size;
    }

    void openUI(Player player) {
        player.openInventory(this.inventory);
    }

    void closeUI(Player player) {
        // CORRECCIÓN LÓGICA 1.21:
        // 'getOpenInventory()' devuelve una vista, no el inventario directo.
        // Debemos comparar con 'getTopInventory()' para saber si es el nuestro.
        if (player.getOpenInventory().getTopInventory().equals(this.inventory)) {
            player.closeInventory();
        }
    }

    // Hemos quitado @Nullable de los argumentos 'title' y 'lore' para arreglar el error de compilación.
    public void setSlot(int id, ItemStack item, String title, String lore) {
        if (title == null) {
            this.inventory.setItem(id, item);
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(title);
            if (lore != null) {
                // Arrays.asList devuelve una lista fija, ArrayList permite modificaciones si fuera necesario.
                meta.setLore(new ArrayList<>(Arrays.asList(lore)));
            }
            item.setItemMeta(meta);
        }
        this.inventory.setItem(id, item);
    }

    public void clearSlot(int id) {
        this.inventory.clear(id);
    }

    public void clearUI() {
        this.inventory.clear();
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    @EventHandler
    abstract void onInventoryClick(InventoryClickEvent event);

}