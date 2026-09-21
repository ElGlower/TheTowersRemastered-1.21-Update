package me.PauMAVA.TTR.commands;

import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.match.ChestRestockManager;
import me.PauMAVA.TTR.match.LobbyParkourManager;
import me.PauMAVA.TTR.match.MatchStatus;
import me.PauMAVA.TTR.match.ZoneWandManager;
import me.PauMAVA.TTR.ui.AdminLeadersGUI;
import me.PauMAVA.TTR.ui.AuctionDraftGUI;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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
                sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt ping [jugador]" + ChatColor.GRAY + " - " + TextUtil.toTiny("Ver latencia y estado de red"));
                sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                return true;
            }

            sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
            sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "◆ " + TextUtil.toTiny("DESTINY TOWERS 26.3 / 26.2") + " ◆");
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt config" + ChatColor.GRAY + " - " + TextUtil.toTiny("Abrir panel de configuración (GUI)"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt screen" + ChatColor.GRAY + " - " + TextUtil.toTiny("Abrir modo pantalla interactivo (Libro)"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt start [now]" + ChatColor.GRAY + " - " + TextUtil.toTiny("Iniciar preparación / partida"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt next" + ChatColor.GRAY + " - " + TextUtil.toTiny("Saltar fase activa / Iniciar de inmediato"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt time <add|set> <s >" + ChatColor.GRAY + " - " + TextUtil.toTiny("Modificar tiempo en tiempo real"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt stop / cancel" + ChatColor.GRAY + " - " + TextUtil.toTiny("Detener y restablecer partida al lobby"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt resetmap" + ChatColor.GRAY + " - " + TextUtil.toTiny("Regenerar mapa atómicamente"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt wand [setspawn|setcage|setbase|setlobby]" + ChatColor.GRAY + " - " + TextUtil.toTiny("Varita de delimitación de zonas"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt parkour <start|cp|end|clear>" + ChatColor.GRAY + " - " + TextUtil.toTiny("Gestionar parkour del lobby"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt reroll" + ChatColor.GRAY + " - " + TextUtil.toTiny("Barajar equipos aleatoriamente"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt credits <add|set> <red|blue> <cant>" + ChatColor.GRAY + " - " + TextUtil.toTiny("Gestión de créditos de subasta"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt restock" + ChatColor.GRAY + " - " + TextUtil.toTiny("Reabastecer cofres y purgar líquidos"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt leaders" + ChatColor.GRAY + " - " + TextUtil.toTiny("Gestor de líderes, equipos y subastas (GUI)"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt voteleader" + ChatColor.GRAY + " - " + TextUtil.toTiny("Iniciar votación de líderes"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt auction [start|skip|gui]" + ChatColor.GRAY + " - " + TextUtil.toTiny("Control de subasta"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt shop <on/off>" + ChatColor.GRAY + " - " + TextUtil.toTiny("Activar/Desactivar tienda de faro"));
            sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.YELLOW + "/dt pingequalizer <on|off> [ms]" + ChatColor.GRAY + " - " + TextUtil.toTiny("Igualador de latencia internacional"));
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
            case "stats":
            case "estadisticas":
                return new StatsCommand().onCommand(sender, command, label, subArgs);
            case "ping":
            case "ms":
            case "latencia": {
                Player target = null;
                if (subArgs.length > 0) {
                    target = Bukkit.getPlayer(subArgs[0]);
                    if (target == null) {
                        sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Jugador no encontrado."));
                        return true;
                    }
                } else if (sender instanceof Player p) {
                    target = p;
                } else {
                    sender.sendMessage(TTRPrefix.TTR_ERROR + "Debes especificar un jugador desde la consola.");
                    return true;
                }
                int ping = target.getPing();
                String pingColor = (ping < 60) ? "§a" : (ping < 130 ? "§e" : "§c");
                sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "◆ " + TextUtil.toTiny("DIAGNÓSTICO DE RED Y LATENCIA") + " ◆");
                sender.sendMessage(ChatColor.GRAY + " » " + TextUtil.toTiny("Jugador: ") + ChatColor.WHITE + target.getName());
                sender.sendMessage(ChatColor.GRAY + " » " + TextUtil.toTiny("Ping actual: ") + pingColor + ping + "ms");
                me.PauMAVA.TTR.network.NetworkFairnessManager nfm = me.PauMAVA.TTR.network.NetworkFairnessManager.getInstance();
                boolean isTracking = nfm.isTrackingActive();
                boolean eqEnabled = nfm.isEqualizerEnabled();
                boolean eqActive = nfm.isEqualizerActive();
                boolean isAuto = nfm.isAutoEqualizer();
                int effectiveMs = nfm.getEffectiveTargetPing();

                sender.sendMessage(ChatColor.GRAY + " » " + TextUtil.toTiny("Compensación de Lag: ") + 
                        (isTracking ? ChatColor.GREEN + "ACTIVA " + ChatColor.GRAY + "(En combate - Historial 1.25s / BoundingBox Rewind)" 
                                    : ChatColor.YELLOW + "EN ESPERA " + ChatColor.GRAY + "(Inactiva en lobby - Se activa solo en combate)"));

                String modeLabel = isAuto ? "AUTO: " + effectiveMs + "ms" : effectiveMs + "ms";
                sender.sendMessage(ChatColor.GRAY + " » " + TextUtil.toTiny("Ping Equalizer: ") + 
                        (eqActive ? ChatColor.GREEN + "ACTIVO " + ChatColor.GRAY + "(" + modeLabel + ")" 
                                  : (eqEnabled ? ChatColor.YELLOW + "CONFIGURADO " + ChatColor.GRAY + "(" + modeLabel + " - se activa en combate)" 
                                               : ChatColor.RED + "DESACTIVADO")));
                sender.sendMessage(ChatColor.GOLD + "▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                return true;
            }
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
            case "cancel":
                return new StopCommand().onCommand(sender, command, label, subArgs);
            case "next":
            case "skipphase":
            case "forcestart":
            case "skip": {
                TTRCore plugin = TTRCore.getInstance();
                if (plugin.getModeAnnouncementManager().isAnnouncing()) {
                    plugin.getModeAnnouncementManager().forceStartNow();
                    sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Lectura de reglas omitida. Iniciando fase..."));
                    return true;
                } else if (plugin.getLeaderVoteManager().isActive()) {
                    plugin.getLeaderVoteManager().forceNextRound();
                    sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Ronda de votación acelerada."));
                    return true;
                } else if (plugin.getAuctionDraftManager().isActive()) {
                    Player p = (sender instanceof Player pl) ? pl : null;
                    plugin.getAuctionDraftManager().skipCandidate(p);
                    sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Jugador en subasta omitido/asignado."));
                    return true;
                } else if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().isPreparing()) {
                    plugin.getCurrentMatch().skipPreparation();
                    sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Fase de preparación finalizada."));
                    return true;
                } else if (plugin.isCounting()) {
                    plugin.getAutoStarter().forceStart();
                    sender.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Conteo forzado a inicio inmediato."));
                    return true;
                } else if (plugin.getCurrentMatch() == null || plugin.getCurrentMatch().getStatus() == MatchStatus.LOBBY) {
                    return new StartCommand().onCommand(sender, command, label, new String[]{"now"});
                } else {
                    sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("No hay ninguna fase activa para saltar."));
                    return true;
                }
            }
            case "time":
            case "timer": {
                if (subArgs.length < 2) {
                    sender.sendMessage(TTRPrefix.TTR_GAME + ChatColor.YELLOW + "Uso: /dt time <add|set> <segundos>");
                    return true;
                }
                String tSub = subArgs[0].toLowerCase();
                int seconds;
                try {
                    seconds = Integer.parseInt(subArgs[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Cantidad de segundos no válida."));
                    return true;
                }

                TTRCore plugin = TTRCore.getInstance();
                if (tSub.equals("add")) {
                    if (plugin.getModeAnnouncementManager().isAnnouncing()) {
                        plugin.getModeAnnouncementManager().addSeconds(seconds);
                    } else if (plugin.getLeaderVoteManager().isActive()) {
                        plugin.getLeaderVoteManager().addSeconds(seconds);
                    } else if (plugin.getAuctionDraftManager().isActive()) {
                        plugin.getAuctionDraftManager().addSeconds(seconds);
                    } else if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().isPreparing()) {
                        plugin.getCurrentMatch().addPreparationTime(seconds);
                    } else if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().isOnCourse()) {
                        plugin.getCurrentMatch().addGameTime(seconds);
                    } else {
                        sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("No hay ninguna fase activa con temporizador."));
                    }
                    return true;
                } else if (tSub.equals("set")) {
                    if (plugin.getModeAnnouncementManager().isAnnouncing()) {
                        plugin.getModeAnnouncementManager().setSeconds(seconds);
                    } else if (plugin.getLeaderVoteManager().isActive()) {
                        plugin.getLeaderVoteManager().setSeconds(seconds);
                    } else if (plugin.getAuctionDraftManager().isActive()) {
                        plugin.getAuctionDraftManager().setSeconds(seconds);
                    } else if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().isPreparing()) {
                        plugin.getCurrentMatch().setPreparationTime(seconds);
                    } else if (plugin.getCurrentMatch() != null && plugin.getCurrentMatch().isOnCourse()) {
                        plugin.getCurrentMatch().setGameTime(seconds);
                    } else {
                        sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("No hay ninguna fase activa con temporizador."));
                    }
                    return true;
                }
                sender.sendMessage(TTRPrefix.TTR_GAME + ChatColor.YELLOW + "Uso: /dt time <add|set> <segundos>");
                return true;
            }
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
                } else if (wandSub.equals("setbase") && subArgs.length > 1) {
                    String team = subArgs[1];
                    Location pos1 = ZoneWandManager.getInstance().getPos1(p);
                    Location pos2 = ZoneWandManager.getInstance().getPos2(p);
                    if (pos1 == null || pos2 == null) {
                        p.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Debes seleccionar Pos1 y Pos2 con la varita primero."));
                        return true;
                    }
                    TTRCore.getInstance().getConfigManager().setTeamBaseRegion(team, pos1, pos2);
                    p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Base de ") + ChatColor.YELLOW + team +
                            ChatColor.GREEN + TextUtil.toTiny(" delimitada y guardada exitosamente."));
                    return true;
                } else if (wandSub.equals("setlobby")) {
                    Location loc = p.getLocation();
                    TTRCore.getInstance().getConfigManager().setLobby(loc);
                    p.sendMessage(TTRPrefix.TTR_SUCCESS + TextUtil.toTiny("Lobby establecido en tu ubicación."));
                    return true;
                } else if (wandSub.equals("chest") && subArgs.length > 1) {
                    org.bukkit.block.Block target = p.getTargetBlockExact(5);
                    if (target == null) {
                        p.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Debes estar mirando directamente a un cofre o contenedor."));
                        return true;
                    }
                    ZoneWandManager.getInstance().handleChestClick(p, target, true);
                    return true;
                } else if (wandSub.equals("inspect")) {
                    org.bukkit.block.Block target = p.getTargetBlockExact(5);
                    if (target == null) {
                        p.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("Debes estar mirando a un bloque o cofre."));
                        return true;
                    }
                    ZoneWandManager.getInstance().handleChestClick(p, target, false);
                    return true;
                }
                p.sendMessage(TTRPrefix.TTR_GAME + ChatColor.YELLOW + "Uso: /dt wand [setspawn <red|blue> | setcage <red|blue> | setbase <red|blue> | setlobby | chest <red|blue> | inspect]");
                return true;
            }
            case "edit":
            case "editmode": {
                if (TTRCore.getInstance().getRollbackManager() == null) {
                    sender.sendMessage(TTRPrefix.TTR_ERROR + TextUtil.toTiny("RollbackManager no está activo."));
                    return true;
                }
                boolean current = TTRCore.getInstance().getRollbackManager().isEditMode();
                boolean newMode;
                if (subArgs.length > 0) {
                    newMode = subArgs[0].equalsIgnoreCase("on") || subArgs[0].equalsIgnoreCase("true");
                } else {
                    newMode = !current;
                }
                TTRCore.getInstance().getRollbackManager().setEditMode(newMode);
                if (newMode) {
                    sender.sendMessage(TTRPrefix.TTR_ADMIN + ChatColor.GREEN + "" + ChatColor.BOLD + TextUtil.toTiny("¡Modo Edición ACTIVADO!") +
                            ChatColor.YELLOW + " Los cambios que hagas ahora NO serán revertidos en el rollback.");
                } else {
                    sender.sendMessage(TTRPrefix.TTR_ADMIN + ChatColor.RED + "" + ChatColor.BOLD + TextUtil.toTiny("¡Modo Edición DESACTIVADO!") +
                            ChatColor.GRAY + " El mapa vuelve a registrar cambios para el rollback.");
                }
                return true;
            }
            case "savemap":
            case "save": {
                if (TTRCore.getInstance().getRollbackManager() != null) {
                    TTRCore.getInstance().getRollbackManager().clearHistory();
                }
                Location lobby = TTRCore.getInstance().getConfigManager().getLobbyLocation();
                if (lobby != null && lobby.getWorld() != null) {
                    lobby.getWorld().save();
                }
                for (org.bukkit.World w : Bukkit.getWorlds()) {
                    if (w.getName().contains("towers") || (lobby != null && w.equals(lobby.getWorld()))) {
                        w.save();
                    }
                }
                TTRCore.getInstance().saveConfig();
                sender.sendMessage(TTRPrefix.TTR_SUCCESS + ChatColor.GREEN + "" + ChatColor.BOLD + TextUtil.toTiny("¡Mapa guardado exitosamente!") +
                        ChatColor.WHITE + " El estado actual del mundo es ahora la plantilla base para futuros reinicios.");
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
                    if (p.getGameMode() != GameMode.SPECTATOR) eligible.add(p);
                }
                if (eligible.isEmpty()) {
                    eligible.addAll(Bukkit.getOnlinePlayers());
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
            case "pingequalizer":
            case "pe":
            case "equalizer": {
                me.PauMAVA.TTR.network.NetworkFairnessManager nfm = me.PauMAVA.TTR.network.NetworkFairnessManager.getInstance();
                if (subArgs.length == 0) {
                    boolean cur = nfm.isEqualizerEnabled();
                    boolean isAuto = nfm.isAutoEqualizer();
                    sender.sendMessage(TTRPrefix.TTR_ADMIN + TextUtil.toTiny("Ping Equalizer está actualmente ") +
                            (cur ? ChatColor.GREEN + "ACTIVADO " + ChatColor.GRAY + (isAuto ? "(Modo AUTO: " + nfm.getEffectiveTargetPing() + "ms)" : "(Objetivo: " + nfm.getTargetPing() + "ms)") 
                                 : ChatColor.RED + "DESACTIVADO") + ".");
                    sender.sendMessage(ChatColor.YELLOW + "Uso: /dt pingequalizer <on|off|auto> [ms]");
                    return true;
                }
                String mode = subArgs[0].toLowerCase();
                if (mode.equals("auto")) {
                    nfm.setEqualizerEnabled(true);
                    nfm.setAutoEqualizer(true);
                    int eff = nfm.getEffectiveTargetPing();
                    Bukkit.broadcastMessage(TTRPrefix.TTR_ADMIN + ChatColor.YELLOW + TextUtil.toTiny("Modo Ping Equalizer ") +
                            ChatColor.GREEN + "" + ChatColor.BOLD + "ACTIVADO (MODO AUTO) " + ChatColor.GRAY + "(Nivelando dinámicamente a ~" + eff + "ms en combate)" +
                            ChatColor.YELLOW + TextUtil.toTiny(" por la administración."));
                    return true;
                }
                boolean enable = mode.equals("on") || mode.equals("true");
                nfm.setEqualizerEnabled(enable);
                nfm.setAutoEqualizer(false);
                if (subArgs.length > 1) {
                    try {
                        int target = Integer.parseInt(subArgs[1]);
                        nfm.setTargetPing(target);
                    } catch (NumberFormatException ignored) {}
                }
                int target = nfm.getTargetPing();
                Bukkit.broadcastMessage(TTRPrefix.TTR_ADMIN + ChatColor.YELLOW + TextUtil.toTiny("Modo Ping Equalizer ") +
                        (enable ? ChatColor.GREEN + "ACTIVADO " + ChatColor.GRAY + "(" + target + "ms objetivo - se activa en combate)" : ChatColor.RED + "DESACTIVADO") +
                        ChatColor.YELLOW + TextUtil.toTiny(" por la administración."));
                return true;
            }
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
