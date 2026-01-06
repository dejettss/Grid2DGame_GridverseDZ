package model.objects;

import model.progression.EnemyXPReward;
import util.Position;
import util.Direction;
import java.awt.Color;
import java.util.Random;

/**
 * Enemy player with basic AI.
 * Each enemy has an XP value awarded to player upon defeat.
 */
public class EnemyPlayer extends DynamicObject {
    
    private String name;
    private double currentLives;
    private int discsAvailable;
    private int maxDiscs;
    private boolean isAlive;
    private Random random;
    private int moveCounter;
    private int changeDirectionInterval;
    private EnemyXPReward xpReward;
    
    public EnemyPlayer(String name, Position startPosition, Color color, int lives, int maxDiscs) {
        this(name, startPosition, color, lives, maxDiscs, EnemyXPReward.BASIC_ENEMY);
    }
    
    public EnemyPlayer(String name, Position startPosition, Color color, int lives, int maxDiscs, EnemyXPReward xpReward) {
        super(startPosition, color, Direction.DOWN, 1);
        this.name = name;
        this.currentLives = lives;
        this.maxDiscs = maxDiscs;
        this.discsAvailable = maxDiscs;
        this.isAlive = true;
        this.random = new Random();
        this.moveCounter = 0;
        this.changeDirectionInterval = 10 + random.nextInt(20);
        this.xpReward = xpReward;
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
    
    public int getMaxDiscs() {
        return maxDiscs;
    }
    
    @Override
    public void update() {
        moveCounter++;
        
        // Change direction occasionally
        if (moveCounter >= changeDirectionInterval) {
            Direction[] directions = Direction.values();
            direction = directions[random.nextInt(directions.length)];
            moveCounter = 0;
            changeDirectionInterval = 10 + random.nextInt(20);
        }
    }
    
    public Direction getNextDirection() {
        return direction;
    }
    
    public boolean shouldThrowDisc() {
        return random.nextInt(100) < 5 && discsAvailable > 0;
    }
    
    /**
     * Gets the XP reward for defeating this enemy.
     */
    public int getXPReward() {
        return xpReward.getXPReward();
    }
    
    /**
     * Gets the XP reward type.
     */
    public EnemyXPReward getXPRewardType() {
        return xpReward;
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
        return 'E';
    }
    
    @Override
    public String getTypeName() {
        return "Enemy";
    }
    
    @Override
    public boolean causesDerez() {
        return false;
    }
}
