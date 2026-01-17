package by.algorithm.alpha.api.modules.impl.movement;


import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.impl.combat.AttackAura;
import by.algorithm.alpha.api.modules.settings.Setting;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.system.events.MovingEvent;
import com.google.common.eventbus.Subscribe;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleAnnot(name = "ElytraMotion", description = "Дополнение к элитра таргету", type = ModuleCategory.Movement)
public class ElytraMotion extends Module {

    public final SliderSetting attackDistance = new SliderSetting("Дистанция", 3.0F, 0.1F, 5.0F, 0.01F);
    private final BooleanSetting auto = new BooleanSetting("Авто Фейр", false);
    public boolean freeze;

    public ElytraMotion() {
        this.addSettings(attackDistance, auto);
    }

    @Subscribe
    public void update(EventUpdate eventUpdate) {
        if (!Minecraft.player.isElytraFlying()) {
            this.freeze = false;
        } else {
            AttackAura killAura = Initclass.getInstance().getFunctionRegistry().getKillAura();
            if (this.check(killAura)) {
                mc.gameSettings.keyBindForward.setPressed(false);
                this.freeze = true;
            } else {
                mc.gameSettings.keyBindForward.setPressed(true);
                this.freeze = false;
            }
        }
    }

    @Subscribe
    private void onMotion(MovingEvent eventMotion) {
        if (this.freeze) {
            eventMotion.getMotion().x = 0.0D;
            eventMotion.getMotion().y = 0.0D;
            eventMotion.getMotion().z = 0.0D;
        }
    }

    public boolean check(AttackAura killAura) {
        LivingEntity target = AttackAura.getTarget();
        if (target == null) {
            return false;
        } else {
            return target.getDistance(Minecraft.player) < attackDistance.get() &&
                    Minecraft.player.isElytraFlying();
        }
    }

    @Override
    public void onDisable() {
        this.freeze = false;
        super.onDisable();
    }
}