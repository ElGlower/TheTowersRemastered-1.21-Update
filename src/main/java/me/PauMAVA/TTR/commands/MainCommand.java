package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class MainCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("destinytowers.admin") && !sender.hasPermission("ttr.admin")) {
            sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("No tienes permisos para administrar Destiny Towers."));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "◆ " + TextUtil.toTiny("DESTINY TOWERS 26.3 / 26.2") + " ◆");
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt config" + ChatColor.GRAY + " - " + TextUtil.toTiny("Abrir panel de configuración (GUI)"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt screen" + ChatColor.GRAY + " - " + TextUtil.toTiny("Abrir modo pantalla interactivo (Libro)"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt start" + ChatColor.GRAY + " - " + TextUtil.toTiny("Iniciar la partida"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt stop" + ChatColor.GRAY + " - " + TextUtil.toTiny("Detener la partida"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt resetmap" + ChatColor.GRAY + " - " + TextUtil.toTiny("Regenerar mapa atómicamente sin reiniciar"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt set <opción>" + ChatColor.GRAY + " - " + TextUtil.toTiny("Configurar lobby, spawns y jaulas"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt event <nombre/stop>" + ChatColor.GRAY + " - " + TextUtil.toTiny("Lanzar eventos de caos"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt forcejoin <player> <equipo>" + ChatColor.GRAY + " - " + TextUtil.toTiny("Forzar equipo"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt revive <player>" + ChatColor.GRAY + " - " + TextUtil.toTiny("Revivir jugador"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt reload" + ChatColor.GRAY + " - " + TextUtil.toTiny("Recargar configuración"));
            sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            return true;
        }

        String sub = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (sub) {
            case "start":
                return new StartCommand().onCommand(sender, command, label, subArgs);
            case "stop":
                return new StopCommand().onCommand(sender, command, label, subArgs);
            case "resetmap":
            case "rollback":
                return new ResetMapCommand().onCommand(sender, command, label, subArgs);
            case "set":
                return new SetupCommand().onCommand(sender, command, label, subArgs);
            case "config":
                return new ConfigCommand().onCommand(sender, command, label, subArgs);
            case "gui":
            case "menu":
                return new ConfigCommand().onCommand(sender, command, label, new String[]{"gui"});
            case "screen":
            case "pantalla":
            case "book":
                return new ConfigCommand().onCommand(sender, command, label, new String[]{"screen"});
            case "event":
            case "events":
                return new EventCommand().onCommand(sender, command, label, subArgs);
            case "forcejoin":
                return new ForceJoinCommand().onCommand(sender, command, label, subArgs);
            case "revive":
                return new ReviveCommand().onCommand(sender, command, label, subArgs);
            case "spectate":
                return new SpectateCommand().onCommand(sender, command, label, subArgs);
            case "play":
            case "join":
                return new JoinCommand().onCommand(sender, command, label, subArgs);
            case "reload":
                TTRCore.getInstance().getConfigManager().reload();
                sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Configuración recargada con éxito."));
                return true;
            default:
                sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Subcomando desconocido. Usa /dt para ver la ayuda."));
                return true;
        }
    }
}
