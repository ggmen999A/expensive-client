package by.algorithm.alpha.api.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import by.algorithm.alpha.system.events.EventPacket;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.api.modules.settings.impl.ColorSetting;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import lombok.Getter;
import net.minecraft.network.play.server.SUpdateTimePacket;

@Getter
@ModuleAnnot(name = "CustomWorld", type = ModuleCategory.Render, description = "Изменение окружающего мира")
public class World extends Module {

    // Слайдер для времени суток (0-24000, где 0 = рассвет, 6000 = полдень, 12000 = закат, 18000 = полночь)
    public SliderSetting timeSlider = new SliderSetting("Время суток", 6000f, 0f, 24000f, 500f);

    // Настройки тумана
    public BooleanSetting customFog = new BooleanSetting("Кастомный туман", false);
    public SliderSetting fogPower = new SliderSetting("Сила тумана", 20f, 1f, 40f, 1f).setVisible(() -> customFog.get());
    public ModeSetting fogMode = new ModeSetting("Режим тумана", "Клиент", "Клиент", "Свой").setVisible(() -> customFog.get());
    public ColorSetting fogColor = new ColorSetting("Цвет тумана", ColorUtils.rgb(255, 255, 255)).setVisible(() -> customFog.get() && fogMode.is("Свой"));

    public World() {
        addSettings(timeSlider, customFog, fogPower, fogMode, fogColor);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SUpdateTimePacket p) {
            // Устанавливаем время согласно слайдеру
            p.worldTime = timeSlider.get().longValue();
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (customFog.get()) {
            applyCustomFog();
        }
    }

    private void applyCustomFog() {
        if (mc.world == null || mc.player == null) return;

        float red, green, blue;

        // Определяем цвет тумана в зависимости от режима
        if (fogMode.is("Клиент")) {
            // Используем цвет темы HUD
            int themeColor = ColorUtils.setAlpha(HUD.getColor(0, 10), 255);
            red = ((themeColor >> 16) & 0xFF) / 255.0F;
            green = ((themeColor >> 8) & 0xFF) / 255.0F;
            blue = (themeColor & 0xFF) / 255.0F;
        } else {
            // Используем кастомный цвет
            int customColor = fogColor.get();
            red = ((customColor >> 16) & 0xFF) / 255.0F;
            green = ((customColor >> 8) & 0xFF) / 255.0F;
            blue = (customColor & 0xFF) / 255.0F;
        }

        // Применяем цвет тумана
        RenderSystem.clearColor(red, green, blue, 0.0F);
        RenderSystem.fog(2918, red, green, blue, 1.0F); // GL_FOG_COLOR = 2918

        // Применяем силу тумана через изменение дистанции
        float renderDistance = mc.gameSettings.renderDistanceChunks * 16.0F;
        float fogStart = renderDistance * 0.25F;
        float fogEnd = renderDistance;

        // Модифицируем дистанции на основе силы тумана
        fogStart /= fogPower.get();
        fogEnd /= (fogPower.get() * 0.5F);

        // Устанавливаем дистанции тумана
        RenderSystem.fogStart(fogStart);
        RenderSystem.fogEnd(fogEnd);
        RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
    }
}