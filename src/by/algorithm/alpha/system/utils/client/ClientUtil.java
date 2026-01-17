package by.algorithm.alpha.system.utils.client;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.SUpdateBossInfoPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import org.lwjgl.glfw.GLFW;

import javax.sound.sampled.*;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.UUID;

import static java.lang.Math.signum;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

@UtilityClass
public class ClientUtil implements IMinecraft {

    private static Clip currentClip = null;
    private static boolean pvpMode;
    private static UUID uuid;

    public void updateBossInfo(SUpdateBossInfoPacket packet) {
        if (packet.getOperation() == SUpdateBossInfoPacket.Operation.ADD) {
            if (StringUtils.stripControlCodes(packet.getName().getString()).toLowerCase().contains("pvp")) {
                pvpMode = true;
                uuid = packet.getUniqueId();
            }
        } else if (packet.getOperation() == SUpdateBossInfoPacket.Operation.REMOVE) {
            if (packet.getUniqueId().equals(uuid))
                pvpMode = false;
        }
    }
    public boolean isConnectedToServer(String ip) {
        return mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP != null && mc.getCurrentServerData().serverIP.contains(ip);
    }
    public boolean isPvP() {
        return pvpMode;
    }

    public void playSound(String sound, float value, boolean nonstop) {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
        }
        try {
            currentClip = AudioSystem.getClip();
            InputStream is = mc.getResourceManager().getResource(new ResourceLocation("expensive/sounds/" + sound + ".wav")).getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bis);
            if (audioInputStream == null) {
                System.out.println("Sound not found!");
                return;
            }

            currentClip.open(audioInputStream);
            currentClip.start();
            FloatControl floatControl = (FloatControl) currentClip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = floatControl.getMinimum();
            float max = floatControl.getMaximum();
            float volumeInDecibels = (float) (min * (1 - (value / 100.0)) + max * (value / 100.0));
            floatControl.setValue(volumeInDecibels);
            if (nonstop) {
                currentClip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        currentClip.setFramePosition(0);
                        currentClip.start();
                    }
                });
            }
        } catch (Exception exception) {
            // Обработка исключения
            exception.printStackTrace();
        }
    }

    public void stopSound() {
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
        }
    }

    public int calc(int value) {
        MainWindow rs = mc.getMainWindow();
        return (int) (value * rs.getGuiScaleFactor() / 2);
    }

    public static int fixHP(String healthString) {
        if (healthString == null || healthString.isEmpty()) {
            return 0;
        }

        // Remove all non-digit characters except minus and dot (for negative numbers and decimals if needed)
        String numericValue = healthString.replaceAll("[^0-9.-]", "").trim();

        try {
            // Parse as double first to handle decimal numbers if they exist
            double health = Double.parseDouble(numericValue);
            // Convert to int (rounding down)
            return (int) health;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String[] parseScoreboardInfo(String scoreString) {
        if (scoreString == null || scoreString.trim().isEmpty()) {
            return null;
        }

        String[] parts = scoreString.split("\\s+");
        if (parts.length < 4) {
            return null;
        }

        try {
            StringBuilder nickname = new StringBuilder();
            for (int i = 0; i < parts.length - 3; i++) {
                if (i > 0) nickname.append(" ");
                nickname.append(parts[i]);
            }
            String value1 = parts[parts.length - 3];
            String value2 = parts[parts.length - 2];
            String type = parts[parts.length - 1];
            Integer.parseInt(value1);
            Integer.parseInt(value2);

            return new String[]{nickname.toString(), value1, value2, type};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Vec2i getMouse(int mouseX, int mouseY) {
        return new Vec2i((int) (mouseX * Minecraft.getInstance().getMainWindow().getGuiScaleFactor() / 2), (int) (mouseY * Minecraft.getInstance().getMainWindow().getGuiScaleFactor() / 2));
    }

    public boolean ctrlIsDown() {
        return GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    public static String pasteString() {
        return GLFW.glfwGetClipboardString(mc.getMainWindow().getHandle());
    }

}
