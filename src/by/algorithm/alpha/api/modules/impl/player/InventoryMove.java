package by.algorithm.alpha.api.modules.impl.player;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventPacket;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.system.events.InventoryCloseEvent;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.system.utils.client.ClientUtil;
import by.algorithm.alpha.system.utils.math.StopWatch;
import by.algorithm.alpha.system.utils.player.MoveUtils;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;

import java.util.ArrayList;
import java.util.List;

@ModuleAnnot(name = "GUIMove", type = ModuleCategory.Player, description = "Хождение в инвентаре")
public class InventoryMove extends Module {

    private final List<IPacket<?>> packet = new ArrayList<>();

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (mc.player != null) {

            final KeyBinding[] pressedKeys = {mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack,
                    mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump,
                    mc.gameSettings.keyBindSprint};
            if (ClientUtil.isConnectedToServer("funtime") || ClientUtil.isConnectedToServer("funsky") ) {
                if (!wait.isReached(400)) {
                    for (KeyBinding keyBinding : pressedKeys) {
                        keyBinding.setPressed(false);
                    }
                    return;
                }
            }


            if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof EditSignScreen) {
                return;
            }

            updateKeyBindingState(pressedKeys);

        }
    }

    @Subscribe
    public void onPacket(EventPacket e) {
        if (ClientUtil.isConnectedToServer("funtime") || ClientUtil.isConnectedToServer("funsky") ) {
            if (e.getPacket() instanceof CClickWindowPacket p && MoveUtils.isMoving()) {
                if (mc.currentScreen instanceof InventoryScreen) {
                    packet.add(p);
                    e.cancel();
                }
            }
        }
    }

    public StopWatch wait = new StopWatch();

    @Subscribe
    public void onClose(InventoryCloseEvent e) {
        if (ClientUtil.isConnectedToServer("funtime") || ClientUtil.isConnectedToServer("funsky")) {
            if (mc.currentScreen instanceof InventoryScreen && !packet.isEmpty() && MoveUtils.isMoving()) {
                new Thread(() -> {
                    wait.reset();
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    for (IPacket p : packet) {
                        mc.player.connection.sendPacketWithoutEvent(p);
                    }
                    packet.clear();
                }).start();
                e.cancel();
            }
        }
    }

    private void updateKeyBindingState(KeyBinding[] keyBindings) {
        for (KeyBinding keyBinding : keyBindings) {
            boolean isKeyPressed = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), keyBinding.getDefault().getKeyCode());
            keyBinding.setPressed(isKeyPressed);
        }
    }
}
