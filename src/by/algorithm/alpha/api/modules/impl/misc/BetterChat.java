package by.algorithm.alpha.api.modules.impl.misc;

import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.system.events.EventPacket;
import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

@Getter
@ModuleAnnot(name = "BetterChat", type = ModuleCategory.Misc, description = "Улучшенный чат")
public class BetterChat extends Module {

    private String lastMessage = "";
    private int amount = 1;
    private int line = 0;

    @Subscribe
    private void onPacket(EventPacket e) {
        if (e.getPacket() instanceof SChatPacket chatPacket) {
            final ITextComponent originalComponent = chatPacket.getChatComponent();
            final String rawMessage = originalComponent.getString();
            final NewChatGui chatGui = mc.ingameGUI.getChatGUI();
            ITextComponent message;
            if (this.lastMessage.equals(rawMessage)) {
                chatGui.deleteChatLine(this.line);
                this.amount++;
                message = originalComponent.deepCopy()
                        .append(new StringTextComponent(TextFormatting.GRAY + " (x" + this.amount + ")"));
            } else {
                this.amount = 1;
                message = originalComponent;
            }
            this.line++;
            this.lastMessage = rawMessage;
            chatGui.printChatMessageWithOptionalDeletion(message, this.line);
            if (this.line > 165) {
                this.line = 0;
            }
            e.cancel();
        }
    }
}