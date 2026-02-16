package ru.akydev.akymoderation.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.akydev.akymoderation.AkyModeration;
import ru.akydev.akymoderation.punishment.Punishment;
import ru.akydev.akymoderation.punishment.PunishmentManager;
import ru.akydev.akymoderation.punishment.PunishmentType;
import ru.akydev.akymoderation.utils.MessageUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ChatListener implements Listener {
    
    private final AkyModeration plugin;
    private final PunishmentManager punishmentManager;
    private final MessageUtils messageUtils;
    
    public ChatListener(AkyModeration plugin, PunishmentManager punishmentManager) {
        this.plugin = plugin;
        this.punishmentManager = punishmentManager;
        this.messageUtils = plugin.getMessageUtils();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        punishmentManager.getActivePunishments(playerUuid)
            .thenAccept(activePunishments -> {
                boolean isMuted = activePunishments.stream()
                    .anyMatch(p -> p != null && ((ru.akydev.akymoderation.punishment.Punishment) p).getType() == PunishmentType.MUTE && ((ru.akydev.akymoderation.punishment.Punishment) p).isActive());
                
                if (isMuted) {
                    event.setCancelled(true);
                    messageUtils.sendMessage(player, messageUtils.getMuteMessage());
                    

                    checkAutoPunishment(player, event.getMessage());
                }
            });
    }
    
    private void checkAutoPunishment(Player player, String message) {

        if (isSpam(message)) {
            punishmentManager.issuePunishment(PunishmentType.MUTE, player, null, 
                "Автоматический мут: спам", "15m", true)
                .thenAccept(success -> {
                    if (success) {
                        messageUtils.sendMessage(player, messageUtils.getAutoSpamViolation());
                        plugin.getLogger().info("AutoModeration: " + player.getName() + " muted for spam: " + message);
                    }
                });
        }
        

        if (containsForbiddenContent(message)) {
            punishmentManager.issuePunishment(PunishmentType.MUTE, player, null, 
                "Автоматический мут: нарушение правил чата", "30m", true)
                .thenAccept(success -> {
                    if (success) {
                        messageUtils.sendMessage(player, messageUtils.getAutoMuteViolation());
                        plugin.getLogger().info("AutoModeration: " + player.getName() + " muted for violation: " + message);
                    }
                });
        }
    }
    
    private boolean isSpam(String message) {
        return message.length() > 200 || 
               message.chars().filter(ch -> ch == message.charAt(0)).count() > message.length() * 0.7 ||
               message.toUpperCase().equals(message) && message.length() > 20;
    }
    
    private boolean containsForbiddenContent(String message) {

        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("ip:") || lowerMessage.contains("discord.gg") || 
               lowerMessage.contains("t.me") || lowerMessage.contains("telegram.org");
    }
}
