package by.algorithm.alpha.api.modules.impl.misc;

import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.api.modules.settings.impl.StringSetting;
import by.algorithm.alpha.system.events.AttackEvent;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.play.client.CChatMessagePacket;

import java.util.HashSet;
import java.util.Set;

@ModuleAnnot(name = "AutoBan", type = ModuleCategory.Misc, description = "Автоматически банит игрока при ударе по нему (через пакеты, без чата)")
public class AutoBan extends Module {

    private final BooleanSetting onlyPlayers = new BooleanSetting("Только игроки", true);
    private final StringSetting reason = new StringSetting("Причина", "Cheating", "Причина для команды /ban");
    private final StringSetting banCommand = new StringSetting("Команда", "/ban", "Команда бана (можно /tempban, /mute и т.д.)");
    private final BooleanSetting cooldown = new BooleanSetting("Кулдаун", true);
    private final SliderSetting delay = new SliderSetting("Задержка (мс)", 0, 0, 1000, 50);
    private final Set<String> bannedPlayers = new HashSet<>();

    public AutoBan() {
        addSettings(onlyPlayers, reason, banCommand, cooldown, delay);
    }

    @Subscribe
    private void onAttack(AttackEvent e) {
        if (mc.player == null || mc.world == null) return;

        // Полная защита от self-ban
        if (e.entity == mc.player) return;

        if (onlyPlayers.get() && !(e.entity instanceof PlayerEntity)) return;

        if (!(e.entity instanceof PlayerEntity player)) return;

        String playerName = player.getName().getString();

        // Защита от бана самого себя по нику (на случай ошибок)
        if (playerName.equalsIgnoreCase(mc.player.getName().getString())) return;

        if (cooldown.get() && bannedPlayers.contains(playerName)) return;

        String fullCommand = banCommand.get() + " " + playerName + " " + reason.get();

        // Задержка, если нужно
        if (delay.get() > 0) {
            new Thread(() -> {
                try {
                    Thread.sleep(delay.get().longValue());
                } catch (InterruptedException ignored) {}
                sendSilentCommand(fullCommand);
            }).start();
        } else {
            sendSilentCommand(fullCommand);
        }

        if (cooldown.get()) {
            bannedPlayers.add(playerName);
        }
    }

    private void sendSilentCommand(String command) {
        if (mc.player == null || mc.player.connection == null) return;
        // Отправка напрямую пакетом — команда выполняется silently (без вывода в чат и без локального эха)
        mc.player.connection.sendPacket(new CChatMessagePacket(command));
    }

    @Override
    public void onDisable() {
        bannedPlayers.clear();
    }
}