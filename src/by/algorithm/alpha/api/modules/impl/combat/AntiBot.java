package by.algorithm.alpha.api.modules.impl.combat;

import com.google.common.eventbus.Subscribe;
import com.mojang.authlib.GameProfile;
import by.algorithm.alpha.system.events.EventPacket;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPlayerListItemPacket;
import net.minecraft.util.math.vector.Vector3d;

import java.util.*;

@ModuleAnnot(
        name = "AntiBot",
        type = ModuleCategory.Combat,
        description = "Advanced AntiBot (Techno bots)"
)
public class AntiBot extends Module {

    private final Set<UUID> susPlayers = new ConcurrentSet<>();
    private static final Map<UUID, Boolean> botsMap = new HashMap<>();

    @Subscribe
    private void onUpdate(EventUpdate e) {
        for (UUID uuid : new HashSet<>(susPlayers)) {
            PlayerEntity p = mc.world.getPlayerByUuid(uuid);
            if (p == null) {
                susPlayers.remove(uuid);
                continue;
            }

            boolean bot = false;

            bot |= !p.getUniqueID()
                    .equals(PlayerEntity.getOfflineUUID(p.getGameProfile().getName()));

            boolean inTab = false;
            for (NetworkPlayerInfo info : Minecraft.player.connection.getPlayerInfoMap()) {
                if (info.getGameProfile().getId().equals(uuid)) {
                    inTab = true;
                    break;
                }
            }
            bot |= !inTab;

            bot |= p.ticksExisted < 20;
            bot |= p.getHealth() <= 0 || p.getHealth() > p.getMaxHealth();

            int armor = 0;
            for (ItemStack stack : p.getArmorInventoryList()) {
                if (!stack.isEmpty()) armor++;
            }
            bot |= p.isInvisible() && armor == 0;

            bot |= p.getHeldItemMainhand().isEmpty()
                    && p.getHeldItemOffhand().isEmpty();

            Vector3d motion = p.getMotion();
            bot |= motion.lengthSquared() == 0 && p.ticksExisted > 40;

            String name = p.getGameProfile().getName();
            bot |= name.length() < 3
                    || name.contains("§")
                    || name.matches(".*\\d{4,}.*");

            bot |= p.getGameProfile().getProperties().isEmpty();
            bot |= mc.player.getDistance(p) > 150;

            botsMap.put(uuid, bot);
            susPlayers.remove(uuid);
        }

        if (mc.player.ticksExisted % 100 == 0) {
            botsMap.keySet().removeIf(u -> mc.world.getPlayerByUuid(u) == null);
        }
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        if (!(e.getPacket() instanceof SPlayerListItemPacket p)) return;
        if (p.getAction() != SPlayerListItemPacket.Action.ADD_PLAYER) return;

        for (SPlayerListItemPacket.AddPlayerData data : p.getEntries()) {
            GameProfile profile = data.getProfile();
            UUID uuid = profile.getId();

            if (botsMap.containsKey(uuid) || susPlayers.contains(uuid)) continue;

            boolean invalid =
                    profile.getProperties().isEmpty()
                            && data.getPing() != 0
                            && profile.getName() != null;

            if (invalid) {
                susPlayers.add(uuid);
            }
        }
    }

    public static boolean isBot(Entity e) {
        return e instanceof PlayerEntity
                && botsMap.getOrDefault(e.getUniqueID(), false);
    }

    @Override
    public void onDisable() {
        botsMap.clear();
        susPlayers.clear();
        super.onDisable();
    }
}
