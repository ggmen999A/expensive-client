package by.algorithm.alpha.system.scripts.interpreter.lib;

import by.algorithm.alpha.system.scripts.interpreter.LuaValue;

class TableLibFunction extends LibFunction {
	public LuaValue call() {
		return argerror(1, "table expected, got no value");
	}
}
