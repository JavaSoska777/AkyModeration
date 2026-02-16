package ru.akydev.akymoderation.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.akydev.akymoderation.AkyModeration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessagesConfig {
    
    private final AkyModeration plugin;
    private FileConfiguration messages;
    private final Map<String, String> cache = new HashMap<>();
    
    public MessagesConfig(AkyModeration plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    private void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        
        messages = YamlConfiguration.loadConfiguration(messagesFile);
        plugin.getLogger().info("Messages loaded successfully");
    }
    
    public String getMessage(String path) {
        if (cache.containsKey(path)) {
            return cache.get(path);
        }
        
        String message = messages.getString(path);
        if (message == null) {
            plugin.getLogger().warning("Message not found: " + path);
            return "&cСообщение не найдено: " + path;
        }
        
        cache.put(path, message);
        return message;
    }
    
    public String getMessage(String path, String... placeholders) {
        String message = getMessage(path);
        
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
            }
        }
        
        return message;
    }
    
    public String getPrefix() {
        return getMessage("general.prefix");
    }
    
    public String getNoPermission() {
        return getMessage("general.no_permission");
    }
    
    public String getPlayerNotFound() {
        return getMessage("general.player_not_found");
    }
    
    public String getPlayerOffline() {
        return getMessage("general.player_offline");
    }
    
    public String getPlayerExempt() {
        return getMessage("general.player_exempt");
    }
    
    public String getConsoleOnly() {
        return getMessage("general.console_only");
    }
    
    public String getPlayerOnly() {
        return getMessage("general.player_only");
    }
    
    public String getPluginNotWorking() {
        return getMessage("general.plugin_not_working");
    }
    
    public String getConfigReloaded() {
        return getMessage("general.config_reloaded");
    }
    
    public String getBanSuccess(String player) {
        return getMessage("punishments.ban.success", "player", player);
    }
    
    public String getBanSuccessOffline(String player) {
        return getMessage("punishments.ban.success_offline", "player", player);
    }
    
    public String getBanFailed() {
        return getMessage("punishments.ban.failed");
    }
    
    public String getMuteSuccess(String player) {
        return getMessage("punishments.mute.success", "player", player);
    }
    
    public String getMuteSuccessOffline(String player) {
        return getMessage("punishments.mute.success_offline", "player", player);
    }
    
    public String getMuteFailed() {
        return getMessage("punishments.mute.failed");
    }
    
    public String getKickSuccess(String player) {
        return getMessage("punishments.kick.success", "player", player);
    }
    
    public String getKickFailed() {
        return getMessage("punishments.kick.failed");
    }
    
    public String getWarnSuccess(String player) {
        return getMessage("punishments.warn.success", "player", player);
    }
    
    public String getWarnFailed() {
        return getMessage("punishments.warn.failed");
    }
    
    public String getUnbanSuccess(String player) {
        return getMessage("punishments.unban.success", "player", player);
    }
    
    public String getUnbanFailed() {
        return getMessage("punishments.unban.failed");
    }
    
    public String getUnmuteSuccess(String player) {
        return getMessage("punishments.unmute.success", "player", player);
    }
    
    public String getUnmuteFailed() {
        return getMessage("punishments.unmute.failed");
    }
    
    public String getKickMessage(String reason) {
        return getMessage("punishments.kick.kick_message", "reason", reason);
    }
    
    public String getBanMessage(String reason, String duration, String moderator) {
        return getMessage("punishments.ban.kick_message", 
            "reason", reason, 
            "duration", duration, 
            "moderator", moderator);
    }
    
    public String getMuteMessage(String reason) {
        return getMessage("punishments.mute.mute_message", "reason", reason);
    }
    
    public String getChatBlocked() {
        return getMessage("punishments.mute.chat_blocked");
    }
    
    public String getWarnReceived(String moderator, String reason) {
        return getMessage("punishments.warn.warn_received", "moderator", moderator, "reason", reason);
    }
    
    public String getAutoMuteViolation() {
        return getMessage("automod.mute_violation");
    }
    
    public String getAutoSpamViolation() {
        return getMessage("automod.spam_violation");
    }
    
    public String getAutoBanViolation() {
        return getMessage("automod.ban_violation");
    }
    
    public String getAutoBanMessage(String duration) {
        return getMessage("automod.auto_ban_message", "duration", duration);
    }
    
    public String getAutoMute(String player) {
        return getMessage("warnings.auto_mute", "player", player);
    }
    
    public String getAutoBan(String player) {
        return getMessage("warnings.auto_ban", "player", player);
    }
    
    public String getWarnLimitReached() {
        return getMessage("warnings.warn_limit_reached");
    }
    
    public String getCurrentWarns(int count) {
        return getMessage("warnings.current_warns", "count", String.valueOf(count));
    }
    
    public String getHistoryHeader(String player) {
        return getMessage("history.header", "player", player);
    }
    
    public String getNoPunishments(String player) {
        return getMessage("history.no_punishments", "player", player);
    }
    
    public String getHistoryEntry(String type, String reason, String duration, String status) {
        return getMessage("history.entry", 
            "type", type, 
            "reason", reason, 
            "duration", duration, 
            "status", status);
    }
    
    public String getActiveHeader(String player) {
        return getMessage("history.active_header", "player", player);
    }
    
    public String getNoActive(String player) {
        return getMessage("history.no_active", "player", player);
    }
    
    public String getActiveEntry(String type, String reason, String duration) {
        return getMessage("history.active_entry", 
            "type", type, 
            "reason", reason, 
            "duration", duration);
    }
    
    public String getHistoryCleared(String player) {
        return getMessage("history.cleared", "player", player);
    }
    
    public String getTypeCleared(String player, String type) {
        return getMessage("history.type_cleared", "player", player, "type", type);
    }
    
    public String getClearFailed() {
        return getMessage("history.clear_failed");
    }
    
    public String getDatabaseError() {
        return getMessage("errors.database_error");
    }
    
    public String getPermissionError() {
        return getMessage("errors.permission_error");
    }
    
    public String getPlayerError() {
        return getMessage("errors.player_error");
    }
    
    public String getConfigError() {
        return getMessage("errors.config_error");
    }
    
    public String getUnknownError() {
        return getMessage("errors.unknown_error");
    }
    
    public String getHelpHeader() {
        return getMessage("admin.help_header");
    }
    
    public String getHelpCommands() {
        return getMessage("admin.help_commands");
    }
    
    public String getCommandUsage(String usage) {
        return getMessage("misc.command_usage", "usage", usage);
    }
    
    public String getCommandDescription(String description) {
        return getMessage("misc.command_description", "description", description);
    }
    
    public String getCommandPermission(String permission) {
        return getMessage("misc.command_permission", "permission", permission);
    }
    
    public String getMuteMessage() {
        return getMessage("punishments.mute.message");
    }
    
    public void reload() {
        cache.clear();
        loadMessages();
    }
}
