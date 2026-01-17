package by.algorithm.alpha.api.modules.impl.misc;


import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.settings.impl.StringSetting;
import by.algorithm.alpha.system.events.EventPacket;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.play.server.SChatPacket;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@ModuleAnnot(name = "AutoRegister", type = ModuleCategory.Misc, description = "Автоматичиски вводит /reg ")
public class AutoRegister extends Module {

    private final StringSetting password = new StringSetting("Пароль", "", "Введите сюда пароль для регистрации", false);

    public AutoRegister() {
        addSettings(password);
    }

    @Subscribe
    public void onPacket(EventPacket event) {

        if (event.getPacket() instanceof SChatPacket chatPacket) {
            String message = chatPacket.getChatComponent().getString().toLowerCase();
            String pass = password.get();

            if (message.contains("/reg") || message.contains("/register") || message.contains("зарегистрируйтесь")) {

                if (message.contains("логин") || message.contains("/login")) {
                    return;
                }

                mc.player.sendChatMessage("/reg " + pass + " " + pass);
            }
        }
    }
}