package ru.akydev.akymoderation.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import ru.akydev.akymoderation.AkyModeration;
import ru.akydev.akymoderation.punishment.Punishment;
import ru.akydev.akymoderation.punishment.PunishmentManager;
import ru.akydev.akymoderation.punishment.PunishmentType;
import ru.akydev.akymoderation.utils.MessageUtils;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LoginListener implements Listener {
    
    private final AkyModeration plugin;
    private final PunishmentManager punishmentManager;
    private final MessageUtils messageUtils;
    
    public LoginListener(AkyModeration plugin, PunishmentManager punishmentManager) {
        this.plugin = plugin;
        this.punishmentManager = punishmentManager;
        this.messageUtils = plugin.getMessageUtils();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        
        UUID playerUuid = event.getUniqueId();

        punishmentManager.getActivePunishments(playerUuid)
            .thenAccept(activePunishments -> {
                boolean isBanned = activePunishments.stream()
                    .anyMatch(p -> p != null && ((ru.akydev.akymoderation.punishment.Punishment) p).getType() == PunishmentType.BAN && ((ru.akydev.akymoderation.punishment.Punishment) p).isActive());
                
                if (isBanned) {
                    String banMessage = messageUtils.colorize(
                        "&cВы забанены на этом сервере!\n\n" +
                        "&7Подайте апелляцию на сайте: &fhttps://github.com/JavaSoska777"
                    );
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, banMessage);
                }
            });
    }
}
