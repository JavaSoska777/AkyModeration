package ru.akydev.akymoderation.automod;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.akydev.akymoderation.AkyModeration;
import ru.akydev.akymoderation.punishment.PunishmentManager;
import ru.akydev.akymoderation.punishment.PunishmentType;
import ru.akydev.akymoderation.utils.MessageUtils;

import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

public class AutoModeration implements Listener {
    
    private final AkyModeration plugin;
    private final PunishmentManager punishmentManager;
    private final MessageUtils messageUtils;
    private final List<Pattern> forbiddenPatterns;
    
    public AutoModeration(AkyModeration plugin, PunishmentManager punishmentManager, MessageUtils messageUtils) {
        this.plugin = plugin;
        this.punishmentManager = punishmentManager;
        this.messageUtils = messageUtils;
        this.forbiddenPatterns = initializePatterns();
    }
    
    private List<Pattern> initializePatterns() {
        List<Pattern> patterns = new ArrayList<>();
        
        patterns.add(Pattern.compile("(?i).*\\b(мать|мат|блять|сука|хуй|пизда|ебать|нахуй|блядь|сучка|хер|говно|жопа).*"));
        patterns.add(Pattern.compile("(?i).*\\b(suck|fuck|shit|bitch|asshole|dick|pussy|cunt|whore).*"));
        patterns.add(Pattern.compile("(?i).*\\b(ip:|port:|server:|connect|join|играй|заходи).*\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}.*"));
        patterns.add(Pattern.compile("(?i).*\\b(discord\\.gg|t\\.me|telegram\\.org|vk\\.com).*"));
        
        return patterns;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        if (player.hasPermission("akymoderation.exempt")) {
            return;
        }
        
        for (Pattern pattern : forbiddenPatterns) {
            if (pattern.matcher(message).matches()) {
                event.setCancelled(true);
                handleViolation(player, message);
                return;
            }
        }
        
        if (isSpam(message)) {
            event.setCancelled(true);
            handleSpam(player, message);
        }
    }
    
    private void handleViolation(Player player, String message) {
        punishmentManager.issuePunishment(PunishmentType.MUTE, player, null, 
            "Автоматический мут: нарушение правил чата", "30m", true)
            .thenAccept(success -> {
                if (success) {
                    messageUtils.sendMessage(player, "&cВы были автоматически замучены за нарушение правил чата!");
                    plugin.getLogger().info("AutoModeration: " + player.getName() + " muted for violation: " + message);
                }
            });
    }
    
    private void handleSpam(Player player, String message) {
        punishmentManager.issuePunishment(PunishmentType.MUTE, player, null, 
            "Автоматический мут: спам", "15m", true)
            .thenAccept(success -> {
                if (success) {
                    messageUtils.sendMessage(player, messageUtils.getAutoSpamViolation());
                    plugin.getLogger().info("AutoModeration: " + player.getName() + " muted for spam: " + message);
                }
            });
    }
    
    private boolean isSpam(String message) {
        return message.length() > 200 || 
               message.chars().filter(ch -> ch == message.charAt(0)).count() > message.length() * 0.7 ||
               message.toUpperCase().equals(message) && message.length() > 20;
    }
}
