package ru.akydev.akymoderation.punishment;

import java.time.LocalDateTime;
import java.util.UUID;

public class Punishment {
    
    private final UUID id;
    private final UUID playerUuid;
    private final UUID moderatorUuid;
    private final PunishmentType type;
    private final String reason;
    private final String duration;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private boolean active;
    private final String playerName;
    
    public Punishment(UUID id, UUID playerUuid, UUID moderatorUuid, PunishmentType type, 
                     String reason, String duration, LocalDateTime createdAt, 
                     LocalDateTime expiresAt, boolean active, String playerName) {
        this.id = id;
        this.playerUuid = playerUuid;
        this.moderatorUuid = moderatorUuid;
        this.type = type;
        this.reason = reason;
        this.duration = duration;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.active = active;
        this.playerName = playerName;
    }
    
    public UUID getId() {
        return id;
    }
    
    public UUID getPlayerUuid() {
        return playerUuid;
    }
    
    public UUID getModeratorUuid() {
        return moderatorUuid;
    }
    
    public PunishmentType getType() {
        return type;
    }
    
    public String getReason() {
        return reason;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
