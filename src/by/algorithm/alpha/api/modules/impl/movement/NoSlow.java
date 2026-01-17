package by.algorithm.alpha.api.modules.impl.movement;

import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.system.events.NoSlowEvent;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import lombok.ToString;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;

@ToString
@ModuleAnnot(name = "NoSlow", type = ModuleCategory.Movement, description = "Отключение замедления при использовании предмета")
public class NoSlow extends Module {

    private final ModeSetting mode = new ModeSetting("Мод", "Matrix", "Matrix", "Grim", "Grim old", "Grim New");
    private final BooleanSetting sprint = new BooleanSetting("Спринт", true);
    public NoSlow() {
        addSettings(mode);
    }

    int ticks = 0;

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (Minecraft.player.isHandActive()) {
            ticks++;
        } else {
            ticks = 0;
        }

    }

    @Subscribe
    public void onEating(NoSlowEvent e) {
        handleEventUpdate(e);
    }


    private void handleEventUpdate(NoSlowEvent eventNoSlow) {
        if (Minecraft.player.isHandActive()) {
            switch (mode.get()) {
                case "Grim" -> handleGrimACMode(eventNoSlow);
                case "Matrix" -> handleMatrixMode(eventNoSlow);
                case "Grim New" -> {
                    if (Minecraft.player.getItemInUseCount() % 2 == 0) {
                        eventNoSlow.cancel();
                    }
                }

                case "Grim old" -> {
                    Hand hand = Minecraft.player.getActiveHand();
                    if (sprint.get()) {
                        boolean canSprint =
                                Minecraft.player.moveForward > 0 &&
                                        !Minecraft.player.isInWater() &&
                                        !Minecraft.player.isPotionActive(Effects.BLINDNESS) &&
                                        Minecraft.player.getFoodStats().getFoodLevel() > 6 &&
                                        !Minecraft.player.isSneaking();

                        if (canSprint) {
                            Minecraft.player.setSprinting(true);
                        }
                    }
                    mc.playerController.processRightClick(
                            Minecraft.player,
                            Minecraft.world,
                            hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND
                    );

                    eventNoSlow.cancel();
                }
            }
        }
    }

    private void handleMatrixMode(NoSlowEvent noSlow) {
        boolean isEating = false;
        int eatTicks = 0;
        if (Minecraft.player.getHeldItemOffhand().getUseAction() == UseAction.EAT && Minecraft.player.getActiveHand() == Hand.MAIN_HAND) {
            isEating = true;
            eatTicks++;
            if (eatTicks >= 3) {
                Minecraft.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
                noSlow.cancel();
            }
            return;
        }
        if (Minecraft.player.getActiveHand() != Hand.MAIN_HAND || Minecraft.player.getHeldItemOffhand().getUseAction() != UseAction.EAT) {
            isEating = false;
            eatTicks = 0;
        }
        if (Minecraft.player.getHeldItemOffhand().getUseAction() == UseAction.BLOCK && Minecraft.player.getActiveHand() == Hand.MAIN_HAND) {
            return;
        }
        if (Minecraft.player.getActiveHand() == Hand.MAIN_HAND) {
            Minecraft.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
            noSlow.cancel();
        }
    }

    private void handleGrimACMode(NoSlowEvent e) {
        if (ticks < 3) {
            Minecraft.player.connection.sendPacket(new CClickWindowPacket(
                    0,
                    15,
                    0,
                    ClickType.PICKUP,
                    ItemStack.EMPTY,
                    (short) 0
            ));
        }
        if (ticks > 4) e.cancel();
    }


}
