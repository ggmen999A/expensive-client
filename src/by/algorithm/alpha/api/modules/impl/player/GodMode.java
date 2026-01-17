package by.algorithm.alpha.api.modules.impl.player;


import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.system.utils.client.ClientUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;

import java.util.Timer;
import java.util.TimerTask;

@ModuleAnnot(name = "GodMode", type = ModuleCategory.Player)
public class GodMode extends Module {

    private final Minecraft mc = Minecraft.getInstance();
    private Timer timer;

    public void onEnable() {
        super.onEnable();
        if (mc.player != null) {
            if (!ClientUtil.isPvP()) {
                mc.player.sendChatMessage("/menu");
            }

            timer = new Timer();
            timer.schedule(new MenuInteractionTask(), 200);
        }
    }

    public void onDisable() {
        super.onDisable();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private class MenuInteractionTask extends TimerTask {
        public void run() {
            if (!ClientUtil.isConnectedToServer("reallyworld")) {
                print("Данная функция работает только на ReallyWorld");
                toggle();
            } else {
                if (mc.currentScreen instanceof ChestScreen) {
                    ChestScreen chestScreen = (ChestScreen) mc.currentScreen;
                    if (ClientUtil.isPvP()) {
                        Slot slot = chestScreen.getContainer().inventorySlots.get((1 * 9) + 4); mc.playerController.windowClick(  chestScreen.getContainer().windowId,  slot.slotNumber, 0, ClickType.PICKUP,  mc.player
                        );
                    } else {
                        Slot slot = chestScreen.getContainer().inventorySlots.get((2 * 9) + 3);
                        if (slot != null) {
                            mc.playerController.windowClick(chestScreen.getContainer().windowId,slot.slotNumber,0,ClickType.PICKUP,mc.player
                            );
                        }
                        timer.schedule(new MenuInteractionTask(), 200);
                    }
                } else {
                    if (!ClientUtil.isPvP()) {
                        if (!(mc.currentScreen instanceof InventoryScreen)) {
                            mc.player.sendChatMessage("/menu");
                            timer.schedule(new MenuInteractionTask(), 1000);
                        }
                    }
                }
            }
        }
    }
}