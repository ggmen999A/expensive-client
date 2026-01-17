package by.algorithm.alpha.api.modules.impl.movement;

import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.system.events.EventUpdate;
import com.google.common.eventbus.Subscribe;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

@ModuleAnnot(
        name = "TridentFly",
        type = ModuleCategory.Movement,
        description = "Флай на трезубце (spam / instant)"
)
public class TridentFly extends Module {

    private final BooleanSetting allowNoWater =
            new BooleanSetting("Allow No Water", true);

    private final BooleanSetting instant =
            new BooleanSetting("Instant", true);

    private final BooleanSetting spam =
            new BooleanSetting("Spam", false);

    private final SliderSetting ticks =
            new SliderSetting("Ticks", 3.0F, 1.0F, 20.0F, 1.0F)
                    .setVisible(spam::get);

    private boolean wasUsingTrident;
    private int useTicks;

    public TridentFly() {
        addSettings(allowNoWater, instant, spam, ticks);
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        if (mc.player == null) return;

        handleSpam();
    }

    private void handleSpam() {
        if (!spam.get()) {
            reset();
            return;
        }

        boolean usingTrident =
                mc.player.isHandActive()
                        && mc.player.getHeldItemMainhand().getItem() == Items.TRIDENT;

        if (usingTrident) {
            useTicks++;

            if (!wasUsingTrident) {
                wasUsingTrident = true;
                useTicks = 0;
            }

            if (useTicks >= ticks.get()) {
                releaseTrident();
                reset();
            }
        } else {
            reset();
        }
    }

    private void releaseTrident() {
        if (mc.player.connection == null) return;

        mc.player.connection.sendPacket(
                new CPlayerDiggingPacket(
                        CPlayerDiggingPacket.Action.RELEASE_USE_ITEM,
                        new BlockPos(0, 0, 0),
                        Direction.DOWN
                )
        );

        mc.player.stopActiveHand();
    }

    private void reset() {
        wasUsingTrident = false;
        useTicks = 0;
    }

    /* ====== API hooks (если используются другими модулями) ====== */

    public boolean shouldAllowNoWater() {
        return isState() && allowNoWater.get();
    }

    public boolean shouldInstantPullback() {
        return isState() && instant.get();
    }

    @Override
    public void onDisable() {
        reset();
        super.onDisable();
    }
}
