package by.algorithm.alpha.system.visuals.gui.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.ModeListSetting;
import by.algorithm.alpha.system.visuals.gui.impl.Component;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.Cursors;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class MultiBoxComponent extends Component {

    final ModeListSetting setting;
    float spacing = 1f;
    private boolean hovered = false;

    public MultiBoxComponent(ModeListSetting setting) {
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

        for (BooleanSetting text : setting.get()) {
            float textWidth = Fonts.sfMedium.getWidth(text.getName(), 5.5f, 0.05f);
            float textHeight = Fonts.sfMedium.getHeight(5.5f) + 1;
            float circleX = getX() + 12;
            float circleRadius = 10f;

            float circleY = getY() + 14 + heightoff;
            float centerY = circleY + textHeight / 2;

            if (MathUtil.isHovered(mouseX, mouseY,
                    circleX - circleRadius, centerY - circleRadius,
                    circleRadius * 2, circleRadius * 2)) {
                anyHovered = true;
            }

            DisplayUtils.drawCircle(circleX, centerY, circleRadius,
                    ColorUtils.setAlpha(
                            text.get() ? ColorUtils.rgb(166, 26, 17) : ColorUtils.rgb(255, 255, 255),
                            text.get() ? 220 : 100
                    )
            );

            DisplayUtils.drawCircle(circleX, centerY, circleRadius - 4, ColorUtils.rgb(30, 30, 30));

            Fonts.sfbold.drawText(stack, text.getName(), circleX + 6.3F,
                    circleY + (textHeight / 2) - 2, ColorUtils.rgb(255, 255, 255), 5.2f, 0.05f); // Adjusted font size

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
    for (BooleanSetting text : setting.get()) {
        float textHeight = Fonts.sfbold.getHeight(5.5f) + 1;
        float circleX = getX() + 12;
        float circleRadius = 10f;
        float circleY = getY() + 15 + heightoff;
        float centerY = circleY + textHeight / 2;

        float dx = mouseX - circleX;
        float dy = mouseY - centerY;
        if (dx * dx + dy * dy <= circleRadius * circleRadius) {
            text.set(!text.get());
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
