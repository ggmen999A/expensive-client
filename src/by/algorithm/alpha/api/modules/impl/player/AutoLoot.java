package by.algorithm.alpha.api.modules.impl.player;


import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
@ModuleAnnot(name = "AutoLoot",type = ModuleCategory.Player, description = "АвтоЛут отмычек на пирате")
public class AutoLoot extends Module {
    private Entity trader;
    public AutoLoot() {
    }
    public void aimAndClick() {
        if (trader == null || mc.player == null) return;

        ClientPlayerEntity player = mc.player;

        Vector3d playerPos = player.getPositionVec();
        Vector3d traderPos = trader.getPositionVec()
                .add(0, trader.getEyeHeight() / 2.0F, 0);

        Vector3d direction = traderPos.subtract(playerPos);

        double distance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);

        float yaw = (float) (Math.atan2(direction.z, direction.x) * (180 / Math.PI)) - 90.0F;
        float pitch = (float) -(Math.atan2(direction.y, distance) * (180 / Math.PI));

        // Поворот игрока
        player.connection.sendPacket(
                new CPlayerPacket.PositionRotationPacket(
                        player.getPosX(),
                        player.getPosY(),
                        player.getPosZ(),
                        yaw,
                        pitch,
                        player.isOnGround()
                )
        );

        // Клик по энтити
        player.connection.sendPacket(
                new CUseEntityPacket(trader, Hand.MAIN_HAND, player.isSneaking())
        );
    }

}