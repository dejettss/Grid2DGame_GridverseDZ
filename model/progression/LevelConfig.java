package model.progression;

import model.arena.ArenaFactory.ArenaType;
import model.objects.Player.PlayerType;
import model.enemy.EnemyType;

/**
 * Configuration class for a single level in the game.
 * Defines player type, enemy type, enemy count, arena type, and XP threshold.
 */
public class LevelConfig {
    private final int levelNumber;
    private final int chapter;
    private final PlayerType playerType;
    private final EnemyType enemyType;
    private final int enemyCount;
    private final ArenaType arenaType;
    private final int xpThreshold;
    
    /**
     * Creates a level configuration.
     * 
     * @param levelNumber The level number (1-16)
     * @param chapter The chapter number (1-4)
     * @param playerType The player character type (TRON or KEVIN)
     * @param enemyType The enemy type for this level
     * @param enemyCount Number of enemies in this level
     * @param arenaType The arena type for this level
     * @param xpThreshold XP required to complete this level (enemy XP * enemy count)
     */
    public LevelConfig(int levelNumber, int chapter, PlayerType playerType, 
                       EnemyType enemyType, int enemyCount, ArenaType arenaType, 
                       int xpThreshold) {
        this.levelNumber = levelNumber;
        this.chapter = chapter;
        this.playerType = playerType;
        this.enemyType = enemyType;
        this.enemyCount = enemyCount;
        this.arenaType = arenaType;
        this.xpThreshold = xpThreshold;
    }
    
    // Getters
    public int getLevelNumber() { return levelNumber; }
    public int getChapter() { return chapter; }
    public PlayerType getPlayerType() { return playerType; }
    public EnemyType getEnemyType() { return enemyType; }
    public int getEnemyCount() { return enemyCount; }
    public ArenaType getArenaType() { return arenaType; }
    public int getXpThreshold() { return xpThreshold; }
    
    /**
     * Gets the chapter-relative level (1-4 within each chapter).
     * 
     * @return Level within chapter (1-4)
     */
    public int getChapterLevel() {
        return ((levelNumber - 1) % 4) + 1;
    }
    
    /**
     * Gets a formatted string describing this level.
     * 
     * @return String like "Chapter 1 - Level 1: Tron vs Koura x1 (ClassicGrid)"
     */
    public String getDisplayName() {
        return String.format("Chapter %d - Level %d: %s vs %s x%d (%s)", 
                           chapter, getChapterLevel(), 
                           playerType.toString(), 
                           enemyType.toString(), 
                           enemyCount, 
                           arenaType.toString());
    }
    
    @Override
    public String toString() {
        return String.format("Level %d (Chapter %d-%d): %s vs %s x%d on %s, XP: %d",
                           levelNumber, chapter, getChapterLevel(),
                           playerType, enemyType, enemyCount, arenaType, xpThreshold);
    }
}
