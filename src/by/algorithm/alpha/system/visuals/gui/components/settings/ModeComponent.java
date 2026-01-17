package by.algorithm.alpha.system.visuals.gui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.system.visuals.gui.impl.Component;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.Cursors;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class ModeComponent extends Component {

    final ModeSetting setting;
    private boolean hovered = false;

    public ModeComponent(ModeSetting setting) {
        this.setting = setting;
        setHeight(22);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        Fonts.montserrat.drawText(stack, setting.getName(), getX() + 7, getY() + 2,
                ColorUtils.rgb(255, 255, 255), 5.8f, 0.05f);

        float heightoff = 0;
        float maxWidth = 0;
        boolean anyHovered = false;

        for (String text : setting.strings) {
            float textWidth = Fonts.sfbold.getWidth(text, 5.5f, 0.05f);
            float textHeight = Fonts.sfbold.getHeight(5.5f) + 1;
            float circleX = getX() + 12;
            float circleRadius = 10f;

            float circleY = getY() + 14 + heightoff;
            float centerY = circleY + textHeight / 2;

            if (MathUtil.isHovered(mouseX, mouseY,
                    circleX - circleRadius, centerY - circleRadius,
                    circleRadius * 2, circleRadius * 2)) {
                anyHovered = true;
            }

            boolean selected = text.equals(setting.get());

            DisplayUtils.drawCircle(circleX, centerY, circleRadius,
                    ColorUtils.setAlpha(
                            selected ? ColorUtils.rgb(166, 26, 17) : ColorUtils.rgb(255, 255, 255),
                            selected ? 220 : 100
                    )
            );

            DisplayUtils.drawCircle(circleX, centerY, circleRadius - 4, ColorUtils.rgb(30, 30, 30));

            Fonts.sfbold.drawText(stack, text, circleX + 6.3F,
                    circleY + (textHeight / 2) - 2, ColorUtils.rgb(255, 255, 255), 5.8f);

            heightoff += circleRadius * 2 - 7;
            maxWidth = Math.max(maxWidth, textWidth + circleRadius + 20);
        }

        setHeight(22 + heightoff - 5);
        setWidth(maxWidth + 15);

        if (anyHovered != hovered) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(),
                    anyHovered ? Cursors.HAND : Cursors.ARROW);
            hovered = anyHovered;
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        if (mouse != 0) return;

        float heightoff = 0;
        for (String text : setting.strings) {
            float textHeight = Fonts.sfbold.getHeight(5.5f) + 1;
            float circleX = getX() + 12;
            float circleRadius = 10f;
            float circleY = getY() + 15 + heightoff;
            float centerY = circleY + textHeight / 2;

            float dx = mouseX - circleX;
            float dy = mouseY - centerY;
            if (dx * dx + dy * dy <= circleRadius * circleRadius) {
                setting.set(text);
                break;
            }

            heightoff += circleRadius * 2 - 7;
        }

        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.visible.get();
    }
}