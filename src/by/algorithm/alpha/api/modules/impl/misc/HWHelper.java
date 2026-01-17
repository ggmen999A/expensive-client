package by.algorithm.alpha.api.modules.impl.misc;


import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.settings.impl.BindSetting;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.ModeListSetting;
import by.algorithm.alpha.system.events.EventKey;
import by.algorithm.alpha.system.events.EventPacket;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.system.utils.player.InventoryUtil;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.Minecraft;
import net.minecraft.item.AirItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;

@ModuleAnnot(
        name = "HWHelper",
        type = ModuleCategory.Misc,
        description = "Хелпа на хв "
)
public class HWHelper extends Module {
    private final ModeListSetting useMode = new ModeListSetting(
            "Режим",
            new BooleanSetting("Packet", true),
            new BooleanSetting("Legit", false)
    );

    private final ModeListSetting mode = new ModeListSetting("Тип", new BooleanSetting("Использование по бинду", true), new BooleanSetting("Закрывать меню", true));
    private final BindSetting fireKey = (new BindSetting("Кнопка взрывной штучки", -1)).setVisible(() -> this.mode.getValueByName("Использование по бинду").get());
    private final BindSetting stanKey = (new BindSetting("Кнопка стана", -1)).setVisible(() -> this.mode.getValueByName("Использование по бинду").get());
    private final BindSetting trapKey = (new BindSetting("Кнопка взрывной трапки", -1)).setVisible(() -> this.mode.getValueByName("Использование по бинду").get());
    InventoryUtil.Hand handUtil = new InventoryUtil.Hand();
    long delay;
    boolean FireThrow;
    boolean trapThrow;
    boolean StanThrow;

    public HWHelper() {
        this.addSettings(useMode, this.fireKey, this.trapKey, this.stanKey);
    }

    @Subscribe
    private void onKey(EventKey e) {
        if (e.getKey() == this.fireKey.get()) {
            this.FireThrow = true;
        }

        if (e.getKey() == this.stanKey.get()) {
            this.StanThrow = true;
        }

        if (e.getKey() == this.trapKey.get()) {
            this.trapThrow = true;
        }

    }
    private int useItemPacket(int hbSlot, int invSlot) {
        if (hbSlot != -1) {
            handUtil.setOriginalSlot(Minecraft.player.inventory.currentItem);
            Minecraft.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            Minecraft.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            Minecraft.player.swingArm(Hand.MAIN_HAND);
            delay = System.currentTimeMillis();
            return hbSlot;
        }

        if (invSlot != -1) {
            handUtil.setOriginalSlot(Minecraft.player.inventory.currentItem);
            GriefHelper.mc.playerController.pickItem(invSlot);
            Minecraft.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            Minecraft.player.swingArm(Hand.MAIN_HAND);
            delay = System.currentTimeMillis();
            return invSlot;
        }

        return -1;
    }
    private int useItemLegit(int hbSlot, int invSlot) {
        int oldSlot = Minecraft.player.inventory.currentItem;

        if (hbSlot != -1) {
            Minecraft.player.inventory.currentItem = hbSlot;
        } else if (invSlot != -1) {
            GriefHelper.mc.playerController.pickItem(invSlot);
        } else {
            return -1;
        }

        mc.playerController.processRightClick(
                Minecraft.player,
                Minecraft.world,
                Hand.MAIN_HAND
        );

        Minecraft.player.swingArm(Hand.MAIN_HAND);
        delay = System.currentTimeMillis();

        Minecraft.player.inventory.currentItem = oldSlot;
        return hbSlot != -1 ? hbSlot : invSlot;
    }

    @Subscribe
    private void onUpdate(EventUpdate e) {
        int old;
        int invSlot;
        int hbSlot;
        if (this.FireThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
            hbSlot = this.getItemForName("взрывная штучка", true);
            invSlot = this.getItemForName("взрывная штучка", false);
            if (invSlot == -1 && hbSlot == -1) {
                this.print("Взрывная штучка не найдена!");
                this.FireThrow = false;
                return;
            }

            if (!Minecraft.player.getCooldownTracker().hasCooldown(Items.FIRE_CHARGE)) {
                this.print("Заюзал взрывную штучку!");
                old = useItem(hbSlot, invSlot);
                if (old > 8) {
                    GriefHelper.mc.playerController.pickItem(old);
                }
            }

            this.FireThrow = false;
        }

        int slot;
        if (this.StanThrow) {
            hbSlot = this.getItemForName("стан", true);
            invSlot = this.getItemForName("стан", true);
            if (invSlot == -1 && hbSlot == -1) {
                this.print("Стан не найден!");
                this.StanThrow = false;
                return;
            }

            if (!Minecraft.player.getCooldownTracker().hasCooldown(Items.NETHER_STAR)) {
                this.print("Заюзал стан!");
                old = Minecraft.player.inventory.currentItem;
                slot = this.useItem(hbSlot, invSlot);
                if (slot > 8) {
                    GriefHelper.mc.playerController.pickItem(slot);
                }

                if (InventoryUtil.findEmptySlot(true) != -1 && Minecraft.player.inventory.currentItem != old) {
                    Minecraft.player.inventory.currentItem = old;
                }
            }

            this.StanThrow = false;
        }

        if (this.trapThrow) {
            hbSlot = this.getItemForName("взрывная трапка", true);
            invSlot = this.getItemForName("взрывная трапка", false);
            if (invSlot == -1 && hbSlot == -1) {
                this.print("Взрывная трапка не найдена!");
                this.trapThrow = false;
                return;
            }

            if (!Minecraft.player.getCooldownTracker().hasCooldown(Items.PRISMARINE_SHARD)) {
                this.print("Заюзал взрывную трапку!");
                old = Minecraft.player.inventory.currentItem;
                slot = this.useItem(hbSlot, invSlot);
                if (slot > 8) {
                    GriefHelper.mc.playerController.pickItem(slot);
                }

                if (InventoryUtil.findEmptySlot(true) != -1 && Minecraft.player.inventory.currentItem != old) {
                    Minecraft.player.inventory.currentItem = old;
                }
            }

            this.trapThrow = false;
        }

        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
    }

    @Subscribe
    private void onPacket(EventPacket e) {
        this.handUtil.onEventPacket(e);
    }

    private int useItem(int hbSlot, int invSlot) {
        if (useMode.getValueByName("Packet").get()) {
            return useItemPacket(hbSlot, invSlot);
        } else {
            return useItemLegit(hbSlot, invSlot);
        }
    }


    public void onDisable() {
        this.FireThrow = false;
        this.trapThrow = false;
        this.delay = 0L;
        super.onDisable();
    }

    private int getItemForName(String name, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;

        for(int i = firstSlot; i < lastSlot; ++i) {
            ItemStack itemStack = Minecraft.player.inventory.getStackInSlot(i);
            String displayName;
            if (!(itemStack.getItem() instanceof AirItem) && (displayName = TextFormatting.getTextWithoutFormattingCodes(itemStack.getDisplayName().getString())) != null && displayName.toLowerCase().contains(name)) {
                return i;
            }
        }

        return -1;
    }
}