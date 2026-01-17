package by.algorithm.alpha.api.modules.impl.render;

import by.algorithm.alpha.api.command.friends.FriendStorage;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.impl.combat.AntiBot;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.ColorSetting;
import by.algorithm.alpha.api.modules.settings.impl.ModeListSetting;
import by.algorithm.alpha.system.events.EventDisplay;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.utils.player.ProjectionUtil;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.*;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.client.renderer.WorldRenderer.frustum;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

@ModuleAnnot(name = "NameTags", type = ModuleCategory.Render)
public class NameTags extends Module {
    public ModeListSetting remove = new ModeListSetting("Убрать", new BooleanSetting("Броня", false), new BooleanSetting("Предмет в руке", false));

    public NameTags() {
        toggle();
        addSettings(remove);
    }

    private final HashMap<Entity, Vector4f> positions = new HashMap<>();

    public ColorSetting color = new ColorSetting("Color", -1);

    @Subscribe
    public void onDisplay(EventDisplay e) {
        if (mc.world == null || e.getType() != EventDisplay.Type.PRE) {
            return;
        }

        positions.clear();

        for (Entity entity : mc.world.getAllEntities()) {
            if (!isValid(entity)) continue;
            if (!(entity instanceof PlayerEntity || entity instanceof ItemEntity)) continue;
            if (entity == mc.player && (mc.gameSettings.getPointOfView() == PointOfView.FIRST_PERSON)) continue;

            double x = MathUtil.interpolate(entity.getPosX(), entity.lastTickPosX, e.getPartialTicks());
            double y = MathUtil.interpolate(entity.getPosY(), entity.lastTickPosY, e.getPartialTicks());
            double z = MathUtil.interpolate(entity.getPosZ(), entity.lastTickPosZ, e.getPartialTicks());

            Vector3d size = new Vector3d(entity.getBoundingBox().maxX - entity.getBoundingBox().minX, entity.getBoundingBox().maxY - entity.getBoundingBox().minY, entity.getBoundingBox().maxZ - entity.getBoundingBox().minZ);

            AxisAlignedBB aabb = new AxisAlignedBB(x - size.x / 2f, y, z - size.z / 2f, x + size.x / 2f, y + size.y, z + size.z / 2f);

            Vector4f position = null;

            for (int i = 0; i < 8; i++) {
                Vector2f vector = ProjectionUtil.project(i % 2 == 0 ? aabb.minX : aabb.maxX, (i / 2) % 2 == 0 ? aabb.minY : aabb.maxY, (i / 4) % 2 == 0 ? aabb.minZ : aabb.maxZ);

                if (position == null) {
                    position = new Vector4f(vector.x, vector.y, 1, 1.0f);
                } else {
                    position.x = Math.min(vector.x, position.x);
                    position.y = Math.min(vector.y, position.y);
                    position.z = Math.max(vector.x, position.z);
                    position.w = Math.max(vector.y, position.w);
                }
            }

            positions.put(entity, position);
        }

        for (Map.Entry<Entity, Vector4f> entry : positions.entrySet()) {
            Entity entity = entry.getKey();

            if (entity instanceof LivingEntity living) {
                Vector4f position = entry.getValue();
                float width = position.z - position.x;

                double distance = mc.player.getDistance(entity);
                float scale = getAdaptiveScale(distance);

                Score score = mc.world.getScoreboard().getOrCreateScore(living.getScoreboardName(), mc.world.getScoreboard().getObjectiveInDisplaySlot(2));
                float hp = living.getHealth();
                float maxHp = living.getMaxHealth();

                String header = mc.ingameGUI.getTabList().header == null ? " " : mc.ingameGUI.getTabList().header.getString().toLowerCase();

                if (mc.getCurrentServerData() != null && mc.getCurrentServerData().serverIP.contains("funtime") && (header.contains("анархия") || header.contains("гриферский"))) {
                    hp = score.getScorePoints();
                    maxHp = 20;
                }

                String hpValue = String.valueOf((int)hp);
                float fontSize = 7 * scale;
                String displayNameString = entity.getDisplayName().getString();
                ITextComponent displayName = parseFormattedText(displayNameString);

                ITextComponent finalComponent;
                if (FriendStorage.isFriend(entity.getName().getString())) {
                    ITextComponent friendPrefix = new StringTextComponent("[F] ").setStyle(Style.EMPTY.setColor(Color.fromInt(ColorUtils.rgb(144, 238, 144))));
                    finalComponent = friendPrefix.deepCopy().append(displayName);
                } else {
                    finalComponent = displayName;
                }
                float nameWidth = getTextComponentWidth(finalComponent, fontSize);
                float bracketOpenWidth = Fonts.tenacity.getWidth(" [", fontSize);
                float hpWidth = Fonts.tenacity.getWidth(hpValue, fontSize);
                float bracketCloseWidth = Fonts.tenacity.getWidth("]", fontSize);
                float totalWidth = nameWidth + bracketOpenWidth + hpWidth + bracketCloseWidth;

                float textHeight = Fonts.tenacity.getHeight(fontSize);

                float textX = position.x + width / 2f - totalWidth / 2f;
                float textY = position.y - (18 * scale);

                float boxPadding = 2f * scale;
                DisplayUtils.drawRoundedRect(textX - boxPadding, textY - boxPadding, totalWidth + boxPadding * 2, textHeight + boxPadding * 2, 3 * scale, ColorUtils.rgba(0, 0, 0, 180));

                float currentX = textX;
                currentX += renderTextComponent(e.getMatrixStack(), finalComponent, currentX, textY, fontSize);
                Fonts.tenacity.drawText(e.getMatrixStack(), " [", currentX, textY, ColorUtils.rgb(160, 160, 160), fontSize);
                currentX += bracketOpenWidth;

                Fonts.tenacity.drawText(e.getMatrixStack(), hpValue, currentX, textY, ColorUtils.rgb(255, 85, 85), fontSize);
                currentX += hpWidth;

                Fonts.tenacity.drawText(e.getMatrixStack(), "HP]", currentX, textY, ColorUtils.rgb(160, 160, 160), fontSize);

                if (!remove.getValueByName("Броня").get()) {
                    drawArmor(e.getMatrixStack(), living, (int) (position.x + width / 2f), (int) (position.y - (38 * scale)), scale);
                }

                if (!remove.getValueByName("Предмет в руке").get()) {
                    drawHandItem(e.getMatrixStack(), living, (int) (position.x + width / 2f), (int) (position.y - (53 * scale)), scale);
                }

            } else if (entity instanceof ItemEntity item) {
                Vector4f position = entry.getValue();
                float width = position.z - position.x;
                float length = mc.fontRenderer.getStringPropertyWidth(entity.getDisplayName());

                double distance = mc.player.getDistance(entity);
                float scale = getAdaptiveScale(distance);

                GL11.glPushMatrix();

                glCenteredScale(position.x + width / 2f - length / 2f, position.y - 7, length, 10, scale);

                mc.fontRenderer.func_243246_a(e.getMatrixStack(), entity.getDisplayName(), position.x + width / 2f - length / 2f, position.y - 7, -1);
                GL11.glPopMatrix();
            }
        }
    }
    private ITextComponent parseFormattedText(String text) {
        if (text == null || !text.contains("§")) {
            return new StringTextComponent(text != null ? text : "");
        }

        IFormattableTextComponent result = new StringTextComponent("");
        StringBuilder currentText = new StringBuilder();
        Style currentStyle = Style.EMPTY;

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '§' && i + 1 < text.length()) {
                if (currentText.length() > 0) {
                    result.append(new StringTextComponent(currentText.toString()).setStyle(currentStyle));
                    currentText = new StringBuilder();
                }

                char code = text.charAt(i + 1);
                TextFormatting formatting = TextFormatting.fromFormattingCode(code);

                if (formatting != null) {
                    if (formatting == TextFormatting.RESET) {
                        currentStyle = Style.EMPTY;
                    } else {
                        currentStyle = currentStyle.applyFormatting(formatting);
                    }
                }

                i++;
            } else {
                currentText.append(text.charAt(i));
            }
        }
        if (currentText.length() > 0) {
            result.append(new StringTextComponent(currentText.toString()).setStyle(currentStyle));
        }

        return result;
    }

    private float renderTextComponent(MatrixStack matrixStack, ITextComponent component, float x, float y, float fontSize) {
        float currentX = x;
        String ownText = component.getUnformattedComponentText();

        if (!ownText.isEmpty()) {
            int color = getTextColor(component);
            Fonts.tenacity.drawText(matrixStack, ownText, currentX, y, color, fontSize);
            currentX += Fonts.tenacity.getWidth(ownText, fontSize);
        }
        for (ITextComponent sibling : component.getSiblings()) {
            currentX += renderTextComponent(matrixStack, sibling, currentX, y, fontSize);
        }

        return currentX - x;
    }

    private float getTextComponentWidth(ITextComponent component, float fontSize) {
        float width = 0;
        String ownText = component.getUnformattedComponentText();

        if (!ownText.isEmpty()) {
            width += Fonts.tenacity.getWidth(ownText, fontSize);
        }
        for (ITextComponent sibling : component.getSiblings()) {
            width += getTextComponentWidth(sibling, fontSize);
        }

        return width;
    }

    private int getTextColor(ITextComponent component) {
        Style style = component.getStyle();
        Color color = style.getColor();

        if (color != null) {
            return color.getColor() | 0xFF000000;
        }
        return 0xFFFFFFFF;
    }

    private float getAdaptiveScale(double distance) {
        float minScale = 0.6f;
        float maxScale = 1.4f;
        float minDistance = 2.0f;
        float maxDistance = 20.0f;

        if (distance <= minDistance) return maxScale;
        if (distance >= maxDistance) return minScale;

        float normalizedDistance = (float) ((distance - minDistance) / (maxDistance - minDistance));
        return maxScale - (normalizedDistance * (maxScale - minScale));
    }

    public boolean isInView(Entity ent) {
        if (mc.getRenderViewEntity() == null) {
            return false;
        }
        frustum.setCameraPosition(mc.getRenderManager().info.getProjectedView().x, mc.getRenderManager().info.getProjectedView().y, mc.getRenderManager().info.getProjectedView().z);
        return frustum.isBoundingBoxInFrustum(ent.getBoundingBox()) || ent.ignoreFrustumCheck;
    }

    private void drawArmor(MatrixStack matrixStack, LivingEntity entity, int posX, int posY, float scale) {
        int baseSize = 14;
        int size = (int) (baseSize * scale);
        int padding = (int) (3 * scale);

        List<ItemStack> armorItems = new ArrayList<>();

        for (ItemStack itemStack : entity.getArmorInventoryList()) {
            if (!itemStack.isEmpty()) {
                armorItems.add(0, itemStack);
            }
        }

        posX -= (armorItems.size() * (size + padding)) / 2f;

        for (ItemStack itemStack : armorItems) {
            GL11.glPushMatrix();

            GL11.glTranslatef(posX + size/2f, posY + size/2f, 0);
            GL11.glScalef(scale, scale, 1);
            GL11.glTranslatef(-baseSize/2f, -baseSize/2f, 0);

            mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, 0, 0);
            mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, 0, 0, null);

            GL11.glPopMatrix();

            if (itemStack.isDamageable()) {
                int durability = itemStack.getMaxDamage() - itemStack.getDamage();
                int maxDurability = itemStack.getMaxDamage();

                String durabilityText = String.valueOf(durability);
                float textWidth = Fonts.tenacity.getWidth(durabilityText, 5f * scale);

                int color = durability < maxDurability * 0.25 ? ColorUtils.rgb(255, 85, 85) :
                        durability < maxDurability * 0.5 ? ColorUtils.rgb(255, 255, 85) :
                                ColorUtils.rgb(85, 255, 85);

                Fonts.tenacity.drawText(matrixStack, durabilityText, posX + size/2f - textWidth/2f, posY + size + 1, color, 5f * scale);
            }

            posX += size + padding;
        }
    }

    private void drawHandItem(MatrixStack matrixStack, LivingEntity entity, int posX, int posY, float scale) {
        ItemStack mainStack = entity.getHeldItemMainhand();

        if (mainStack.isEmpty()) return;

        int baseSize = 14;
        int size = (int) (baseSize * scale);

        GL11.glPushMatrix();

        GL11.glTranslatef(posX, posY + size/2f, 0);
        GL11.glScalef(scale, scale, 1);
        GL11.glTranslatef(-baseSize/2f, -baseSize/2f, 0);

        mc.getItemRenderer().renderItemAndEffectIntoGUI(mainStack, 0, 0);
        mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, mainStack, 0, 0, null);

        GL11.glPopMatrix();
    }

    public boolean isValid(Entity e) {
        if (AntiBot.isBot(e)) return false;
        return isInView(e);
    }

    public void glCenteredScale(final float x, final float y, final float w, final float h, final float f) {
        glTranslatef(x + w / 2, y + h / 2, 0);
        glScalef(f, f, 1);
        glTranslatef(-x - w / 2, -y - h / 2, 0);
    }
}