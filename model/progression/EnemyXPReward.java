package model.progression;

/**
 * Defines XP rewards for defeating different enemy types.
 */
public enum EnemyXPReward {
    BASIC_ENEMY(50),      // Standard enemy
    ELITE_ENEMY(100),     // Harder enemies
    BOSS_ENEMY(250);      // Boss-type enemies
    
    private final int xpReward;
    
    EnemyXPReward(int xpReward) {
        this.xpReward = xpReward;
    }
    
    public int getXPReward() {
        return xpReward;
    }
}
