package ru.akydev.akymoderation;

import org.bukkit.plugin.java.JavaPlugin;
import ru.akydev.akymoderation.automod.AutoModeration;
import ru.akydev.akymoderation.backup.BackupManager;
import ru.akydev.akymoderation.cache.CacheManager;
import ru.akydev.akymoderation.commands.*;
import ru.akydev.akymoderation.config.ConfigManager;
import ru.akydev.akymoderation.database.DatabaseManager;
import ru.akydev.akymoderation.integration.StaffWorkHook;
import ru.akydev.akymoderation.listener.ChatListener;
import ru.akydev.akymoderation.listener.LoginListener;
import ru.akydev.akymoderation.metrics.MetricsManager;
import ru.akydev.akymoderation.placeholder.ModerationPlaceholderExpansion;
import ru.akydev.akymoderation.punishment.PunishmentManager;
import ru.akydev.akymoderation.utils.MessageUtils;

public class AkyModeration extends JavaPlugin {
    
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private PunishmentManager punishmentManager;
    private MessageUtils messageUtils;
    private AutoModeration autoModeration;
    private StaffWorkHook staffWorkHook;
    private CacheManager cacheManager;
    private MetricsManager metricsManager;
    private BackupManager backupManager;
    
    @Override
    public void onEnable() {
        getLogger().info("Starting AkyModeration...");

        getLogger().info("Initializing configuration...");
        configManager = new ConfigManager(this);

        messageUtils = new MessageUtils(configManager.getMessagesConfig());

        getLogger().info("Initializing integrations...");
        staffWorkHook = new StaffWorkHook(this);
        staffWorkHook.initialize();

        getLogger().info("Initializing database...");
        databaseManager = new DatabaseManager(this);

        getLogger().info("Initializing punishment manager...");
        punishmentManager = new PunishmentManager(this, databaseManager, messageUtils);

        getLogger().info("Initializing auto moderation...");
        autoModeration = new AutoModeration(this, punishmentManager, messageUtils);

        getLogger().info("Initializing cache manager...");
        cacheManager = new CacheManager(30, java.util.concurrent.TimeUnit.MINUTES);

        getLogger().info("Initializing metrics manager...");
        metricsManager = new MetricsManager(this);

        getLogger().info("Initializing backup manager...");
        backupManager = new BackupManager(this);

        getLogger().info("Registering commands...");
        registerCommands();

        getLogger().info("Registering listeners...");
        registerListeners();

        getLogger().info("Registering placeholders...");
        registerPlaceholders();

        getLogger().info("Starting metrics task...");
        metricsManager.startMetricsTask();
        
        getLogger().info("AkyModeration enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Disabling AkyModeration...");
        
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        if (cacheManager != null) {
            cacheManager.clear();
        }
        
        if (metricsManager != null) {
            metricsManager.shutdown();
        }
        
        getLogger().info("AkyModeration disabled successfully!");
    }
    
    private void registerCommands() {
        getCommand("mod").setExecutor(new MainCommand(this));
        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("mute").setExecutor(new MuteCommand(this));
        getCommand("kick").setExecutor(new KickCommand(this));
        getCommand("warn").setExecutor(new WarnCommand(this));
        getCommand("unban").setExecutor(new UnbanCommand(this));
        getCommand("unmute").setExecutor(new UnmuteCommand(this));
        getCommand("backup").setExecutor(new BackupCommand(this));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new ChatListener(this, punishmentManager), this);
        getServer().getPluginManager().registerEvents(new LoginListener(this, punishmentManager), this);
        getServer().getPluginManager().registerEvents(autoModeration, this);
    }
    
    private void registerPlaceholders() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ModerationPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI integration enabled!");
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }
    
    public MessageUtils getMessageUtils() {
        return messageUtils;
    }
    
    public AutoModeration getAutoModeration() {
        return autoModeration;
    }
    
    public StaffWorkHook getStaffWorkHook() {
        return staffWorkHook;
    }
    
    public CacheManager getCacheManager() {
        return cacheManager;
    }
    
    public MetricsManager getMetricsManager() {
        return metricsManager;
    }
    
    public BackupManager getBackupManager() {
        return backupManager;
    }
}
