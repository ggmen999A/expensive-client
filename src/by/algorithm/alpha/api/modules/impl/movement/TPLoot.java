package by.algorithm.alpha.api.modules.impl.movement;


import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.system.events.EventUpdate;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@ModuleAnnot(name = "TPLoot", type = ModuleCategory.Player, description = "Пеним Лони / грим")
public class TPLoot extends Module {
    private final Minecraft mc = Minecraft.getInstance();

    private Vector3d initialPosition = null;
    private long actionTimer = 0;
    private boolean waitingForSpawn = false;
    private int lastItemCount = 0;

    private static final Set<Item> VALUABLE_ITEMS = new HashSet<>(Arrays.asList(
            Items.TOTEM_OF_UNDYING, Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE,
            Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS, Items.NETHERITE_SWORD,
            Items.NETHERITE_PICKAXE, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE,
            Items.PLAYER_HEAD, Items.SHULKER_BOX, Items.NETHERITE_INGOT, Items.SPLASH_POTION
    ));

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null) return;

        if (waitingForSpawn) {
            if (System.currentTimeMillis() - actionTimer >= 300) {
                mc.player.sendChatMessage("/spawn");
                waitingForSpawn = false;
                initialPosition = null;
                this.toggle();
            }
            return;
        }

        if (initialPosition == null) {
            initialPosition = mc.player.getPositionVec();
            lastItemCount = getItemCount();
        }

        processLootLogic();
    }

    private void processLootLogic() {
        double radius = 50.0;
        Vector3d playerPos = mc.player.getPositionVec();

        List<ItemEntity> items = mc.world.getEntitiesWithinAABB(ItemEntity.class,
                mc.player.getBoundingBox().grow(radius),
                entity -> entity.getItem() != null && VALUABLE_ITEMS.contains(entity.getItem().getItem())
        );

        if (!items.isEmpty()) {
            ItemEntity target = items.get(0);
            Vector3d itemPos = target.getPositionVec();

            double lerp = 0.35;
            Vector3d nextStep = playerPos.add(itemPos.subtract(playerPos).scale(lerp));
            mc.player.setPosition(nextStep.x, nextStep.y, nextStep.z);

            if (playerPos.distanceTo(itemPos) < 0.8) {
                if (!target.isAlive() || getItemCount() > lastItemCount) {
                    mc.player.sendChatMessage("/fly");
                    actionTimer = System.currentTimeMillis();
                    waitingForSpawn = true;
                }
            }
        }
    }

    private int getItemCount() {
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                count += stack.getCount();
            }
        }
        return count;
    }

    @Override
    public void onDisable() {
        initialPosition = null;
        waitingForSpawn = false;
        super.onDisable();
    }
}