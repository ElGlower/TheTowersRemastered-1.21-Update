package me.PauMAVA.TTR.voice;

import org.bukkit.entity.Player;

public interface VoiceChatBridge {
    void init();
    void assignPlayerToTeamVoice(Player player, String teamName);
    void clearAllVoiceGroups();
    boolean isHooked();
}
