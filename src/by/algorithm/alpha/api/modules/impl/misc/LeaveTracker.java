package by.algorithm.alpha.api.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventEntityLeave;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;

@ModuleAnnot(name = "LeaveTracker", type = ModuleCategory.Misc, description = "Логирование о ливах игроков")
public class LeaveTracker extends Module {


    @Subscribe
    private void onEntityLeave(EventEntityLeave eel) {
        Entity entity = eel.getEntity();

        if (!isEntityValid(entity)) {
            return;
        }

        String message = "Игрок "
                + entity.getDisplayName().getString()
                + " ливнул на "
                + entity.getStringPosition();

        print(message);
    }

    private boolean isEntityValid(Entity entity) {
        if (!(entity instanceof AbstractClientPlayerEntity) || entity instanceof ClientPlayerEntity) {
            return false;
        }

        return !(mc.player.getDistance(entity) < 100);
    }
}
