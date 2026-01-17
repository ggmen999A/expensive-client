package by.algorithm.alpha.system.visuals.gui.components;

import by.algorithm.alpha.api.modules.settings.impl.*;
import by.algorithm.alpha.system.visuals.gui.components.settings.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.settings.Setting;
import by.algorithm.alpha.system.visuals.gui.impl.Component;
import by.algorithm.alpha.system.visuals.gui.MainPanel;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.utils.math.Vector4i;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.Cursors;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.Stencil;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.glfw.GLFW;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

@Getter
@Setter
public class ModuleComponent extends Component {
    private final Vector4f ROUNDING_VECTOR = new Vector4f(7, 7, 7, 7);
    private final Vector4i BORDER_COLOR = new Vector4i(ColorUtils.rgb(45, 46, 53), ColorUtils.rgb(25, 26, 31), ColorUtils.rgb(45, 46, 53), ColorUtils.rgb(25, 26, 31));

    private final Module function;
    public Animation animation = new Animation();
    public boolean open;
    private boolean bind;
    private MainPanel panel;

    private final ObjectArrayList<Component> components = new ObjectArrayList<>();

    public ModuleComponent(Module function) {
        this.function = function;
        for (Setting<?> setting : function.getSettings()) {
            if (setting instanceof BooleanSetting bool) {
                components.add(new BooleanComponent(bool));
            }
            if (setting instanceof SliderSetting slider) {
                components.add(new SliderComponent(slider));
            }
            if (setting instanceof BindSetting bind) {
                components.add(new BindComponent(bind));
            }
            if (setting instanceof ModeSetting mode) {
                components.add(new ModeComponent(mode));
            }
            if (setting instanceof ModeListSetting mode) {
                components.add(new MultiBoxComponent(mode));
            }
            if (setting instanceof StringSetting string) {
                components.add(new StringComponent(string));
            }
        }
        animation = animation.animate(open ? 1 : 0, 0.3);
    }

    public void drawComponents(MatrixStack stack, float mouseX, float mouseY) {
        if (animation.getValue() > 0) {
            if (animation.getValue() > 0.1 && components.stream().filter(Component::isVisible).count() >= 1) {
                DisplayUtils.drawRectVerticalW(getX() + 5, getY() + 20, getWidth() - 10, 0.5f, ColorUtils.rgb(42, 44, 50), ColorUtils.rgb(28, 28, 33));
            }
            Stencil.initStencilToWrite();
            DisplayUtils.drawRoundedRect(getX() + 0.5f, getY() + 0.5f, getWidth() - 1, getHeight() - 1, ROUNDING_VECTOR, ColorUtils.rgba(23, 23, 23, (int) (255 * 0.33)));
            Stencil.readStencilBuffer(1);
            float y = getY() + 20;
            for (Component component : components) {
                if (component.isVisible()) {
                    component.setX(getX());
                    component.setY(y);
                    component.setWidth(getWidth());
                    component.render(stack, mouseX, mouseY);
                    y += component.getHeight();
                }
            }
            Stencil.uninitStencilBuffer();
        }
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        for (Component component : components) {
            component.mouseRelease(mouseX, mouseY, mouse);
        }
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    private boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        int color = ColorUtils.interpolate(ColorUtils.rgb(255, 255, 255), ColorUtils.rgb(255, 255, 255), (float) function.getAnimation().getValue());
        function.getAnimation().update();
        super.render(stack, mouseX, mouseY);
        drawOutlinedRect(mouseX, mouseY);
        drawText(stack, color);
        drawComponents(stack, mouseX, mouseY);
        boolean isHovered = MathUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), 20);

        if (isHovered) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        if (isHovered(mouseX, mouseY, 20)) {
            if (button == 0) function.toggle();
            if (button == 1) {
                open = !open;
                animation = animation.animate(open ? 1 : 0, 0.2, Easings.CIRC_OUT);
            }
            if (button == 2) {
                bind = !bind;
            }
        }
        if (isHovered(mouseX, mouseY)) {
            if (open) {
                for (Component component : components) {
                    if (component.isVisible()) component.mouseClick(mouseX, mouseY, button);
                }
            }
        }
        super.mouseClick(mouseX, mouseY, button);
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (Component component : components) {
            if (component.isVisible()) component.charTyped(codePoint, modifiers);
        }
        super.charTyped(codePoint, modifiers);
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        for (Component component : components) {
            if (component.isVisible()) component.keyPressed(key, scanCode, modifiers);
        }
        if (bind) {
            if (key == GLFW.GLFW_KEY_DELETE) {
                function.setBind(0);
            } else function.setBind(key);
            bind = false;
        }
        super.keyPressed(key, scanCode, modifiers);
    }

    private void drawOutlinedRect(float mouseX, float mouseY) {
        DisplayUtils.drawRoundedRectOutline(
                getX() + 0.2f,
                getY() + 0.5f,
                getWidth() - 1f,
                getHeight() - 1f,
                6,
                1,
                function.isState() ? ColorUtils.setAlpha(ColorUtils.rgb(176, 31, 21), 75) : ColorUtils.setAlpha(ColorUtils.rgb(155, 155, 155), 55)
        );

        if (function.isState()) {
            DisplayUtils.drawRoundedRect(
                    getX() + 0.2f,
                    getY() + 0.5f,
                    getWidth() - 1f,
                    getHeight() - 1f,
                    new Vector4f(6, 6, 6, 6),
                    new Vector4i(
                            ColorUtils.rgba(176, 31, 21, 27),
                            ColorUtils.rgba(176, 31, 21, 27),
                            ColorUtils.rgba(166, 26, 17, 27),
                            ColorUtils.rgba(166, 26, 17, 27)
                    )
            );
        }

        if (MathUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), 20.0F)) {
            if (!this.hovered) {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
                this.hovered = true;
            }
        } else if (this.hovered) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
            this.hovered = false;
        }
    }

    public boolean isMouseOver(float f, float f2) {
        float f3 = this.getX();
        float f4 = this.getY();
        float f5 = this.getWidth();
        float f6 = this.getHeight();
        return f >= f3 && f <= f3 + f5 && f2 >= f4 && f2 <= f4 + f6;
    }

    private void drawText(MatrixStack stack, int color) {
        if (!bind) {
            Fonts.montserrat.drawText(stack, function.getName(), getX() + 6, getY() + 6.5f, color, 7, 0.1f);
        }

        if (components.stream().filter(Component::isVisible).count() >= 1) {
            if (bind) {
                Fonts.montserrat.drawText(stack, "Press key for bind",
                        getX() + 5,
                        getY() + Fonts.icons.getHeight(6) + 1,
                        ColorUtils.rgb(161, 164, 177), 7, 0.1f);
            } else {
                Fonts.icons.drawText(stack, !open ? "B" : "C", getX() + getWidth() - 6 - Fonts.icons.getWidth(!open ? "B" : "C", 6), getY() + Fonts.icons.getHeight(6) + 1, ColorUtils.rgb(161, 164, 177), 6);
            }
        } else {
            if (bind) {
                Fonts.montserrat.drawText(stack, "Press key for bind",
                        getX() + 5,
                        getY() + Fonts.icons.getHeight(6) + 1,
                        ColorUtils.rgb(161, 164, 177), 7, 0.1f);
            }
        }
    }
}