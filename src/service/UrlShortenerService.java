package service;

import config.AppConfig;
import model.ShortUrl;import repository.UrlRepository;import java.awt.Desktop;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

public class UrlShortenerService {
    private final UrlRepository urlRepository;
    private final UserService userService;
    private final Random random;
    private final AppConfig config;

    public List<ShortUrl> getUserUrls(UUID userId) {
        return urlRepository.findByUserId(userId);
    }

    public UrlShortenerService(UserService userService, AppConfig config) {
        this.config = config;
        this.urlRepository = new UrlRepository();
        this.userService = userService;
        this.random = new Random();
    }

    public String createShortUrl(String longUrl, UUID userId, int clickLimit) {
        if (!userService.userExists(userId)) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        String shortCode;
        do {
            shortCode = generateShortCode();
        } while (urlRepository.findByCode(shortCode).isPresent());


        int lifetimeHours = config.getLinkLifetimeHours();

        ShortUrl shortUrl = new ShortUrl(shortCode, longUrl, userId,
                clickLimit, lifetimeHours);
        urlRepository.save(shortUrl);

        return config.getBaseUrl() + shortCode;
    }

    public boolean navigateToUrl(String shortUrlString) {
        String shortCode = extractShortCode(shortUrlString);

        Optional<ShortUrl> urlOpt = urlRepository.findByCode(shortCode);
        if (urlOpt.isEmpty()) {
            System.out.println();
            System.out.println("Ошибка: Ссылка не найдена");
            return false;
        }

        ShortUrl shortUrl = urlOpt.get();

        if (shortUrl.isExpired()) {
            System.out.println();
            System.out.println("Ошибка: Срок действия ссылки истек");
            System.out.println("Данная ссылка была аннулирована.");
            urlRepository.deleteByCode(shortCode);
            return false;
        }

        if (!shortUrl.incrementClickCount()) {
            System.out.println();
            System.out.println("Ошибка: Лимит переходов исчерпан");
            return false;
        }

        try {
            Desktop.getDesktop().browse(new URI(shortUrl.getLongUrl()));
            System.out.println("Перенаправление на: " + shortUrl.getLongUrl());
            return true;
        } catch (Exception e) {
            System.out.println("Ошибка при открытии браузера: " + e.getMessage());
            return false;
        }
    }

    public boolean updateClickLimit(String shortCode, UUID userId, int newLimit) {
        Optional<ShortUrl> urlOpt = urlRepository.findByCode(shortCode);
        if (urlOpt.isEmpty()) {
            return false;
        }

        ShortUrl url = urlOpt.get();

        url.setClickLimit(newLimit);
        return true;
    }

    public boolean deleteUrl(String shortCode, UUID userId) {
        Optional<ShortUrl> urlOpt = urlRepository.findByCode(shortCode);
        if (urlOpt.isEmpty()) {
            return false;
        }

        ShortUrl url = urlOpt.get();

        if (!url.getUserId().equals(userId)) {
            System.out.println("Ошибка: Нет прав на удаление этой ссылки");
            return false;
        }

        urlRepository.deleteByCode(shortCode);
        return true;
    }

    public boolean verifyUser(String shortCode, UUID userId) {
        Optional<ShortUrl> urlOpt = urlRepository.findByCode(shortCode);
        if (urlOpt.isEmpty()) {
            return false;
        }

        ShortUrl url = urlOpt.get();

        return url.getUserId().equals(userId);
    }

    private String generateShortCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        int codeLength = config.getShortCodeLength();
        StringBuilder sb = new StringBuilder(codeLength);
        for (int i = 0; i < codeLength; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String extractShortCode(String shortUrl) {
        return shortUrl.replace(config.getBaseUrl(), "");
    }
}