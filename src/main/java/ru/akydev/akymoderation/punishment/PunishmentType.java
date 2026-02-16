package ru.akydev.akymoderation.punishment;

public enum PunishmentType {
    BAN("Бан"),
    MUTE("Мут"),
    KICK("Кик"),
    WARN("Предупреждение");
    
    private final String displayName;
    
    PunishmentType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
