package model.arena.types;

import model.arena.Arena;

/**
 * OpenFrontier Arena - An open arena with no boundary walls.
 * Players and enemies that move outside the grid fall into derez and are destroyed.
 */
public class OpenFrontier extends Arena {
    
    public OpenFrontier() {
        super("Open Frontier", true);
        generateStaticObjects();
    }

    @Override
    protected void generateStaticObjects() {
        // No boundary walls - this is an open arena
        // Players can fall off the edge into derez
        
        // Add some floating platforms/obstacles for strategic gameplay
        generateFloatingPlatforms();
    }

    /**
     * Creates floating platform structures throughout the arena.
     */
    private void generateFloatingPlatforms() {
        // Central platform cluster
        int centerX = GRID_WIDTH / 2;
        int centerY = GRID_HEIGHT / 2;
        
        // Central cross-shaped obstacle formation
        for (int i = -3; i <= 3; i++) {
            placeObstacle(centerX + i, centerY);
            placeObstacle(centerX, centerY + i);
        }
        
        // Corner platforms (safe zones near edges)
        createPlatform(5, 5, 4);
        createPlatform(GRID_WIDTH - 9, 5, 4);
        createPlatform(5, GRID_HEIGHT - 9, 4);
        createPlatform(GRID_WIDTH - 9, GRID_HEIGHT - 9, 4);
        
        // Mid-edge platforms
        createPlatform(centerX - 2, 5, 3);
        createPlatform(centerX - 2, GRID_HEIGHT - 8, 3);
        createPlatform(5, centerY - 2, 3);
        createPlatform(GRID_WIDTH - 8, centerY - 2, 3);
        
        // Scattered small obstacles for variety
        placeObstacle(15, 15);
        placeObstacle(25, 15);
        placeObstacle(15, 25);
        placeObstacle(25, 25);
        
        // Ring of obstacles at mid-distance from center
        int ringRadius = 12;
        for (int angle = 0; angle < 360; angle += 45) {
            double rad = Math.toRadians(angle);
            int x = centerX + (int)(ringRadius * Math.cos(rad));
            int y = centerY + (int)(ringRadius * Math.sin(rad));
            if (isValidPosition(new util.Position(x, y))) {
                placeObstacle(x, y);
            }
        }
    }

    /**
     * Creates a square platform of obstacles.
     */
    private void createPlatform(int startX, int startY, int size) {
        for (int x = startX; x < startX + size && x < GRID_WIDTH; x++) {
            for (int y = startY; y < startY + size && y < GRID_HEIGHT; y++) {
                // Create hollow square (only edges)
                if (x == startX || x == startX + size - 1 || 
                    y == startY || y == startY + size - 1) {
                    placeWall(x, y);
                }
            }
        }
    }
}
