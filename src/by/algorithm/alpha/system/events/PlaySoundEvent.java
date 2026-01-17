package by.algorithm.alpha.system.events;

import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;

public class PlaySoundEvent {
    
    public SoundEvent sound;
    public SoundCategory category;
    public Vector3d position;
    public float volume;
    public float pitch;
    private boolean canceled = false;

    public PlaySoundEvent(SoundEvent sound, SoundCategory category, Vector3d position, float volume, float pitch) {
        this.sound = sound;
        this.category = category;
        this.position = position;
        this.volume = volume;
        this.pitch = pitch;
    }

    public PlaySoundEvent(SoundEvent sound, SoundCategory category, double x, double y, double z, float volume, float pitch) {
        this(sound, category, new Vector3d(x, y, z), volume, pitch);
    }

    public SoundEvent getSound() {
        return sound;
    }

    public SoundCategory getCategory() {
        return category;
    }

    public Vector3d getPosition() {
        return position;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}