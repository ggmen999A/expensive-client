package by.algorithm.alpha.api.modules.impl.render;

import com.mojang.blaze3d.platform.GlStateManager;
import by.algorithm.alpha.system.events.EventDisplay;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.system.utils.player.ProjectionUtil;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.vector.Vector2f;

@ModuleAnnot(name = "ShulkerView", type = ModuleCategory.Render, description = "Возможность видеть предметы в шалкерах не открывая их")
public class ShulkerChecker extends Module {

    public void onRender(EventDisplay e) {
        for (Entity entity : mc.world.getAllEntities()) {
            double x = 0, y = 0, z = 0;
            ItemStack stack = null;
            
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                if (player.getName().equals(mc.player.getName())) continue;

                stack = player.inventory.getStackInSlot(player.inventory.currentItem);
                x = player.getPosX();
                y = player.getPosY() + player.getHeight() + 1.25f;
                z = player.getPosZ();
            }
            
            if (entity instanceof ItemEntity) {
                ItemStack s = ((ItemEntity) entity).getItem();
                if (Block.getBlockFromItem(s.getItem()) instanceof ShulkerBoxBlock) {
                    stack = s;
                    x = entity.getPosX();
                    y = entity.getPosY() + 0.5f;
                    z = entity.getPosZ();
                }
            }
            
            if (stack == null || !(Block.getBlockFromItem(stack.getItem()) instanceof ShulkerBoxBlock)) continue;

            CompoundNBT tag = stack.getTag();
            if (tag == null || !tag.contains("BlockEntityTag", 10)) continue;

            CompoundNBT blocksTag = tag.getCompound("BlockEntityTag");
            if (!blocksTag.contains("Items", 9)) continue;

            NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
            ItemStackHelper.loadAllItems(blocksTag, items);

            if (items.isEmpty()) continue;

            GlStateManager.pushMatrix();
            Vector2f vec = ProjectionUtil.project((float) x, (float) y, (float) z);
            
            double dx = mc.player.getPosX() - x;
            double dy = mc.player.getPosY() - y;
            double dz = mc.player.getPosZ() - z;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            double scale = Math.max(0.4, Math.min(1.5, 2.0 / distance));

            float startX = vec.x;
            float startY = vec.y;
            float posX = startX;
            float posY = startY;

            GlStateManager.translated(startX, startY, 0);
            GlStateManager.scaled(scale, scale, scale);

            DisplayUtils.drawRoundedRect(0, 0, (20 * 9f) + 4.5f, (20 * 3) + 1.5f, 3, ColorUtils.rgb(50, 50, 50));

            for (ItemStack item : items) {
                mc.getItemRenderer().renderItemAndEffectIntoGUI(item, (int) (posX - startX), (int) (posY - startY));
                mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, item, (int) (posX - startX), (int) (posY - startY), null);

                posX += 20;
                if (posX >= startX + 20 * 9f) {
                    posX = startX;
                    posY += 20;
                }
            }

            GlStateManager.popMatrix();
        }
    }
}