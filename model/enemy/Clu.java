package model.enemy;

import java.awt.Color;
import util.Position;
import util.Direction;
import java.util.*;

/**
 * Clu enemy - Boss-level enemy with superior stats.
 * Loaded from monsters.txt file.
 * 
 * Difficulty: IMPOSSIBLE
 * Intelligence: Brilliant - Corrupted intelligence with strategic, unpredictable behavior
 * Movement: Very fast, aggressive, capable of logical yet non-deterministic decisions
 */
public class Clu extends Enemy {
    
    // Gold color (255, 215, 0)
    private static final Color GOLD = new Color(255, 215, 0);
    
    // AI Strategy constants
    private static final int PREDICTION_DEPTH = 5;      // How far ahead Clu predicts
    private static final int TRAP_RADIUS = 8;           // Distance for trap setting
    private static final double AGGRESSION_FACTOR = 0.8; // 80% aggressive, 20% unpredictable
    private static final int MEMORY_SIZE = 20;          // Remembers last 20 moves
    
    // AI State
    private List<Position> moveHistory;
    private Direction lastDirection;
    private int movesSinceDirectionChange;
    private Random strategicRandom;
    private AIMode currentMode;
    
    // AI Modes for unpredictable behavior
    public enum AIMode {
        HUNT,           // Direct pursuit with prediction
        FLANK,          // Circle around to cut off escape
        TRAP,           // Set up strategic trap
        BAIT,           // Fake retreat to lure player
        CHAOS           // Unpredictable movement
    }
    
    /**
     * Constructor that loads Clu's stats from enemy data.
     */
    public Clu(int speed, int handling, int lives, int discs, int xp) {
        super("Clu", GOLD, speed, handling, lives, discs, xp);
        this.moveHistory = new ArrayList<>();
        this.lastDirection = Direction.RIGHT;
        this.movesSinceDirectionChange = 0;
        this.strategicRandom = new Random();
        this.currentMode = AIMode.HUNT;
    }
    
    /**
     * Calculates the next move using brilliant AI strategy.
     * Combines logical pathfinding with unpredictable tactical decisions.
     * 
     * @param currentPos Clu's current position
     * @param targetPos Player's current position
     * @param targetVelocity Player's velocity/direction
     * @param arena The game arena for collision detection
     * @param jetWalls Positions of all jet walls
     * @return The optimal direction to move
     */
    public Direction calculateNextMove(Position currentPos, Position targetPos, 
                                      Direction targetVelocity, Object arena, 
                                      Set<Position> jetWalls) {
        // Update move history
        updateMoveHistory(currentPos);
        
        // Adaptive mode switching - unpredictable yet logical
        updateAIMode(currentPos, targetPos, jetWalls);
        
        Direction chosenDirection;
        
        switch (currentMode) {
            case HUNT:
                chosenDirection = huntMode(currentPos, targetPos, targetVelocity, jetWalls);
                break;
            case FLANK:
                chosenDirection = flankMode(currentPos, targetPos, targetVelocity, jetWalls);
                break;
            case TRAP:
                chosenDirection = trapMode(currentPos, targetPos, jetWalls);
                break;
            case BAIT:
                chosenDirection = baitMode(currentPos, targetPos, jetWalls);
                break;
            case CHAOS:
                chosenDirection = chaosMode(currentPos, jetWalls);
                break;
            default:
                chosenDirection = huntMode(currentPos, targetPos, targetVelocity, jetWalls);
        }
        
        // Validate and apply aggressive handling
        chosenDirection = validateAndRefine(currentPos, chosenDirection, jetWalls);
        
        // Update state
        if (chosenDirection != lastDirection) {
            movesSinceDirectionChange = 0;
        } else {
            movesSinceDirectionChange++;
        }
        lastDirection = chosenDirection;
        
        return chosenDirection;
    }
    
    /**
     * HUNT mode: Aggressive pursuit with predictive targeting.
     */
    private Direction huntMode(Position currentPos, Position targetPos, 
                              Direction targetVelocity, Set<Position> jetWalls) {
        // Predict where the player will be
        Position predictedPos = predictTargetPosition(targetPos, targetVelocity, PREDICTION_DEPTH);
        
        // Calculate direction to predicted position
        int dx = predictedPos.getX() - currentPos.getX();
        int dy = predictedPos.getY() - currentPos.getY();
        
        // Prioritize the axis with greater distance (aggressive closing)
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else if (Math.abs(dy) > 0) {
            return dy > 0 ? Direction.DOWN : Direction.UP;
        }
        
        return lastDirection;
    }
    
    /**
     * FLANK mode: Circle around to cut off escape routes.
     */
    private Direction flankMode(Position currentPos, Position targetPos, 
                               Direction targetVelocity, Set<Position> jetWalls) {
        // Calculate perpendicular approach
        int dx = targetPos.getX() - currentPos.getX();
        int dy = targetPos.getY() - currentPos.getY();
        
        // Move perpendicular to the direct line, then close in
        boolean movePerpendicularly = strategicRandom.nextDouble() < 0.6;
        
        if (movePerpendicularly) {
            // Move perpendicular to intercept escape route
            if (Math.abs(dx) > Math.abs(dy)) {
                return dy >= 0 ? Direction.DOWN : Direction.UP;
            } else {
                return dx >= 0 ? Direction.RIGHT : Direction.LEFT;
            }
        } else {
            // Close in from the side
            return huntMode(currentPos, targetPos, targetVelocity, jetWalls);
        }
    }
    
    /**
     * TRAP mode: Position to create inescapable situations.
     */
    private Direction trapMode(Position currentPos, Position targetPos, Set<Position> jetWalls) {
        // Find position that maximizes player's danger
        Direction[] directions = Direction.values();
        Direction bestDir = lastDirection;
        int maxTrapScore = 0;
        
        for (Direction dir : directions) {
            Position nextPos = currentPos.move(dir);
            int trapScore = calculateTrapScore(nextPos, targetPos, jetWalls);
            
            if (trapScore > maxTrapScore) {
                maxTrapScore = trapScore;
                bestDir = dir;
            }
        }
        
        return bestDir;
    }
    
    /**
     * BAIT mode: Fake retreat to lure player into trap.
     */
    private Direction baitMode(Position currentPos, Position targetPos, Set<Position> jetWalls) {
        // Move away from player to create false sense of safety
        int dx = currentPos.getX() - targetPos.getX();
        int dy = currentPos.getY() - targetPos.getY();
        
        // But position for sudden reversal
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else if (Math.abs(dy) > 0) {
            return dy > 0 ? Direction.DOWN : Direction.UP;
        }
        
        return lastDirection;
    }
    
    /**
     * CHAOS mode: Unpredictable but not random - uses complex patterns.
     */
    private Direction chaosMode(Position currentPos, Set<Position> jetWalls) {
        // Non-deterministic but logical - based on position hash
        int positionHash = currentPos.hashCode();
        int timeComponent = (int) (System.currentTimeMillis() / 1000);
        int seed = positionHash ^ timeComponent;
        
        Direction[] directions = Direction.values();
        Direction choice = directions[Math.abs(seed) % directions.length];
        
        // Ensure it's not a suicide move
        Position nextPos = currentPos.move(choice);
        if (jetWalls.contains(nextPos)) {
            // Try perpendicular directions
            choice = choice.rotateClockwise();
        }
        
        return choice;
    }
    
    /**
     * Updates AI mode based on tactical situation.
     */
    private void updateAIMode(Position currentPos, Position targetPos, Set<Position> jetWalls) {
        int distance = manhattanDistance(currentPos, targetPos);
        double randomFactor = strategicRandom.nextDouble();
        
        // Strategic mode selection with unpredictability
        if (distance < 5 && randomFactor < AGGRESSION_FACTOR) {
            currentMode = AIMode.HUNT;
        } else if (distance < 10 && randomFactor < 0.5) {
            currentMode = AIMode.FLANK;
        } else if (distance < TRAP_RADIUS && randomFactor < 0.7) {
            currentMode = AIMode.TRAP;
        } else if (distance > 15 && randomFactor < 0.3) {
            currentMode = AIMode.BAIT;
        } else if (randomFactor > 0.85) {
            // 15% chance of chaos - unpredictable
            currentMode = AIMode.CHAOS;
        }
        
        // Switch modes if stuck
        if (movesSinceDirectionChange > 10) {
            currentMode = AIMode.CHAOS;
        }
    }
    
    /**
     * Predicts target position based on velocity and time steps.
     */
    private Position predictTargetPosition(Position currentTarget, Direction velocity, int steps) {
        if (velocity == null) {
            return currentTarget;
        }
        
        int predictedX = currentTarget.getX() + (velocity.getDx() * steps);
        int predictedY = currentTarget.getY() + (velocity.getDy() * steps);
        
        return new Position(predictedX, predictedY);
    }
    
    /**
     * Calculates how well a position contributes to trapping the player.
     */
    private int calculateTrapScore(Position pos, Position targetPos, Set<Position> jetWalls) {
        int score = 0;
        
        // Closer to player increases score
        int distance = manhattanDistance(pos, targetPos);
        score += (20 - distance);
        
        // Being near jet walls increases trap potential
        for (Direction dir : Direction.values()) {
            if (jetWalls.contains(pos.move(dir))) {
                score += 3;
            }
        }
        
        return score;
    }
    
    /**
     * Validates move and applies aggressive refinements.
     */
    private Direction validateAndRefine(Position currentPos, Direction direction, 
                                       Set<Position> jetWalls) {
        Position nextPos = currentPos.move(direction);
        
        // Check if move leads to collision
        if (jetWalls.contains(nextPos)) {
            // Aggressive handling - try all other directions
            for (Direction altDir : Direction.values()) {
                Position altPos = currentPos.move(altDir);
                if (!jetWalls.contains(altPos)) {
                    return altDir;
                }
            }
        }
        
        return direction;
    }
    
    /**
     * Calculates Manhattan distance between two positions.
     */
    private int manhattanDistance(Position a, Position b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
    
    /**
     * Updates move history for pattern analysis.
     */
    private void updateMoveHistory(Position pos) {
        moveHistory.add(pos);
        if (moveHistory.size() > MEMORY_SIZE) {
            moveHistory.remove(0);
        }
    }
    
    /**
     * Gets the current AI mode for debugging/display.
     */
    public AIMode getCurrentMode() {
        return currentMode;
    }
    
    @Override
    public void levelUp() {
        // Clu becomes even more unpredictable and aggressive on level up
        // TODO: Enhance AI parameters on level up
    }
}
