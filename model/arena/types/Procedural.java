package model.arena.types;

import model.arena.Arena;
import java.util.Random;

/**
 * Procedural Arena - A randomly generated arena based on a seed.
 * Creates unique layouts with procedurally placed walls and obstacles.
 */
public class Procedural extends Arena {
    private final long seed;
    private Random random;
    
    /**
     * Creates a procedural arena with a random seed.
     */
    public Procedural() {
        this(System.currentTimeMillis());
    }
    
    /**
     * Creates a procedural arena with a specific seed for reproducible layouts.
     */
    public Procedural(long seed) {
        super("Procedural Arena (Seed: " + seed + ")", false);
        this.seed = seed;
        this.random = new Random(seed);
        generateStaticObjects();
    }

    @Override
    protected void generateStaticObjects() {
        // Create boundary walls
        for (int x = 0; x < GRID_WIDTH; x++) {
            placeWall(x, 0);
            placeWall(x, GRID_HEIGHT - 1);
        }
        
        for (int y = 0; y < GRID_HEIGHT; y++) {
            placeWall(0, y);
            placeWall(GRID_WIDTH - 1, y);
        }
        
        // Generate usable procedural content
        generateScatteredObstacles();
    }

    /**
     * Generates scattered obstacles in a grid pattern with randomization.
     * Ensures the arena remains navigable for dynamic objects.
     */
    private void generateScatteredObstacles() {
        int spacing = 6 + random.nextInt(4); // 6-9 cells between potential obstacles
        int obstacleChance = 40; // 40% chance to place obstacle at grid point
        
        // Place obstacles on a grid with randomization
        for (int x = spacing; x < GRID_WIDTH - spacing; x += spacing) {
            for (int y = spacing; y < GRID_HEIGHT - spacing; y += spacing) {
                // Random chance to place obstacle
                if (random.nextInt(100) < obstacleChance) {
                    placeObstacle(x, y);
                    
                    // Small chance to create a 2x2 or L-shape cluster
                    int clusterType = random.nextInt(100);
                    if (clusterType < 15) {
                        // 2x2 block
                        placeObstacle(x + 1, y);
                        placeObstacle(x, y + 1);
                        placeObstacle(x + 1, y + 1);
                    } else if (clusterType < 25) {
                        // L-shape
                        int direction = random.nextInt(4);
                        switch (direction) {
                            case 0: // Top-right L
                                placeObstacle(x + 1, y);
                                placeObstacle(x, y - 1);
                                break;
                            case 1: // Bottom-right L
                                placeObstacle(x + 1, y);
                                placeObstacle(x, y + 1);
                                break;
                            case 2: // Bottom-left L
                                placeObstacle(x - 1, y);
                                placeObstacle(x, y + 1);
                                break;
                            case 3: // Top-left L
                                placeObstacle(x - 1, y);
                                placeObstacle(x, y - 1);
                                break;
                        }
                    }
                }
            }
        }
        
        // Add some single random obstacles in between grid points
        int extraObstacles = 5 + random.nextInt(10);
        for (int i = 0; i < extraObstacles; i++) {
            int x = 5 + random.nextInt(GRID_WIDTH - 10);
            int y = 5 + random.nextInt(GRID_HEIGHT - 10);
            
            util.Position pos = new util.Position(x, y);
            if (isCellEmpty(pos)) {
                placeObstacle(x, y);
            }
        }
    }

    /**
     * Gets the seed used for generation.
     */
    public long getSeed() {
        return seed;
    }
}
