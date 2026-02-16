package ru.akydev.akymoderation.metrics;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import ru.akydev.akymoderation.AkyModeration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MetricsManager {
    
    private final AkyModeration plugin;
    private final ConcurrentHashMap<String, AtomicInteger> counters;
    private BukkitTask metricsTask;
    
    public MetricsManager(AkyModeration plugin) {
        this.plugin = plugin;
        this.counters = new ConcurrentHashMap<>();
        initializeCounters();
    }
    
    private void initializeCounters() {
        counters.put("bans", new AtomicInteger(0));
        counters.put("mutes", new AtomicInteger(0));
        counters.put("kicks", new AtomicInteger(0));
        counters.put("warns", new AtomicInteger(0));
        counters.put("unbans", new AtomicInteger(0));
        counters.put("unmutes", new AtomicInteger(0));
        counters.put("automod_kicks", new AtomicInteger(0));
        counters.put("automod_mutes", new AtomicInteger(0));
        counters.put("total_punishments", new AtomicInteger(0));
        counters.put("active_punishments", new AtomicInteger(0));
    }
    
    public void incrementBan() {
        counters.get("bans").incrementAndGet();
        counters.get("total_punishments").incrementAndGet();
        counters.get("active_punishments").incrementAndGet();
    }
    
    public void incrementMute() {
        counters.get("mutes").incrementAndGet();
        counters.get("total_punishments").incrementAndGet();
        counters.get("active_punishments").incrementAndGet();
    }
    
    public void incrementKick() {
        counters.get("kicks").incrementAndGet();
        counters.get("total_punishments").incrementAndGet();
    }
    
    public void incrementWarn() {
        counters.get("warns").incrementAndGet();
        counters.get("total_punishments").incrementAndGet();
    }
    
    public void incrementUnban() {
        counters.get("unbans").incrementAndGet();
        counters.get("active_punishments").decrementAndGet();
    }
    
    public void incrementUnmute() {
        counters.get("unmutes").incrementAndGet();
        counters.get("active_punishments").decrementAndGet();
    }
    
    public void incrementAutoModKick() {
        counters.get("automod_kicks").incrementAndGet();
        counters.get("total_punishments").incrementAndGet();
    }
    
    public void incrementAutoModMute() {
        counters.get("automod_mutes").incrementAndGet();
        counters.get("total_punishments").incrementAndGet();
        counters.get("active_punishments").incrementAndGet();
    }
    
    public int getBanCount() {
        return counters.get("bans").get();
    }
    
    public int getMuteCount() {
        return counters.get("mutes").get();
    }
    
    public int getKickCount() {
        return counters.get("kicks").get();
    }
    
    public int getWarnCount() {
        return counters.get("warns").get();
    }
    
    public int getUnbanCount() {
        return counters.get("unbans").get();
    }
    
    public int getUnmuteCount() {
        return counters.get("unmutes").get();
    }
    
    public int getAutoModKickCount() {
        return counters.get("automod_kicks").get();
    }
    
    public int getAutoModMuteCount() {
        return counters.get("automod_mutes").get();
    }
    
    public int getTotalPunishmentCount() {
        return counters.get("total_punishments").get();
    }
    
    public int getActivePunishmentCount() {
        return counters.get("active_punishments").get();
    }
    
    public Map<String, Integer> getAllMetrics() {
        Map<String, Integer> metrics = new HashMap<>();
        counters.forEach((key, value) -> metrics.put(key, value.get()));
        return metrics;
    }
    
    public void resetMetrics() {
        counters.values().forEach(counter -> counter.set(0));
    }
    
    public void logMetrics() {
        if (plugin.getConfigManager().getMainConfig().isDebug()) {
            plugin.getLogger().info("=== AkyModeration Metrics ===");
            getAllMetrics().forEach((key, value) -> {
                plugin.getLogger().info(key + ": " + value);
            });
        }
    }
    
    public void startMetricsTask() {
        metricsTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::logMetrics, 20L * 60 * 5, 20L * 60 * 5);
    }
    
    public void shutdown() {
        if (metricsTask != null) {
            metricsTask.cancel();
        }
    }
}
