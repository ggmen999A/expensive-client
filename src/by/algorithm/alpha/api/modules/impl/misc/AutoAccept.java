package by.algorithm.alpha.api.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.api.command.friends.FriendStorage;
import by.algorithm.alpha.system.events.EventPacket;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import net.minecraft.network.play.server.SChatPacket;

import java.util.Locale;

@ModuleAnnot(name = "AutoAccept", type = ModuleCategory.Misc, description = "Автоматически принимает запросы на телепортацию от игроков/друзей")
public class AutoAccept extends Module {

    private final BooleanSetting onlyFriend = new BooleanSetting("Только друзья", false);

    public AutoAccept() {
        addSettings(onlyFriend);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player == null || mc.world == null) return;

        if (e.getPacket() instanceof SChatPacket p) {
            String raw = p.getChatComponent().getString().toLowerCase(Locale.ROOT);
            if (raw.contains("телепортироваться") || raw.contains("has requested teleport") || raw.contains("просит к вам телепортироваться")) {
                if (onlyFriend.get()) {
                    boolean yes = false;

                    for (String friend : FriendStorage.getFriends()) {
                        if (raw.contains(friend.toLowerCase(Locale.ROOT))) {
                            yes = true;
                            break;
                        }
                    }

                    if (!yes) return;
                }

                mc.player.sendChatMessage("/tpaccept");
                //print("accepted: " + raw);
            }
        }
    }
}
