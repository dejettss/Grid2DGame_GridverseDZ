package model.objects;

import util.Position;
import util.Direction;
import java.awt.Color;

/**
 * Abstract class for all dynamic objects in the arena.
 * Dynamic objects can move and interact with the environment.
 */
public abstract class DynamicObject extends GameObject {
    protected Direction direction;
    protected int speed;

    public DynamicObject(Position position, Color color, Direction direction, int speed) {
        super(position, color, false);
        this.direction = direction;
        this.speed = speed;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /**
     * Updates the object's state (called each game tick).
     */
    public abstract void update();

    /**
     * Handles collision with another game object.
     */
    public abstract void onCollision(GameObject other);
}
