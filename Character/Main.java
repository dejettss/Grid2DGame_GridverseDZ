import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- TRON: LEGACY (Console Edition) ---");
        
        // 1. SETUP
        ArenaConfig config = new ArenaConfig("Grid", true, false);
        Arena arena = new Arena(config);
        Scanner scanner = new Scanner(System.in);
        
        // 2. LOAD PLAYER (Kevin)
        Character playerChar = CharacterLoader.loadCharacter("Kevin");
        if (playerChar == null) {
            System.out.println("Error: Could not load Kevin from character.txt");
            return;
        }
        // Spawn Player at top-left (ish)
        Position startPos = new Position(2, 2);
        Player player = new Player(playerChar, startPos);
        arena.setCellType(2, 2, CellType.PLAYER);

        // 3. SPAWN ENEMIES
        ArrayList<enemy> enemyList = new ArrayList<>();
        ArrayList<enemy> templates = enemy.loadEnemies("enemies.txt");
        
        System.out.println("Spawning 3 Enemies...");
        Random rand = new Random();
        for (int i = 0; i < 3; i++) {
            if (templates.isEmpty()) break;
            // Pick random template
            enemy template = templates.get(rand.nextInt(templates.size()));
            enemy clone = new enemy(template);
            clone.spawn(arena); // This puts them on the grid
            enemyList.add(clone);
        }

        // 4. GAME LOOP
        boolean running = true;
        int turn = 1;

        while (running) {
            // A. RENDER
            printArena(arena, player, enemyList);
            System.out.println("TURN " + turn + " | WASD to Move | Q to Quit");
            System.out.print("> ");
            
            // B. INPUT
            String input = scanner.nextLine().toUpperCase();
            if (input.equals("Q")) {
                running = false;
                break;
            }

            // Update Direction based on Input
            if (input.equals("W")) player.setDirection(Direction.UP);
            else if (input.equals("S")) player.setDirection(Direction.DOWN);
            else if (input.equals("A")) player.setDirection(Direction.LEFT);
            else if (input.equals("D")) player.setDirection(Direction.RIGHT);

            // C. MOVE PLAYER
            // We use the method from your teammate's Arena class
            boolean moved = arena.movePlayer(player);
            if (!moved) {
                System.out.println("CRASH! You hit a wall! GAME OVER.");
                running = false;
                break;
            }

            // D. MOVE ENEMIES (The AI Hook)
            Position pPos = player.getPosition();
            for (enemy e : enemyList) {
                // PASS PLAYER COORDINATES so A* works!
                e.move(arena, pPos.x, pPos.y);
                
                // Check Collision (Did enemy hit player?)
                if (e.x == pPos.x && e.y == pPos.y) {
                    System.out.println("CRASH! " + e.getName() + " derezzed you!");
                    running = false;
                }
            }
            
            turn++;
        }
        System.out.println("--- END OF LINE ---");
    }

    // --- HELPER: Draw the Map in Console ---
    // (Since Arena.java didn't have a print method in the file you sent)
    public static void printArena(Arena arena, Player p, ArrayList<enemy> enemies) {
        // Top Border
        for(int i=0; i<Arena.WIDTH+2; i++) System.out.print("#");
        System.out.println();

        for (int y = 0; y < Arena.HEIGHT; y++) {
            System.out.print("#"); // Left Border
            for (int x = 0; x < Arena.WIDTH; x++) {
                CellType type = arena.getCellType(x, y);
                
                String symbol = " ";
                
                if (type == CellType.WALL) symbol = "#";
                else if (type == CellType.JETWALL) symbol = "+";
                else if (type == CellType.PLAYER) symbol = "P";
                else if (type == CellType.ENEMY) {
                    symbol = "E";
                    // Try to find which enemy it is to print first letter
                    for(enemy e : enemies) {
                        if (e.x == x && e.y == y) {
                            symbol = e.getName().substring(0, 1);
                            break;
                        }
                    }
                }
                System.out.print(symbol);
            }
            System.out.println("#"); // Right Border
        }
        
        // Bottom Border
        for(int i=0; i<Arena.WIDTH+2; i++) System.out.print("#");
        System.out.println();
    }
}