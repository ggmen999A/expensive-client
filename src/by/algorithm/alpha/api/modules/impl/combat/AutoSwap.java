package by.algorithm.alpha.api.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventCooldown;
import by.algorithm.alpha.system.events.EventKey;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.BindSetting;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.system.utils.math.StopWatch;
import by.algorithm.alpha.system.utils.player.InventoryUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.*;
import net.minecraft.potion.Effects;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleAnnot(name = "AutoSwap", type = ModuleCategory.Combat, description = "Автоматический свап предметов по нажатию на бинд")
public class AutoSwap extends Module {
    final ModeSetting swapMode = new ModeSetting("Тип", "Умный", "Умный", "По бинду");
    final ModeSetting itemType = new ModeSetting("Предмет", "Щит", "Щит", "Геплы", "Тотем", "Шар");
    final ModeSetting swapType = new ModeSetting("Свапать на", "Геплы", "Щит", "Геплы", "Тотем", "Шар");
    final ModeSetting smartSwapMode = new ModeSetting("Умный выбор", "Включен", "Включен", "Отключен").setVisible(() ->
            (itemType.get().equals(swapType.get()) && (itemType.is("Тотем") || itemType.is("Шар"))) ||
                    (!itemType.get().equals(swapType.get()) && (itemType.is("Тотем") || itemType.is("Шар") || swapType.is("Тотем") || swapType.is("Шар"))));

    final BindSetting keyToSwap = new BindSetting("Кнопка", -1).setVisible(() -> swapMode.is("По бинду"));
    final SliderSetting health = new SliderSetting("Здоровье", 11.0F, 5.0F, 19.0F, 0.5F).setVisible(() -> swapMode.is("Умный"));
    final StopWatch stopWatch = new StopWatch();
    boolean shieldIsCooldown;
    int oldItem = -1;

    final StopWatch delay = new StopWatch();
    final StopWatch sprintDelay = new StopWatch();
    final AutoTotem autoTotem;

    private boolean needsSprintRestore = false;
    private boolean wasSprinting = false;

    public AutoSwap(AutoTotem autoTotem) {
        this.autoTotem = autoTotem;
        addSettings(swapMode, itemType, swapType, keyToSwap, health, smartSwapMode);
    }

    @Subscribe
    public void onEventKey(EventKey e) {
        if (!swapMode.is("По бинду") || mc.player == null) {
            return;
        }
        if (mc == null || mc.player == null || mc.world == null) {
            return;
        }

        ItemStack offhandItemStack = mc.player.getHeldItemOffhand();
        boolean isOffhandNotEmpty = !(offhandItemStack.getItem() instanceof AirItem);

        if (e.isKeyDown(keyToSwap.get()) && stopWatch.isReached(200)) {
            Item currentItem = offhandItemStack.getItem();
            Item swapItem = getSwapItem();
            Item selectedItem = getSelectedItem();

            boolean isHoldingSwapItem = currentItem == swapItem;
            boolean isHoldingSelectedItem = currentItem == selectedItem;

            int selectedItemSlot = getBestItemSlot(selectedItem);
            int swapItemSlot = getBestItemSlot(swapItem);

            if (selectedItemSlot >= 0) {
                if (!isHoldingSelectedItem) {
                    safeSwapItem(selectedItemSlot, 45, isOffhandNotEmpty);
                    stopWatch.reset();
                    return;
                }
            }
            if (swapItemSlot >= 0) {
                if (!isHoldingSwapItem) {
                    safeSwapItem(swapItemSlot, 45, isOffhandNotEmpty);
                    stopWatch.reset();
                }
            }
        }
    }

    @Subscribe
    private void onCooldown(EventCooldown e) {
        shieldIsCooldown = isCooldown(e);
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (!swapMode.is("Умный") || mc.player == null) {  // Add null check
            return;
        }

        // Восстановление спринта после задержки
        if (needsSprintRestore && sprintDelay.isReached(150)) {
            mc.player.setSprinting(wasSprinting);
            needsSprintRestore = false;
        }

        Item currentItem = mc.player.getHeldItemOffhand().getItem();

        if (stopWatch.isReached(400L)) {
            swapIfShieldIsBroken(currentItem);
            swapIfHealthToLow(currentItem);
            swapIfBetterItemAvailable(currentItem);
            stopWatch.reset();
        }
        boolean isRightClickWithGoldenAppleActive = false;

        if (currentItem == Items.GOLDEN_APPLE && !mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE)) {
            isRightClickWithGoldenAppleActive = mc.gameSettings.keyBindUseItem.isKeyDown();
        }

        if (isRightClickWithGoldenAppleActive) {
            stopWatch.reset();
        }
    }

    @Override
    public void onDisable() {
        shieldIsCooldown = false;
        oldItem = -1;
        needsSprintRestore = false;
        wasSprinting = false;
        super.onDisable();
    }

    private void swapIfBetterItemAvailable(Item currentItem) {
        if (!smartSwapMode.is("Включен")) {
            return;
        }

        boolean isOffhandNotEmpty = !(currentItem instanceof AirItem);
        ItemStack currentStack = mc.player.getHeldItemOffhand();

        // Если в руке тотем и надо менять на тотем
        if (itemType.is("Тотем") && swapType.is("Тотем") && currentItem == Items.TOTEM_OF_UNDYING) {
            int betterTotemSlot = getBetterTotemSlot(currentStack);
            if (betterTotemSlot >= 0) {
                safeSwapItem(betterTotemSlot, 45, isOffhandNotEmpty);
            }
        }

        // Если в руке шар и надо менять на шар
        if (itemType.is("Шар") && swapType.is("Шар") && currentItem == Items.PLAYER_HEAD) {
            int betterSphereSlot = getBetterSphereSlot(currentStack);
            if (betterSphereSlot >= 0) {
                safeSwapItem(betterSphereSlot, 45, isOffhandNotEmpty);
            }
        }

        // Если в руке шар а надо тотем, или наоборот
        if (!itemType.get().equals(swapType.get())) {
            if ((itemType.is("Шар") && swapType.is("Тотем")) || (itemType.is("Тотем") && swapType.is("Шар"))) {
                Item targetItem = getSwapItem();
                int betterItemSlot = getBestItemSlot(targetItem);

                if (betterItemSlot >= 0 && shouldSwapForBetterItem(currentStack, mc.player.inventory.getStackInSlot(betterItemSlot))) {
                    safeSwapItem(betterItemSlot, 45, isOffhandNotEmpty);
                    if (isOffhandNotEmpty && oldItem == -1) {
                        oldItem = betterItemSlot;
                    }
                }
            }
        }
    }

    private void swapIfHealthToLow(Item currentItem) {
        if (mc.player == null) return;
        boolean isOffhandNotEmpty = !(currentItem instanceof AirItem);
        boolean isHoldingGoldenApple = currentItem == getSwapItem();
        boolean isHoldingSelectedItem = currentItem == getSelectedItem();
        boolean gappleIsNotCooldown = !mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE);

        int goldenAppleSlot = getBestItemSlot(getSwapItem());

        if (shieldIsCooldown || !gappleIsNotCooldown) {
            return;
        }

        if (isLowHealth() && !isHoldingGoldenApple && isHoldingSelectedItem) {
            safeSwapItem(goldenAppleSlot, 45, isOffhandNotEmpty);
            if (isOffhandNotEmpty && oldItem == -1) {
                oldItem = goldenAppleSlot;
            }
        } else if (!isLowHealth() && isHoldingGoldenApple && oldItem >= 0) {
            safeSwapItem(oldItem, 45, isOffhandNotEmpty);
            oldItem = -1;
        }
    }

    private void swapIfShieldIsBroken(Item currentItem) {
        if (mc.player == null) return;
        boolean isOffhandNotEmpty = !(currentItem instanceof AirItem);
        boolean isHoldingGoldenApple = currentItem == getSwapItem();
        boolean isHoldingSelectedItem = currentItem == getSelectedItem();
        boolean gappleIsNotCooldown = !mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE);
        int goldenAppleSlot = getBestItemSlot(getSwapItem());

        if (shieldIsCooldown && !isHoldingGoldenApple && isHoldingSelectedItem && gappleIsNotCooldown) {
            safeSwapItem(goldenAppleSlot, 45, isOffhandNotEmpty);
            if (isOffhandNotEmpty && oldItem == -1) {
                oldItem = goldenAppleSlot;
            }
            print(shieldIsCooldown + "");
        } else if (!shieldIsCooldown && isHoldingGoldenApple && oldItem >= 0) {
            safeSwapItem(oldItem, 45, isOffhandNotEmpty);
            oldItem = -1;
        }
    }

    private boolean isLowHealth() {
        float currentHealth = mc.player.getHealth() + (mc.player.isPotionActive(Effects.ABSORPTION) ? mc.player.getAbsorptionAmount() : 0.0f);
        return currentHealth <= health.get();
    }

    private boolean isCooldown(EventCooldown cooldown) {
        Item item = cooldown.getItem();

        if (!itemType.is("Shield")) {
            return false;
        } else {
            return cooldown.isAdded() && item instanceof ShieldItem;
        }
    }

    private Item getSwapItem() {
        return getItemByType(swapType.get());
    }

    private Item getSelectedItem() {
        return getItemByType(itemType.get());
    }

    private Item getItemByType(String itemType) {
        return switch (itemType) {
            case "Щит" -> Items.SHIELD;
            case "Тотем" -> Items.TOTEM_OF_UNDYING;
            case "Геплы" -> Items.GOLDEN_APPLE;
            case "Шар" -> Items.PLAYER_HEAD;
            default -> Items.AIR;
        };
    }

    private int getBestItemSlot(Item item) {
        if (!smartSwapMode.is("Включен")) {
            return getSlot(item);
        }

        if (item == Items.TOTEM_OF_UNDYING) {
            return getBestTotemSlot();
        } else if (item == Items.PLAYER_HEAD) {
            return getBestSphereSlot();
        } else {
            return getSlot(item);
        }
    }

    private int getBestTotemSlot() {
        List<ItemSlotPair> totems = getAllItemsOfType(Items.TOTEM_OF_UNDYING);
        if (totems.isEmpty()) return -1;

        ItemSlotPair bestTotem = totems.get(0);
        double bestScore = evaluateTotemScore(bestTotem.stack);

        for (ItemSlotPair totem : totems) {
            double score = evaluateTotemScore(totem.stack);
            if (score > bestScore) {
                bestScore = score;
                bestTotem = totem;
            }
        }

        return bestTotem.slot;
    }

    private int getBetterTotemSlot(ItemStack currentTotem) {
        List<ItemSlotPair> totems = getAllItemsOfType(Items.TOTEM_OF_UNDYING);
        double currentScore = evaluateTotemScore(currentTotem);

        ItemSlotPair bestTotem = null;
        double bestScore = currentScore;

        for (ItemSlotPair totem : totems) {
            double score = evaluateTotemScore(totem.stack);
            if (score > bestScore) {
                bestScore = score;
                bestTotem = totem;
            }
        }

        return bestTotem != null ? bestTotem.slot : -1;
    }

    private int getBestSphereSlot() {
        List<ItemSlotPair> spheres = getAllItemsOfType(Items.PLAYER_HEAD);
        if (spheres.isEmpty()) return -1;

        ItemSlotPair bestSphere = spheres.get(0);
        double bestScore = evaluateSphereScore(bestSphere.stack);

        for (ItemSlotPair sphere : spheres) {
            double score = evaluateSphereScore(sphere.stack);
            if (score > bestScore) {
                bestScore = score;
                bestSphere = sphere;
            }
        }

        return bestSphere.slot;
    }

    private int getBetterSphereSlot(ItemStack currentSphere) {
        List<ItemSlotPair> spheres = getAllItemsOfType(Items.PLAYER_HEAD);
        double currentScore = evaluateSphereScore(currentSphere);

        ItemSlotPair bestSphere = null;
        double bestScore = currentScore;

        for (ItemSlotPair sphere : spheres) {
            double score = evaluateSphereScore(sphere.stack);
            if (score > bestScore) {
                bestScore = score;
                bestSphere = sphere;
            }
        }

        return bestSphere != null ? bestSphere.slot : -1;
    }

    private boolean shouldSwapForBetterItem(ItemStack currentItem, ItemStack newItem) {
        double currentScore = 0;
        double newScore = 0;

        if (currentItem.getItem() == Items.TOTEM_OF_UNDYING) {
            currentScore = evaluateTotemScore(currentItem);
        } else if (currentItem.getItem() == Items.PLAYER_HEAD) {
            currentScore = evaluateSphereScore(currentItem);
        }

        if (newItem.getItem() == Items.TOTEM_OF_UNDYING) {
            newScore = evaluateTotemScore(newItem);
        } else if (newItem.getItem() == Items.PLAYER_HEAD) {
            newScore = evaluateSphereScore(newItem);
        }

        return newScore > currentScore + 10; // Минимальная разница для свапа
    }

    private double evaluateTotemScore(ItemStack totem) {
        double score = 0;
        PlayerSituation situation = analyzePlayerSituation();

        Map<String, Integer> effects = getItemEffects(totem);

        for (Map.Entry<String, Integer> effect : effects.entrySet()) {
            String effectName = effect.getKey();
            int value = effect.getValue();

            switch (effectName.toLowerCase()) {
                case "health":
                case "absorption":
                    if (situation.needsHealth) {
                        score += value * 15; // Высокий приоритет здоровья при низком HP
                    } else {
                        score += value * 5;
                    }
                    break;
                case "strength":
                case "attack":
                    if (situation.inCombat) {
                        score += value * 12;
                    } else {
                        score += value * 3;
                    }
                    break;
                case "speed":
                case "swiftness":
                    if (situation.needsEscape) {
                        score += value * 10;
                    } else {
                        score += value * 6;
                    }
                    break;
                case "resistance":
                case "protection":
                    if (situation.takingDamage) {
                        score += value * 8;
                    } else {
                        score += value * 4;
                    }
                    break;
                case "regeneration":
                    if (situation.needsHealing) {
                        score += value * 7;
                    } else {
                        score += value * 3;
                    }
                    break;
            }

            // Негативные эффекты уменьшают счет
            if (value < 0) {
                score += value * 20; // Сильное наказание за негативные эффекты
            }
        }

        return score;
    }

    private double evaluateSphereScore(ItemStack sphere) {
        double score = 0;
        PlayerSituation situation = analyzePlayerSituation();

        Map<String, Integer> effects = getItemEffects(sphere);

        for (Map.Entry<String, Integer> effect : effects.entrySet()) {
            String effectName = effect.getKey();
            int value = effect.getValue();

            switch (effectName.toLowerCase()) {
                case "health":
                case "absorption":
                    if (situation.needsHealth) {
                        score += value * 12;
                    } else {
                        score += value * 4;
                    }
                    break;
                case "speed":
                case "swiftness":
                    if (situation.needsEscape) {
                        score += value * 15; // Сферы часто дают скорость
                    } else {
                        score += value * 8;
                    }
                    break;
                case "invisibility":
                    if (situation.needsEscape) {
                        score += value * 20;
                    } else {
                        score += value * 5;
                    }
                    break;
                case "jump":
                case "jumpboost":
                    if (situation.needsEscape) {
                        score += value * 8;
                    } else {
                        score += value * 3;
                    }
                    break;
            }

            if (value < 0) {
                score += value * 15;
            }
        }

        return score;
    }

    private PlayerSituation analyzePlayerSituation() {
        PlayerSituation situation = new PlayerSituation();

        float currentHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        float maxHealth = mc.player.getMaxHealth();

        situation.needsHealth = currentHealth < health.get();
        situation.needsHealing = currentHealth < maxHealth * 0.7f;
        situation.takingDamage = mc.player.hurtTime > 0;
        situation.inCombat = mc.player.getLastAttackedEntity() != null;
        situation.needsEscape = situation.needsHealth && (situation.inCombat || situation.takingDamage);

        return situation;
    }

    private Map<String, Integer> getItemEffects(ItemStack item) {
        Map<String, Integer> effects = new HashMap<>();

        if (item.hasTag()) {
            CompoundNBT tag = item.getTag();

            // Анализ кастомных тегов эффектов
            if (tag.contains("CustomEffects")) {
                ListNBT effectsList = tag.getList("CustomEffects", 10);
                for (int i = 0; i < effectsList.size(); i++) {
                    CompoundNBT effectTag = effectsList.getCompound(i);
                    String effectName = effectTag.getString("EffectName");
                    int effectValue = effectTag.getInt("EffectValue");
                    effects.put(effectName, effectValue);
                }
            }

            // Анализ стандартных зачарований и модификаторов
            if (tag.contains("AttributeModifiers")) {
                ListNBT modifiers = tag.getList("AttributeModifiers", 10);
                for (int i = 0; i < modifiers.size(); i++) {
                    CompoundNBT modifier = modifiers.getCompound(i);
                    String attribute = modifier.getString("AttributeName");
                    double amount = modifier.getDouble("Amount");

                    if (attribute.contains("health")) {
                        effects.put("health", (int) amount);
                    } else if (attribute.contains("speed")) {
                        effects.put("speed", (int) amount);
                    }
                }
            }
        }

        return effects;
    }

    private List<ItemSlotPair> getAllItemsOfType(Item item) {
        List<ItemSlotPair> items = new ArrayList<>();

        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == item) {
                int slot = i < 9 ? i + 36 : i;
                items.add(new ItemSlotPair(stack, slot));
            }
        }

        return items;
    }

    /**
     * Безопасно выполняет смену предмета с отключением спринта
     * @param fromSlot слот источника
     * @param toSlot слот назначения
     * @param isOffhandNotEmpty есть ли предмет в руке
     */
    private void safeSwapItem(int fromSlot, int toSlot, boolean isOffhandNotEmpty) {
        if (mc.player == null || mc.world == null) return;
        // Сохраняем текущее состояние спринта
        wasSprinting = mc.player.isSprinting();

        // Отключаем спринт перед свапом
        if (wasSprinting) {
            mc.player.setSprinting(false);
        }

        // Небольшая задержка для корректной обработки
        new Thread(() -> {
            try {
                Thread.sleep(50); // 50мс задержка
                InventoryUtil.moveItem(fromSlot, toSlot, isOffhandNotEmpty);

                // Устанавливаем флаг для восстановления спринта
                needsSprintRestore = true;
                sprintDelay.reset();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private int getSlot(Item item) {
        int finalSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == item) {
                if (mc.player.inventory.getStackInSlot(i).isEnchanted()) {
                    finalSlot = i;
                    break;
                } else {
                    finalSlot = i;
                }
            }
        }
        if (finalSlot < 9 && finalSlot != -1) {
            finalSlot = finalSlot + 36;
        }
        return finalSlot;
    }

    private static class ItemSlotPair {
        final ItemStack stack;
        final int slot;

        ItemSlotPair(ItemStack stack, int slot) {
            this.stack = stack;
            this.slot = slot;
        }
    }

    private static class PlayerSituation {
        boolean needsHealth;
        boolean needsHealing;
        boolean takingDamage;
        boolean inCombat;
        boolean needsEscape;
    }
}