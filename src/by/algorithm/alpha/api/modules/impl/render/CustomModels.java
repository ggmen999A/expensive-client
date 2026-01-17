package by.algorithm.alpha.api.modules.impl.render;


import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import net.minecraftforge.eventbus.api.Event;


@ModuleAnnot(name = "CustomModels", type = ModuleCategory.Render, description = "Кастомная модель игрока")
public class CustomModels extends Module {

    public final ModeSetting models = new ModeSetting("Модель", "Заяц", "Заяц","Михаил Евгенич","Белый демон", "Демон");
    public final BooleanSetting friends = new BooleanSetting("Применять на друзей",true);

    public CustomModels() {
        addSettings(models, friends);
    }

    public boolean onEvent(Event event) {
        return false;
    }
}