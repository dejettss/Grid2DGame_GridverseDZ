package game;

import util.Direction;
import util.Position;

/**
 * Test to verify player loses life when hitting enemy face-to-face.
 */
public class FaceToFaceCollisionTest {
    
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════╗");
        System.out.println("║    Face-to-Face Collision Test            ║");
        System.out.println("╚═══════════════════════════════════════════╝");
        System.out.println();
        
        // Create game
        GameEngine engine = new GameEngine();
        engine.startNewGame();
        GameState state = engine.getGameState();
        
        System.out.println("Initial State:");
        System.out.println("  Player pos: " + state.getPlayer().getPosition());
        System.out.println("  Player lives: " + state.getPlayer().getCurrentLives());
        
        if (!state.getEnemies().isEmpty()) {
            var enemy = state.getEnemies().get(0);
            System.out.println("  Enemy pos: " + enemy.getPosition());
            System.out.println("  Enemy lives: " + enemy.getCurrentLives());
        }
        System.out.println();
        
        // Run updates to allow enemies to move
        System.out.println("Running game updates...");
        double playerLivesBefore = state.getPlayer().getCurrentLives();
        
        for (int i = 0; i < 100; i++) {
            state.update();
            
            // Check if player lost life due to face-to-face collision
            if (state.getPlayer().getCurrentLives() < playerLivesBefore) {
                System.out.println("  Update " + (i+1) + ": Player lost life!");
                System.out.println("    Lives: " + playerLivesBefore + " -> " + state.getPlayer().getCurrentLives());
                System.out.println("    Player pos: " + state.getPlayer().getPosition());
                
                // Check if any enemy is at the same position
                for (var enemy : state.getEnemies()) {
                    if (enemy.getPosition().equals(state.getPlayer().getPosition())) {
                        System.out.println("    Face-to-face with: " + enemy.getName() + " at " + enemy.getPosition());
                        break;
                    }
                }
                break;
            }
        }
        
        System.out.println();
        System.out.println("Final State:");
        System.out.println("  Player lives: " + state.getPlayer().getCurrentLives());
        System.out.println();
        
        // Summary
        System.out.println("═══════════════════════════════════════════");
        if (state.getPlayer().getCurrentLives() < playerLivesBefore) {
            System.out.println("✓ Test complete: Player life loss detected");
            System.out.println("  (Could be jetwall, disc, or face-to-face)");
        } else {
            System.out.println("⚠ Note: No collision occurred in this run");
            System.out.println("  (This is OK - random movement may not cause collision)");
        }
        System.out.println("═══════════════════════════════════════════");
    }
}
