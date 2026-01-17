package by.algorithm.alpha.api.modules.impl.player;

import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.system.events.EventKey;
import by.algorithm.alpha.system.events.EventMotion;
import by.algorithm.alpha.system.events.EventPacket;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleReg;
import by.algorithm.alpha.api.modules.settings.impl.BindSetting;
import by.algorithm.alpha.system.utils.math.StopWatch;
import by.algorithm.alpha.system.utils.player.InventoryUtil;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleAnnot(name = "ClickPearl", type = ModuleCategory.Player, description = "Бросок эндер жемчуга по бинду")
public class ClickPearl extends Module {
    final ModeSetting mode = new ModeSetting("Режим", "Legit", "Legit", "Packet");

    final BindSetting throwKey = new BindSetting("Кнопка", -98);
    final StopWatch stopWatch = new StopWatch();
    final InventoryUtil.Hand handUtil = new InventoryUtil.Hand();

    final ItemCooldown itemCooldown;
    long delay;
    boolean throwPearl;

    public ClickPearl(ItemCooldown itemCooldown) {
        this.itemCooldown = itemCooldown;
        addSettings(throwKey, mode);
    }

    @Subscribe
    public void onKey(EventKey e) {
        throwPearl = e.getKey() == throwKey.get();
    }
    @Subscribe
    private void onMotion(EventMotion e) {
        if (!throwPearl) return;

        if (Minecraft.player.getCooldownTracker().hasCooldown(Items.ENDER_PEARL)) {
            throwPearl = false;
            return;
        }

        boolean offhandPearl = Minecraft.player.getHeldItemOffhand().getItem() instanceof EnderPearlItem;

        if (mode.is("Legit")) {

            if (offhandPearl) {
                mc.playerController.processRightClick(Minecraft.player, Minecraft.world, Hand.OFF_HAND);
                Minecraft.player.swingArm(Hand.OFF_HAND);
            } else {
                int slot = InventoryUtil.getInstance()
                        .getSlotInInventoryOrHotbar(Items.ENDER_PEARL, true);

                if (slot != -1 && slot < 9) {
                    handUtil.setOriginalSlot(Minecraft.player.inventory.currentItem);

                    Minecraft.player.inventory.currentItem = slot;

                    mc.playerController.processRightClick(Minecraft.player, Minecraft.world, Hand.MAIN_HAND);
                    Minecraft.player.swingArm(Hand.MAIN_HAND);

                    delay = System.currentTimeMillis();
                }
            }

        } else {
            // Packet режим — без изменений
            if (offhandPearl) {
                Minecraft.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
                Minecraft.player.swingArm(Hand.MAIN_HAND);
            } else {
                int slot = findPearlAndThrow();
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
            }
        }

        throwPearl = false;
    }



    @Subscribe
    private void onUpdate(EventUpdate e) {
        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        this.handUtil.onEventPacket(e);
    }

    private int findPearlAndThrow() {
        int hbSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, true);
        if (hbSlot != -1) {
            this.handUtil.setOriginalSlot(Minecraft.player.inventory.currentItem);
            if (hbSlot != Minecraft.player.inventory.currentItem) {
                Minecraft.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            }
            Minecraft.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            Minecraft.player.swingArm(Hand.MAIN_HAND);

            ModuleReg functionRegistry = Initclass.getInstance().getFunctionRegistry();
            ItemCooldown itemCooldown = functionRegistry.getItemCooldown();
            ItemCooldown.ItemEnum itemEnum = ItemCooldown.ItemEnum.getItemEnum(Items.ENDER_PEARL);

            if (itemCooldown.isState() && itemEnum != null && itemCooldown.isCurrentItem(itemEnum)) {
                itemCooldown.lastUseItemTime.put(itemEnum.getItem(), System.currentTimeMillis());
            }

            if (hbSlot != Minecraft.player.inventory.currentItem) {
                Minecraft.player.connection.sendPacket(new CHeldItemChangePacket(Minecraft.player.inventory.currentItem));
            }
            this.delay = System.currentTimeMillis();
            return hbSlot;
        }

        int invSlot = InventoryUtil.getInstance().getSlotInInventoryOrHotbar(Items.ENDER_PEARL, false);

        if (invSlot != -1) {
            handUtil.setOriginalSlot(Minecraft.player.inventory.currentItem);
            mc.playerController.pickItem(invSlot);
            Minecraft.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            Minecraft.player.swingArm(Hand.MAIN_HAND);

            ModuleReg functionRegistry = Initclass.getInstance().getFunctionRegistry();
            ItemCooldown itemCooldown = functionRegistry.getItemCooldown();
            ItemCooldown.ItemEnum itemEnum = ItemCooldown.ItemEnum.getItemEnum(Items.ENDER_PEARL);

            if (itemCooldown.isState() && itemEnum != null && itemCooldown.isCurrentItem(itemEnum)) {
                itemCooldown.lastUseItemTime.put(itemEnum.getItem(), System.currentTimeMillis());
            }
            this.delay = System.currentTimeMillis();
            return invSlot;
        }
        return -1;
    }


    @Override
    public void onDisable() {
        throwPearl = false;
        delay = 0;
        super.onDisable();
    }
}
