package by.algorithm.alpha.system.visuals.gui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.system.visuals.gui.impl.Component;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.Cursors;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

/**
 * BooleanComponent
 */
public class BooleanComponent extends Component {

    private final BooleanSetting setting;
    private static final ResourceLocation CHECK_IMAGE = new ResourceLocation("expensive/images/check.png");

    public BooleanComponent(BooleanSetting setting) {
        this.setting = setting;
        setHeight(16);
        animation = new Animation().animate(setting.get() ? 1 : 0, 0.2, Easings.CIRC_OUT);
        appearanceAnimation = new Animation().animate(0, 0.3, Easings.CIRC_OUT); 
    }

    private Animation animation = new Animation();
    private Animation appearanceAnimation;
    private float width, height;
    private boolean hovered = false;
    private boolean hasAppeared = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        animation.update();
        
        if (!hasAppeared) {
            appearanceAnimation.update();
            if (appearanceAnimation.getValue() < 1) {
                appearanceAnimation.animate(1, 0.3, Easings.CIRC_OUT);
            } else {
                hasAppeared = true;
            }
        }

        float appearanceAlpha = hasAppeared ? 1 : (float) appearanceAnimation.getValue();

        Fonts.montserrat.drawText(stack, setting.getName(), getX() + 5, getY() + 6.5f / 2f + 1, 
            ColorUtils.setAlpha(ColorUtils.rgb(160, 163, 175), (int)(255 * appearanceAlpha)), 6.5f, 0.05f);

        width = 11;
        height = 11;
        float boxX = getX() + getWidth() - width - 7;
        float boxY = getY() + getHeight() / 2f - height / 2f;

        if (setting.get()) {
            float checkAlpha = (float) animation.getValue();
            DisplayUtils.drawRoundedRect(boxX, boxY, width, height, 3f, 
                ColorUtils.setAlpha(ColorUtils.rgb(166, 26, 17), (int)(255 * appearanceAlpha)));
            Minecraft.getInstance().getTextureManager().bindTexture(CHECK_IMAGE);
            DisplayUtils.drawImage(CHECK_IMAGE, boxX + 2, boxY + 3, 7, 7, 
                ColorUtils.setAlpha(ColorUtils.rgb(255, 255, 255), (int)(255 * appearanceAlpha * checkAlpha)));
        } else {
            DisplayUtils.drawRoundedRectOutline(
                boxX, boxY, width, height, 3, 1, 
                ColorUtils.setAlpha(ColorUtils.rgb(155, 155, 155), (int)(55 * appearanceAlpha)));
        }

        if (isHovered(mouseX, mouseY)) {
            if (MathUtil.isHovered(mouseX, mouseY, boxX, boxY, width, height)) {
                if (!hovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
                    hovered = true;
                }
            } else {
                if (hovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
                    hovered = false;
                }
            }
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        float boxX = getX() + getWidth() - width - 7;
        float boxY = getY() + getHeight() / 2f - height / 2f;
        if (MathUtil.isHovered(mouseX, mouseY, boxX, boxY, width, height)) {
            setting.set(!setting.get());
            animation = new Animation().animate(setting.get() ? 1 : 0, 0.2, Easings.CIRC_OUT);
        }
        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}