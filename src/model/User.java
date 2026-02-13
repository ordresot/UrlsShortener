package model;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private final UUID id;
    private final LocalDateTime firstSeen;

    public User() {
        this.id = UUID.randomUUID();
        this.firstSeen = LocalDateTime.now();
    }

    public User(UUID id) {
        this.id = id;
        this.firstSeen = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }
}