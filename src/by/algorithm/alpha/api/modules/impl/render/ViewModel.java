package by.algorithm.alpha.api.modules.impl.render;

import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;

@ModuleAnnot(name = "ViewModel", type = ModuleCategory.Render, description = "Изменение XYZ руки")
public class ViewModel extends Module {
    public final SliderSetting right_x = new SliderSetting("RightX", 0.0F, -2.0f, 2.0f, 0.1F);
    public final SliderSetting right_y = new SliderSetting("RightY", 0.0F, -2.0f, 2.0f, 0.1F);
    public final SliderSetting right_z = new SliderSetting("RightZ", 0.0F, -2.0f, 2.0f, 0.1F);
    public final SliderSetting left_x = new SliderSetting("LeftX", 0.0F, -2.0f, 2.0f, 0.1F);
    public final SliderSetting left_y = new SliderSetting("LeftY", 0.0F, -2.0f, 2.0f, 0.1F);
    public final SliderSetting left_z = new SliderSetting("LeftZ", 0.0F, -2.0f, 2.0f, 0.1F);

    public ViewModel() {
        addSettings(right_x, right_y, right_z, left_x, left_y, left_z);
    }
}
