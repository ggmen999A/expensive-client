package by.algorithm.alpha.system.visuals.gui.components.settings;

import by.algorithm.alpha.system.utils.math.Vector4i;
import com.mojang.blaze3d.matrix.MatrixStack;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.system.visuals.gui.impl.Component;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;

/**
 * SliderComponent
 */
public class SliderComponent extends Component {

    private final SliderSetting setting;

    public SliderComponent(SliderSetting setting) {
        this.setting = setting;
        this.setHeight(18);
    }

    private float anim;
    private float displayValue;
    private boolean drag;
    private boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);

        displayValue = MathUtil.fast(displayValue, setting.get(), 15);

        Fonts.montserrat.drawText(stack, setting.getName(), getX() + 5, getY() + 4.5f / 2f + 1,
                ColorUtils.rgb(255, 255, 255), 5.5f, 0.05f);
        Fonts.sfMedium.drawText(stack, String.format("%.2f", displayValue),
                getX() + getWidth() - 5 - Fonts.sfbold.getWidth(String.format("%.2f", displayValue), 5.5f),
                getY() + 4.5f / 2f, ColorUtils.rgb(255, 255, 255), 5.5f);
        DisplayUtils.drawRoundedRect(getX() + 5, getY() + 11, getWidth() - 10, 4, 0.6f, ColorUtils.rgb(28, 28, 31));
        anim = MathUtil.fast(anim, (getWidth() - 10) * (setting.get() - setting.min) / (setting.max - setting.min), 20);
        float sliderWidth = anim;
        DisplayUtils.drawRoundedRect(getX() + 5, getY() + 11, sliderWidth, 4, new Vector4f(0.6f, 0.6f, 0.6f, 0.6f),
                new Vector4i(ColorUtils.rgb(176, 31, 21), ColorUtils.rgb(176, 31, 21), ColorUtils.rgb(166, 26, 17), ColorUtils.rgb(166, 26, 17)));
        DisplayUtils.drawCircle(getX() + 5 + sliderWidth, getY() + 12.7f, 6, ColorUtils.rgb(255, 255, 255));
        if (drag) {
            setting.set((float) MathHelper.clamp(MathUtil.round((mouseX - getX() - 5) / (getWidth() - 10) * (setting.max - setting.min) + setting.min, setting.increment), setting.min, setting.max));
        }
        if (isHovered(mouseX, mouseY)) {
            if (MathUtil.isHovered(mouseX, mouseY, getX() + 5, getY() + 10, getWidth() - 10, 2)) {
                if (!hovered) {
                    hovered = true;
                }
            } else {
                if (hovered) {
                    hovered = false;
                }
            }
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        if (MathUtil.isHovered(mouseX, mouseY, getX() + 5, getY() + 11, getWidth() - 10, 4)) {
            drag = true;
        }
        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        drag = false;
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}