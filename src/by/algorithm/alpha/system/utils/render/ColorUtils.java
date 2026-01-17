package by.algorithm.alpha.system.utils.render;

import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import by.algorithm.alpha.api.modules.impl.render.HUD;
import by.algorithm.alpha.system.utils.math.MathUtil;
import java.awt.*;

@UtilityClass
public class ColorUtils {

    public final int green = new Color(64, 255, 64).getRGB();
    public final int yellow = new Color(255, 255, 64).getRGB();
    public final int orange = new Color(255, 128, 32).getRGB();
    public final int red = new Color(255, 64, 64).getRGB();

    // Добавляем класс IntColor для совместимости
    public static class IntColor {
        public static final int WHITE = 0xFFFFFFFF;
        public static final int BLACK = 0xFF000000;
        public static final int RED = 0xFFFF0000;
        public static final int GREEN = 0xFF00FF00;
        public static final int BLUE = 0xFF0000FF;
        public static final int YELLOW = 0xFFFFFF00;
        public static final int CYAN = 0xFF00FFFF;
        public static final int MAGENTA = 0xFFFF00FF;
        public static final int GRAY = 0xFF808080;
        public static final int LIGHT_GRAY = 0xFFC0C0C0;
        public static final int DARK_GRAY = 0xFF404040;

        public static int rgb(int r, int g, int b) {
            return ColorUtils.rgb(r, g, b);
        }

        public static int rgba(int r, int g, int b, int a) {
            return ColorUtils.rgba(r, g, b, a);
        }

        // Метод для извлечения RGB компонентов из int цвета
        public static float[] rgb(int color) {
            return ColorUtils.rgba(color);
        }
    }

    public static int rgb(int r, int g, int b) {
        return 255 << 24 | r << 16 | g << 8 | b;
    }

    public static int rgba(int r, int g, int b, int a) {
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static void setAlphaColor(final int color, final float alpha) {
        final float red = (float) (color >> 16 & 255) / 255.0F;
        final float green = (float) (color >> 8 & 255) / 255.0F;
        final float blue = (float) (color & 255) / 255.0F;
        RenderSystem.color4f(red, green, blue, alpha);
    }

    public static int reAlphaInt(final int color,
                                 final int alpha) {
        return (MathHelper.clamp(alpha, 0, 255) << 24) | (color & 16777215);
    }

    public static int getColor(int index) {
        return HUD.getColor(index);
    }

    public static void setColor(int color) {
        setAlphaColor(color, (float) (color >> 24 & 255) / 255.0F);
    }

    public static int toColor(String hexColor) {
        int argb = Integer.parseInt(hexColor.substring(1), 16);
        return setAlpha(argb, 255);
    }

    public static int setAlpha(int color, int alpha) {
        return (color & 0x00ffffff) | (alpha << 24);
    }

    public static float[] rgba(final int color) {
        return new float[] {
                (color >> 16 & 255) / 255f,
                (color >> 8 & 255) / 255f,
                (color & 255) / 255f,
                (color >> 24 & 255) / 255f
        };
    }

    public static int gradient(int start, int end, int index, int speed) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        angle = (angle > 180 ? 360 - angle : angle) + 180;
        int color = interpolate(start, end, MathHelper.clamp(angle / 180f - 1, 0, 1));
        float[] hs = rgba(color);
        float[] hsb = Color.RGBtoHSB((int) (hs[0] * 255), (int) (hs[1] * 255), (int) (hs[2] * 255), null);

        hsb[1] *= 1.5F;
        hsb[1] = Math.min(hsb[1], 1.0f);

        return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
    }

    // Добавляем в ColorUtils.java
    public static int darkRedGradient(int index, int speed) {
        int startColor = rgba(40, 0, 0, 255); // Темно-красный
        int endColor = rgba(80, 10, 10, 255); // Средне-красный
        return gradient(startColor, endColor, index, speed);
    }

    public static int redAccentGradient(int index, int speed) {
        int startColor = rgba(180, 30, 30, 255); // Ярко-красный
        int endColor = rgba(120, 10, 10, 255); // Темно-красный
        return gradient(startColor, endColor, index, speed);
    }

    public static int interpolate(int start, int end, float value) {
        float[] startColor = rgba(start);
        float[] endColor = rgba(end);

        return rgba((int) MathUtil.interpolate(startColor[0] * 255, endColor[0] * 255, value),
                (int) MathUtil.interpolate(startColor[1] * 255, endColor[1] * 255, value),
                (int) MathUtil.interpolate(startColor[2] * 255, endColor[2] * 255, value),
                (int) MathUtil.interpolate(startColor[3] * 255, endColor[3] * 255, value));
    }

    public static int getRed(final int hex) {
        return hex >> 16 & 255;
    }

    public static int getGreen(final int hex) {
        return hex >> 8 & 255;
    }

    public static int getBlue(final int hex) {
        return hex & 255;
    }

    public static int getAlpha(final int hex) {
        return hex >> 24 & 255;
    }

    public static int interpolateColor(int color1, int color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));

        int red1 = getRed(color1);
        int green1 = getGreen(color1);
        int blue1 = getBlue(color1);
        int alpha1 = getAlpha(color1);

        int red2 = getRed(color2);
        int green2 = getGreen(color2);
        int blue2 = getBlue(color2);
        int alpha2 = getAlpha(color2);

        int interpolatedRed = interpolateInt(red1, red2, amount);
        int interpolatedGreen = interpolateInt(green1, green2, amount);
        int interpolatedBlue = interpolateInt(blue1, blue2, amount);
        int interpolatedAlpha = interpolateInt(alpha1, alpha2, amount);

        return (interpolatedAlpha << 24) | (interpolatedRed << 16) | (interpolatedGreen << 8) | interpolatedBlue;
    }

    public static Double interpolateD(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return interpolateD(oldValue, newValue, (float) interpolationValue).intValue();
    }
}