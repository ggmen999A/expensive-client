package by.algorithm.alpha.api.modules.impl.misc;


import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.api.modules.settings.impl.StringSetting;
import by.algorithm.alpha.system.events.EventUpdate;
import com.google.common.eventbus.Subscribe;

@ModuleAnnot(name = "AutoMessage", type = ModuleCategory.Misc)
public class AutoMessage extends Module {

    private final StringSetting text = new StringSetting("Сообщение", "", "Текст для отправки");

    private final SliderSetting delay = new SliderSetting("Задержка (мин)", 1, 1, 60, 1);

    private long lastSendTime;

    public AutoMessage() {
        addSettings(text, delay);
    }

    @Override
    public void onEnable() {
        lastSendTime = System.currentTimeMillis();
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (mc.player == null || mc.world == null) return;

        if (text.get().isEmpty()) {
            return;
        }

        long timeDelay = (long) (delay.get() * 60000L);

        if (System.currentTimeMillis() - lastSendTime > timeDelay) {

            mc.player.sendChatMessage(text.get());

            lastSendTime = System.currentTimeMillis();
        }
    }
}