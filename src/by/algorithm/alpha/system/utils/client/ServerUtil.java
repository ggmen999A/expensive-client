package by.algorithm.alpha.system.utils.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

public class ServerUtil implements IMinecraft{
    public static String calculateBPS() {
        return String.format("%.2f", Math.hypot(ServerUtil.mc.player.getPosX() - ServerUtil.mc.player.prevPosX, ServerUtil.mc.player.getPosZ() - ServerUtil.mc.player.prevPosZ) * (double)ServerUtil.mc.timer.timerSpeed * 20.0);
    }

    public static void drawItemStack(ItemStack itemStack, float f, float f2, boolean bl, boolean bl2, float f3) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef(f, f2, 0.0f);
        if (bl2) {
            GL11.glScaled(f3, f3, f3);
        }
        mc.getItemRenderer().renderItemAndEffectIntoGUI(itemStack, 0, 0);
        if (bl) {
            mc.getItemRenderer().renderItemOverlays(ServerUtil.mc.fontRenderer, itemStack, 0, 0);
        }
        RenderSystem.popMatrix();
    }

    public static int calculatePing() {
        return mc.player.connection.getPlayerInfo(mc.player.getUniqueID()) != null ?
                mc.player.connection.getPlayerInfo(mc.player.getUniqueID()).getResponseTime() : 0;
    }

    public static String serverIP() {
        return mc.getCurrentServerData() != null && ServerUtil.mc.getCurrentServerData().serverIP != null && !mc.isSingleplayer() ? ServerUtil.mc.getCurrentServerData().serverIP : "localhost";
    }
}
