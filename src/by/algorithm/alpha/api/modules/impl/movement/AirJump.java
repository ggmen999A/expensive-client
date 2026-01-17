package by.algorithm.alpha.api.modules.impl.movement;

import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.system.events.EventMotion;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.DeadBushBlock;

@ModuleAnnot(name = "AirJump", type = ModuleCategory.Movement, description = "Прыгаем в воздухе")
public class AirJump extends Module {

    private ModeSetting mode = new ModeSetting("Обход", "Matrix", "Default", "Matrix");

    public AirJump() {
        addSettings(mode);
    }

    @Subscribe
    public void onUpdate(EventMotion e) {
        if (mode.is("Default")) {
            mc.player.onGround = true;
        }

        if (mode.is("Matrix")) {
            if (!mc.world.getCollisionShapes(mc.player, mc.player.getBoundingBox().expand(0.5, 0, 0.5).offset(0, -1, 0)).toList().isEmpty() && mc.player.ticksExisted % 2 == 0) {
                mc.player.fallDistance = 0;
                mc.player.jumpTicks = 0;
                e.setOnGround(true);
                mc.player.onGround = true;
            }
        }
    }
}
