package by.algorithm.alpha.api.modules.impl.misc;


import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.system.events.EventUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;

@ModuleAnnot(name = "ExpBottleFill", type = ModuleCategory.Misc)

public class ExpBottleFill extends Module {

    private boolean cmd = false;

    private boolean eblan228 = false;

    private final SliderSetting eblan227 = new SliderSetting("Уровней для заполнения", 15, 15, 50, 15);



    public ExpBottleFill() {

        addSettings(eblan227);

    }



    public void onUpdate(EventUpdate e) {

        if (mc.player == null || mc.world == null) return;

        try {

        } catch (Exception ignored) {}

        if (mc.player.experienceLevel < 15) {

            if (!eblan228) {

                print("У вас нет лвла для заполнения пузырькоф");

                eblan228 = true;

            }

            if (mc.currentScreen instanceof ChestScreen) {

                Minecraft.player.closeScreen();

            }

            return;

        } else {

            eblan228 = false;

        }

        if (mc.currentScreen instanceof ChestScreen chestScreen) {

            int level = Math.round(eblan227.get());

            String search = level + " Уров";

            for (int i = 0; i < chestScreen.getContainer().inventorySlots.size(); ++i) {

                ItemStack stack = chestScreen.getContainer().getSlot(i).getStack();

                String name = stack.getDisplayName().getString();

                if (name.contains(search) || stack.getItem() == net.minecraft.item.Items.DRAGON_BREATH) {

                    mc.playerController.windowClick(chestScreen.getContainer().windowId, i, 0, ClickType.PICKUP, mc.player);

                    break;

                }

            }

        }

        if (!cmd) {

            mc.player.sendChatMessage("/exp");

            cmd = true;

        }

    }




    public void onDisable() {

        cmd = false;

        eblan228 = false;


    }

}
