package ru.akydev.akymoderation.database;

import ru.akydev.akymoderation.AkyModeration;
import ru.akydev.akymoderation.punishment.Punishment;
import ru.akydev.akymoderation.punishment.PunishmentType;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseManager {
    
    private final AkyModeration plugin;
    private Connection connection;
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, PreparedStatement> statementCache;
    
    public DatabaseManager(AkyModeration plugin) {
        this.plugin = plugin;
        this.executorService = Executors.newFixedThreadPool(4);
        this.statementCache = new ConcurrentHashMap<>();
        initialize();
    }
    
    private void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/punishments.db");
            createTables();
            plugin.getLogger().info("Database connected successfully");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
        }
    }
    
    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS punishments (" +
                "id TEXT PRIMARY KEY," +
                "player_uuid TEXT NOT NULL," +
                "moderator_uuid TEXT NOT NULL," +
                "player_name TEXT NOT NULL," +
                "type TEXT NOT NULL," +
                "reason TEXT NOT NULL," +
                "duration TEXT NOT NULL," +
                "created_at TEXT NOT NULL," +
                "expires_at TEXT," +
                "active BOOLEAN NOT NULL" +
                ")");
            
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_uuid ON punishments(player_uuid)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_type ON punishments(type)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_active ON punishments(active)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_created_at ON punishments(created_at)");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create tables: " + e.getMessage());
        }
    }
    
    public CompletableFuture<Boolean> addPunishment(Punishment punishment) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "INSERT INTO punishments (id, player_uuid, moderator_uuid, player_name, type, reason, duration, created_at, expires_at, active) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = getCachedStatement(sql)) {
                stmt.setString(1, punishment.getId().toString());
                stmt.setString(2, punishment.getPlayerUuid().toString());
                stmt.setString(3, punishment.getModeratorUuid().toString());
                stmt.setString(4, punishment.getPlayerName());
                stmt.setString(5, punishment.getType().name());
                stmt.setString(6, punishment.getReason());
                stmt.setString(7, punishment.getDuration());
                stmt.setString(8, punishment.getCreatedAt().toString());
                stmt.setString(9, punishment.getExpiresAt() != null ? punishment.getExpiresAt().toString() : null);
                stmt.setBoolean(10, punishment.isActive());
                
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to add punishment: " + e.getMessage());
                return false;
            }
        }, executorService);
    }
    
    public CompletableFuture<Boolean> removeActivePunishment(UUID playerUuid, PunishmentType type) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE punishments SET active = false WHERE player_uuid = ? AND type = ? AND active = true";
            
            try (PreparedStatement stmt = getCachedStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                stmt.setString(2, type.name());
                
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to remove punishment: " + e.getMessage());
                return false;
            }
        }, executorService);
    }
    
    public CompletableFuture<List<Punishment>> getPlayerPunishments(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM punishments WHERE player_uuid = ? ORDER BY created_at DESC";
            List<Punishment> punishments = new ArrayList<>();
            
            try (PreparedStatement stmt = getCachedStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    punishments.add(mapResultSetToPunishment(rs));
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get player punishments: " + e.getMessage());
            }
            
            return punishments;
        }, executorService);
    }
    
    public CompletableFuture<List<Punishment>> getActivePunishments(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM punishments WHERE player_uuid = ? AND active = true";
            List<Punishment> punishments = new ArrayList<>();
            
            try (PreparedStatement stmt = getCachedStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    punishments.add(mapResultSetToPunishment(rs));
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get active punishments: " + e.getMessage());
            }
            
            return punishments;
        }, executorService);
    }
    
    public CompletableFuture<Integer> getPunishmentCount(UUID playerUuid, PunishmentType type) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT COUNT(*) FROM punishments WHERE player_uuid = ? AND type = ?";
            
            try (PreparedStatement stmt = getCachedStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                stmt.setString(2, type.name());
                ResultSet rs = stmt.executeQuery();
                
                return rs.next() ? rs.getInt(1) : 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to get punishment count: " + e.getMessage());
                return 0;
            }
        }, executorService);
    }
    
    public CompletableFuture<Boolean> clearPlayerHistory(UUID playerUuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM punishments WHERE player_uuid = ?";
            
            try (PreparedStatement stmt = getCachedStatement(sql)) {
                stmt.setString(1, playerUuid.toString());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to clear player history: " + e.getMessage());
                return false;
            }
        }, executorService);
    }
    
    public CompletableFuture<Boolean> clearPunishmentType(UUID playerUuid, String typeName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                PunishmentType type = PunishmentType.valueOf(typeName.toUpperCase());
                String sql = "DELETE FROM punishments WHERE player_uuid = ? AND type = ?";
                
                try (PreparedStatement stmt = getCachedStatement(sql)) {
                    stmt.setString(1, playerUuid.toString());
                    stmt.setString(2, type.name());
                    return stmt.executeUpdate() > 0;
                } catch (SQLException e) {
                    plugin.getLogger().severe("Failed to clear punishment type: " + e.getMessage());
                    return false;
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid punishment type: " + typeName);
                return false;
            }
        }, executorService);
    }
    
    private PreparedStatement getCachedStatement(String sql) throws SQLException {
        return statementCache.computeIfAbsent(sql, key -> {
            try {
                return connection.prepareStatement(key);
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to prepare statement: " + e.getMessage());
                throw new RuntimeException("Failed to prepare statement", e);
            }
        });
    }
    
    private Punishment mapResultSetToPunishment(ResultSet rs) throws SQLException {
        return new Punishment(
            UUID.fromString(rs.getString("id")),
            UUID.fromString(rs.getString("player_uuid")),
            UUID.fromString(rs.getString("moderator_uuid")),
            PunishmentType.valueOf(rs.getString("type")),
            rs.getString("reason"),
            rs.getString("duration"),
            LocalDateTime.parse(rs.getString("created_at")),
            rs.getString("expires_at") != null ? LocalDateTime.parse(rs.getString("expires_at")) : null,
            rs.getBoolean("active"),
            rs.getString("player_name")
        );
    }
    
    public void close() {
        try {
            statementCache.values().forEach(stmt -> {
                try {
                    if (stmt != null && !stmt.isClosed()) {
                        stmt.close();
                    }
                } catch (SQLException e) {
                    plugin.getLogger().warning("Failed to close statement: " + e.getMessage());
                }
            });
            
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
        }
    }
}
