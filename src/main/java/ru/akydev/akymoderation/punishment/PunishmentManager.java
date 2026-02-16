package ru.akydev.akymoderation.punishment;

import org.bukkit.entity.Player;
import ru.akydev.akymoderation.AkyModeration;
import ru.akydev.akymoderation.database.DatabaseManager;
import ru.akydev.akymoderation.utils.MessageUtils;
import ru.akydev.akymoderation.utils.TimeUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PunishmentManager {
    
    private final AkyModeration plugin;
    private final DatabaseManager databaseManager;
    private final MessageUtils messageUtils;
    private final ConcurrentHashMap<UUID, List<Punishment>> activePunishmentsCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> punishmentCountCache = new ConcurrentHashMap<>();
    
    public PunishmentManager(AkyModeration plugin, DatabaseManager databaseManager, MessageUtils messageUtils) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.messageUtils = messageUtils;
    }
    
    public CompletableFuture<Boolean> issuePunishment(PunishmentType type, Player target, 
                                                      Player moderator, String reason, 
                                                      String duration, boolean silent) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID punishmentId = UUID.randomUUID();
                LocalDateTime expiresAt = calculateExpiration(duration);
                
                Punishment punishment = new Punishment(
                    punishmentId,
                    target.getUniqueId(),
                    moderator.getUniqueId(),
                    type,
                    reason,
                    duration,
                    LocalDateTime.now(),
                    expiresAt,
                    true,
                    target.getName()
                );
                
                boolean success = databaseManager.addPunishment(punishment).join();
                
                if (success) {
                    updateCache(target.getUniqueId(), type, true);
                    applyPunishment(type, target, moderator, reason, duration, silent);
                    logPunishment(moderator, target, type, reason, duration);
                }
                
                return success;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to issue punishment: " + e.getMessage());
                return false;
            }
        });
    }
    
    public CompletableFuture<Boolean> issuePunishmentOffline(PunishmentType type, String playerName, 
                                                           UUID playerUuid, Player moderator, 
                                                           String reason, String duration, boolean silent) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UUID punishmentId = UUID.randomUUID();
                LocalDateTime expiresAt = calculateExpiration(duration);
                
                Punishment punishment = new Punishment(
                    punishmentId,
                    playerUuid,
                    moderator.getUniqueId(),
                    type,
                    reason,
                    duration,
                    LocalDateTime.now(),
                    expiresAt,
                    true,
                    playerName
                );
                
                boolean success = databaseManager.addPunishment(punishment).join();
                
                if (success) {
                    updateCache(playerUuid, type, true);
                    logPunishment(moderator, playerName, type, reason, duration);
                    
                    if (!silent) {
                        broadcastPunishment(type, playerName, moderator.getName(), reason);
                    }
                }
                
                return success;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to issue offline punishment: " + e.getMessage());
                return false;
            }
        });
    }
    
    public CompletableFuture<List<Punishment>> getPlayerPunishments(UUID playerUuid) {
        return databaseManager.getPlayerPunishments(playerUuid);
    }
    
    public CompletableFuture<List<Punishment>> getActivePunishments(UUID playerUuid) {
        java.util.List<Punishment> cached = activePunishmentsCache.get(playerUuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        
        return databaseManager.getActivePunishments(playerUuid)
            .thenApply(punishments -> {
                activePunishmentsCache.put(playerUuid, punishments);
                return punishments;
            });
    }
    
    public CompletableFuture<Integer> getPunishmentCount(UUID playerUuid, PunishmentType type) {
        String cacheKey = playerUuid.toString() + ":" + type.name();
        Integer cached = punishmentCountCache.get(cacheKey);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        
        return databaseManager.getPunishmentCount(playerUuid, type)
            .thenApply(count -> {
                punishmentCountCache.put(cacheKey, count);
                return count;
            });
    }
    
    public CompletableFuture<Boolean> clearPlayerHistory(UUID playerUuid) {
        return databaseManager.clearPlayerHistory(playerUuid);
    }
    
    public CompletableFuture<Boolean> clearPunishmentType(UUID playerUuid, String typeName) {
        try {
            PunishmentType type = PunishmentType.valueOf(typeName.toUpperCase());
            return databaseManager.clearPunishmentType(playerUuid, type.name());
        } catch (IllegalArgumentException e) {
            return CompletableFuture.completedFuture(false);
        }
    }
    
    public CompletableFuture<Boolean> removePunishment(UUID playerUuid, PunishmentType type) {
        return databaseManager.removeActivePunishment(playerUuid, type);
    }
    
    private LocalDateTime calculateExpiration(String duration) {
        if (duration.equals("0") || duration.equalsIgnoreCase("perm") || duration.equalsIgnoreCase("permanent")) {
            return null;
        }
        
        long millis = TimeUtils.parseTime(duration);
        if (millis <= 0) {
            return null;
        }
        
        return LocalDateTime.now().plusSeconds(millis / 1000);
    }
    
    private void applyPunishment(PunishmentType type, Player target, Player moderator, 
                                String reason, String duration, boolean silent) {
        switch (type) {
            case BAN:
                target.kickPlayer(formatBanMessage(reason, duration));
                break;
            case MUTE:
                if (!silent) {
                    broadcastPunishment(type, target.getName(), moderator.getName(), reason);
                }
                break;
            case KICK:
                target.kickPlayer(formatKickMessage(reason));
                break;
            case WARN:
                if (!silent) {
                    broadcastPunishment(type, target.getName(), moderator.getName(), reason);
                }
                break;
        }
    }
    
    private void broadcastPunishment(PunishmentType type, String targetName, String moderatorName, String reason) {
        String message = String.format("&c%s &7был %s &7игроком &e%s &7по причине: &f%s", 
            targetName, type.getDisplayName().toLowerCase(), moderatorName, reason);
        
        plugin.getServer().broadcastMessage(plugin.getMessageUtils().colorize(message));
        
        if (plugin.getConfigManager().getMainConfig().isDebug()) {
            plugin.getLogger().info("Broadcast: " + message);
        }
    }
    
    private void logPunishment(Player moderator, Player target, PunishmentType type, String reason, String duration) {
        String logMessage = String.format("[PUNISHMENT] %s issued %s against %s for: %s (%s)", 
            moderator.getName(), type.name(), target.getName(), reason, duration);
        plugin.getLogger().info(logMessage);
    }
    
    private void logPunishment(Player moderator, String targetName, PunishmentType type, String reason, String duration) {
        String logMessage = String.format("[PUNISHMENT] %s issued %s against %s for: %s (%s)", 
            moderator.getName(), type.name(), targetName, reason, duration);
        plugin.getLogger().info(logMessage);
    }
    
    private String formatBanMessage(String reason, String duration) {
        return plugin.getMessageUtils().colorize(String.format(
            "&cВы были забанены!\n\n" +
            "&7Причина: &f%s\n" +
            "&7Длительность: &f%s\n" +
            "&7Подайте апелляцию на сайте: &fhttps://github.com/JavaSoska777",
            reason, duration.equals("0") ? "Навсегда" : duration
        ));
    }
    
    private String formatKickMessage(String reason) {
        return plugin.getMessageUtils().colorize(String.format(
            "&cВы были кикнуты!\n\n" +
            "&7Причина: &f%s",
            reason
        ));
    }
    
    private void updateCache(UUID playerUuid, PunishmentType type, boolean active) {
        List<Punishment> current = activePunishmentsCache.getOrDefault(playerUuid, List.of());
        
        if (active) {
            activePunishmentsCache.put(playerUuid, current);
        }
        
        String cacheKey = playerUuid.toString() + ":" + type.name();
        int currentCount = punishmentCountCache.getOrDefault(cacheKey, 0);
        punishmentCountCache.put(cacheKey, active ? currentCount + 1 : Math.max(0, currentCount - 1));
    }
    
    public void clearCache() {
        activePunishmentsCache.clear();
        punishmentCountCache.clear();
    }
}
