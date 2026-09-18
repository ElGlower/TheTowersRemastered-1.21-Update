package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.ChestRestockManager;
import me.PauMAVA.TTR.match.LobbyParkourManager;
import me.PauMAVA.TTR.match.ZoneWandManager;
import me.PauMAVA.TTR.ui.AdminLeadersGUI;
import me.PauMAVA.TTR.ui.AuctionDraftGUI;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        boolean isAdmin = sender.hasPermission("destinytowers.admin") || sender.hasPermission("ttr.admin") || sender.isOp();

        if (args.length == 0) {
            if (!isAdmin) {
                sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "◆ " + TextUtil.toTiny("DESTINY TOWERS") + " ◆");
                sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt join <equipo>" + ChatColor.GRAY + " - " + TextUtil.toTiny("Unirte a un equipo"));
                sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt spectate" + ChatColor.GRAY + " - " + TextUtil.toTiny("Entrar a modo espectador"));
                sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt play" + ChatColor.GRAY + " - " + TextUtil.toTiny("Salir de espectador y jugar"));
                sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/bid <monto>" + ChatColor.GRAY + " - " + TextUtil.toTiny("Pujar en la subasta"));
                sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                return true;
            }

            sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "◆ " + TextUtil.toTiny("DESTINY TOWERS 26.3 / 26.2") + " ◆");
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt config" + ChatColor.GRAY + " - " + TextUtil.toTiny("Abrir panel de configuración (GUI)"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt screen" + ChatColor.GRAY + " - " + TextUtil.toTiny("Abrir modo pantalla interactivo (Libro)"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt start [now]" + ChatColor.GRAY + " - " + TextUtil.toTiny("Iniciar preparación / partida"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt stop" + ChatColor.GRAY + " - " + TextUtil.toTiny("Detener y limpiar partida"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt resetmap" + ChatColor.GRAY + " - " + TextUtil.toTiny("Regenerar mapa atómicamente"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt wand [setspawn|setcage|setlobby]" + ChatColor.GRAY + " - " + TextUtil.toTiny("Varita de delimitación de zonas"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt parkour <start|cp|end|clear>" + ChatColor.GRAY + " - " + TextUtil.toTiny("Gestionar parkour del lobby"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt reroll" + ChatColor.GRAY + " - " + TextUtil.toTiny("Barajar equipos aleatoriamente"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt credits <add|set> <red|blue> <cant>" + ChatColor.GRAY + " - " + TextUtil.toTiny("Gestión de créditos de subasta"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt restock" + ChatColor.GRAY + " - " + TextUtil.toTiny("Reabastecer cofres y purgar líquidos"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt leaders" + ChatColor.GRAY + " - " + TextUtil.toTiny("Gestor de líderes, equipos y subastas (GUI)"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt voteleader" + ChatColor.GRAY + " - " + TextUtil.toTiny("Iniciar votación de líderes"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt auction [start|skip|gui]" + ChatColor.GRAY + " - " + TextUtil.toTiny("Control de subasta"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt shop <on/off>" + ChatColor.GRAY + " - " + TextUtil.toTiny("Activar/Desactivar tienda de faro"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt reload" + ChatColor.GRAY + " - " + TextUtil.toTiny("Recargar configuración"));
            sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            return true;
        }

        String sub = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        // Subcomandos accesibles para todos los jugadores
        switch (sub) {
            case "play":
            case "join":
                return new JoinCommand().onCommand(sender, command, label, subArgs);
            case "spectate":
                return new SpectateCommand().onCommand(sender, command, label, subArgs);
            case "bid":
                return new BidCommand().onCommand(sender, command, label, subArgs);
        }

        // Subcomandos estrictamente administrativos
        if (!isAdmin) {
            sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("No tienes permisos para usar este comando administrativo."));
            return true;
        }

        switch (sub) {
            case "start":
                return new StartCommand().onCommand(sender, command, label, subArgs);
            case "stop":
                return new StopCommand().onCommand(sender, command, label, subArgs);
            case "resetmap":
            case "rollback":
                return new ResetMapCommand().onCommand(sender, command, label, subArgs);
            case "restock": {
                int touched = ChestRestockManager.getInstance().restockAndPurgeArenaChests(true);
                sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Cofres reabastecidos y líquidos purgados en ") +
                        ChatColor.YELLOW + touched + ChatColor.GREEN + TextUtil.toTiny(" contenedores."));
                return true;
            }
            case "wand": {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(TTRPrefix.TTR_ERROR + "Solo jugadores.");
                    return true;
                }
                if (subArgs.length == 0) {
                    ZoneWandManager.getInstance().giveWand(p);
                    return true;
                }
                String wandSub = subArgs[0].toLowerCase();
                if (wandSub.equals("setspawn") && subArgs.length > 1) {
                    String team = subArgs[1];
                    Location loc = p.getLocation();
                    TTRCore.getInstance().getConfigManager().setTeamSpawn(team, loc);
                    p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Spawn de ") + ChatColor.YELLOW + team +
                            ChatColor.GREEN + TextUtil.toTiny(" establecido en tu ubicación."));
                    return true;
                } else if (wandSub.equals("setcage") && subArgs.length > 1) {
                    String team = subArgs[1];
                    Location loc = p.getLocation();
                    TTRCore.getInstance().getConfigManager().setTeamCage(team, loc);
                    p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Jaula de ") + ChatColor.YELLOW + team +
                            ChatColor.GREEN + TextUtil.toTiny(" guardada en tu ubicación."));
                    return true;
                } else if (wandSub.equals("setlobby")) {
                    Location loc = p.getLocation();
                    TTRCore.getInstance().getConfigManager().setLobby(loc);
                    p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Lobby establecido en tu ubicación."));
                    return true;
                }
                p.sendMessage(TTRPrefix.TTR_GAME + ChatColor.YELLOW + "Uso: /dt wand [setspawn <red|blue> | setcage <red|blue> | setlobby]");
                return true;
            }
            case "parkour": {
                if (!(sender instanceof Player p)) {
                    sender.sendMessage(TTRPrefix.TTR_ERROR + "Solo jugadores.");
                    return true;
                }
                if (subArgs.length == 0) {
                    p.sendMessage(TTRPrefix.TTR_GAME + ChatColor.YELLOW + "Uso: /dt parkour <start | cp | end | clear>");
                    return true;
                }
                String pkSub = subArgs[0].toLowerCase();
                LobbyParkourManager pk = LobbyParkourManager.getInstance();
                if (pkSub.equals("start")) {
                    pk.setStartLocation(p.getLocation());
                    p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Punto de inicio del Parkour fijado."));
                    return true;
                } else if (pkSub.equals("cp") || pkSub.equals("addcheckpoint") || pkSub.equals("checkpoint")) {
                    pk.addCheckpoint(p.getLocation());
                    p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Checkpoint #") + pk.getCheckpoints().size() + TextUtil.toTiny(" guardado."));
                    return true;
                } else if (pkSub.equals("end") || pkSub.equals("meta")) {
                    pk.setEndLocation(p.getLocation());
                    p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Meta del Parkour fijada."));
                    return true;
                } else if (pkSub.equals("clear")) {
                    pk.clearParkour();
                    p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Todos los puntos de Parkour han sido borrados."));
                    return true;
                }
                p.sendMessage(TTRPrefix.TTR_GAME + ChatColor.YELLOW + "Uso: /dt parkour <start | cp | end | clear>");
                return true;
            }
            case "reroll": {
                List<Player> eligible = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!TTRCore.isAdmin(p)) eligible.add(p);
                }
                Collections.shuffle(eligible);
                TTRCore.getInstance().getTeamHandler().clearTeams();
                for (int i = 0; i < eligible.size(); i++) {
                    Player target = eligible.get(i);
                    String teamId = (i % 2 == 0) ? "Red" : "Blue";
                    TTRCore.getInstance().getTeamHandler().addPlayerToTeam(target, teamId);
                }
                Bukkit.broadcastMessage(TTRPrefix.TTR_GAME + ChatColor.YELLOW + "" + ChatColor.BOLD +
                        TextUtil.toTiny("¡Equipos barajados aleatoriamente por la administración!"));
                return true;
            }
            case "credits": {
                if (subArgs.length < 3) {
                    sender.sendMessage(TTRPrefix.TTR_GAME + ChatColor.YELLOW + "Uso: /dt credits <add|set> <red|blue> <monto>");
                    return true;
                }
                String action = subArgs[0].toLowerCase();
                String team = subArgs[1].toLowerCase();
                int amount;
                try {
                    amount = Integer.parseInt(subArgs[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Monto numérico no válido."));
                    return true;
                }
                if (action.equals("add")) {
                    TTRCore.getInstance().getAuctionDraftManager().addCredits(team, amount);
                } else if (action.equals("set")) {
                    TTRCore.getInstance().getAuctionDraftManager().setCredits(team, amount);
                }
                return true;
            }
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
            case "leaders":
            case "teams":
                if (sender instanceof Player p) {
                    AdminLeadersGUI.open(p);
                } else {
                    sender.sendMessage(TTRPrefix.TTR_ERROR + "Solo jugadores.");
                }
                return true;
            case "voteleader":
            case "voting":
                TTRCore.getInstance().getLeaderVoteManager().startVoting(true);
                sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Votación de líderes iniciada."));
                return true;
            case "auction":
            case "draft": {
                if (subArgs.length > 0) {
                    String aSub = subArgs[0].toLowerCase();
                    if (aSub.equals("skip")) {
                        Player p = (sender instanceof Player pl) ? pl : null;
                        TTRCore.getInstance().getAuctionDraftManager().skipCandidate(p);
                        return true;
                    } else if (aSub.equals("gui") && sender instanceof Player pl) {
                        AuctionDraftGUI.open(pl, TTRCore.getInstance().getAuctionDraftManager());
                        return true;
                    }
                }
                TTRCore.getInstance().getAuctionDraftManager().startDraft();
                sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Subasta de miembros iniciada."));
                return true;
            }
            case "shop": {
                if (subArgs.length > 0) {
                    boolean enable = subArgs[0].equalsIgnoreCase("on") || subArgs[0].equalsIgnoreCase("true");
                    TTRCore.getInstance().setBeaconShopEnabled(enable);
                    sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Tienda del faro ") + (enable ? "ACTIVADA" : "DESACTIVADA"));
                } else {
                    boolean cur = TTRCore.getInstance().isBeaconShopEnabled();
                    TTRCore.getInstance().setBeaconShopEnabled(!cur);
                    sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Tienda del faro ") + (!cur ? "ACTIVADA" : "DESACTIVADA"));
                }
                return true;
            }
            case "event":
            case "events":
                return new EventCommand().onCommand(sender, command, label, subArgs);
            case "forcejoin":
                return new ForceJoinCommand().onCommand(sender, command, label, subArgs);
            case "revive":
                return new ReviveCommand().onCommand(sender, command, label, subArgs);
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
