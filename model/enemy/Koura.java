package model.enemy;

import java.awt.Color;
import util.Position;
import util.Direction;
import java.util.*;

/**
 * Koura enemy - Low-level enemy with basic stats.
 * Loaded from monsters.txt file.
 * 
 * Difficulty: EASY
 * Intelligence: Low - Weak AI with random, erratic movement
 * Movement: Slow speed, erratic handling, minimal avoidance
 */
public class Koura extends Enemy {
    
    // AI Strategy constants
    private static final double RANDOM_FACTOR = 0.6;    // 60% random movement
    private static final int MAX_SAME_DIRECTION = 3;    // Changes direction frequently
    private static final double PANIC_THRESHOLD = 4.0;  // Distance to start panicking
    
    // AI State
    private Direction currentDirection;
    private int movesInCurrentDirection;
    private Random random;
    private boolean isPanicking;
    
    /**
     * Constructor that loads Koura's stats from enemy data.
     */
    public Koura(int speed, int handling, int lives, int discs, int xp) {
        super("Koura", Color.GREEN, speed, handling, lives, discs, xp);
        this.random = new Random();
        this.currentDirection = randomDirection();
        this.movesInCurrentDirection = 0;
        this.isPanicking = false;
    }
    
    /**
     * Calculates the next move using weak, erratic AI.
     * Random movement with minimal strategy or avoidance.
     * 
     * @param currentPos Koura's current position
     * @param targetPos Player's current position
     * @param jetWalls Positions of all jet walls
     * @return The chosen direction (mostly random)
     */
    public Direction calculateNextMove(Position currentPos, Position targetPos,
                                      Set<Position> jetWalls) {
        Direction chosenDirection;
        
        // Check if player is too close (panic mode)
        double distance = manhattanDistance(currentPos, targetPos);
        isPanicking = distance < PANIC_THRESHOLD;
        
        if (isPanicking) {
            // Panic! Try to run away (but still somewhat random)
            chosenDirection = panicMode(currentPos, targetPos);
        } else {
            // Normal erratic movement
            chosenDirection = erraticMovement(currentPos, targetPos);
        }
        
        // Erratic handling - might change direction randomly
        chosenDirection = erraticHandling(currentPos, chosenDirection, jetWalls);
        
        // Update state
        if (chosenDirection == currentDirection) {
            movesInCurrentDirection++;
        } else {
            movesInCurrentDirection = 0;
            currentDirection = chosenDirection;
        }
        
        // Force direction change if stuck too long (erratic behavior)
        if (movesInCurrentDirection >= MAX_SAME_DIRECTION) {
            currentDirection = randomDirection();
            movesInCurrentDirection = 0;
            chosenDirection = currentDirection;
        }
        
        return chosenDirection;
    }
    
    /**
     * Erratic movement - mostly random with slight bias toward target.
     */
    private Direction erraticMovement(Position currentPos, Position targetPos) {
        // 60% random, 40% toward target
        if (random.nextDouble() < RANDOM_FACTOR) {
            return randomDirection();
        } else {
            // Weak attempt to move toward target
            return weakPursuit(currentPos, targetPos);
        }
    }
    
    /**
     * Panic mode - tries to run away but still erratic.
     */
    private Direction panicMode(Position currentPos, Position targetPos) {
        // Try to move away, but 40% chance of random panic movement
        if (random.nextDouble() < 0.4) {
            // Panic causes random movement
            return randomDirection();
        } else {
            // Try to flee (move away from player)
            return moveAway(currentPos, targetPos);
        }
    }
    
    /**
     * Weak pursuit - very basic pathfinding with errors.
     */
    private Direction weakPursuit(Position currentPos, Position targetPos) {
        int dx = targetPos.getX() - currentPos.getX();
        int dy = targetPos.getY() - currentPos.getY();
        
        // Sometimes gets confused and moves on wrong axis
        if (random.nextDouble() < 0.3) {
            // Confused! Swap axes
            int temp = dx;
            dx = dy;
            dy = temp;
        }
        
        // Move on primary axis (with possible confusion)
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else if (Math.abs(dy) > 0) {
            return dy > 0 ? Direction.DOWN : Direction.UP;
        } else {
            return currentDirection;
        }
    }
    
    /**
     * Moves away from target (used in panic mode).
     */
    private Direction moveAway(Position currentPos, Position targetPos) {
        int dx = targetPos.getX() - currentPos.getX();
        int dy = targetPos.getY() - currentPos.getY();
        
        // Flee on primary axis
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direction.LEFT : Direction.RIGHT;
        } else if (Math.abs(dy) > 0) {
            return dy > 0 ? Direction.UP : Direction.DOWN;
        } else {
            return randomDirection();
        }
    }
    
    /**
     * Erratic handling - minimal collision avoidance, random changes.
     */
    private Direction erraticHandling(Position currentPos, Direction intended,
                                     Set<Position> jetWalls) {
        Position nextPos = currentPos.move(intended);
        
        // Check for immediate collision
        if (jetWalls.contains(nextPos)) {
            // Minimal avoidance - just pick a random different direction
            List<Direction> alternatives = new ArrayList<>();
            
            for (Direction dir : Direction.values()) {
                if (dir != intended && !jetWalls.contains(currentPos.move(dir))) {
                    alternatives.add(dir);
                }
            }
            
            if (!alternatives.isEmpty()) {
                // Pick random alternative
                return alternatives.get(random.nextInt(alternatives.size()));
            } else {
                // No safe move - just turn around
                return intended.opposite();
            }
        }
        
        // 20% chance to randomly change direction (erratic!)
        if (random.nextDouble() < 0.2) {
            Direction randomDir = randomDirection();
            if (!jetWalls.contains(currentPos.move(randomDir))) {
                return randomDir;
            }
        }
        
        return intended;
    }
    
    /**
     * Returns a random direction.
     */
    private Direction randomDirection() {
        Direction[] directions = Direction.values();
        return directions[random.nextInt(directions.length)];
    }
    
    /**
     * Calculates Manhattan distance.
     */
    private int manhattanDistance(Position a, Position b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
    
    /**
     * Checks if Koura is currently panicking.
     */
    public boolean isPanicking() {
        return isPanicking;
    }
    
    /**
     * Gets current direction for debugging.
     */
    public Direction getCurrentDirection() {
        return currentDirection;
    }
    
    /**
     * Gets moves in current direction.
     */
    public int getMovesInCurrentDirection() {
        return movesInCurrentDirection;
    }
    
    @Override
    public void levelUp() {
        // Koura doesn't improve much on level up (weak AI)
        // TODO: Slight reduction in random factor
    }
}
