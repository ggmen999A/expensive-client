package by.algorithm.alpha.api.modules.impl.player;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import net.minecraft.util.Hand;
import java.lang.reflect.Field;

@ModuleAnnot(name = "NoDelay", type = ModuleCategory.Player, description = "Отключение задержек")
public class NoDelay extends Module {

    public final BooleanSetting blockPlacement = new BooleanSetting("Установка блоков", true);
    public final BooleanSetting jumping = new BooleanSetting("Прыжки", true);
    public final BooleanSetting experienceBottle = new BooleanSetting("Пузырек опыта", true);

    public NoDelay() {
        addSettings(blockPlacement, jumping, experienceBottle);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (blockPlacement.get()) {
            try {
                Field rightClickDelayField = mc.getClass().getDeclaredField("rightClickDelayTimer");
                rightClickDelayField.setAccessible(true);
                rightClickDelayField.set(mc, 0);
            } catch (Exception ex) {
                // Fallback if reflection fails
            }
        }

        if (jumping.get()) {
            mc.player.jumpTicks = 0;
        }

        if (experienceBottle.get()) {
            if (mc.player.getHeldItem(Hand.MAIN_HAND).getItem().toString().contains("experience_bottle")) {
                mc.player.getCooldownTracker().removeCooldown(mc.player.getHeldItem(Hand.MAIN_HAND).getItem());
            }
        }
    }
}