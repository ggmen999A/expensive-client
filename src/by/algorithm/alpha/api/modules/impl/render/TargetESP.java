package by.algorithm.alpha.api.modules.impl.render;

import static com.mojang.blaze3d.platform.GlStateManager.GL_QUADS;
import static com.mojang.blaze3d.systems.RenderSystem.depthMask;
import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR_TEX;

import by.algorithm.alpha.api.modules.impl.combat.AttackAura;
import com.mojang.blaze3d.matrix.MatrixStack;
import by.algorithm.alpha.system.events.EventDisplay;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.system.events.WorldEvent;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.system.utils.animations.Animation;
import by.algorithm.alpha.system.utils.animations.Direction;
import by.algorithm.alpha.system.utils.animations.impl.DecelerateAnimation;
import by.algorithm.alpha.system.utils.animations.impl.EaseBackIn;
import by.algorithm.alpha.system.utils.math.MathUtil;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.system.events.EventDisplay.Type;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.system.utils.math.Vector4i;
import by.algorithm.alpha.system.utils.player.ProjectionUtil;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;


@ModuleAnnot(name = "TargetESP", type = ModuleCategory.Render, description = "Обводка персонажа при атаке AttackAura")
public class TargetESP extends Module {
    public ModeSetting mode = new ModeSetting("Мод", "Ромб", "Ромб", "Призраки");
    public BooleanSetting animka = new BooleanSetting("Статичный", true);
    private final Animation alpha = new DecelerateAnimation(600, 255);
    private final Animation scaleAnimation = new EaseBackIn(400, 1.0, 1.5f);
    private final AttackAura killAura;
    private LivingEntity currentTarget;
    private static long startTime = System.currentTimeMillis();
    private long lastTime = System.currentTimeMillis();

    public TargetESP(AttackAura KillAura) {
        this.killAura = KillAura;
        addSettings(mode, animka);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        boolean bl = (Initclass.getInstance().getFunctionRegistry().getKillAura().isState());
        if (killAura.getTarget() != null) {
            currentTarget = killAura.getTarget();
        }

        boolean hasTarget = bl && killAura.getTarget() != null;
        this.alpha.setDirection(hasTarget ? Direction.FORWARDS : Direction.BACKWARDS);
        this.scaleAnimation.setDirection(hasTarget ? Direction.FORWARDS : Direction.BACKWARDS);
    }

    @Subscribe
    public void onRender(WorldEvent e) {
        if (this.alpha.finished(Direction.BACKWARDS)) {
            return;
        }
        if (mode.is("Призраки")) {
            if (this.currentTarget != null && this.currentTarget != mc.player) {
                MatrixStack ms = new MatrixStack();
                ms.push();
                RenderSystem.pushMatrix();
                RenderSystem.disableLighting();
                depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.shadeModel(7425);
                RenderSystem.disableCull();
                RenderSystem.disableAlphaTest();
                RenderSystem.blendFuncSeparate(770, 1, 0, 1);
                double x = currentTarget.getPosX();
                double y = currentTarget.getPosY() + currentTarget.getHeight() / 2f;
                double z = currentTarget.getPosZ();
                double radius = 0.67;
                float speed = 45;
                float size = 0.4f;
                double distance = 19;
                int lenght = 20;
                int maxAlpha = (int) (255 * this.alpha.getOutput() / 255.0);
                int alphaFactor = 15;
                ActiveRenderInfo camera = mc.getRenderManager().info;
                ms.translate(-mc.getRenderManager().info.getProjectedView().getX(),
                        -mc.getRenderManager().info.getProjectedView().getY(),
                        -mc.getRenderManager().info.getProjectedView().getZ());

                Vector3d interpolated = MathUtil.interpolate(currentTarget.getPositionVec(), new Vector3d(currentTarget.lastTickPosX, currentTarget.lastTickPosY, currentTarget.lastTickPosZ), e.getPartialTicks());
                interpolated.y += 0.75f;
                ms.translate(interpolated.x + 0.2f, interpolated.y + 0.5f, interpolated.z);

                // Apply scale animation
                float scale = (float) this.scaleAnimation.getOutput();
                ms.scale(scale, scale, scale);

                mc.getTextureManager().bindTexture(new ResourceLocation("expensive/images/glow.png"));
                for (int i = 0; i < lenght; i++) {
                    Quaternion r = camera.getRotation().copy();
                    buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                    double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                    double s = Math.sin(angle) * radius;
                    double c = Math.cos(angle) * radius;
                    ms.translate(s, (c), -c);
                    ms.translate(-size / 2f, -size / 2f, 0);
                    ms.rotate(r);
                    ms.translate(size / 2f, size / 2f, 0);
                    // Red theme colors
                    int color = ColorUtils.rgb(255, 140, 140); // Light red
                    int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                    buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(DisplayUtils.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(DisplayUtils.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(DisplayUtils.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(DisplayUtils.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                    tessellator.draw();
                    ms.translate(-size / 2f, -size / 2f, 0);
                    r.conjugate();
                    ms.rotate(r);
                    ms.translate(size / 2f, size / 2f, 0);
                    ms.translate(-(s), -(c), (c));
                }
                for (int i = 0; i < lenght; i++) {
                    Quaternion r = camera.getRotation().copy();
                    buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                    double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                    double s = Math.sin(angle) * radius;
                    double c = Math.cos(angle) * radius;
                    ms.translate(-s, s, -c);
                    ms.translate(-size / 2f, -size / 2f, 0);
                    ms.rotate(r);
                    ms.translate(size / 2f, size / 2f, 0);
                    // Red theme colors with darker variation
                    int color = ColorUtils.rgb(220, 100, 100); // Medium red
                    int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                    buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(DisplayUtils.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(DisplayUtils.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(DisplayUtils.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(DisplayUtils.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                    tessellator.draw();
                    ms.translate(-size / 2f, -size / 2f, 0);
                    r.conjugate();
                    ms.rotate(r);
                    ms.translate(size / 2f, size / 2f, 0);
                    ms.translate((s), -(s), (c));
                }
                for (int i = 0; i < lenght; i++) {
                    Quaternion r = camera.getRotation().copy();
                    buffer.begin(GL_QUADS, POSITION_COLOR_TEX);
                    double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / (speed);
                    double s = Math.sin(angle) * radius;
                    double c = Math.cos(angle) * radius;
                    ms.translate(-(s), -(s), (c));
                    ms.translate(-size / 2f, -size / 2f, 0);
                    ms.rotate(r);
                    ms.translate(size / 2f, size / 2f, 0);
                    // Red theme colors with bright variation
                    int color = ColorUtils.rgb(255, 160, 160); // Bright red
                    int alpha = MathHelper.clamp(maxAlpha - (i * alphaFactor), 0, maxAlpha);
                    buffer.pos(ms.getLast().getMatrix(), 0, -size, 0).color(DisplayUtils.reAlphaInt(color, alpha)).tex(0, 0).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), -size, -size, 0).color(DisplayUtils.reAlphaInt(color, alpha)).tex(0, 1).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), -size, 0, 0).color(DisplayUtils.reAlphaInt(color, alpha)).tex(1, 1).endVertex();
                    buffer.pos(ms.getLast().getMatrix(), 0, 0, 0).color(DisplayUtils.reAlphaInt(color, alpha)).tex(1, 0).endVertex();
                    tessellator.draw();
                    ms.translate(-size / 2f, -size / 2f, 0);
                    r.conjugate();
                    ms.rotate(r);
                    ms.translate(size / 2f, size / 2f, 0);
                    ms.translate((s), (s), -(c));
                }
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableBlend();
                RenderSystem.enableCull();
                RenderSystem.enableAlphaTest();
                depthMask(true);
                RenderSystem.popMatrix();
                ms.pop();
            }
        }
    }


    @Subscribe
    private void onDisplay(EventDisplay e) {
        if (this.alpha.finished(Direction.BACKWARDS)) {
            return;

        }
        double sin;
        Vector3d interpolated;
        float size;
        Vector2f pos;
        int alpha;

        if (e.getType() != Type.PRE) {
            return;
        }
        if (mode.is("Ромб")) {
            if (this.currentTarget != null && this.currentTarget != mc.player) {
                sin = Math.sin((double) System.currentTimeMillis() / 1000.0);
                interpolated = this.currentTarget.getPositon(e.getPartialTicks());
                if (animka.get()) {
                    size = (float) this.getScale(interpolated, 16);
                } else {
                    size = 150;
                }

                pos = ProjectionUtil.project(interpolated.x, interpolated.y + (double) (this.currentTarget.getHeight() / 1.95F), interpolated.z);
                GlStateManager.pushMatrix();
                GlStateManager.translatef(pos.x, pos.y, 0.0F);

                // Apply scale animation
                float scale = (float) this.scaleAnimation.getOutput();
                GlStateManager.scalef(scale, scale, 1.0F);

                GlStateManager.rotatef((float) sin * 360.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.translatef(-pos.x, -pos.y, 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(770, 1);
                alpha = (int) this.alpha.getOutput();

                // Red theme gradient colors
                DisplayUtils.drawImageAlpha(new ResourceLocation("expensive/images/target.png"),
                        pos.x - size / 2.0F, pos.y - size / 2.0F, size, size, new Vector4i(
                                ColorUtils.rgba(255, 140, 140, alpha), // Light red
                                ColorUtils.rgba(220, 100, 100, alpha), // Medium red
                                ColorUtils.rgba(255, 160, 160, alpha), // Bright red
                                ColorUtils.rgba(200, 80, 80, alpha)    // Dark red
                        ));

                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }

    public double getScale(Vector3d position, double size) {
        Vector3d cam = mc.getRenderManager().info.getProjectedView();
        double distance = cam.distanceTo(position);
        double fov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);
        return Math.max(10.0, 1000.0 / distance) * (size / 30.0) / (fov == 70.0 ? 1.0 : fov / 70.0);
    }
}