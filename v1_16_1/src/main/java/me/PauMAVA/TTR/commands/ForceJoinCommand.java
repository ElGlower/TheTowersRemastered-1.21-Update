package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ForceJoinCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ttr.admin")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Uso: /ttrforcejoin <Jugador> <Equipo>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Jugador no encontrado.");
            return true;
        }

        String teamNameArg = args[1];
        String realTeamName = null;

        for (String t : TTRCore.getInstance().getConfigManager().getTeamNames()) {
            if (t.equalsIgnoreCase(teamNameArg)) {
                realTeamName = t;
                break;
            }
        }

        if (realTeamName == null) {
            sender.sendMessage(ChatColor.RED + "El equipo '" + teamNameArg + "' no existe.");
            return true;
        }

        TTRCore.getInstance().getTeamHandler().addPlayer(realTeamName, target);

        if (TTRCore.getInstance().getCurrentMatch().getStatus() == MatchStatus.INGAME) {
            target.getInventory().clear();
            target.setGameMode(GameMode.SURVIVAL);
            target.setHealth(20);
            target.setFoodLevel(20);

            Location spawn = TTRCore.getInstance().getConfigManager().getTeamSpawn(realTeamName);
            if (spawn != null && spawn.getWorld() != null) target.teleport(spawn);

            giveKit(target, realTeamName);

            if (TTRCore.getInstance().getCurrentMatch().getBossBar() != null) {
                TTRCore.getInstance().getCurrentMatch().getBossBar().addPlayer(target);
            }

            // configuracion de la espada con esto subes la velocidad de ataque para que sea 1.8
            if (target.getAttribute(Attribute.GENERIC_ATTACK_SPEED) != null) {
                target.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(4.0);
            }

        }

        sender.sendMessage(TTRPrefix.TTR_GAME + " " + ChatColor.GREEN + "Jugador " + target.getName() + " unido a " + realTeamName);
        target.sendMessage(TTRPrefix.TTR_GAME + " " + ChatColor.YELLOW + "¡Te han unido a la partida!");

        return true;
    }

    private void giveKit(Player player, String teamName) {
        ChatColor color = TTRCore.getInstance().getConfigManager().getTeamColor(teamName);
        Color armorColor = (color == ChatColor.RED) ? Color.RED : Color.BLUE;
        if (color == ChatColor.GREEN) armorColor = Color.GREEN;
        if (color == ChatColor.YELLOW) armorColor = Color.YELLOW;

        ItemStack[] armor = new ItemStack[]{
                new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.LEATHER_LEGGINGS),
                new ItemStack(Material.LEATHER_CHESTPLATE), new ItemStack(Material.LEATHER_HELMET)
        };

        for (ItemStack item : armor) {
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
            if (meta != null) {
                meta.setColor(armorColor);
                meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
                meta.setUnbreakable(true);
                item.setItemMeta(meta);
            }
        }
        player.getInventory().setArmorContents(armor);
        player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
        player.getInventory().addItem(new ItemStack(Material.GLASS, 32));
        player.getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 32));
    }
}