package model.objects.staticobj;

import model.objects.StaticObject;
import util.Position;
import java.awt.Color;

/**
 * Represents a JetWall (light trail) left by players or enemies.
 * JetWalls are impassable and cause derez on collision.
 */
public class JetWall extends StaticObject {
    private final Color ownerColor;
    private final String ownerId;  // ID of entity who created this jetwall

    public JetWall(Position position, Color ownerColor) {
        this(position, ownerColor, null);
    }

    public JetWall(Position position, Color ownerColor, String ownerId) {
        super(position, ownerColor);
        this.ownerColor = ownerColor;
        this.ownerId = ownerId;
    }

    public Color getOwnerColor() {
        return ownerColor;
    }
    
    public String getOwnerId() {
        return ownerId;
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
        return '+';
    }

    @Override
    public String getTypeName() {
        return "JetWall";
    }
}
