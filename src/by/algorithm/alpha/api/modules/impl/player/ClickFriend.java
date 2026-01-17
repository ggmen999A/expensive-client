package by.algorithm.alpha.api.modules.impl.player;

import com.google.common.eventbus.Subscribe;
import by.algorithm.alpha.system.events.EventKey;
import by.algorithm.alpha.api.modules.api.ModuleCategory;
import by.algorithm.alpha.api.modules.api.Module;
import by.algorithm.alpha.api.modules.settings.impl.BindSetting;
import by.algorithm.alpha.system.utils.player.PlayerUtils;
import net.minecraft.entity.player.PlayerEntity;
import by.algorithm.alpha.api.command.friends.FriendStorage;
import by.algorithm.alpha.api.modules.api.ModuleAnnot;

@ModuleAnnot(name = "ClickFriend", type = ModuleCategory.Player, description = "Ну что тут непонятного сука")
public class ClickFriend extends Module {
    final BindSetting throwKey = new BindSetting("Кнопка", -98);
    public ClickFriend() {
        addSettings(throwKey);
    }
    @Subscribe
    public void onKey(EventKey e) {
        if (e.getKey() == throwKey.get() && mc.pointedEntity instanceof PlayerEntity) {

            if (mc.player == null || mc.pointedEntity == null) {
                return;
            }

            String playerName = mc.pointedEntity.getName().getString();

            if (!PlayerUtils.isNameValid(playerName)) {
                print("Невозможно добавить бота в друзья, увы, как бы вам не хотелось это сделать");
                return;
            }

            if (FriendStorage.isFriend(playerName)) {
                FriendStorage.remove(playerName);
                printStatus(playerName, true);
            } else {
                FriendStorage.add(playerName);
                printStatus(playerName, false);
            }
        }
    }

    void printStatus(String name, boolean remove) {
        if (remove) print(name + " удалён из друзей");
        else print(name + " добавлен в друзья");
    }
}
