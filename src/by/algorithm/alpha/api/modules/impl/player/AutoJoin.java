package by.algorithm.alpha.api.modules.impl.player;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventPacket;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.system.utils.math.StopWatch;
import by.algorithm.alpha.system.utils.client.IMinecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ChatType;

@ModuleAnnot(name = "AutoJoin", type = ModuleCategory.Player, description = "Автоматический заход на дуэли для SpookyTime")
public class AutoJoin extends Module implements IMinecraft {
    private final ModeSetting joinMode = new ModeSetting("Мод", "SpookyTime", "SpookyTime");
    private final StopWatch stopWatch = new StopWatch();
    private final StopWatch clickDelayWatch = new StopWatch();

    private boolean isWaitingForMenu = false;
    private boolean hasClickedCompass = false;
    private boolean wasInMenuScreen = false;
    private int attemptCount = 0;

    public AutoJoin() {
        addSettings(joinMode);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SChatPacket chatPacket) {
            if (chatPacket.getType() == ChatType.SYSTEM && chatPacket.getChatComponent().getString().contains("Сервер заполнен!")) {
                resetState();
            }
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        if (checkScoreboardForDuels()) {
            print("Зашел на дуэли за " + attemptCount + " попыток");
            toggle();
            return;
        }

        if (wasInMenuScreen && !(mc.currentScreen instanceof ContainerScreen<?>)) {
            resetState();
            wasInMenuScreen = false;
        }

        if (hasClickedCompass && !isWaitingForMenu) {
            if (mc.currentScreen instanceof ContainerScreen<?>) {
                isWaitingForMenu = true;
                stopWatch.reset();
            } else if (clickDelayWatch.isReached(1)) {
                resetState();
            }
            return;
        }

        if (!isWaitingForMenu && !hasClickedCompass) {
            int compassSlot = getCompassSlot();
            if (compassSlot == -1) return;

            if (!clickDelayWatch.isReached(1)) return;

            int hotbarSlot = compassSlot < 9 ? compassSlot : compassSlot - 36;
            if (mc.player.inventory.currentItem != hotbarSlot) {
                mc.player.inventory.currentItem = hotbarSlot;
                return;
            }

            mc.playerController.processRightClick(mc.player, mc.world, Hand.MAIN_HAND);
            hasClickedCompass = true;
            clickDelayWatch.reset();
            return;
        }

        if (isWaitingForMenu && mc.currentScreen instanceof ContainerScreen<?> screen && stopWatch.isReached(200)) {
            Container container = screen.getContainer();
            int targetSlot = 14;

            if (targetSlot >= 0 && targetSlot < container.inventorySlots.size()) {
                mc.playerController.windowClick(container.windowId, targetSlot, 0, ClickType.PICKUP, mc.player);
                wasInMenuScreen = true;
                attemptCount++;
                resetState();
            }
        }
    }

    private void resetState() {
        isWaitingForMenu = false;
        hasClickedCompass = false;
        clickDelayWatch.reset();
    }

    private boolean checkScoreboardForDuels() {
        if (mc.world == null) return false;

        try {
            Scoreboard scoreboard = mc.world.getScoreboard();
            if (scoreboard == null) return false;

            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
            if (objective == null) return false;

            String displayName = objective.getDisplayName().getString();
            if (displayName.contains("Дуэли")) {
                return true;
            }

            return scoreboard.getSortedScores(objective).stream()
                    .anyMatch(score -> score.getPlayerName().contains("Дуэли"));

        } catch (Exception ex) {
            return false;
        }
    }

    private int getCompassSlot() {
        for (int i = 0; i < mc.player.inventory.mainInventory.size(); i++) {
            ItemStack stack = mc.player.inventory.mainInventory.get(i);
            if (stack.getItem() == Items.COMPASS) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        attemptCount = 0;
        resetState();
        wasInMenuScreen = false;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        resetState();
        wasInMenuScreen = false;
    }
}