package by.algorithm.alpha.system.visuals.hud.impl;

import by.algorithm.alpha.system.utils.animations.Animation;
import by.algorithm.alpha.system.utils.animations.Direction;
import by.algorithm.alpha.system.utils.animations.impl.EaseInOutQuad;
import com.mojang.blaze3d.matrix.MatrixStack;
import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.system.events.EventDisplay;
import by.algorithm.alpha.system.utils.dragable.Dragging;
import by.algorithm.alpha.system.utils.math.Vector4i;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.visuals.hud.ElementRenderer;
import by.algorithm.alpha.system.visuals.styles.Style;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class Potions implements ElementRenderer {
    final Dragging dragging;

    final Animation animation = new EaseInOutQuad(300, 1, Direction.BACKWARDS);
    long lastStateChangeTime = 0;
    boolean lastShouldRender = false;

    private static final List<String> HARMFUL_EFFECTS = Arrays.asList(
            "minecraft:poison", "minecraft:wither", "minecraft:weakness", "minecraft:slowness",
            "minecraft:mining_fatigue", "minecraft:nausea", "minecraft:blindness", "minecraft:hunger",
            "minecraft:levitation", "minecraft:unluck", "minecraft:bad_omen"
    );

    private static final Map<String, String> RUSSIAN_EFFECT_NAMES = new HashMap<>();

    static {
        RUSSIAN_EFFECT_NAMES.put("minecraft:speed", "Скорость");
        RUSSIAN_EFFECT_NAMES.put("minecraft:slowness", "Медлительность");
        RUSSIAN_EFFECT_NAMES.put("minecraft:haste", "Спешка");
        RUSSIAN_EFFECT_NAMES.put("minecraft:mining_fatigue", "Утомление");
        RUSSIAN_EFFECT_NAMES.put("minecraft:strength", "Сила");
        RUSSIAN_EFFECT_NAMES.put("minecraft:instant_health", "Мгновенное лечение");
        RUSSIAN_EFFECT_NAMES.put("minecraft:instant_damage", "Мгновенный урон");
        RUSSIAN_EFFECT_NAMES.put("minecraft:jump_boost", "Прыгучесть");
        RUSSIAN_EFFECT_NAMES.put("minecraft:nausea", "Тошнота");
        RUSSIAN_EFFECT_NAMES.put("minecraft:regeneration", "Регенерация");
        RUSSIAN_EFFECT_NAMES.put("minecraft:resistance", "Сопротивление");
        RUSSIAN_EFFECT_NAMES.put("minecraft:fire_resistance", "Огнестойкость");
        RUSSIAN_EFFECT_NAMES.put("minecraft:water_breathing", "Подводное дыхание");
        RUSSIAN_EFFECT_NAMES.put("minecraft:invisibility", "Невидимость");
        RUSSIAN_EFFECT_NAMES.put("minecraft:blindness", "Слепота");
        RUSSIAN_EFFECT_NAMES.put("minecraft:night_vision", "Ночное зрение");
        RUSSIAN_EFFECT_NAMES.put("minecraft:hunger", "Голод");
        RUSSIAN_EFFECT_NAMES.put("minecraft:weakness", "Слабость");
        RUSSIAN_EFFECT_NAMES.put("minecraft:poison", "Отравление");
        RUSSIAN_EFFECT_NAMES.put("minecraft:wither", "Иссушение");
        RUSSIAN_EFFECT_NAMES.put("minecraft:health_boost", "Увеличение здоровья");
        RUSSIAN_EFFECT_NAMES.put("minecraft:absorption", "Поглощение");
        RUSSIAN_EFFECT_NAMES.put("minecraft:saturation", "Насыщение");
        RUSSIAN_EFFECT_NAMES.put("minecraft:glowing", "Свечение");
        RUSSIAN_EFFECT_NAMES.put("minecraft:levitation", "Левитация");
        RUSSIAN_EFFECT_NAMES.put("minecraft:luck", "Удача");
        RUSSIAN_EFFECT_NAMES.put("minecraft:unluck", "Неудача");
        RUSSIAN_EFFECT_NAMES.put("minecraft:slow_falling", "Медленное падение");
        RUSSIAN_EFFECT_NAMES.put("minecraft:conduit_power", "Сила маяка");
        RUSSIAN_EFFECT_NAMES.put("minecraft:dolphins_grace", "Грация дельфина");
        RUSSIAN_EFFECT_NAMES.put("minecraft:bad_omen", "Дурное предзнаменование");
        RUSSIAN_EFFECT_NAMES.put("minecraft:hero_of_the_village", "Герой деревни");
    }

    private static final String[] ROMAN_NUMERALS = {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack matrix = eventDisplay.getMatrixStack();
        Minecraft mc = Minecraft.getInstance();

        String name = "Potions";
        float x = dragging.getX();
        float y = dragging.getY();

        boolean isChatOpen = mc.currentScreen instanceof ChatScreen;
        boolean shouldRender = mc.player != null && !isChatOpen && !mc.player.getActivePotionEffects().isEmpty();

        if (shouldRender != lastShouldRender) {
            lastStateChangeTime = System.currentTimeMillis();
            lastShouldRender = shouldRender;
        }

        if (isChatOpen) {
            animation.setDirection(Direction.FORWARDS);
        } else if (!shouldRender) {
            animation.setDirection(Direction.BACKWARDS);
            if (animation.getOutput() == 0) return;
        } else {
            animation.setDirection(Direction.FORWARDS);
            if (animation.getOutput() == 0) {
                animation.reset();
                animation.setDirection(Direction.FORWARDS);
            }
        }

        float animationValue = (float) animation.getOutput();
        float timeSinceChange = (System.currentTimeMillis() - lastStateChangeTime) / 1000.0f;
        float scaleFactor = 1.0f;

        if (!isChatOpen && shouldRender && timeSinceChange < 0.3f && !lastShouldRender) {
            scaleFactor = timeSinceChange < 0.15f
                    ? 1.0f + 0.05f * (timeSinceChange / 0.15f)
                    : 1.05f - 0.05f * ((timeSinceChange - 0.15f) / 0.15f);
        } else if (!isChatOpen && !shouldRender && animationValue > 0) {
            scaleFactor = 1.0f - 0.05f * (1.0f - animationValue);
        }

        float yOff = 0;
        float height = 18;
        float maxWidth = 80f;

        for (EffectInstance ef : mc.player.getActivePotionEffects()) {
            Effect effect = ef.getPotion();
            int amp = ef.getAmplifier();
            String ampStr = amp >= 1 && amp <= 9 ? " " + ROMAN_NUMERALS[amp] : "";

            String effectId = Registry.EFFECTS.getKey(effect).toString();
            String nameText = RUSSIAN_EFFECT_NAMES.getOrDefault(effectId, effectId) + ampStr;
            String timeText = EffectUtils.getPotionDurationString(ef, 1);

            float nameWidth = Fonts.tenacity.getWidth(nameText, 8);
            float timeWidth = Fonts.tenacity.getWidth(timeText, 7);
            float localWidth = nameWidth + timeWidth + 20f;

            if (localWidth > maxWidth - 10) {
                maxWidth = localWidth + 20;
            }

            height += 12.5f;
        }

        float scaledWidth = maxWidth * animationValue * scaleFactor;
        float scaledHeight = height * animationValue * scaleFactor;
        float offsetX = x + (maxWidth - scaledWidth) / 2;
        float offsetY = y + (height - scaledHeight) / 2;

        Style style = Initclass.getInstance().getStyleManager().getCurrentStyle();
        Vector4i vector4i = new Vector4i(style.getFirstColor().getRGB(), style.getFirstColor().getRGB(),
                style.getSecondColor().getRGB(), style.getSecondColor().getRGB());

        DisplayUtils.drawRoundedRect(offsetX, offsetY, scaledWidth, scaledHeight, 4 * animationValue * scaleFactor,
                ColorUtils.rgba(0, 0, 0, (int) (200 * animationValue)));

        float scaledImageSize = 10 * animationValue * scaleFactor;

        Fonts.tenacity.drawText(matrix, name, offsetX + 19.5f * animationValue * scaleFactor, offsetY + 4f * animationValue * scaleFactor,
                ColorUtils.setAlpha(-1, (int) (255 * animationValue)), 9 * animationValue * scaleFactor);
        Fonts.nur.drawText(matrix, "B", offsetX + 6.5f * animationValue * scaleFactor, offsetY + 5.5f * animationValue * scaleFactor,
                ColorUtils.setAlpha(ColorUtils.rgb(166, 26, 17), (int) (255 * animationValue)), 8.5f * animationValue * scaleFactor);

        yOff = 0;

        for (EffectInstance ef : mc.player.getActivePotionEffects()) {
            Effect effect = ef.getPotion();
            int amp = ef.getAmplifier();
            String ampStr = amp >= 1 && amp <= 9 ? " " + ROMAN_NUMERALS[amp] : "";

            String effectId = Registry.EFFECTS.getKey(effect).toString();
            String nameText = RUSSIAN_EFFECT_NAMES.getOrDefault(effectId, effectId) + ampStr;
            String timeText = EffectUtils.getPotionDurationString(ef, 1);

            float timeWidth = Fonts.tenacity.getWidth(timeText, 7);

            int textColor = HARMFUL_EFFECTS.contains(effectId)
                    ? ColorUtils.rgba(255, 50, 50, 240)
                    : ColorUtils.rgba(240, 240, 240, 240);

            int adjustedTextColor = ColorUtils.setAlpha(textColor, (int) (240 * animationValue));
            int adjustedTimeColor = ColorUtils.setAlpha(ColorUtils.rgb(166, 26, 17), (int) (255 * animationValue));

            ResourceLocation texture = new ResourceLocation("minecraft",
                    "textures/mob_effect/" + Registry.EFFECTS.getKey(effect).getPath() + ".png");
            mc.getTextureManager().bindTexture(texture);
            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            com.mojang.blaze3d.systems.RenderSystem.color4f(1.0f, 1.0f, 1.0f, animationValue);
            AbstractGui.blit(matrix, (int) (offsetX + 3 * animationValue * scaleFactor),
                    (int) (offsetY + 19.5f * animationValue * scaleFactor + yOff), 0, 0, (int) scaledImageSize, (int) scaledImageSize, (int) scaledImageSize, (int) scaledImageSize);
            com.mojang.blaze3d.systems.RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            com.mojang.blaze3d.systems.RenderSystem.disableBlend();

            Fonts.tenacity.drawText(matrix, nameText, offsetX + 15 * animationValue * scaleFactor,
                    offsetY + 18.5f * animationValue * scaleFactor + yOff, adjustedTextColor, 8 * animationValue * scaleFactor);

            Fonts.tenacity.drawText(matrix, timeText, offsetX + scaledWidth - timeWidth - 5,
                    offsetY + 19f * animationValue * scaleFactor + yOff, adjustedTimeColor, 7 * animationValue * scaleFactor);

            yOff += 12.5f;
        }

        dragging.setWidth(maxWidth);
        dragging.setHeight(height);
    }
}