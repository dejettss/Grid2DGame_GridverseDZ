package model.character;

import java.awt.Color;

/**
 * Kevin character - Advanced character with superior stats.
 * Loaded from characters.txt file.
 */
public class Kevin extends Character {
    
    /**
     * Constructor that loads Kevin's stats from character data.
     */
    public Kevin(int speed, int handling, int lives, int discs, int xp) {
        super("Kevin", Color.WHITE, speed, handling, lives, discs, xp);
    }
    
    @Override
    public void levelUp() {
        
    }
}
