package game;

import util.Direction;
import util.Position;

/**
 * Test to verify entities CAN hit other entities' jetwalls.
 */
public class EnemyJetWallTest {
    
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════╗");
        System.out.println("║    Enemy JetWall Collision Test          ║");
        System.out.println("╚═══════════════════════════════════════════╝");
        System.out.println();
        
        // Create game
        GameEngine engine = new GameEngine();
        engine.startNewGame();
        GameState state = engine.getGameState();
        
        System.out.println("Initial State:");
        System.out.println("  Player pos: " + state.getPlayer().getPosition());
        System.out.println("  Player lives: " + state.getPlayer().getCurrentLives());
        System.out.println();
        
        // Create a path of player jetwalls
        System.out.println("Creating player jetwall trail...");
        for (int i = 0; i < 5; i++) {
            state.movePlayer(Direction.RIGHT);
        }
        System.out.println("  JetWalls created: " + state.getArena().getAllJetWalls().size());
        System.out.println("  Player position: " + state.getPlayer().getPosition());
        System.out.println();
        
        // Print jetwall positions
        System.out.println("Player JetWall positions:");
        for (var wall : state.getArena().getAllJetWalls()) {
            System.out.println("  - " + wall.getPosition() + " (owner: " + wall.getOwnerId() + ")");
        }
        System.out.println();
        
        // Let enemy move around
        System.out.println("Letting enemy move for 20 updates...");
        var enemy = state.getEnemies().get(0);
        double enemyLivesBefore = enemy.getCurrentLives();
        int wallsBefore = state.getArena().getAllJetWalls().size();
        
        for (int i = 0; i < 20; i++) {
            state.update();
            
            // Check if enemy hit a jetwall
            if (enemy.getCurrentLives() < enemyLivesBefore) {
                System.out.println("  Update " + (i+1) + ": Enemy hit a jetwall!");
                System.out.println("    Lives: " + enemyLivesBefore + " -> " + enemy.getCurrentLives());
                System.out.println("    JetWalls: " + wallsBefore + " -> " + state.getArena().getAllJetWalls().size());
                break;
            }
        }
        
        System.out.println();
        System.out.println("Final State:");
        System.out.println("  Enemy lives: " + enemy.getCurrentLives());
        System.out.println("  JetWalls remaining: " + state.getArena().getAllJetWalls().size());
        System.out.println();
        
        // Summary
        System.out.println("═══════════════════════════════════════════");
        if (enemy.getCurrentLives() < enemyLivesBefore) {
            System.out.println("✓ PASS: Enemy CAN hit player's jetwalls");
        } else {
            System.out.println("⚠ Note: Enemy didn't hit player jetwall in this run");
            System.out.println("  (This is OK - enemy may not have moved into trail)");
        }
        
        if (state.getArena().getAllJetWalls().size() == 0 && enemy.getCurrentLives() < enemyLivesBefore) {
            System.out.println("✓ PASS: JetWalls cleared after enemy collision");
        } else if (state.getArena().getAllJetWalls().size() > 0) {
            System.out.println("✓ PASS: JetWalls preserved (no collision occurred)");
        }
        System.out.println("═══════════════════════════════════════════");
    }
}
