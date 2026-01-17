package by.algorithm.alpha.system.visuals.gui;

import java.util.ArrayList;
import java.util.List;

import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.system.utils.client.ClientUtil;
import by.algorithm.alpha.system.utils.client.Vec2i;
import by.algorithm.alpha.system.utils.render.*;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import by.algorithm.alpha.system.visuals.gui.impl.SearchField;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.system.utils.other.CustomFramebuffer;
import by.algorithm.alpha.system.utils.client.IMinecraft;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.visuals.gui.components.ModuleComponent;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

public class GUI extends Screen implements IMinecraft {

    private final List<MainPanel> panels = new ArrayList<>();
    @Getter
    private static Animation animation = new Animation();
    public SearchField searchField;

    public GUI(ITextComponent titleIn) {
        super(titleIn);
        for (ModuleCategory category : ModuleCategory.values()) {
            panels.add(new MainPanel(category));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void init() {
        animation = animation.animate(1, 0.25f, Easings.EXPO_OUT);
        int windowWidth = ClientUtil.calc(mc.getMainWindow().getScaledWidth());
        int windowHeight = ClientUtil.calc(mc.getMainWindow().getScaledHeight());

        float x = (windowWidth / 2f) - (panels.size() * (135 + 10) / 2f) + 2 * (135 + 10) + 27.5f;
        float y = windowHeight / 2f + (650 / 2f) / 2f + 30;

        searchField = new SearchField((int) x, (int) y, 120, 16, "Поиск");
        super.init();
    }

    public static float scale = 1.0f;

    @Override
    public void closeScreen() {
        super.closeScreen();
        GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Vec2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        Vec2i fix = ClientUtil.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        for (MainPanel panel : panels) {
            if (MathUtil.isHovered((float) mouseX, (float) mouseY, panel.getX(), panel.getY(), panel.getWidth(),
                    panel.getHeight())) {
                panel.setScroll((float) (panel.getScroll() + (delta * 20)));
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (MainPanel panel : panels) {
            panel.charTyped(codePoint, modifiers);
        }
        if (searchField.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    // Helper method to find the hovered module
    private ModuleComponent getHoveredModule(float mouseX, float mouseY) {
        for (MainPanel panel : panels) {
            for (ModuleComponent component : panel.getModules()) {
                if (!Initclass.getInstance().getDropDown().searchCheck(component.getFunction().getName()) &&
                        MathUtil.isHovered(mouseX, mouseY, component.getX(), component.getY(), component.getWidth(), 20)) {
                    return component;
                }
            }
        }
        return null;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        KawaseBlur.blur.updateBlur(3, 3);
        mc.gameRenderer.setupOverlayRendering(2);
        animation.update();

        if (animation.getValue() < 0.1) {
            closeScreen();
        }
        final float off = 8;
        float width = panels.size() * (135 - off);

        updateScaleBasedOnScreenWidth();

        int windowWidth = ClientUtil.calc(mc.getMainWindow().getScaledWidth());
        int windowHeight = ClientUtil.calc(mc.getMainWindow().getScaledHeight());

        Vec2i fixMouse = adjustMouseCoordinates(mouseX, mouseY);
        Vec2i fix = ClientUtil.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        // Render panels and their content
        Stencil.initStencilToWrite();
        GlStateManager.pushMatrix();
        GlStateManager.translatef(windowWidth / 2f, windowHeight / 2f, 0);
        GlStateManager.scaled(animation.getValue(), animation.getValue(), 1);
        GlStateManager.scaled(scale, scale, 1);
        GlStateManager.translatef(-windowWidth / 2f, -windowHeight / 2f, 0);

        for (MainPanel panel : panels) {
            DisplayUtils.drawRoundedRect(panel.getX(), panel.getY(), panel.getWidth(),
                    panel.getHeight() - 2,
                    new Vector4f(7, 7, 7, 7), -1);
        }
        GlStateManager.popMatrix();
        Stencil.readStencilBuffer(1);
        GlStateManager.bindTexture(KawaseBlur.blur.BLURRED.framebufferTexture);
        CustomFramebuffer.drawTexture();
        Stencil.uninitStencilBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.translatef(windowWidth / 2f, windowHeight / 2f, 0);
        GlStateManager.scaled(animation.getValue(), animation.getValue(), 1);
        GlStateManager.scaled(scale, scale, 1);
        GlStateManager.translatef(-windowWidth / 2f, -windowHeight / 2f, 0);
        for (MainPanel panel : panels) {
            panel.setY(windowHeight / 2f - (747 / 2) / 2f);
            panel.setX((windowWidth / 2f) - (width / 2f) + panel.getCategory().ordinal() *
                    (135 - off) - off / 2f);
            float animationValue = (float) animation.getValue() * scale;

            float halfAnimationValueRest = (1 - animationValue) / 2f;

            float testX = panel.getX() + (panel.getWidth() * halfAnimationValueRest);
            float testY = panel.getY() + (panel.getHeight() * halfAnimationValueRest);
            float testW = panel.getWidth() * animationValue;
            float testH = panel.getHeight() * animationValue;

            testX = testX * animationValue + ((windowWidth - testW) *
                    halfAnimationValueRest);

            Scissor.push();
            Scissor.setFromComponentCoordinates(testX, testY, testW, testH - 0.5f);
            panel.render(matrixStack, mouseX, mouseY);
            Scissor.unset();
            Scissor.pop();
        }
        searchField.render(matrixStack, mouseX, mouseY, partialTicks);
        GlStateManager.popMatrix();

        ModuleComponent hoveredModule = getHoveredModule(mouseX, mouseY);
        if (hoveredModule != null) {
            String description = hoveredModule.getFunction().getDescription();
            float fontSize = 11.2f;
            float textWidth = Fonts.tenacity.getWidth(description, fontSize);
            float textHeight = Fonts.tenacity.getHeight(fontSize);
            float textX = (windowWidth - textWidth) / 2f;
            float textY = panels.get(0).getY() - textHeight - 50;
            Fonts.tenacity.drawText(matrixStack, description, textX, textY, ColorUtils.rgb(255, 255, 255), fontSize, 0.1f);
        }

        mc.gameRenderer.setupOverlayRendering();
    }

    public boolean isSearching() {
        return !searchField.isEmpty();
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public boolean searchCheck(String text) {
        return isSearching() && !text
                .replaceAll(" ", "")
                .toLowerCase()
                .contains(getSearchText()
                        .replaceAll(" ", "")
                        .toLowerCase());
    }

    private void updateScaleBasedOnScreenWidth() {
        final float PANEL_WIDTH = 135;
        final float MARGIN = 10;
        final float MIN_SCALE = 0.5f;

        float totalPanelWidth = panels.size() * (PANEL_WIDTH + MARGIN);
        float screenWidth = mc.getMainWindow().getScaledWidth();

        if (totalPanelWidth >= screenWidth) {
            scale = screenWidth / totalPanelWidth;
            scale = MathHelper.clamp(scale, MIN_SCALE, 1.0f);
        } else {
            scale = 1f;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (MainPanel panel : panels) {
            panel.keyPressed(keyCode, scanCode, modifiers);
        }
        if (searchField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            animation = animation.animate(0, 0.25f, Easings.EXPO_OUT);
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private Vec2i adjustMouseCoordinates(int mouseX, int mouseY) {
        int windowWidth = mc.getMainWindow().getScaledWidth();
        int windowHeight = mc.getMainWindow().getScaledHeight();

        float adjustedMouseX = (mouseX - windowWidth / 2f) / scale + windowWidth / 2f;
        float adjustedMouseY = (mouseY - windowHeight / 2f) / scale + windowHeight / 2f;

        return new Vec2i((int) adjustedMouseX, (int) adjustedMouseY);
    }

    private double pathX(float mouseX, float scale) {
        if (scale == 1) return mouseX;
        int windowWidth = mc.getMainWindow().scaledWidth();
        int windowHeight = mc.getMainWindow().scaledHeight();
        mouseX /= (scale);
        mouseX -= (windowWidth / 2f) - (windowWidth / 2f) * (scale);
        return mouseX;
    }

    private double pathY(float mouseY, float scale) {
        if (scale == 1) return mouseY;
        int windowWidth = mc.getMainWindow().scaledWidth();
        int windowHeight = mc.getMainWindow().scaledHeight();
        mouseY /= scale;
        mouseY -= (windowHeight / 2f) - (windowHeight / 2f) * (scale);
        return mouseY;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        Vec2i fix = ClientUtil.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();

        for (MainPanel panel : panels) {
            panel.mouseClick((float) mouseX, (float) mouseY, button);
        }
        if (searchField.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vec2i fixMouse = adjustMouseCoordinates((int) mouseX, (int) mouseY);
        Vec2i fix = ClientUtil.getMouse(fixMouse.getX(), fixMouse.getY());
        mouseX = fix.getX();
        mouseY = fix.getY();
        for (MainPanel panel : panels) {
            panel.mouseRelease((float) mouseX, (float) mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}