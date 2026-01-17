package by.algorithm.alpha.api.modules.impl.misc;

import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.system.events.EventPacket;
import by.algorithm.alpha.system.events.EventUpdate;
import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.server.SSendResourcePackPacket;

@ModuleAnnot(name = "SRPSpoofer", type = ModuleCategory.Misc, description = "Отключение требования ресурспака для ReallyWorld")
public class SRPSpoofer extends Module {

    private enum ResourcePackAction {
        ACCEPT, SEND, WAIT
    }

    @Getter
    @Setter
    private ResourcePackAction currentAction = ResourcePackAction.WAIT;
    private String resourcePackHash = ""; // Храним хэш ресурспака
    private long counter = 0;

    @Subscribe
    public void onPacket(EventPacket event) {
        if (event.getPacket() instanceof SSendResourcePackPacket) {
            SSendResourcePackPacket packet = (SSendResourcePackPacket) event.getPacket();
            resourcePackHash = packet.getHash(); // Сохраняем хэш
            currentAction = ResourcePackAction.ACCEPT;
            event.cancel(); // Отменяем загрузку ресурспака
        }
    }

    @Subscribe
    public void onUpdate(EventUpdate event) {
        ClientPlayNetHandler networkHandler = Minecraft.getInstance().getConnection();
        if (networkHandler != null) {
            if (currentAction == ResourcePackAction.ACCEPT) {
                networkHandler.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.ACCEPTED));
                currentAction = ResourcePackAction.SEND;
                counter = 0;
            } else if (currentAction == ResourcePackAction.SEND) {
                if (counter++ >= 100) { // Уменьшаем задержку до ~5 секунд
                    networkHandler.sendPacket(new CResourcePackStatusPacket(CResourcePackStatusPacket.Action.SUCCESSFULLY_LOADED));
                    currentAction = ResourcePackAction.WAIT;
                }
            }
        }
    }

    @Override
    public void onDisable() {
        currentAction = ResourcePackAction.WAIT;
        resourcePackHash = "";
        super.onDisable();
    }
}