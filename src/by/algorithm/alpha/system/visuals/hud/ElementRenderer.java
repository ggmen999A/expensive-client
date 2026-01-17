package by.algorithm.alpha.system.visuals.hud;

import by.algorithm.alpha.system.events.EventDisplay;
import by.algorithm.alpha.system.utils.client.IMinecraft;

public interface ElementRenderer extends IMinecraft {
    void render(EventDisplay eventDisplay);
}
