package model.objects;

import model.progression.LevelingSystem;
import util.Position;
import util.Direction;
import java.awt.Color;

/**
 * Player character in the game.
 * Supports leveling system with character-specific rewards.
 */
public class Player extends DynamicObject {
    
    public enum PlayerType {
        TRON,   // Gets +1 disc per level
        KEVIN   // Gets +1 life per level
    }
    
    private String name;
    private PlayerType playerType;
    private int maxLives;
    private double currentLives;
    private int discsAvailable;
    private int maxDiscs;
    private boolean isAlive;
    private LevelingSystem levelingSystem;
    
    public Player(String name, Position startPosition, Color color, int lives, int maxDiscs) {
        this(name, PlayerType.TRON, startPosition, color, lives, maxDiscs);
    }
    
    public Player(String name, PlayerType type, Position startPosition, Color color, int lives, int maxDiscs) {
        super(startPosition, color, Direction.UP, 1);
        this.name = name;
        this.playerType = type;
        this.maxLives = lives;
        this.currentLives = lives;
        this.maxDiscs = maxDiscs;
        this.discsAvailable = maxDiscs;
        this.isAlive = true;
        this.levelingSystem = new LevelingSystem();
    }
    
    public String getName() {
        return name;
    }
    
    public double getCurrentLives() {
        return currentLives;
    }
    
    public int getCurrentLivesInt() {
        return (int) Math.ceil(currentLives);
    }
    
    public void setCurrentLives(double lives) {
        this.currentLives = Math.max(0.0, lives);
        this.isAlive = this.currentLives > 0.0;
    }
    
    public int getDiscsAvailable() {
        return discsAvailable;
    }
    
    public boolean isAlive() {
        return isAlive;
    }
    
    public int getMaxLives() {
        return maxLives;
    }
    
    public int getMaxDiscs() {
        return maxDiscs;
    }
    
    /**
     * Adds XP and handles level up with rewards.
     * Both Tron and Kevin get: +1 life AND +1 disc per level
     * 
     * @param xp Amount of XP to add
     * @return true if player leveled up
     */
    public boolean addXP(int xp) {
        boolean leveledUp = levelingSystem.addXP(xp);
        
        if (leveledUp) {
            // Both character types get +1 life AND +1 disc
            maxLives++;
            currentLives++;
            maxDiscs++;
            discsAvailable++;
        }
        
        return leveledUp;
    }
    
    /**
     * Gets the leveling system.
     */
    public LevelingSystem getLevelingSystem() {
        return levelingSystem;
    }
    
    /**
     * Gets the player type.
     */
    public PlayerType getPlayerType() {
        return playerType;
    }
    
    /**
     * Gets current level.
     */
    public int getLevel() {
        return levelingSystem.getCurrentLevel();
    }
    
    @Override
    public void update() {
        // Player update handled by input
    }
    
    @Override
    public void onCollision(GameObject other) {
        // Handled by CollisionManager
    }
    
    @Override
    public boolean isTraversable() {
        return false;
    }
    
    @Override
    public char getSymbol() {
        return 'P';
    }
    
    @Override
    public String getTypeName() {
        return "Player";
    }
    
    @Override
    public boolean causesDerez() {
        return false;
    }
}
