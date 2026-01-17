package by.algorithm.alpha.system.visuals.mainmenu.altmanager;

import by.algorithm.alpha.Initclass;
import by.algorithm.alpha.system.utils.client.IMinecraft;
import com.google.gson.*;

import net.minecraft.util.Session;

import java.io.*;
import java.util.UUID;

public class AltConfig implements IMinecraft {

    static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static final File file = new File(mc.gameDir, "expensive\\files\\accounts.json");

    public void init() throws Exception {
        // Создаем директорию если она не существует
        File directory = file.getParentFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }

        if (!file.exists()) {
            file.createNewFile();
            // Создаем пустой JSON объект если файл новый
            try (PrintWriter printWriter = new PrintWriter(file)) {
                printWriter.println("{}");
            }
        } else {
            readAlts();
        }
    }

    public static void updateFile() {
        try {
            // Создаем директорию если она не существует
            File directory = file.getParentFile();
            if (!directory.exists()) {
                directory.mkdirs();
            }

            JsonObject jsonObject = new JsonObject();

            // Сохраняем последний использованный аккаунт
            jsonObject.addProperty("last", mc.session.getUsername());

            // Сохраняем список всех аккаунтов
            JsonArray altsArray = new JsonArray();
            for (Alt alt : Initclass.getInstance().getAltScreen().alts) {
                altsArray.add(alt.name);
            }

            jsonObject.add("alts", altsArray);

            // Записываем в файл
            try (PrintWriter printWriter = new PrintWriter(file, "UTF-8")) {
                printWriter.println(gson.toJson(jsonObject));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readAlts() {
        try {
            if (file.length() == 0) {
                return; // Файл пустой
            }

            JsonElement jsonElement = JsonParser.parseReader(new BufferedReader(new FileReader(file, java.nio.charset.StandardCharsets.UTF_8)));

            if (jsonElement.isJsonNull() || !jsonElement.isJsonObject()) {
                return;
            }

            JsonObject jsonObject = jsonElement.getAsJsonObject();

            // Загружаем список аккаунтов
            if (jsonObject.has("alts")) {
                for (JsonElement element : jsonObject.get("alts").getAsJsonArray()) {
                    String name = element.getAsString();
                    Initclass.getInstance().getAltScreen().alts.add(new Alt(name));
                }
            }

            // Устанавливаем последний использованный аккаунт
            if (jsonObject.has("last")) {
                String lastAccount = jsonObject.get("last").getAsString();
                mc.session = new Session(lastAccount, UUID.randomUUID().toString(), "", "mojang");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод для добавления нового аккаунта и автоматического обновления файла
     */
    public static void addAlt(String accountName) {
        Alt newAlt = new Alt(accountName);
        Initclass.getInstance().getAltScreen().alts.add(newAlt);

        // Устанавливаем новый аккаунт как текущий
        mc.session = new Session(accountName, UUID.randomUUID().toString(), "", "mojang");

        // Обновляем файл
        updateFile();
    }

    /**
     * Метод для переключения на аккаунт и автоматического обновления файла
     */
    public static void switchToAlt(String accountName) {
        mc.session = new Session(accountName, UUID.randomUUID().toString(), "", "mojang");

        // Обновляем файл чтобы сохранить последний выбранный аккаунт
        updateFile();
    }

    /**
     * Метод для удаления аккаунта и автоматического обновления файла
     */
    public static void removeAlt(Alt alt) {
        Initclass.getInstance().getAltScreen().alts.remove(alt);

        // Обновляем файл
        updateFile();
    }
}