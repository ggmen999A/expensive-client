package by.algorithm.alpha.system.visuals.hud;

import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.system.utils.client.IMinecraft;

public interface ElementUpdater extends IMinecraft {

    void update(EventUpdate e);
}
