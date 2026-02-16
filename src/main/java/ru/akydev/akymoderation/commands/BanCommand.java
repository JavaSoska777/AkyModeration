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
import ru.akydev.akymoderation.utils.TimeUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BanCommand implements CommandExecutor {
    
    private final AkyModeration plugin;
    private final PunishmentManager punishmentManager;
    private final MessageUtils messageUtils;
    private final java.util.concurrent.ConcurrentHashMap<String, Long> cooldowns = new java.util.concurrent.ConcurrentHashMap<>();
    
    public BanCommand(AkyModeration plugin) {
        this.plugin = plugin;
        this.punishmentManager = plugin.getPunishmentManager();
        this.messageUtils = plugin.getMessageUtils();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("akymoderation.ban")) {
            messageUtils.sendMessage(sender, messageUtils.getNoPermission());
            return true;
        }
        
        if (args.length < 2) {
            messageUtils.sendMessage(sender, "&cИспользование: /ban <игрок> [время] <причина>");
            return true;
        }
        
        String cooldownKey = sender.getName() + ":ban";
        long currentTime = System.currentTimeMillis();
        if (cooldowns.containsKey(cooldownKey) && currentTime - cooldowns.get(cooldownKey) < 5000) {
            messageUtils.sendMessage(sender, "&cПодождите 5 секунд перед повторным использованием команды!");
            return true;
        }
        cooldowns.put(cooldownKey, currentTime);
        
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        boolean silent = false;
        
        int startIndex = 1;
        String duration = plugin.getConfigManager().getMainConfig().getDefaultBanDuration();
        
        if (args[1].equalsIgnoreCase("-s")) {
            silent = true;
            startIndex = 2;
        }
        
        if (args.length > startIndex) {
            String possibleDuration = args[startIndex];
            if (TimeUtils.parseTime(possibleDuration) > 0 || possibleDuration.equalsIgnoreCase("perm") || possibleDuration.equalsIgnoreCase("permanent")) {
                duration = possibleDuration;
                startIndex++;
            }
        }
        
        if (args.length <= startIndex) {
            messageUtils.sendMessage(sender, "&cУкажите причину бана!");
            return true;
        }
        
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            if (i > startIndex) {
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
            punishmentManager.issuePunishment(PunishmentType.BAN, target, moderator, reason, duration, silent)
                .thenAccept(success -> {
                    if (success) {
                        messageUtils.sendMessage(moderator, "&aИгрок " + target.getName() + " успешно забанен!");
                    } else {
                        messageUtils.sendMessage(moderator, "&cНе удалось забанить игрока!");
                    }
                });
        } else {
            if (!(sender instanceof Player)) {
                messageUtils.sendMessage(sender, "&cЭта команда может быть выполнена только игроком!");
                return true;
            }
            
            Player moderator = (Player) sender;
            final String finalDuration = duration;
            final boolean finalSilent = silent;
            CompletableFuture.supplyAsync(() -> {
                try {
                    UUID targetUuid = UUID.fromString("OfflinePlayer:" + targetName);
                    
                    return punishmentManager.issuePunishmentOffline(PunishmentType.BAN, targetName, targetUuid, moderator, reason, finalDuration, finalSilent)
                        .thenAccept(success -> {
                            if (success) {
                                messageUtils.sendMessage(moderator, "&aИгрок " + targetName + " успешно забанен!");
                            } else {
                                messageUtils.sendMessage(moderator, "&cНе удалось забанить игрока!");
                            }
                        });
                } catch (Exception e) {
                    messageUtils.sendMessage(moderator, "&cИгрок не найден!");
                    return null;
                }
            });
        }
        
        return true;
    }
}
