package model.character;

import java.awt.Color;

/**
 * Represents a playable character in the Tron game.
 * Character data is loaded from external file (characters.txt).
 */
public class Character {
    private String name;
    private Color color;
    private int speed;
    private int handling;
    private int lives;
    private int discs;
    private int xp;
    
    /**
     * Constructor for creating a character.
     */
    public Character(String name, Color color, int speed, int handling, int lives, int discs, int xp) {
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
     * Increases character's experience points.
     */
    public void addXp(int amount) {
        this.xp += amount;
    }
    
    /**
     * Decreases character's lives.
     */
    public void loseLife() {
        if (lives > 0) {
            lives--;
        }
    }
    
    /**
     * Increases character's discs.
     */
    public void addDisc() {
        discs++;
    }
    
    /**
     * Decreases character's discs.
     */
    public void useDisc() {
        if (discs > 0) {
            discs--;
        }
    }
    
    /**
     * Checks if character is still alive.
     */
    public boolean isAlive() {
        return lives > 0;
    }
    
    /**
     * Levels up the character.
     * Implementation will be added later based on game mechanics.
     */
    public void levelUp() {
        
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
            "Character: %s\n" +
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
        if (color.equals(Color.BLUE)) return "Blue";
        if (color.equals(Color.WHITE)) return "White";
        return "Custom";
    }
}
