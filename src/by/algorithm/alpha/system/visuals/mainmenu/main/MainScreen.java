package by.algorithm.alpha.system.visuals.mainmenu.main;

import com.mojang.blaze3d.matrix.MatrixStack;
import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.system.utils.client.ClientUtil;
import by.algorithm.alpha.system.utils.client.Vec2i;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.utils.math.StopWatch;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.GameSettings;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static by.algorithm.alpha.system.utils.client.IMinecraft.mc;

public class MainScreen extends Screen {
    public MainScreen() {
        super(ITextComponent.getTextComponentOrEmpty(""));
    }

    private final List<Button> buttons = new ArrayList<>();
    private final List<FloatingParticle> particles = new ArrayList<>();
    private final Random random = new Random();
    private final StopWatch particleTimer = new StopWatch();
    private final ResourceLocation backgroundImage = new ResourceLocation("expensive/images/mainmenu/photo.png");

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        configureRefreshRate();

        float widthButton = 200;
        float heightButton = 28; // Уменьшена высота кнопок с 35 до 28
        float gap = 10;
        float sideButtonWidth = (widthButton - gap) / 2f;

        float x = ClientUtil.calc(width) / 2f - widthButton / 2f;
        float y = Math.round(ClientUtil.calc(height) / 2f - 20);

        buttons.clear();

        buttons.add(new Button(x, y - 60, widthButton, heightButton, "Одиночная игра", () -> {
            mc.displayGuiScreen(new WorldSelectionScreen(this));
        }));

        buttons.add(new Button(x, y - 20, widthButton, heightButton, "Сетевая игра", () -> {
            mc.displayGuiScreen(new MultiplayerScreen(this));
        }));

        buttons.add(new Button(x, y + 20, widthButton, heightButton, "Настройки", () -> {
            mc.displayGuiScreen(new OptionsScreen(this, mc.gameSettings));
        }));

        buttons.add(new Button(x, y + 60, sideButtonWidth, heightButton, "Аккаунты", () -> {
            mc.displayGuiScreen(Initclass.getInstance().getAltScreen());
        }));

        buttons.add(new Button(x + sideButtonWidth + gap, y + 60, sideButtonWidth, heightButton, "Выйти", mc::shutdownMinecraftApplet));

        initializeParticles();
    }

    private void initializeParticles() {
        particles.clear();
        for (int i = 0; i < 20; i++) { // Увеличено количество частиц
            particles.add(new FloatingParticle());
        }
    }

    public void configureRefreshRate() {
        Minecraft mc = Minecraft.getInstance();
        GameSettings settings = mc.gameSettings;
        settings.vsync = false;
        settings.framerateLimit = 360;
        settings.saveOptions();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        MainWindow mainWindow = mc.getMainWindow();
        int windowWidth = ClientUtil.calc(mainWindow.getScaledWidth());
        int windowHeight = ClientUtil.calc(mainWindow.getScaledHeight());

        renderBackground(windowWidth, windowHeight);
        renderFloatingParticles(matrixStack);

        DisplayUtils.drawRoundedRect(0, 0, width, height, 0, ColorUtils.rgba(0, 0, 0, 100));

        Fonts.montserrat.drawCenteredText(matrixStack, "Solth", width / 2, height / 2 - 120, ColorUtils.rgb(255, 255, 255), 16);

        drawButtons(matrixStack, mouseX, mouseY, partialTicks);
        updateParticles();

        mc.gameRenderer.setupOverlayRendering();
    }

    private void renderBackground(int windowWidth, int windowHeight) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        DisplayUtils.drawImage(backgroundImage, 0, 0, windowWidth, windowHeight, ColorUtils.rgba(255, 255, 255, 200));
        DisplayUtils.drawRoundedRect(0, 0, windowWidth, windowHeight, 0, ColorUtils.rgba(20, 0, 0, 60));

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void renderFloatingParticles(MatrixStack matrixStack) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        for (FloatingParticle particle : particles) {
            // Улучшена видимость частиц - более яркие и заметные
            float alpha = (float) (Math.sin(particle.life * 0.08f) * 0.4f + 0.7f) * particle.maxAlpha;

            // Добавлен эффект свечения с градиентом
            int coreColor = ColorUtils.rgba(255, 80, 80, (int)(alpha * 255));
            int glowColor = ColorUtils.rgba(255, 40, 40, (int)(alpha * 120));

            // Внешнее свечение
            DisplayUtils.drawRoundedRect(
                    particle.x - particle.size,
                    particle.y - particle.size,
                    particle.size * 2, particle.size * 2,
                    particle.size, glowColor
            );

            // Основная частица
            DisplayUtils.drawRoundedRect(
                    particle.x - particle.size / 2,
                    particle.y - particle.size / 2,
                    particle.size, particle.size,
                    particle.size / 2, coreColor
            );
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void updateParticles() {
        if (particleTimer.isReached(200)) { // Уменьшен интервал появления
            if (particles.size() < 25) {
                particles.add(new FloatingParticle());
            }
            particleTimer.reset();
        }

        particles.removeIf(particle -> {
            particle.update();
            return particle.y < -10 || particle.life > 600;
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2i fixed = ClientUtil.getMouse((int) mouseX, (int) mouseY);
        buttons.forEach(b -> b.click(fixed.getX(), fixed.getY(), button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawButtons(MatrixStack stack, int mX, int mY, float pt) {
        for (Button button : buttons) {
            button.render(stack, mX, mY, pt);
        }
    }

    private class FloatingParticle {
        float x, y, size, speed, life, maxAlpha;
        float horizontalOffset;

        public FloatingParticle() {
            reset();
        }

        private void reset() {
            x = random.nextFloat() * width;
            y = height + 10;
            size = random.nextFloat() * 4 + 2; // Увеличен размер частиц
            speed = random.nextFloat() * 2.0f + 0.8f; // Увеличена скорость
            life = 0;
            maxAlpha = random.nextFloat() * 0.6f + 0.6f; // Увеличена максимальная прозрачность
            horizontalOffset = random.nextFloat() * 2 - 1;
        }

        public void update() {
            y -= speed;
            // Улучшенное горизонтальное движение
            x += (float)(Math.sin(life * 0.04f) * 0.5f + horizontalOffset * 0.2f);
            life++;
        }
    }

    private class Button {
        private final float x, y, width, height;
        private String text;
        private Runnable action;
        private float animation = 0.0f;
        private float scaleAnimation = 0.0f;
        private float glowAnimation = 0.0f;

        public Button(float x, float y, float width, float height, String text, Runnable action) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
            this.action = action;
        }

        public void render(MatrixStack stack, int mouseX, int mouseY, float pt) {
            Vec2i fixed = ClientUtil.getMouse(mouseX, mouseY);
            boolean hovered = MathUtil.isHovered(fixed.getX(), fixed.getY(), x, y, width, height);

            // Многослойная анимация
            animation = MathUtil.lerp(animation, hovered ? 1.0f : 0.0f, 8.0f);
            scaleAnimation = MathUtil.lerp(scaleAnimation, hovered ? 1.0f : 0.0f, 12.0f);
            glowAnimation = MathUtil.lerp(glowAnimation, hovered ? 1.0f : 0.0f, 6.0f);

            int cornerRadius = 8;

            // Эффект масштабирования
            float scale = 1.0f + (scaleAnimation * 0.02f);
            float scaledWidth = width * scale;
            float scaledHeight = height * scale;
            float offsetX = (scaledWidth - width) / 2;
            float offsetY = (scaledHeight - height) / 2;

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();

            // Внешнее свечение при наведении
            if (glowAnimation > 0.1f) {
                int glowColor = ColorUtils.rgba(255, 0, 0, (int)(30 * glowAnimation));
                DisplayUtils.drawRoundedRect(
                        x - offsetX - 3, y - offsetY - 3,
                        scaledWidth + 6, scaledHeight + 6,
                        new Vector4f(cornerRadius + 3, cornerRadius + 3, cornerRadius + 3, cornerRadius + 3),
                        glowColor
                );
            }

            // Основной фон кнопки с градиентным эффектом
            int baseColor = ColorUtils.rgba(35, 35, 45, 190);
            int hoverColor = ColorUtils.rgba(60, 40, 40, 220);
            int currentColor = ColorUtils.interpolateColor(baseColor, hoverColor, animation);

            DisplayUtils.drawRoundedRect(
                    x - offsetX, y - offsetY, scaledWidth, scaledHeight,
                    new Vector4f(cornerRadius, cornerRadius, cornerRadius, cornerRadius),
                    currentColor
            );

            // Анимированная граница
            if (animation > 0.05f) {
                int borderAlpha = (int)(100 * animation);
                int borderColor = ColorUtils.rgba(255, 60, 60, borderAlpha);

                // Двойная граница для эффекта глубины
                DisplayUtils.drawRoundedRect(
                        x - offsetX - 1, y - offsetY - 1,
                        scaledWidth + 2, scaledHeight + 2,
                        new Vector4f(cornerRadius + 1, cornerRadius + 1, cornerRadius + 1, cornerRadius + 1),
                        borderColor
                );

                // Внутренняя светлая граница
                int innerBorderColor = ColorUtils.rgba(255, 120, 120, borderAlpha / 2);
                DisplayUtils.drawRoundedRect(
                        x - offsetX + 1, y - offsetY + 1,
                        scaledWidth - 2, scaledHeight - 2,
                        new Vector4f(cornerRadius - 1, cornerRadius - 1, cornerRadius - 1, cornerRadius - 1),
                        innerBorderColor
                );
            }

            // Анимированный текст с эффектом свечения
            int baseTextColor = ColorUtils.rgb(200, 200, 210);
            int hoverTextColor = ColorUtils.rgb(255, 240, 240);
            int textColor = ColorUtils.interpolateColor(baseTextColor, hoverTextColor, animation);

            // Тень для текста при наведении
            if (animation > 0.3f) {
                int shadowColor = ColorUtils.rgba(255, 0, 0, (int)(60 * animation));
                Fonts.montserrat.drawCenteredText(
                        stack, text,
                        x + scaledWidth / 2 + 1, y + scaledHeight / 2 - 1,
                        shadowColor,
                        8.5f
                );
            }

            Fonts.montserrat.drawCenteredText(
                    stack, text,
                    x + scaledWidth / 2 - offsetX, y + scaledHeight / 2 - offsetY - 2,
                    textColor,
                    8.5f
            );

            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        public void click(int mouseX, int mouseY, int button) {
            if (MathUtil.isHovered(mouseX, mouseY, x, y, width, height)) {
                action.run();
            }
        }
    }
}