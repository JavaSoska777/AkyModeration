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

public class UnmuteCommand implements CommandExecutor {
    
    private final AkyModeration plugin;
    private final PunishmentManager punishmentManager;
    private final MessageUtils messageUtils;
    
    public UnmuteCommand(AkyModeration plugin) {
        this.plugin = plugin;
        this.punishmentManager = plugin.getPunishmentManager();
        this.messageUtils = plugin.getMessageUtils();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("akymoderation.unmute")) {
            messageUtils.sendMessage(sender, messageUtils.getNoPermission());
            return true;
        }
        
        if (args.length < 1) {
            messageUtils.sendMessage(sender, "&cИспользование: /unmute <игрок>");
            return true;
        }
        
        String targetName = args[0];
        UUID targetUuid = UUID.fromString("OfflinePlayer:" + targetName);
        
        punishmentManager.removePunishment(targetUuid, PunishmentType.MUTE)
            .thenAccept(success -> {
                if (success) {
                    messageUtils.sendMessage(sender, "&aИгрок " + targetName + " успешно размучен!");
                } else {
                    messageUtils.sendMessage(sender, "&cНе удалось размутить игрока!");
                }
            });
        
        return true;
    }
}
