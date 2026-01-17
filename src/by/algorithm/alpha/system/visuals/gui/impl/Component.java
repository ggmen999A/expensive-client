package by.algorithm.alpha.system.visuals.gui.impl;

import by.algorithm.alpha.system.visuals.gui.MainPanel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Component implements IBuilder {

    private float x, y, width, height;
    private MainPanel panel;

    public boolean isHovered(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean isHovered(float mouseX, float mouseY, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean isVisible() {
        return true;
    }

}
