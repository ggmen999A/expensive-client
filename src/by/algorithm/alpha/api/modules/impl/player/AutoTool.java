package by.algorithm.alpha.api.modules.impl.player;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import net.minecraft.block.Block;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.math.BlockRayTraceResult;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import net.minecraft.item.ItemStack;

@ModuleAnnot(name = "AutoTool", type = ModuleCategory.Player, description = "Автоматический выбор подходящего предмета в нужный случай")
public class AutoTool extends Module {

    public final BooleanSetting silent = new BooleanSetting("Незаметный", true);

    public int itemIndex = -1, oldSlot = -1;
    boolean status;
    boolean clicked;
    private ItemStack swappedItem = null;
    private int swappedSlot = -1;

    public AutoTool() {
        addSettings(silent);
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.player.isCreative()) {
            resetState();
            return;
        }

        if (isMousePressed()) {
            if (itemIndex == -1) {
                int[] bestToolInfo = findBestToolSlot();
                if (bestToolInfo != null) {
                    status = true;
                    oldSlot = mc.player.inventory.currentItem;

                    if (bestToolInfo[0] < 9) {
                        // Инструмент уже в хотбаре
                        itemIndex = bestToolInfo[0];
                    } else {
                        // Инструмент в инвентаре - перемещаем в хотбар
                        itemIndex = oldSlot; // Используем текущий слот хотбара для обмена
                        swappedSlot = bestToolInfo[0];
                        swappedItem = mc.player.inventory.getStackInSlot(itemIndex);

                        // Обмен предметов
                        if (silent.get()) {
                            // Симулируем клик для обмена предметов
                            mc.playerController.windowClick(
                                    mc.player.container.windowId,
                                    swappedSlot, 0,
                                    net.minecraft.inventory.container.ClickType.SWAP,
                                    mc.player
                            );
                            mc.playerController.windowClick(
                                    mc.player.container.windowId,
                                    itemIndex + 36, 0, // 36 - это первый слот хотбара в окне инвентаря
                                    net.minecraft.inventory.container.ClickType.SWAP,
                                    mc.player
                            );
                        } else {
                            // Реальный обмен
                            ItemStack toolItem = mc.player.inventory.getStackInSlot(swappedSlot);
                            mc.player.inventory.setInventorySlotContents(itemIndex, toolItem);
                            mc.player.inventory.setInventorySlotContents(swappedSlot, swappedItem);
                        }
                    }

                    if (silent.get()) {
                        mc.player.connection.sendPacket(new CHeldItemChangePacket(itemIndex));
                    } else {
                        mc.player.inventory.currentItem = itemIndex;
                    }
                }
            }
        } else if (status && oldSlot != -1) {
            // Возвращаем предметы на место, если нужно
            if (swappedItem != null && swappedSlot != -1) {
                if (silent.get()) {
                    // Возвращаем обмен обратно
                    mc.playerController.windowClick(
                            mc.player.container.windowId,
                            itemIndex + 36, 0,
                            net.minecraft.inventory.container.ClickType.SWAP,
                            mc.player
                    );
                    mc.playerController.windowClick(
                            mc.player.container.windowId,
                            swappedSlot, 0,
                            net.minecraft.inventory.container.ClickType.SWAP,
                            mc.player
                    );
                } else {
                    // Реальный возврат
                    ItemStack currentItem = mc.player.inventory.getStackInSlot(itemIndex);
                    mc.player.inventory.setInventorySlotContents(swappedSlot, currentItem);
                    mc.player.inventory.setInventorySlotContents(itemIndex, swappedItem);
                }
                swappedItem = null;
                swappedSlot = -1;
            }

            // Возвращаем выбранный слот
            if (silent.get()) {
                mc.player.connection.sendPacket(new CHeldItemChangePacket(oldSlot));
            } else {
                mc.player.inventory.currentItem = oldSlot;
            }

            resetState();
        }
    }

    @Override
    public void onDisable() {
        resetState();
        super.onDisable();
    }

    private void resetState() {
        status = false;
        itemIndex = -1;
        oldSlot = -1;
        swappedItem = null;
        swappedSlot = -1;
    }

    private int[] findBestToolSlot() {
        if (mc.objectMouseOver instanceof BlockRayTraceResult blockRayTraceResult) {
            Block block = mc.world.getBlockState(blockRayTraceResult.getPos()).getBlock();

            int bestSlot = -1;
            float bestSpeed = 1.0f;

            // Проверяем весь инвентарь (0-35 - основной инвентарь, 36-44 - хотбар)
            for (int slot = 0; slot < 36; slot++) {
                ItemStack stack = mc.player.inventory.getStackInSlot(slot);
                if (stack.isEmpty()) continue;

                float speed = stack.getDestroySpeed(block.getDefaultState());
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = slot;
                }
            }

            return bestSlot == -1 ? null : new int[]{bestSlot};
        }
        return null;
    }

    private boolean isMousePressed() {
        return mc.objectMouseOver != null && mc.gameSettings.keyBindAttack.isKeyDown();
    }
}