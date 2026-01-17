package by.algorithm.alpha.system.visuals.gui;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;

import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.system.visuals.gui.components.ModuleComponent;
import by.algorithm.alpha.system.visuals.gui.impl.Component;
import by.algorithm.alpha.system.visuals.gui.impl.IBuilder;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.utils.math.Vector4i;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.Scissor;
import by.algorithm.alpha.system.utils.render.Stencil;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;

@Getter
@Setter
public class MainPanel implements IBuilder {

    private final ModuleCategory category;
    protected float x;
    protected float y;
    protected final float width = 120;
    protected final float height = 340;

    private List<ModuleComponent> modules = new ArrayList<>();
    private float scroll, animatedScrool;

    public MainPanel(ModuleCategory category) {
        this.category = category;

        for (Module function : Initclass.getInstance().getFunctionRegistry().getFunctions()) {
            if (function.getCategory() == category) {
                ModuleComponent component = new ModuleComponent(function);
                component.setPanel(this);
                modules.add(component);
            }
        }
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        animatedScrool = MathUtil.fast(animatedScrool, scroll, 10);

        float header = 55 / 2f;
        float headerFont = 11;

        DisplayUtils.drawRoundedRect(x, y, width, height, 6,
                ColorUtils.rgba(0, 0, 0, 200));

        Fonts.sfMedium.drawCenteredText(stack, category.getName(), x + width / 2,
                y + header / 2f - Fonts.montserrat.getHeight(headerFont) / 2f - 1, -1, headerFont);

        drawComponents(stack, mouseX, mouseY);
    }

    protected void drawOutline() {
        Stencil.initStencilToWrite();

        DisplayUtils.drawRoundedRect(x + 0.5f, y + 0.5f, width - 1, height - 1, new Vector4f(6.5F, 6.5F, 6.5F, 6.5F),
                ColorUtils.rgba(23, 23, 23, (int) (255 * 0.33)));

        Stencil.readStencilBuffer(0);

        DisplayUtils.drawRoundedRect(x, y, width, height,
                new Vector4f(6.5f, 6.5f, 6.5f, 6.5f),
                new Vector4i(ColorUtils.rgb(48, 53, 60), ColorUtils.rgb(0, 0, 0), ColorUtils.rgb(48, 53, 60),
                        ColorUtils.rgb(0, 0, 0)));

        Stencil.uninitStencilBuffer();
    }

    float max = 0;

    private void drawComponents(MatrixStack stack, float mouseX, float mouseY) {
        float animationValue = (float) GUI.getAnimation().getValue() * GUI.scale;

        float halfAnimationValueRest = (1 - animationValue) / 2f;
        float height = getHeight();
        float testX = getX() + (getWidth() * halfAnimationValueRest);
        float testY = getY() + 55 / 2f + (height * halfAnimationValueRest);
        float testW = getWidth() * animationValue;
        float testH = height * animationValue;

        testX = testX * animationValue + ((Minecraft.getInstance().getMainWindow().getScaledWidth() - testW) *
                halfAnimationValueRest);

        Scissor.push();
        Scissor.setFromComponentCoordinates(testX, testY, testW, testH);
        float offset = 0;
        float header = 55 / 2f;

        if (max > height - header - 10) {
            scroll = MathHelper.clamp(scroll, -max + height - header - 10, 0);
            animatedScrool = MathHelper.clamp(animatedScrool, -max + height - header - 10, 0);
        } else {
            scroll = 0;
            animatedScrool = 0;
        }
        for (ModuleComponent component : modules) {
            if (Initclass.getInstance().getDropDown().searchCheck(component.getFunction().getName())) {
                continue;
            }
            component.setX(getX() + 5);
            component.setY(getY() + header + offset + 6 + animatedScrool);
            component.setWidth(getWidth() - 10);
            component.setHeight(20);
            component.animation.update();
            if (component.animation.getValue() > 0) {
                float componentOffset = 0;
                for (Component component2 : component.getComponents()) {
                    if (component2.isVisible())
                        componentOffset += component2.getHeight();
                }
                componentOffset *= component.animation.getValue();
                component.setHeight(component.getHeight() + componentOffset);
            }
            component.render(stack, mouseX, mouseY);
            offset += component.getHeight() + 3.5f;
        }
        max = offset;

        Scissor.unset();
        Scissor.pop();
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        float header = 1;
        float animationValue = (float) GUI.getAnimation().getValue() * GUI.scale;
        float halfAnimationValueRest = (1 - animationValue) / 2f;
        float testX = getX() + (getWidth() * halfAnimationValueRest);
        float testY = getY() + -header + (getHeight() * halfAnimationValueRest);
        float testW = getWidth() * animationValue;
        float testH = getHeight() * animationValue;

        if (!MathUtil.isHovered(mouseX, mouseY, testX, testY, testW, testH)) {
            return;
        }

        for (ModuleComponent component : modules) {
            if (Initclass.getInstance().getDropDown().searchCheck(component.getFunction().getName())) {
                continue;
            }

            float componentX = component.getX();
            float componentY = component.getY();
            float componentWidth = component.getWidth();
            float componentHeight = component.getHeight();

            if (componentY + componentHeight >= testY && componentY <= testY + testH) {
                if (MathUtil.isHovered(mouseX, mouseY, componentX, componentY, componentWidth, componentHeight)) {
                    component.mouseClick(mouseX, mouseY, button);
                }
            }
        }
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        for (ModuleComponent component : modules) {
            component.keyPressed(key, scanCode, modifiers);
        }
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (ModuleComponent component : modules) {
            component.charTyped(codePoint, modifiers);
        }
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int button) {
        float header = 55 / 2f;
        float animationValue = (float) GUI.getAnimation().getValue() * GUI.scale;
        float halfAnimationValueRest = (1 - animationValue) / 2f;
        float testX = getX() + (getWidth() * halfAnimationValueRest);
        float testY = getY() + header + (getHeight() * halfAnimationValueRest);
        float testW = getWidth() * animationValue;
        float testH = getHeight() * animationValue;

        if (!MathUtil.isHovered(mouseX, mouseY, testX, testY, testW, testH)) {
            return;
        }

        for (ModuleComponent component : modules) {
            if (Initclass.getInstance().getDropDown().searchCheck(component.getFunction().getName())) {
                continue;
            }

            float componentX = component.getX();
            float componentY = component.getY();
            float componentWidth = component.getWidth();
            float componentHeight = component.getHeight();

            if (componentY + componentHeight >= testY && componentY <= testY + testH) {
                if (MathUtil.isHovered(mouseX, mouseY, componentX, componentY, componentWidth, componentHeight)) {
                    component.mouseRelease(mouseX, mouseY, button);
                }
            }
        }
    }
}