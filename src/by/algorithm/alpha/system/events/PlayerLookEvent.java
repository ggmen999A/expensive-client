package by.algorithm.alpha.system.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.vector.Vector2f;

@Getter
@Setter
@AllArgsConstructor
public final class PlayerLookEvent {
    private Vector2f rotation;

    public Vector2f getRotation() {
        return rotation;
    }
}