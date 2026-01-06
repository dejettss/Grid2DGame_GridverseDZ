package model.enemy;

import java.awt.Color;
import util.Position;
import util.Direction;
import java.util.*;

/**
 * Sark enemy - Mid-level enemy with moderate stats.
 * Loaded from monsters.txt file.
 * 
 * Difficulty: MEDIUM
 * Intelligence: Moderate - Standard enforcer with basic prediction
 * Movement: Medium speed, predictable handling, basic avoidance
 */
public class Sark extends Enemy {
    
    // AI Strategy constants
    private static final int PREDICTION_STEPS = 2;      // Basic 2-step prediction
    private static final int AVOIDANCE_RANGE = 3;       // Distance to start avoiding
    private static final int MEMORY_SIZE = 5;           // Limited pattern memory
    private static final int DIRECTION_PERSISTENCE = 4; // Frames before direction change
    
    // AI State
    private Direction currentDirection;
    private int framesInCurrentDirection;
    private List<Position> recentPath;
    private EnforcerState state;
    
    // Enforcer states
    public enum EnforcerState {
        PATROL,     // Standard pursuit
        AVOID,      // Collision avoidance
        PURSUE      // Direct chase
    }
    
    /**
     * Constructor that loads Sark's stats from enemy data.
     */
    public Sark(int speed, int handling, int lives, int discs, int xp) {
        super("Sark", Color.YELLOW, speed, handling, lives, discs, xp);
        this.currentDirection = Direction.RIGHT;
        this.framesInCurrentDirection = 0;
        this.recentPath = new ArrayList<>();
        this.state = EnforcerState.PATROL;
    }
    
    /**
     * Calculates the next move using standard enforcer AI.
     * Predictable with basic path prediction and collision avoidance.
     * 
     * @param currentPos Sark's current position
     * @param targetPos Player's current position
     * @param targetVelocity Player's velocity/direction
     * @param jetWalls Positions of all jet walls
     * @return The chosen direction
     */
    public Direction calculateNextMove(Position currentPos, Position targetPos,
                                      Direction targetVelocity, Set<Position> jetWalls) {
        // Update tracking
        updateTracking(currentPos);
        
        // Determine state based on situation
        updateState(currentPos, targetPos, jetWalls);
        
        Direction chosenDirection;
        
        switch (state) {
            case PATROL:
                chosenDirection = patrolMode(currentPos, targetPos, targetVelocity);
                break;
            case AVOID:
                chosenDirection = avoidMode(currentPos, targetPos, jetWalls);
                break;
            case PURSUE:
                chosenDirection = pursueMode(currentPos, targetPos, targetVelocity);
                break;
            default:
                chosenDirection = patrolMode(currentPos, targetPos, targetVelocity);
        }
        
        // Apply predictable handling
        chosenDirection = predictableHandling(currentPos, chosenDirection, jetWalls);
        
        // Update direction persistence
        if (chosenDirection == currentDirection) {
            framesInCurrentDirection++;
        } else {
            framesInCurrentDirection = 0;
            currentDirection = chosenDirection;
        }
        
        return chosenDirection;
    }
    
    /**
     * PATROL mode: Standard pursuit with basic prediction.
     */
    private Direction patrolMode(Position currentPos, Position targetPos, 
                                Direction targetVelocity) {
        // Basic 2-step prediction
        Position predictedTarget = basicPredict(targetPos, targetVelocity);
        
        // Standard pathfinding toward predicted position
        return moveToward(currentPos, predictedTarget);
    }
    
    /**
     * AVOID mode: Simple collision avoidance.
     */
    private Direction avoidMode(Position currentPos, Position targetPos,
                               Set<Position> jetWalls) {
        // Check all four directions for safety
        List<Direction> safeDirections = new ArrayList<>();
        
        for (Direction dir : Direction.values()) {
            Position nextPos = currentPos.move(dir);
            if (!jetWalls.contains(nextPos) && !isInRecentPath(nextPos)) {
                safeDirections.add(dir);
            }
        }
        
        if (safeDirections.isEmpty()) {
            // No safe direction - pick least dangerous
            return currentDirection.opposite();
        }
        
        // Choose safe direction closest to target
        Direction best = safeDirections.get(0);
        int bestDist = Integer.MAX_VALUE;
        
        for (Direction dir : safeDirections) {
            Position nextPos = currentPos.move(dir);
            int dist = manhattanDistance(nextPos, targetPos);
            if (dist < bestDist) {
                bestDist = dist;
                best = dir;
            }
        }
        
        return best;
    }
    
    /**
     * PURSUE mode: Direct chase without much strategy.
     */
    private Direction pursueMode(Position currentPos, Position targetPos,
                                Direction targetVelocity) {
        // Simple direct pursuit
        return moveToward(currentPos, targetPos);
    }
    
    /**
     * Updates enforcer state based on situation.
     */
    private void updateState(Position currentPos, Position targetPos,
                           Set<Position> jetWalls) {
        // Check for immediate danger
        if (hasNearbyObstacles(currentPos, jetWalls)) {
            state = EnforcerState.AVOID;
            return;
        }
        
        int distance = manhattanDistance(currentPos, targetPos);
        
        // Simple state logic
        if (distance < 8) {
            state = EnforcerState.PURSUE;
        } else {
            state = EnforcerState.PATROL;
        }
    }
    
    /**
     * Basic 2-step prediction of target position.
     */
    private Position basicPredict(Position current, Direction velocity) {
        if (velocity == null) {
            return current;
        }
        
        // Simple 2-step linear prediction
        int dx = velocity.getDx() * PREDICTION_STEPS;
        int dy = velocity.getDy() * PREDICTION_STEPS;
        
        return new Position(current.getX() + dx, current.getY() + dy);
    }
    
    /**
     * Predictable handling - maintains direction for consistency.
     */
    private Direction predictableHandling(Position currentPos, Direction intended,
                                         Set<Position> jetWalls) {
        Position nextPos = currentPos.move(intended);
        
        // Check for collision
        if (jetWalls.contains(nextPos)) {
            // Predictable alternative - try perpendicular first
            Direction alt1 = intended.rotateClockwise();
            Direction alt2 = intended.rotateCounterClockwise();
            
            if (!jetWalls.contains(currentPos.move(alt1))) {
                return alt1;
            } else if (!jetWalls.contains(currentPos.move(alt2))) {
                return alt2;
            } else {
                return intended.opposite();
            }
        }
        
        // Prefer maintaining current direction (predictable)
        if (framesInCurrentDirection < DIRECTION_PERSISTENCE) {
            Position continuePos = currentPos.move(currentDirection);
            if (!jetWalls.contains(continuePos)) {
                return currentDirection;
            }
        }
        
        return intended;
    }
    
    /**
     * Moves toward target position using simple logic.
     */
    private Direction moveToward(Position from, Position to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        
        // Prioritize larger distance axis
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else if (Math.abs(dy) > 0) {
            return dy > 0 ? Direction.DOWN : Direction.UP;
        }
        
        return currentDirection;
    }
    
    /**
     * Checks if there are obstacles nearby.
     */
    private boolean hasNearbyObstacles(Position pos, Set<Position> walls) {
        int obstacleCount = 0;
        
        for (Direction dir : Direction.values()) {
            Position checkPos = pos.move(dir);
            if (walls.contains(checkPos)) {
                obstacleCount++;
            }
        }
        
        // Consider 2 or more adjacent obstacles as "nearby danger"
        return obstacleCount >= 2;
    }
    
    /**
     * Checks if position is in recent path (avoid loops).
     */
    private boolean isInRecentPath(Position pos) {
        return recentPath.contains(pos);
    }
    
    /**
     * Updates tracking data.
     */
    private void updateTracking(Position pos) {
        recentPath.add(pos);
        if (recentPath.size() > MEMORY_SIZE) {
            recentPath.remove(0);
        }
    }
    
    /**
     * Calculates Manhattan distance.
     */
    private int manhattanDistance(Position a, Position b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
    
    /**
     * Gets current enforcer state for debugging.
     */
    public EnforcerState getCurrentState() {
        return state;
    }
    
    /**
     * Gets current direction persistence.
     */
    public int getDirectionPersistence() {
        return framesInCurrentDirection;
    }
    
    @Override
    public void levelUp() {
        // Sark becomes slightly more persistent on level up
        // TODO: Enhance prediction steps and persistence
    }
}
