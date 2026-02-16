package ru.akydev.akymoderation.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import ru.akydev.akymoderation.config.MessagesConfig;

public class MessageUtils {
    
    private MessagesConfig messagesConfig;
    
    public MessageUtils(MessagesConfig messagesConfig) {
        this.messagesConfig = messagesConfig;
    }
    
    public String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }
    
    public void sendMessageWithPrefix(CommandSender sender, String message) {
        sendMessage(sender, messagesConfig.getPrefix() + message);
    }
    
    public String getNoPermission() {
        return messagesConfig.getNoPermission();
    }
    
    public String getPlayerNotFound() {
        return messagesConfig.getPlayerNotFound();
    }
    
    public String getPlayerOffline() {
        return messagesConfig.getPlayerOffline();
    }
    
    public String getPlayerExempt() {
        return messagesConfig.getPlayerExempt();
    }
    
    public String getNotInWork() {
        return messagesConfig.getPluginNotWorking();
    }
    
    public String getPrefix() {
        return messagesConfig.getPrefix();
    }
    
    public String getMuteMessage() {
        return messagesConfig.getMuteMessage();
    }
    
    public String getAutoMuteViolation() {
        return messagesConfig.getAutoMuteViolation();
    }
    
    public String getAutoSpamViolation() {
        return messagesConfig.getAutoSpamViolation();
    }
}
