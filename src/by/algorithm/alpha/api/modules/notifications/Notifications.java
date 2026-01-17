package by.algorithm.alpha.api.modules.notifications;

public class Notifications {

    // Инициализируем NotificationManager
    public static NotificationManager NOTIFICATION_MANAGER = new NotificationManager();

    public static void add(String text, String content, int time) {
        //System.out.println("Добавляем уведомление: " + text); // Дебаг который нихуя не нужин
        if (NOTIFICATION_MANAGER != null) {
            NOTIFICATION_MANAGER.add(text, content, time);
        } else {
            System.out.println("NOTIFICATION_MANAGER is null!");
        }
    }

    public static void add(String text, int time) {
        add(text, "", time);
    }

    public static void add(String text) {
        add(text, "", 3);
    }

    // Методы для различных типов уведомлений
    public static void addError(String text) {
        add("Ошибка: " + text, "", 4);
    }

    public static void addSuccess(String text) {
        add("Успешно: " + text, "", 3);
    }

    public static void addWarning(String text) {
        add("Предупреждение: " + text, "", 3);
    }

    public static void addInfo(String text) {
        add("Информация: " + text, "", 2);
    }
}