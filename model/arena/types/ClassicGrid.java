package model.arena.types;

import model.arena.Arena;

/**
 * ClassicGrid Arena - A closed arena with solid boundary walls.
 * Standard square arena with walls forming a complete perimeter.
 */
public class ClassicGrid extends Arena {
    
    public ClassicGrid() {
        super("Classic Grid", false);
        generateStaticObjects();
    }

    @Override
    protected void generateStaticObjects() {
        // Create solid boundary walls around the entire perimeter
        // No internal obstacles - pure classic Tron arena
        
        // Top and bottom walls
        for (int x = 0; x < GRID_WIDTH; x++) {
            placeWall(x, 0);                    // Top wall
            placeWall(x, GRID_HEIGHT - 1);      // Bottom wall
        }
        
        // Left and right walls
        for (int y = 0; y < GRID_HEIGHT; y++) {
            placeWall(0, y);                    // Left wall
            placeWall(GRID_WIDTH - 1, y);       // Right wall
        }
    }
}
