package by.algorithm.alpha.api.modules.impl.movement;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;

@ModuleAnnot(name = "Timer", type = ModuleCategory.Movement, description = "Ускорение игрового времени")
public class Timer extends Module {

    private final SliderSetting speed = new SliderSetting("Скорость", 2f, 0.1f, 10f, 0.1f);

    public Timer() {
        addSettings(speed);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        mc.timer.timerSpeed = speed.get();
    }

    private void reset() {
        mc.timer.timerSpeed = 1;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
    }
}
