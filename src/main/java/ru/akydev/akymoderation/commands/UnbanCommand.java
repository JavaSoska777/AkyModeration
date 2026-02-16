package ru.akydev.akymoderation.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.akydev.akymoderation.AkyModeration;
import ru.akydev.akymoderation.punishment.PunishmentManager;
import ru.akydev.akymoderation.punishment.PunishmentType;
import ru.akydev.akymoderation.utils.MessageUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UnbanCommand implements CommandExecutor {
    
    private final AkyModeration plugin;
    private final PunishmentManager punishmentManager;
    private final MessageUtils messageUtils;
    
    public UnbanCommand(AkyModeration plugin) {
        this.plugin = plugin;
        this.punishmentManager = plugin.getPunishmentManager();
        this.messageUtils = plugin.getMessageUtils();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("akymoderation.unban")) {
            messageUtils.sendMessage(sender, messageUtils.getNoPermission());
            return true;
        }
        
        if (args.length < 1) {
            messageUtils.sendMessage(sender, "&cИспользование: /unban <игрок>");
            return true;
        }
        
        String targetName = args[0];
        UUID targetUuid = UUID.fromString("OfflinePlayer:" + targetName);
        
        punishmentManager.removePunishment(targetUuid, PunishmentType.BAN)
            .thenAccept(success -> {
                if (success) {
                    messageUtils.sendMessage(sender, "&aИгрок " + targetName + " успешно разбанен!");
                } else {
                    messageUtils.sendMessage(sender, "&cНе удалось разбанить игрока!");
                }
            });
        
        return true;
    }
}
