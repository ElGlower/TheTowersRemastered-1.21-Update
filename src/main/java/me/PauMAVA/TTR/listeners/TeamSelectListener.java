package me.PauMAVA.TTR.listeners;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.teams.TTRTeam;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
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
    private final String GUI_TITLE = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + TextUtil.toTiny("Seleccionar Equipo");

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

    public void openTeamGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, GUI_TITLE);

        int redCount = 0;
        int blueCount = 0;
        TTRTeam redTeam = plugin.getTeamHandler().getTeam("Red");
        TTRTeam blueTeam = plugin.getTeamHandler().getTeam("Blue");
        if (redTeam != null) redCount = redTeam.getPlayers().size();
        if (blueTeam != null) blueCount = blueTeam.getPlayers().size();

        ItemStack red = createGuiItem(Material.RED_WOOL, 
                ChatColor.RED + "" + ChatColor.BOLD + TextUtil.toTiny("Equipo Rojo"), 
                ChatColor.GRAY + TextUtil.toTiny("Jugadores: ") + ChatColor.YELLOW + TextUtil.toTiny(String.valueOf(redCount)),
                ChatColor.DARK_GRAY + TextUtil.toTiny("Click para unirte"));

        ItemStack blue = createGuiItem(Material.BLUE_WOOL, 
                ChatColor.BLUE + "" + ChatColor.BOLD + TextUtil.toTiny("Equipo Azul"), 
                ChatColor.GRAY + TextUtil.toTiny("Jugadores: ") + ChatColor.YELLOW + TextUtil.toTiny(String.valueOf(blueCount)),
                ChatColor.DARK_GRAY + TextUtil.toTiny("Click para unirte"));

        ItemStack spec = createGuiItem(Material.ENDER_EYE, 
                ChatColor.GRAY + "" + ChatColor.BOLD + TextUtil.toTiny("Espectador"), 
                ChatColor.DARK_GRAY + TextUtil.toTiny("Observar la partida sin jugar"));

        gui.setItem(11, red);
        gui.setItem(15, blue);
        gui.setItem(22, spec);

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
    }

    public void openTeamSelection(Player player) {
        openTeamGUI(player);
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
            player.sendMessage(TTRPrefix.TTR_GAME + ChatColor.GRAY + TextUtil.toTiny("Ahora eres espectador."));
        }
    }

    private void attemptJoin(Player player, String teamName) {
        plugin.getTeamHandler().addPlayerToTeam(player, teamName);
        TTRTeam team = plugin.getTeamHandler().getPlayerTeam(player);

        if (team != null && team.getIdentifier().equalsIgnoreCase(teamName)) {
            player.closeInventory();
            ChatColor color = team.getColor();
            player.sendMessage(TTRPrefix.TTR_SUCCESS + color + TextUtil.toTiny("¡Te has unido al equipo " + teamName.toUpperCase() + "!"));
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

            if (plugin.getCurrentMatch().getStatus() == MatchStatus.INGAME) {
                spawnPlayerInGame(player, team);
            } else {
                player.sendMessage(TTRPrefix.TTR_GAME + ChatColor.YELLOW + TextUtil.toTiny("Esperando a que comience la partida..."));
            }
        } else {
            player.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("No pudiste unirte al equipo " + teamName));
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

        // 3. Aplicar mejoras activas del equipo (Protección, Speed, Haste)
        if (team.getArmorProtectionLevel() > 0) {
            ItemStack[] armor = player.getInventory().getArmorContents();
            for (ItemStack piece : armor) {
                if (piece != null && piece.getType() != Material.AIR) {
                    piece.addUnsafeEnchantment(Enchantment.PROTECTION, team.getArmorProtectionLevel());
                }
            }
            player.getInventory().setArmorContents(armor);
        }

        if (team.hasTeamSpeed()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
        }
        if (team.hasTeamHaste()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, Integer.MAX_VALUE, 0, false, false));
        }

        // 4. Teletransporte al Spawn del equipo (CORREGIDO: usando ConfigManager)
        Location spawnLoc = plugin.getConfigManager().getTeamSpawn(team.getIdentifier());
        if (spawnLoc != null) {
            player.teleport(spawnLoc);
        } else {
            Location lobby = plugin.getConfigManager().getLobbyLocation();
            if (lobby != null) player.teleport(lobby);
        }
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }

    private ItemStack createColoredArmor(Material mat, Color color) {
        ItemStack item = new ItemStack(mat);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta != null) {
            meta.setColor(color);
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGuiItem(Material mat, String name, String... loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(line);
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
