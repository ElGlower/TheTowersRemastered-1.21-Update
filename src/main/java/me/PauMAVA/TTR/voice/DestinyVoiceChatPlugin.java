package me.PauMAVA.TTR.voice;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;

public class DestinyVoiceChatPlugin implements VoicechatPlugin {

    private final SimpleVoiceChatBridge bridge;

    public DestinyVoiceChatPlugin(SimpleVoiceChatBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public String getPluginId() {
        return "destinytowers";
    }

    @Override
    public void initialize(VoicechatApi api) {
        if (api instanceof VoicechatServerApi serverApi) {
            bridge.setServerApi(serverApi);
        }
    }
}
