package by.algorithm.alpha.api.modules.impl.misc;


import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.settings.impl.BindSetting;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.ModeListSetting;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.system.events.EventKey;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.system.utils.math.StopWatch;
import by.algorithm.alpha.system.utils.player.InventoryUtil;
import com.google.common.eventbus.Subscribe;
import net.minecraft.item.AirItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.text.TextFormatting;

@ModuleAnnot(name = "ServerHelper", type = ModuleCategory.Misc)
public class AnarchyHelper extends Module {
    private final ModeSetting mode = new ModeSetting("Сервер", "FunTime", "FunTime", "HolyWorld");
    private final ModeListSetting typemode = new ModeListSetting("Тип",
            new BooleanSetting("Использование по бинду", true));

    private final BindSetting stanKey = new BindSetting("Кнопка Стана", -1)
            .setVisible(() -> typemode.getValueByName("Использование по бинду").get()).setVisible(() -> mode.is("HolyWorld"));
    private final BindSetting opitKey = new BindSetting("Кнопка П.Опыта", -1)
            .setVisible(() -> typemode.getValueByName("Использование по бинду").get()).setVisible(() -> mode.is("HolyWorld"));
    private final BindSetting vzrivtrapKey = new BindSetting("Кнопка В.Трапки", -1)
            .setVisible(() -> typemode.getValueByName("Использование по бинду").get()).setVisible(() -> mode.is("HolyWorld"));
    private final BindSetting flamepalkaKey = new BindSetting("Кнопка B.Палочки", -1)
            .setVisible(() -> typemode.getValueByName("Использование по бинду").get()).setVisible(() -> mode.is("HolyWorld"));
    private final BindSetting serkaKey = new BindSetting("Кнопка М.Зелья", -1)
            .setVisible(() -> typemode.getValueByName("Использование по бинду").get()).setVisible(() -> mode.is("HolyWorld"));


    //ft
    private final BindSetting disorientationKey = new BindSetting("Дезориентация", -1)
            .setVisible(() -> typemode.getValueByName("Использование по бинду").get()).setVisible(() -> mode.is("FunTime"));
    private final BindSetting plastKey = new BindSetting("Пласт", -1)
            .setVisible(() -> typemode.getValueByName("Использование по бинду").get()).setVisible(() -> mode.is("FunTime"));
    private final BindSetting yawpilKey = new BindSetting("Явная пыль", -1)
            .setVisible(() -> typemode.getValueByName("Использование по бинду").get()).setVisible(() -> mode.is("FunTime"));
    private final BindSetting trapKey = new BindSetting("Трапка", -1)
            .setVisible(() -> typemode.getValueByName("Использование по бинду").get()).setVisible(() -> mode.is("FunTime"));
    final StopWatch stopWatch = new StopWatch();




    InventoryUtil.Hand handUtil = new InventoryUtil.Hand();
    long delay;
    boolean stanThrow, opitThrow, vzrivtrapkaThrow, flamepalkaThrow, serkaThrow;
    boolean disorientationThrow, trapThrow, plastThorw, yawpilThorw;


    public AnarchyHelper() {
        addSettings(mode, stanKey, opitKey, vzrivtrapKey, flamepalkaKey, serkaKey, disorientationKey, trapKey, plastKey, yawpilKey);
    }


    @Subscribe
    private void onKey(EventKey e) {
        if (e.getKey() == stanKey.get()) {
            stanThrow = true;
        }
        if (e.getKey() == opitKey.get()) {
            opitThrow = true;
        }
        if (e.getKey() == vzrivtrapKey.get()) {
            vzrivtrapkaThrow = true;
        }
        if (e.getKey() == flamepalkaKey.get()) {
            flamepalkaThrow = true;
        }
        if (e.getKey() == serkaKey.get()) {
            serkaThrow = true;
        }
        if (e.getKey() == disorientationKey.get()) {
            disorientationThrow = true;
        }
        if (e.getKey() == trapKey.get()) {
            trapThrow = true;
        }
        if (e.getKey() == plastKey.get()) {
            plastThorw = true;
        }
        if (e.getKey() == yawpilKey.get()) {
            yawpilThorw = true;
        }
    }
    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (stanThrow) {
            if (disorientationThrow) {
                this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
                int hbSlot = getItemForName("дезориентация", true);
                int invSlot = getItemForName("дезориентация", false);

                if (invSlot == -1 && hbSlot == -1) {
                    print("Дезориентация не найдена!");
                    disorientationThrow = false;
                    return;
                }

                if (!mc.player.getCooldownTracker().hasCooldown(Items.ENDER_EYE)) {
                    print("Заюзал дезориентацию!");
                    int slot = findAndTrowItem(hbSlot, invSlot);
                    if (slot > 8) {
                        mc.playerController.pickItem(slot);
                    }
                }
                disorientationThrow = false;
            }
            if (plastThorw) {
                this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
                int hbSlot = getItemForName("пласт", true);
                int invSlot = getItemForName("пласт", false);

                if (invSlot == -1 && hbSlot == -1) {
                    print("Пласт не найден!");
                    plastThorw = false;
                    return;
                }

                if (!mc.player.getCooldownTracker().hasCooldown(Items.DRIED_KELP)) {
                    print("Заюзал пласт!");
                    int slot = findAndTrowItem(hbSlot, invSlot);
                    if (slot > 8) {
                        mc.playerController.pickItem(slot);
                    }
                }
                plastThorw = false;
            }
            if (yawpilThorw) {
                this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
                int hbSlot = getItemForName("явная пыль", true);
                int invSlot = getItemForName("явная пыль", false);

                if (invSlot == -1 && hbSlot == -1) {
                    print("Пыль не найдена!");
                    yawpilThorw = false;
                    return;
                }

                if (!mc.player.getCooldownTracker().hasCooldown(Items.SUGAR)) {
                    print("Заюзал Явную пыль!");
                    int slot = findAndTrowItem(hbSlot, invSlot);
                    if (slot > 8) {
                        mc.playerController.pickItem(slot);
                    }
                }
                yawpilThorw = false;
            }
            if (trapThrow) {
                int hbSlot = getItemForName("трапка", true);
                int invSlot = getItemForName("трапка", false);


                if (invSlot == -1 && hbSlot == -1) {
                    print("Трапка не найдена");
                    trapThrow = false;
                    return;
                }

                if (!mc.player.getCooldownTracker().hasCooldown(Items.NETHERITE_SCRAP)) {
                    print("Заюзал трапку!");
                    int old = mc.player.inventory.currentItem;

                    int slot = findAndTrowItem(hbSlot, invSlot);
                    if (slot > 8) {
                        mc.playerController.pickItem(slot);
                    }
                    if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                        mc.player.inventory.currentItem = old;
                    }
                }
                trapThrow = false;
            }




            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
            int hbSlot = getItemForName("оглушение", true);
            int invSlot = getItemForName("оглушение", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Стан не найден!");
                stanThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.NETHER_STAR)) {
                print("Использовал стан!");
                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
            }
            stanThrow = false;
        }
        if (opitThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
            int hbSlot = getItemForName("пузырь опыта", true);
            int invSlot = getItemForName("пузырь опыта", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Пузырь опыта не найден!");
                opitThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.EXPERIENCE_BOTTLE)) {
                print("Использовал пузырь с опытом!");
                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
            }
            opitThrow = false;
        }
        if (vzrivtrapkaThrow) {
            this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
            int hbSlot = getItemForName("взрывная трапка", true);
            int invSlot = getItemForName("взрывная трапка", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Взрывная трапка не найдена!");
                vzrivtrapkaThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.PRISMARINE_SHARD)) {
                print("Заюзал взрывную трапку!");
                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
            }
            vzrivtrapkaThrow = false;
        }
        if (flamepalkaThrow) {
            // САЛАТ СПАСАЙ
            int hbSlot = getItemForName("взрывная палочка", true); // мега поиск ящика
            int invSlot = getItemForName("взрывная палочка", false); // мега поиск ящика


            if (invSlot == -1 && hbSlot == -1) {
                print("Взрыв. палка не найдена!");
                flamepalkaThrow = false;
                return;
            }

            if (!mc.player.getCooldownTracker().hasCooldown(Items.BLAZE_ROD)) {
                print("Использовал взрывную палочку!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            flamepalkaThrow = false;
        }
        if (serkaThrow) {
            int hbSlot = getItemForName("молочно", true);
            int invSlot = getItemForName("молочно", false);

            if (invSlot == -1 && hbSlot == -1) {
                print("Молочное зелье не найдено");
                serkaThrow = false;
                return;
            }


            if (!mc.player.getCooldownTracker().hasCooldown(Items.SPLASH_POTION)) {
                print("Использовал молочное зелье!");
                int old = mc.player.inventory.currentItem;

                int slot = findAndTrowItem(hbSlot, invSlot);
                if (slot > 8) {
                    mc.playerController.pickItem(slot);
                }
                if (InventoryUtil.findEmptySlot(true) != -1 && mc.player.inventory.currentItem != old) {
                    mc.player.inventory.currentItem = old;
                }
            }
            serkaThrow = false;
        }
        this.handUtil.handleItemChange(System.currentTimeMillis() - this.delay > 200L);
    }


    private int findAndTrowItem(int hbSlot, int invSlot) {
        if (hbSlot != -1) {
            this.handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.player.connection.sendPacket(new CHeldItemChangePacket(hbSlot));
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);
            this.delay = System.currentTimeMillis();
            return hbSlot;
        }
        if (invSlot != -1) {
            handUtil.setOriginalSlot(mc.player.inventory.currentItem);
            mc.playerController.pickItem(invSlot);
            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.MAIN_HAND));
            mc.player.swingArm(Hand.MAIN_HAND);
            this.delay = System.currentTimeMillis();
            return invSlot;
        }
        return -1;
    }
    @Override
    public void onDisable() {
        flamepalkaThrow = false;
        vzrivtrapkaThrow = false;
        opitThrow = false;
        stanThrow = false;
        serkaThrow = false;
        delay = 0;
        super.onDisable();
    }
    private int getItemForName(String name, boolean inHotBar) {
        int firstSlot = inHotBar ? 0 : 9;
        int lastSlot = inHotBar ? 9 : 36;
        for (int i = firstSlot; i < lastSlot; i++) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);

            if (itemStack.getItem() instanceof AirItem) {
                continue;
            }

            String displayName = TextFormatting.getTextWithoutFormattingCodes(itemStack.getDisplayName().getString());
            if (displayName != null && displayName.toLowerCase().contains(name)) {
                return i;
            }
        }
        return -1;
    }




}
