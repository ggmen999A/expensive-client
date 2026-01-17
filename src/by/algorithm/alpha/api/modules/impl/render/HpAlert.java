package by.algorithm.alpha.api.modules.impl.render;

import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.api.modules.settings.impl.SliderSetting;
import by.algorithm.alpha.api.modules.settings.impl.StringSetting;
import by.algorithm.alpha.system.events.EventDisplay;
import by.algorithm.alpha.system.utils.render.DisplayUtils;
import by.algorithm.alpha.system.utils.render.font.font.Fonts;
import com.google.common.eventbus.Subscribe;
import net.minecraft.util.ResourceLocation;

@ModuleAnnot(
        name = "HpAlert",
        type = ModuleCategory.Render,
        description = "Оповещение при низком HP"
)
public class HpAlert extends Module {

    private final SliderSetting hp =
            new SliderSetting("Хп для срабатывания", 8.0F, 1.0F, 20.0F, 1.0F);

    private final StringSetting message =
            new StringSetting("Текст сообщения", "Нужен отхил!", "Текст для оповещения");

    private final ModeSetting font =
            new ModeSetting("Шрифт", "SFUI", "SFUI", "Montserrat");

    private final ModeSetting style =
            new ModeSetting("Стиль", "brokenIOS", "brokenIOS", "fireIOS", "3D", "Alert");

    private float anim = 0.0F;
    private boolean animDir = true;
    private long lastUpdate;

    private static final int FONT_SIZE = 20;
    private static final float IMG_W = 120.0F;
    private static final float IMG_H = 95.0F;
    private static final int Y_POS = 50;
    private static final int TEXT_OFFSET = 15;

    public HpAlert() {
        addSettings(hp, message, font, style);
    }

    @Subscribe
    public void onRender(EventDisplay e) {
        if (e.getType() != EventDisplay.Type.HIGH) return;
        if (mc.player == null) return;
        if (!isState()) return;

        if (mc.player.getHealth() >= hp.get()) return;

        updateAnimation();

        int screenWidth = mc.getMainWindow().getScaledWidth();
        int color = ((int) (anim * 255) << 24) | 0xFFFFFF;

        float imgX = (screenWidth - IMG_W) / 2.0F;
        float imgY = Y_POS;

        String text = message.get();
        float textWidth = Fonts.sfui.getWidth(text, FONT_SIZE);
        float textX = (screenWidth - textWidth) / 2.0F;
        float textY = imgY + IMG_H + TEXT_OFFSET;

        if (anim > 0.0F) {
            switch (font.get()) {
                case "SFUI" ->
                        Fonts.sfui.drawText(e.getMatrixStack(), text, textX, textY, color, FONT_SIZE);
                case "Montserrat" ->
                        Fonts.montserrat.drawText(e.getMatrixStack(), text, textX, textY, color, FONT_SIZE);
            }
        }

        drawImage(imgX, imgY);
    }

    private void updateAnimation() {
        long now = System.currentTimeMillis();
        if (now - lastUpdate < 50L) return;
        lastUpdate = now;

        if (animDir) {
            anim += 0.07F;
            if (anim >= 1.0F) {
                anim = 1.0F;
                animDir = false;
            }
        } else {
            anim -= 0.07F;
            if (anim <= 0.0F) {
                anim = 0.0F;
                animDir = true;
            }
        }
    }

    private void drawImage(float x, float y) {
        ResourceLocation brokenIOS = new ResourceLocation("expensive/images/lowhp1.png");
        ResourceLocation fireIOS = new ResourceLocation("expensive/images/lowhp2.png");
        ResourceLocation threeD = new ResourceLocation("expensive/images/lowhp3.png");
        ResourceLocation alert = new ResourceLocation("expensive/images/lowhp4.png");

        switch (style.get()) {
            case "brokenIOS" ->
                    DisplayUtils.drawImage(brokenIOS, x, y, IMG_W, IMG_H, -2);
            case "fireIOS" ->
                    DisplayUtils.drawImage(fireIOS, x, y, IMG_W, IMG_H, -2);
            case "3D" ->
                    DisplayUtils.drawImage(threeD, x, y, IMG_W, IMG_H, -2);
            case "Alert" ->
                    DisplayUtils.drawImage(alert, x, y, IMG_W, IMG_H, -2);
        }
    }

    @Override
    public void onDisable() {
        anim = 0.0F;
        animDir = true;
        super.onDisable();
    }
}
