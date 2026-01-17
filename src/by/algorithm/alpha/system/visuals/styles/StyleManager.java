package by.algorithm.alpha.system.visuals.styles;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StyleManager {
    final List<Style> styleList;
    private List<Style> styles;

    @Setter
    private Style current;

    @Setter
    Style currentStyle;

    public StyleManager(List<Style> styleList, Style currentStyle) {
        this.styleList = styleList;
        this.currentStyle = currentStyle;
    }

}
