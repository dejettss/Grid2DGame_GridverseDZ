package model.collision;

/**
 * Represents the result of a collision check.
 * Contains information about collision type, damage, and description.
 */
public class CollisionResult {
    
    private final CollisionType type;
    private final double livesLost;
    private final String description;
    
    /**
     * Constructor for CollisionResult.
     * 
     * @param type Type of collision
     * @param livesLost Number of lives lost
     * @param description Description of the collision
     */
    public CollisionResult(CollisionType type, double livesLost, String description) {
        this.type = type;
        this.livesLost = livesLost;
        this.description = description;
    }
    
    /**
     * Gets the collision type.
     * 
     * @return Collision type
     */
    public CollisionType getType() {
        return type;
    }
    
    /**
     * Gets the number of lives lost.
     * 
     * @return Lives lost
     */
    public double getLivesLost() {
        return livesLost;
    }
    
    /**
     * Gets the collision description.
     * 
     * @return Description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this was a collision (not NONE).
     * 
     * @return true if collision occurred
     */
    public boolean isCollision() {
        return type != CollisionType.NONE;
    }
    
    /**
     * Checks if this was a fatal collision.
     * 
     * @return true if collision is fatal
     */
    public boolean isFatal() {
        return type == CollisionType.FALL_OFF_ARENA;
    }
    
    @Override
    public String toString() {
        if (type == CollisionType.NONE) {
            return "No collision";
        }
        return String.format("%s: %s (-%s lives)",
                type, description, 
                livesLost == (int)livesLost ? 
                    String.valueOf((int)livesLost) : 
                    String.valueOf(livesLost));
    }
}
