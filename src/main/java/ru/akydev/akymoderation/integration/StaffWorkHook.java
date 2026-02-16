package ru.akydev.akymoderation.integration;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import ru.akydev.akymoderation.AkyModeration;

public class StaffWorkHook {
    
    private final AkyModeration plugin;
    private boolean enabled = false;
    private Plugin staffWorkPlugin;
    
    public StaffWorkHook(AkyModeration plugin) {
        this.plugin = plugin;
    }
    
    public void initialize() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("StaffWork");
        if (plugin != null && plugin.isEnabled()) {
            this.staffWorkPlugin = plugin;
            this.enabled = true;
            plugin.getLogger().info("StaffWork integration enabled");
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void logPunishment(String moderatorName, String playerName, String punishmentType, String reason) {
        if (!enabled) {
            return;
        }
        
        try {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                    String.format("staffwork log %s наказал %s (%s): %s", 
                        moderatorName, playerName, punishmentType, reason));
            });
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to log punishment to StaffWork: " + e.getMessage());
        }
    }
}
