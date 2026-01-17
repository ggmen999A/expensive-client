package by.algorithm.alpha.api.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.BindSetting;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import lombok.ToString;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

@ToString
@ModuleAnnot(name = "ElytraHelper", type = ModuleCategory.Misc, description = "Автоматизация работы с элитрами")
public class ElytraHelper extends Module {

    private final BindSetting swapBind = new BindSetting("Кнопка свапа", 0);
    private final BindSetting fireworkBind = new BindSetting("Кнопка фейерверка", 0);
    private final BooleanSetting autoTakeoff = new BooleanSetting("Авто взлет", true);
    private final BooleanSetting autoFirework = new BooleanSetting("Авто фейерверк", true);

    public ElytraHelper() {
        addSettings(swapBind, fireworkBind, autoTakeoff, autoFirework.setVisible(() -> autoTakeoff.get()));
    }

    private boolean wasSwapPressed = false;
    private boolean wasFireworkPressed = false;
    private boolean shouldTakeoff = false;
    private int takeoffDelay = 0;
    private int fireworkDelay = 0;
    private int originalSlot = -1;
    private boolean isProcessingFirework = false;
    private int fireworkOriginalSlot = -1;
    private boolean needToRestoreSwap = false;

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        handleSwapBind();
        handleFireworkBind();
        handleAutoTakeoff();
        handleFireworkUsage();
    }

    private void handleSwapBind() {
        if (swapBind.get() == 0) return;

        boolean isSwapPressed = isKeyPressed(swapBind.get());

        if (isSwapPressed && !wasSwapPressed) {
            performElytraSwap();
        }

        wasSwapPressed = isSwapPressed;
    }

    private void handleFireworkBind() {
        if (fireworkBind.get() == 0) return;

        boolean isFireworkPressed = isKeyPressed(fireworkBind.get());

        if (isFireworkPressed && !wasFireworkPressed && !isProcessingFirework) {
            startFireworkUsage();
        }

        wasFireworkPressed = isFireworkPressed;
    }

    private void handleAutoTakeoff() {
        if (shouldTakeoff && takeoffDelay > 0) {
            takeoffDelay--;
            if (takeoffDelay == 0) {
                performTakeoff();
                shouldTakeoff = false;
            }
        }
    }

    private void handleFireworkUsage() {
        if (isProcessingFirework && fireworkDelay > 0) {
            fireworkDelay--;
            if (fireworkDelay == 0) {
                restoreOriginalSlot();
                isProcessingFirework = false;
            }
        }
    }

    private void performElytraSwap() {
        ItemStack chestplate = mc.player.inventory.armorItemInSlot(2);

        if (chestplate.getItem() instanceof ElytraItem) {
            swapElytraToChestplate();
        } else {
            swapChestplateToElytra();
        }
    }

    private void swapElytraToChestplate() {
        int chestplateSlot = findChestplateInInventory();
        if (chestplateSlot != -1) {
            mc.playerController.windowClick(
                    mc.player.container.windowId,
                    chestplateSlot,
                    0,
                    ClickType.PICKUP,
                    mc.player
            );

            mc.playerController.windowClick(
                    mc.player.container.windowId,
                    6,
                    0,
                    ClickType.PICKUP,
                    mc.player
            );

            mc.playerController.windowClick(
                    mc.player.container.windowId,
                    chestplateSlot,
                    0,
                    ClickType.PICKUP,
                    mc.player
            );
        }
    }

    private void swapChestplateToElytra() {
        int elytraSlot = findElytraInInventory();
        if (elytraSlot != -1) {
            mc.playerController.windowClick(
                    mc.player.container.windowId,
                    elytraSlot,
                    0,
                    ClickType.PICKUP,
                    mc.player
            );

            mc.playerController.windowClick(
                    mc.player.container.windowId,
                    6,
                    0,
                    ClickType.PICKUP,
                    mc.player
            );

            mc.playerController.windowClick(
                    mc.player.container.windowId,
                    elytraSlot,
                    0,
                    ClickType.PICKUP,
                    mc.player
            );

            if (autoTakeoff.get()) {
                shouldTakeoff = true;
                takeoffDelay = 3;
            }
        }
    }

    private void performTakeoff() {
        if (mc.player.isOnGround()) {
            mc.player.jump();

            if (autoFirework.get()) {
                startFireworkUsage();
            }
        }
    }

    private void startFireworkUsage() {
        int fireworkSlot = findFireworkInInventory();
        if (fireworkSlot == -1) return;

        originalSlot = mc.player.inventory.currentItem;
        fireworkOriginalSlot = fireworkSlot;
        needToRestoreSwap = false;

        // Если фейерверк уже в хотбаре
        if (fireworkSlot < 9) {
            mc.player.inventory.currentItem = fireworkSlot;
        } else {
            // Фейерверк в инвентаре - делаем умный свап
            ItemStack currentItem = mc.player.inventory.getStackInSlot(originalSlot);

            if (!currentItem.isEmpty()) {
                // В руках что-то есть - свапаем с фейерверком
                swapItemsBetweenSlots(originalSlot, fireworkSlot);
                needToRestoreSwap = true;
                // Фейерверк теперь в originalSlot
                mc.player.inventory.currentItem = originalSlot;
            } else {
                // В руках пусто - просто перемещаем фейерверк в текущий слот
                swapItemsBetweenSlots(originalSlot, fireworkSlot);
                mc.player.inventory.currentItem = originalSlot;
            }
        }

        mc.playerController.processRightClick(mc.player, mc.world, Hand.MAIN_HAND);

        fireworkDelay = 2;
        isProcessingFirework = true;
    }

    private void restoreOriginalSlot() {
        if (originalSlot != -1 && originalSlot >= 0 && originalSlot < 9) {
            // Если делали свап - восстанавливаем предметы на места
            if (needToRestoreSwap && fireworkOriginalSlot != -1) {
                swapItemsBetweenSlots(originalSlot, fireworkOriginalSlot);
                needToRestoreSwap = false;
            }

            mc.player.inventory.currentItem = originalSlot;
            originalSlot = -1;
            fireworkOriginalSlot = -1;
        }
    }

    private int findElytraInInventory() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ElytraItem) {
                return i < 9 ? i + 36 : i;
            }
        }
        return -1;
    }

    private int findChestplateInInventory() {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ArmorItem) {
                ArmorItem armorItem = (ArmorItem) stack.getItem();
                if (armorItem.getEquipmentSlot().getIndex() == 2) {
                    return i < 9 ? i + 36 : i;
                }
            }
        }
        return -1;
    }

    private int findFireworkInInventory() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof FireworkRocketItem) {
                return i;
            }
        }

        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof FireworkRocketItem) {
                return i;
            }
        }

        return -1;
    }

    private int findFreeHotbarSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private void swapItemsBetweenSlots(int slot1, int slot2) {
        // Конвертируем слоты в правильные индексы для windowClick
        int windowSlot1 = slot1 < 9 ? slot1 + 36 : slot1;
        int windowSlot2 = slot2 < 9 ? slot2 + 36 : slot2;

        // Берем предмет из первого слота
        mc.playerController.windowClick(
                mc.player.container.windowId,
                windowSlot1,
                0,
                ClickType.PICKUP,
                mc.player
        );

        // Берем предмет из второго слота (теперь в курсоре первый предмет)
        mc.playerController.windowClick(
                mc.player.container.windowId,
                windowSlot2,
                0,
                ClickType.PICKUP,
                mc.player
        );

        // Кладем второй предмет в первый слот
        mc.playerController.windowClick(
                mc.player.container.windowId,
                windowSlot1,
                0,
                ClickType.PICKUP,
                mc.player
        );
    }

    // Исправленный метод для обработки кнопок мыши
    private boolean isKeyPressed(int keyCode) {
        if (keyCode == 0) return false;

        try {
            // Проверка кнопок мыши для различных соглашений кодирования

            // Соглашение 1: отрицательные значения (-99=ПКМ, -98=mouse4, -97=mouse5)
            if (keyCode < 0) {
                int mouseButton = getMouseButtonFromNegativeCode(keyCode);
                if (mouseButton != -1) {
                    return GLFW.glfwGetMouseButton(mc.getMainWindow().getHandle(), mouseButton) == GLFW.GLFW_PRESS;
                }
            }

            // Соглашение 2: значения >= 1000 (обычно 1001=ПКМ, 1003=mouse4, 1004=mouse5)
            if (keyCode >= 1000 && keyCode < 1010) {
                int mouseButton = getMouseButtonFromThousandCode(keyCode);
                if (mouseButton != -1) {
                    return GLFW.glfwGetMouseButton(mc.getMainWindow().getHandle(), mouseButton) == GLFW.GLFW_PRESS;
                }
            }

            // Соглашение 3: значения >= 100000
            if (keyCode >= 100000 && keyCode < 100010) {
                int mouseButton = keyCode - 100000;
                if (mouseButton >= 0 && mouseButton <= 7) {
                    return GLFW.glfwGetMouseButton(mc.getMainWindow().getHandle(), mouseButton) == GLFW.GLFW_PRESS;
                }
            }

            // Соглашение 4: прямые GLFW коды (0=ЛКМ, 1=ПКМ, 2=СКМ, 3=mouse4, 4=mouse5)
            if (keyCode >= 0 && keyCode <= 7) {
                return GLFW.glfwGetMouseButton(mc.getMainWindow().getHandle(), keyCode) == GLFW.GLFW_PRESS;
            }

            // Обычные клавиши клавиатуры
            if (keyCode > 7 && keyCode < 1000) {
                return InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyCode);
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // Преобразование отрицательных кодов в GLFW mouse button
    private int getMouseButtonFromNegativeCode(int keyCode) {
        switch (keyCode) {
            case -100: return GLFW.GLFW_MOUSE_BUTTON_LEFT;   // ЛКМ
            case -99:  return GLFW.GLFW_MOUSE_BUTTON_RIGHT;  // ПКМ
            case -98:  return GLFW.GLFW_MOUSE_BUTTON_5;      // Mouse5 (была перепутана)
            case -97:  return GLFW.GLFW_MOUSE_BUTTON_4;      // Mouse4 (была перепутана)
            case -96:  return GLFW.GLFW_MOUSE_BUTTON_MIDDLE; // СКМ
            default:   return -1;
        }
    }

    // Преобразование кодов >= 1000 в GLFW mouse button
    private int getMouseButtonFromThousandCode(int keyCode) {
        int offset = keyCode - 1000;
        switch (offset) {
            case 0: return GLFW.GLFW_MOUSE_BUTTON_LEFT;   // ЛКМ
            case 1: return GLFW.GLFW_MOUSE_BUTTON_RIGHT;  // ПКМ
            case 2: return GLFW.GLFW_MOUSE_BUTTON_MIDDLE; // СКМ
            case 3: return GLFW.GLFW_MOUSE_BUTTON_5;      // Mouse5 (исправлено)
            case 4: return GLFW.GLFW_MOUSE_BUTTON_4;      // Mouse4 (исправлено)
            default: return offset <= 7 ? offset : -1;
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        shouldTakeoff = false;
        takeoffDelay = 0;
        fireworkDelay = 0;
        isProcessingFirework = false;
        originalSlot = -1;
        fireworkOriginalSlot = -1;
        needToRestoreSwap = false;
        wasSwapPressed = false;
        wasFireworkPressed = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        shouldTakeoff = false;
        takeoffDelay = 0;
        fireworkDelay = 0;
        isProcessingFirework = false;
        if (originalSlot != -1) {
            restoreOriginalSlot();
        }
        originalSlot = -1;
        fireworkOriginalSlot = -1;
        needToRestoreSwap = false;
        wasSwapPressed = false;
        wasFireworkPressed = false;
    }
}