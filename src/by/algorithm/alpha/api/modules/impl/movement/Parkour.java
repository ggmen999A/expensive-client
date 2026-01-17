package by.algorithm.alpha.api.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.system.utils.player.MoveUtils;

@ModuleAnnot(name = "Parkour", type = ModuleCategory.Movement, description = "Автопрыжок на краю блока")
public class Parkour extends Module {

    @Subscribe
    private void onUpdate(EventUpdate e) {

        if (MoveUtils.isBlockUnder(0.001f) && mc.player.isOnGround()) {
            mc.player.jump();
        }
    }

}
