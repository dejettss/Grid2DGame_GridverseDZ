package model.collision;

/**
 * Types of collisions that can occur in the game.
 */
public enum CollisionType {
    NONE,                // No collision
    JETWALL,             // Hit a JetWall (-0.5 lives)
    DISC_HIT,            // Struck by a disc (-1 life)
    FALL_OFF_ARENA       // Fell off open arena (instant death - all lives lost)
}
