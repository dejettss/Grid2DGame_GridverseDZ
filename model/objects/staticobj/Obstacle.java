package model.objects.staticobj;

import model.objects.StaticObject;
import util.Position;
import java.awt.Color;

/**
 * Represents an obstacle in the arena.
 * Obstacles block movement and cause derez on collision.
 */
public class Obstacle extends StaticObject {
    
    public Obstacle(Position position) {
        super(position, new Color(255, 100, 0)); // Orange neon
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
        return 'O';
    }

    @Override
    public String getTypeName() {
        return "Obstacle";
    }
}
