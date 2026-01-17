package by.algorithm.alpha.api.modules.impl.render;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import by.algorithm.alpha.system.events.WorldEvent;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

@ModuleAnnot(name = "China Hat", type = ModuleCategory.Render, description = "Китайская шляпа или нимб над головой")
public class ChinaHat extends Module {

    public ModeSetting mode = new ModeSetting("Mode", "Мод", "Шляпа", "Нимб");

    public ChinaHat() {
        addSettings(mode);
    }

    @Subscribe
    private void onRender(WorldEvent e) {
        if (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON) return;

        if (mode.is("Шляпа")) {
            hat(e);
        } else if (mode.is("Нимб")) {
            halo(e);
        }
    }

    private void hat(WorldEvent e) {
        float radius = 0.6f;
        GlStateManager.pushMatrix();
        RenderSystem.translated(-mc.getRenderManager().info.getProjectedView().x, -mc.getRenderManager().info.getProjectedView().y, -mc.getRenderManager().info.getProjectedView().z);
        Vector3d interpolated = MathUtil.interpolate(mc.player.getPositionVec(), new Vector3d(mc.player.lastTickPosX, mc.player.lastTickPosY, mc.player.lastTickPosZ), e.getPartialTicks());
        interpolated.y -= 0.05f;
        RenderSystem.translated(interpolated.x, interpolated.y + mc.player.getHeight(), interpolated.z);
        final double yaw = mc.getRenderManager().info.getYaw();
        GL11.glRotatef((float) -yaw, 0f, 1f, 0f);
        RenderSystem.translated(-interpolated.x, -(interpolated.y + mc.player.getHeight()), -interpolated.z);
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(770, 771);
        RenderSystem.shadeModel(7425);
        RenderSystem.lineWidth(3);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        buffer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        buffer.pos(interpolated.x, interpolated.y + mc.player.getHeight() + 0.3, interpolated.z).color(ColorUtils.setAlpha(HUD.getColor(0, 1), 128)).endVertex();
        for (int i = 0; i <= 360; i++) {
            float x = (float) (interpolated.x + MathHelper.sin((float) Math.toRadians(i)) * radius);
            float z = (float) (interpolated.z + -MathHelper.cos((float) Math.toRadians(i)) * radius);
            buffer.pos(x, interpolated.y + mc.player.getHeight(), z).color(ColorUtils.setAlpha(HUD.getColor(i, 1), 128)).endVertex();
        }
        tessellator.draw();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 360; i++) {
            float x = (float) (interpolated.x + MathHelper.sin((float) Math.toRadians(i)) * radius);
            float z = (float) (interpolated.z + -MathHelper.cos((float) Math.toRadians(i)) * radius);
            buffer.pos(x, interpolated.y + mc.player.getHeight(), z).color(ColorUtils.setAlpha(HUD.getColor(i, 1), 255)).endVertex();
        }
        tessellator.draw();

        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.shadeModel(7424);
        GlStateManager.popMatrix();
    }

    private void halo(WorldEvent e) {
        float majorRadius = 0.4f;
        float minorRadius = 0.06f;
        int segments = 32;
        int tubeSegments = 16;

        GlStateManager.pushMatrix();

        RenderSystem.translated(-mc.getRenderManager().info.getProjectedView().x, -mc.getRenderManager().info.getProjectedView().y, -mc.getRenderManager().info.getProjectedView().z);
        Vector3d interpolated = MathUtil.interpolate(mc.player.getPositionVec(), new Vector3d(mc.player.lastTickPosX, mc.player.lastTickPosY, mc.player.lastTickPosZ), e.getPartialTicks());
        interpolated.y -= 0.05f;

        double haloY = interpolated.y + mc.player.getHeight() + 0.3;

        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.disableTexture();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(770, 771);
        RenderSystem.shadeModel(7425);
        RenderSystem.lineWidth(1);

        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        for (int i = 0; i < segments; i++) {
            buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            for (int j = 0; j <= tubeSegments; j++) {
                for (int k = 0; k <= 1; k++) {
                    float theta = (float) (2.0 * Math.PI * (i + k) / segments);
                    float phi = (float) (2.0 * Math.PI * j / tubeSegments);
                    float x = (float) (interpolated.x + (majorRadius + minorRadius * Math.cos(phi)) * Math.cos(theta));
                    float y = (float) (haloY + minorRadius * Math.sin(phi));
                    float z = (float) (interpolated.z + (majorRadius + minorRadius * Math.cos(phi)) * Math.sin(theta));
                    int colorIndex = (int) (theta * 180 / Math.PI);
                    int alpha = (int) (120 + 60 * Math.sin(phi));
                    buffer.pos(x, y, z).color(ColorUtils.setAlpha(HUD.getColor(colorIndex, 1), alpha)).endVertex();
                }
            }
            tessellator.draw();
        }
        for (int ring = 0; ring < 3; ring++) {
            float ringRadius = majorRadius + (ring * 0.05f);
            int ringAlpha = 80 - (ring * 25);

            buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i <= 360; i += 2) {
                float angle = (float) Math.toRadians(i);
                float x = (float) (interpolated.x + Math.cos(angle) * ringRadius);
                float z = (float) (interpolated.z + Math.sin(angle) * ringRadius);
                buffer.pos(x, haloY, z).color(ColorUtils.setAlpha(HUD.getColor(i, 1), ringAlpha)).endVertex();
            }
            tessellator.draw();
        }

        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.shadeModel(7424);
        GlStateManager.popMatrix();
    }
}