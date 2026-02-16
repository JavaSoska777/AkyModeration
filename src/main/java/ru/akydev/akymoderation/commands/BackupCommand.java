package ru.akydev.akymoderation.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import ru.akydev.akymoderation.AkyModeration;
import ru.akydev.akymoderation.backup.BackupManager;
import ru.akydev.akymoderation.utils.MessageUtils;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class BackupCommand implements CommandExecutor {
    
    private final AkyModeration plugin;
    private final BackupManager backupManager;
    private final MessageUtils messageUtils;
    
    public BackupCommand(AkyModeration plugin) {
        this.plugin = plugin;
        this.backupManager = new BackupManager(plugin);
        this.messageUtils = plugin.getMessageUtils();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("akymoderation.backup")) {
            messageUtils.sendMessage(sender, messageUtils.getNoPermission());
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "create":
                createBackup(sender);
                break;
                
            case "restore":
                if (args.length < 2) {
                    messageUtils.sendMessage(sender, "&cИспользование: /backup restore <имя_файла>");
                    return true;
                }
                restoreBackup(sender, args[1]);
                break;
                
            case "list":
                listBackups(sender);
                break;
                
            case "cleanup":
                cleanupBackups(sender);
                break;
                
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    private void createBackup(CommandSender sender) {
        messageUtils.sendMessage(sender, "&eСоздание резервной копии...");
        
        backupManager.createBackup()
            .thenAccept(success -> {
                if (success) {
                    messageUtils.sendMessage(sender, "&aРезервная копия успешно создана!");
                } else {
                    messageUtils.sendMessage(sender, "&cНе удалось создать резервную копию!");
                }
            });
    }
    
    private void restoreBackup(CommandSender sender, String fileName) {
        if (!fileName.endsWith(".zip")) {
            fileName += ".zip";
        }
        
        messageUtils.sendMessage(sender, "&eВосстановление резервной копии...");
        
        backupManager.restoreBackup(fileName)
            .thenAccept(success -> {
                if (success) {
                    messageUtils.sendMessage(sender, "&aРезервная копия успешно восстановлена!");
                    messageUtils.sendMessage(sender, "&eПерезапустите сервер для применения изменений.");
                } else {
                    messageUtils.sendMessage(sender, "&cНе удалось восстановить резервную копию!");
                }
            });
    }
    
    private void listBackups(CommandSender sender) {
        File[] backups = backupManager.getBackupList();
        
        if (backups == null || backups.length == 0) {
            messageUtils.sendMessage(sender, "&cРезервные копии не найдены!");
            return;
        }
        
        messageUtils.sendMessage(sender, "&6=== Доступные резервные копии ===");
        
        for (int i = 0; i < backups.length; i++) {
            File backup = backups[i];
            String size = formatFileSize(backup.length());
            String date = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm")
                .format(new java.util.Date(backup.lastModified()));
            
            messageUtils.sendMessage(sender, String.format("&e%d. &7%s &8(%s - %s)", 
                i + 1, backup.getName(), size, date));
        }
    }
    
    private void cleanupBackups(CommandSender sender) {
        int maxBackups = plugin.getConfigManager().getMainConfig().getMaxHistorySize() / 100;
        
        backupManager.cleanupOldBackups(maxBackups);
        messageUtils.sendMessage(sender, "&aСтарые резервные копии очищены!");
    }
    
    private void sendHelp(CommandSender sender) {
        messageUtils.sendMessage(sender, "&6=== Команды резервного копирования ===");
        messageUtils.sendMessage(sender, "&e/backup create &7- Создать резервную копию");
        messageUtils.sendMessage(sender, "&e/backup restore <файл> &7- Восстановить из копии");
        messageUtils.sendMessage(sender, "&e/backup list &7- Показать список копий");
        messageUtils.sendMessage(sender, "&e/backup cleanup &7- Очистить старые копии");
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}
