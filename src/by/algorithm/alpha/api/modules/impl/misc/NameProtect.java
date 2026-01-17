package by.algorithm.alpha.api.modules.impl.misc;

import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import net.minecraft.client.Minecraft;

@ModuleAnnot(name = "NameProtect", type = ModuleCategory.Misc, description = "Скрывает ваш игровой никнейм")
public class NameProtect extends Module {

    public static final String fakeName = "Protected";

    public NameProtect() {
    }

    public static String getReplaced(String input) {
        if (Initclass.getInstance() != null && Initclass.getInstance().getFunctionRegistry().getNameProtect().isState()) {
            input = input.replace(Minecraft.getInstance().session.getUsername(), fakeName);
        }
        return input;
    }
}