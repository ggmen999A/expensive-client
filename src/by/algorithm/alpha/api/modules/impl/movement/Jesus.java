package by.algorithm.alpha.api.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.system.utils.player.MoveUtils;

@ModuleAnnot(name = "Jesus", type = ModuleCategory.Movement, description = "Ходьба по воде")
public class Jesus extends Module {

    @Subscribe
    private void onUpdate(EventUpdate update) {
        if (mc.player.isInWater()) {
            float moveSpeed = 10.0f;
            moveSpeed /= 100.0f;

            double moveX = mc.player.getForward().x * moveSpeed;
            double moveZ = mc.player.getForward().z * moveSpeed;
            mc.player.motion.y = 0f;
            if (MoveUtils.isMoving()) {
                if (MoveUtils.getMotion() < 0.9f) {
                    mc.player.motion.x *= 1.25f;
                    mc.player.motion.z *= 1.25f;
                }
            }
        }
    }
}