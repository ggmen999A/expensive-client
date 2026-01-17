package by.algorithm.alpha.system.visuals.hud.impl;

import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.system.events.EventDisplay;
import by.algorithm.alpha.system.utils.animations.Animation;
import by.algorithm.alpha.system.utils.animations.Direction;
import by.algorithm.alpha.system.utils.animations.impl.EaseBackIn;
import by.algorithm.alpha.system.utils.animations.impl.EaseInOutQuad;
import by.algorithm.alpha.system.utils.client.ClientUtil;
import by.algorithm.alpha.system.utils.dragable.Dragging;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.utils.math.StopWatch;
import by.algorithm.alpha.system.utils.math.Vector4i;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.Scissor;
import by.algorithm.alpha.system.utils.render.Stencil;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import by.algorithm.alpha.system.visuals.hud.ElementRenderer;
import by.algorithm.alpha.system.visuals.styles.Style;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.Collection;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class TargetHUD implements ElementRenderer {

    final StopWatch stopWatch = new StopWatch();
    float realHealth = 0;
    final Dragging dragging;
    LivingEntity entity = null;
    boolean allow;
    final Animation animation = new EaseBackIn(400, 1, 1);
    final Animation hitAnimation = new EaseInOutQuad(300, 1, Direction.BACKWARDS);
    float healthAnimation = 0.0f;
    float absorptionAnimation = 0.0f;
    float hitRedOverlay = 0.0f;
    float width;
    float height;
    String hpDisplayType = "Число";

    public void setHpDisplayType(String type) {
        this.hpDisplayType = type;
    }

    @Override
    public void render(EventDisplay eventDisplay) {
        float x = dragging.getX();
        float y = dragging.getY();
        int round = 6;
        entity = getTarget(entity);

        boolean out = !allow || stopWatch.isReached(1000);
        animation.setDuration(out ? 400 : 300);
        animation.setDirection(out ? Direction.BACKWARDS : Direction.FORWARDS);

        if (entity != null && entity.hurtTime > 0) {
            hitAnimation.setDirection(Direction.FORWARDS);
            hitRedOverlay = 1.0f;
        } else {
            hitAnimation.setDirection(Direction.BACKWARDS);
        }
        hitRedOverlay = (float) hitAnimation.getOutput();

        if (animation.getOutput() == 0.0f) {
            entity = null;
        }

        if (entity != null) {
            MatrixStack matrix = eventDisplay.getMatrixStack();
            String name = entity.getName().getString();
            Score score = mc.world.getScoreboard().getOrCreateScore(entity.getScoreboardName(),
                    mc.world.getScoreboard().getObjectiveInDisplaySlot(2));

            float hp = entity.getHealth();
            float maxHp = entity.getMaxHealth();
            String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();

            boolean isReallyWorld = mc.getCurrentServerData() != null &&
                    (mc.getCurrentServerData().serverIP.contains("reallyworld") || mc.getCurrentServerData().serverIP.contains("funsky") ||
                            mc.getCurrentServerData().serverIP.contains("funtime"));

            if (isReallyWorld && entity instanceof PlayerEntity) {
                ScoreObjective sidebarObjective = mc.world.getScoreboard().getObjectiveInDisplaySlot(2);
                if (sidebarObjective != null) {
                    boolean found = false;
                    Collection<Score> scores = mc.world.getScoreboard().getSortedScores(sidebarObjective);

                    for (Score scoreEntry : scores) {
                        String scoreName = scoreEntry.getPlayerName();
                        if (scoreName.startsWith(entity.getName().getString())) {
                            if (scoreName.contains("хп") || scoreName.contains("hp")) {
                                hp = scoreEntry.getScorePoints();
                                maxHp = 20;
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        Score defaultScore = mc.world.getScoreboard().getOrCreateScore(
                                entity.getScoreboardName(),
                                sidebarObjective
                        );
                        hp = defaultScore.getScorePoints();
                        maxHp = 20;
                    }
                }
            }
            else if (mc.getCurrentServerData() != null &&
                    mc.getCurrentServerData().serverIP.contains("funtime") &&
                    (header.contains("анархия") || header.contains("гриферский")) &&
                    entity instanceof PlayerEntity) {

                Score funtimeScore = mc.world.getScoreboard().getOrCreateScore(
                        entity.getScoreboardName(),
                        mc.world.getScoreboard().getObjectiveInDisplaySlot(2)
                );
                hp = funtimeScore.getScorePoints();
                maxHp = 20;
            }

            healthAnimation = MathUtil.fast(healthAnimation, MathHelper.clamp(hp / maxHp, 0, 1), 10);
            absorptionAnimation = MathUtil.fast(absorptionAnimation, MathHelper.clamp(entity.getAbsorptionAmount() / maxHp, 0, 1), 10);

            float animationValue = (float) animation.getOutput();
            float halfAnimationValueRest = (1 - animationValue) / 2f;

            float testX = x + (width * halfAnimationValueRest);
            float testY = y + (height * halfAnimationValueRest);
            float testW = width * animationValue;
            float testH = height * animationValue;
            int windowWidth = ClientUtil.calc(mc.getMainWindow().getScaledWidth());
            width = 100;

            GlStateManager.pushMatrix();
            Style style = Initclass.getInstance().getStyleManager().getCurrentStyle();

            sizeAnimation(x + (width / 2), y + (height / 2), animation.getOutput());
            Vector4i vector4i = new Vector4i(style.getFirstColor().getRGB(), style.getFirstColor().getRGB(),
                    style.getSecondColor().getRGB(), style.getSecondColor().getRGB());

            DisplayUtils.drawRoundedRect(x, y, width, height, 4, ColorUtils.rgba(0, 0, 0, 200));

            Stencil.initStencilToWrite();
            DisplayUtils.drawRoundedRect(x + 3, y + 3, 35, 35, 1, ColorUtils.rgba(255, 255, 255, 255));
            Stencil.readStencilBuffer(1);
            float hurtPercent = (entity.hurtTime - (entity.hurtTime != 0 ? mc.timer.renderPartialTicks : 0.0f)) / 10.0f;
            String rs = EntityType.getKey(((Entity)entity).getType()).getPath();
            ResourceLocation skin = entity instanceof AbstractClientPlayerEntity e ? (e).getLocationSkin() : new ResourceLocation("textures/entity/"+rs+".png");
            DisplayUtils.drawHead(skin, x + 4, y + 3, 25, 25, 4, 1, hurtPercent);
            if (hitRedOverlay > 0) {
                GlStateManager.enableBlend();
                DisplayUtils.drawRoundedRect(x + 5, y + 5, 25, 25, 1, ColorUtils.rgba(255, 0, 0, (int)(hitRedOverlay * 100)));
                GlStateManager.disableBlend();
            }
            Stencil.uninitStencilBuffer();

            Scissor.push();
            Scissor.setFromComponentCoordinates(testX, testY, testW - 6, testH);

            Fonts.tenacity.drawText(eventDisplay.getMatrixStack(), entity.getName().getString(), x + 35, y + 5.5f, -1, 8);

            Scissor.unset();
            Scissor.pop();

            float healthPercent = MathHelper.clamp(hp / maxHp, 0, 1);
            int red, green, blue;

            if (healthPercent <= 0.33f) {
                red = 220;
                green = 20;
                blue = 20;
            } else if (healthPercent <= 0.66f) {
                red = 255;
                green = 140;
                blue = 0;
            } else {
                red = 0;
                green = 180;
                blue = 0;
            }

            int hpBarColor = ColorUtils.rgb(red, green, blue);

            DisplayUtils.drawRoundedRect(x + 35, y + 22, (width - 40), 6, new Vector4f(3, 3, 3, 3), ColorUtils.rgb(32, 32, 32));
            DisplayUtils.drawRoundedRect(x + 35, y + 22, (width - 40) * healthAnimation, 6, new Vector4f(3, 3, 3, 3), hpBarColor);
            if (entity.getAbsorptionAmount() > 0) {
                DisplayUtils.drawRoundedRect(x + 35, y + 22, (width - 40) * absorptionAnimation, 6, new Vector4f(3, 3, 3, 3), ColorUtils.rgb(218, 165, 32));
            }

            String hpText;
            if (hpDisplayType.equals("Проценты")) {
                int percentage = (int) ((hp / maxHp) * 100);
                hpText = percentage + "%";
            } else {
                hpText = String.valueOf((int) hp);
            }
            String absorptionText = entity.getAbsorptionAmount() > 0 ? " + " + (int) entity.getAbsorptionAmount() + " AB" : "";

            Fonts.tenacity.drawText(eventDisplay.getMatrixStack(), "HP: ", x + 35.5f, y + 14, ColorUtils.rgb(255, 255, 255), 7);
            Fonts.tenacity.drawText(eventDisplay.getMatrixStack(), hpText, x + 35.5f + Fonts.tenacity.getWidth("HP: ", 7),
                    y + 14.5f, ColorUtils.rgb(255, 255, 255), 7, 0.1f);
            if (!absorptionText.isEmpty()) {
                Fonts.tenacity.drawText(eventDisplay.getMatrixStack(), absorptionText,
                        x + 35.5f + Fonts.tenacity.getWidth("HP: " + hpText, 7),
                        y + 14.5f, ColorUtils.setAlpha(ColorUtils.rgb(218, 165, 32), 240), 7, 0.1f);
            }

            GlStateManager.popMatrix();

            width = 100;
            height = 35;
            dragging.setWidth(width);
            dragging.setHeight(height);
        }
    }

    private LivingEntity getTarget(LivingEntity nullTarget) {
        LivingEntity auraTarget = Initclass.getInstance().getFunctionRegistry().getKillAura().getTarget();
        LivingEntity target = nullTarget;
        if (auraTarget != null) {
            stopWatch.reset();
            allow = true;
            target = auraTarget;
        } else if (mc.currentScreen instanceof ChatScreen) {
            stopWatch.reset();
            allow = true;
            target = mc.player;
        } else {
            allow = false;
        }
        return target;
    }

    public static void sizeAnimation(double width, double height, double scale) {
        GlStateManager.translated(width, height, 0);
        GlStateManager.scaled(scale, scale, scale);
        GlStateManager.translated(-width, -height, 0);
    }

    public void drawFace(ResourceLocation res, float d,
                         float y,
                         float u,
                         float v,
                         float uWidth,
                         float vHeight,
                         float width,
                         float height,
                         float tileWidth,
                         float tileHeight,
                         LivingEntity target) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        mc.getTextureManager().bindTexture(res);
        float hurtPercent = (float) hitAnimation.getOutput();
        GL11.glColor4f(1, 1 - hurtPercent, 1 - hurtPercent, 1);
        AbstractGui.drawScaledCustomSizeModalRect(d, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight);
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glPopMatrix();
    }

    public void drawTargetHead(LivingEntity entity, float x, float y, float width, float height) {
        if (entity != null) {
            EntityRenderer<? super LivingEntity> rendererManager = mc.getRenderManager().getRenderer(entity);
            drawFace(rendererManager.getEntityTexture(entity), x, y, 8F, 8F, 8F, 8F, width, height, 64F, 64F, entity);
        }
    }
}