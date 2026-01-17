package by.algorithm.alpha.api.modules.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ModuleCategory {

    Combat("Combat", "A"),
    Movement("Movement", "B"),
    Player("Player", "C"),
    Render("Render", "Z"),
    Misc("Misc", "AASF");

    public final String name;
    public final String icon;

}
