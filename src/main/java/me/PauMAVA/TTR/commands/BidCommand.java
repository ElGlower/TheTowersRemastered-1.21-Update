package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BidCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(TTRPrefix.TTR_ERROR + "Solo jugadores.");
            return true;
        }

        if (!TTRCore.getInstance().getAuctionDraftManager().isActive()) {
            player.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("No hay una subasta activa en este momento."));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(TTRPrefix.TTR_GAME + ChatColor.YELLOW + TextUtil.toTiny("Uso: ") +
                    ChatColor.WHITE + "/" + label + " <cantidad>");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Debes ingresar un número válido."));
            return true;
        }

        if (amount <= 0) {
            player.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("La puja debe ser mayor a 0."));
            return true;
        }

        TTRCore.getInstance().getAuctionDraftManager().bid(player, amount);
        return true;
    }
}
