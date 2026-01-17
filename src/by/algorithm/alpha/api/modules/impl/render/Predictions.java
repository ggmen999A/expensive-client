package by.algorithm.alpha.api.modules.impl.render;

import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.system.events.EventDisplay;
import by.algorithm.alpha.system.events.WorldEvent;
import by.algorithm.alpha.system.utils.player.ProjectionUtil;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

@ModuleAnnot(name = "Predictions", type = ModuleCategory.Render)
public class Predictions extends Module {

    public Predictions() {
        addSettings();
    }

    enum ProjectileType {
        ENDER_PEARL("textures/item/ender_pearl.png", ColorUtils.rgba(76, 175, 80, 255)),
        TRIDENT("textures/item/trident.png", ColorUtils.rgba(255, 193, 7, 255)),
        POTION("textures/item/splash_potion.png", ColorUtils.rgba(156, 39, 176, 255)),
        ARROW("textures/item/arrow.png", ColorUtils.rgba(255, 87, 34, 255));

        private final String texture;
        private final int color;

        ProjectileType(String texture, int color) {
            this.texture = texture;
            this.color = color;
        }

        public String getTexture() {
            return texture;
        }

        public int getColor() {
            return color;
        }
    }

    record ProjectilePoint(Vector3d position, int ticks, ProjectileType type, String ownerName) {
    }

    final List<ProjectilePoint> projectilePoints = new ArrayList<>();

    private String getOwnerName(Entity projectile) {
        Entity owner = null;

        if (projectile instanceof ThrowableEntity throwable) {
            owner = throwable.func_234616_v_();
        } else if (projectile instanceof AbstractArrowEntity arrow) {
            owner = arrow.func_234616_v_();
        } else if (projectile instanceof TridentEntity trident) {
            owner = trident.func_234616_v_();
        }

        if (owner instanceof PlayerEntity player) {
            return player.getName().getString();
        }

        return owner != null ? owner.getName().getString() : "Система";
    }

    @Subscribe
    public void aa(EventDisplay e) {
        for (ProjectilePoint point : projectilePoints) {
            Vector3d pos = point.position;
            Vector2f projection = ProjectionUtil.project(pos.x, pos.y - 0.3F, pos.z);
            int ticks = point.ticks;
            ProjectileType type = point.type;
            String ownerName = point.ownerName;

            if (projection.equals(new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE))) {
                continue;
            }

            double time = ticks * 50 / 1000.0;
            String timeText = String.format("%.1f сек.", time);
            String ownerText = ownerName;

            float timeWidth = Fonts.montserrat.getWidth(timeText, 7);
            float ownerWidth = Fonts.montserrat.getWidth(ownerText, 6);
            float maxTextWidth = Math.max(timeWidth, ownerWidth);

            float iconSize = 16;
            float padding = 4;
            float rectWidth = iconSize + padding + 1 + 2 + maxTextWidth + padding * 2;
            float rectHeight = 20;

            float posX = projection.x - rectWidth / 2;
            float posY = projection.y - rectHeight / 2;

            DisplayUtils.drawRoundedRect(posX, posY, rectWidth, rectHeight, 4, ColorUtils.rgba(0, 0, 0, 130));

            DisplayUtils.drawImage(new ResourceLocation(type.getTexture()),
                    (int) (posX + padding), (int) (posY + 2), (int) iconSize, (int) iconSize, -1);

            float lineX = posX + iconSize + padding + 2;
            float lineY = posY + 4;
            float lineHeight = rectHeight - 8;
            DisplayUtils.drawRoundedRect(lineX, lineY, 1, lineHeight, 0, ColorUtils.rgba(255, 255, 255, 120));

            float textStartX = posX + iconSize + padding * 2 + 4;
            Fonts.montserrat.drawText(e.getMatrixStack(), timeText,
                    textStartX, posY + 3, -1, 7);

            Fonts.montserrat.drawText(e.getMatrixStack(), ownerText,
                    textStartX, posY + 12,
                    ColorUtils.rgba(170, 170, 170, 255), 6);
        }
    }

    @Subscribe
    public void onRender(WorldEvent event) {
        glPushMatrix();

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glEnable(GL_LINE_SMOOTH);

        Vector3d renderOffset = mc.getRenderManager().info.getProjectedView();

        glTranslated(-renderOffset.x, -renderOffset.y, -renderOffset.z);

        glLineWidth(3);

        buffer.begin(1, DefaultVertexFormats.POSITION);

        projectilePoints.clear();

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof EnderPearlEntity enderPearl) {
                String ownerName = getOwnerName(enderPearl);

                Vector3d motion = enderPearl.getMotion();
                Vector3d pos = enderPearl.getPositionVec();
                Vector3d prevPos;
                int ticks = 0;

                for (int i = 0; i < 150; i++) {
                    prevPos = pos;
                    pos = pos.add(motion);
                    motion = getNextMotionThrowable(enderPearl, motion);

                    ColorUtils.setAlpha(ProjectileType.ENDER_PEARL.getColor(), 165);

                    buffer.pos(prevPos.x, prevPos.y, prevPos.z).endVertex();

                    RayTraceContext rayTraceContext = new RayTraceContext(
                            prevPos,
                            pos,
                            RayTraceContext.BlockMode.COLLIDER,
                            RayTraceContext.FluidMode.NONE,
                            enderPearl
                    );

                    BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);

                    if (blockHitResult.getType() == RayTraceResult.Type.BLOCK) {
                        pos = blockHitResult.getHitVec();
                    }

                    buffer.pos(pos.x, pos.y, pos.z).endVertex();

                    if (blockHitResult.getType() == BlockRayTraceResult.Type.BLOCK || pos.y < -128) {
                        projectilePoints.add(new ProjectilePoint(pos, ticks, ProjectileType.ENDER_PEARL, ownerName));
                        break;
                    }
                    ticks++;
                }
            } else if (entity instanceof PotionEntity potion) {
                String ownerName = getOwnerName(potion);

                Vector3d motion = potion.getMotion();
                Vector3d pos = potion.getPositionVec();
                Vector3d prevPos;
                int ticks = 0;

                for (int i = 0; i < 150; i++) {
                    prevPos = pos;
                    pos = pos.add(motion);
                    motion = getNextMotionThrowable(potion, motion);

                    ColorUtils.setAlpha(ProjectileType.POTION.getColor(), 165);

                    buffer.pos(prevPos.x, prevPos.y, prevPos.z).endVertex();

                    RayTraceContext rayTraceContext = new RayTraceContext(
                            prevPos,
                            pos,
                            RayTraceContext.BlockMode.COLLIDER,
                            RayTraceContext.FluidMode.NONE,
                            potion
                    );

                    BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);

                    if (blockHitResult.getType() == RayTraceResult.Type.BLOCK) {
                        pos = blockHitResult.getHitVec();
                    }

                    buffer.pos(pos.x, pos.y, pos.z).endVertex();

                    if (blockHitResult.getType() == BlockRayTraceResult.Type.BLOCK || pos.y < -128) {
                        projectilePoints.add(new ProjectilePoint(pos, ticks, ProjectileType.POTION, ownerName));
                        break;
                    }
                    ticks++;
                }
            } else if (entity instanceof TridentEntity trident) {
                String ownerName = getOwnerName(trident);

                Vector3d motion = trident.getMotion();
                Vector3d pos = trident.getPositionVec();
                Vector3d prevPos;
                int ticks = 0;

                for (int i = 0; i < 150; i++) {
                    prevPos = pos;
                    pos = pos.add(motion);
                    motion = getNextMotionProjectile(motion, false);

                    ColorUtils.setAlpha(ProjectileType.TRIDENT.getColor(), 165);

                    buffer.pos(prevPos.x, prevPos.y, prevPos.z).endVertex();

                    RayTraceContext rayTraceContext = new RayTraceContext(
                            prevPos,
                            pos,
                            RayTraceContext.BlockMode.COLLIDER,
                            RayTraceContext.FluidMode.NONE,
                            trident
                    );

                    BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);

                    if (blockHitResult.getType() == RayTraceResult.Type.BLOCK) {
                        pos = blockHitResult.getHitVec();
                    }

                    buffer.pos(pos.x, pos.y, pos.z).endVertex();

                    if (blockHitResult.getType() == BlockRayTraceResult.Type.BLOCK || pos.y < -128) {
                        projectilePoints.add(new ProjectilePoint(pos, ticks, ProjectileType.TRIDENT, ownerName));
                        break;
                    }
                    ticks++;
                }
            } else if (entity instanceof AbstractArrowEntity arrow) {
                String ownerName = getOwnerName(arrow);

                Vector3d motion = arrow.getMotion();
                Vector3d pos = arrow.getPositionVec();
                Vector3d prevPos;
                int ticks = 0;

                for (int i = 0; i < 150; i++) {
                    prevPos = pos;
                    pos = pos.add(motion);
                    motion = getNextMotionProjectile(motion, true);

                    ColorUtils.setAlpha(ProjectileType.ARROW.getColor(), 165);

                    buffer.pos(prevPos.x, prevPos.y, prevPos.z).endVertex();

                    RayTraceContext rayTraceContext = new RayTraceContext(
                            prevPos,
                            pos,
                            RayTraceContext.BlockMode.COLLIDER,
                            RayTraceContext.FluidMode.NONE,
                            arrow
                    );

                    BlockRayTraceResult blockHitResult = mc.world.rayTraceBlocks(rayTraceContext);

                    if (blockHitResult.getType() == RayTraceResult.Type.BLOCK) {
                        pos = blockHitResult.getHitVec();
                    }

                    buffer.pos(pos.x, pos.y, pos.z).endVertex();

                    if (blockHitResult.getType() == BlockRayTraceResult.Type.BLOCK || pos.y < -128) {
                        projectilePoints.add(new ProjectilePoint(pos, ticks, ProjectileType.ARROW, ownerName));
                        break;
                    }
                    ticks++;
                }
            }
        }

        tessellator.draw();

        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);

        glPopMatrix();
    }

    private Vector3d getNextMotionThrowable(ThrowableEntity throwable, Vector3d motion) {
        if (throwable.isInWater()) {
            motion = motion.scale(0.8);
        } else {
            motion = motion.scale(0.99);
        }

        if (!throwable.hasNoGravity()) {
            motion.y -= throwable.getGravityVelocity();
        }

        return motion;
    }

    private Vector3d getNextMotionProjectile(Vector3d motion, boolean isArrow) {
        if (isArrow) {
            motion = motion.scale(0.99);
        } else {
            motion = motion.scale(0.99);
        }

        motion.y -= isArrow ? 0.05 : 0.05;

        return motion;
    }
}