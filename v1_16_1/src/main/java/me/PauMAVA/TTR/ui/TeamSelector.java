package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TeamSelector implements Listener {

    private Player player;
    private Inventory inv;

    // Constructor
    public TeamSelector() {}

    public TeamSelector(Player player) {
        this.player = player;
        this.inv = Bukkit.createInventory(null, 9, "Elige tu Equipo");
    }

    public void openSelector() {
        // Red Team (Slot 3)
        ItemStack red = new ItemStack(Material.RED_WOOL);
        ItemMeta redMeta = red.getItemMeta();
        redMeta.setDisplayName(ChatColor.RED + "Equipo Rojo");
        red.setItemMeta(redMeta);
        inv.setItem(3, red);

        // Blue Team (Slot 5)
        ItemStack blue = new ItemStack(Material.BLUE_WOOL);
        ItemMeta blueMeta = blue.getItemMeta();
        blueMeta.setDisplayName(ChatColor.BLUE + "Equipo Azul");
        blue.setItemMeta(blueMeta);
        inv.setItem(5, blue);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Elige tu Equipo")) return;
        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;
        Player p = (Player) event.getWhoClicked();

        String teamName = null;
        if (event.getCurrentItem().getType() == Material.RED_WOOL) teamName = "Red";
        else if (event.getCurrentItem().getType() == Material.BLUE_WOOL) teamName = "Blue";

        if (teamName != null) {
            TTRCore.getInstance().getTeamHandler().addPlayer(teamName, p);
            p.sendMessage(ChatColor.GREEN + "Te has unido al equipo " + teamName);
            p.closeInventory();

            // --- ENTRAR SI LA PARTIDA YA EMPEZÓ ---
            if (TTRCore.getInstance().getCurrentMatch().getStatus() == MatchStatus.INGAME) {
                TTRCore.getInstance().getCurrentMatch().joinPlayerToMatch(p);
                p.sendMessage(ChatColor.YELLOW + "¡Uniéndote a la partida en curso!");
            }
        }
    }
}