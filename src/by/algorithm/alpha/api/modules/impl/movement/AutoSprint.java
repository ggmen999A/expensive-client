package by.algorithm.alpha.api.modules.impl.movement;

import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;

@ModuleAnnot(name = "Sprint", type = ModuleCategory.Movement, description = "Автоматический спринт")
public class AutoSprint extends Module {
    public BooleanSetting saveSprint = new BooleanSetting("Сохранять спринт", true);
    public AutoSprint() {
        addSettings(saveSprint);
    }
}
