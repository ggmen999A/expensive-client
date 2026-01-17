package by.algorithm.alpha.system.visuals.hud.impl;

import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.system.events.EventDisplay;
import by.algorithm.alpha.system.utils.animations.Animation;
import by.algorithm.alpha.system.utils.animations.Direction;
import by.algorithm.alpha.system.utils.animations.impl.EaseInOutQuad;
import by.algorithm.alpha.system.utils.client.KeyStorage;
import by.algorithm.alpha.system.utils.dragable.Dragging;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.Scissor;
import by.algorithm.alpha.system.visuals.hud.ElementRenderer;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ChatScreen;

import java.util.HashMap;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ActiveBinds implements ElementRenderer {

    final Dragging dragging;
    final Animation animation = new EaseInOutQuad(300, 1, Direction.BACKWARDS);
    float width;
    float height;
    long lastStateChangeTime = 0;
    boolean lastShouldRender = false;
    private static final float MAX_WIDTH = 140f;
    private static final Map<String, String> KEY_ABBREVIATIONS = new HashMap<String, String>() {{
        put("BACKSLASH", "\\");
        put("ESCAPE", "ESC");
        put("SEMICOLON", ";");
        put("APOSTROPHE", "'");
        put("GRAVE_ACCENT", "`");
        put("LEFT_BRACKET", "[");
        put("RIGHT_BRACKET", "]");
        put("COMMA", ",");
        put("PERIOD", ".");
        put("SLASH", "/");
        put("CAPS_LOCK", "CAPS");
        put("LEFT_SHIFT", "LSHIFT");
        put("RIGHT_SHIFT", "RSHIFT");
        put("LEFT_CONTROL", "LCTRL");
        put("RIGHT_CONTROL", "RCTRL");
        put("LEFT_ALT", "LALT");
        put("RIGHT_ALT", "RALT");
        put("LEFT_SUPER", "LWIN");
        put("RIGHT_SUPER", "RWIN");
        put("SPACE", "SPACE");
        put("ENTER", "ENTER");
        put("BACKSPACE", "BKSP");
        put("DELETE", "DEL");
        put("INSERT", "INS");
        put("HOME", "HOME");
        put("END", "END");
        put("PAGE_UP", "PgUp");
        put("PAGE_DOWN", "PgDn");
        put("UP", "↑");
        put("DOWN", "↓");
        put("LEFT", "←");
        put("RIGHT", "→");
        put("TAB", "TAB");
        put("PRINT_SCREEN", "PrtSc");
        put("SCROLL_LOCK", "ScrLk");
        put("PAUSE", "Pause");
        put("MENU", "Menu");
        put("NUMPAD_0", "Num0");
        put("NUMPAD_1", "Num1");
        put("NUMPAD_2", "Num2");
        put("NUMPAD_3", "Num3");
        put("NUMPAD_4", "Num4");
        put("NUMPAD_5", "Num5");
        put("NUMPAD_6", "Num6");
        put("NUMPAD_7", "Num7");
        put("NUMPAD_8", "Num8");
        put("NUMPAD_9", "Num9");
        put("NUMPAD_DECIMAL", "Num.");
        put("NUMPAD_DIVIDE", "Num/");
        put("NUMPAD_MULTIPLY", "Num*");
        put("NUMPAD_SUBTRACT", "Num-");
        put("NUMPAD_ADD", "Num+");
        put("NUMPAD_ENTER", "NumEnter");
        put("NUMPAD_EQUAL", "Num=");
    }};
    private String getAbbreviatedKeyName(String originalKey) {
        return KEY_ABBREVIATIONS.getOrDefault(originalKey, originalKey);
    }
    private String truncateText(String text, float maxWidth, float fontSize) {
        if (Fonts.tenacity.getWidth(text, fontSize) <= maxWidth) {
            return text;
        }

        String truncated = text;
        while (Fonts.tenacity.getWidth(truncated + "...", fontSize) > maxWidth && truncated.length() > 1) {
            truncated = truncated.substring(0, truncated.length() - 1);
        }
        return truncated + "...";
    }

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();
        float posX = dragging.getX();
        float posY = dragging.getY();
        String name = "KeyBinds";

        for (Module f : Initclass.getInstance().getFunctionRegistry().getFunctions()) {
            f.getAnimation().update();
        }

        boolean shouldRender = mc.currentScreen instanceof ChatScreen ||
                Initclass.getInstance().getFunctionRegistry().getFunctions().stream()
                        .anyMatch(f -> f.getAnimation().getValue() > 0 && f.getBind() != 0);

        if (shouldRender != lastShouldRender) {
            lastStateChangeTime = System.currentTimeMillis();
            lastShouldRender = shouldRender;
        }

        if (!shouldRender) {
            animation.setDirection(Direction.BACKWARDS);
            if (animation.getOutput() == 0) {
                return;
            }
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

        if (shouldRender && timeSinceChange < 0.3f) {
            if (timeSinceChange < 0.15f) {
                scaleFactor = 1.0f + 0.05f * (timeSinceChange / 0.15f);
            } else {
                scaleFactor = 1.05f - 0.05f * ((timeSinceChange - 0.15f) / 0.15f);
            }
        } else if (!shouldRender && animationValue > 0) {
            scaleFactor = 1.0f - 0.05f * (1.0f - animationValue);
        }

        height = 18;
        float maxWidth = 80f;
        float yOff = 0;
        for (Module f : Initclass.getInstance().getFunctionRegistry().getFunctions()) {
            if (!(f.getAnimation().getValue() > 0 && f.getBind() != 0)) continue;

            String nameText = f.getName();
            float nameWidth = Fonts.tenacity.getWidth(nameText, 8);

            String originalBindText = KeyStorage.getKey(f.getBind());
            String abbreviatedBindText = getAbbreviatedKeyName(originalBindText);
            String bindText = "[" + abbreviatedBindText + "]";
            float bindWidth = Fonts.tenacity.getWidth(bindText, 7);

            float localWidth = 10 + 5 + nameWidth + bindWidth + 5;

            if (localWidth > MAX_WIDTH - 10) {
                maxWidth = MAX_WIDTH;
            } else if (localWidth > maxWidth - 10) {
                maxWidth = localWidth + 10;
            }

            height += 12.5f;
        }

        float scaledWidth = maxWidth * animationValue * scaleFactor;
        float scaledHeight = height * animationValue * scaleFactor;
        float offsetX = posX + (maxWidth - scaledWidth) / 2;
        float offsetY = posY + (height - scaledHeight) / 2;

        DisplayUtils.drawRoundedRect(offsetX, offsetY, scaledWidth, scaledHeight, 4 * animationValue * scaleFactor, ColorUtils.rgba(0, 0, 0, (int) (200 * animationValue)));

        Scissor.push();
        Scissor.setFromComponentCoordinates(offsetX, offsetY, scaledWidth, scaledHeight);

        float scaledImageSize = 10 * animationValue * scaleFactor;

        Fonts.tenacity.drawText(ms, name, offsetX + 19.5f * animationValue * scaleFactor, offsetY + 4f * animationValue * scaleFactor,
                ColorUtils.setAlpha(-1, (int) (255 * animationValue)), 9 * animationValue * scaleFactor);
        Fonts.nur.drawText(ms, "A", offsetX + 6.5f * animationValue * scaleFactor, offsetY + 5.5f * animationValue * scaleFactor,
                ColorUtils.setAlpha(ColorUtils.rgb(166, 26, 17), (int) (255 * animationValue)), 8.5f * animationValue * scaleFactor);

        yOff = 0;
        for (Module f : Initclass.getInstance().getFunctionRegistry().getFunctions()) {
            if (!(f.getAnimation().getValue() > 0 && f.getBind() != 0)) continue;
            String nameText = f.getName();
            String originalBindText = KeyStorage.getKey(f.getBind());
            String abbreviatedBindText = getAbbreviatedKeyName(originalBindText);
            String bindText = "[" + abbreviatedBindText + "]";
            float fontSize = 8 * animationValue * scaleFactor;
            float bindFontSize = 7 * animationValue * scaleFactor;
            float bindWidth = Fonts.tenacity.getWidth(bindText, bindFontSize);
            float minGap = 10 * animationValue * scaleFactor;
            float availableNameWidth = scaledWidth - bindWidth - minGap - 7.5f;
            String displayNameText = truncateText(nameText, availableNameWidth, fontSize);

            float nameWidth = Fonts.tenacity.getWidth(displayNameText, fontSize);
            float nameX = offsetX + 4.5f;
            float desiredBindX = offsetX + scaledWidth - bindWidth - 3 * animationValue * scaleFactor;
            float minBindX = nameX + nameWidth + minGap;
            float bindX = Math.max(desiredBindX, minBindX);

            Fonts.tenacity.drawText(ms, displayNameText, nameX, offsetY + 19.5f * animationValue * scaleFactor + yOff,
                    ColorUtils.rgba(255, 255, 255, (int) (255 * f.getAnimation().getValue() * animationValue)),
                    fontSize);
            Fonts.tenacity.drawText(ms, bindText, bindX - 1, offsetY + 20.5f * animationValue * scaleFactor + yOff,
                    ColorUtils.setAlpha(ColorUtils.rgb(166, 26, 17), (int) (255 * f.getAnimation().getValue() * animationValue)),
                    bindFontSize);

            yOff += 12.5f;
        }

        Scissor.unset();
        Scissor.pop();

        width = Math.max(maxWidth, 80);

        if (shouldRender) {
            dragging.setWidth(width);
            dragging.setHeight(height);
        }
    }
}