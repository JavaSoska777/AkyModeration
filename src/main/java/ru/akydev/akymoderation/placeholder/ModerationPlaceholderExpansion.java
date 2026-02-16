package ru.akydev.akymoderation.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import ru.akydev.akymoderation.AkyModeration;
import ru.akydev.akymoderation.punishment.Punishment;
import ru.akydev.akymoderation.punishment.PunishmentManager;
import ru.akydev.akymoderation.punishment.PunishmentType;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ModerationPlaceholderExpansion extends PlaceholderExpansion {
    
    private final AkyModeration plugin;
    private final PunishmentManager punishmentManager;
    
    public ModerationPlaceholderExpansion(AkyModeration plugin) {
        this.plugin = plugin;
        this.punishmentManager = plugin.getPunishmentManager();
    }
    
    @Override
    public String getIdentifier() {
        return "akymoderation";
    }
    
    @Override
    public String getAuthor() {
        return "AkyDev";
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }
        
        UUID playerUuid = player.getUniqueId();
        
        switch (params.toLowerCase()) {
            case "is_banned":
                return isPlayerBanned(playerUuid) ? "Да" : "Нет";
            case "is_muted":
                return isPlayerMuted(playerUuid) ? "Да" : "Нет";
            case "ban_count":
                return String.valueOf(getPunishmentCount(playerUuid, PunishmentType.BAN));
            case "mute_count":
                return String.valueOf(getPunishmentCount(playerUuid, PunishmentType.MUTE));
            case "kick_count":
                return String.valueOf(getPunishmentCount(playerUuid, PunishmentType.KICK));
            case "warn_count":
                return String.valueOf(getPunishmentCount(playerUuid, PunishmentType.WARN));
            case "total_punishments":
                return String.valueOf(getTotalPunishmentCount(playerUuid));
            default:
                return null;
        }
    }
    
    private boolean isPlayerBanned(UUID playerUuid) {
        try {
            return punishmentManager.getActivePunishments(playerUuid).join().stream()
                .anyMatch(p -> p != null && ((ru.akydev.akymoderation.punishment.Punishment) p).getType() == PunishmentType.BAN && ((ru.akydev.akymoderation.punishment.Punishment) p).isActive());
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isPlayerMuted(UUID playerUuid) {
        try {
            return punishmentManager.getActivePunishments(playerUuid).join().stream()
                .anyMatch(p -> p != null && ((ru.akydev.akymoderation.punishment.Punishment) p).getType() == PunishmentType.MUTE && ((ru.akydev.akymoderation.punishment.Punishment) p).isActive());
        } catch (Exception e) {
            return false;
        }
    }
    
    private int getPunishmentCount(UUID playerUuid, PunishmentType type) {
        try {
            return punishmentManager.getPunishmentCount(playerUuid, type).join();
        } catch (Exception e) {
            return 0;
        }
    }
    
    private int getTotalPunishmentCount(UUID playerUuid) {
        try {
            List<Punishment> punishments = punishmentManager.getPlayerPunishments(playerUuid).join();
            return punishments.size();
        } catch (Exception e) {
            return 0;
        }
    }
}
