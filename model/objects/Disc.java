package model.objects;

import util.Position;
import util.Direction;
import java.awt.Color;

/**
 * Represents a disc in the Tron game.
 * Discs can be thrown by players or enemies and stay on the grid until recaptured.
 * 
 * Throwing Mechanics:
 * - Range: Up to 3 grid units
 * - Once thrown, disc stays where it lands
 * - Can only be recaptured by the owner
 */
public class Disc extends DynamicObject {
    
    private static final int MAX_THROW_RANGE = 3;  // Maximum throw distance
    private static final int DISC_SPEED = 0;       // Discs don't move on their own
    
    private String ownerId;           // ID of the owner (player/enemy)
    private Direction throwDirection; // Direction disc was thrown
    private boolean isHeld;           // True if held by owner, false if on grid
    private int throwDistance;        // Distance disc was thrown
    
    /**
     * Constructor for creating a disc.
     * 
     * @param ownerId Unique identifier of the owner
     * @param ownerColor Color representing the owner
     * @param position Initial position (usually owner's position)
     */
    public Disc(String ownerId, Color ownerColor, Position position) {
        super(position, ownerColor, Direction.UP, DISC_SPEED);
        this.ownerId = ownerId;
        this.isHeld = true;  // Disc starts held by owner
        this.throwDirection = null;
        this.throwDistance = 0;
    }
    
    /**
     * Throws the disc in a specified direction.
     * The disc will travel up to the specified distance, but will stop before
     * hitting any wall, obstacle, or JetWall.
     * 
     * Rules:
     * - If obstacle at 3rd unit: disc moves 2 units
     * - If obstacle at 2nd unit: disc moves 1 unit
     * - If obstacle at 1st unit: disc cannot be thrown (returns null)
     * 
     * @param direction Direction to throw the disc
     * @param distance Distance to throw (1-3 units)
     * @param obstacles Positions that block disc travel (walls, obstacles, JetWalls)
     * @return The final position where disc lands, or null if throw is invalid
     */
    public Position throwDisc(Direction direction, int distance, java.util.Set<Position> obstacles) {
        if (!isHeld) {
            // Disc is already thrown
            return null;
        }
        
        if (distance < 1 || distance > MAX_THROW_RANGE) {
            // Invalid throw distance
            return null;
        }
        
        // Calculate landing position considering obstacles
        Position landingPos = calculateLandingPosition(position, direction, distance, obstacles);
        
        if (landingPos != null) {
            // Valid throw
            this.position = landingPos;
            this.throwDirection = direction;
            this.throwDistance = calculateActualDistance(position, landingPos, direction);
            this.isHeld = false;
            return landingPos;
        }
        
        return null;  // Throw blocked (obstacle in 1st unit)
    }
    
    /**
     * Calculates the actual distance traveled.
     */
    private int calculateActualDistance(Position start, Position end, Direction dir) {
        int distance = 0;
        Position current = start;
        
        while (!current.equals(end)) {
            current = current.move(dir);
            distance++;
        }
        
        return distance;
    }
    
    /**
     * Calculates where the disc will land considering obstacles.
     * Disc stops BEFORE hitting a wall, obstacle, or JetWall.
     * 
     * Returns:
     * - null if 1st unit is blocked (can't throw)
     * - Position before obstacle if path is blocked
     * - Final position if path is clear
     */
    private Position calculateLandingPosition(Position start, Direction dir, int distance,
                                             java.util.Set<Position> obstacles) {
        Position lastValid = start;
        
        for (int i = 1; i <= distance; i++) {
            Position next = lastValid.move(dir);
            
            // Check if next position is blocked by obstacle
            if (obstacles.contains(next)) {
                // Obstacle found at position i
                if (i == 1) {
                    // First unit is blocked - cannot throw at all
                    return null;
                } else {
                    // Stop at lastValid (before the obstacle)
                    // i=2 means obstacle at 2nd unit -> disc moves 1 unit (lastValid)
                    // i=3 means obstacle at 3rd unit -> disc moves 2 units (lastValid)
                    this.position = lastValid;
                    return lastValid;
                }
            }
            
            // Next position is clear, continue
            lastValid = next;
        }
        
        // Full distance traveled without obstacles
        this.position = lastValid;
        return lastValid;
    }
    
    /**
     * Attempts to recapture the disc.
     * Only the owner can recapture their disc.
     * 
     * @param attempterId ID of the entity trying to recapture
     * @param attempterPos Position of the entity
     * @return true if successfully recaptured, false otherwise
     */
    public boolean recapture(String attempterId, Position attempterPos) {
        if (isHeld) {
            // Disc is already held
            return false;
        }
        
        if (!attempterId.equals(ownerId)) {
            // Only owner can recapture their disc
            return false;
        }
        
        // Check if owner is adjacent to or on disc position
        if (isAdjacentOrSame(attempterPos, position)) {
            this.isHeld = true;
            this.position = attempterPos;  // Disc moves to owner's position
            this.throwDirection = null;
            this.throwDistance = 0;
            return true;
        }
        
        return false;  // Too far away to recapture
    }
    
    /**
     * Checks if two positions are adjacent or the same.
     */
    private boolean isAdjacentOrSame(Position p1, Position p2) {
        int dx = Math.abs(p1.getX() - p2.getX());
        int dy = Math.abs(p1.getY() - p2.getY());
        
        // Same position or adjacent (including diagonals)
        return dx <= 1 && dy <= 1;
    }
    
    /**
     * Updates disc position when owner moves (only if held).
     */
    public void updatePosition(Position newPosition) {
        if (isHeld) {
            this.position = newPosition;
        }
    }
    
    /**
     * Update method required by DynamicObject.
     * Discs don't move on their own - they're either held or stationary.
     */
    @Override
    public void update() {
        // Discs don't have autonomous movement
        // Position updates happen via updatePosition() or throwDisc()
    }
    
    /**
     * Handles collision with another game object.
     * Used when disc hits something or something hits the disc.
     */
    @Override
    public void onCollision(GameObject other) {
        // Collision handling will be managed by game logic
        // Disc doesn't cause automatic derez - handled by combat system
    }
    
    /**
     * Checks if disc can be thrown (must be held and owner has it).
     */
    public boolean canThrow() {
        return isHeld;
    }
    
    /**
     * Checks if a specific entity can recapture this disc.
     */
    public boolean canBeRecapturedBy(String attempterId) {
        return !isHeld && attempterId.equals(ownerId);
    }
    
    // Getters
    public String getOwnerId() {
        return ownerId;
    }
    
    public Color getOwnerColor() {
        return color;  // Use inherited color field
    }
    
    @Override
    public Position getPosition() {
        return position;
    }
    
    public Direction getThrowDirection() {
        return throwDirection;
    }
    
    public boolean isHeld() {
        return isHeld;
    }
    
    public int getThrowDistance() {
        return throwDistance;
    }
    
    public static int getMaxThrowRange() {
        return MAX_THROW_RANGE;
    }
    
    // GameObject abstract methods implementation
    @Override
    public boolean isTraversable() {
        // Can move through disc position (but might pick it up)
        return true;
    }
    
    @Override
    public boolean causesDerez() {
        // Disc itself doesn't cause derez (collision handling is separate)
        return false;
    }
    
    @Override
    public char getSymbol() {
        return isHeld ? 'H' : 'D';  // H = Held, D = Disc on grid
    }
    
    @Override
    public String getTypeName() {
        return "Disc";
    }
    
    @Override
    public String toString() {
        return String.format("Disc[Owner: %s, Position: %s, Held: %s]",
                ownerId, position, isHeld);
    }
    
    /**
     * Gets detailed disc information.
     */
    public String toDetailedString() {
        if (isHeld) {
            return String.format("Disc held by %s at %s", ownerId, position);
        } else {
            return String.format("Disc thrown by %s, landed at %s (thrown %s for %d units)",
                    ownerId, position, throwDirection, throwDistance);
        }
    }
}
