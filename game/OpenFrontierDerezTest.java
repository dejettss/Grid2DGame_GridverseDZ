package game;

import model.arena.ArenaFactory;
import model.progression.LevelConfig;
import util.Direction;

/**
 * Test to verify enemy jetwalls are cleared when enemy falls into derez in OpenFrontier arena.
 */
public class OpenFrontierDerezTest {
    
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════╗");
        System.out.println("║  OpenFrontier Derez JetWall Clear Test   ║");
        System.out.println("╚═══════════════════════════════════════════╝");
        System.out.println();
        
        // Create a game with OpenFrontier arena (level 13)
        GameEngine engine = new GameEngine();
        
        // Initialize level 13 (OpenFrontier arena)
        System.out.println("Creating OpenFrontier arena (Level 13)...");
        engine.startLevel(13);
        GameState state = engine.getGameState();
        
        System.out.println("Arena: " + state.getArena().getArenaName());
        System.out.println("Is Open Arena: " + state.getArena().isOpenArena());
        System.out.println();
        
        // Get initial state
        System.out.println("Initial State:");
        System.out.println("  JetWalls in arena: " + state.getArena().getJetWallCount());
        System.out.println("  Alive enemies: " + state.getAliveEnemyCount());
        System.out.println();
        
        // Move player to create some jetwalls
        System.out.println("Moving player to create jetwalls...");
        for (int i = 0; i < 3; i++) {
            state.movePlayer(Direction.RIGHT);
        }
        System.out.println("  Player JetWalls created: " + state.getArena().getJetWallCount());
        System.out.println();
        
        // Run game updates to let enemies move
        System.out.println("Running game updates (enemies moving)...");
        int updateCount = 0;
        int maxUpdates = 200;
        int initialEnemyCount = state.getAliveEnemyCount();
        
        while (updateCount < maxUpdates && state.getAliveEnemyCount() > 0) {
            int jetWallsBefore = state.getArena().getJetWallCount();
            int enemiesBefore = state.getAliveEnemyCount();
            
            state.update();
            updateCount++;
            
            int jetWallsAfter = state.getArena().getJetWallCount();
            int enemiesAfter = state.getAliveEnemyCount();
            
            // Check if an enemy died (fell into derez or other cause)
            if (enemiesAfter < enemiesBefore) {
                System.out.println("  Update " + updateCount + ": Enemy died!");
                System.out.println("    Enemies: " + enemiesBefore + " → " + enemiesAfter);
                System.out.println("    JetWalls: " + jetWallsBefore + " → " + jetWallsAfter);
                
                // Check which enemies are still alive
                for (var enemy : state.getEnemies()) {
                    System.out.println("    " + enemy.getName() + ": " + 
                                     (enemy.isAlive() ? "ALIVE" : "DEAD"));
                }
                
                // Check jetwall owners
                System.out.println("    Remaining JetWalls by owner:");
                var jetWalls = state.getArena().getAllJetWalls();
                var ownerCounts = new java.util.HashMap<String, Integer>();
                for (var wall : jetWalls) {
                    String owner = wall.getOwnerId() != null ? wall.getOwnerId() : "Unknown";
                    ownerCounts.put(owner, ownerCounts.getOrDefault(owner, 0) + 1);
                }
                for (var entry : ownerCounts.entrySet()) {
                    System.out.println("      " + entry.getKey() + ": " + entry.getValue());
                }
                System.out.println();
            }
        }
        
        System.out.println();
        System.out.println("Final State (after " + updateCount + " updates):");
        System.out.println("  Alive enemies: " + state.getAliveEnemyCount() + "/" + state.getTotalEnemyCount());
        System.out.println("  JetWalls in arena: " + state.getArena().getJetWallCount());
        
        // Check jetwall ownership
        var jetWalls = state.getArena().getAllJetWalls();
        System.out.println("\nRemaining JetWalls by owner:");
        var ownerCounts = new java.util.HashMap<String, Integer>();
        for (var wall : jetWalls) {
            String owner = wall.getOwnerId() != null ? wall.getOwnerId() : "Unknown";
            ownerCounts.put(owner, ownerCounts.getOrDefault(owner, 0) + 1);
        }
        for (var entry : ownerCounts.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        
        System.out.println();
        System.out.println("═══════════════════════════════════════════");
        
        // Check if dead enemies still have jetwalls
        boolean deadEnemiesHaveJetWalls = false;
        for (var enemy : state.getEnemies()) {
            if (!enemy.isAlive()) {
                // Count jetwalls owned by this dead enemy
                int wallCount = 0;
                for (var wall : jetWalls) {
                    if (enemy.getName().equals(wall.getOwnerId())) {
                        wallCount++;
                    }
                }
                
                if (wallCount > 0) {
                    System.out.println("⚠ FAIL: Dead enemy " + enemy.getName() + 
                                     " still has " + wallCount + " jetwalls!");
                    deadEnemiesHaveJetWalls = true;
                } else {
                    System.out.println("✓ PASS: Dead enemy " + enemy.getName() + 
                                     " has no jetwalls remaining");
                }
            }
        }
        
        if (!deadEnemiesHaveJetWalls && state.getAliveEnemyCount() < initialEnemyCount) {
            System.out.println("✓ TEST PASSED: Dead enemies' jetwalls were properly cleared!");
        } else if (state.getAliveEnemyCount() == initialEnemyCount) {
            System.out.println("⚠ Note: No enemies died during test (they may not have fallen off edge)");
        }
        
        System.out.println("═══════════════════════════════════════════");
    }
}
