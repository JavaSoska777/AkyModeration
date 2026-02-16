package ru.akydev.akymoderation.config;

import ru.akydev.akymoderation.AkyModeration;

public class ConfigManager {
    
    private final AkyModeration plugin;
    private MainConfig mainConfig;
    private MessagesConfig messagesConfig;
    
    public ConfigManager(AkyModeration plugin) {
        this.plugin = plugin;
        this.mainConfig = new MainConfig(plugin);
        this.messagesConfig = new MessagesConfig(plugin);
    }
    
    public MainConfig getMainConfig() {
        return mainConfig;
    }
    
    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }
    
    public void reload() {
        plugin.reloadConfig();
        this.mainConfig = new MainConfig(plugin);
        this.messagesConfig.reload();
    }
}
