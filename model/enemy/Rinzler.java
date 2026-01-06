package model.enemy;

import java.awt.Color;
import util.Position;
import util.Direction;
import java.util.*;

/**
 * Rinzler enemy - High-level enemy with advanced stats.
 * Loaded from monsters.txt file.
 * 
 * Difficulty: HARD
 * Intelligence: Brilliant - Silent hunter with tactical, adaptive behavior
 * Movement: Very fast, clever handling, capable of limited collaboration
 */
public class Rinzler extends Enemy {
    
    // AI Strategy constants
    private static final int STALKING_DISTANCE = 12;    // Optimal hunting distance
    private static final int STRIKE_DISTANCE = 6;       // Attack range
    private static final int EVASION_DISTANCE = 3;      // Close quarters evasion
    private static final int COLLABORATION_RANGE = 15;  // Range to coordinate with allies
    private static final int PATTERN_MEMORY = 15;       // Pattern recognition depth
    
    // AI State
    private List<Position> targetHistory;               // Track player patterns
    private List<Position> pathHistory;                 // Own movement history
    private Direction currentDirection;
    private HunterMode mode;
    private Position lastKnownTarget;
    private int stalkerPatience;                        // Frames willing to stalk
    
    // Hunter modes for tactical adaptation
    public enum HunterMode {
        STALK,          // Maintain distance, observe patterns
        STRIKE,         // Close in for attack
        EVADE,          // Tactical retreat and repositioning
        FLANK,          // Circle to advantageous position
        COORDINATE      // Team up with nearby enemies
    }
    
    /**
     * Constructor that loads Rinzler's stats from enemy data.
     */
    public Rinzler(int speed, int handling, int lives, int discs, int xp) {
        super("Rinzler", Color.RED, speed, handling, lives, discs, xp);
        this.targetHistory = new ArrayList<>();
        this.pathHistory = new ArrayList<>();
        this.currentDirection = Direction.DOWN;
        this.mode = HunterMode.STALK;
        this.stalkerPatience = 0;
    }
    
    /**
     * Calculates the next move using silent hunter tactics.
     * Highly adaptive with pattern recognition and collaboration.
     * 
     * @param currentPos Rinzler's current position
     * @param targetPos Player's current position
     * @param targetVelocity Player's velocity/direction
     * @param jetWalls Positions of all jet walls
     * @param allies Positions of allied enemies (for coordination)
     * @return The optimal tactical direction
     */
    public Direction calculateNextMove(Position currentPos, Position targetPos,
                                      Direction targetVelocity, Set<Position> jetWalls,
                                      List<Position> allies) {
        // Update tracking data
        updateTracking(currentPos, targetPos);
        
        // Analyze tactical situation
        int distance = manhattanDistance(currentPos, targetPos);
        adaptMode(distance, targetPos, allies);
        
        Direction chosenDirection;
        
        switch (mode) {
            case STALK:
                chosenDirection = stalkMode(currentPos, targetPos, targetVelocity, jetWalls);
                break;
            case STRIKE:
                chosenDirection = strikeMode(currentPos, targetPos, targetVelocity, jetWalls);
                break;
            case EVADE:
                chosenDirection = evadeMode(currentPos, targetPos, jetWalls);
                break;
            case FLANK:
                chosenDirection = flankMode(currentPos, targetPos, jetWalls);
                break;
            case COORDINATE:
                chosenDirection = coordinateMode(currentPos, targetPos, allies, jetWalls);
                break;
            default:
                chosenDirection = stalkMode(currentPos, targetPos, targetVelocity, jetWalls);
        }
        
        // Clever handling - validate and optimize
        chosenDirection = cleverHandling(currentPos, chosenDirection, targetPos, jetWalls);
        
        currentDirection = chosenDirection;
        return chosenDirection;
    }
    
    /**
     * STALK mode: Maintain optimal hunting distance, observe patterns.
     * Silent approach - doesn't rush in.
     */
    private Direction stalkMode(Position currentPos, Position targetPos,
                               Direction targetVelocity, Set<Position> jetWalls) {
        stalkerPatience++;
        
        int distance = manhattanDistance(currentPos, targetPos);
        
        // Maintain stalking distance
        if (distance > STALKING_DISTANCE) {
            // Close in slowly
            return moveToward(currentPos, targetPos, 0.7);
        } else if (distance < STALKING_DISTANCE - 2) {
            // Back off to maintain distance
            return moveAway(currentPos, targetPos);
        } else {
            // Perfect stalking distance - circle around
            return circlePath(currentPos, targetPos);
        }
    }
    
    /**
     * STRIKE mode: Aggressive close-in attack.
     * Uses pattern prediction for optimal intercept.
     */
    private Direction strikeMode(Position currentPos, Position targetPos,
                                Direction targetVelocity, Set<Position> jetWalls) {
        // Predict target movement based on pattern history
        Position predictedPos = predictTargetMovement(targetPos, targetVelocity);
        
        // Direct aggressive approach
        return moveToward(currentPos, predictedPos, 1.0);
    }
    
    /**
     * EVADE mode: Tactical retreat and repositioning.
     * Creates distance while maintaining line of sight.
     */
    private Direction evadeMode(Position currentPos, Position targetPos, 
                               Set<Position> jetWalls) {
        // Move away but stay tactical
        Direction awayDir = moveAway(currentPos, targetPos);
        
        // Check if we can use walls for cover
        Position nextPos = currentPos.move(awayDir);
        
        // Prefer directions that use walls as cover
        for (Direction dir : Direction.values()) {
            Position testPos = currentPos.move(dir);
            if (!jetWalls.contains(testPos) && hasWallCover(testPos, targetPos, jetWalls)) {
                return dir;
            }
        }
        
        return awayDir;
    }
    
    /**
     * FLANK mode: Circle to advantageous position.
     * Clever positioning to cut off escape routes.
     */
    private Direction flankMode(Position currentPos, Position targetPos,
                               Set<Position> jetWalls) {
        // Calculate perpendicular vector for flanking
        int dx = targetPos.getX() - currentPos.getX();
        int dy = targetPos.getY() - currentPos.getY();
        
        // Choose flanking direction based on distance
        Direction flankDir;
        if (Math.abs(dx) > Math.abs(dy)) {
            // Flank vertically
            flankDir = dy >= 0 ? Direction.DOWN : Direction.UP;
        } else {
            // Flank horizontally
            flankDir = dx >= 0 ? Direction.RIGHT : Direction.LEFT;
        }
        
        // Alternate flanking direction for unpredictability
        if (pathHistory.size() % 3 == 0) {
            flankDir = flankDir.opposite();
        }
        
        return flankDir;
    }
    
    /**
     * COORDINATE mode: Limited collaboration with other enemies.
     * Adjusts position based on ally locations for pincer attacks.
     */
    private Direction coordinateMode(Position currentPos, Position targetPos,
                                    List<Position> allies, Set<Position> jetWalls) {
        if (allies == null || allies.isEmpty()) {
            return strikeMode(currentPos, targetPos, null, jetWalls);
        }
        
        // Find nearest ally
        Position nearestAlly = findNearestAlly(currentPos, allies);
        if (nearestAlly == null) {
            return strikeMode(currentPos, targetPos, null, jetWalls);
        }
        
        // Calculate pincer position
        // Rinzler and ally should attack from different angles
        int allyToTargetDx = targetPos.getX() - nearestAlly.getX();
        int allyToTargetDy = targetPos.getY() - nearestAlly.getY();
        
        // Move to opposite side of target from ally
        Direction oppositeDir;
        if (Math.abs(allyToTargetDx) > Math.abs(allyToTargetDy)) {
            oppositeDir = allyToTargetDx > 0 ? Direction.LEFT : Direction.RIGHT;
        } else {
            oppositeDir = allyToTargetDy > 0 ? Direction.UP : Direction.DOWN;
        }
        
        // Move toward target from opposite direction
        Position idealPos = targetPos.move(oppositeDir).move(oppositeDir);
        return moveToward(currentPos, idealPos, 0.8);
    }
    
    /**
     * Adapts hunting mode based on tactical situation.
     */
    private void adaptMode(int distance, Position targetPos, List<Position> allies) {
        // Check for nearby allies
        boolean hasNearbyAllies = allies != null && hasAlliesInRange(allies, COLLABORATION_RANGE);
        
        // Adaptive mode switching
        if (hasNearbyAllies && distance < STRIKE_DISTANCE * 2) {
            mode = HunterMode.COORDINATE;
        } else if (distance <= EVASION_DISTANCE) {
            // Too close - tactical retreat
            mode = HunterMode.EVADE;
        } else if (distance <= STRIKE_DISTANCE) {
            // Strike range - attack
            mode = HunterMode.STRIKE;
        } else if (distance <= STALKING_DISTANCE) {
            // Optimal hunting distance - stalk
            mode = HunterMode.STALK;
        } else if (distance > STALKING_DISTANCE && stalkerPatience > 30) {
            // Been stalking too long - close in
            mode = HunterMode.STRIKE;
            stalkerPatience = 0;
        } else {
            // Default - stalk and observe
            mode = HunterMode.STALK;
        }
        
        // Occasionally flank for unpredictability
        if (pathHistory.size() % 20 == 0) {
            mode = HunterMode.FLANK;
        }
    }
    
    /**
     * Predicts target movement based on pattern history.
     */
    private Position predictTargetMovement(Position currentTarget, Direction velocity) {
        if (targetHistory.size() < 3) {
            // Not enough data - use velocity
            if (velocity != null) {
                return currentTarget.move(velocity).move(velocity);
            }
            return currentTarget;
        }
        
        // Analyze pattern from history
        Position last = targetHistory.get(targetHistory.size() - 1);
        Position secondLast = targetHistory.get(targetHistory.size() - 2);
        
        int dx = last.getX() - secondLast.getX();
        int dy = last.getY() - secondLast.getY();
        
        // Predict 2-3 steps ahead
        return new Position(currentTarget.getX() + dx * 2, currentTarget.getY() + dy * 2);
    }
    
    /**
     * Clever handling - validates moves and finds optimal paths.
     */
    private Direction cleverHandling(Position currentPos, Direction intended,
                                    Position targetPos, Set<Position> jetWalls) {
        Position nextPos = currentPos.move(intended);
        
        // Check for collision
        if (jetWalls.contains(nextPos)) {
            // Find alternative clever route
            return findCleverAlternative(currentPos, intended, targetPos, jetWalls);
        }
        
        // Check if move leads to dead end
        if (isDeadEnd(nextPos, jetWalls)) {
            // Avoid dead ends
            return findCleverAlternative(currentPos, intended, targetPos, jetWalls);
        }
        
        return intended;
    }
    
    /**
     * Finds clever alternative when primary path is blocked.
     */
    private Direction findCleverAlternative(Position currentPos, Direction blocked,
                                           Position targetPos, Set<Position> jetWalls) {
        // Try perpendicular directions first (clever navigation)
        Direction clockwise = blocked.rotateClockwise();
        Direction counterClockwise = blocked.rotateCounterClockwise();
        
        Position cwPos = currentPos.move(clockwise);
        Position ccwPos = currentPos.move(counterClockwise);
        
        boolean cwClear = !jetWalls.contains(cwPos);
        boolean ccwClear = !jetWalls.contains(ccwPos);
        
        if (cwClear && ccwClear) {
            // Both clear - choose based on target position
            int cwDist = manhattanDistance(cwPos, targetPos);
            int ccwDist = manhattanDistance(ccwPos, targetPos);
            return cwDist < ccwDist ? clockwise : counterClockwise;
        } else if (cwClear) {
            return clockwise;
        } else if (ccwClear) {
            return counterClockwise;
        } else {
            // Only opposite direction left
            return blocked.opposite();
        }
    }
    
    /**
     * Moves toward target with specified aggression (0.0-1.0).
     */
    private Direction moveToward(Position from, Position to, double aggression) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        
        // Reduce aggression affects priority
        if (Math.random() > aggression) {
            // Less aggressive - consider secondary axis
            if (Math.abs(dx) > 0 && Math.abs(dy) > 0) {
                return Math.random() < 0.5 ?
                    (dx > 0 ? Direction.RIGHT : Direction.LEFT) :
                    (dy > 0 ? Direction.DOWN : Direction.UP);
            }
        }
        
        // Primary axis movement
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else if (Math.abs(dy) > 0) {
            return dy > 0 ? Direction.DOWN : Direction.UP;
        }
        
        return currentDirection;
    }
    
    /**
     * Moves away from target.
     */
    private Direction moveAway(Position from, Position to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direction.LEFT : Direction.RIGHT;
        } else if (Math.abs(dy) > 0) {
            return dy > 0 ? Direction.UP : Direction.DOWN;
        }
        
        return currentDirection;
    }
    
    /**
     * Circles around target position.
     */
    private Direction circlePath(Position from, Position to) {
        // Perpendicular movement for circling
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();
        
        if (Math.abs(dx) > Math.abs(dy)) {
            return pathHistory.size() % 2 == 0 ? Direction.DOWN : Direction.UP;
        } else {
            return pathHistory.size() % 2 == 0 ? Direction.RIGHT : Direction.LEFT;
        }
    }
    
    /**
     * Checks if position has wall cover from target.
     */
    private boolean hasWallCover(Position pos, Position target, Set<Position> walls) {
        // Check if there's a wall between pos and target
        int dx = Integer.compare(target.getX() - pos.getX(), 0);
        int dy = Integer.compare(target.getY() - pos.getY(), 0);
        
        Position checkPos = pos.add(dx, dy);
        return walls.contains(checkPos);
    }
    
    /**
     * Checks if position is a dead end.
     */
    private boolean isDeadEnd(Position pos, Set<Position> walls) {
        int blockedDirections = 0;
        for (Direction dir : Direction.values()) {
            if (walls.contains(pos.move(dir))) {
                blockedDirections++;
            }
        }
        return blockedDirections >= 3;
    }
    
    /**
     * Finds nearest ally position.
     */
    private Position findNearestAlly(Position currentPos, List<Position> allies) {
        Position nearest = null;
        int minDist = Integer.MAX_VALUE;
        
        for (Position ally : allies) {
            int dist = manhattanDistance(currentPos, ally);
            if (dist < minDist && dist > 0) {  // dist > 0 to exclude self
                minDist = dist;
                nearest = ally;
            }
        }
        
        return nearest;
    }
    
    /**
     * Checks if there are allies within range.
     */
    private boolean hasAlliesInRange(List<Position> allies, int range) {
        // Implementation would check if any ally is within range
        // For now, return true if allies exist
        return !allies.isEmpty();
    }
    
    /**
     * Updates tracking data for pattern recognition.
     */
    private void updateTracking(Position currentPos, Position targetPos) {
        pathHistory.add(currentPos);
        targetHistory.add(targetPos);
        
        if (pathHistory.size() > PATTERN_MEMORY) {
            pathHistory.remove(0);
        }
        if (targetHistory.size() > PATTERN_MEMORY) {
            targetHistory.remove(0);
        }
        
        lastKnownTarget = targetPos;
    }
    
    /**
     * Calculates Manhattan distance.
     */
    private int manhattanDistance(Position a, Position b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
    
    /**
     * Gets current hunter mode for debugging.
     */
    public HunterMode getCurrentMode() {
        return mode;
    }
    
    @Override
    public void levelUp() {
        // Rinzler becomes more adaptive and tactical on level up
        // TODO: Enhance pattern recognition and collaboration range
    }
}
