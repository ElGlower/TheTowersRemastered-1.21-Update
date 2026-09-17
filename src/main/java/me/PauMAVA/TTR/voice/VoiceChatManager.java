package me.PauMAVA.TTR.voice;

import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VoiceChatManager {

    private static final VoiceChatManager INSTANCE = new VoiceChatManager();
    private VoiceChatBridge bridge = new NoOpVoiceChatBridge();

    private VoiceChatManager() {}

    public static VoiceChatManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        if (!Bukkit.getPluginManager().isPluginEnabled("voicechat")) {
            this.bridge = new NoOpVoiceChatBridge();
            return;
        }

        try {
            Class.forName("de.maxhenkel.voicechat.api.BukkitVoicechatService");
            this.bridge = (VoiceChatBridge) Class.forName("me.PauMAVA.TTR.voice.SimpleVoiceChatBridge")
                    .getDeclaredConstructor()
                    .newInstance();
            this.bridge.init();
        } catch (Throwable t) {
            Bukkit.getConsoleSender().sendMessage(TTRPrefix.TTR_ERROR +
                    TextUtil.toTiny("Simple Voice Chat no disponible o error al vincular: ") + t.getMessage());
            this.bridge = new NoOpVoiceChatBridge();
        }
    }

    public boolean isHooked() {
        return bridge != null && bridge.isHooked();
    }

    public void assignPlayerToTeamVoice(Player player, String teamName) {
        if (bridge != null) {
            bridge.assignPlayerToTeamVoice(player, teamName);
        }
    }

    public void clearAllVoiceGroups() {
        if (bridge != null) {
            bridge.clearAllVoiceGroups();
        }
    }
}
