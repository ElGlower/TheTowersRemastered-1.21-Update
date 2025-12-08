package me.PauMAVA.TTR.util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TTRTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("ttrset")) {
            if (args.length == 1) {
                completions.addAll(Arrays.asList("lobby", "redspawn", "bluespawn", "iron", "xp", "coal", "emerald"));
            }
        }

        if (command.getName().equalsIgnoreCase("ttrevent")) {
            if (args.length == 1) {
                completions.addAll(Arrays.asList("jump", "speed", "blind", "slow", "size", "meteors", "fall", "auto"));
            }
        }

        return completions;
    }
}