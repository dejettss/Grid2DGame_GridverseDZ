package model.arena;

import model.arena.types.*;

/**
 * Factory class for creating different types of arenas.
 * Implements Factory Pattern for clean arena instantiation.
 */
public class ArenaFactory {
    
    public enum ArenaType {
        CLASSIC_GRID,
        NEON_MAZE,
        OPEN_FRONTIER,
        PROCEDURAL
    }
    
    /**
     * Creates an arena of the specified type.
     */
    public static Arena createArena(ArenaType type) {
        switch (type) {
            case CLASSIC_GRID:
                return new ClassicGrid();
            case NEON_MAZE:
                return new NeonMaze();
            case OPEN_FRONTIER:
                return new OpenFrontier();
            case PROCEDURAL:
                return new Procedural();
            default:
                return new ClassicGrid();
        }
    }
    
    /**
     * Creates a procedural arena with a specific seed.
     */
    public static Arena createProceduralArena(long seed) {
        return new Procedural(seed);
    }
    
    /**
     * Gets all available arena types.
     */
    public static ArenaType[] getAvailableArenas() {
        return ArenaType.values();
    }
    
    /**
     * Gets a description for each arena type.
     */
    public static String getArenaDescription(ArenaType type) {
        switch (type) {
            case CLASSIC_GRID:
                return "Standard square arena with solid boundary walls.";
            case NEON_MAZE:
                return "Maze-like structure with narrow corridors and strategic chokepoints.";
            case OPEN_FRONTIER:
                return "Open arena with no boundaries - fall off the edge to derez.";
            case PROCEDURAL:
                return "Randomly generated layout using procedural generation.";
            default:
                return "Unknown arena type.";
        }
    }
}
