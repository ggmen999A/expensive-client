package by.algorithm.alpha.api.modules.impl.player;

import com.google.common.eventbus.Subscribe;
import net.minecraft.network.play.client.CPlayerPacket;
import by.algorithm.alpha.system.events.EventPacket;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;

@ModuleAnnot(name = "NoRotate", type = ModuleCategory.Player, description = "Отключение ротаций со стороны сервера")
public class NoRotate extends Module {
    private float targetYaw;
    private float targetPitch;
    private boolean isPacketSent;

    @Subscribe
    public void onPacket(EventPacket event) {
        if (event.isSend()) {
            if (this.isPacketSent) {
                if (event.getPacket() instanceof CPlayerPacket playerPacket) {
                    playerPacket.setRotation(targetYaw, targetPitch);
                    this.isPacketSent = false;
                }
            }
        }
    }

    public void sendRotationPacket(float yaw, float pitch) {
        this.targetYaw = yaw;
        this.targetPitch = pitch;
        this.isPacketSent = true;
    }
}
