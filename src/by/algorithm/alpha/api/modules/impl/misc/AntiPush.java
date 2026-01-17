package by.algorithm.alpha.api.modules.impl.misc;

import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.ModeListSetting;
import lombok.Getter;

@Getter
@ModuleAnnot(name = "AntiPush", type = ModuleCategory.Player, description = "Отключает отталкивание коллизиями от блоков")
public class AntiPush extends Module {

    private final ModeListSetting modes = new ModeListSetting("Тип",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Вода", false),
            new BooleanSetting("Блоки", true));

    public AntiPush() {
        addSettings(modes);
    }

}
