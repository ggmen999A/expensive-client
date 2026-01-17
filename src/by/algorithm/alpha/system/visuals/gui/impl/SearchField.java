package by.algorithm.alpha.system.visuals.gui.impl;

import by.algorithm.alpha.system.utils.client.ClientUtil;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.KawaseBlur;
import by.algorithm.alpha.system.utils.render.Scissor;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

@Setter
@Getter
public class SearchField {

    private int x, y, width, height;
    private String text;
    private boolean isFocused;
    private boolean typing;
    private final String placeholder;

    public SearchField(int x, int y, int width, int height, String placeholder) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.placeholder = placeholder;
        this.text = "";
        this.isFocused = false;
        this.typing = false;
    }

    public void updatePosition() {
        this.width = 120;
        this.height = 20;

        int screenW = Minecraft.getInstance().getMainWindow().getScaledWidth();
        int screenH = Minecraft.getInstance().getMainWindow().getScaledHeight();

        // Центр GUI по X
        this.x = (screenW - width) / 2;

        // Ниже категорий
        this.y = screenH - height - 90;
    }


    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        updatePosition();

        String textToDraw = text.isEmpty() && !typing ? placeholder : text;
        String cursor = typing && System.currentTimeMillis() % 1000 > 500 ? "_" : "";

        KawaseBlur.blur.updateBlur(3, 3);
        KawaseBlur.blur.render(() ->
                DisplayUtils.drawRoundedRect(
                        x, y, width, height, 4,
                        ColorUtils.rgba(0, 0, 0, 170)
                )
        );

        DisplayUtils.drawRoundedRect(
                x, y, width, height, 4,
                ColorUtils.rgba(0, 0, 0, 120)
        );

        Scissor.push();
        Scissor.setFromComponentCoordinates(
                x + 2,
                y,
                width - 21,
                height
        );

        Fonts.sfMedium.drawText(
                matrixStack,
                textToDraw + cursor,
                x + 5,
                y + (height - 8f) / 2 + 1f,
                ColorUtils.rgb(200, 200, 200),
                7
        );
        Scissor.pop();
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (typing) {
            text += codePoint;
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!isFocused) return false;

        if (ClientUtil.ctrlIsDown()) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                text = "";
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_V) {
                text += ClientUtil.pasteString();
                return true;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !text.isEmpty()) {
            text = text.substring(0, text.length() - 1);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
            typing = false;
            isFocused = false;
            return true;
        }

        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean wasFocused = isFocused;
        isFocused = MathUtil.isHovered(
                (float) mouseX,
                (float) mouseY,
                x, y, width, height
        );

        if (isFocused && button == 0) {
            typing = true;
            return true;
        } else if (!isFocused && wasFocused) {
            typing = false;
        }

        return false;
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }

    public void setFocused(boolean focused) {
        isFocused = focused;
        typing = focused;
    }

    public void clear() {
        text = "";
        typing = false;
        isFocused = false;
    }
}
