package by.algorithm.alpha.api.modules.impl.render;

import by.algorithm.alpha.api.modules.impl.combat.AttackAura;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import com.mojang.blaze3d.matrix.MatrixStack;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import net.minecraft.util.math.vector.Vector3f;

@ModuleAnnot(name = "SwingAnimation", type = ModuleCategory.Render, description = "Анимации руки")
public class SwingAnimation extends Module {

    public ModeSetting animationMode = new ModeSetting("Мод", "Диагональный удар", "Диагональный удар", "Плавная волна", "Вертикальный слэш", "Ванилла");
    public SliderSetting swingPower = new SliderSetting("Сила", 5.0f, 1.0f, 10.0f, 0.05f);
    public SliderSetting swingSpeed = new SliderSetting("Скорость", 10.0f, 3.0f, 10.0f, 1.0f);
    public SliderSetting scale = new SliderSetting("Размер", 1.0f, 0.5f, 1.5f, 0.05f);
    public final BooleanSetting onlyAura = new BooleanSetting("Только с AttackAura", false);
    public AttackAura killAura;

    public SwingAnimation(AttackAura killAura) {
        this.killAura = killAura;
        addSettings(animationMode, swingPower, swingSpeed, scale, onlyAura);
    }

    public void animationProcess(MatrixStack stack, float swingProgress, Runnable runnable) {
        float anim = (float) Math.sin(swingProgress * (Math.PI / 2) * 2);

        if (onlyAura.get() && killAura.getTarget() == null) {
            runnable.run();
            return;
        }

        switch (animationMode.getIndex()) {
            case 0:
                stack.scale(scale.get(), scale.get(), scale.get());
                stack.translate(0.4f, 0.1f, -0.5);
                stack.rotate(Vector3f.YP.rotationDegrees(90));
                stack.rotate(Vector3f.ZP.rotationDegrees(-60));
                stack.rotate(Vector3f.XP.rotationDegrees(-90
                        - (swingPower.get() * 10) * anim));
                break;
            case 1:
                stack.scale(scale.get(), scale.get(), scale.get());
                stack.translate(0.0, 0, -0.5f);
                stack.rotate(Vector3f.YP.rotationDegrees(15 * anim));

                stack.rotate(Vector3f.ZP.rotationDegrees(-60 * anim));
                stack.rotate(Vector3f.XP.rotationDegrees((-90 - (swingPower.get())) * anim));
                break;
            case 2:
                stack.scale(scale.get(), scale.get(), scale.get());
                stack.translate(0.4f, 0, -0.5f);
                stack.rotate(Vector3f.YP.rotationDegrees(90));
                stack.rotate(Vector3f.ZP.rotationDegrees(-30));
                stack.rotate(Vector3f.XP.rotationDegrees(-90
                        - (swingPower.get() * 10) * anim));
                break;
            default:
                stack.scale(scale.get(), scale.get(), scale.get());
                runnable.run();
                break;
        }
    }

}
