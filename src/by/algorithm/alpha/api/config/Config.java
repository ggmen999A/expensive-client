package by.algorithm.alpha.api.config;

import by.algorithm.alpha.api.modules.settings.impl.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.api.modules.settings.Setting;
import by.algorithm.alpha.system.visuals.styles.Style;
import by.algorithm.alpha.system.utils.client.IMinecraft;
import by.algorithm.alpha.system.utils.dragable.DragManager;
import by.algorithm.alpha.system.utils.dragable.Dragging;
import lombok.Getter;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class Config implements IMinecraft {
    private final File file;
    private final String name;
    private long lastUpdated;
    private boolean modified; // Track if config has been modified

    public Config(String name) {
        this.name = name;
        this.file = new File(new File(Minecraft.getInstance().gameDir, "\\expensive\\configs"), name + ".json");
        this.lastUpdated = Instant.now().getEpochSecond();
        this.modified = false;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public void loadConfig(JsonObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        if (jsonObject.has("lastUpdated")) {
            this.lastUpdated = jsonObject.get("lastUpdated").getAsLong();
        }

        if (jsonObject.has("functions")) {
            loadFunctionSettings(jsonObject.getAsJsonObject("functions"));
        }

        if (jsonObject.has("styles")) {
            loadStyleSettings(jsonObject.getAsJsonObject("styles"));
        }

        if (jsonObject.has("dragging")) {
            loadDraggingSettings(jsonObject.getAsJsonObject("dragging"));
        }
        this.modified = false; // Reset modified flag after loading
    }

    private void loadDraggingSettings(JsonObject draggingObject) {
        for (Map.Entry<String, JsonElement> entry : draggingObject.entrySet()) {
            String elementName = entry.getKey();
            JsonObject positionObject = entry.getValue().getAsJsonObject();

            Dragging dragging = DragManager.draggables.get(elementName);
            if (dragging != null) {
                if (positionObject.has("x")) {
                    dragging.setX(positionObject.get("x").getAsFloat());
                }
                if (positionObject.has("y")) {
                    dragging.setY(positionObject.get("y").getAsFloat());
                }
                if (positionObject.has("width")) {
                    dragging.setWidth(positionObject.get("width").getAsFloat());
                }
                if (positionObject.has("height")) {
                    dragging.setHeight(positionObject.get("height").getAsFloat());
                }
                this.modified = true; // Mark as modified when settings are loaded
            }
        }
    }

    private void loadStyleSettings(JsonObject stylesObject) {
        for (Map.Entry<String, JsonElement> entry : stylesObject.entrySet()) {
            String styleName = entry.getKey();
            JsonObject styleObject = entry.getValue().getAsJsonObject();
            Style style = findStyleByName(styleName);
            if (style != null && styleObject.has("selected")) {
                boolean isSelected = styleObject.get("selected").getAsBoolean();
                if (isSelected) {
                    Initclass.getInstance().getStyleManager().setCurrentStyle(style);
                    this.modified = true; // Mark as modified when style is selected
                }
            }
        }
    }

    private Style findStyleByName(String styleName) {
        for (Style style : Initclass.getInstance().getStyleManager().getStyleList()) {
            if (style.getStyleName().equalsIgnoreCase(styleName)) {
                return style;
            }
        }
        return null;
    }

    private void loadFunctionSettings(JsonObject functionsObject) {
        Initclass.getInstance().getFunctionRegistry().getFunctions().forEach(f -> {
            JsonObject moduleObject = functionsObject.getAsJsonObject(f.getName().toLowerCase());
            if (moduleObject == null) {
                return;
            }

            f.setState(false, true);
            loadSettingFromJson(moduleObject, "bind", value -> f.setBind(value.getAsInt()));
            loadSettingFromJson(moduleObject, "state", value -> f.setState(value.getAsBoolean(), true));
            f.getSettings().forEach(setting -> loadIndividualSetting(moduleObject, setting));
            this.modified = true; // Mark as modified when function settings are loaded
        });
    }

    private void loadIndividualSetting(JsonObject moduleObject, Setting<?> setting) {
        JsonElement settingElement = moduleObject.get(setting.getName());

        if (settingElement == null || settingElement.isJsonNull()) {
            return;
        }

        if (setting instanceof SliderSetting) {
            ((SliderSetting) setting).set(settingElement.getAsFloat());
        } else if (setting instanceof BooleanSetting) {
            ((BooleanSetting) setting).set(settingElement.getAsBoolean());
        } else if (setting instanceof ColorSetting) {
            ((ColorSetting) setting).set(settingElement.getAsInt());
        } else if (setting instanceof ModeSetting) {
            ((ModeSetting) setting).set(settingElement.getAsString());
        } else if (setting instanceof BindSetting) {
            ((BindSetting) setting).set(settingElement.getAsInt());
        } else if (setting instanceof StringSetting) {
            ((StringSetting) setting).set(settingElement.getAsString());
        } else if (setting instanceof ModeListSetting) {
            loadModeListSetting((ModeListSetting) setting, moduleObject);
        }
        this.modified = true; // Mark as modified when individual settings are loaded
    }

    private void loadModeListSetting(ModeListSetting setting, JsonObject moduleObject) {
        JsonObject elements = moduleObject.getAsJsonObject(setting.getName());
        setting.get().forEach(option -> {
            JsonElement optionElement = elements.get(option.getName());
            if (optionElement != null && !optionElement.isJsonNull()) {
                option.set(optionElement.getAsBoolean());
                this.modified = true; // Mark as modified when mode list settings are loaded
            }
        });
    }

    private void loadSettingFromJson(JsonObject jsonObject, String key, Consumer<JsonElement> consumer) {
        JsonElement element = jsonObject.get(key);
        if (element != null && !element.isJsonNull()) {
            consumer.accept(element);
            this.modified = true; // Mark as modified when settings are loaded
        }
    }

    public JsonObject saveConfig() {
        JsonObject functionsObject = new JsonObject();
        JsonObject stylesObject = new JsonObject();
        JsonObject draggingObject = new JsonObject();

        saveFunctionSettings(functionsObject);
        saveStyleSettings(stylesObject);
        saveDraggingSettings(draggingObject);

        JsonObject newObject = new JsonObject();
        newObject.add("functions", functionsObject);
        newObject.add("styles", stylesObject);
        newObject.add("dragging", draggingObject);
        newObject.addProperty("lastUpdated", Instant.now().getEpochSecond());

        this.lastUpdated = Instant.now().getEpochSecond();
        this.modified = false; // Reset modified flag after saving
        return newObject;
    }

    private void saveDraggingSettings(JsonObject draggingObject) {
        for (Map.Entry<String, Dragging> entry : DragManager.draggables.entrySet()) {
            String elementName = entry.getKey();
            Dragging dragging = entry.getValue();
            if (elementName.equalsIgnoreCase("watermark")) {
                continue;
            }
            JsonObject positionObject = new JsonObject();
            positionObject.addProperty("x", dragging.getX());
            positionObject.addProperty("y", dragging.getY());
            positionObject.addProperty("width", dragging.getWidth());
            positionObject.addProperty("height", dragging.getHeight());

            draggingObject.add(elementName, positionObject);
        }
    }

    private void saveFunctionSettings(JsonObject functionsObject) {
        Initclass.getInstance().getFunctionRegistry().getFunctions().forEach(module -> {
            JsonObject moduleObject = new JsonObject();

            moduleObject.addProperty("bind", module.getBind());
            moduleObject.addProperty("state", module.isState());

            module.getSettings().forEach(setting -> saveIndividualSetting(moduleObject, setting));

            functionsObject.add(module.getName().toLowerCase(), moduleObject);
        });
    }

    private void saveIndividualSetting(JsonObject moduleObject, Setting<?> setting) {
        if (setting instanceof BooleanSetting) {
            moduleObject.addProperty(setting.getName(), ((BooleanSetting) setting).get());
        } else if (setting instanceof SliderSetting) {
            moduleObject.addProperty(setting.getName(), ((SliderSetting) setting).get());
        } else if (setting instanceof ModeSetting) {
            moduleObject.addProperty(setting.getName(), ((ModeSetting) setting).get());
        } else if (setting instanceof ColorSetting) {
            moduleObject.addProperty(setting.getName(), ((ColorSetting) setting).get());
        } else if (setting instanceof BindSetting) {
            moduleObject.addProperty(setting.getName(), ((BindSetting) setting).get());
        } else if (setting instanceof StringSetting) {
            moduleObject.addProperty(setting.getName(), ((StringSetting) setting).get());
        } else if (setting instanceof ModeListSetting) {
            saveModeListSetting(moduleObject, (ModeListSetting) setting);
        }
    }

    private void saveModeListSetting(JsonObject moduleObject, ModeListSetting setting) {
        JsonObject elements = new JsonObject();
        setting.get().forEach(option -> elements.addProperty(option.getName(), option.get()));
        moduleObject.add(setting.getName(), elements);
    }

    private void saveStyleSettings(JsonObject stylesObject) {
        for (Style style : Initclass.getInstance().getStyleManager().getStyleList()) {
            JsonObject styleObject = new JsonObject();
            styleObject.addProperty("selected", Initclass.getInstance().getStyleManager().getCurrentStyle() == style);
            stylesObject.add(style.getStyleName(), styleObject);
        }
    }
}