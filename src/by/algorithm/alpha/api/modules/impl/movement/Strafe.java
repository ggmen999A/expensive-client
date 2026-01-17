package by.algorithm.alpha.api.modules.impl.movement;

import by.algorithm.alpha.api.modules.impl.combat.AttackAura;
import by.algorithm.alpha.system.events.*;
import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.system.utils.player.DamagePlayerUtil;
import by.algorithm.alpha.system.utils.player.MoveUtils;
import by.algorithm.alpha.system.utils.player.StrafeMovement;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoulSandBlock;
import net.minecraft.block.material.Material;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;

@ModuleAnnot(name = "Strafe", type = ModuleCategory.Movement, description = "Стрейф в воздухе без замедлений")
public class Strafe extends Module {
    private final BooleanSetting damageBoost = new BooleanSetting("Буст с дамагом", false);
    private final BooleanSetting onlyAir = new BooleanSetting("Только в воздухе", true);

    private final SliderSetting boostSpeed = new SliderSetting("Значение буста", 0.7f, 0.1F, 5.0f, 0.1F);

    private final DamagePlayerUtil damageUtil = new DamagePlayerUtil();
    private final StrafeMovement strafeMovement = new StrafeMovement();

    private final TargetStrafe targetStrafe;
    private final AttackAura killAura;

    public Strafe(TargetStrafe targetStrafe, AttackAura killAura) {
        this.targetStrafe = targetStrafe;
        this.killAura = killAura;
        addSettings(damageBoost, onlyAir, boostSpeed);
    }

    @Subscribe
    private void onAction(ActionEvent e) {
        handleEventAction(e);
    }

    @Subscribe
    private void onMoving(MovingEvent e) {
        handleEventMove(e);
    }

    @Subscribe
    private void onPostMove(PostMoveEvent e) {
        handleEventPostMove(e);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        handleEventPacket(e);
    }

    @Subscribe
    private void onDamage(EventDamageReceive e) {
        handleDamageEvent(e);
    }

    private void handleDamageEvent(EventDamageReceive damage) {
        if (damageBoost.get()) {
            damageUtil.processDamage(damage);
        }
    }

    private void handleEventAction(ActionEvent action) {
        if (strafes()) {
            handleStrafesEventAction(action);
        }
        if (strafeMovement.isNeedSwap()) {
            handleNeedSwapEventAction(action);
        }
    }

    private void handleEventMove(MovingEvent eventMove) {
        if (strafes()) {
            handleStrafesEventMove(eventMove);
        } else {
            strafeMovement.setOldSpeed(0);
        }
    }

    private void handleEventPostMove(PostMoveEvent eventPostMove) {
        strafeMovement.postMove(eventPostMove.getHorizontalMove());
    }

    private void handleEventPacket(EventPacket packet) {
        if (packet.getType() == EventPacket.Type.RECEIVE) {
            if (damageBoost.get()) {
                damageUtil.onPacketEvent(packet);
            }
            handleReceivePacketEventPacket(packet);
        }
    }

    private void handleStrafesEventAction(ActionEvent action) {
        if (CEntityActionPacket.lastUpdatedSprint != strafeMovement.isNeedSprintState()) {
            action.setSprintState(!CEntityActionPacket.lastUpdatedSprint);
        }
    }

    private void handleStrafesEventMove(MovingEvent eventMove) {
        if (targetStrafe.isState() && (killAura.isState() && killAura.getTarget() != null)) {
            return;
        }

        final float damageSpeed = boostSpeed.get().floatValue() / 10.0F;
        final double speed = strafeMovement.calculateSpeed(eventMove, damageBoost.get(), damageUtil.isNormalDamage(), false, damageSpeed);

        MoveUtils.MoveEvent.setMoveMotion(eventMove, speed);
    }

    private void handleNeedSwapEventAction(ActionEvent action) {
        action.setSprintState(!mc.player.serverSprintState);
        strafeMovement.setNeedSwap(false);
    }

    private void handleReceivePacketEventPacket(EventPacket packet) {
        if (packet.getPacket() instanceof SPlayerPositionLookPacket) {
            strafeMovement.setOldSpeed(0);
        }
    }

    public boolean strafes() {
        if (isInvalidPlayerState()) {
            return false;
        }

        // Проверка на только в воздухе
        if (onlyAir.get() && mc.player.onGround) {
            return false;
        }

        BlockPos playerPosition = new BlockPos(mc.player.getPositionVec());

        if (isPlayerInWebOrSoulSand(playerPosition)) {
            return false;
        }

        return isPlayerAbleToStrafe();
    }

    private boolean isInvalidPlayerState() {
        return mc.player == null || mc.world == null
                || mc.player.isSneaking()
                || mc.player.isElytraFlying()
                || mc.player.isInWater()
                || mc.player.isInLava()
                || mc.player.isOnLadder()
                || mc.player.isPassenger()
                || mc.player.isPotionActive(Effects.SLOW_FALLING);
    }

    private boolean isPlayerInWebOrSoulSand(BlockPos playerPosition) {
        Material playerMaterial = mc.world.getBlockState(playerPosition).getMaterial();
        Block oneBelowBlock = mc.world.getBlockState(playerPosition.down()).getBlock();

        return playerMaterial == Material.WEB || oneBelowBlock instanceof SoulSandBlock;
    }

    private boolean isPlayerAbleToStrafe() {
        return !mc.player.abilities.isFlying && !mc.player.isPotionActive(Effects.LEVITATION);
    }

    @Override
    public void onEnable() {
        strafeMovement.setOldSpeed(0);
        super.onEnable();
    }
}