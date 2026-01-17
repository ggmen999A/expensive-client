package by.algorithm.alpha.api.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventPacket;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.system.utils.math.StopWatch;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.Hand;

@ModuleAnnot(name = "AutoFish", type = ModuleCategory.Misc, description = "Рыбачит за вас")
public class AutoFish extends Module {

    private final StopWatch delay = new StopWatch();
    private boolean isHooked = false;
    private boolean needToHook = false;

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (delay.isReached(600) && isHooked) {
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            isHooked = false;
            needToHook = true;
            delay.reset();
        }

        if (delay.isReached(300) && needToHook) {
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            needToHook = false;
            delay.reset();
        }
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SPlaySoundEffectPacket p) {
            if (p.getSound().getName().getPath().equals("entity.fishing_bobber.splash")) {
                isHooked = true;
                delay.reset();
            }
        }
    }
}
