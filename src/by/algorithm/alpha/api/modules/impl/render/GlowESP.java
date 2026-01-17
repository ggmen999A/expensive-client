package by.algorithm.alpha.api.modules.impl.render;

import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.ColorSetting;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MainWindow;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleAnnot(name = "GlowESP", type = ModuleCategory.Render, description = "Подсветка сущностей через блюр фреймбуффера")
public class GlowESP extends Module {

    final BooleanSetting playersOnly = new BooleanSetting("Только игроки", true);
    final BooleanSetting mobs = new BooleanSetting("Мобы", false);
    final SliderSetting radius = new SliderSetting("Радиус блюра", 4.0F, 1.0F, 15.0F, 0.5F);
    final SliderSetting intensity = new SliderSetting("Интенсивность", 1.5F, 0.5F, 4.0F, 0.1F);
    final ColorSetting glowColor = new ColorSetting("Цвет", -1); // Белый по умолчанию

    Framebuffer entitiesFramebuffer;

    public GlowESP() {
        addSettings(playersOnly, mobs, radius, intensity, glowColor);
    }

    private Framebuffer createOrResizeFramebuffer(Framebuffer framebuffer) {
        MainWindow window = mc.getMainWindow();
        int width = window.getFramebufferWidth();
        int height = window.getFramebufferHeight();

        if (framebuffer == null || framebuffer.framebufferWidth != width || framebuffer.framebufferHeight != height) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            framebuffer = new Framebuffer(width, height, true, Minecraft.IS_RUNNING_ON_MAC);
        }

        return framebuffer;
    }

    // Вызывается перед началом отрисовки сущностей (инжект в WorldRenderer.renderEntities или аналог)
    public void onPreEntitiesRender() {
        if (!isState()) return;

        entitiesFramebuffer = createOrResizeFramebuffer(entitiesFramebuffer);

        entitiesFramebuffer.framebufferClear(Minecraft.IS_RUNNING_ON_MAC);
        entitiesFramebuffer.bindFramebuffer(true);
    }

    // Вызывается после отрисовки всех сущностей (инжект после цикла)
    public void onPostEntitiesRender() {
        if (!isState()) return;

        entitiesFramebuffer.unbindFramebuffer();
        mc.getFramebuffer().bindFramebuffer(true);
    }

    // Вызывается в renderGameOverlay / renderIngameGui (инжект в конец метода)
    public void onRenderOverlay() {
        if (!isState() || entitiesFramebuffer == null) return;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        // Первый проход — с блюром (предполагаем, что у вас есть BlurHelper или шейдер блюра)
        // Если шейдера нет — можно сделать несколько проходов с оффсетом для имитации блюра
        // Пример с простым имитационным блюром через несколько проходов:
        int color = glowColor.get();
        float alpha = intensity.get();

        GL11.glColor4f(((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8)  & 0xFF) / 255.0F,
                (color         & 0xFF) / 255.0F,
                alpha / 10.0F);

        float rad = radius.get();
// Первый проход — имитация блюра через несколько проходов с смещением
        GL11.glColor4f(
                ((glowColor.get() >> 16) & 0xFF) / 255.0F,
                ((glowColor.get() >> 8)  & 0xFF) / 255.0F,
                (glowColor.get()         & 0xFF) / 255.0F,
                intensity.get() / 10.0F
        );


// Горизонтальный блюр (несколько проходов для большей интенсивности)
        for (int i = 0; i < 4; i++) {
            // Смещение вправо
            GL11.glTranslatef(rad / 2.0F, 0, 0);
            entitiesFramebuffer.framebufferRenderExt(
                    mc.getMainWindow().getScaledWidth(),
                    mc.getMainWindow().getScaledHeight(),
                    false  // НЕ отключать blend
            );

            // Смещение влево
            GL11.glTranslatef(-rad, 0, 0);
            entitiesFramebuffer.framebufferRenderExt(
                    mc.getMainWindow().getScaledWidth(),
                    mc.getMainWindow().getScaledHeight(),
                    false
            );

            // Возврат в центр + вертикальное смещение
            GL11.glTranslatef(rad / 2.0F, rad / 2.0F, 0);
            entitiesFramebuffer.framebufferRenderExt(
                    mc.getMainWindow().getScaledWidth(),
                    mc.getMainWindow().getScaledHeight(),
                    false
            );

            // Вертикальное смещение вниз
            GL11.glTranslatef(0, -rad, 0);
            entitiesFramebuffer.framebufferRenderExt(
                    mc.getMainWindow().getScaledWidth(),
                    mc.getMainWindow().getScaledHeight(),
                    false
            );
        }

// Второй проход — оригинальная текстура без смещения и с полной непрозрачностью
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        entitiesFramebuffer.framebufferRenderExt(
                mc.getMainWindow().getScaledWidth(),
                mc.getMainWindow().getScaledHeight(),
                false
        );


        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    public void onDisable() {
        if (entitiesFramebuffer != null) {
            entitiesFramebuffer.deleteFramebuffer();
            entitiesFramebuffer = null;
        }
        super.onDisable();
    }
}