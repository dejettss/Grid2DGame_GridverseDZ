package util;

/**
 * Represents the four cardinal directions for movement in the grid.
 */
public enum Direction {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    /**
     * Returns the opposite direction.
     */
    public Direction opposite() {
        switch (this) {
            case UP: return DOWN;
            case DOWN: return UP;
            case LEFT: return RIGHT;
            case RIGHT: return LEFT;
            default: return this;
        }
    }

    /**
     * Returns the direction rotated 90 degrees clockwise.
     */
    public Direction rotateClockwise() {
        switch (this) {
            case UP: return RIGHT;
            case RIGHT: return DOWN;
            case DOWN: return LEFT;
            case LEFT: return UP;
            default: return this;
        }
    }

    /**
     * Returns the direction rotated 90 degrees counter-clockwise.
     */
    public Direction rotateCounterClockwise() {
        switch (this) {
            case UP: return LEFT;
            case LEFT: return DOWN;
            case DOWN: return RIGHT;
            case RIGHT: return UP;
            default: return this;
        }
    }
}
