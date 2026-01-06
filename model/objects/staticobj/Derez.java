package model.objects.staticobj;

import model.objects.StaticObject;
import util.Position;
import java.awt.Color;

/**
 * Represents the derez zone (void) outside the grid boundaries in open arenas.
 * Falling into derez causes immediate destruction.
 */
public class Derez extends StaticObject {
    
    public Derez(Position position) {
        super(position, new Color(5, 5, 10)); // Very dark, almost black
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
        return '~';
    }

    @Override
    public String getTypeName() {
        return "Derez";
    }
}
