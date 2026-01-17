package by.algorithm.alpha.api.modules.impl.movement;

import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.system.events.EventMotion;
import by.algorithm.alpha.system.events.EventPacket;
import com.google.common.eventbus.Subscribe;
import net.minecraft.network.IPacket;

import java.util.concurrent.ConcurrentLinkedQueue;

@ModuleAnnot(
        name = "Blink",
        type = ModuleCategory.Movement,
        description = "Задерживает отправку пакетов"
)
public class Blink extends Module {

    /* ===== TimedPacket ===== */

    public static class TimedPacket {
        private final IPacket<?> packet;
        private final long time;

        public TimedPacket(IPacket<?> packet, long time) {
            this.packet = packet;
            this.time = time;
        }

        public IPacket<?> getPacket() {
            return packet;
        }

        public long getTime() {
            return time;
        }
    }

    /* ===== Settings ===== */

    private final SliderSetting delay =
            new SliderSetting("Задержка", 1000F, 100F, 5000F, 100F);

    public Blink() {
        addSettings(delay);
    }

    /* ===== State ===== */

    private static final ConcurrentLinkedQueue<TimedPacket> packets =
            new ConcurrentLinkedQueue<>();

    /* ===== Events ===== */

    @Subscribe
    public void onPacket(EventPacket e) {
        if (!isState()) return;
        if (!e.isSend()) return;
        if (mc.player == null || mc.player.connection == null) return;

        IPacket<?> packet = e.getPacket();
        packets.add(new TimedPacket(packet, System.currentTimeMillis()));
        e.cancel();
    }

    @Subscribe
    public void onMotion(EventMotion e) {
        if (!isState()) return;
        if (mc.player == null || mc.player.connection == null) return;

        long now = System.currentTimeMillis();

        for (TimedPacket timed : packets) {
            if (now - timed.getTime() >= delay.get()) {
                mc.player.connection
                        .getNetworkManager()
                        .sendPacketWithoutEvent(timed.getPacket());
                packets.remove(timed);
            }
        }
    }

    /* ===== Disable ===== */

    @Override
    public void onDisable() {
        super.onDisable();

        if (mc.player == null || mc.player.connection == null) {
            packets.clear();
            return;
        }

        for (TimedPacket timed : packets) {
            mc.player.connection
                    .getNetworkManager()
                    .sendPacketWithoutEvent(timed.getPacket());
        }

        packets.clear();
    }
}
