package model.character;

import java.awt.Color;

/**
 * Tron character - Balanced character with moderate stats.
 * Loaded from characters.txt file.
 */
public class Tron extends Character {
    
    /**
     * Constructor that loads Tron's stats from character data.
     */
    public Tron(int speed, int handling, int lives, int discs, int xp) {
        super("Tron", Color.BLUE, speed, handling, lives, discs, xp);
    }
    
    @Override
    public void levelUp() {
        
    }
}
