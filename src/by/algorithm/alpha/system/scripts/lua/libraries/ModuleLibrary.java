package by.algorithm.alpha.system.scripts.lua.libraries;

import by.algorithm.alpha.system.scripts.interpreter.LuaValue;
import by.algorithm.alpha.system.scripts.interpreter.compiler.jse.CoerceJavaToLua;
import by.algorithm.alpha.system.scripts.interpreter.lib.OneArgFunction;
import by.algorithm.alpha.system.scripts.interpreter.lib.TwoArgFunction;
import by.algorithm.alpha.system.scripts.lua.classes.ModuleClass;

public class ModuleLibrary extends TwoArgFunction {

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaValue library = tableOf();
        library.set("register", new register());

        env.set("module", library);
        return library;
    }

    public class register extends OneArgFunction {

        @Override
        public LuaValue call(LuaValue arg) {
            return CoerceJavaToLua.coerce(new ModuleClass(arg.toString()));
        }

    }

}
