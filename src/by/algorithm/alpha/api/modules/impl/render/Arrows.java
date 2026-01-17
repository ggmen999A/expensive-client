package by.algorithm.alpha.api.modules.impl.render;

import by.algorithm.alpha.api.command.friends.FriendStorage;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.system.events.EventDisplay;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.utils.math.Vector4i;
import by.algorithm.alpha.system.utils.player.PlayerUtils;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.font.Fonts;
import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@ModuleAnnot(name = "Arrows", type = ModuleCategory.Render, description = "Стрелочки до игроков")
public class Arrows extends Module {

    private final BooleanSetting showNickname = new BooleanSetting("Писать никнейм", false);
    private final BooleanSetting showDistance = new BooleanSetting("Писать расстояние", false);
    private final ModeSetting textPosition = new ModeSetting("Позиция текста", "Сверху", "Сверху", "Снизу");

    public float animationStep;

    public Arrows() {
        addSettings(showNickname, showDistance, textPosition);
    }
    private float lastYaw;
    private float lastPitch;
    private float animatedYaw;
    private float animatedPitch;

    @Subscribe
    public void onDisplay(EventDisplay e) {
        if (mc.player == null || mc.world == null || e.getType() != EventDisplay.Type.PRE) {
            return;
        }

        float size = 60;

        if (mc.currentScreen instanceof InventoryScreen) {
            size += 100;
        }
        animationStep = MathUtil.fast(animationStep, size, 6);

        PointOfView pointOfView = mc.gameSettings.getPointOfView();

        // Работаем как в первом, так и в третьем лице
        if (pointOfView == PointOfView.FIRST_PERSON ||
                pointOfView == PointOfView.THIRD_PERSON_BACK ||
                pointOfView == PointOfView.THIRD_PERSON_FRONT) {

            MatrixStack matrixStack = new MatrixStack();
            for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
                if (!PlayerUtils.isNameValid(player.getNameClear()) || mc.player == player)
                    continue;

                double x = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * mc.getRenderPartialTicks()
                        - mc.getRenderManager().info.getProjectedView().getX();
                double z = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * mc.getRenderPartialTicks()
                        - mc.getRenderManager().info.getProjectedView().getZ();

                // Получаем базовый угол камеры
                float cameraYaw = mc.getRenderManager().info.getYaw();

                double cos = MathHelper.cos((float) (cameraYaw * (Math.PI * 2 / 360)));
                double sin = MathHelper.sin((float) (cameraYaw * (Math.PI * 2 / 360)));
                double rotY = -(z * cos - x * sin);
                double rotX = -(x * cos + z * sin);
                float angle = (float) (Math.atan2(rotY, rotX) * 180 / Math.PI);

                // Корректировка направления стрелок в зависимости от вида камеры
                if (pointOfView == PointOfView.THIRD_PERSON_FRONT) {
                    // Для вида спереди поворачиваем стрелки на 180 градусов
                    // так как камера смотрит в противоположную сторону
                    angle += 180f;
                }

                double x2 = animationStep * MathHelper.cos((float) Math.toRadians(angle)) + window.getScaledWidth() / 2f;
                double y2 = animationStep * MathHelper.sin((float) Math.toRadians(angle)) + window.getScaledHeight() / 2f;
                x2 += animatedYaw;
                y2 += animatedPitch;

                int color = FriendStorage.isFriend(player.getGameProfile().getName()) ? FriendStorage.getColor() : ColorUtils.rgb(255,255,255);

                GlStateManager.pushMatrix();
                GlStateManager.disableBlend();
                GlStateManager.translated(x2, y2, 0);
                GlStateManager.rotatef(angle, 0, 0, 1);
                drawTriangle(-4, -1F, 6F, 9F, new Color(0, 0, 0, 32));
                drawTriangle(-3F, 0F, 5F, 7F, new Color(color));
                GlStateManager.enableBlend();
                GlStateManager.popMatrix();

                if (showNickname.get() || showDistance.get()) {
                    renderPlayerInfo(matrixStack, player, x2, y2, angle, color);
                }
            }
        }
        lastYaw = mc.player.rotationYaw;
        lastPitch = mc.player.rotationPitch;
    }

    private void renderPlayerInfo(MatrixStack matrixStack, AbstractClientPlayerEntity player, double arrowX, double arrowY, float angle, int color) {
        if (Fonts.gilroy[14] == null) return;

        double distance = mc.player.getDistance(player);
        String nickname = player.getNameClear();
        String distanceText = " [" + String.format("%.1f", distance) + "m]";

        float textDistance = 25f;

        float textAngle;
        if (textPosition.is("Сверху")) {
            textAngle = angle;
        } else {
            textAngle = angle + 180f;
        }

        float baseTextX = (float) arrowX + textDistance * MathHelper.cos((float) Math.toRadians(textAngle));
        float baseTextY = (float) arrowY + textDistance * MathHelper.sin((float) Math.toRadians(textAngle));

        if (showNickname.get() && showDistance.get()) {
            float totalWidth = Fonts.gilroy[14].getWidth(nickname + distanceText);
            float startX = baseTextX - totalWidth / 2f;

            Fonts.gilroy[14].drawStringWithOutline(matrixStack, nickname, startX, baseTextY, color);

            float nicknameWidth = Fonts.gilroy[14].getWidth(nickname);
            int distanceColor = getDistanceColor(distance);
            Fonts.gilroy[14].drawStringWithOutline(matrixStack, distanceText, startX + nicknameWidth, baseTextY, distanceColor);

        } else if (showNickname.get()) {
            float nicknameWidth = Fonts.gilroy[14].getWidth(nickname);
            float nicknameX = baseTextX - nicknameWidth / 2f;
            Fonts.gilroy[14].drawStringWithOutline(matrixStack, nickname, nicknameX, baseTextY, color);

        } else if (showDistance.get()) {
            String onlyDistance = "[" + String.format("%.1f", distance) + "m]";
            float distanceWidth = Fonts.gilroy[14].getWidth(onlyDistance);
            float distanceX = baseTextX - distanceWidth / 2f;
            int distanceColor = getDistanceColor(distance);
            Fonts.gilroy[14].drawStringWithOutline(matrixStack, onlyDistance, distanceX, baseTextY, distanceColor);
        }
    }

    private int getDistanceColor(double distance) {
        if (distance <= 10) {
            return ColorUtils.rgb(255, 85, 85);
        } else if (distance <= 25) {
            return ColorUtils.rgb(255, 170, 85);
        } else if (distance <= 50) {
            return ColorUtils.rgb(255, 255, 85);
        } else {
            return ColorUtils.rgb(255, 255, 255);
        }
    }

    public static void drawTriangle(float x, float y, float width, float height, Color color) {
        DisplayUtils.drawImageAlpha(new ResourceLocation("expensive/images/triangle.png"), -8.0F, -9.0F, 18, 18, new Vector4i(ColorUtils.setAlpha(HUD.getColor(0, 1.0F), 125), ColorUtils.setAlpha(HUD.getColor(90, 1.0F), 125), ColorUtils.setAlpha(HUD.getColor(180, 1.0F), 125), ColorUtils.setAlpha(HUD.getColor(270, 1.0F), 125)));
        GL11.glPushMatrix();
        GL11.glPopMatrix();
    }
}