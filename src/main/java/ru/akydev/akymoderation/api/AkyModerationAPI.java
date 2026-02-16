package ru.akydev.akymoderation.api;

import org.bukkit.entity.Player;
import ru.akydev.akymoderation.punishment.Punishment;
import ru.akydev.akymoderation.punishment.PunishmentType;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class AkyModerationAPI {
    
    private static AkyModerationAPI instance;
    
    public static AkyModerationAPI getInstance() {
        return instance;
    }
    
    public static void setInstance(AkyModerationAPI instance) {
        AkyModerationAPI.instance = instance;
    }
    
    public CompletableFuture<Boolean> banPlayer(Player target, Player moderator, String reason, String duration) {
        return banPlayer(target, moderator, reason, duration, false);
    }
    
    public CompletableFuture<Boolean> banPlayer(Player target, Player moderator, String reason, String duration, boolean silent) {
        return issuePunishment(PunishmentType.BAN, target, moderator, reason, duration, silent);
    }
    
    public CompletableFuture<Boolean> mutePlayer(Player target, Player moderator, String reason, String duration) {
        return mutePlayer(target, moderator, reason, duration, false);
    }
    
    public CompletableFuture<Boolean> mutePlayer(Player target, Player moderator, String reason, String duration, boolean silent) {
        return issuePunishment(PunishmentType.MUTE, target, moderator, reason, duration, silent);
    }
    
    public CompletableFuture<Boolean> kickPlayer(Player target, Player moderator, String reason) {
        return kickPlayer(target, moderator, reason, false);
    }
    
    public CompletableFuture<Boolean> kickPlayer(Player target, Player moderator, String reason, boolean silent) {
        return issuePunishment(PunishmentType.KICK, target, moderator, reason, "0", silent);
    }
    
    public CompletableFuture<Boolean> warnPlayer(Player target, Player moderator, String reason) {
        return warnPlayer(target, moderator, reason, false);
    }
    
    public CompletableFuture<Boolean> warnPlayer(Player target, Player moderator, String reason, boolean silent) {
        return issuePunishment(PunishmentType.WARN, target, moderator, reason, "0", silent);
    }
    
    public CompletableFuture<Boolean> unbanPlayer(UUID playerUuid) {
        return removePunishment(playerUuid, PunishmentType.BAN);
    }
    
    public CompletableFuture<Boolean> unmutePlayer(UUID playerUuid) {
        return removePunishment(playerUuid, PunishmentType.MUTE);
    }
    
    public CompletableFuture<List<Punishment>> getPlayerPunishments(UUID playerUuid) {
        return getPlayerPunishmentsFromManager(playerUuid);
    }
    
    public CompletableFuture<List<Punishment>> getActivePunishments(UUID playerUuid) {
        return getActivePunishmentsFromManager(playerUuid);
    }
    
    public CompletableFuture<Integer> getPunishmentCount(UUID playerUuid, PunishmentType type) {
        return getPunishmentCountFromManager(playerUuid, type);
    }
    
    public boolean isPlayerBanned(UUID playerUuid) {
        try {
            return getActivePunishmentsFromManager(playerUuid).join().stream()
                .anyMatch(p -> p.getType() == PunishmentType.BAN && p.isActive());
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isPlayerMuted(UUID playerUuid) {
        try {
            return getActivePunishmentsFromManager(playerUuid).join().stream()
                .anyMatch(p -> p.getType() == PunishmentType.MUTE && p.isActive());
        } catch (Exception e) {
            return false;
        }
    }
    
    public CompletableFuture<Boolean> clearPlayerHistory(UUID playerUuid) {
        return clearPlayerHistoryFromManager(playerUuid);
    }
    
    protected abstract CompletableFuture<Boolean> issuePunishment(PunishmentType type, Player target, Player moderator, String reason, String duration, boolean silent);
    protected abstract CompletableFuture<Boolean> removePunishment(UUID playerUuid, PunishmentType type);
    protected abstract CompletableFuture<List<Punishment>> getPlayerPunishmentsFromManager(UUID playerUuid);
    protected abstract CompletableFuture<List<Punishment>> getActivePunishmentsFromManager(UUID playerUuid);
    protected abstract CompletableFuture<Integer> getPunishmentCountFromManager(UUID playerUuid, PunishmentType type);
    protected abstract CompletableFuture<Boolean> clearPlayerHistoryFromManager(UUID playerUuid);
}
