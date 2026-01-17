package by.algorithm.alpha.system.visuals.hud.impl;

import by.algorithm.alpha.system.events.EventDisplay;
import by.algorithm.alpha.system.utils.client.ServerUtil;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.visuals.hud.ElementRenderer;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import com.mojang.blaze3d.matrix.MatrixStack;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.ResourceLocation;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class WaterMark implements ElementRenderer {
    final ResourceLocation logo = new ResourceLocation("expensive/images/hud/25252525.png");
    float interpolatedFps = 0;
    float interpolatedPing = 0;
    float interpolatedBps = 0;
    float previousFps = 0;
    float previousPing = 0;
    float previousBps = 0;
    final float interpolationFactor = 0.08f;

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack matrix = eventDisplay.getMatrixStack();
        float currentFps = mc.debugFPS;
        float currentPing = ServerUtil.calculatePing();
        String bpsString = ServerUtil.calculateBPS().replace(",", ".");
        float currentBps = Float.parseFloat(bpsString);
        interpolatedFps = lerp(interpolatedFps, currentFps, interpolationFactor);
        interpolatedPing = lerp(interpolatedPing, currentPing, interpolationFactor);
        interpolatedBps = lerp(interpolatedBps, currentBps, interpolationFactor);
        String calcfps = String.format("%.0f", interpolatedFps);
        String calcping = String.format("%.0f ms", interpolatedPing);
        String calcbps = String.format("%.1f BPS", interpolatedBps);
        String ipcalc = ServerUtil.serverIP();
        String coordscalc = (int) mc.player.getPosX() + " , " + (int) mc.player.getPosY() + " , " + (int) mc.player.getPosZ();
        String username = "Solth";
        String usericon = "E";
        String fpsicon = "P";
        String bpsicon = "L";
        String pingicon = "K";
        String coordsicon = "i";
        String ipicon = "b";
        String timeicon = "O";
        StringBuilder calctime = new StringBuilder("");
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        calctime.append(time.format(formatter));
        int colorated = ColorUtils.rgb(166, 26, 17);
        int defaultcolor = ColorUtils.rgba(0,0,0,200);
        float usernamewidth = Fonts.tenacity.getWidth(username, 7.8f);
        float timewidth = Fonts.tenacity.getWidth(calctime + "", 7.8f);
        float fpswidth = Fonts.tenacity.getWidth(calcfps, 7.8f);
        float pingwidth = Fonts.tenacity.getWidth(calcping, 7.8f);
        float coordswidth = Fonts.tenacity.getWidth(coordscalc, 7.8f);
        float ipwidth = Fonts.tenacity.getWidth(ipcalc, 7.8f);
        float bpswidth = Fonts.tenacity.getWidth(calcbps, 7.8f);
        float x = 5;
        float y = 5;
        float width = 100.8f;
        float height = 15.8f;
        DisplayUtils.drawRoundedRect(x, y, width / 2 - 34, height, 4, defaultcolor);
        DisplayUtils.drawRoundedRect(x + 20, y, width + (usernamewidth + timewidth + fpswidth + ipwidth) - 23, height, 4, defaultcolor);
        DisplayUtils.drawRoundedRect(x, y + 17, (pingwidth + 18), height, 4, defaultcolor);
        DisplayUtils.drawRoundedRect(x + (pingwidth + 20), y + 17, (coordswidth + 15), height, 4, defaultcolor);
        DisplayUtils.drawRoundedRect(x + (pingwidth + coordswidth + 37), y + 17, (bpswidth + 17), height, 4, defaultcolor);
        DisplayUtils.drawImage(logo, x + 3, y + 3, 9.8f, 9.8f, colorated);
        Fonts.nur.drawText(matrix, usericon, x + 24, y + 4, colorated, 8.3f);
        Fonts.tenacity.drawText(matrix, username, x + 35, y + 3.3f, -1, 7.8f);
        DisplayUtils.drawCircle(x + 40 + (usernamewidth), y + 7.5f, 4.8f, colorated);
        Fonts.nur.drawText(matrix, timeicon, x + 44 + (usernamewidth), y + 4, colorated, 8.3f);
        Fonts.tenacity.drawText(matrix, calctime + "", x + 54 + (usernamewidth), y + 3.3f, -1, 7.8f);
        DisplayUtils.drawCircle(x + 59 + (usernamewidth + timewidth), y + 7.5f, 4.8f, colorated);
        Fonts.nur.drawText(matrix, fpsicon, x + 63 + (usernamewidth + timewidth), y + 4, colorated, 8.3f);
        Fonts.tenacity.drawText(matrix, calcfps, x + 73 + (usernamewidth + timewidth), y + 3.3f, -1, 7.8f);
        DisplayUtils.drawCircle(x + 59 + (usernamewidth + timewidth), y + 7.5f, 4.8f, colorated);
        Fonts.icos.drawText(matrix, ipicon, x + 83 + (usernamewidth + timewidth + fpswidth), y + 4, colorated, 9.8f);
        Fonts.tenacity.drawText(matrix, ipcalc, x + 93 + (usernamewidth + timewidth + fpswidth), y + 3.3f, -1, 7.8f);
        DisplayUtils.drawCircle(x + 78.5f + (usernamewidth + timewidth + fpswidth), y + 7.5f, 4.8f, colorated);
        Fonts.nur.drawText(matrix, pingicon, x + 3, y + 21, colorated, 8.8f);
        Fonts.tenacity.drawText(matrix, calcping, x + 14, y + 21, -1, 7.8f);
        Fonts.icos.drawText(matrix, coordsicon, x + (pingwidth + 23), y + 21, colorated, 9.8f);
        Fonts.tenacity.drawText(matrix, coordscalc, x + (pingwidth + 32), y + 21, -1, 7.8f);
        Fonts.nur.drawText(matrix, bpsicon, x + (pingwidth + coordswidth + 41), y + 21, colorated, 8.8f);
        Fonts.tenacity.drawText(matrix, calcbps, x + (pingwidth + coordswidth + 50), y + 21, -1, 7.8f);
        previousFps = currentFps;
        previousPing = currentPing;
        previousBps = currentBps;
    }

    private float lerp(float from, float to, float factor) {
        return from + (to - from) * factor;
    }
}