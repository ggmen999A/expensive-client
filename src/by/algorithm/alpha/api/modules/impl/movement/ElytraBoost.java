package by.algorithm.alpha.api.modules.impl.movement;

import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.Setting;
import by.algorithm.alpha.api.modules.settings.impl.BindSetting;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.system.events.EventKey;
import by.algorithm.alpha.system.events.EventUpdate;
import com.google.common.eventbus.Subscribe;

@ModuleAnnot(name = "ElytraBoost", type = ModuleCategory.Movement, description = "Ускоряет на элитрах")
public class ElytraBoost extends Module {
    public boolean isAntiTargetEnabled = false;
    public BindSetting antitarget = new BindSetting("AntiTarget", 0);
    boolean groundStart = false;
    public final SliderSetting speedtoTarget = new SliderSetting("Скорость при таргете", 1.65F, 1.0F, 2.0F, 0.01F);
    public final SliderSetting speedwithTarget = new SliderSetting("Скорость без таргете", 1.65F, 1.0F, 2.0F, 0.01F);
    public SliderSetting speedantitarget = new SliderSetting("Speed AT", 1.0F, 1.0F, 4.0F, 0.1F);

    public ElytraBoost() {
        this.addSettings(new Setting[]{this.speedtoTarget, this.speedwithTarget, this.antitarget, this.speedantitarget});
    }

    @Subscribe
    private void onKey(EventKey e) {
        if (e.getKey() == (Integer)this.antitarget.get()) {
            this.isAntiTargetEnabled = !this.isAntiTargetEnabled;
            this.print("AntiTarget " + (this.isAntiTargetEnabled ? "on" : "off"));
        }

    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (this.isAntiTargetEnabled && e instanceof EventUpdate) {
            mc.player.rotationPitch = -35.0F;
        }

    }

    public void onDisable() {
        super.onDisable();
        this.isAntiTargetEnabled = false;
    }
}
