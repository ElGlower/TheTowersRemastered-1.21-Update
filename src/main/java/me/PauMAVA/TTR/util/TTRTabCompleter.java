package me.PauMAVA.TTR.util;

import me.PauMAVA.TTR.TTRCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TTRTabCompleter implements TabCompleter {

    private static final List<String> ADMIN_MAIN_SUBS = Arrays.asList(
            "start", "stop", "cancel", "next", "skipphase", "time", "resetmap", "edit", "savemap", "wand", "parkour", "reroll", "credits", "restock",
            "set", "config", "screen", "gui", "leaders", "voteleader", "auction", "shop", "ping", "pingequalizer",
            "event", "forcejoin", "revive", "spectate", "play", "join", "bid", "stats", "estadisticas", "reload"
    );

    private static final List<String> PLAYER_MAIN_SUBS = Arrays.asList(
            "join", "play", "spectate", "bid", "stats", "estadisticas", "ping"
    );

    private static final List<String> SET_SUBS = Arrays.asList(
            "lobby", "redspawn", "bluespawn", "redcage", "bluecage", "iron", "coal", "emerald", "xp"
    );

    private static final List<String> CONFIG_SUBS = Arrays.asList(
            "time", "points", "prep", "autostart-time", "autostart-players", "vote-time", "rules-time", "auction-time", "gui", "screen"
    );

    private static final List<String> EVENT_SUBS = Arrays.asList(
            "jump", "speed", "blind", "giga", "mini", "meteors", "auto", "stop"
    );

    private static final List<String> WAND_SUBS = Arrays.asList(
            "setspawn", "setcage", "setbase", "setlobby", "chest", "inspect"
    );

    private static final List<String> PARKOUR_SUBS = Arrays.asList(
            "start", "cp", "end", "clear"
    );

    private static final List<String> CREDITS_SUBS = Arrays.asList(
            "add", "set"
    );

    private static final List<String> TEAMS = Arrays.asList(
            "Red", "Blue"
    );

    private static final List<String> AUCTION_SUBS = Arrays.asList(
            "start", "skip", "gui"
    );

    private static final List<String> COMMON_AMOUNTS = Arrays.asList(
            "1", "5", "10", "25", "50", "100"
    );

    private static final List<String> TIME_SUBS = Arrays.asList(
            "add", "set"
    );

    private static final List<String> COMMON_SECONDS = Arrays.asList(
            "5", "10", "15", "20", "30", "45", "60"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmdName = command.getName().toLowerCase();
        boolean isAdmin = sender.hasPermission("destinytowers.admin") || sender.hasPermission("ttr.admin") || sender.isOp();

        if (cmdName.equals("bid")) {
            if (args.length == 1) return filterStartingWith(COMMON_AMOUNTS, args[0]);
            return new ArrayList<>();
        }

        if (cmdName.equals("stats") || cmdName.equals("estadisticas") || cmdName.equals("dtstats")) {
            if (args.length == 1) return filterStartingWith(getOnlinePlayerNames(), args[0]);
            return new ArrayList<>();
        }

        if (cmdName.equals("dt") || cmdName.equals("destinytowers") || cmdName.equals("ttr") || cmdName.equals("thetowers") || cmdName.equals("towers")) {
            if (args.length == 1) {
                List<String> pool = isAdmin ? ADMIN_MAIN_SUBS : PLAYER_MAIN_SUBS;
                return filterStartingWith(pool, args[0]);
            }

            if (args.length == 2) {
                String sub = args[0].toLowerCase();
                if (sub.equals("join")) return filterStartingWith(TEAMS, args[1]);
                if (sub.equals("bid")) return filterStartingWith(COMMON_AMOUNTS, args[1]);
                if (sub.equals("stats") || sub.equals("estadisticas") || sub.equals("ping") || sub.equals("ms") || sub.equals("latencia")) {
                    return filterStartingWith(getOnlinePlayerNames(), args[1]);
                }

                if (!isAdmin) return new ArrayList<>();

                if (sub.equals("start")) return filterStartingWith(Arrays.asList("now"), args[1]);
                if (sub.equals("wand")) return filterStartingWith(WAND_SUBS, args[1]);
                if (sub.equals("edit") || sub.equals("editmode")) return filterStartingWith(Arrays.asList("on", "off"), args[1]);
                if (sub.equals("pingequalizer") || sub.equals("pe") || sub.equals("equalizer")) {
                    return filterStartingWith(Arrays.asList("on", "off", "auto"), args[1]);
                }
                if (sub.equals("time") || sub.equals("timer")) return filterStartingWith(TIME_SUBS, args[1]);
                if (sub.equals("parkour")) return filterStartingWith(PARKOUR_SUBS, args[1]);
                if (sub.equals("credits")) return filterStartingWith(CREDITS_SUBS, args[1]);
                if (sub.equals("auction")) return filterStartingWith(AUCTION_SUBS, args[1]);
                if (sub.equals("shop")) return filterStartingWith(Arrays.asList("on", "off"), args[1]);
                if (sub.equals("set")) return filterStartingWith(SET_SUBS, args[1]);
                if (sub.equals("config")) return filterStartingWith(CONFIG_SUBS, args[1]);
                if (sub.equals("event")) return filterStartingWith(EVENT_SUBS, args[1]);
                if (sub.equals("forcejoin") || sub.equals("revive")) {
                    return filterStartingWith(getOnlinePlayerNames(), args[1]);
                }
            }

            if (args.length == 3 && isAdmin) {
                String sub = args[0].toLowerCase();
                String sub2 = args[1].toLowerCase();

                if (sub.equals("pingequalizer") || sub.equals("pe") || sub.equals("equalizer")) {
                    return filterStartingWith(Arrays.asList("80", "100", "120", "140", "160"), args[2]);
                }
                if (sub.equals("forcejoin")) {
                    return filterStartingWith(TEAMS, args[2]);
                }
                if (sub.equals("time") || sub.equals("timer")) {
                    return filterStartingWith(COMMON_SECONDS, args[2]);
                }
                if (sub.equals("wand") && (sub2.equals("setspawn") || sub2.equals("setcage") || sub2.equals("setbase") || sub2.equals("chest"))) {
                    return filterStartingWith(TEAMS, args[2]);
                }
                if (sub.equals("credits")) {
                    return filterStartingWith(Arrays.asList("red", "blue"), args[2]);
                }
            }

            if (args.length == 4 && isAdmin) {
                String sub = args[0].toLowerCase();
                if (sub.equals("credits")) {
                    return filterStartingWith(Arrays.asList("10", "25", "50", "100"), args[3]);
                }
            }
        } else if (cmdName.equals("ttrset") && isAdmin) {
            if (args.length == 1) return filterStartingWith(SET_SUBS, args[0]);
        } else if (cmdName.equals("ttrconfig") && isAdmin) {
            if (args.length == 1) return filterStartingWith(CONFIG_SUBS, args[0]);
        } else if (cmdName.equals("ttrevent") && isAdmin) {
            if (args.length == 1) return filterStartingWith(EVENT_SUBS, args[0]);
        } else if (cmdName.equals("ttrforcejoin") && isAdmin) {
            if (args.length == 1) return filterStartingWith(getOnlinePlayerNames(), args[0]);
            if (args.length == 2) return filterStartingWith(TEAMS, args[1]);
        } else if (cmdName.equals("ttrrevive") && isAdmin) {
            if (args.length == 1) return filterStartingWith(getOnlinePlayerNames(), args[0]);
        }

        return new ArrayList<>();
    }

    private List<String> filterStartingWith(List<String> list, String prefix) {
        String lower = prefix.toLowerCase();
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(lower))
                .collect(Collectors.toList());
    }

    private List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }
}
