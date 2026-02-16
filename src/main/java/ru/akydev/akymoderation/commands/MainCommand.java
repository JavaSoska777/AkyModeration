package ru.akydev.akymoderation.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.akydev.akymoderation.AkyModeration;
import ru.akydev.akymoderation.punishment.PunishmentManager;
import ru.akydev.akymoderation.utils.MessageUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MainCommand implements CommandExecutor {
    
    private final AkyModeration plugin;
    private final PunishmentManager punishmentManager;
    private final MessageUtils messageUtils;
    
    public MainCommand(AkyModeration plugin) {
        this.plugin = plugin;
        this.punishmentManager = plugin.getPunishmentManager();
        this.messageUtils = plugin.getMessageUtils();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("akymoderation.admin")) {
            messageUtils.sendMessage(sender, messageUtils.getNoPermission());
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "history":
                if (args.length < 2) {
                    messageUtils.sendMessage(sender, "&cИспользование: /mod history <игрок>");
                    return true;
                }
                showHistory(sender, args[1]);
                break;
                
            case "check":
                if (args.length < 2) {
                    messageUtils.sendMessage(sender, "&cИспользование: /mod check <игрок>");
                    return true;
                }
                checkPlayer(sender, args[1]);
                break;
                
            case "reload":
                reloadConfig(sender);
                break;
                
            case "clear":
                if (args.length < 2) {
                    messageUtils.sendMessage(sender, "&cИспользование: /mod clear <игрок> [тип]");
                    return true;
                }
                clearHistory(sender, args[1], args.length > 2 ? args[2] : "all");
                break;
                
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        messageUtils.sendMessage(sender, "&6=== AkyModeration ===");
        messageUtils.sendMessage(sender, "&e/mod history <игрок> &7- Показать историю наказаний");
        messageUtils.sendMessage(sender, "&e/mod check <игрок> &7- Проверить активные наказания");
        messageUtils.sendMessage(sender, "&e/mod clear <игрок> [тип] &7- Очистить историю");
        messageUtils.sendMessage(sender, "&e/mod reload &7- Перезагрузить конфиг");
    }
    
    private void showHistory(CommandSender sender, String playerName) {
        UUID targetUuid = UUID.fromString("OfflinePlayer:" + playerName);
        
        punishmentManager.getPlayerPunishments(targetUuid)
            .thenAccept(punishments -> {
                if (punishments.isEmpty()) {
                    messageUtils.sendMessage(sender, "&aУ игрока " + playerName + " нет наказаний");
                    return;
                }
                
                messageUtils.sendMessage(sender, "&6=== История наказаний " + playerName + " ===");
                punishments.forEach(punishment -> {
                    String status = punishment.isActive() ? "&cАктивно" : "&aИстекло";
                    messageUtils.sendMessage(sender, String.format("&e%s &7- %s &8(%s) &7- %s", 
                        punishment.getType().name(), 
                        punishment.getReason(), 
                        punishment.getDuration().equals("0") ? "Навсегда" : punishment.getDuration(),
                        status));
                });
            });
    }
    
    private void checkPlayer(CommandSender sender, String playerName) {
        UUID targetUuid = UUID.fromString("OfflinePlayer:" + playerName);
        
        punishmentManager.getActivePunishments(targetUuid)
            .thenAccept(punishments -> {
                if (punishments.isEmpty()) {
                    messageUtils.sendMessage(sender, "&aУ игрока " + playerName + " нет активных наказаний");
                    return;
                }
                
                messageUtils.sendMessage(sender, "&6=== Активные наказания " + playerName + " ===");
                punishments.forEach(punishment -> {
                    messageUtils.sendMessage(sender, String.format("&e%s &7- %s &8(%s)", 
                        punishment.getType().name(), 
                        punishment.getReason(), 
                        punishment.getDuration().equals("0") ? "Навсегда" : punishment.getDuration()));
                });
            });
    }
    
    private void clearHistory(CommandSender sender, String playerName, String type) {
        UUID targetUuid = UUID.fromString("OfflinePlayer:" + playerName);
        
        if (type.equalsIgnoreCase("all")) {
            punishmentManager.clearPlayerHistory(targetUuid)
                .thenAccept(success -> {
                    if (success) {
                        messageUtils.sendMessage(sender, "&aИстория игрока " + playerName + " очищена");
                    } else {
                        messageUtils.sendMessage(sender, "&cНе удалось очистить историю");
                    }
                });
        } else {
            try {
                punishmentManager.clearPunishmentType(targetUuid, type)
                    .thenAccept(success -> {
                        if (success) {
                            messageUtils.sendMessage(sender, "&aНаказания типа " + type + " для игрока " + playerName + " очищены");
                        } else {
                            messageUtils.sendMessage(sender, "&cНе удалось очистить наказания");
                        }
                    });
            } catch (IllegalArgumentException e) {
                messageUtils.sendMessage(sender, "&cНеверный тип наказания");
            }
        }
    }
    
    private void reloadConfig(CommandSender sender) {
        plugin.reloadConfig();
        messageUtils.sendMessage(sender, "&aКонфигурация перезагружена");
    }
}
