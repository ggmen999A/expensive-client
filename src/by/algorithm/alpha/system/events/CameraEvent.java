package by.algorithm.alpha.system.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
public class CameraEvent {
    public float partialTicks;

    @Getter @Setter
    private float yaw;

    @Getter @Setter
    private float pitch;

    @Getter @Setter
    private float roll;

    public CameraEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }
}