package by.algorithm.alpha.api.modules.impl.misc;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.system.events.EventPacket;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import lombok.ToString;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.inventory.container.ClickType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;

@ToString
@ModuleAnnot(name = "AutoDuel", type = ModuleCategory.Misc, description = "Автоматически вызывает игроков на дуэль")
public class AutoDuel extends Module {

    private final ModeSetting duelType = new ModeSetting("Тип дуэли", "Щит",
            "Щит", "Шипы", "Лук", "Тотемы", "Без дебаффов", "Шары", "Классик", "Читерский рай", "Незерка");

    private final BooleanSetting randomPlayer = new BooleanSetting("Случайный игрок", true);

    private final Pattern validNamePattern = Pattern.compile("^[a-zA-Z0-9_]+$");

    public AutoDuel() {
        addSettings(duelType, randomPlayer);
    }

    private int ticks = 0;
    private boolean waitingForMenu = false;
    private boolean menuProcessed = false;
    private boolean waitingForSetupMenu = false;
    private boolean setupMenuProcessed = false;
    private String currentTarget = "";
    private boolean firstRun = true;
    private final Random random = new Random();
    private final Set<String> usedPlayers = new HashSet<>();
    private final Set<String> failedPlayers = new HashSet<>();

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        ticks++;

        if (waitingForMenu) {
            if (mc.currentScreen instanceof ChestScreen) {
                ChestScreen chest = (ChestScreen) mc.currentScreen;
                if (chest.getTitle().getString().contains("Выбор набора") || chest.getTitle().getString().contains("дуэл")) {
                    if (!menuProcessed) {
                        processDuelMenu();
                        menuProcessed = true;
                    }
                }
            }
            return;
        }

        if (waitingForSetupMenu) {
            if (mc.currentScreen instanceof ChestScreen) {
                ChestScreen chest = (ChestScreen) mc.currentScreen;
                if (chest.getTitle().getString().contains("Настройка поединка")) {
                    if (!setupMenuProcessed) {
                        processSetupMenu();
                        setupMenuProcessed = true;
                    }
                }
            }
            return;
        }

        if (firstRun) {
            sendDuelRequest();
            firstRun = false;
            ticks = 0;
            return;
        }

        if (ticks >= 15) {
            sendDuelRequest();
            ticks = 0;
        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SChatPacket) {
            SChatPacket chatPacket = (SChatPacket) e.getPacket();
            String message = chatPacket.getChatComponent().getString();

            if (message.contains("Вы уже отправили запрос") || message.contains("невозможно вызвать") || message.contains("не принял") || message.contains("уже на дуэли") || message.contains("не найден") || message.contains("уже участвует")) {
                if (!currentTarget.isEmpty()) {
                    failedPlayers.add(currentTarget);
                    resetStateForNextDuel();
                }
            }
        }
    }

    private void sendDuelRequest() {
        if (!randomPlayer.get()) return;

        List<String> players = getValidPlayers();
        if (players.isEmpty()) {
            resetState();
            return;
        }

        String targetName = players.get(random.nextInt(players.size()));
        currentTarget = targetName;

        if (failedPlayers.contains(currentTarget)) {
            resetStateForNextDuel();
            return;
        }

        String command = "/duel " + currentTarget;
        mc.player.connection.sendPacket(new CChatMessagePacket(command));

        waitingForMenu = true;
        menuProcessed = false;
    }

    private void processDuelMenu() {
        if (!(mc.currentScreen instanceof ChestScreen)) return;

        ChestScreen chest = (ChestScreen) mc.currentScreen;
        int slotToClick = getDuelTypeSlot();

        mc.playerController.windowClick(
                chest.getContainer().windowId,
                slotToClick,
                0,
                ClickType.PICKUP,
                mc.player
        );

        waitingForMenu = false;
        menuProcessed = false;
        waitingForSetupMenu = true;
        setupMenuProcessed = false;
    }

    private void processSetupMenu() {
        if (!(mc.currentScreen instanceof ChestScreen)) return;

        ChestScreen chest = (ChestScreen) mc.currentScreen;

        mc.playerController.windowClick(
                chest.getContainer().windowId,
                0,
                0,
                ClickType.PICKUP,
                mc.player
        );
        resetStateForNextDuel();
    }

    private int getDuelTypeSlot() {
        return switch (duelType.get()) {
            case "Щит" -> 0;
            case "Шипы" -> 1;
            case "Лук" -> 2;
            case "Тотемы" -> 3;
            case "Без дебаффов" -> 4;
            case "Шары" -> 5;
            case "Классик" -> 6;
            case "Читерский рай" -> 7;
            case "Незерка" -> 8;
            default -> 0;
        };
    }

    private boolean isValidPlayerName(String playerName) {
        if (playerName == null || playerName.isEmpty()) return false;
        if (playerName.length() < 3 || playerName.length() > 16) return false;
        return validNamePattern.matcher(playerName).matches();
    }

    private List<String> getValidPlayers() {
        List<String> validPlayers = new ArrayList<>();

        if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.getPlayerInfoMap().forEach(playerInfo -> {
                String playerName = playerInfo.getGameProfile().getName();
                if (!playerName.equals(mc.player.getName().getString()) &&
                        isValidPlayerName(playerName) &&
                        !usedPlayers.contains(playerName) &&
                        !failedPlayers.contains(playerName)) {
                    validPlayers.add(playerName);
                }
            });
        }

        return validPlayers;
    }

    private void resetState() {
        waitingForMenu = false;
        menuProcessed = false;
        waitingForSetupMenu = false;
        setupMenuProcessed = false;
        currentTarget = "";
    }

    private void resetStateForNextDuel() {
        waitingForMenu = false;
        menuProcessed = false;
        waitingForSetupMenu = false;
        setupMenuProcessed = false;
        currentTarget = "";
    }

    @Override
    public void onEnable() {
        super.onEnable();
        resetState();
        firstRun = true;
        ticks = 0;
        usedPlayers.clear();
        failedPlayers.clear();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        resetState();
        usedPlayers.clear();
        failedPlayers.clear();
    }
}