package by.algorithm.alpha.api.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;

@ModuleAnnot(name = "AntiAFK", type = ModuleCategory.Player, description = "Не дает серверу кикнуть вас через /shop")
public class AntiAFK extends Module {

    private int tickCounter = 0;
    private boolean shopSent = false;

    @Subscribe
    private void onUpdate(EventUpdate e) {
        tickCounter++;

        if (tickCounter % 80 == 0) {
            if (!shopSent) {
                mc.player.sendChatMessage("/menu");
                shopSent = true;
            } else {
                mc.player.closeScreen();
                shopSent = false;
            }
        }
    }
}