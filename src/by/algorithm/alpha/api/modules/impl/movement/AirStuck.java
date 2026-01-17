package by.algorithm.alpha.api.modules.impl.movement;


import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.system.events.EventChangeWorld;
import by.algorithm.alpha.system.events.EventLivingUpdate;
import by.algorithm.alpha.system.events.EventMotion;
import by.algorithm.alpha.system.events.EventPacket;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.server.SJoinGamePacket;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;

@ModuleAnnot(name = "AirStuck", type = ModuleCategory.Movement)
public class AirStuck extends Module {
    private boolean oldIsFlying;
    float yaw;
    float pitch;
    float yawoff;

    @Subscribe
    public void onMotion(EventMotion e) {
        if (Minecraft.player != null && Minecraft.player.ticksExisted % 10 == 0) {
            Minecraft.player.connection.sendPacket(new CPlayerPacket(Minecraft.player.isOnGround()));
        }

        if (Minecraft.player != null) {
            e.cancel();
        }

        if (Minecraft.player.isSprinting()) {
            Minecraft.player.setSprinting(false);
        }

        Minecraft.player.rotationYawHead = this.yaw;
        Minecraft.player.renderYawOffset = this.yawoff;
        Minecraft.player.rotationPitchHead = this.pitch;
    }

    @Subscribe
    public void onLivingUpdate(EventLivingUpdate e) {
        if (Minecraft.player != null) {
            Minecraft.player.noClip = true;
            Minecraft.player.setOnGround(false);
            Minecraft.player.setMotion(0.0F, 0.0F, 0.0F);
            Minecraft.player.abilities.isFlying = true;
        }

    }

    @Subscribe
    public void onPacket(EventChangeWorld event) {
        this.setState(false, false);
    }

    @Subscribe
    public void onPacket(EventPacket event) {
        if (Minecraft.player != null) {
            IPacket var3 = event.getPacket();
            if (var3 instanceof CPlayerPacket packet) {
                if (packet.moving) {
                    packet.x = Minecraft.player.getPosX();
                    packet.y = Minecraft.player.getPosY();
                    packet.z = Minecraft.player.getPosZ();
                }

                packet.onGround = Minecraft.player.isOnGround();
                if (packet.rotating) {
                    packet.yaw = Minecraft.player.rotationYaw;
                    packet.pitch = Minecraft.player.rotationPitch;
                }
            }

            if (event.getPacket() instanceof SJoinGamePacket) {
                this.toggle();
            }
        }

    }

    public void onEnable() {
        super.onEnable();
        if (Minecraft.player != null) {
            this.oldIsFlying = Minecraft.player.abilities.isFlying;
            Minecraft.player.movementInput = new MovementInput();
            Minecraft.player.moveForward = 0.0F;
            Minecraft.player.moveStrafing = 0.0F;
            this.yaw = Minecraft.player.rotationYaw;
            this.pitch = Minecraft.player.rotationPitch;
            this.yawoff = Minecraft.player.renderYawOffset;
        }
    }

    public void onDisable() {
        super.onDisable();
        if (Minecraft.player != null) {
            Minecraft.player.movementInput = new MovementInputFromOptions(mc.gameSettings);
            Minecraft.player.abilities.isFlying = this.oldIsFlying;
        }
    }
}