package by.algorithm.alpha.system.events;

import net.minecraftforge.eventbus.api.Event;

public class CancelEvent extends Event {

    private boolean isCancel;

    public void cancel() {
        isCancel = true;
    }
    public void open() {
        isCancel = false;
    }
    public boolean isCancel() {
        return isCancel;
    }

}
