package by.algorithm.alpha.api.modules.impl.render;

import by.algorithm.alpha.api.modules.settings.impl.ModeSetting;
import by.algorithm.alpha.system.visuals.hud.impl.*;
import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.system.events.EventDisplay;
import by.algorithm.alpha.system.events.EventUpdate;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;
import by.algorithm.alpha.api.modules.settings.impl.BooleanSetting;
import by.algorithm.alpha.api.modules.settings.impl.ModeListSetting;
import by.algorithm.alpha.system.visuals.styles.StyleManager;
import by.algorithm.alpha.system.utils.dragable.Dragging;
import by.algorithm.alpha.system.utils.render.ColorUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleAnnot(name = "HUD", type = ModuleCategory.Render, description = "Интерфейс клиента")
public class HUD extends Module {

    public static ModeListSetting elements = new ModeListSetting("Элементы",
            new BooleanSetting("Лого", true),
            new BooleanSetting("Эффекты", false),
            new BooleanSetting("Список модерации", false),
            new BooleanSetting("Активная музыка", false),
            new BooleanSetting("Активные бинды", false),
            new BooleanSetting("Активный таргет", false),
            new BooleanSetting("Броня", false)
    );

    private final ModeSetting hpDisplayType = new ModeSetting("Формат HP", "Число", "Число", "Проценты");

    final WaterMark waterMark;
    final ArmorInfo info;
    final Potions potionRenderer;
    final ActiveBinds keyBindRenderer;
    final TargetHUD targetInfoRenderer;
    final StaffActive staffActive;
    final MediaplayerRenderer mediaplayerRenderer;

    @Subscribe
    private void onUpdate(EventUpdate e) {
        if (mc.gameSettings.showDebugInfo) {
            return;
        }
        if (elements.getValueByName("Список модерации").get()) staffActive.update(e);
    }

    @Subscribe
    private void onDisplay(EventDisplay e) {
        if (mc.gameSettings.showDebugInfo || e.getType() != EventDisplay.Type.POST) {
            return;
        }

        if (elements.getValueByName("Лого").get()) waterMark.render(e);
        if (elements.getValueByName("Броня").get()) info.render(e);
        if (elements.getValueByName("Эффекты").get()) potionRenderer.render(e);
        if (elements.getValueByName("Активные бинды").get()) keyBindRenderer.render(e);
        if (elements.getValueByName("Активная музыка").get()) mediaplayerRenderer.render(e);
        if (elements.getValueByName("Список модерации").get()) staffActive.render(e);
        if (elements.getValueByName("Активный таргет").get()) {
            targetInfoRenderer.setHpDisplayType(hpDisplayType.get());
            targetInfoRenderer.render(e);
        }
    }

    public HUD() {
        Dragging potions = Initclass.getInstance().createDrag(this, "Potions", 278, 5);
        Dragging keyBinds = Initclass.getInstance().createDrag(this, "Key Binds", 185, 5);
        Dragging dragging = Initclass.getInstance().createDrag(this, "TargetHUD", 74, 128);
        Dragging staffList = Initclass.getInstance().createDrag(this, "Staff Active", 96, 5);
        Dragging activeMusic = Initclass.getInstance().createDrag(this, "Music Active", 96, 5);

        potionRenderer = new Potions(potions);
        keyBindRenderer = new ActiveBinds(keyBinds);
        mediaplayerRenderer = new MediaplayerRenderer(activeMusic);
        targetInfoRenderer = new TargetHUD(dragging);
        staffActive = new StaffActive(staffList);
        waterMark = new WaterMark();
        info = new ArmorInfo();

        addSettings(elements, hpDisplayType);
    }

    public static int getColor(int index) {
        StyleManager styleManager = Initclass.getInstance().getStyleManager();
        return ColorUtils.gradient(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), index * 16, 10);
    }

    public static int getColor(int index, float mult) {
        StyleManager styleManager = Initclass.getInstance().getStyleManager();
        return ColorUtils.gradient(styleManager.getCurrentStyle().getFirstColor().getRGB(), styleManager.getCurrentStyle().getSecondColor().getRGB(), (int) (index * mult), 10);
    }

    public static int getColor(int firstColor, int secondColor, int index, float mult) {
        return ColorUtils.gradient(firstColor, secondColor, (int) (index * mult), 10);
    }
}