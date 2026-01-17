package by.algorithm.alpha.system.visuals.mainmenu.altmanager;

import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.api.modules.impl.render.HUD;
import by.algorithm.alpha.system.utils.client.IMinecraft;
import by.algorithm.alpha.system.utils.client.Vec2i;
import by.algorithm.alpha.system.utils.math.MathUtil;
import by.algorithm.alpha.system.utils.math.StopWatch;
import by.algorithm.alpha.system.utils.math.Vector4i;
import by.algorithm.alpha.system.utils.player.TimerUtility;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.Scissor;
import by.algorithm.alpha.system.utils.client.ClientUtil;
import net.minecraft.util.math.vector.Vector4f;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Session;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.List;

public class AltScreen extends Screen implements IMinecraft {

    public AltScreen() {
        super(new StringTextComponent(""));
    }

    public final TimerUtility timer = new TimerUtility();

    public final List<Alt> alts = new ArrayList<>();

    public final StopWatch timer1 = new StopWatch();
    public static float o = 0;
    public float scroll;
    public float scrollAn;

    private String altName = "";
    private boolean typing;
    float minus = 14;
    float offset = 6f;
    float width = 250, height = 240;

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        scrollAn = MathUtil.lerp(scrollAn, scroll, 5);

     //   RenderUtility.drawShader(timer);
      //  RenderUtility.drawImage(backmenu, -5, 0,1000, 1000, -1);
        mc.gameRenderer.setupOverlayRendering(2);
        MainWindow mainWindow = mc.getMainWindow();
        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2f, y = mc.getMainWindow().getScaledHeight() / 2f - height / 2f;


        // Квадрат фона
        float bgX = x - offset, bgY = y - offset, bgWidth = width + offset * 2, bgHeight = height + offset * 2;
        int bgRectColor = ColorUtils.rgba(40, 40, 40, 160);
       // RectUtility.getInstance().drawRoundedRectShadowed(matrixStack, bgX, bgY, bgX + bgWidth, bgY + bgHeight, 8, 5, bgRectColor, bgRectColor, bgRectColor, bgRectColor, false, false, true, true);

        int megaradiun = 12;
        int megaradiun1 = 10;
        DisplayUtils.drawRoundedRect(x, y, width, height + 3, new Vector4f((float)megaradiun, (float)megaradiun, (float)megaradiun, (float)megaradiun), ColorUtils.rgba(25,25,25,222));
        // alt screen name
        Fonts.montserrat.drawCenteredText(matrixStack, "Аккаунты", x + width / 2, y + offset * 2, HUD.getColor(0), 10);
        Fonts.montserrat.drawCenteredText(matrixStack, "Аккаунты", x + width / 2, y + offset * 2, HUD.getColor(0), 10);


        DisplayUtils.drawRoundedRect(x + offset - 1, y + offset + 64 - minus * 2.5f + 177 - offset * 2, width + 2 - offset * 2f, 20f, 3, ColorUtils.rgba(5, 5, 5, 80));

        Scissor.push();
        Scissor.setFromComponentCoordinates(x + offset, y + offset + 64 - minus * 2.5f + 177 - offset * 2, width - offset * 2f, 20f);
        Fonts.montserrat.drawText(matrixStack, typing ? (altName + (typing ? "_" : "")) : "Введи сюда свой никнейм..", x + offset + 5f,
                y + offset + 69f - minus * 2.5f + 177 - offset * 2, ColorUtils.rgba(255, 255, 255, 155), 9);
        Scissor.unset();
        Scissor.pop();

        // Знак для ввода рандомного ника
        int col = ColorUtils.rgb(38, 33, 54);
        DisplayUtils.drawRoundedRect(x + width - offset - Fonts.montserrat.getWidth("Генератор", 10) - offset * 2, y + offset + 63.9f - minus * 2.5f + 177 - offset * 2, Fonts.montserrat.getWidth("Генератор", 10) + 13f, 20,2, ColorUtils.rgba(15, 15, 15, 180));
        Fonts.montserrat.drawCenteredText(matrixStack, "Генератор", x - 2 + width - offset * 2 - 24, y + 5 + offset + 65 - minus * 2.5f + 175 - offset * 2, -1, 10);

        // Вывод никнеймов
        float dick = 1;

        DisplayUtils.drawRoundedRect(x + offset - dick, y + offset + 60f - minus * 2, width - offset * 2f + dick * 2, 177.5f - minus * 2, 6, ColorUtils.rgba(15, 15, 15, 155));

        // Надпись при пустом листе аккаунтов
        if (alts.isEmpty()) Fonts.montserrat.drawCenteredText(matrixStack, "Пустовато как-то...", x + width / 2f, (float) (y + offset + 60f - minus * 2.5 + (177.5f - minus) / 2), -1, 10);
        float size = 0f, iter = scrollAn, offsetAccounts = 0f;

        boolean hovered = false;

        Scissor.push();
        Scissor.setFromComponentCoordinates(x + offset, y + offset + 60f - minus * 2, width - offset * 2f, 177.5f - minus * 2);
        for (Alt alt : alts) {
            float scrollY = y + iter * 22f;
            int color = (mc.session.getUsername().equals(alt.name)) ? ColorUtils.rgba(80, 80, 80, 145) : ColorUtils.rgba(30, 30, 30, 145);

            int radius = 4;
            DisplayUtils.drawRoundedRect(x + offset + 2f, scrollY + offset + 62 + offsetAccounts - minus * 2, width - offset * 2f - 4f, 20f, new Vector4f((float)radius, (float)radius, (float)radius, (float)radius), new Vector4i(ColorUtils.setAlpha(HUD.getColor(0), 65), ColorUtils.setAlpha(HUD.getColor(0), 65), ColorUtils.setAlpha(HUD.getColor(0), 65), ColorUtils.setAlpha(HUD.getColor(0), 65)));
            if(mc.session.getUsername().equals(alt.name)) {
                DisplayUtils.drawRoundedRect(x + offset + 2f, scrollY + offset + 62 + offsetAccounts - minus * 2, width - offset * 2f - 4f, 20f, new Vector4f((float)radius, (float)radius, (float)radius, (float)radius), new Vector4i(ColorUtils.setAlpha(HUD.getColor(0), 135), ColorUtils.setAlpha(HUD.getColor(0), 135), ColorUtils.setAlpha(HUD.getColor(0), 135), ColorUtils.setAlpha(HUD.getColor(0), 135)));

            }
            Fonts.montserrat.drawText(matrixStack, alt.name, x - 15 + offset + 24f, scrollY - 2 + offset + 69 + offsetAccounts - minus * 2, -1, 9);

            mc.getTextureManager().bindTexture(alt.skin);

            iter++;
            size++;
        }
        scroll = MathHelper.clamp(scroll, size > 8 ? -size + 4 : 0, 0);
        Scissor.unset();
        Scissor.pop();

        mc.gameRenderer.setupOverlayRendering();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!altName.isEmpty() && typing)
                altName = altName.substring(0, altName.length() - 1);
        }

        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            if (!altName.isEmpty() && altName.length() >= 3) {
                alts.add(new Alt(altName));
                AltConfig.updateFile();
            }
            typing = false;
            altName = "";
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (typing) {
                typing = false;
                altName = "";
            }
        }

        boolean ctrlDown = GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(mc.getMainWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
        if (typing) {
            if (ClientUtil.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_V) {
                try {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (ClientUtil.ctrlIsDown() && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                try {
                    altName = "";
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (altName.length() <= 20) altName += Character.toString(codePoint);
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vec2i fixed = MathUtil.getMouse2i((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2f, y = mc.getMainWindow().getScaledHeight() / 2f - height / 2f;

        if (button == 0 && DisplayUtils.isInRegion(mouseX, mouseY, x + width - offset - Fonts.montserrat.getWidth("Random", 9) - offset * 2, y + offset + 64 - minus * 2.5f + 177 - offset * 2, Fonts.montserrat.getWidth("Random", 9) + 13f, 20)) {
            alts.add(new Alt(Initclass.getInstance().randomNickname()));
            AltConfig.updateFile();
        }
        if (button == 0 && DisplayUtils.isInRegion(mouseX, mouseY, x + offset - 1, y + offset + 64 - minus * 2.5f + 177 - offset * 2, width + 2 - offset * 2f, 20f)
                && !DisplayUtils.isInRegion(mouseX, mouseY, x + width - offset - Fonts.montserrat.getWidth("Random", 9) - offset * 2, y + offset + 64 - minus * 2.5f + 177 - offset * 2, Fonts.montserrat.getWidth("Random", 9) + 12f, 20)) {
            typing = !typing;
        }

        // Основной функционал позволяющий позволяющий брать/удалять ник
        float iter = scrollAn, offsetAccounts = 0f;
        Iterator<Alt> iterator = alts.iterator();
        while (iterator.hasNext()) {
           Alt account = iterator.next();

            float scrollY = y + iter * 22f;

            if (DisplayUtils.isInRegion(mouseX, mouseY, x + offset + 2f, scrollY + offset + 62 + offsetAccounts - minus * 2, width - offset * 2f - 4f, 20f)) {
                if (button == 0) {
                    mc.session = new Session(account.name, "", "", "mojang");
                } else if (button == 1) {
                    iterator.remove();
                    AltConfig.updateFile();;
                }
            }

            iter++;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        Vec2i fixed = MathUtil.getMouse2i((int) mouseX, (int) mouseY);
        mouseX = fixed.getX();
        mouseY = fixed.getY();

        float x = mc.getMainWindow().getScaledWidth() / 2f - width / 2f, y = mc.getMainWindow().getScaledHeight() / 2f - height / 2f;

        if (DisplayUtils.isInRegion(mouseX, mouseY, x + offset, y + offset + 60f - minus * 2, width - offset * 2f, 177.5f - minus * 2)) scroll += delta * 1;
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
    }

    @Override
    public void tick() {
        super.tick();
    }
}
