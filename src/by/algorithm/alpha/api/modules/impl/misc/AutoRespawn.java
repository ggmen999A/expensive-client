package by.algorithm.alpha.api.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import net.minecraft.client.gui.screen.DeathScreen;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;

@ModuleAnnot(name = "AutoRespawn", type = ModuleCategory.Misc, description = "Возрождается за вас при смерти")
public class AutoRespawn extends Module {

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        if (mc.currentScreen instanceof DeathScreen && mc.player.deathTime > 3) {
            mc.player.respawnPlayer();
            mc.displayGuiScreen(null);
        }
    }
}
