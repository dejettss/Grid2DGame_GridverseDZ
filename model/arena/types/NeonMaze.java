package model.arena.types;

import model.arena.Arena;

/**
 * NeonMaze Arena - A closed arena with simple maze structure.
 * Features clear pathways and wide corridors for easy navigation.
 */
public class NeonMaze extends Arena {
    
    public NeonMaze() {
        super("Neon Maze", false);
        generateStaticObjects();
    }

    @Override
    protected void generateStaticObjects() {
        // Create outer boundary walls
        for (int x = 0; x < GRID_WIDTH; x++) {
            placeWall(x, 0);
            placeWall(x, GRID_HEIGHT - 1);
        }
        
        for (int y = 0; y < GRID_HEIGHT; y++) {
            placeWall(0, y);
            placeWall(GRID_WIDTH - 1, y);
        }
        
        // Create maze-like internal structure with corridors
        generateMazeWalls();
    }

    /**
     * Generates a simple maze-like pattern with clear pathways.
     * Ensures enough space for characters and enemies to navigate.
     */
    private void generateMazeWalls() {
        // Create simple room-based maze with wide corridors
        int roomSize = 10;
        int corridorWidth = 4;
        
        // Create horizontal dividers with gaps
        for (int y = roomSize; y < GRID_HEIGHT - 1; y += roomSize) {
            for (int x = 1; x < GRID_WIDTH - 1; x++) {
                // Leave gaps in the middle of each section
                int sectionMid = (x / roomSize) * roomSize + roomSize / 2;
                int distanceFromGap = Math.abs(x - sectionMid);
                
                if (distanceFromGap > corridorWidth / 2) {
                    placeWall(x, y);
                }
            }
        }
        
        // Create vertical dividers with gaps (offset from horizontal)
        for (int x = roomSize; x < GRID_WIDTH - 1; x += roomSize) {
            for (int y = 1; y < GRID_HEIGHT - 1; y++) {
                // Leave gaps in the middle of each section
                int sectionMid = (y / roomSize) * roomSize + roomSize / 2;
                int distanceFromGap = Math.abs(y - sectionMid);
                
                // Offset gaps from horizontal walls
                boolean isNearHorizontalWall = (y % roomSize) < 2 || (y % roomSize) > roomSize - 3;
                
                if (distanceFromGap > corridorWidth / 2 && !isNearHorizontalWall) {
                    placeWall(x, y);
                }
            }
        }
        
        // Add strategic obstacles at some intersections (not all)
        for (int x = roomSize; x < GRID_WIDTH - roomSize; x += roomSize * 2) {
            for (int y = roomSize; y < GRID_HEIGHT - roomSize; y += roomSize * 2) {
                placeObstacle(x, y);
            }
        }
    }
}
