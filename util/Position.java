package util;

/**
 * Represents a position in the grid arena with x and y coordinates.
 * Immutable class for thread-safe position handling.
 */
public class Position {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    /**
     * Creates a new Position by adding the given offsets.
     */
    public Position add(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }

    /**
     * Creates a new Position by moving in a given direction.
     */
    public Position move(Direction direction) {
        return new Position(x + direction.getDx(), y + direction.getDy());
    }

    /**
     * Checks if this position is within the bounds of a grid.
     */
    public boolean isInBounds(int width, int height) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Calculates Manhattan distance to another position.
     * Useful for AI pathfinding and distance checks.
     */
    public int manhattanDistance(Position other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    /**
     * Calculates Euclidean distance to another position.
     * Returns the straight-line distance.
     */
    public double euclideanDistance(Position other) {
        int dx = this.x - other.x;
        int dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Gets the general direction from this position to another.
     * Returns the primary direction (prioritizes horizontal over vertical).
     */
    public Direction getDirectionTo(Position target) {
        int dx = target.x - this.x;
        int dy = target.y - this.y;
        
        // Prioritize larger difference
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        } else if (dy != 0) {
            return dy > 0 ? Direction.DOWN : Direction.UP;
        }
        
        // Same position - return arbitrary direction
        return Direction.UP;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "Position(" + x + ", " + y + ")";
    }
}
