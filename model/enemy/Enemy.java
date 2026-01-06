package model.enemy;

import java.awt.Color;

/**
 * Represents an AI-controlled enemy in the Tron game.
 * Enemy data is loaded from external file (monsters.txt).
 */
public class Enemy {
    private String name;
    private Color color;
    private int speed;
    private int handling;
    private int lives;
    private int discs;
    private int xp;
    
    /**
     * Constructor for creating an enemy.
     */
    public Enemy(String name, Color color, int speed, int handling, int lives, int discs, int xp) {
        this.name = name;
        this.color = color;
        this.speed = speed;
        this.handling = handling;
        this.lives = lives;
        this.discs = discs;
        this.xp = xp;
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public Color getColor() {
        return color;
    }
    
    public int getSpeed() {
        return speed;
    }
    
    public int getHandling() {
        return handling;
    }
    
    public int getLives() {
        return lives;
    }
    
    public int getDiscs() {
        return discs;
    }
    
    public int getXp() {
        return xp;
    }
    
    // Setters
    public void setLives(int lives) {
        this.lives = lives;
    }
    
    public void setDiscs(int discs) {
        this.discs = discs;
    }
    
    public void setXp(int xp) {
        this.xp = xp;
    }
    
    /**
     * Increases enemy's experience points.
     */
    public void addXp(int amount) {
        this.xp += amount;
    }
    
    /**
     * Decreases enemy's lives.
     */
    public void loseLife() {
        if (lives > 0) {
            lives--;
        }
    }
    
    /**
     * Increases enemy's discs.
     */
    public void addDisc() {
        discs++;
    }
    
    /**
     * Decreases enemy's discs.
     */
    public void useDisc() {
        if (discs > 0) {
            discs--;
        }
    }
    
    /**
     * Checks if enemy is still alive.
     */
    public boolean isAlive() {
        return lives > 0;
    }
    
    /**
     * Levels up the enemy.
     * Can be overridden by specific enemy types.
     */
    public void levelUp() {
        // TODO: Implement level up logic
        // Possible implementations:
        // - Increase speed
        // - Increase handling
        // - Add extra lives
        // - Add discs
        // - Enhanced AI behavior
    }
    
    @Override
    public String toString() {
        return String.format("%s [Speed: %d, Handling: %d, Lives: %d, Discs: %d, XP: %d]",
                name, speed, handling, lives, discs, xp);
    }
    
    /**
     * Returns a detailed string representation.
     */
    public String toDetailedString() {
        return String.format(
            "Enemy: %s\n" +
            "  Color: %s\n" +
            "  Speed: %d\n" +
            "  Handling: %d\n" +
            "  Lives: %d\n" +
            "  Discs: %d\n" +
            "  XP: %d",
            name, getColorName(), speed, handling, lives, discs, xp
        );
    }
    
    /**
     * Gets the color name as a string.
     */
    private String getColorName() {
        if (color.equals(Color.GREEN)) return "Green";
        if (color.equals(Color.YELLOW)) return "Yellow";
        if (color.equals(Color.RED)) return "Red";
        if (color.equals(new Color(255, 215, 0))) return "Gold";
        return "Custom";
    }
}
