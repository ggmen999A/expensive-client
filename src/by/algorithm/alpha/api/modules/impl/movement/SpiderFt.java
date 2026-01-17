package by.algorithm.alpha.api.modules.impl.movement;

import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.system.events.EventMotion;
import by.algorithm.alpha.system.utils.math.StopWatch;
import by.algorithm.alpha.system.utils.player.MouseUtil;
import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;

@ModuleAnnot(name = "SpiderFt", type = ModuleCategory.Movement, description = "Человек пук")
public class SpiderFt extends Module {

    private final StopWatch stopWatch = new StopWatch();

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Subscribe
    private void onMotion(EventMotion motion) {
        if (!mc.player.collidedHorizontally) return;

        if (stopWatch.isReached(350L)) {
            motion.setOnGround(true);
            mc.player.setOnGround(true);
            mc.player.collidedVertically = true;
            mc.player.collidedHorizontally = true;
            mc.player.isAirBorne = true;
            mc.player.jump();
            stopWatch.reset();

            int blockSlot = getBlockSlot(true);
            if (blockSlot != -1 && mc.player.fallDistance > 0 && mc.player.fallDistance < 1.5f) {
                placeBlocks(motion, blockSlot);
            }
        }
    }

    private void placeBlocks(EventMotion motion, int slot) {
        int last = mc.player.inventory.currentItem;
        mc.player.inventory.currentItem = slot;
        motion.setPitch(80);
        motion.setYaw(mc.player.getHorizontalFacing().getHorizontalAngle());
        BlockRayTraceResult r = (BlockRayTraceResult) MouseUtil.rayTrace(4, motion.getYaw(), motion.getPitch(), mc.player);
        mc.player.swingArm(Hand.MAIN_HAND);
        mc.playerController.processRightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, r);
        mc.player.inventory.currentItem = last;
        mc.player.fallDistance = 0;
    }

    public int getBlockSlot(boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;

        for (int i = firstSlot; i < lastSlot; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            Item item = stack.getItem();

            if (item == Items.REPEATING_COMMAND_BLOCK) {
                return i;
            }
        }
        return -1;
    }
}
