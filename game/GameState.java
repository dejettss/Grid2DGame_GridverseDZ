package game;

import model.arena.Arena;
import model.arena.ArenaFactory;
import model.arena.JetWallManager;
import model.objects.*;
import model.collision.*;
import model.progression.*;
import model.enemy.*;
import model.character.*;
import util.Position;
import util.Direction;
import java.awt.Color;
import java.util.*;

/**
 * Complete game state management for TRON game.
 * Handles all game logic, entities, collision, progression, and state tracking.
 * Completely separate from UI - pure game logic.
 */
public class GameState {
    
    // Core game systems
    private Arena arena;
    private JetWallManager jetWallManager;
    private DiscManager discManager;
    private CollisionManager collisionManager;
    
    // Entities
    private Player player;
    private List<EnemyPlayer> enemies;
    private Map<String, Boolean> enemyDefeatedStatus;
    
    // Game state flags
    private boolean gameRunning;
    private boolean gamePaused;
    private boolean gameOver;
    private boolean playerWon;
    private String gameOverReason;
    
    // Level configuration
    private int currentLevelNumber;
    private LevelConfig currentLevelConfig;
    
    // Timing
    private Random random;
    private long levelStartTime;
    
    // Movement timing (frame counters for speed-based movement)
    private int playerMoveCounter;
    private Map<String, Integer> enemyMoveCounters;
    
    // Statistics
    private int enemiesDefeatedThisLevel;
    private int playerDeathsThisLevel;
    
    /**
     * Constructor initializes empty game state.
     */
    public GameState() {
        this.enemies = new ArrayList<>();
        this.enemyDefeatedStatus = new HashMap<>();
        this.gameRunning = false;
        this.gamePaused = false;
        this.gameOver = false;
        this.playerWon = false;
        this.gameOverReason = "";
        this.random = new Random();
        this.enemiesDefeatedThisLevel = 0;
        this.playerDeathsThisLevel = 0;
        this.playerMoveCounter = 0;
        this.enemyMoveCounters = new HashMap<>();
    }
    
    /**
     * Initializes game for a specific level.
     * Loads configuration, spawns entities, sets up systems.
     * 
     * @param levelNumber Level to initialize (1-16)
     */
    public void initializeLevel(int levelNumber) {
        // Validate level number
        if (!LevelManager.isValidLevel(levelNumber)) {
            throw new IllegalArgumentException("Invalid level number: " + levelNumber);
        }
        
        this.currentLevelNumber = levelNumber;
        this.currentLevelConfig = LevelManager.getLevelConfig(levelNumber);
        
        // Create arena based on level config
        arena = ArenaFactory.createArena(currentLevelConfig.getArenaType());
        
        // Initialize core systems
        jetWallManager = new JetWallManager(arena);
        discManager = new DiscManager();
        discManager.setArena(arena);
        collisionManager = new CollisionManager(arena, jetWallManager);
        
        // Clear entity tracking
        enemies.clear();
        enemyDefeatedStatus.clear();
        enemiesDefeatedThisLevel = 0;
        playerDeathsThisLevel = 0;
        
        // Clear movement counters
        playerMoveCounter = 0;
        enemyMoveCounters.clear();
        
        // Spawn player
        spawnPlayer();
        
        // Spawn enemies
        spawnEnemies();
        
        // Set game state
        gameRunning = true;
        gameOver = false;
        gamePaused = false;
        playerWon = false;
        gameOverReason = "";
        levelStartTime = System.currentTimeMillis();
    }
    
    /**
     * Spawns the player based on level configuration.
     */
    private void spawnPlayer() {
        // Load character from characters.txt
        model.character.Character characterData = null;
        try {
            String characterName = currentLevelConfig.getPlayerType() == Player.PlayerType.TRON ? 
                                   "Tron" : "Kevin";
            characterData = CharacterLoader.loadCharacterByName(characterName);
        } catch (Exception e) {
            System.err.println("Error loading character: " + e.getMessage());
        }
        
        // Use character data or defaults
        int lives = characterData != null ? characterData.getLives() : 3;
        int discs = characterData != null ? characterData.getDiscs() : 3;
        Color playerColor = characterData != null ? characterData.getColor() : 
                           (currentLevelConfig.getPlayerType() == Player.PlayerType.TRON ? 
                            new Color(0, 150, 255) : new Color(255, 140, 0));
        
        // Spawn in random safe quadrant
        Position playerStart = getRandomSpawnInQuadrant(1); // Top-left quadrant
        
        player = new Player(
            "Player",
            currentLevelConfig.getPlayerType(),
            playerStart,
            playerColor,
            lives,
            discs
        );
        
        // Set the XP threshold to match this game level's requirement
        if (player.getLevelingSystem() != null) {
            player.getLevelingSystem().setXPThreshold(currentLevelConfig.getXpThreshold());
        }
        
        // Register with systems
        collisionManager.registerEntity("Player", lives);
        jetWallManager.registerEntity("Player", playerColor, playerStart);
        
        // Create and register discs
        discManager.createDiscsForEntity("Player", playerColor, playerStart, discs);
        for (Disc disc : discManager.getEntityDiscs("Player")) {
            collisionManager.registerDisc("Player", disc);
        }
    }
    
    /**
     * Spawns enemies based on level configuration.
     */
    private void spawnEnemies() {
        int enemyCount = currentLevelConfig.getEnemyCount();
        EnemyType enemyType = currentLevelConfig.getEnemyType();
        
        // Load enemy data from monsters.txt
        Enemy enemyTemplate = null;
        try {
            enemyTemplate = EnemyLoader.loadEnemyByName("monsters.txt", enemyType.getDisplayName());
        } catch (Exception e) {
            System.err.println("Error loading enemy: " + e.getMessage());
        }
        
        // Use enemy data or defaults
        int lives = enemyTemplate != null ? enemyTemplate.getLives() : 2;
        int discs = enemyTemplate != null ? enemyTemplate.getDiscs() : 2;
        Color enemyColor = enemyTemplate != null ? enemyTemplate.getColor() : getDefaultEnemyColor(enemyType);
        int xpReward = enemyTemplate != null ? enemyTemplate.getXp() : 50;
        
        // Determine XP reward type
        EnemyXPReward xpRewardType = determineXPRewardType(enemyType);
        
        // Spawn enemies in different quadrants
        int[] quadrants = {2, 3, 4, 2}; // Top-right, bottom-left, bottom-right, top-right
        
        for (int i = 0; i < enemyCount; i++) {
            int quadrant = quadrants[i % quadrants.length];
            Position enemyStart = getRandomSpawnInQuadrant(quadrant);
            
            String enemyId = enemyType.getDisplayName() + (i + 1);
            
            EnemyPlayer enemy = new EnemyPlayer(
                enemyId,
                enemyStart,
                enemyColor,
                lives,
                discs,
                xpRewardType
            );
            
            enemies.add(enemy);
            enemyDefeatedStatus.put(enemyId, false);
            
            // Initialize movement counter for this enemy
            enemyMoveCounters.put(enemyId, 0);
            
            // Register with systems
            collisionManager.registerEntity(enemyId, lives);
            jetWallManager.registerEntity(enemyId, enemyColor, enemyStart);
            
            // Create and register discs
            discManager.createDiscsForEntity(enemyId, enemyColor, enemyStart, discs);
            for (Disc disc : discManager.getEntityDiscs(enemyId)) {
                collisionManager.registerDisc(enemyId, disc);
            }
        }
    }
    
    /**
     * Gets default enemy color based on type.
     */
    private Color getDefaultEnemyColor(EnemyType type) {
        switch (type) {
            case CLU:
                return new Color(255, 215, 0); // Gold
            case SARK:
                return Color.YELLOW;
            case RINZLER:
                return Color.RED;
            case KOURA:
                return Color.GREEN;
            default:
                return new Color(255, 100, 100);
        }
    }
    
    /**
     * Determines XP reward type based on enemy type.
     */
    private EnemyXPReward determineXPRewardType(EnemyType type) {
        switch (type) {
            case KOURA:
                return EnemyXPReward.BASIC_ENEMY;
            case SARK:
                return EnemyXPReward.BASIC_ENEMY;
            case RINZLER:
                return EnemyXPReward.ELITE_ENEMY;
            case CLU:
                return EnemyXPReward.BOSS_ENEMY;
            default:
                return EnemyXPReward.BASIC_ENEMY;
        }
    }
    
    /**
     * Gets a random spawn position within the specified quadrant.
     * Ensures position is empty and away from edges.
     * 
     * Quadrants:
     *  1 | 2
     *  -----
     *  3 | 4
     */
    private Position getRandomSpawnInQuadrant(int quadrant) {
        int minX, maxX, minY, maxY;
        int padding = 3; // Keep away from edges
        
        int midX = Arena.GRID_WIDTH / 2;
        int midY = Arena.GRID_HEIGHT / 2;
        
        switch (quadrant) {
            case 1: // Top-left
                minX = padding;
                maxX = midX - padding;
                minY = padding;
                maxY = midY - padding;
                break;
            case 2: // Top-right
                minX = midX + padding;
                maxX = Arena.GRID_WIDTH - padding;
                minY = padding;
                maxY = midY - padding;
                break;
            case 3: // Bottom-left
                minX = padding;
                maxX = midX - padding;
                minY = midY + padding;
                maxY = Arena.GRID_HEIGHT - padding;
                break;
            case 4: // Bottom-right
                minX = midX + padding;
                maxX = Arena.GRID_WIDTH - padding;
                minY = midY + padding;
                maxY = Arena.GRID_HEIGHT - padding;
                break;
            default:
                minX = padding;
                maxX = Arena.GRID_WIDTH - padding;
                minY = padding;
                maxY = Arena.GRID_HEIGHT - padding;
        }
        
        // Try to find an empty position
        Position spawnPos;
        int attempts = 0;
        int maxAttempts = 100;
        
        do {
            int x = minX + random.nextInt(Math.max(1, maxX - minX));
            int y = minY + random.nextInt(Math.max(1, maxY - minY));
            spawnPos = new Position(x, y);
            attempts++;
            
            if (attempts >= maxAttempts) {
                break; // Give up and use this position
            }
        } while (!arena.isCellEmpty(spawnPos));
        
        return spawnPos;
    }
    
    /**
     * Main game update loop.
     * Called every frame to update all game logic.
     */
    public void update() {
        if (!gameRunning || gamePaused || gameOver) {
            return;
        }
        
        // Update player auto-movement
        updatePlayer();
        
        // Update enemies every game tick (synchronized with game loop)
        updateEnemies();
        
        // Update discs (projectiles)
        updateDiscs();
        
        // Auto-recapture discs
        autoRecaptureDiscs();
        
        // Check for defeated enemies and award XP
        checkDefeatedEnemies();
        
        // Check win/lose conditions
        checkGameStatus();
    }
    
    /**
     * Updates player auto-movement based on current direction and speed.
     */
    private void updatePlayer() {
        if (!player.isAlive()) {
            return;
        }
        
        // Increment player movement counter
        playerMoveCounter++;
        
        // Check if player should move based on speed
        int moveInterval = calculateMoveInterval(player.getSpeed());
        
        if (playerMoveCounter >= moveInterval) {
            // Reset counter and move in current direction
            playerMoveCounter = 0;
            
            // Move player in their current facing direction
            boolean moved = moveEntity(player, "Player", player.getDirection());
            
            // If blocked, player stays in place but keeps trying
            if (!moved) {
                // Player is blocked, check if trapped
                if (isEntityTrapped(player.getPosition(), "Player")) {
                    // Player is trapped - force damage
                    handleTrappedEntity(player, "Player");
                }
            }
        }
    }
    
    /**
     * Updates all enemy AI and movement.
     */
    private void updateEnemies() {
        for (int i = 0; i < enemies.size(); i++) {
            EnemyPlayer enemy = enemies.get(i);
            String enemyId = enemy.getName();
            
            if (enemy.isAlive()) {
                // Increment movement counter
                int counter = enemyMoveCounters.getOrDefault(enemyId, 0) + 1;
                enemyMoveCounters.put(enemyId, counter);
                
                // Check if enemy should move based on speed
                // Speed determines movement frequency: higher speed = move more often
                // Speed 1 = every 10 frames, Speed 2 = every 5 frames, Speed 3 = every 3 frames, Speed 4 = every 2 frames
                int moveInterval = calculateMoveInterval(enemy.getSpeed());
                
                if (counter >= moveInterval) {
                    // Reset counter and allow movement
                    enemyMoveCounters.put(enemyId, 0);
                    
                    // Get next direction using appropriate AI based on enemy type
                    Direction nextDir = calculateEnemyAIMove(enemy, enemyId);
                    
                    // Move enemy - if move fails (blocked), enemy stays in place
                    boolean moved = moveEntity(enemy, enemyId, nextDir);
                    
                    // Update enemy's facing direction even if blocked
                    if (!moved) {
                        enemy.setDirection(nextDir);
                        
                        // Check if enemy is completely trapped (all directions blocked)
                        if (isEntityTrapped(enemy.getPosition(), enemyId)) {
                            // Enemy is trapped - force damage from surrounding obstacles/jetwalls
                            handleTrappedEntity(enemy, enemyId);
                        }
                    }
                }
                
                // Smart disc throwing based on enemy type (not affected by speed)
                if (shouldEnemyThrowDisc(enemy) && discManager.canThrow(enemyId)) {
                    int range = calculateDiscRange(enemy);
                    discManager.throwDisc(enemyId, enemy.getDirection(), range);
                }
            }
        }
        
        // Check for face-to-face collisions between player and enemies
        checkFaceToFaceCollisions();
    }
    
    /**
     * Calculates movement interval based on speed.
     * Higher speed = shorter interval = moves more frequently.
     * Based on 60 FPS game loop (~16ms per frame).
     * 4X SPEED for ultra fast-paced gameplay!
     * 
     * @param speed Speed attribute (1-4 typically)
     * @return Number of frames between movements
     */
    private int calculateMoveInterval(int speed) {
        // At 60 FPS, create ultra fast-paced movement (4x faster!)
        // Speed 1: 4 frames (~0.067 seconds) - Slow (15 moves/sec)
        // Speed 2: 2 frames (~0.033 seconds) - Medium (30 moves/sec)
        // Speed 3: 1 frame (~0.017 seconds) - Fast (60 moves/sec)
        // Speed 4: 1 frame (~0.017 seconds) - Very Fast (60 moves/sec!)
        // Speed 5+: 1 frame - Max speed (60 moves/sec - every frame!)
        
        switch (speed) {
            case 1:
                return 4;  // Slow - 15 moves per second
            case 2:
                return 2;  // Medium - 30 moves per second
            case 3:
                return 1;  // Fast - 60 moves per second (every frame!)
            case 4:
                return 1;  // Very Fast - 60 moves per second (every frame!)
            default:
                return 1;  // Max speed - every frame
        }
    }
    
    /**
     * Calculates enemy move using appropriate AI based on enemy type.
     */
    private Direction calculateEnemyAIMove(EnemyPlayer enemy, String enemyId) {
        // Get enemy type from name
        EnemyType type = getEnemyTypeFromName(enemyId);
        
        // Collect information for AI
        Position enemyPos = enemy.getPosition();
        Position playerPos = player.getPosition();
        Direction playerDir = player.getDirection();
        Set<Position> jetWallPositions = new HashSet<>(arena.getAllJetWallPositions());
        
        // Get other enemy positions for coordination (Rinzler)
        List<Position> allyPositions = new ArrayList<>();
        for (EnemyPlayer other : enemies) {
            if (other != enemy && other.isAlive()) {
                allyPositions.add(other.getPosition());
            }
        }
        
        // Call appropriate AI based on type
        switch (type) {
            case KOURA:
                // Simple erratic AI
                model.enemy.Koura kouraAI = new model.enemy.Koura(1, 1, enemy.getCurrentLivesInt(), enemy.getMaxDiscs(), 0);
                return kouraAI.calculateNextMove(enemyPos, playerPos, jetWallPositions);
                
            case SARK:
                // Standard enforcer AI with basic prediction
                model.enemy.Sark sarkAI = new model.enemy.Sark(2, 2, enemy.getCurrentLivesInt(), enemy.getMaxDiscs(), 0);
                return sarkAI.calculateNextMove(enemyPos, playerPos, playerDir, jetWallPositions);
                
            case RINZLER:
                // Advanced hunter AI with coordination
                model.enemy.Rinzler rinzlerAI = new model.enemy.Rinzler(3, 3, enemy.getCurrentLivesInt(), enemy.getMaxDiscs(), 0);
                return rinzlerAI.calculateNextMove(enemyPos, playerPos, playerDir, jetWallPositions, allyPositions);
                
            case CLU:
                // Boss-level brilliant AI
                model.enemy.Clu cluAI = new model.enemy.Clu(4, 4, enemy.getCurrentLivesInt(), enemy.getMaxDiscs(), 0);
                return cluAI.calculateNextMove(enemyPos, playerPos, playerDir, arena, jetWallPositions);
                
            default:
                // Fallback to simple random movement
                enemy.update();
                return enemy.getNextDirection();
        }
    }
    
    /**
     * Gets enemy type from enemy name/ID.
     */
    private EnemyType getEnemyTypeFromName(String enemyId) {
        if (enemyId.contains("Koura")) return EnemyType.KOURA;
        if (enemyId.contains("Sark")) return EnemyType.SARK;
        if (enemyId.contains("Rinzler")) return EnemyType.RINZLER;
        if (enemyId.contains("Clu")) return EnemyType.CLU;
        return EnemyType.KOURA; // Default
    }
    
    /**
     * Determines if enemy should throw disc based on AI level.
     */
    private boolean shouldEnemyThrowDisc(EnemyPlayer enemy) {
        String enemyId = enemy.getName();
        EnemyType type = getEnemyTypeFromName(enemyId);
        
        // Different throw rates based on enemy intelligence
        switch (type) {
            case KOURA:
                return random.nextInt(100) < 2; // 2% chance (erratic)
            case SARK:
                return random.nextInt(100) < 5; // 5% chance (standard)
            case RINZLER:
                return random.nextInt(100) < 8; // 8% chance (tactical)
            case CLU:
                return random.nextInt(100) < 12; // 12% chance (aggressive)
            default:
                return random.nextInt(100) < 5;
        }
    }
    
    /**
     * Calculates disc throw range based on enemy type.
     */
    private int calculateDiscRange(EnemyPlayer enemy) {
        String enemyId = enemy.getName();
        EnemyType type = getEnemyTypeFromName(enemyId);
        
        // Different ranges based on enemy skill
        switch (type) {
            case KOURA:
                return 1 + random.nextInt(2); // 1-2 range (poor aim)
            case SARK:
                return 1 + random.nextInt(3); // 1-3 range (average)
            case RINZLER:
                return 2 + random.nextInt(2); // 2-3 range (precise)
            case CLU:
                return 2 + random.nextInt(2); // 2-3 range (strategic)
            default:
                return 1 + random.nextInt(3);
        }
    }
    
    /**
     * Checks for face-to-face collisions between player and enemies.
     * When player and enemy occupy the same position, player loses 1 life.
     */
    private void checkFaceToFaceCollisions() {
        if (!player.isAlive()) {
            return;
        }
        
        Position playerPos = player.getPosition();
        
        for (EnemyPlayer enemy : enemies) {
            if (enemy.isAlive() && enemy.getPosition().equals(playerPos)) {
                // Face-to-face collision detected! Player loses 1 life
                double livesBefore = collisionManager.getEntityLives("Player");
                collisionManager.setEntityLives("Player", livesBefore - 1.0);
                updateEntityLives("Player");
                double livesAfter = collisionManager.getEntityLives("Player");
                
                // Debug logging
                System.out.println("[FACE-TO-FACE COLLISION] Player hit " + enemy.getName() + 
                                 " at " + playerPos + " | Lives: " + livesBefore + " → " + livesAfter);
                
                // Only apply damage once per update cycle
                break;
            }
        }
    }
    
    /**
     * Checks if an entity is completely trapped (all 4 directions blocked).
     * 
     * @param currentPos Entity's current position
     * @param entityId Entity identifier (for logging)
     * @return true if entity cannot move in any direction
     */
    private boolean isEntityTrapped(Position currentPos, String entityId) {
        // Check all 4 directions
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        int blockedDirections = 0;
        
        for (Direction dir : directions) {
            Position checkPos = currentPos.move(dir);
            
            // Check if this direction is blocked
            if (!arena.isValidPosition(checkPos)) {
                blockedDirections++;
                continue;
            }
            
            // Check for jetwall
            if (arena.isJetWall(checkPos)) {
                blockedDirections++;
                continue;
            }
            
            // Check for non-traversable objects (walls, obstacles)
            GameObject obj = arena.getObjectAt(checkPos);
            if (obj != null && !obj.isTraversable()) {
                blockedDirections++;
                continue;
            }
        }
        
        // Entity is trapped if all 4 directions are blocked
        return blockedDirections >= 4;
    }
    
    /**
     * Handles an entity that is completely trapped.
     * Forces the entity to take damage from surrounding hazards.
     * 
     * @param entity The trapped entity
     * @param entityId Entity identifier
     */
    private void handleTrappedEntity(DynamicObject entity, String entityId) {
        Position currentPos = entity.getPosition();
        
        // Check all 4 directions to find a jetwall to "hit"
        Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        boolean hitJetWall = false;
        
        for (Direction dir : directions) {
            Position checkPos = currentPos.move(dir);
            
            if (arena.isValidPosition(checkPos) && arena.isJetWall(checkPos)) {
                // Force entity to take jetwall damage
                double livesBefore = collisionManager.getEntityLives(entityId);
                collisionManager.processMovementCollision(entityId, checkPos);
                updateEntityLives(entityId);
                double livesAfter = collisionManager.getEntityLives(entityId);
                
                // Debug logging
                System.out.println("[TRAPPED ENTITY] " + entityId + " is trapped at " + currentPos + 
                                 " | Forced jetwall damage | Lives: " + livesBefore + " → " + livesAfter);
                
                hitJetWall = true;
                break; // Only apply damage once per update
            }
        }
        
        // If no jetwall but still trapped by walls/obstacles, apply generic damage
        if (!hitJetWall) {
            double livesBefore = collisionManager.getEntityLives(entityId);
            collisionManager.setEntityLives(entityId, livesBefore - 0.5);
            updateEntityLives(entityId);
            double livesAfter = collisionManager.getEntityLives(entityId);
            
            // Debug logging
            System.out.println("[TRAPPED ENTITY] " + entityId + " is trapped by obstacles at " + currentPos + 
                             " | Forced damage | Lives: " + livesBefore + " → " + livesAfter);
        }
    }
    
    /**
     * Updates all active discs.
     */
    private void updateDiscs() {
        for (Disc disc : discManager.getDiscsOnGrid()) {
            disc.update();
        }
    }
    
    /**
     * Auto-recaptures discs when entities move over them.
     */
    private void autoRecaptureDiscs() {
        if (player.isAlive()) {
            discManager.recaptureDisc("Player", player.getPosition());
        }
        
        for (EnemyPlayer enemy : enemies) {
            if (enemy.isAlive()) {
                discManager.recaptureDisc(enemy.getName(), enemy.getPosition());
            }
        }
    }
    
    /**
     * Checks for defeated enemies and awards XP.
     * Only awards XP once per enemy.
     */
    private void checkDefeatedEnemies() {
        for (EnemyPlayer enemy : enemies) {
            String enemyId = enemy.getName();
            
            // Check if enemy was just defeated
            if (!enemy.isAlive() && !enemyDefeatedStatus.get(enemyId)) {
                // Award XP to player
                int xpReward = enemy.getXPReward();
                boolean leveledUp = player.addXP(xpReward);
                
                // Mark as defeated
                enemyDefeatedStatus.put(enemyId, true);
                enemiesDefeatedThisLevel++;
                
                // Clear the defeated enemy's jetwalls
                jetWallManager.clearEntityJetWalls(enemyId);
                
                // Debug logging
                System.out.println("[ENEMY DEFEATED] " + enemyId + " defeated | JetWalls cleared");
                
                // Handle level up
                if (leveledUp) {
                    handlePlayerLevelUp();
                }
            }
        }
    }
    
    /**
     * Handles player level up with rewards.
     */
    private void handlePlayerLevelUp() {
        // Player class already handles rewards in addXP()
        // This is just for logging/notifications
        System.out.println("LEVEL UP! Player is now level " + player.getLevel());
        System.out.println("Gained +1 life and +1 disc!");
        System.out.println("  Total lives: " + player.getMaxLives());
        System.out.println("  Total discs: " + player.getMaxDiscs());
    }
    
    /**
     * Checks win/lose conditions.
     */
    private void checkGameStatus() {
        // Condition 1: Player loses all lives
        if (!player.isAlive()) {
            gameOver = true;
            playerWon = false;
            gameRunning = false;
            gameOverReason = "Player lost all lives";
            playerDeathsThisLevel++;
            
            // Clear the player's jetwalls when they die
            jetWallManager.clearEntityJetWalls("Player");
            
            // Debug logging
            System.out.println("[PLAYER DEFEATED] Player died | JetWalls cleared");
            
            return;
        }
        
        // Condition 2: All enemies defeated
        boolean allEnemiesDefeated = enemies.stream().allMatch(e -> !e.isAlive());
        
        if (allEnemiesDefeated && enemies.size() > 0) {
            gameOver = true;
            playerWon = true;
            gameRunning = false;
            gameOverReason = "All enemies defeated!";
            
            // Fill XP bar to show completion (visual feedback)
            if (player != null && player.getLevelingSystem() != null) {
                player.getLevelingSystem().fillXPBar();
            }
        }
    }
    
    /**
     * Changes the player's movement direction.
     * Player moves automatically - this only changes direction (like classic TRON).
     * 
     * @param direction New direction to face and move
     * @return true always (direction change successful)
     */
    public boolean movePlayer(Direction direction) {
        if (!player.isAlive() || !gameRunning || gamePaused) {
            return false;
        }
        
        // Only change direction - movement happens automatically in update()
        player.setDirection(direction);
        return true;
    }
    
    /**
     * Sets the player's direction (for auto-movement system).
     * This is a simplified version of movePlayer for UI controls.
     * 
     * @param direction New direction for player to move
     */
    public void setPlayerDirection(Direction direction) {
        if (player != null && player.isAlive() && gameRunning && !gamePaused) {
            player.setDirection(direction);
        }
    }
    
    /**
     * Moves an entity with full collision checking.
     * Checks walls, obstacles, JetWalls, discs, and arena boundaries.
     * 
     * @param entity Entity to move
     * @param entityId Entity identifier
     * @param direction Direction to move
     * @return true if move succeeded, false if blocked
     */
    private boolean moveEntity(DynamicObject entity, String entityId, Direction direction) {
        Position currentPos = entity.getPosition();
        Position newPos = currentPos.move(direction);
        
        // Check arena boundaries
        if (!arena.isValidPosition(newPos)) {
            // Handle falling off open arena
            if (arena.isFallingIntoDerez(newPos)) {
                // Instant death in open arena
                collisionManager.setEntityLives(entityId, 0.0);
                updateEntityLives(entityId);
                
                // Clear the entity's jetwalls when they fall into derez
                jetWallManager.clearEntityJetWalls(entityId);
                
                // Debug logging
                System.out.println("[FALL INTO DEREZ] " + entityId + " fell off the arena at " + newPos + 
                                 " | JetWalls cleared");
                
                return false;
            }
            // Blocked by boundary
            return false;
        }
        
        // Check for JetWall collision FIRST (before other obstacles)
        if (arena.isJetWall(newPos)) {
            // Entity hit a JetWall - apply damage and clear all jetwalls
            // ANY jetwall causes damage (ownership doesn't matter)
            double livesBefore = collisionManager.getEntityLives(entityId);
            collisionManager.processMovementCollision(entityId, newPos);
            updateEntityLives(entityId);
            double livesAfter = collisionManager.getEntityLives(entityId);
            
            // Debug logging
            System.out.println("[JETWALL HIT] " + entityId + " hit jetwall at " + newPos + 
                             " | Lives: " + livesBefore + " → " + livesAfter);
            
            // JetWalls are auto-cleared by processMovementCollision
            // Entity can't move into the jetwall position
            return false;
        }
        
        // Check for walls and obstacles AFTER jetwall check
        GameObject objAtNewPos = arena.getObjectAt(newPos);
        if (objAtNewPos != null && !objAtNewPos.isTraversable()) {
            // Blocked by wall or obstacle - can't move
            return false;
        }
        
        // Process disc collisions
        CollisionResult discResult = collisionManager.checkDiscCollisionAtPosition(entityId, newPos);
        if (discResult != null) {
            // Hit by a disc - take damage but can still move
            updateEntityLives(entityId);
        }
        
        // Move is valid - update position
        entity.setPosition(newPos);
        
        // Create JetWall at previous position
        jetWallManager.moveEntity(entityId, newPos);
        
        // Update held disc positions
        discManager.updateEntityPosition(entityId, newPos);
        
        return true;
    }
    
    /**
     * Updates entity lives from collision manager.
     */
    private void updateEntityLives(String entityId) {
        double lives = collisionManager.getEntityLives(entityId);
        
        if (entityId.equals("Player")) {
            player.setCurrentLives(lives);
        } else {
            for (EnemyPlayer enemy : enemies) {
                if (enemy.getName().equals(entityId)) {
                    enemy.setCurrentLives(lives);
                    break;
                }
            }
        }
    }
    
    /**
     * Player throws a disc.
     * 
     * @param range Throw range (1-3)
     * @return true if disc was thrown, false otherwise
     */
    public boolean playerThrowDisc(int range) {
        if (!player.isAlive() || !gameRunning || gamePaused) {
            return false;
        }
        
        if (!discManager.canThrow("Player")) {
            return false;
        }
        
        // Validate range
        range = Math.max(1, Math.min(3, range));
        
        Position landingPos = discManager.throwDisc("Player", player.getDirection(), range);
        return landingPos != null;
    }
    
    /**
     * Toggles pause state.
     */
    public void togglePause() {
        if (gameRunning && !gameOver) {
            gamePaused = !gamePaused;
        }
    }
    
    /**
     * Manually exits the game (player quits).
     */
    public void exitGame() {
        gameOver = true;
        playerWon = false;
        gameRunning = false;
        gameOverReason = "Player exited";
    }
    
    /**
     * Gets elapsed time for current level in milliseconds.
     */
    public long getElapsedTime() {
        if (levelStartTime == 0) {
            return 0;
        }
        return System.currentTimeMillis() - levelStartTime;
    }
    
    // ==================== GETTERS ====================
    
    public Arena getArena() {
        return arena;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public List<EnemyPlayer> getEnemies() {
        return Collections.unmodifiableList(enemies);
    }
    
    public JetWallManager getJetWallManager() {
        return jetWallManager;
    }
    
    public DiscManager getDiscManager() {
        return discManager;
    }
    
    public CollisionManager getCollisionManager() {
        return collisionManager;
    }
    
    public boolean isGameRunning() {
        return gameRunning;
    }
    
    public boolean isGamePaused() {
        return gamePaused;
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public boolean isPlayerWon() {
        return playerWon;
    }
    
    public String getGameOverReason() {
        return gameOverReason;
    }
    
    public int getCurrentLevelNumber() {
        return currentLevelNumber;
    }
    
    public LevelConfig getCurrentLevelConfig() {
        return currentLevelConfig;
    }
    
    public int getEnemiesDefeatedThisLevel() {
        return enemiesDefeatedThisLevel;
    }
    
    public int getPlayerDeathsThisLevel() {
        return playerDeathsThisLevel;
    }
    
    public int getAliveEnemyCount() {
        return (int) enemies.stream().filter(EnemyPlayer::isAlive).count();
    }
    
    public int getTotalEnemyCount() {
        return enemies.size();
    }
}
