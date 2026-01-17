package by.algorithm.alpha.api.modules.impl.player;

import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.system.events.EventPacket;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.math.BlockPos;

@ModuleAnnot(name = "FastShulkerBreak", type = ModuleCategory.Player, description = "Насилуем Замок")
public class FastShulkerBreak extends Module {
    private final SliderSetting breakCount = new SliderSetting("Кол-во разрушений", 5, 2, 1000, 1);
    private BlockPos lastBreakPos = null;
    private long lastBreakTime = 0;

    public FastShulkerBreak() {
        addSettings(breakCount);
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (e.isSend() && e.getPacket() instanceof CPlayerDiggingPacket packet && packet.getAction() == CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK) {
            BlockPos pos = packet.getPosition();
            if (Minecraft.getInstance().world == null) return;

            boolean isShulker = Minecraft.getInstance().world.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock;
            if (isShulker && (lastBreakPos == null || !lastBreakPos.equals(pos) || System.currentTimeMillis() - lastBreakTime > 500)) {
                int count = Math.min(Math.round(breakCount.get()), 10000) - 1;
                for (int i = 0; i < count; i++) {
                    Minecraft.getInstance().player.connection.sendPacket(new CPlayerDiggingPacket(
                            CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, pos, packet.getFacing()));
                }
                lastBreakPos = pos;
                lastBreakTime = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        lastBreakPos = null;
        lastBreakTime = 0;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        lastBreakPos = null;
        lastBreakTime = 0;
    }
}
