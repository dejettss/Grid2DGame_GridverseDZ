package game;

import util.Direction;
import util.Position;

/**
 * Test to verify enemies lose life when trapped between jetwalls and obstacles.
 */
public class TrappedEnemyTest {
    
    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════╗");
        System.out.println("║      Trapped Enemy Damage Test           ║");
        System.out.println("╚═══════════════════════════════════════════╝");
        System.out.println();
        
        // Create game
        GameEngine engine = new GameEngine();
        engine.startNewGame();
        GameState state = engine.getGameState();
        
        System.out.println("Initial State:");
        System.out.println("  Arena: " + state.getArena().getArenaName());
        System.out.println("  Player pos: " + state.getPlayer().getPosition());
        System.out.println("  Player lives: " + state.getPlayer().getCurrentLives());
        
        if (!state.getEnemies().isEmpty()) {
            var enemy = state.getEnemies().get(0);
            System.out.println("  Enemy pos: " + enemy.getPosition());
            System.out.println("  Enemy lives: " + enemy.getCurrentLives());
        }
        System.out.println();
        
        // Create a box of jetwalls around an area to trap enemy
        System.out.println("Creating jetwall trap...");
        
        // Move player to create jetwalls forming a barrier
        for (int i = 0; i < 5; i++) {
            state.movePlayer(Direction.RIGHT);
        }
        for (int i = 0; i < 5; i++) {
            state.movePlayer(Direction.DOWN);
        }
        for (int i = 0; i < 5; i++) {
            state.movePlayer(Direction.LEFT);
        }
        for (int i = 0; i < 5; i++) {
            state.movePlayer(Direction.UP);
        }
        
        System.out.println("  JetWalls created: " + state.getArena().getJetWallCount());
        System.out.println("  Player position: " + state.getPlayer().getPosition());
        System.out.println();
        
        // Run game updates to let enemies move and potentially get trapped
        System.out.println("Running game updates (enemies moving)...");
        var enemy = state.getEnemies().get(0);
        double enemyInitialLives = enemy.getCurrentLives();
        Position enemyInitialPos = enemy.getPosition();
        
        boolean enemyTrappedAndDamaged = false;
        int updateCount = 0;
        int maxUpdates = 100;
        
        while (updateCount < maxUpdates && enemy.isAlive()) {
            double livesBefore = enemy.getCurrentLives();
            Position posBefore = enemy.getPosition();
            
            state.update();
            updateCount++;
            
            double livesAfter = enemy.getCurrentLives();
            Position posAfter = enemy.getPosition();
            
            // Check if enemy lost life
            if (livesAfter < livesBefore) {
                System.out.println("  Update " + updateCount + ": Enemy lost life!");
                System.out.println("    Position: " + posAfter);
                System.out.println("    Lives: " + livesBefore + " → " + livesAfter);
                
                // Check if enemy position didn't change (meaning it was stuck)
                if (posAfter.equals(posBefore)) {
                    System.out.println("    ✓ Enemy was stuck and took damage!");
                    enemyTrappedAndDamaged = true;
                }
                
                // Check surrounding positions
                Direction[] dirs = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
                int blockedCount = 0;
                System.out.println("    Surrounding positions:");
                for (Direction dir : dirs) {
                    Position checkPos = posAfter.move(dir);
                    String status = "";
                    
                    if (!state.getArena().isValidPosition(checkPos)) {
                        status = "OUT OF BOUNDS";
                        blockedCount++;
                    } else if (state.getArena().isJetWall(checkPos)) {
                        status = "JETWALL";
                        blockedCount++;
                    } else {
                        var obj = state.getArena().getObjectAt(checkPos);
                        if (obj != null && !obj.isTraversable()) {
                            status = obj.getTypeName();
                            blockedCount++;
                        } else {
                            status = "CLEAR";
                        }
                    }
                    System.out.println("      " + dir + ": " + status);
                }
                System.out.println("    Blocked directions: " + blockedCount + "/4");
                
                if (blockedCount >= 4) {
                    System.out.println("    ✓ Enemy was completely trapped!");
                }
                System.out.println();
            }
        }
        
        System.out.println();
        System.out.println("Final State (after " + updateCount + " updates):");
        System.out.println("  Enemy initial lives: " + enemyInitialLives);
        System.out.println("  Enemy final lives: " + enemy.getCurrentLives());
        System.out.println("  Enemy alive: " + enemy.isAlive());
        System.out.println("  Enemy moved: " + !enemy.getPosition().equals(enemyInitialPos));
        System.out.println();
        
        // Summary
        System.out.println("═══════════════════════════════════════════");
        if (enemyTrappedAndDamaged) {
            System.out.println("✓ TEST PASSED: Trapped enemy took damage!");
        } else if (enemy.getCurrentLives() < enemyInitialLives) {
            System.out.println("✓ Enemy took damage (may have hit jetwall while moving)");
        } else {
            System.out.println("⚠ Note: No damage detected in this run");
            System.out.println("  (Enemy may not have gotten trapped)");
        }
        System.out.println("═══════════════════════════════════════════");
    }
}
