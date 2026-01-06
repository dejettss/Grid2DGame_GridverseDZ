package game;

import util.Direction;

/**
 * Test to verify player jetwalls are cleared when player falls into derez or dies.
 */
public class PlayerDerezTest {
    
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════╗");
        System.out.println("║    Player JetWall Clear on Death Test    ║");
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
        System.out.println("  Player position: " + state.getPlayer().getPosition());
        System.out.println("  Player lives: " + state.getPlayer().getCurrentLives());
        System.out.println("  JetWalls in arena: " + state.getArena().getJetWallCount());
        System.out.println();
        
        // Move player to create jetwalls and move toward edge
        System.out.println("Moving player to create jetwalls and approach edge...");
        for (int i = 0; i < 10; i++) {
            state.movePlayer(Direction.LEFT);
        }
        
        int jetWallsCreated = state.getArena().getJetWallCount();
        System.out.println("  Player JetWalls created: " + jetWallsCreated);
        System.out.println("  Player position: " + state.getPlayer().getPosition());
        System.out.println();
        
        // Count player's jetwalls
        int playerJetWalls = 0;
        for (var wall : state.getArena().getAllJetWalls()) {
            if ("Player".equals(wall.getOwnerId())) {
                playerJetWalls++;
            }
        }
        System.out.println("  Player's JetWalls: " + playerJetWalls);
        System.out.println();
        
        // Continue moving left to fall off edge
        System.out.println("Moving player off the edge...");
        boolean playerAlive = true;
        int moveCount = 0;
        
        while (playerAlive && moveCount < 50) {
            playerAlive = state.getPlayer().isAlive();
            
            if (!playerAlive) {
                break;
            }
            
            state.movePlayer(Direction.LEFT);
            moveCount++;
            
            // Check if player died
            if (!state.getPlayer().isAlive()) {
                System.out.println("  Player died after " + moveCount + " additional moves!");
                System.out.println("  Final position attempt: " + state.getPlayer().getPosition());
                break;
            }
        }
        
        System.out.println();
        
        // Run update to trigger checkGameStatus
        state.update();
        
        System.out.println("Final State:");
        System.out.println("  Player alive: " + state.getPlayer().isAlive());
        System.out.println("  Game over: " + state.isGameOver());
        System.out.println("  JetWalls in arena: " + state.getArena().getJetWallCount());
        System.out.println();
        
        // Count remaining jetwalls by owner
        System.out.println("Remaining JetWalls by owner:");
        var jetWalls = state.getArena().getAllJetWalls();
        var ownerCounts = new java.util.HashMap<String, Integer>();
        for (var wall : jetWalls) {
            String owner = wall.getOwnerId() != null ? wall.getOwnerId() : "Unknown";
            ownerCounts.put(owner, ownerCounts.getOrDefault(owner, 0) + 1);
        }
        for (var entry : ownerCounts.entrySet()) {
            System.out.println("  " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println();
        
        // Check if player jetwalls were cleared
        int remainingPlayerJetWalls = 0;
        for (var wall : jetWalls) {
            if ("Player".equals(wall.getOwnerId())) {
                remainingPlayerJetWalls++;
            }
        }
        
        System.out.println("═══════════════════════════════════════════");
        if (!state.getPlayer().isAlive() && remainingPlayerJetWalls == 0) {
            System.out.println("✓ TEST PASSED: Player's jetwalls were cleared on death!");
            System.out.println("  Had " + playerJetWalls + " jetwalls, now has 0");
        } else if (!state.getPlayer().isAlive() && remainingPlayerJetWalls > 0) {
            System.out.println("⚠ FAIL: Player died but " + remainingPlayerJetWalls + " jetwalls remain!");
        } else {
            System.out.println("⚠ Note: Player did not die during test");
            System.out.println("  (May need to move further to fall off edge)");
        }
        System.out.println("═══════════════════════════════════════════");
    }
}
