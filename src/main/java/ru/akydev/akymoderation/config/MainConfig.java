package ru.akydev.akymoderation.config;

import org.bukkit.configuration.file.FileConfiguration;
import ru.akydev.akymoderation.AkyModeration;

public class MainConfig {
    
    private final AkyModeration plugin;
    private final FileConfiguration config;
    
    public MainConfig(AkyModeration plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        plugin.saveDefaultConfig();
    }
    
    public String getDefaultBanDuration() {
        return config.getString("punishments.ban.default_duration", "7d");
    }
    
    public String getDefaultMuteDuration() {
        return config.getString("punishments.mute.default_duration", "1h");
    }
    
    public int getAutoBanAfter() {
        return config.getInt("punishments.warn.auto_ban_after", 5);
    }
    
    public int getAutoMuteAfter() {
        return config.getInt("punishments.warn.auto_mute_after", 3);
    }
    
    public String getWarnBanDuration() {
        return config.getString("punishments.warn.ban_duration", "1d");
    }
    
    public String getWarnMuteDuration() {
        return config.getString("punishments.warn.mute_duration", "30m");
    }
    
    public boolean isIpBanEnabled() {
        return config.getBoolean("punishments.ban.ip_ban", false);
    }
    
    public boolean isBanAltsEnabled() {
        return config.getBoolean("punishments.ban.ban_alts", false);
    }
    
    public int getMaxAccountsPerIp() {
        return config.getInt("punishments.ban.max_accounts_per_ip", 3);
    }
    
    public boolean isSilentModeEnabled() {
        return config.getBoolean("punishments.silent_mode", false);
    }
    
    public String getNotInWorkMessage() {
        return config.getString("messages.not_in_work", "&cПлагин в данный момент не работает!");
    }
    
    public boolean isWarnSystemEnabled() {
        return config.getBoolean("punishments.warn.enabled", true);
    }
    
    public String getPrefix() {
        return config.getString("messages.prefix", "&6[AkyModeration] ");
    }
    
    public boolean isDebug() {
        return config.getBoolean("general.debug", false);
    }
    
    public int getMaxHistorySize() {
        return config.getInt("general.max_history_size", 1000);
    }
    
    public String getDatabaseType() {
        return config.getString("database.type", "sqlite");
    }
    
    public String getDatabaseHost() {
        return config.getString("database.mysql.host", "localhost");
    }
    
    public int getDatabasePort() {
        return config.getInt("database.mysql.port", 3306);
    }
    
    public String getDatabaseName() {
        return config.getString("database.mysql.database", "akymoderation");
    }
    
    public String getDatabaseUsername() {
        return config.getString("database.mysql.username", "root");
    }
    
    public String getDatabasePassword() {
        return config.getString("database.mysql.password", "");
    }
}
