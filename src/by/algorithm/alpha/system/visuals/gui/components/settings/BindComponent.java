package by.algorithm.alpha.system.visuals.gui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import by.algorithm.alpha.api.modules.settings.impl.BindSetting;
import by.algorithm.alpha.system.visuals.gui.impl.Component;
import by.algorithm.alpha.system.utils.client.KeyStorage;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.Cursors;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class BindComponent extends Component {

    final BindSetting setting;

    public BindComponent(BindSetting setting) {
        this.setting = setting;
        this.setHeight(16);
    }

    boolean activated;
    boolean hovered = false;
    private static BindComponent currentlyActivated = null;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        Fonts.montserrat.drawText(stack, setting.getName(), getX() + 5, getY() + 6.5f / 2f + 1, ColorUtils.rgb(255, 255, 255), 5.5f, 0.05f);
        String bind = KeyStorage.getKey(setting.get());
        if (bind == null || setting.get() == -1) {
            bind = "Нету";
        }

        boolean next = Fonts.montserrat.getWidth(bind, 5.5f, activated ? 0.1f : 0.05f) >= 16;
        float x = next ? getX() + 5 : getX() + getWidth() - 7 - Fonts.montserrat.getWidth(bind, 5.5f, activated ? 0.1f : 0.05f);
        float y = getY() + 5.5f / 2f + (5.5f / 2f) + (next ? 8 : 0);

        if (!bind.equals("Нету")) {
            DisplayUtils.drawRoundedRect(x - 2 + 0.5F, y - 2, Fonts.montserrat.getWidth(bind, 5.5f, activated ? 0.1f : 0.05f) + 4, 5.5f + 4, 3, ColorUtils.setAlpha(ColorUtils.rgb(176, 31, 21), 220));
        } else {
            DisplayUtils.drawRoundedRectOutline(
                    x - 2 + 0.5F,
                    y - 2,
                    Fonts.montserrat.getWidth(bind, 5.5f, activated ? 0.1f : 0.05f) + 4,
                    5.5f + 4,
                    3,
                    1,
                    ColorUtils.setAlpha(ColorUtils.rgb(155, 155, 155), 55));
        }

        Fonts.sfbold.drawText(stack, bind, x, y, activated ? -1 : ColorUtils.rgb(160, 163, 175), 5.5f, activated ? 0.1f : 0.05f);

        if (isHovered(mouseX, mouseY)) {
            if (MathUtil.isHovered(mouseX, mouseY, x - 2 + 0.5F, y - 2, Fonts.montserrat.getWidth(bind, 5.5f, activated ? 0.1f : 0.05f) + 4, 5.5f + 4)) {
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
        setHeight(next ? 20 : 16);
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        if (activated) {
            if (key == GLFW.GLFW_KEY_DELETE) {
                setting.set(-1);
                activated = false;
                currentlyActivated = null;
                return;
            }
            setting.set(key);
            activated = false;
            currentlyActivated = null;
        }
        super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        if (isHovered(mouseX, mouseY) && mouse == 0) {
            if (!activated) {
                if (currentlyActivated != null && currentlyActivated != this) {
                    currentlyActivated.activated = false;
                }
                activated = true;
                currentlyActivated = this;
            } else {
                activated = false;
                currentlyActivated = null;
            }
        }

        if (activated && mouse >= 1) {
            System.out.println(-100 + mouse);
            setting.set(-100 + mouse);
            activated = false;
            currentlyActivated = null;
        }

        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}