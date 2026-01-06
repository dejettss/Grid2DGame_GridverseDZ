package model.objects.staticobj;

import model.objects.StaticObject;
import util.Position;
import java.awt.Color;

/**
 * Represents an empty traversable cell in the arena.
 * This is the default state for cells where dynamic objects can move.
 */
public class Empty extends StaticObject {
    
    public Empty(Position position) {
        super(position, new Color(10, 10, 20)); // Dark background
    }

    @Override
    public boolean isTraversable() {
        return true;
    }

    @Override
    public boolean causesDerez() {
        return false;
    }

    @Override
    public char getSymbol() {
        return ' ';
    }

    @Override
    public String getTypeName() {
        return "Empty";
    }
}
