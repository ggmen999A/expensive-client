package by.algorithm.alpha.system.scripts.lua.libraries;

import by.algorithm.alpha.system.scripts.interpreter.LuaValue;
import by.algorithm.alpha.system.scripts.interpreter.compiler.jse.CoerceJavaToLua;
import by.algorithm.alpha.system.scripts.interpreter.lib.TwoArgFunction;
import by.algorithm.alpha.system.scripts.interpreter.lib.ZeroArgFunction;
import net.minecraft.client.Minecraft;

public class PlayerLibrary extends TwoArgFunction {

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = tableOf();
        library.set("entity", new entity());
    
        env.set("player", library);
        return library;
    }

    public class entity extends ZeroArgFunction {

        @Override
        public LuaValue call() {
            return CoerceJavaToLua.coerce(Minecraft.getInstance().player.getLuaClass());
        }
    
        
    }

}
