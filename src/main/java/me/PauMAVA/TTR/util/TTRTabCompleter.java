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

    private static final List<String> MAIN_SUBS = Arrays.asList(
            "start", "stop", "resetmap", "set", "config", "event", "forcejoin", "revive", "spectate", "play", "reload"
    );

    private static final List<String> SET_SUBS = Arrays.asList(
            "lobby", "redspawn", "bluespawn", "redcage", "bluecage", "iron", "coal", "emerald", "xp"
    );

    private static final List<String> CONFIG_SUBS = Arrays.asList(
            "time", "points", "duration", "maxpoints"
    );

    private static final List<String> EVENT_SUBS = Arrays.asList(
            "jump", "speed", "blind", "giga", "mini", "meteors", "auto", "stop"
    );

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        String cmdName = command.getName().toLowerCase();

        if (cmdName.equals("ttr") || cmdName.equals("thetowers") || cmdName.equals("towers")) {
            if (args.length == 1) {
                return filterStartingWith(MAIN_SUBS, args[0]);
            }
            if (args.length == 2) {
                String sub = args[0].toLowerCase();
                if (sub.equals("set")) return filterStartingWith(SET_SUBS, args[1]);
                if (sub.equals("config")) return filterStartingWith(CONFIG_SUBS, args[1]);
                if (sub.equals("event")) return filterStartingWith(EVENT_SUBS, args[1]);
                if (sub.equals("forcejoin") || sub.equals("revive")) {
                    return filterStartingWith(getOnlinePlayerNames(), args[1]);
                }
            }
            if (args.length == 3 && args[0].equalsIgnoreCase("forcejoin")) {
                Set<String> teams = TTRCore.getInstance().getConfigManager().getTeamNames();
                if (teams != null) return filterStartingWith(new ArrayList<>(teams), args[2]);
            }
        } else if (cmdName.equals("ttrset")) {
            if (args.length == 1) return filterStartingWith(SET_SUBS, args[0]);
        } else if (cmdName.equals("ttrconfig")) {
            if (args.length == 1) return filterStartingWith(CONFIG_SUBS, args[0]);
        } else if (cmdName.equals("ttrevent")) {
            if (args.length == 1) return filterStartingWith(EVENT_SUBS, args[0]);
        } else if (cmdName.equals("ttrforcejoin")) {
            if (args.length == 1) return filterStartingWith(getOnlinePlayerNames(), args[0]);
            if (args.length == 2) {
                Set<String> teams = TTRCore.getInstance().getConfigManager().getTeamNames();
                if (teams != null) return filterStartingWith(new ArrayList<>(teams), args[1]);
            }
        } else if (cmdName.equals("ttrrevive")) {
            if (args.length == 1) return filterStartingWith(getOnlinePlayerNames(), args[0]);
        }

        return completions;
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
