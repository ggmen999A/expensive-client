package by.algorithm.alpha.system.visuals.styles;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

import java.awt.*;

@AllArgsConstructor
@Data
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Style {

    String styleName;
    Color firstColor;
    Color secondColor;

}
