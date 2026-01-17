package by.algorithm.alpha.api.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventPacket;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;

@ModuleAnnot(name = "ItemSwapFix", type = ModuleCategory.Misc, description = "Исправление свапа предметов")
public class ItemSwapFix extends Module {

    @Subscribe
    private void onPacket(EventPacket e) {
        if (mc.player == null) return;
        if (e.getPacket() instanceof SHeldItemChangePacket wrapper) {
            final int serverSlot = wrapper.getHeldItemHotbarIndex();
            if (serverSlot != mc.player.inventory.currentItem) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(Math.max(mc.player.inventory.currentItem - 1, 0)));
                mc.player.connection.sendPacket(new CHeldItemChangePacket(mc.player.inventory.currentItem));
                e.cancel();
            }
        }
    }
}
