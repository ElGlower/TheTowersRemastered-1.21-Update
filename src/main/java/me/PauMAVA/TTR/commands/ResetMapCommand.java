package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ResetMapCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("ttr.admin")) {
            sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("No tienes permiso."));
            return true;
        }

        if (TTRCore.getInstance().getRollbackManager() == null) {
            sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("El motor de rollback no está activo."));
            return true;
        }

        sender.sendMessage(TTRPrefix.TTR_GAME + TextUtil.toTiny("Iniciando regeneración de mapa..."));
        int count = TTRCore.getInstance().getRollbackManager().restoreMap();
        sender.sendMessage(TTRPrefix.TTR_SUCCESS + 
                TextUtil.toTiny("¡Mapa regenerado exitosamente! ") + 
                ChatColor.YELLOW + count + ChatColor.GREEN + TextUtil.toTiny(" bloques restaurados."));
        return true;
    }
}
