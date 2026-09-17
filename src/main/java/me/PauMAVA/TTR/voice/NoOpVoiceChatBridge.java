package me.PauMAVA.TTR.voice;

import org.bukkit.entity.Player;

public class NoOpVoiceChatBridge implements VoiceChatBridge {

    @Override
    public void init() {}

    @Override
    public void assignPlayerToTeamVoice(Player player, String teamName) {}

    @Override
    public void clearAllVoiceGroups() {}

    @Override
    public boolean isHooked() {
        return false;
    }
}
