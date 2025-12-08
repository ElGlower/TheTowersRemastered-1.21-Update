/*
 * TheTowersRemastered (TTR) - 1.21 Updated Version
 * Copyright (c) 2019-2021  Pau Machetti Vallverdú (Author Original)
 * Copyright (c) 2025  @StartCes, @Ripkyng1, @ElGlower (Mantenedores/Updates)
 *
 */

package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EnableDisableCommand implements CommandExecutor {

    private TTRCore plugin;

    public EnableDisableCommand(TTRCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender theSender, Command cmd, String label, String[] args) {
        // Verificamos permisos por seguridad
        if (!theSender.hasPermission("ttr.admin")) {
            theSender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return true;
        }

        if (label.equalsIgnoreCase("ttrenable")) {
            plugin.getConfigManager().setEnableOnStart(true);

            // Mensaje directo en texto para asegurar que se vea
            theSender.sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.GREEN + "¡El juego ha sido HABILITADO correctamente!");
            theSender.sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.GRAY + "Ahora los jugadores pueden unirse.");

        } else if (label.equalsIgnoreCase("ttrdisable")) {
            plugin.getConfigManager().setEnableOnStart(false);

            //Mensaje directo en texto
            theSender.sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.RED + "¡El juego ha sido DESHABILITADO!");
            theSender.sendMessage(TTRPrefix.TTR_GAME + "" + ChatColor.GRAY + "Nadie podrá unirse hasta que lo habilites de nuevo.");
        }

        return true; // Devolvemos TRUE para que no salga el mensaje de error de sintaxis en el chat
    }
}