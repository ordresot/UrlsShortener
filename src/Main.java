import config.AppConfig;
import model.ShortUrl;
import model.User;
import service.UrlShortenerService;
import service.UserService;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static UserService userService;
    private static UrlShortenerService urlService;
    private static UUID currentUserId = null;
    private static AppConfig config;

    public static void main(String[] args) {
        config = new AppConfig();
        userService = new UserService();
        urlService = new UrlShortenerService(userService, config);

        System.out.println("=== Сервис сокращения ссылок ===");
        System.out.println("Добро пожаловать!");

        handleUserIdentification();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    createShortUrl();
                    break;
                case "2":
                    navigateToUrl();
                    break;
                case "3":
                    viewMyUrls();
                    break;
                case "4":
                    updateClickLimit();
                    break;
                case "5":
                    deleteUrl();
                    break;
                case "6":
                    changeUser();
                    break;
                default:
                    System.out.println("Неверный выбор. Попробуйте снова.");
            }
        }

        scanner.close();
    }

    private static void handleUserIdentification() {
        System.out.println("\n--- Идентификация пользователя ---");
        System.out.println("1. У меня уже есть UUID");
        System.out.println("2. Я новый пользователь");
        System.out.print("Выберите вариант: ");

        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            System.out.print("Введите ваш UUID: ");
            String uuidStr = scanner.nextLine();
            try {
                UUID uuid = UUID.fromString(uuidStr);
                if (userService.userExists(uuid)) {
                    currentUserId = uuid;
                    System.out.println("Добро пожаловать обратно! Ваш UUID: " + currentUserId);
                } else {
                    System.out.println("Данный UUID не найден в системе.");
                    handleUserIdentification();
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Неверный формат UUID. Попробуйте снова.");
                handleUserIdentification(); // Повторяем процесс
            }
        } else {
            System.out.println("UUID будет сгенерирован при создании первой ссылки.");
            System.out.println("Вы можете просматривать существующие ссылки или создать свою.");
            currentUserId = null;
        }
    }

    private static void printMenu() {
        System.out.println("\n=== Меню ===");
        System.out.println("1. Создать короткую ссылку");
        System.out.println("2. Перейти по короткой ссылке");
        System.out.println("3. Мои ссылки");
        System.out.println("4. Изменить лимит переходов");
        System.out.println("5. Удалить ссылку");
        System.out.println("6. Сменить пользователя");
        System.out.println("0. Выход");

        if (currentUserId != null) {
            System.out.println("Текущий пользователь: " + currentUserId);
        } else {
            System.out.println("Текущий пользователь: не авторизован (UUID не сгенерирован)");
        }
        System.out.print("Выберите действие: ");
    }

    private static void createShortUrl() {
        System.out.print("Введите URL: ");
        String longUrl = scanner.nextLine();

        System.out.print("Введите лимит переходов: ");
        int clickLimit;
        try {
            clickLimit = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: Введите корректное число");
            return;
        }

        if (currentUserId == null) {
            System.out.println();
            User user = userService.createUser();
            System.out.println("Генерируем ваш UUID...");
            currentUserId = user.getId();
            System.out.println("Ваш новый UUID: " + currentUserId);
            System.out.println("СОХРАНИТЕ ЭТОТ UUID для доступа к вашим ссылкам в будущем!");
            System.out.println();
        }

        try {
            String shortUrl = urlService.createShortUrl(longUrl, currentUserId, clickLimit);
            System.out.println("Короткая ссылка создана: " + shortUrl);
            System.out.println("Срок действия:" + config.getLinkLifetimeHours() + "часа");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private static void navigateToUrl() {
        System.out.print("Введите короткую ссылку: ");
        String shortUrl = scanner.nextLine();

        urlService.navigateToUrl(shortUrl);
    }

    private static void viewMyUrls() {
        if (currentUserId == null) {
            System.out.println("Вы не авторизованы. Чтобы просмотреть свои ссылки, нужно создать первую ссылку или войти с UUID.");
            return;
        }

        List<ShortUrl> urls = urlService.getUserUrls(currentUserId);

        if (urls.isEmpty()) {
            System.out.println("У вас пока нет ссылок");
            return;
        }

        System.out.println("\n=== Ваши ссылки (UUID: " + currentUserId + ") ===");
        System.out.println("Всего ссылок: " + urls.size());
        System.out.println();

        for (int i = 0; i < urls.size(); i++) {
            ShortUrl url = urls.get(i);
            System.out.println((i + 1) + ". Код: " + url.getShortCode());
            System.out.println("   URL: " + truncate(url.getLongUrl(), 50));
            System.out.println("   Переходов: " + url.getClickCount() + "/" + url.getClickLimit());
            System.out.println("   Создана: " + url.getCreatedAt());
            System.out.println("   Истекает: " + url.getExpiresAt());
            System.out.println("   Статус: " + (url.isActive() && !url.isExpired() ? "Активна" : "Заблокирована/Истекла"));
            System.out.println();
        }
    }

    private static String truncate(String str, int length) {
        if (str.length() <= length) return str;
        return str.substring(0, length) + "...";
    }

    private static void updateClickLimit() {
        if (currentUserId == null) {
            System.out.println("Необходимо авторизоваться для редактирования ссылок.");
            return;
        }

        System.out.print("Введите код ссылки (символы после clck.ru/) для удаления: ");
        String shortCode = scanner.nextLine();

        if (urlService.verifyUser(shortCode, currentUserId)) {
            System.out.print("Введите новый лимит переходов: ");
            int newLimit;
            try {
                newLimit = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Введите корректное число");
                return;
            }

            if (urlService.updateClickLimit(shortCode, currentUserId, newLimit)) {
                System.out.println("Лимит переходов успешно обновлен");
            } else {
                System.out.println("Ошибка: Ссылка не найдена.");
            }
        }
        else {
            System.out.println("Ошибка: У вас нет прав на удаление.");
        }
    }

    private static void deleteUrl() {
        if (currentUserId == null) {
            System.out.println("Необходимо авторизоваться для удаления ссылок.");
            return;
        }

        System.out.print("Введите код ссылки (символы после clck.ru/) для удаления: ");
        String shortCode = scanner.nextLine();

        if (urlService.deleteUrl(shortCode, currentUserId)) {
            System.out.println("Ссылка успешно удалена");
        } else {
            System.out.println("Ошибка: Ссылка не найдена или нет прав на удаление");
        }
    }

    private static void changeUser() {
        System.out.println("Смена пользователя...");
        handleUserIdentification();
    }
}