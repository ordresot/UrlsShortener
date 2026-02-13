package model;

import java.time.LocalDateTime;
import java.util.UUID;

public class ShortUrl {
    private final String shortCode;
    private final String longUrl;
    private final UUID userId;
    private int clickLimit;
    private int clickCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private boolean active;

    public ShortUrl(String shortCode, String longUrl, UUID userId, int clickLimit, int lifetimeHours) {
        this.shortCode = shortCode;
        this.longUrl = longUrl;
        this.userId = userId;
        this.clickLimit = clickLimit;
        this.clickCount = 0;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = createdAt.plusHours(lifetimeHours);
        this.active = true;
    }

    public String getShortCode() { return shortCode; }
    public String getLongUrl() { return longUrl; }
    public UUID getUserId() { return userId; }
    public int getClickLimit() { return clickLimit; }
    public int getClickCount() { return clickCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public boolean isActive() { return active; }

    public void setClickLimit(int clickLimit) {
        if (clickLimit > this.clickLimit) {
            this.active = true;
        }
        else if (clickLimit < this.clickLimit) {
            this.clickCount = clickLimit;
        }
        this.clickLimit = clickLimit;
    }
    public void setActive(boolean active) { this.active = active; }

    public boolean incrementClickCount() {
        if (clickCount >= clickLimit) {
            active = false;
            return false;
        }
        else {
            clickCount++;
            return true;
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}