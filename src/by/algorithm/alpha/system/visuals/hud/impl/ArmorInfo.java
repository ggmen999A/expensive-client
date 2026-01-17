package by.algorithm.alpha.system.visuals.hud.impl;

import com.mojang.blaze3d.matrix.MatrixStack;
import by.algorithm.alpha.system.events.EventDisplay;
import by.algorithm.alpha.system.visuals.hud.ElementRenderer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ArmorInfo implements ElementRenderer {

    @Override
    public void render(EventDisplay eventDisplay) {
        MatrixStack ms = eventDisplay.getMatrixStack();
        Minecraft mc = Minecraft.getInstance();

        float x = (mc.getMainWindow().getScaledWidth() - 182) / 2 + 182 + 5f;
        float y = mc.getMainWindow().getScaledHeight() - 21.5f;

        int armorCount = 0;
        for (EquipmentSlotType slot : EquipmentSlotType.values()) {
            if (slot.getSlotType() == EquipmentSlotType.Group.ARMOR) {
                ItemStack itemStack = mc.player.getItemStackFromSlot(slot);
                if (!itemStack.isEmpty()) {
                    armorCount++;
                }
            }
        }

        if (armorCount == 0) {
            return;
        }

        float targetWidth = armorCount * (16 + 2);
        float height = 22;

        // Изменен порядок: теперь сначала шляпа, потом нагрудник, штаны и ботинки
        EquipmentSlotType[] armorSlots = {
                EquipmentSlotType.HEAD,
                EquipmentSlotType.CHEST,
                EquipmentSlotType.LEGS,
                EquipmentSlotType.FEET
        };

        float currentPosX = x;
        for (EquipmentSlotType slot : armorSlots) {
            ItemStack itemStack = mc.player.getItemStackFromSlot(slot);

            if (itemStack.isEmpty()) {
                continue;
            }

            mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, (int) currentPosX, (int) y + 3);
            mc.getItemRenderer().renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, (int) currentPosX, (int) y + 3, null);

            currentPosX += 16 + 2;
        }
    }
}