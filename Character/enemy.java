// File: enemy.java
import java.io.*;
import java.util.*;

public class enemy extends Character {

    // --- COORDINATES (AI manages its own position) ---
    public int x = -1;
    public int y = -1;

    // --- STATS & AI FIELDS ---
    private String difficulty;
    private double aggression;
    private int xpReward;
    
    // Movement Accumulator (For speed control)
    private double moveAccumulator = 0.0;
    private final double MOVE_COST = 10.0; 

    // Combat Cooldowns
    private long lastThrowTime = 0;
    private static final int BASE_COOLDOWN = 5000;

    // --- CONSTRUCTOR ---
    // Note: Constructor name must match Class name (enemy)
    public enemy(String name, String color, String difficulty, int xpReward, double speed, double handling, double aggression) {
        // Pass stats up to the shared Character class
        super(name, color, speed, handling, 3, 1, 0); 
        this.difficulty = difficulty;
        this.xpReward = xpReward;
        this.aggression = aggression;
    }

    // --- COPY CONSTRUCTOR (For spawning clones) ---
    public enemy(enemy template) {
        super(template.name, template.color, template.speed, template.handling, template.lives, template.discsOwned, 0);
        this.difficulty = template.difficulty;
        this.xpReward = template.xpReward;
        this.aggression = template.aggression;
        this.x = -1; 
        this.y = -1;
    }

    // --- SPAWN METHOD ---
    public void spawn(Arena arena) {
        Random rand = new Random();
        boolean valid = false;
        
        // Safety Break: Don't loop forever if map is full
        int attempts = 0;
        while (!valid && attempts < 100) {
            int rx = rand.nextInt(Arena.WIDTH);
            int ry = rand.nextInt(Arena.HEIGHT);
            
            // Check your team's grid using CellType
            if (arena.getCellType(rx, ry) == CellType.EMPTY) {
                this.x = rx;
                this.y = ry;
                // Mark position on the map
                arena.setCellType(this.x, this.y, CellType.ENEMY);
                valid = true;
            }
            attempts++;
        }
    }

    // --- MOVE METHOD (The Brain) ---
    public void move(Arena arena, int targetX, int targetY) {
        this.moveAccumulator += this.speed;

        while (this.moveAccumulator >= MOVE_COST) {
            
            // 1. CLEAR OLD POSITION (Leave a Jetwall trail)
            arena.setCellType(this.x, this.y, CellType.JETWALL);

            // 2. DECIDE NEXT MOVE BASED ON DIFFICULTY
            if (difficulty.equalsIgnoreCase("Impossible") || difficulty.equalsIgnoreCase("Hard")) {
                // "Actual AI": Use A* Algorithm
                moveSmartAStar(arena, targetX, targetY);
            } 
            else if (difficulty.equalsIgnoreCase("Medium")) {
                // "Predictable": Simple Greedy Chase
                moveGreedy(arena, targetX, targetY);
            } 
            else {
                // "Erratic": Random Stumbling
                moveRandomly(arena);
            }
            
            // 3. MARK NEW POSITION
            // Only overwrite if it's not the player (collision logic handled elsewhere)
            if (arena.getCellType(this.x, this.y) != CellType.PLAYER) {
                arena.setCellType(this.x, this.y, CellType.ENEMY);
            }

            this.moveAccumulator -= MOVE_COST; 
        }
    }

    // ---------------------------------------------------------
    // ALGORITHM 1: A* PATHFINDING (Professional Game AI)
    // ---------------------------------------------------------
    private void moveSmartAStar(Arena arena, int targetX, int targetY) {
        // If close, just attack directly
        if (Math.abs(this.x - targetX) + Math.abs(this.y - targetY) <= 1) {
            return; 
        }

        Node nextStep = getAStarNextStep(arena, this.x, this.y, targetX, targetY);

        if (nextStep != null) {
            this.x = nextStep.x;
            this.y = nextStep.y;
        } else {
            // If path blocked (trapped), panic and move randomly
            moveRandomly(arena);
        }
    }

    // ---------------------------------------------------------
    // ALGORITHM 2: GREEDY CHASE (Simple AI)
    // ---------------------------------------------------------
    private void moveGreedy(Arena arena, int targetX, int targetY) {
        int xDiff = targetX - this.x;
        int yDiff = targetY - this.y;
        boolean preferHorizontal = Math.abs(xDiff) > Math.abs(yDiff);

        int bestX = this.x + (preferHorizontal ? (xDiff > 0 ? 1 : -1) : 0);
        int bestY = this.y + (!preferHorizontal ? (yDiff > 0 ? 1 : -1) : 0);

        if (isValidMove(arena, bestX, bestY)) {
            this.x = bestX; this.y = bestY;
        } else {
            // Slide along wall
            int altX = this.x + (!preferHorizontal ? (xDiff > 0 ? 1 : -1) : 0);
            int altY = this.y + (preferHorizontal ? (yDiff > 0 ? 1 : -1) : 0);
            if (isValidMove(arena, altX, altY)) {
                this.x = altX; this.y = altY;
            } else {
                moveRandomly(arena);
            }
        }
    }

    // ---------------------------------------------------------
    // ALGORITHM 3: RANDOM (Dumb AI)
    // ---------------------------------------------------------
    private void moveRandomly(Arena arena) {
        Random rand = new Random();
        for (int i=0; i<4; i++) {
            int dir = rand.nextInt(4);
            int nx = this.x, ny = this.y;
            if (dir==0) ny--; else if(dir==1) ny++; else if(dir==2) nx--; else nx++;
            
            if (isValidMove(arena, nx, ny)) {
                this.x = nx; this.y = ny;
                return;
            }
        }
    }

    // --- HELPER: Is this spot safe? ---
    private boolean isValidMove(Arena arena, int x, int y) {
        if (!arena.isInside(x, y)) return false;
        CellType type = arena.getCellType(x, y);
        // Can walk on EMPTY or PLAYER (to kill)
        return type == CellType.EMPTY || type == CellType.PLAYER;
    }

    // =========================================================
    //  A* ALGORITHM MATH (The Complex Part)
    // =========================================================
    private Node getAStarNextStep(Arena arena, int startX, int startY, int goalX, int goalY) {
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingInt(n -> n.fCost));
        boolean[][] closedList = new boolean[Arena.WIDTH][Arena.HEIGHT];

        Node startNode = new Node(startX, startY, null, 0, getDistance(startX, startY, goalX, goalY));
        openList.add(startNode);

        int maxSearchSteps = 150; // Performance Limit
        int steps = 0;

        while (!openList.isEmpty() && steps < maxSearchSteps) {
            Node current = openList.poll();
            steps++;

            if (current.x == goalX && current.y == goalY) return getFirstStep(current);
            
            // Stop if we hit the player (Goal reached)
            if (Math.abs(current.x - goalX) + Math.abs(current.y - goalY) <= 1) return getFirstStep(current);

            closedList[current.x][current.y] = true;

            int[][] dirs = {{0,1}, {0,-1}, {1,0}, {-1,0}};
            for (int[] dir : dirs) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];

                if (isValidMove(arena, nx, ny) && !closedList[nx][ny]) {
                    int newGCost = current.gCost + 1;
                    int newHCost = getDistance(nx, ny, goalX, goalY);
                    Node neighbor = new Node(nx, ny, current, newGCost, newHCost);
                    openList.add(neighbor);
                }
            }
        }
        return null;
    }

    private Node getFirstStep(Node target) {
        Node step = target;
        while (step.parent != null && step.parent.parent != null) {
            step = step.parent;
        }
        return step;
    }

    private int getDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    private class Node {
        int x, y, gCost, hCost, fCost;
        Node parent;
        public Node(int x, int y, Node parent, int g, int h) {
            this.x = x; this.y = y; this.parent = parent;
            this.gCost = g; this.hCost = h;
            this.fCost = g + h;
        }
    }

    // --- LOADING LOGIC ---
    public static ArrayList<enemy> loadEnemies(String filename) {
        ArrayList<enemy> enemyList = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue; 
                String[] data = line.split(",");
                // Basic parsing
                enemyList.add(new enemy(data[0], data[1], data[2], Integer.parseInt(data[3]), 
                              Double.parseDouble(data[4]), Double.parseDouble(data[5]), Double.parseDouble(data[6])));
            }
            scanner.close();
        } catch (Exception e) { System.out.println("Error loading enemies: " + e.getMessage()); }
        return enemyList;
    }

    // --- MAIN TESTER ---
    public static void main(String[] args) {
        Arena fakeArena = new Arena(new ArenaConfig("Test", true, false)); 
        ArrayList<enemy> templates = enemy.loadEnemies("enemies.txt");
        ArrayList<enemy> myEnemies = new ArrayList<>();
        Random rand = new Random();

        if (templates.isEmpty()) {
            System.out.println("No enemies loaded.");
            return;
        }

        System.out.println("Generating 7 Random Enemies...");
        for (int i = 0; i < 7; i++) {
            int index = rand.nextInt(templates.size());
            myEnemies.add(new enemy(templates.get(index)));
        }

        for (enemy e : myEnemies) e.spawn(fakeArena);
    }
}