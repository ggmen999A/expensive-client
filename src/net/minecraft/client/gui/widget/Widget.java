package net.minecraft.client.gui.widget;

import by.algorithm.alpha.system.utils.math.Vector4i;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class Widget extends AbstractGui implements IRenderable, IGuiEventListener {
    public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    protected int width;
    protected int height;
    public int x;
    public int y;
    private ITextComponent message;
    protected boolean isHovered;
    public boolean active = true;
    public boolean visible = true;
    protected float alpha = 1.0F;
    protected long nextNarration = Long.MAX_VALUE;
    private boolean focused;
    private boolean wasHovered = false;
    private long animationStartTime = 0;

    public Widget(int x, int y, int width, int height, ITextComponent title) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.message = title;
    }

    public int getHeightRealms() {
        return this.height;
    }

    protected int getYImage(boolean isHovered) {
        int i = 1;

        if (!this.active) {
            i = 0;
        } else if (isHovered) {
            i = 2;
        }

        return i;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

            if (this.wasHovered != this.isHovered()) {
                if (this.isHovered()) {
                    if (this.focused) {
                        this.queueNarration(200);
                    } else {
                        this.queueNarration(750);
                    }
                } else {
                    this.nextNarration = Long.MAX_VALUE;
                }
            }

            if (this.visible) {
                this.renderButton(matrixStack, mouseX, mouseY, partialTicks);
            }

            this.narrate();
            this.wasHovered = this.isHovered();
        }
    }

    protected void narrate() {
        if (this.active && this.isHovered() && Util.milliTime() > this.nextNarration) {
            String s = this.getNarrationMessage().getString();

            if (!s.isEmpty()) {
                NarratorChatListener.INSTANCE.say(s);
                this.nextNarration = Long.MAX_VALUE;
            }
        }
    }

    protected IFormattableTextComponent getNarrationMessage() {
        return new TranslationTextComponent("gui.narrate.button", this.getMessage());
    }

    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer fontrenderer = minecraft.fontRenderer;

        // Анимационные параметры
        float animationSpeed = 0.15f; // Скорость анимации (умеренная)
        long currentTime = System.currentTimeMillis();

        // Если состояние hover изменилось, сохраняем время изменения
        if (this.isHovered() != wasHovered) {
            animationStartTime = currentTime;
            wasHovered = this.isHovered();
        }

        // Рассчитываем прогресс анимации (0-1)
        float progress = MathHelper.clamp((currentTime - animationStartTime) / (1000f * animationSpeed), 0, 1);

        // Если анимация завершена, устанавливаем конечные цвета
        if (progress >= 1) {
            progress = 1;
        }

        // Цвета для обычного состояния
        Vector4i normalColors = new Vector4i(
                0xFF000000, // Верхний левый
                0xFF000000, // Верхний правый
                0xFF330000, // Нижний левый
                0xFF330000  // Нижний правый
        );

        // Цвета для состояния hover
        Vector4i hoverColors = new Vector4i(
                0xFF550000, // Верхний левый (красный)
                0xFF1A1A1A, // Верхний правый (черный)
                0xFF1A1A1A, // Нижний левый (черный)
                0xFF550000  // Нижний правый (красный)
        );

        // Промежуточные цвета во время анимации
        Vector4i currentColors;
        if (this.isHovered()) {
            // Анимация к hover состоянию
            currentColors = new Vector4i(
                    interpolateColor(normalColors.getX(), hoverColors.getX(), progress),
                    interpolateColor(normalColors.getY(), hoverColors.getY(), progress),
                    interpolateColor(normalColors.getZ(), hoverColors.getZ(), progress),
                    interpolateColor(normalColors.getW(), hoverColors.getW(), progress)
            );
        } else {
            // Анимация к нормальному состоянию
            currentColors = new Vector4i(
                    interpolateColor(hoverColors.getX(), normalColors.getX(), progress),
                    interpolateColor(hoverColors.getY(), normalColors.getY(), progress),
                    interpolateColor(hoverColors.getZ(), normalColors.getZ(), progress),
                    interpolateColor(hoverColors.getW(), normalColors.getW(), progress)
            );
        }

        float radius = 8.0f;
        Vector4f cornerRadii = new Vector4f(radius, radius, radius, radius);

        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        // Рисуем кнопку с текущими цветами
        DisplayUtils.drawRoundedRect(
                this.x, this.y, this.width, this.height,
                cornerRadii,
                currentColors
        );

        this.renderBg(matrixStack, minecraft, mouseX, mouseY);

        int textColor = this.active ? 0xFFFFFFFF : 0xFFA0A0A0;
        drawCenteredString(
                matrixStack,
                fontrenderer,
                this.getMessage(),
                this.x + this.width / 2,
                this.y + (this.height - 8) / 2,
                textColor | MathHelper.ceil(this.alpha * 255.0F) << 24
        );

        RenderSystem.popMatrix();
    }

    // Вспомогательный метод для интерполяции цветов
    private int interpolateColor(int startColor, int endColor, float progress) {
        int startA = (startColor >> 24) & 0xFF;
        int startR = (startColor >> 16) & 0xFF;
        int startG = (startColor >> 8) & 0xFF;
        int startB = startColor & 0xFF;

        int endA = (endColor >> 24) & 0xFF;
        int endR = (endColor >> 16) & 0xFF;
        int endG = (endColor >> 8) & 0xFF;
        int endB = endColor & 0xFF;

        int currentA = (int)(startA + (endA - startA) * progress);
        int currentR = (int)(startR + (endR - startR) * progress);
        int currentG = (int)(startG + (endG - startG) * progress);
        int currentB = (int)(startB + (endB - startB) * progress);

        return (currentA << 24) | (currentR << 16) | (currentG << 8) | currentB;
    }

    protected void renderBg(MatrixStack matrixStack, Minecraft minecraft, int mouseX, int mouseY) {
    }

    public void onClick(double mouseX, double mouseY) {
    }

    public void onRelease(double mouseX, double mouseY) {
    }

    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible) {
            if (this.isValidClickButton(button)) {
                boolean flag = this.clicked(mouseX, mouseY);

                if (flag) {
                    this.playDownSound(Minecraft.getInstance().getSoundHandler());
                    this.onClick(mouseX, mouseY);
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.isValidClickButton(button)) {
            this.onRelease(mouseX, mouseY);
            return true;
        } else {
            return false;
        }
    }

    protected boolean isValidClickButton(int button) {
        return button == 0;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.isValidClickButton(button)) {
            this.onDrag(mouseX, mouseY, dragX, dragY);
            return true;
        } else {
            return false;
        }
    }

    protected boolean clicked(double mouseX, double mouseY) {
        return this.active && this.visible && mouseX >= (double) this.x && mouseY >= (double) this.y && mouseX < (double) (this.x + this.width) && mouseY < (double) (this.y + this.height);
    }

    public boolean isHovered() {
        return this.isHovered || this.focused;
    }

    public boolean changeFocus(boolean focus) {
        if (this.active && this.visible) {
            this.focused = !this.focused;
            this.onFocusedChanged(this.focused);
            return this.focused;
        } else {
            return false;
        }
    }

    protected void onFocusedChanged(boolean focused) {
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active && this.visible && mouseX >= (double) this.x && mouseY >= (double) this.y && mouseX < (double) (this.x + this.width) && mouseY < (double) (this.y + this.height);
    }

    public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
    }

    public void playDownSound(SoundHandler handler) {
        handler.play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public int getWidth() {
        return this.width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setMessage(ITextComponent message) {
        if (!Objects.equals(message.getString(), this.message.getString())) {
            this.queueNarration(250);
        }

        this.message = message;
    }

    public void queueNarration(int delay) {
        this.nextNarration = Util.milliTime() + (long) delay;
    }

    public ITextComponent getMessage() {
        return this.message;
    }

    public boolean isFocused() {
        return this.focused;
    }

    protected void setFocused(boolean focused) {
        this.focused = focused;
    }
}
