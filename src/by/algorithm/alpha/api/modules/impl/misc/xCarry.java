package by.algorithm.alpha.api.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventPacket;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import net.minecraft.network.play.client.CCloseWindowPacket;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;

@ModuleAnnot(name = "xCarry", type = ModuleCategory.Misc, description = "Перемещение предметов в верстаке в инвентаре")
public class xCarry extends Module {

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player == null) return;

        if (e.getPacket() instanceof CCloseWindowPacket) {
            e.cancel();
        }
    }
}
