package game;

import model.progression.LevelConfig;
import model.progression.LevelManager;
import java.util.*;

/**
 * Complete game engine managing 16 levels across 4 chapters.
 * Handles level progression, game flow, and overall game state.
 * Completely UI-independent - pure game logic.
 */
public class GameEngine {
    
    /**
     * Overall game state enum.
     */
    public enum GamePhase {
        MENU,           // Main menu / Not started
        READY,          // Ready to start level
        PLAYING,        // Currently playing
        LEVEL_COMPLETE, // Level completed successfully
        LEVEL_FAILED,   // Level failed (player died)
        GAME_OVER,      // Game over (quit or too many failures)
        VICTORY         // All 16 levels completed
    }
    
    // Current game phase
    private GamePhase currentPhase;
    
    // Current game state (handles active gameplay)
    private GameState gameState;
    
    // Progression tracking
    private int currentLevel;
    private int highestLevelReached;
    
    // Overall statistics
    private int totalEnemiesDefeated;
    private int totalPlayerDeaths;
    private long totalPlayTime;
    private Map<Integer, Long> levelCompletionTimes;
    private Map<Integer, Integer> levelAttempts;
    
    // Level timing
    private long currentLevelStartTime;
    
    /**
     * Constructor initializes the game engine.
     */
    public GameEngine() {
        this.currentPhase = GamePhase.MENU;
        this.currentLevel = 1;
        this.highestLevelReached = 1;
        this.totalEnemiesDefeated = 0;
        this.totalPlayerDeaths = 0;
        this.totalPlayTime = 0;
        this.levelCompletionTimes = new HashMap<>();
        this.levelAttempts = new HashMap<>();
    }
    
    /**
     * Starts a new game from level 1.
     * Resets all statistics and progression.
     */
    public void startNewGame() {
        currentLevel = 1;
        highestLevelReached = 1;
        totalEnemiesDefeated = 0;
        totalPlayerDeaths = 0;
        totalPlayTime = 0;
        levelCompletionTimes.clear();
        levelAttempts.clear();
        
        currentPhase = GamePhase.READY;
        startLevel(1);
    }
    
    /**
     * Continues game from highest reached level.
     */
    public void continueGame() {
        currentLevel = highestLevelReached;
        currentPhase = GamePhase.READY;
        startLevel(currentLevel);
    }
    
    /**
     * Starts a specific level.
     * 
     * @param levelNumber Level to start (1-16)
     */
    public void startLevel(int levelNumber) {
        if (!LevelManager.isValidLevel(levelNumber)) {
            System.err.println("Invalid level number: " + levelNumber);
            return;
        }
        
        currentLevel = levelNumber;
        
        // Track attempts
        levelAttempts.put(levelNumber, levelAttempts.getOrDefault(levelNumber, 0) + 1);
        
        // Create new game state for this level
        gameState = new GameState();
        gameState.initializeLevel(levelNumber);
        
        // Set phase and timing
        currentPhase = GamePhase.PLAYING;
        currentLevelStartTime = System.currentTimeMillis();
    }
    
    /**
     * Main update loop - updates current game state.
     * Should be called every frame.
     */
    public void update() {
        if (currentPhase != GamePhase.PLAYING || gameState == null) {
            return;
        }
        
        // Update game state
        gameState.update();
        
        // Check if level ended
        if (gameState.isGameOver()) {
            handleLevelEnd();
        }
    }
    
    /**
     * Handles level completion or failure.
     */
    private void handleLevelEnd() {
        if (gameState == null) {
            return;
        }
        
        // Calculate level completion time
        long levelTime = System.currentTimeMillis() - currentLevelStartTime;
        totalPlayTime += levelTime;
        
        // Update statistics
        totalEnemiesDefeated += gameState.getEnemiesDefeatedThisLevel();
        totalPlayerDeaths += gameState.getPlayerDeathsThisLevel();
        
        if (gameState.isPlayerWon()) {
            // Level completed successfully
            handleLevelComplete(levelTime);
        } else {
            // Level failed
            handleLevelFailed();
        }
    }
    
    /**
     * Handles successful level completion.
     */
    private void handleLevelComplete(long levelTime) {
        currentPhase = GamePhase.LEVEL_COMPLETE;
        
        // Record completion time
        levelCompletionTimes.put(currentLevel, levelTime);
        
        // Update highest level reached
        if (currentLevel >= highestLevelReached) {
            highestLevelReached = currentLevel + 1;
        }
        
        // Check if game is complete (all 16 levels)
        if (currentLevel >= LevelManager.TOTAL_LEVELS) {
            currentPhase = GamePhase.VICTORY;
        }
    }
    
    /**
     * Handles level failure.
     */
    private void handleLevelFailed() {
        currentPhase = GamePhase.LEVEL_FAILED;
    }
    
    /**
     * Advances to the next level.
     * Only works if current level is complete.
     */
    public void nextLevel() {
        if (currentPhase != GamePhase.LEVEL_COMPLETE) {
            return;
        }
        
        if (currentLevel < LevelManager.TOTAL_LEVELS) {
            currentLevel++;
            startLevel(currentLevel);
        } else {
            currentPhase = GamePhase.VICTORY;
        }
    }
    
    /**
     * Restarts the current level.
     */
    public void restartLevel() {
        startLevel(currentLevel);
    }
    
    /**
     * Returns to main menu.
     */
    public void returnToMenu() {
        currentPhase = GamePhase.MENU;
        gameState = null;
    }
    
    /**
     * Quits the current game.
     */
    public void quitGame() {
        if (gameState != null) {
            gameState.exitGame();
        }
        currentPhase = GamePhase.GAME_OVER;
    }
    
    /**
     * Gets the current level configuration.
     */
    public LevelConfig getCurrentLevelConfig() {
        return LevelManager.getLevelConfig(currentLevel);
    }
    
    /**
     * Gets current chapter number.
     */
    public int getCurrentChapter() {
        return LevelManager.getChapter(currentLevel);
    }
    
    /**
     * Gets current chapter-relative level (1-4).
     */
    public int getCurrentChapterLevel() {
        return LevelManager.getChapterLevel(currentLevel);
    }
    
    /**
     * Gets level completion percentage.
     */
    public int getCompletionPercentage() {
        int completedLevels = highestLevelReached - 1;
        return (completedLevels * 100) / LevelManager.TOTAL_LEVELS;
    }
    
    /**
     * Checks if a specific level has been completed.
     */
    public boolean isLevelCompleted(int levelNumber) {
        return levelNumber < highestLevelReached;
    }
    
    /**
     * Gets completion time for a specific level.
     */
    public long getLevelCompletionTime(int levelNumber) {
        return levelCompletionTimes.getOrDefault(levelNumber, 0L);
    }
    
    /**
     * Gets attempt count for a specific level.
     */
    public int getLevelAttempts(int levelNumber) {
        return levelAttempts.getOrDefault(levelNumber, 0);
    }
    
    /**
     * Formats time in milliseconds to readable string.
     */
    public static String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format("%d:%02d", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    /**
     * Gets overall game statistics as formatted string.
     */
    public String getStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════ GAME STATISTICS ═══════════════\n");
        sb.append(String.format("Progress: Level %d/%d (Chapter %d)\n", 
                  currentLevel, LevelManager.TOTAL_LEVELS, getCurrentChapter()));
        sb.append(String.format("Completion: %d%%\n", getCompletionPercentage()));
        sb.append(String.format("Enemies Defeated: %d\n", totalEnemiesDefeated));
        sb.append(String.format("Deaths: %d\n", totalPlayerDeaths));
        sb.append(String.format("Total Play Time: %s\n", formatTime(totalPlayTime)));
        sb.append("════════════════════════════════════════════════");
        return sb.toString();
    }
    
    /**
     * Gets chapter progress summary.
     */
    public String getChapterProgress() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════ CHAPTER PROGRESS ═══════════════\n");
        
        for (int chapter = 1; chapter <= LevelManager.TOTAL_CHAPTERS; chapter++) {
            int startLevel = LevelManager.getChapterStartLevel(chapter);
            int endLevel = LevelManager.getChapterEndLevel(chapter);
            int completed = 0;
            
            for (int level = startLevel; level <= endLevel; level++) {
                if (isLevelCompleted(level)) {
                    completed++;
                }
            }
            
            String status = completed == 4 ? "✓" : (completed > 0 ? "●" : "○");
            sb.append(String.format("Chapter %d: %s [%d/4 levels]\n", 
                      chapter, status, completed));
        }
        
        sb.append("════════════════════════════════════════════════");
        return sb.toString();
    }
    
    /**
     * Gets victory screen text.
     */
    public String getVictoryScreen() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("████████████████████████████████████████████████\n");
        sb.append("█                                              █\n");
        sb.append("█           ★ VICTORY ACHIEVED ★               █\n");
        sb.append("█                                              █\n");
        sb.append("█        ALL 16 LEVELS CONQUERED!              █\n");
        sb.append("█          THE GRID IS LIBERATED!              █\n");
        sb.append("█                                              █\n");
        sb.append("████████████████████████████████████████████████\n");
        sb.append("\n");
        sb.append("╔════════════════════════════════════════════════╗\n");
        sb.append("║              FINAL STATISTICS                  ║\n");
        sb.append("╠════════════════════════════════════════════════╣\n");
        sb.append(String.format("║ Chapters Completed:        4/4                 ║\n"));
        sb.append(String.format("║ Levels Conquered:         16/16                ║\n"));
        sb.append(String.format("║ Total Enemies Defeated:   %-20d ║\n", totalEnemiesDefeated));
        sb.append(String.format("║ Total Deaths:             %-20d ║\n", totalPlayerDeaths));
        sb.append(String.format("║ Total Time:               %-20s ║\n", formatTime(totalPlayTime)));
        sb.append("╚════════════════════════════════════════════════╝\n");
        sb.append("\n");
        sb.append("         You are the ultimate Grid warrior!\n");
        sb.append("\n");
        
        return sb.toString();
    }
    
    // ==================== GETTERS ====================
    
    public GamePhase getCurrentPhase() {
        return currentPhase;
    }
    
    public GameState getGameState() {
        return gameState;
    }
    
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    public int getHighestLevelReached() {
        return highestLevelReached;
    }
    
    public int getTotalEnemiesDefeated() {
        return totalEnemiesDefeated;
    }
    
    public int getTotalPlayerDeaths() {
        return totalPlayerDeaths;
    }
    
    public long getTotalPlayTime() {
        return totalPlayTime;
    }
    
    public Map<Integer, Long> getLevelCompletionTimes() {
        return Collections.unmodifiableMap(levelCompletionTimes);
    }
    
    public Map<Integer, Integer> getLevelAttempts() {
        return Collections.unmodifiableMap(levelAttempts);
    }
}
