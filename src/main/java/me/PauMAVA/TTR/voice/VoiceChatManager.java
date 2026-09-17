package me.PauMAVA.TTR.voice;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import me.PauMAVA.TTR.TTRCore;
import me.PauMAVA.TTR.util.TextUtil;
import me.PauMAVA.TTR.util.TTRPrefix;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VoiceChatManager {

    private static final VoiceChatManager INSTANCE = new VoiceChatManager();
    private VoicechatServerApi serverApi;
    private final Map<String, Group> teamGroups = new ConcurrentHashMap<>();
    private boolean hooked = false;

    private VoiceChatManager() {}

    public static VoiceChatManager getInstance() {
        return INSTANCE;
    }

    public void init() {
        if (!Bukkit.getPluginManager().isPluginEnabled("voicechat")) {
            return;
        }

        try {
            BukkitVoicechatService service = Bukkit.getServicesManager().load(BukkitVoicechatService.class);
            if (service != null) {
                service.registerPlugin(new DestinyVoiceChatPlugin());
                this.hooked = true;
                Bukkit.getConsoleSender().sendMessage(TTRPrefix.TTR_SUCCESS +
                        TextUtil.toTiny("Simple Voice Chat API vinculada y sincronizada."));
            }
        } catch (Throwable t) {
            Bukkit.getConsoleSender().sendMessage(TTRPrefix.TTR_ERROR +
                    TextUtil.toTiny("Error al registrar integración de Voice Chat: ") + t.getMessage());
        }
    }

    public void setServerApi(VoicechatServerApi api) {
        this.serverApi = api;
    }

    public boolean isHooked() {
        return hooked && serverApi != null;
    }

    public void assignPlayerToTeamVoice(Player player, String teamName) {
        if (!isHooked() || player == null) return;
        if (!TTRCore.getInstance().getConfig().getBoolean("voicechat.enabled", true)) return;

        try {
            VoicechatConnection conn = serverApi.getConnectionOf(player.getUniqueId());
            if (conn == null) return;

            if (teamName == null) {
                conn.setGroup(null);
                return;
            }

            boolean isolated = TTRCore.getInstance().getConfig().getBoolean("voicechat.isolated", true);
            Group group = teamGroups.computeIfAbsent(teamName.toLowerCase(), name -> {
                return serverApi.groupBuilder()
                        .setName("Equipo " + teamName)
                        .setType(isolated ? Group.Type.ISOLATED : Group.Type.NORMAL)
                        .setPersistent(false)
                        .build();
            });

            conn.setGroup(group);
        } catch (Throwable ignored) {}
    }

    public void clearAllVoiceGroups() {
        if (!isHooked()) return;
        try {
            for (Player p : Bukkit.getOnlinePlayers()) {
                VoicechatConnection conn = serverApi.getConnectionOf(p.getUniqueId());
                if (conn != null) {
                    conn.setGroup(null);
                }
            }
            teamGroups.clear();
        } catch (Throwable ignored) {}
    }
}
