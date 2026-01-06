package model.objects;

import util.Position;
import java.awt.Color;

/**
 * Abstract base class for all game objects in the Tron arena.
 * Defines common properties and behaviors for both static and dynamic objects.
 */
public abstract class GameObject {
    protected Position position;
    protected Color color;
    protected boolean isStatic;

    public GameObject(Position position, Color color, boolean isStatic) {
        this.position = position;
        this.color = color;
        this.isStatic = isStatic;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        if (!isStatic) {
            this.position = position;
        }
    }

    public Color getColor() {
        return color;
    }

    public boolean isStatic() {
        return isStatic;
    }

    /**
     * Returns whether this object can be traversed by dynamic objects.
     */
    public abstract boolean isTraversable();

    /**
     * Returns whether this object causes derez (destruction) on contact.
     */
    public abstract boolean causesDerez();

    /**
     * Returns the symbol/character representation for console display.
     */
    public abstract char getSymbol();

    /**
     * Returns the type name of this game object.
     */
    public abstract String getTypeName();

    @Override
    public String toString() {
        return getTypeName() + " at " + position;
    }
}
