package by.algorithm.alpha.api.modules.impl.movement;

import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.ModeListSetting;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.system.utils.player.PlayerUtils;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;

@ModuleAnnot(name = "CollisionDisabler", type = ModuleCategory.Movement, description = "Отключение коллизий в блоках")
public class CollisionDisabler extends Module {

    public static CollisionDisabler getInstance() {
        return null;
    }

    private final ModeListSetting collisionSettings = new ModeListSetting("Не замедлятся в",
            new BooleanSetting("Паутине", true),
            new BooleanSetting("Сладких ягодах", true)
    );

    private boolean inWeb = false;
    private boolean inSweetBerries = false;

    public void onUpdate(EventUpdate event) {
        if (mc.player == null || mc.world == null) return;

        BlockPos playerPos = new BlockPos(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ());
        inWeb = mc.world.getBlockState(playerPos).getBlock() == Blocks.COBWEB ||
                mc.world.getBlockState(playerPos.up()).getBlock() == Blocks.COBWEB;

        inSweetBerries = mc.world.getBlockState(playerPos).getBlock() == Blocks.SWEET_BERRY_BUSH ||
                mc.world.getBlockState(playerPos.up()).getBlock() == Blocks.SWEET_BERRY_BUSH;

        if (inWeb && collisionSettings.getValueByName("Паутине").get()) {
            preventWebSlowdown();
        }
        if (inSweetBerries && collisionSettings.getValueByName("Сладких ягодах").get()) {
            preventSweetBerrySlowdown();
        }
    }

    private void preventWebSlowdown() {
        if (mc.player == null) return;
        if (PlayerUtils.isPlayerInWeb()) {
            // Reset motion multiplier for cobwebs to maintain normal movement speed
            mc.player.setMotionMultiplier(Blocks.AIR.getDefaultState(), new Vector3d(1.0D, 1.0D, 1.0D));

            // Additional direct motion modification for enhanced effectiveness
            Vector3d currentMotion = mc.player.getMotion();
            if (currentMotion.length() < 0.1) {
                double motionX = mc.player.moveForward * 0.15 * Math.sin(-mc.player.rotationYaw * Math.PI / 180);
                double motionZ = mc.player.moveForward * 0.15 * Math.cos(-mc.player.rotationYaw * Math.PI / 180);
                mc.player.setMotion(motionX, currentMotion.y, motionZ);
            }
        }
    }


    private void preventSweetBerrySlowdown() {
        if (mc.player == null) return;

        // Primary approach: Reset motion multiplier applied by sweet berry bushes
        mc.player.setMotionMultiplier(Blocks.AIR.getDefaultState(), new Vector3d(1.0D, 1.0D, 1.0D));

        // Secondary approach: Direct motion adjustment to counteract slowdown effects
        Vector3d currentMotion = mc.player.getMotion();
        double motionThreshold = 0.05; // Threshold indicating potential slowdown

        if (currentMotion.length() < motionThreshold && (mc.player.moveForward != 0 || mc.player.moveStrafing != 0)) {
            // Calculate desired motion based on player input
            double motionMultiplier = 0.2; // Standard walking speed multiplier
            double yawRadians = Math.toRadians(mc.player.rotationYaw + 90.0f);

            double motionX = mc.player.moveForward * motionMultiplier * Math.cos(yawRadians)
                    + mc.player.moveStrafing * motionMultiplier * Math.sin(yawRadians);
            double motionZ = mc.player.moveForward * motionMultiplier * Math.sin(yawRadians)
                    - mc.player.moveStrafing * motionMultiplier * Math.cos(yawRadians);

            mc.player.setMotion(motionX, currentMotion.y, motionZ);
        }
    }

    public void onDisable() {
        super.onDisable();
        inWeb = false;
        inSweetBerries = false;
        if (mc.player != null) {
            mc.player.setMotionMultiplier(Blocks.AIR.getDefaultState(), new Vector3d(1.0D, 1.0D, 1.0D));
        }
    }
}