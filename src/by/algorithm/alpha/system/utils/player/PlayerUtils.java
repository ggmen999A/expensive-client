package by.algorithm.alpha.system.utils.player;

import lombok.experimental.UtilityClass;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

import java.util.regex.Pattern;

@UtilityClass
public class PlayerUtils {

    private final Pattern NAME_REGEX = Pattern.compile("^[A-zА-я0-9_]{3,16}$");

    public boolean isNameValid(String name) {
        return NAME_REGEX.matcher(name).matches();
    }

    public static boolean isPlayerInWeb() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.world == null) {
            return false;
        }
        BlockPos playerPos = mc.player.getPosition();
        BlockState blockAtPlayer = mc.world.getBlockState(playerPos);
        BlockState blockAbovePlayer = mc.world.getBlockState(playerPos.up());
        return blockAtPlayer.getBlock() == Blocks.COBWEB ||
                blockAbovePlayer.getBlock() == Blocks.COBWEB;
    }
}
