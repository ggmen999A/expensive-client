package by.algorithm.alpha.api.modules.api;

import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.api.modules.impl.misc.ClientSounds;
import by.algorithm.alpha.api.modules.notifications.Notifications;
import by.algorithm.alpha.api.modules.settings.Setting;
import by.algorithm.alpha.system.utils.client.ClientUtil;
import by.algorithm.alpha.system.utils.client.IMinecraft;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import ru.hogoshi.Animation;
import ru.hogoshi.util.Easings;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public abstract class Module implements IMinecraft {

    final String name;
    final ModuleCategory category;
    private final String description;

    boolean state;
    @Setter
    int bind;
    final List<Setting<?>> settings = new ObjectArrayList<>();

    final Animation animation = new Animation();

    public Module() {
        this.name = getClass().getAnnotation(ModuleAnnot.class).name();
        this.category = getClass().getAnnotation(ModuleAnnot.class).type();
        this.bind = getClass().getAnnotation(ModuleAnnot.class).key();
        this.description = getClass().getAnnotation(ModuleAnnot.class).description();
    }

    public Module(String name) {
        this.name = name;
        this.category = ModuleCategory.Combat;
        this.description = this.getClass().getAnnotation(ModuleAnnot.class).description();
    }

    public void addSettings(Setting<?>... settings) {
        this.settings.addAll(List.of(settings));
    }

    public void onEnable() {
        animation.animate(1, 0.25f, Easings.CIRC_OUT);
        Initclass.getInstance().getEventBus().register(this);
    }

    public void onDisable() {
        animation.animate(0, 0.25f, Easings.CIRC_OUT);
        Initclass.getInstance().getEventBus().unregister(this);
    }


    // Замените существующий метод toggle() на этот:
    public final void toggle() {
        boolean bl = this.state = !this.state;

        if (!this.state) {
            this.onDisable();
            // Используем статический метод add() вместо прямого обращения к NOTIFICATION_MANAGER
            Notifications.add(this.name + " выключен.", "", 2);
        } else {
            this.onEnable();
            // Используем статический метод add() вместо прямого обращения к NOTIFICATION_MANAGER
            Notifications.add(this.name + " включен.", "", 2);
        }
    }

    public final void setState(boolean newState, boolean config) {
        if (state == newState) {
            return;
        }

        state = newState;

        try {
            if (state) {
                onEnable();
            } else {
                onDisable();
            }
            if (!config) {
                ModuleReg functionRegistry = Initclass.getInstance().getFunctionRegistry();
                ClientSounds clientSounds = functionRegistry.getClientSounds();

                if (clientSounds != null && clientSounds.isState()) {
                    String fileName = clientSounds.getFileName(state);
                    float volume = clientSounds.volume.get();
                    ClientUtil.playSound(fileName, volume, false);
                }
            }
        } catch (Exception e) {
            handleException(state ? "onEnable" : "onDisable", e);
        }

    }

    private void handleException(String methodName, Exception e) {
        if (mc.player != null) {
            print("[" + name + "] Произошла ошибка в методе " + TextFormatting.RED + methodName + TextFormatting.WHITE
                    + "() Предоставьте это сообщение разработчику: " + TextFormatting.GRAY + e.getMessage());
            e.printStackTrace();
        } else {
            System.out.println("[" + name + " Error" + methodName + "() Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}