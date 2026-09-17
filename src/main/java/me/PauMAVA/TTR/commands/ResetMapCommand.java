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

        if (args.length > 0 && args[0].equalsIgnoreCase("backup")) {
            org.bukkit.Location lobby = TTRCore.getInstance().getConfigManager().getLobbyLocation();
            org.bukkit.World arenaWorld = (lobby != null) ? lobby.getWorld() : (org.bukkit.Bukkit.getWorlds().isEmpty() ? null : org.bukkit.Bukkit.getWorlds().get(0));
            if (arenaWorld != null && TTRCore.getInstance().getWorldBackupManager() != null) {
                boolean ok = TTRCore.getInstance().getWorldBackupManager().createBackup(arenaWorld);
                if (ok) {
                    sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("¡Copia de seguridad del mapa creada exitosamente!"));
                } else {
                    sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Error al crear la copia de seguridad."));
                }
                return true;
            }
        }

        sender.sendMessage(TTRPrefix.TTR_GAME + TextUtil.toTiny("Iniciando regeneración de mapa a estado 0..."));
        int count = TTRCore.getInstance().getRollbackManager().restoreMap();
        sender.sendMessage(TTRPrefix.TTR_SUCCESS + 
                TextUtil.toTiny("¡Mapa regenerado exitosamente! ") + 
                ChatColor.YELLOW + count + ChatColor.GREEN + TextUtil.toTiny(" bloques y estado restaurados."));
        return true;
    }
}
