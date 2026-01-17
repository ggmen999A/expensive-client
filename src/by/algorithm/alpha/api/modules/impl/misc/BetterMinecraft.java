package by.algorithm.alpha.api.modules.impl.misc;

import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;

@ModuleAnnot(name = "BetterMinecraft", type = ModuleCategory.Misc, description = "Улучшения для майнкрафта")
public class BetterMinecraft extends Module {

    public final BooleanSetting smoothCamera = new BooleanSetting("Плавная камера", true);
    public final BooleanSetting betterTab = new BooleanSetting("Улучшенный таб", true);
    public final BooleanSetting betterHotbar = new BooleanSetting("Улучшенный хотбар", true);

    public BetterMinecraft() {
        addSettings(smoothCamera, betterTab, betterHotbar);
    }
}