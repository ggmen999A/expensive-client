package by.algorithm.alpha.api.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventKey;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.BindSetting;
import by.algorithm.alpha.system.utils.math.StopWatch;
import by.algorithm.alpha.system.utils.player.InventoryUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleAnnot(name = "FTHelper", type = ModuleCategory.Misc, description = "Вспомогательный модуль для FunTime")
public class FTHelper extends Module {

    final BindSetting disorientationBind = new BindSetting("Дезориентация", -1);
    final BindSetting shulkerBind = new BindSetting("Шалкер", -1);
    final BindSetting trapBind = new BindSetting("Трапка", -1);
    final BindSetting plastBind = new BindSetting("Пласт", -1);
    final BindSetting dustBind = new BindSetting("Явная пыль", -1);
    final BindSetting crossbowBind = new BindSetting("Арбалет", -1);
    final BindSetting burpBind = new BindSetting("Отрыжка", -1);
    final BindSetting acidBind = new BindSetting("Серка", -1);

    final StopWatch usageDelay = new StopWatch();
    final StopWatch swapBackDelay = new StopWatch();

    // Состояние использования предметов
    boolean crossbowPressed = false;
    boolean awaitingSwapBack = false;
    int originalSlot = -1;
    int originalItemSlot = -1;
    ItemStack originalItem = ItemStack.EMPTY;

    public FTHelper() {
        addSettings(disorientationBind, shulkerBind, trapBind, plastBind,
                dustBind, crossbowBind, burpBind, acidBind);
    }

    @Subscribe
    public void onKey(EventKey e) {
        if (!usageDelay.isReached(300) || awaitingSwapBack) return;

        if (e.isKeyDown(disorientationBind.get())) {
            handleItemUsage("Дезориентация", null, ItemType.REGULAR);
        } else if (e.isKeyDown(shulkerBind.get())) {
            handleItemUsage("Shulker Box", Items.SHULKER_BOX, ItemType.REGULAR);
        } else if (e.isKeyDown(trapBind.get())) {
            handleItemUsage("Трапка", null, ItemType.REGULAR);
        } else if (e.isKeyDown(plastBind.get())) {
            handleItemUsage("Пласт", null, ItemType.REGULAR);
        } else if (e.isKeyDown(dustBind.get())) {
            handleItemUsage("Явная пыль", Items.SUGAR, ItemType.REGULAR);
        } else if (e.isKeyDown(burpBind.get())) {
            handleItemUsage("Зелье Отрыжки", null, ItemType.THROWABLE_POTION);
        } else if (e.isKeyDown(acidBind.get())) {
            handleItemUsage("Серная кислота", null, ItemType.THROWABLE_POTION);
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        // Обработка арбалета по зажатию клавиши
        if (isKeyPressed(crossbowBind.get()) && !crossbowPressed) {
            crossbowPressed = true;
            handleItemUsage("Арбалет", Items.CROSSBOW, ItemType.CROSSBOW);
        } else if (!isKeyPressed(crossbowBind.get()) && crossbowPressed) {
            crossbowPressed = false;
            mc.gameSettings.keyBindUseItem.setPressed(false);
            if (awaitingSwapBack) {
                swapBackDelay.reset();
            }
        }

        // Продолжение зажатия арбалета
        if (crossbowPressed && awaitingSwapBack) {
            mc.gameSettings.keyBindUseItem.setPressed(true);
        }

        // Проверка завершения использования и возврат предметов
        if (awaitingSwapBack && swapBackDelay.isReached(150)) {
            if (!crossbowPressed && (!mc.player.isHandActive() || mc.player.getActiveHand() != Hand.MAIN_HAND)) {
                performSwapBack();
            }
        }
    }

    private void handleItemUsage(String itemName, Item itemType, ItemType type) {
        int itemSlot = findItemSlot(itemName, itemType);

        if (itemSlot == -1) {
            print(getItemNotFoundMessage(itemName));
            return;
        }

        // Сохраняем состояние
        originalSlot = mc.player.inventory.currentItem;
        originalItemSlot = itemSlot;
        originalItem = mc.player.inventory.getStackInSlot(originalSlot).copy();

        // Выполняем обмен предметов
        performItemSwap(itemSlot);

        // Активируем предмет в зависимости от типа
        switch (type) {
            case REGULAR:
            case THROWABLE_POTION:
                activateItem();
                awaitingSwapBack = true;
                swapBackDelay.reset();
                break;
            case CROSSBOW:
                awaitingSwapBack = true;
                break;
        }

        usageDelay.reset();
    }

    private void performItemSwap(int itemSlot) {
        int currentSlot = mc.player.inventory.currentItem;

        if (itemSlot >= 36) {
            // Предмет в хотбаре
            int hotbarIndex = itemSlot - 36;
            if (hotbarIndex != currentSlot) {
                // Обмениваем предметы в хотбаре
                InventoryUtil.moveItem(currentSlot + 36, itemSlot, true);
                mc.player.inventory.currentItem = hotbarIndex;
            }
        } else {
            // Предмет в основном инвентаре
            InventoryUtil.moveItem(itemSlot, currentSlot + 36, true);
        }
    }

    private void performSwapBack() {
        if (originalSlot == -1 || originalItemSlot == -1) {
            resetState();
            return;
        }

        try {
            int currentSlot = mc.player.inventory.currentItem;

            if (originalItemSlot >= 36) {
                // Возвращаем предметы в хотбаре
                int hotbarIndex = originalItemSlot - 36;
                if (hotbarIndex != originalSlot) {
                    InventoryUtil.moveItem(currentSlot + 36, originalItemSlot, true);
                    mc.player.inventory.currentItem = originalSlot;
                }
            } else {
                // Возвращаем предмет в основной инвентарь
                InventoryUtil.moveItem(originalSlot + 36, originalItemSlot, true);
            }
        } catch (Exception ex) {
            print("Ошибка при возврате предмета: " + ex.getMessage());
        }

        resetState();
    }

    private void activateItem() {
        try {
            // Прямая активация предмета через playerController
            mc.playerController.processRightClick(mc.player, mc.world, Hand.MAIN_HAND);
        } catch (Exception ex) {
            // Резервный способ активации
            mc.gameSettings.keyBindUseItem.setPressed(true);

            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    mc.gameSettings.keyBindUseItem.setPressed(false);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    private boolean isKeyPressed(int keyCode) {
        if (keyCode == -1) return false;
        try {
            return org.lwjgl.glfw.GLFW.glfwGetKey(mc.getMainWindow().getHandle(), keyCode) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
        } catch (Exception e) {
            return false;
        }
    }

    private int findItemSlot(String itemName, Item itemType) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);

            if (stack.isEmpty()) continue;

            // Проверка по типу предмета (приоритет)
            if (itemType != null && stack.getItem() == itemType) {
                return i < 9 ? i + 36 : i;
            }

            // Проверка по отображаемому названию
            String displayName = stack.getDisplayName().getString();
            if (containsIgnoreCase(displayName, itemName)) {
                return i < 9 ? i + 36 : i;
            }

            // Проверка по NBT тегам для кастомных предметов
            if (stack.hasTag()) {
                CompoundNBT tag = stack.getTag();
                if (tag.contains("hud")) {
                    CompoundNBT display = tag.getCompound("hud");
                    if (display.contains("Name")) {
                        String customName = display.getString("Name");
                        if (containsIgnoreCase(customName, itemName)) {
                            return i < 9 ? i + 36 : i;
                        }
                    }
                }

                // Дополнительная проверка NBT для специальных предметов
                if (checkCustomNBT(tag, itemName)) {
                    return i < 9 ? i + 36 : i;
                }
            }
        }

        return -1;
    }

    private boolean containsIgnoreCase(String source, String target) {
        return source.toLowerCase().contains(target.toLowerCase());
    }

    private boolean checkCustomNBT(CompoundNBT tag, String itemName) {
        // Проверка дополнительных NBT тегов для кастомных предметов
        if (tag.contains("CustomName")) {
            String customName = tag.getString("CustomName");
            if (containsIgnoreCase(customName, itemName)) {
                return true;
            }
        }

        if (tag.contains("ItemName")) {
            String itemTag = tag.getString("ItemName");
            if (containsIgnoreCase(itemTag, itemName)) {
                return true;
            }
        }

        return false;
    }

    private String getItemNotFoundMessage(String itemName) {
        return switch (itemName) {
            case "Дезориентация" -> "Зелье дезориентации не найдено!";
            case "Shulker Box" -> "Шалкер не найден!";
            case "Трапка" -> "Трапка не найдена!";
            case "Пласт" -> "Пласт не найден!";
            case "Явная пыль" -> "Явная пыль не найдена!";
            case "Арбалет" -> "Арбалет не найден!";
            case "Зелье Отрыжки" -> "Зелье отрыжки не найдено!";
            case "Серная кислота" -> "Серная кислота не найдена!";
            default -> itemName + " не найден!";
        };
    }

    private void resetState() {
        awaitingSwapBack = false;
        originalSlot = -1;
        originalItemSlot = -1;
        originalItem = ItemStack.EMPTY;
    }

    @Override
    public void onDisable() {
        crossbowPressed = false;
        mc.gameSettings.keyBindUseItem.setPressed(false);
        resetState();
        super.onDisable();
    }

    private enum ItemType {
        REGULAR,
        THROWABLE_POTION,
        CROSSBOW
    }
}