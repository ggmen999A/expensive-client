package by.algorithm.alpha.api.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import net.minecraft.entity.player.PlayerEntity;

@ModuleAnnot(name = "SeeInvisibles", type = ModuleCategory.Render, description = "Возможность видеть невидимых игроков")
public class SeeInvisibles extends Module {


    @Subscribe
    private void onUpdate(EventUpdate e) {
        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player && player.isInvisible()) {
                player.setInvisible(false);
            }
        }
    }
}
