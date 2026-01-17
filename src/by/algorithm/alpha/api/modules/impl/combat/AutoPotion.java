package by.algorithm.alpha.api.modules.impl.combat;

import by.algorithm.alpha.system.utils.player.MoveUtils;
import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventMotion;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.system.utils.math.StopWatch;
import by.algorithm.alpha.system.utils.player.InventoryUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleAnnot(name = "AutoPotion", type = ModuleCategory.Combat, description = "Автоматически кидает взрывные зелья под себя с silent-поворотом")
public class AutoPotion extends Module {

    private final BooleanSetting mefedron = new BooleanSetting("Зелье скорости", false);
    private final BooleanSetting hui = new BooleanSetting("Зелье силы", false);
    private final BooleanSetting litenergy = new BooleanSetting("Зелье огнестойкости", false);

    float[] serverRotation = null;
    float[] originalRotation = null;
    final StopWatch stopWatch = new StopWatch();

    public AutoPotion() {
        this.addSettings(this.mefedron, hui, litenergy);
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (!canThrowPotion()) {
            resetRotation();
            return;
        }

        boolean needThrow = isActive() &&
                ((this.mefedron.get() && !mc.player.isPotionActive(Effects.SPEED) && hasPotionInInventory(Potions.SPEED)) ||
                        (this.hui.get() && !mc.player.isPotionActive(Effects.STRENGTH) && hasPotionInInventory(Potions.STRENGTH)) ||
                        (this.litenergy.get() && !mc.player.isPotionActive(Effects.FIRE_RESISTANCE) && hasPotionInInventory(Potions.FIRE_RESIST)));

        if (needThrow) {
            if (originalRotation == null) {
                originalRotation = new float[]{mc.player.rotationYaw, mc.player.rotationPitch};
            }

            serverRotation = new float[]{originalRotation[0], 90.0F};
            e.setYaw(serverRotation[0]);
            e.setPitch(serverRotation[1]);

            e.setPostMotion(() -> {
                if (serverRotation != null) {
                    throwPotions();
                    resetRotation();
                    stopWatch.reset();
                }
            });
        } else {
            resetRotation();
        }
    }

    private void throwPotions() {
        int oldSlot = mc.player.inventory.currentItem;

        if (this.mefedron.get() && shouldUsePotion(Potions.SPEED)) {
            sendPotion(Potions.SPEED);
        }
        if (this.hui.get() && shouldUsePotion(Potions.STRENGTH)) {
            sendPotion(Potions.STRENGTH);
        }
        if (this.litenergy.get() && shouldUsePotion(Potions.FIRE_RESIST)) {
            sendPotion(Potions.FIRE_RESIST);
        }

        mc.player.connection.sendPacket(new CHeldItemChangePacket(oldSlot));
        mc.playerController.syncCurrentPlayItem();
    }

    private void resetRotation() {
        if (originalRotation != null) {
            mc.player.rotationYaw = originalRotation[0];
            mc.player.rotationPitch = originalRotation[1];
            originalRotation = null;
            serverRotation = null;
        }
    }

    public boolean isActive() {
        return this.mefedron.get() || this.hui.get() || this.litenergy.get();
    }

    private boolean hasPotionInInventory(Potions potion) {
        int potionId = potion.getId();
        return this.findPotionSlot(potionId, true) != -1 || this.findPotionSlot(potionId, false) != -1;
    }

    private boolean canThrowPotion() {
        boolean isOnGround = !MoveUtils.isBlockUnder(0.5F) || mc.player.isOnGround();
        boolean timeIsReached = stopWatch.isReached(400);
        boolean ticksExisted = mc.player.ticksExisted > 100;
        return isOnGround && timeIsReached && ticksExisted;
    }

    private boolean shouldUsePotion(Potions potions) {
        return !mc.player.isPotionActive(potions.getPotion());
    }

    private void sendPotion(Potions potions) {
        int potionId = potions.getId();
        int hotBarSlot = findPotionSlot(potionId, true);

        if (hotBarSlot != -1) {
            mc.player.connection.sendPacket(new CHeldItemChangePacket(hotBarSlot));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);
            return;
        }

        int inventorySlot = findPotionSlot(potionId, false);
        if (inventorySlot != -1) {
            int bestSlotInHotBar = InventoryUtil.getInstance().findBestSlotInHotBar();
            InventoryUtil.moveItem(inventorySlot, bestSlotInHotBar + 36,
                    mc.player.inventory.getStackInSlot(bestSlotInHotBar).getItem() != Items.AIR);

            mc.player.connection.sendPacket(new CHeldItemChangePacket(bestSlotInHotBar));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);
        }
    }

    private int findPotionSlot(int id, boolean inHotBar) {
        int start = inHotBar ? 0 : 9;
        int end = inHotBar ? 9 : 36;

        for (int i = start; i < end; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof SplashPotionItem)) continue;

            List<EffectInstance> potionEffects = PotionUtils.getEffectsFromStack(stack);
            for (EffectInstance effectInstance : potionEffects) {
                if (effectInstance.getPotion() == Effect.get(id)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    public enum Potions {
        STRENGTH(Effects.STRENGTH, 5),
        SPEED(Effects.SPEED, 1),
        FIRE_RESIST(Effects.FIRE_RESISTANCE, 12);

        final Effect potion;
        final int id;

        Potions(Effect potion, int potionId) {
            this.potion = potion;
            this.id = potionId;
        }

        public Effect getPotion() {
            return potion;
        }

        public int getId() {
            return id;
        }
    }
}