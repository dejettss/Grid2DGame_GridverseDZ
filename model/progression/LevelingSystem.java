package model.progression;

/**
 * Manages player experience points and leveling.
 * Now uses level-specific XP thresholds from LevelManager.
 */
public class LevelingSystem {
    
    private int currentLevel;
    private int currentXP;
    private int xpToNextLevel;
    private int totalXPEarned;
    
    public LevelingSystem() {
        this.currentLevel = 1;
        this.currentXP = 0;
        this.totalXPEarned = 0;
        updateXPThreshold();
    }
    
    /**
     * Updates the XP threshold based on current level using LevelManager.
     */
    private void updateXPThreshold() {
        // Check if we've completed all levels
        if (LevelManager.isGameComplete(currentLevel)) {
            xpToNextLevel = 0; // No more levels
            return;
        }
        
        // Get the XP threshold for current level from LevelManager
        LevelConfig config = LevelManager.getLevelConfig(currentLevel);
        if (config != null) {
            xpToNextLevel = config.getXpThreshold();
        } else {
            xpToNextLevel = 50; // Fallback default
        }
    }
    
    /**
     * Adds XP and checks for level up.
     * 
     * @param xp Amount of XP to add
     * @return true if player leveled up, false otherwise
     */
    public boolean addXP(int xp) {
        if (xp <= 0) {
            return false;
        }
        
        currentXP += xp;
        totalXPEarned += xp;
        
        // Check if level up occurred
        if (currentXP >= xpToNextLevel) {
            levelUp();
            return true;
        }
        
        return false;
    }
    
    /**
     * Handles level up logic.
     */
    private void levelUp() {
        currentLevel++;
        currentXP -= xpToNextLevel; // Carry over excess XP
        
        // Update XP threshold for new level
        updateXPThreshold();
        
        // If we still have enough XP, level up again (recursive)
        if (currentXP >= xpToNextLevel && xpToNextLevel > 0) {
            levelUp();
        }
    }
    
    /**
     * Gets the current level.
     */
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    /**
     * Gets current XP in this level.
     */
    public int getCurrentXP() {
        return currentXP;
    }
    
    /**
     * Gets XP needed for next level.
     */
    public int getXPToNextLevel() {
        return xpToNextLevel;
    }
    
    /**
     * Sets the XP threshold for the current game level.
     * Used to sync with game level requirements instead of player progression.
     * 
     * @param threshold The XP threshold for this game level
     */
    public void setXPThreshold(int threshold) {
        this.xpToNextLevel = Math.max(1, threshold);
    }
    
    /**
     * Gets total XP earned across all levels.
     */
    public int getTotalXP() {
        return totalXPEarned;
    }
    
    /**
     * Gets progress to next level as percentage (0-100).
     */
    public int getProgressPercentage() {
        return (int)((currentXP * 100.0) / xpToNextLevel);
    }
    
    /**
     * Fills the XP bar to maximum (for level completion visual).
     */
    public void fillXPBar() {
        currentXP = xpToNextLevel;
    }
    
    /**
     * Resets XP to 0 (for new level start).
     */
    public void resetXP() {
        currentXP = 0;
    }
    
    /**
     * Resets the leveling system.
     */
    public void reset() {
        currentLevel = 1;
        currentXP = 0;
        totalXPEarned = 0;
        updateXPThreshold();
    }
    
    /**
     * Gets the current chapter number.
     */
    public int getCurrentChapter() {
        return LevelManager.getChapter(currentLevel);
    }
    
    /**
     * Gets the level within the current chapter (1-4).
     */
    public int getChapterLevel() {
        return LevelManager.getChapterLevel(currentLevel);
    }
    
    /**
     * Gets the current level configuration.
     */
    public LevelConfig getCurrentLevelConfig() {
        return LevelManager.getLevelConfig(currentLevel);
    }
    
    /**
     * Checks if all levels are completed.
     */
    public boolean isGameComplete() {
        return LevelManager.isGameComplete(currentLevel);
    }
}
