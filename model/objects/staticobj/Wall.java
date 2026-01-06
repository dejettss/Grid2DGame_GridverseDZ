package model.objects.staticobj;

import model.objects.StaticObject;
import util.Position;
import java.awt.Color;

/**
 * Represents a solid wall in the arena.
 * Walls cannot be traversed and cause immediate derez on collision.
 */
public class Wall extends StaticObject {
    
    public Wall(Position position) {
        super(position, new Color(0, 200, 255)); // Cyan neon
    }

    @Override
    public boolean isTraversable() {
        return false;
    }

    @Override
    public boolean causesDerez() {
        return true;
    }

    @Override
    public char getSymbol() {
        return '#';
    }

    @Override
    public String getTypeName() {
        return "Wall";
    }
}
