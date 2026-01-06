package model.enemy;

/**
 * Enum representing different enemy types in the game.
 */
public enum EnemyType {
    CLU("Clu", "Rogue program seeking control"),
    SARK("Sark", "Commander of the MCP forces"),
    KOURA("Koura", "Swift and unpredictable"),
    RINZLER("Rinzler", "Elite enforcer program");
    
    private final String displayName;
    private final String description;
    
    EnemyType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
