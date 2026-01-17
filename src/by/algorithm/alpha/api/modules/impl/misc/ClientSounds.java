package by.algorithm.alpha.api.modules.impl.misc;

import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;

@ModuleAnnot(name = "ClientSounds", type = ModuleCategory.Misc, description = "Звуки клиента при включении функций")
public class ClientSounds extends Module {

    public ModeSetting mode = new ModeSetting("Тип", "Обычный", "Обычный", "Пузырьки");
    public SliderSetting volume = new SliderSetting("Громкость", 70.0f, 0.0f, 100.0f, 1.0f);

    public ClientSounds() {
        addSettings(mode, volume);
    }


    public String getFileName(boolean state) {
        switch (mode.get()) {
            case "Обычный" -> {
                return state ? "enable" : "disable".toString();
            }
            case "Пузырьки" -> {
                return state ? "enableBubbles" : "disableBubbles";
            }
        }
        return "";
    }
}
