package model.progression;

import model.arena.ArenaFactory.ArenaType;
import model.objects.Player.PlayerType;
import model.enemy.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager class for level progression system.
 * Handles 16 levels across 4 chapters with specific configurations.
 * 
 * Level Structure:
 * - Total Levels: 16
 * - Total Chapters: 4
 * - Chapter 1: Levels 1-4
 * - Chapter 2: Levels 5-8
 * - Chapter 3: Levels 9-12
 * - Chapter 4: Levels 13-16
 * 
 * XP Threshold Formula: Enemy XP * Enemy Count
 * (XP values loaded from Enemy classes via monsters.txt)
 */
public class LevelManager {
    
    // Constants
    public static final int TOTAL_LEVELS = 16;
    public static final int TOTAL_CHAPTERS = 4;
    public static final int LEVELS_PER_CHAPTER = 4;
    
    // Enemy XP values (loaded from Enemy classes)
    private static final int KOURA_XP;
    private static final int SARK_XP;
    private static final int RINZLER_XP;
    private static final int CLU_XP;
    
    // Level configurations storage
    private static final Map<Integer, LevelConfig> LEVELS = new HashMap<>();
    
    // Static initialization - load enemy XP values and create levels
    static {
        // Load enemy XP values from the Enemy classes (via monsters.txt)
        String filePath = "monsters.txt";
        Enemy koura = EnemyLoader.loadEnemyByName(filePath, "Koura");
        Enemy sark = EnemyLoader.loadEnemyByName(filePath, "Sark");
        Enemy rinzler = EnemyLoader.loadEnemyByName(filePath, "Rinzler");
        Enemy clu = EnemyLoader.loadEnemyByName(filePath, "Clu");
        
        // Use loaded XP values or defaults if loading fails
        KOURA_XP = (koura != null) ? koura.getXp() : 10;
        SARK_XP = (sark != null) ? sark.getXp() : 100;
        RINZLER_XP = (rinzler != null) ? rinzler.getXp() : 500;
        CLU_XP = (clu != null) ? clu.getXp() : 1000;
        
        initializeLevels();
    }
    
    /**
     * Initializes all 16 level configurations according to the level mapping.
     * Levels 1-8: Tron
     * Levels 9-16: Kevin
     */
    private static void initializeLevels() {
        // CHAPTER 1: Levels 1-4 (Koura) - TRON
        LEVELS.put(1, new LevelConfig(1, 1, PlayerType.TRON, EnemyType.KOURA, 1, 
                   ArenaType.CLASSIC_GRID, KOURA_XP * 1));
        LEVELS.put(2, new LevelConfig(2, 1, PlayerType.TRON, EnemyType.KOURA, 2, 
                   ArenaType.OPEN_FRONTIER, KOURA_XP * 2));
        LEVELS.put(3, new LevelConfig(3, 1, PlayerType.TRON, EnemyType.KOURA, 3, 
                   ArenaType.NEON_MAZE, KOURA_XP * 3));
        LEVELS.put(4, new LevelConfig(4, 1, PlayerType.TRON, EnemyType.KOURA, 4, 
                   ArenaType.PROCEDURAL, KOURA_XP * 4));
        
        // CHAPTER 2: Levels 5-8 (Sark) - TRON
        LEVELS.put(5, new LevelConfig(5, 2, PlayerType.TRON, EnemyType.SARK, 1, 
                   ArenaType.CLASSIC_GRID, SARK_XP * 1));
        LEVELS.put(6, new LevelConfig(6, 2, PlayerType.TRON, EnemyType.SARK, 2, 
                   ArenaType.OPEN_FRONTIER, SARK_XP * 2));
        LEVELS.put(7, new LevelConfig(7, 2, PlayerType.TRON, EnemyType.SARK, 3, 
                   ArenaType.NEON_MAZE, SARK_XP * 3));
        LEVELS.put(8, new LevelConfig(8, 2, PlayerType.TRON, EnemyType.SARK, 4, 
                   ArenaType.PROCEDURAL, SARK_XP * 4));
        
        // CHAPTER 3: Levels 9-12 (Rinzler) - KEVIN
        LEVELS.put(9, new LevelConfig(9, 3, PlayerType.KEVIN, EnemyType.RINZLER, 1, 
                   ArenaType.CLASSIC_GRID, RINZLER_XP * 1));
        LEVELS.put(10, new LevelConfig(10, 3, PlayerType.KEVIN, EnemyType.RINZLER, 2, 
                   ArenaType.OPEN_FRONTIER, RINZLER_XP * 2));
        LEVELS.put(11, new LevelConfig(11, 3, PlayerType.KEVIN, EnemyType.RINZLER, 3, 
                   ArenaType.NEON_MAZE, RINZLER_XP * 3));
        LEVELS.put(12, new LevelConfig(12, 3, PlayerType.KEVIN, EnemyType.RINZLER, 4, 
                   ArenaType.PROCEDURAL, RINZLER_XP * 4));
        
        // CHAPTER 4: Levels 13-16 (Clu) - KEVIN
        LEVELS.put(13, new LevelConfig(13, 4, PlayerType.KEVIN, EnemyType.CLU, 1, 
                   ArenaType.CLASSIC_GRID, CLU_XP * 1));
        LEVELS.put(14, new LevelConfig(14, 4, PlayerType.KEVIN, EnemyType.CLU, 2, 
                   ArenaType.OPEN_FRONTIER, CLU_XP * 2));
        LEVELS.put(15, new LevelConfig(15, 4, PlayerType.KEVIN, EnemyType.CLU, 3, 
                   ArenaType.NEON_MAZE, CLU_XP * 3));
        LEVELS.put(16, new LevelConfig(16, 4, PlayerType.KEVIN, EnemyType.CLU, 4, 
                   ArenaType.PROCEDURAL, CLU_XP * 4));
    }
    
    /**
     * Gets the configuration for a specific level.
     * 
     * @param levelNumber Level number (1-16)
     * @return LevelConfig for the specified level, or null if invalid
     */
    public static LevelConfig getLevelConfig(int levelNumber) {
        return LEVELS.get(levelNumber);
    }
    
    /**
     * Gets the chapter number for a given level.
     * 
     * @param levelNumber Level number (1-16)
     * @return Chapter number (1-4), or 0 if invalid
     */
    public static int getChapter(int levelNumber) {
        if (levelNumber < 1 || levelNumber > TOTAL_LEVELS) {
            return 0;
        }
        return ((levelNumber - 1) / LEVELS_PER_CHAPTER) + 1;
    }
    
    /**
     * Gets the level within a chapter (1-4).
     * 
     * @param levelNumber Level number (1-16)
     * @return Level within chapter (1-4), or 0 if invalid
     */
    public static int getChapterLevel(int levelNumber) {
        if (levelNumber < 1 || levelNumber > TOTAL_LEVELS) {
            return 0;
        }
        return ((levelNumber - 1) % LEVELS_PER_CHAPTER) + 1;
    }
    
    /**
     * Checks if a level number is valid.
     * 
     * @param levelNumber Level number to check
     * @return True if level is valid (1-16), false otherwise
     */
    public static boolean isValidLevel(int levelNumber) {
        return levelNumber >= 1 && levelNumber <= TOTAL_LEVELS;
    }
    
    /**
     * Checks if the player has completed all levels.
     * 
     * @param currentLevel Current level number
     * @return True if all levels completed (reached level 17+)
     */
    public static boolean isGameComplete(int currentLevel) {
        return currentLevel > TOTAL_LEVELS;
    }
    
    /**
     * Gets the starting level of a chapter.
     * 
     * @param chapter Chapter number (1-4)
     * @return Starting level of the chapter, or 0 if invalid
     */
    public static int getChapterStartLevel(int chapter) {
        if (chapter < 1 || chapter > TOTAL_CHAPTERS) {
            return 0;
        }
        return ((chapter - 1) * LEVELS_PER_CHAPTER) + 1;
    }
    
    /**
     * Gets the ending level of a chapter.
     * 
     * @param chapter Chapter number (1-4)
     * @return Ending level of the chapter, or 0 if invalid
     */
    public static int getChapterEndLevel(int chapter) {
        if (chapter < 1 || chapter > TOTAL_CHAPTERS) {
            return 0;
        }
        return chapter * LEVELS_PER_CHAPTER;
    }
    
    /**
     * Gets a formatted string describing the level structure.
     * 
     * @return Multi-line string describing all levels
     */
    public static String getLevelStructure() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== FORTRON LEVEL STRUCTURE ===\n");
        sb.append(String.format("Total Levels: %d | Total Chapters: %d\n\n", 
                               TOTAL_LEVELS, TOTAL_CHAPTERS));
        
        for (int chapter = 1; chapter <= TOTAL_CHAPTERS; chapter++) {
            sb.append(String.format("--- CHAPTER %d ---\n", chapter));
            int startLevel = getChapterStartLevel(chapter);
            int endLevel = getChapterEndLevel(chapter);
            
            for (int level = startLevel; level <= endLevel; level++) {
                LevelConfig config = getLevelConfig(level);
                if (config != null) {
                    sb.append(String.format("  %s\n", config.toString()));
                }
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Gets the enemy type for a specific chapter.
     * 
     * @param chapter Chapter number (1-4)
     * @return Enemy type for the chapter
     */
    public static EnemyType getChapterEnemyType(int chapter) {
        switch (chapter) {
            case 1: return EnemyType.KOURA;
            case 2: return EnemyType.SARK;
            case 3: return EnemyType.RINZLER;
            case 4: return EnemyType.CLU;
            default: return null;
        }
    }
    
    /**
     * Gets the XP value for a specific enemy type.
     * These values are loaded from monsters.txt.
     * 
     * @param type Enemy type
     * @return XP value for that enemy type
     */
    public static int getEnemyXP(EnemyType type) {
        switch (type) {
            case KOURA: return KOURA_XP;
            case SARK: return SARK_XP;
            case RINZLER: return RINZLER_XP;
            case CLU: return CLU_XP;
            default: return 0;
        }
    }
}
