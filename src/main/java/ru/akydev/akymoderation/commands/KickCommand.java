package ru.akydev.akymoderation.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.akydev.akymoderation.AkyModeration;
import ru.akydev.akymoderation.punishment.PunishmentManager;
import ru.akydev.akymoderation.punishment.PunishmentType;
import ru.akydev.akymoderation.utils.MessageUtils;

public class KickCommand implements CommandExecutor {
    
    private final AkyModeration plugin;
    private final PunishmentManager punishmentManager;
    private final MessageUtils messageUtils;
    
    public KickCommand(AkyModeration plugin) {
        this.plugin = plugin;
        this.punishmentManager = plugin.getPunishmentManager();
        this.messageUtils = plugin.getMessageUtils();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("akymoderation.kick")) {
            messageUtils.sendMessage(sender, messageUtils.getNoPermission());
            return true;
        }
        
        if (args.length < 2) {
            messageUtils.sendMessage(sender, "&cИспользование: /kick <игрок> <причина>");
            return true;
        }
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                reasonBuilder.append(" ");
            }
            reasonBuilder.append(args[i]);
        }
        String reason = reasonBuilder.toString();
        
        if (target != null && target.isOnline()) {
            if (target.hasPermission("akymoderation.exempt")) {
                messageUtils.sendMessage(sender, "&cЭтот игрок имеет иммунитет к наказаниям!");
                return true;
            }
            
            if (!(sender instanceof Player)) {
                messageUtils.sendMessage(sender, "&cЭта команда может быть выполнена только игроком!");
                return true;
            }
            
            Player moderator = (Player) sender;
            punishmentManager.issuePunishment(PunishmentType.KICK, target, moderator, reason, "0", false)
                .thenAccept(success -> {
                    if (success) {
                        messageUtils.sendMessage(moderator, "&aИгрок " + target.getName() + " успешно кикнут!");
                    } else {
                        messageUtils.sendMessage(moderator, "&cНе удалось кикнуть игрока!");
                    }
                });
        } else {
            messageUtils.sendMessage(sender, messageUtils.getPlayerNotFound());
            return true;
        }
        
        return true;
    }
}
