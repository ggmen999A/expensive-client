package by.algorithm.alpha.api.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.system.utils.math.StopWatch;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleAnnot(name = "BowSpammer", type = ModuleCategory.Combat, description = "Автоматический спам лука с настраиваемой скоростью")
public class BowSpammer extends Module {
    final SliderSetting bowSpeed = new SliderSetting("Скорость лука", 3.0f, 1.0f, 20.0f, 0.1f);
    final StopWatch stopWatch = new StopWatch();

    boolean isDrawing = false;

    public BowSpammer() {
        addSettings(bowSpeed);
    }

    @Subscribe
    private void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null) {
            reset();
            return;
        }

        ItemStack mainHandItem = mc.player.getHeldItem(Hand.MAIN_HAND);
        ItemStack offHandItem = mc.player.getHeldItem(Hand.OFF_HAND);

        // Проверяем, есть ли лук в руках
        boolean hasBow = (mainHandItem.getItem() instanceof BowItem) || (offHandItem.getItem() instanceof BowItem);

        if (!hasBow) {
            reset();
            return;
        }

        // Проверяем, зажата ли ПКМ пользователем
        boolean rightClickPressed = mc.gameSettings.keyBindUseItem.isKeyDown();

        if (rightClickPressed) {
            if (!isDrawing) {
                // Начинаем натяжение лука
                isDrawing = true;
                stopWatch.reset();
            } else if (mc.player.isHandActive()) {
                // Лук натягивается, проверяем время
                long drawTime = (long) (1000.0f / bowSpeed.get()); // Время в миллисекундах

                if (stopWatch.isReached(drawTime)) {
                    // Принудительно отпускаем лук для выстрела
                    performBowRelease();

                    // Сбрасываем для нового цикла
                    stopWatch.reset();
                    isDrawing = false; // Сбрасываем, чтобы начать новый цикл
                }
            }
        } else {
            reset();
        }
    }

    private void performBowRelease() {
        try {
            // Способ 1: Прямой вызов отпускания лука
            if (mc.player.isHandActive()) {
                Hand activeHand = mc.player.getActiveHand();
                ItemStack activeStack = mc.player.getHeldItem(activeHand);

                if (activeStack.getItem() instanceof BowItem) {
                    // Симулируем отпускание ПКМ через playerController
                    mc.playerController.onStoppedUsingItem(mc.player);
                }
            }
        } catch (Exception e) {
            // Если основной способ не работает, пробуем альтернативный
            mc.player.resetActiveHand();
        }
    }

    @Override
    public void onDisable() {
        reset();
        // Останавливаем использование предмета при отключении функции
        if (mc.player != null && mc.player.isHandActive()) {
            mc.player.resetActiveHand();
        }
        super.onDisable();
    }

    private void reset() {
        isDrawing = false;
        stopWatch.reset();
    }
}