package model.objects;

import util.Position;
import java.awt.Color;

/**
 * Abstract class for all static objects in the arena.
 * Static objects cannot be moved or removed once placed.
 */
public abstract class StaticObject extends GameObject {
    
    public StaticObject(Position position, Color color) {
        super(position, color, true);
    }

    @Override
    public final void setPosition(Position position) {
        // Static objects cannot change position
        throw new UnsupportedOperationException("Static objects cannot be moved");
    }
}
