package by.algorithm.alpha.api.modules.impl.player;

import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.system.events.EventUpdate;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.play.client.CPlayerPacket;

@ModuleAnnot(name = "KTLeave", type = ModuleCategory.Player, description = "Выход в KT для FunSky")
public class KTLeave extends Module {

    public KTLeave() {
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (e instanceof EventUpdate) {
            mc.player.connection.sendPacket(new CPlayerPacket.RotationPacket(Float.NaN, Float.NaN, true));
            toggle();
        }

    }
}
