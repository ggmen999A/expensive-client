package by.algorithm.alpha.api.modules.impl.player;

import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.system.utils.math.StopWatch;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.WorkbenchContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

@ModuleAnnot(name = "AutoCraft", type = ModuleCategory.Player, description = "Скидаем дельту")
public class AutoCraft extends Module {

    public ModeSetting mode = new ModeSetting("Мод", "Пласт", "Пласт", "Трапка");

    private final StopWatch timer = new StopWatch();
    private BlockPos tablePos;

    public AutoCraft() {
        addSettings(mode);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        tablePos = null;
        timer.reset();
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null) return;

        if (tablePos == null) {
            findTable();
            if (tablePos == null) {
                toggle();
                return;
            }
        }

        if (!(mc.player.openContainer instanceof WorkbenchContainer)) {
            openTable();
            return;
        }

        Container container = mc.player.openContainer;


        ItemStack result = container.getSlot(0).getStack();
        if (!result.isEmpty()) {
            clickSlot(0, 0, ClickType.QUICK_MOVE);
            mc.player.closeScreen();
            return;
        }


        for (int i = 1; i <= 9; i++) {
            if (!container.getSlot(i).getStack().isEmpty()) {
                return;
            }
        }


        Item centerItem;
        if (mode.is("Пласт")) {
            centerItem = Items.DIAMOND;
        } else { // Трапка
            centerItem = Items.NETHERITE_INGOT;
        }

        Item obsidian = Item.getItemFromBlock(Blocks.OBSIDIAN);

        int obsidianCount = countItem(obsidian, 10, 45);
        int centerSlot = findItem(centerItem, 10, 45);

        if (obsidianCount < 8 || centerSlot == -1) {
            mc.player.closeScreen();
            toggle();
            return;
        }

        int obsidianSlot = findItem(obsidian, 10, 45);


        clickSlot(obsidianSlot, 0, ClickType.PICKUP);
        for (int slot = 1; slot <= 9; slot++) {
            if (slot == 5) continue;
            clickSlot(slot, 1, ClickType.PICKUP);
        }
        clickSlot(obsidianSlot, 0, ClickType.PICKUP);


        clickSlot(centerSlot, 0, ClickType.PICKUP);
        clickSlot(5, 0, ClickType.PICKUP);
    }

    private void findTable() {
        BlockPos p = mc.player.getPosition();
        for (int x = -5; x <= 5; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos pos = p.add(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock() == Blocks.CRAFTING_TABLE) {
                        tablePos = pos;
                        return;
                    }
                }
            }
        }
    }

    private void openTable() {
        if (timer.isReached(500)) {
            Vector3d hitVec = new Vector3d(
                    tablePos.getX() + 0.5,
                    tablePos.getY() + 1.0,
                    tablePos.getZ() + 0.5
            );
            BlockRayTraceResult ray = new BlockRayTraceResult(hitVec, Direction.UP, tablePos, false);
            mc.player.connection.sendPacket(
                    new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND, ray)
            );
            timer.reset();
        }
    }

    private int findItem(Item item, int start, int end) {
        Container container = mc.player.openContainer;
        for (int i = start; i <= end; i++) {
            ItemStack stack = container.getSlot(i).getStack();
            if (!stack.isEmpty() && stack.getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    private int countItem(Item item, int start, int end) {
        int count = 0;
        Container container = mc.player.openContainer;
        for (int i = start; i <= end; i++) {
            ItemStack stack = container.getSlot(i).getStack();
            if (!stack.isEmpty() && stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void clickSlot(int slot, int button, ClickType type) {
        mc.playerController.windowClick(
                mc.player.openContainer.windowId,
                slot,
                button,
                type,
                mc.player
        );
    }
}
