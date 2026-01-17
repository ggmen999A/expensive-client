package by.algorithm.alpha.api.modules.impl.combat;

import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.system.events.EventPacket;
import by.algorithm.alpha.system.utils.math.MathUtil;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CUseEntityPacket;

@ModuleAnnot(name = "Criticals", type = ModuleCategory.Combat, description = "Криты с места")
public class Criticals extends Module {

    private final ModeSetting mode = new ModeSetting("Режим", "Packet",
            "Packet", "ReallyWorld", "Grim 1.17+", "HolyWorld Old", "RwWeb");

    public static boolean cancelCrit = false;

    public Criticals() {
        addSettings(mode);
    }

    @Subscribe
    public void onPacket(EventPacket event) {
        if (!event.isSend()) return;

        if (event.getPacket() instanceof CUseEntityPacket packet && packet.getAction() == CUseEntityPacket.Action.ATTACK) {
            Entity entity = packet.getEntityFromWorld(Minecraft.world);
            if (!(entity instanceof LivingEntity) || entity instanceof EnderCrystalEntity || cancelCrit) return;

            if (Minecraft.player.isElytraFlying() || Minecraft.player.isPassenger()) return;

            double x = Minecraft.player.getPosX();
            double y = Minecraft.player.getPosY();
            double z = Minecraft.player.getPosZ();

            switch (mode.get()) {
                case "ReallyWorld" -> {
                    double offset = MathUtil.randomizeFloat(1.0E-7F, 9.0E-7F);
                    Minecraft.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y + offset, z, false));
                    Minecraft.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y, z, false));
                    Minecraft.player.fallDistance = 0.001F;
                }
                case "Grim 1.17+" -> {
                    Minecraft.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y + 0.0625, z, false));
                    Minecraft.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y, z, false));
                    Minecraft.player.fallDistance = 0.1F;
                }
                case "Packet" -> {
                    if (Minecraft.player.isInWater() || Minecraft.player.isInLava()) return;
                    if (Minecraft.player.isOnGround()) {
                        Minecraft.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y + 0.0625, z, false));
                        Minecraft.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y, z, false));
                        Minecraft.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y + 1.1E-5, z, false));
                        Minecraft.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y, z, false));
                    }
                }
                case "HolyWorld Old" -> {
                    if (Minecraft.player.isInWater() || Minecraft.player.isInLava()) return;
                    if (!Minecraft.player.isOnGround() && Minecraft.player.fallDistance == 0) {
                        Minecraft.player.fallDistance = 0.001f;
                        Minecraft.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y - 1e-6, z, false));
                    }
                }
                case "RwWeb" -> {
                    if (!Minecraft.player.isOnGround() && Minecraft.player.getMotion().y < 0) {
                        if (Minecraft.world.getBlockState(Minecraft.player.getPosition()).getBlock() == Blocks.COBWEB) {
                            Minecraft.player.fallDistance = 0.08f;
                            Minecraft.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y + 0.035, z, false));
                            Minecraft.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y, z, false));
                            Minecraft.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y + 0.011, z, false));
                            Minecraft.player.connection.sendPacket(new CPlayerPacket.PositionPacket(x, y, z, false));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onEnable() {
        cancelCrit = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        cancelCrit = false;
        super.onDisable();
    }
}