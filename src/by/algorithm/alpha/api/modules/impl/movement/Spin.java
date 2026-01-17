package by.algorithm.alpha.api.modules.impl.movement;

import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.system.events.EventMotion;
import com.google.common.eventbus.Subscribe;

@ModuleAnnot(name = "Spin", type = ModuleCategory.Movement)
public class Spin extends Module {

    private final SliderSetting value = new SliderSetting("Сила", 50.0f, 1.0f, 100.0f, 1.0f);
    private float rotationYaw = 0;
    private long lastRotationTime = 0;

    public Spin() {
        addSettings(value);
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Subscribe
    private void onWalking(EventMotion e) {
        if (System.currentTimeMillis() - lastRotationTime >= 0) {
            rotationYaw -= value.get();
            if (mc.player != null) {
                mc.player.rotationYawHead = rotationYaw;
                mc.player.renderYawOffset = rotationYaw;
                e.setYaw(rotationYaw);
            }
            lastRotationTime = System.currentTimeMillis();
        }
    }
}