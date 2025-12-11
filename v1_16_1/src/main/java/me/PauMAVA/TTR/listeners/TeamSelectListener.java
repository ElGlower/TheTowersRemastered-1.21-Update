package me.PauMAVA.TTR.listeners;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class TeamSelectListener implements Listener {

    private final TTRCore plugin;
    private final String GUI_TITLE = ChatColor.DARK_AQUA + "Seleccionar Equipo";

    public TeamSelectListener(TTRCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && item.getType() == Material.NETHER_STAR) {
                event.setCancelled(true);
                openTeamGUI(event.getPlayer());
            }
        }
    }

    private void openTeamGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        ItemStack red = createGuiItem(Material.RED_WOOL, ChatColor.RED + "Equipo Rojo", ChatColor.GRAY + "Click para unirte al " + ChatColor.RED + "ROJO");
        ItemStack blue = createGuiItem(Material.BLUE_WOOL, ChatColor.BLUE + "Equipo Azul", ChatColor.GRAY + "Click para unirte al " + ChatColor.BLUE + "AZUL");
        ItemStack spec = createGuiItem(Material.ENDER_EYE, ChatColor.GRAY + "Espectador", ChatColor.DARK_GRAY + "Observar la partida");

        gui.setItem(11, red);
        gui.setItem(15, blue);
        gui.setItem(22, spec);

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        Material mat = event.getCurrentItem().getType();

        if (mat == Material.RED_WOOL) attemptJoin(player, "Red");
        else if (mat == Material.BLUE_WOOL) attemptJoin(player, "Blue");
        else if (mat == Material.ENDER_EYE) {
            player.closeInventory();
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage(ChatColor.GRAY + "Ahora eres espectador.");
        }
    }

    private void attemptJoin(Player player, String teamName) {
        plugin.getTeamHandler().addPlayerToTeam(player, teamName);
        TTRTeam team = plugin.getTeamHandler().getPlayerTeam(player);

        if (team != null && team.getIdentifier().equalsIgnoreCase(teamName)) {
            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "¡Te has unido al equipo " + teamName.toUpperCase() + "!");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

            if (plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
                spawnPlayerInGame(player, team);
            } else {
                player.sendMessage(ChatColor.YELLOW + "Espera a que inicie la partida...");
            }
        } else {
            player.sendMessage(ChatColor.RED + "No pudimos unirte al equipo " + teamName);
        }
    }

    private void spawnPlayerInGame(Player player, TTRTeam team) {
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();

        boolean isRed = team.getIdentifier().equalsIgnoreCase("Red");
        Color armorColor = isRed ? Color.RED : Color.BLUE;
        Material glassMat = isRed ? Material.RED_STAINED_GLASS : Material.BLUE_STAINED_GLASS;

        // 1. Armadura de Cuero Tintada
        player.getInventory().setHelmet(createColoredArmor(Material.LEATHER_HELMET, armorColor));
        player.getInventory().setChestplate(createColoredArmor(Material.LEATHER_CHESTPLATE, armorColor));
        player.getInventory().setLeggings(createColoredArmor(Material.LEATHER_LEGGINGS, armorColor));
        player.getInventory().setBoots(createColoredArmor(Material.LEATHER_BOOTS, armorColor));

        // 2. Ítems del Kit
        player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
        player.getInventory().addItem(new ItemStack(glassMat, 32));
        player.getInventory().addItem(new ItemStack(Material.BREAD, 16));

        // 3. APLICAR MEJORAS EXISTENTES (Late Join Fix)
        // Protección
        if (team.getArmorProtectionLevel() > 0) {
            ItemStack[] armor = player.getInventory().getArmorContents();
            for (ItemStack piece : armor) {
                if (piece != null && piece.getType() != Material.AIR) {
                    piece.addUnsafeEnchantment(Enchantment.PROTECTION, team.getArmorProtectionLevel());
                }
            }
            player.getInventory().setArmorContents(armor);
        }

        // Efectos (Speed / Haste)
        if (team.hasTeamSpeed()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
        }
        if (team.hasTeamHaste()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 0, false, false));
        }

        // 4. TP al Spawn
        if (plugin.getConfig().contains("spawns." + team.getIdentifier())) {
            player.teleport(plugin.getConfig().getLocation("spawns." + team.getIdentifier()));
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    }

    private ItemStack createColoredArmor(Material mat, Color color) {
        ItemStack item = new ItemStack(mat);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color);
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createGuiItem(Material mat, String name, String loreLine) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        lore.add(loreLine);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}