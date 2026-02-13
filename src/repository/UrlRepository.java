package repository;

import model.ShortUrl;import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UrlRepository {
    private final Map<String, ShortUrl> urlsByCode = new ConcurrentHashMap<>();
    private final Map<UUID, List<ShortUrl>> urlsByUser = new ConcurrentHashMap<>();

    public void save(ShortUrl shortUrl) {
        urlsByCode.put(shortUrl.getShortCode(), shortUrl);
        urlsByUser.computeIfAbsent(shortUrl.getUserId(), k -> new ArrayList<>())
                .add(shortUrl);
    }

    public Optional<ShortUrl> findByCode(String shortCode) {
        return Optional.ofNullable(urlsByCode.get(shortCode));
    }

    public List<ShortUrl> findByUserId(UUID userId) {
        return urlsByUser.getOrDefault(userId, new ArrayList<>());
    }

    public void deleteByCode(String shortCode) {
        ShortUrl url = urlsByCode.remove(shortCode);
        if (url != null) {
            List<ShortUrl> userUrls = urlsByUser.get(url.getUserId());
            if (userUrls != null) {
                userUrls.remove(url);
            }
        }
    }

    public List<ShortUrl> findAll() {
        return new ArrayList<>(urlsByCode.values());
    }
}