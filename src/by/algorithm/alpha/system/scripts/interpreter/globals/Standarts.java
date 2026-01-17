package by.algorithm.alpha.system.scripts.interpreter.globals;

import by.algorithm.alpha.system.scripts.interpreter.compiler.LuaC;
import by.algorithm.alpha.system.scripts.interpreter.Globals;
import by.algorithm.alpha.system.scripts.interpreter.LoadState;
import by.algorithm.alpha.system.scripts.interpreter.lib.*;
import by.algorithm.alpha.system.scripts.interpreter.lib.*;
import by.algorithm.alpha.system.scripts.lua.libraries.ModuleLibrary;
import by.algorithm.alpha.system.scripts.lua.libraries.PlayerLibrary;

public class Standarts {
    public static Globals standardGlobals() {
        Globals globals = new Globals();
        globals.load(new BaseLib());
        globals.load(new Bit32Lib());
        globals.load(new MathLib());
        globals.load(new TableLib());
        globals.load(new StringLib());
        globals.load(new PlayerLibrary());
        globals.load(new ModuleLibrary());
        LoadState.install(globals);
        LuaC.install(globals);
        return globals;
    }
}
