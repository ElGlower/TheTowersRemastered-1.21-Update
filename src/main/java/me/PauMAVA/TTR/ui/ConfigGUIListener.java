package me.PauMAVA.TTR.ui;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.commands.SetupCommand;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class ConfigGUIListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getView().getTitle().equals(ConfigGUI.TITLE)) {
            event.setCancelled(true);

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            String action = clicked.getItemMeta().getPersistentDataContainer().get(ConfigGUI.KEY_ACTION, PersistentDataType.STRING);
            if (action == null) return;

            TTRCore plugin = TTRCore.getInstance();
            ClickType click = event.getClick();

            switch (action) {
                case "duration": {
                    int cur = plugin.getConfig().getInt("match.duration", 1200);
                    int delta = click.isShiftClick() ? 300 : (click.isRightClick() ? -60 : 60);
                    int next = Math.max(60, cur + delta);
                    plugin.getConfig().set("match.duration", next);
                    plugin.saveConfig();
                    if (plugin.getCurrentMatch() != null) {
                        plugin.getCurrentMatch().setRemainingTime(next);
                    }
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                    ConfigGUI.open(player);
                    break;
                }
                case "points": {
                    int cur = plugin.getConfig().getInt("match.maxpoints", 10);
                    int delta = click.isRightClick() ? -1 : 1;
                    int next = Math.max(1, cur + delta);
                    plugin.getConfig().set("match.maxpoints", next);
                    plugin.saveConfig();
                    if (plugin.getCurrentMatch() != null) {
                        plugin.getCurrentMatch().setMaxPointsToWin(next);
                    }
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                    ConfigGUI.open(player);
                    break;
                }
                case "autostart": {
                    if (click.isLeftClick()) {
                        boolean enabled = plugin.getConfig().getBoolean("autostart.enabled", true);
                        plugin.getConfig().set("autostart.enabled", !enabled);
                        plugin.saveConfig();
                        if (plugin.getAutoStarter() != null) {
                            if (!enabled) plugin.getAutoStarter().checkStartConditions();
                            else plugin.getAutoStarter().cancelCountdown();
                        }
                    } else if (click.isRightClick()) {
                        int cur = plugin.getConfig().getInt("autostart.count", 4);
                        int delta = click.isShiftClick() ? -1 : 1;
                        plugin.getConfig().set("autostart.count", Math.max(1, cur + delta));
                        plugin.saveConfig();
                    }
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                    ConfigGUI.open(player);
                    break;
                }
                case "rollback": {
                    if (click.isLeftClick()) {
                        player.closeInventory();
                        if (plugin.getRollbackManager() != null) {
                            int count = plugin.getRollbackManager().getRecordedBlockModifications();
                            plugin.getRollbackManager().rollback();
                            player.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Mapa regenerado (" + count + " bloques)."));
                            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1f, 1f);
                        }
                    } else {
                        boolean cur = plugin.getConfig().getBoolean("rollback.auto_restore_on_end", true);
                        plugin.getConfig().set("rollback.auto_restore_on_end", !cur);
                        plugin.saveConfig();
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                        ConfigGUI.open(player);
                    }
                    break;
                }
                case "matchcontrol": {
                    player.closeInventory();
                    if (click.isLeftClick()) {
                        player.performCommand("ttr start");
                    } else {
                        player.performCommand("ttr stop");
                    }
                    break;
                }
                case "lobby": {
                    player.closeInventory();
                    player.performCommand("setlobby");
                    break;
                }
                case "spawns": {
                    player.closeInventory();
                    if (click.isShiftClick()) {
                        if (click.isLeftClick()) player.performCommand("ttr set redcage");
                        else player.performCommand("ttr set bluecage");
                    } else {
                        if (click.isLeftClick()) player.performCommand("ttr set redspawn");
                        else player.performCommand("ttr set bluespawn");
                    }
                    break;
                }
                case "events_toggle": {
                    boolean cur = plugin.getEventManager().isAutoMode();
                    plugin.getEventManager().toggleAutoMode(!cur);
                    plugin.getConfig().set("events.enabled", !cur);
                    plugin.saveConfig();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                    ConfigGUI.open(player);
                    break;
                }
                case "beacon_toggle": {
                    boolean cur = plugin.isBeaconShopEnabled();
                    plugin.setBeaconShopEnabled(!cur);
                    player.sendMessage(TTRPrefix.TTR_GAME + TextUtil.toTiny("Tienda del faro: ") + (!cur ? ChatColor.GREEN + TextUtil.toTiny("ACTIVADA") : ChatColor.RED + TextUtil.toTiny("DESACTIVADA")));
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                    ConfigGUI.open(player);
                    break;
                }
                case "mode_cycle": {
                    plugin.setCurrentSelectionMode(plugin.getCurrentSelectionMode().next());
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                    ConfigGUI.open(player);
                    break;
                }
                case "admin_leaders": {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                    AdminLeadersGUI.open(player);
                    break;
                }
                case "screen": {
                    player.closeInventory();
                    ConfigScreen.openScreen(player);
                    break;
                }
                case "close": {
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.8f);
                    break;
                }
            }
        }
    }
}
