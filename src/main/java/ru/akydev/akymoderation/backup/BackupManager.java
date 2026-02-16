package ru.akydev.akymoderation.backup;

import ru.akydev.akymoderation.AkyModeration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupManager {
    
    private final AkyModeration plugin;
    private final File dataFolder;
    private final File backupFolder;
    
    public BackupManager(AkyModeration plugin) {
        this.plugin = plugin;
        this.dataFolder = plugin.getDataFolder();
        this.backupFolder = new File(dataFolder, "backups");
        
        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }
    }
    
    public CompletableFuture<Boolean> createBackup() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                String backupFileName = "backup_" + timestamp + ".zip";
                File backupFile = new File(backupFolder, backupFileName);
                
                createZipBackup(backupFile);
                
                plugin.getLogger().info("Backup created successfully: " + backupFileName);
                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to create backup: " + e.getMessage());
                return false;
            }
        });
    }
    
    private void createZipBackup(File backupFile) throws IOException {
        try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(backupFile.toPath()))) {
            Path dataPath = dataFolder.toPath();
            
            Files.walk(dataPath)
                .filter(path -> !path.equals(dataPath))
                .filter(path -> !path.getFileName().toString().equals("backups"))
                .forEach(path -> {
                    try {
                        ZipEntry zipEntry = new ZipEntry(dataPath.relativize(path).toString());
                        zipOut.putNextEntry(zipEntry);
                        Files.copy(path, zipOut);
                        zipOut.closeEntry();
                    } catch (IOException e) {
                        plugin.getLogger().warning("Failed to add file to backup: " + path);
                    }
                });
        }
    }
    
    public CompletableFuture<Boolean> restoreBackup(String backupFileName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                File backupFile = new File(backupFolder, backupFileName);
                
                if (!backupFile.exists()) {
                    plugin.getLogger().warning("Backup file not found: " + backupFileName);
                    return false;
                }
                
                extractZipBackup(backupFile);
                
                plugin.getLogger().info("Backup restored successfully: " + backupFileName);
                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to restore backup: " + e.getMessage());
                return false;
            }
        });
    }
    
    private void extractZipBackup(File backupFile) throws IOException {
        Path backupPath = Paths.get(backupFile.getAbsolutePath());
        
        try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(backupFile)) {
            zipFile.stream().forEach(entry -> {
                try {
                    Path entryPath = dataFolder.toPath().resolve(entry.getName());
                    
                    if (entry.isDirectory()) {
                        Files.createDirectories(entryPath);
                    } else {
                        Files.createDirectories(entryPath.getParent());
                        Files.copy(zipFile.getInputStream(entry), entryPath);
                    }
                } catch (IOException e) {
                    plugin.getLogger().warning("Failed to extract file: " + entry.getName());
                }
            });
        }
    }
    
    public void cleanupOldBackups(int maxBackups) {
        File[] backupFiles = backupFolder.listFiles((dir, name) -> name.endsWith(".zip"));
        
        if (backupFiles == null || backupFiles.length <= maxBackups) {
            return;
        }
        
        java.util.Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        
        for (int i = maxBackups; i < backupFiles.length; i++) {
            if (backupFiles[i].delete()) {
                plugin.getLogger().info("Deleted old backup: " + backupFiles[i].getName());
            } else {
                plugin.getLogger().warning("Failed to delete old backup: " + backupFiles[i].getName());
            }
        }
    }
    
    public File[] getBackupList() {
        File[] backupFiles = backupFolder.listFiles((dir, name) -> name.endsWith(".zip"));
        
        if (backupFiles != null) {
            java.util.Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
        }
        
        return backupFiles;
    }
}
