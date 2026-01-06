package model.enemy;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads enemy data from monsters.txt file.
 * Uses File I/O to read enemy stats - no hardcoded values.
 */
public class EnemyLoader {
    
    /**
     * Loads all enemies from monsters.txt file.
     * @param filePath Path to the monsters.txt file
     * @return List of Enemy objects
     */
    public static List<Enemy> loadEnemies(String filePath) {
        List<Enemy> enemies = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean headerSkipped = false;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip empty lines and header
                if (line.isEmpty() || line.startsWith("#")) {
                    headerSkipped = true;
                    continue;
                }
                
                if (headerSkipped) {
                    Enemy enemy = parseEnemyLine(line);
                    if (enemy != null) {
                        enemies.add(enemy);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading enemies from file: " + e.getMessage());
        }
        
        return enemies;
    }
    
    /**
     * Loads a specific enemy by name.
     * @param filePath Path to the monsters.txt file
     * @param enemyName Name of the enemy to load
     * @return Enemy object or null if not found
     */
    public static Enemy loadEnemyByName(String filePath, String enemyName) {
        List<Enemy> enemies = loadEnemies(filePath);
        
        for (Enemy enemy : enemies) {
            if (enemy.getName().equalsIgnoreCase(enemyName)) {
                return enemy;
            }
        }
        
        return null;
    }
    
    /**
     * Parses a line from monsters.txt and creates appropriate Enemy subclass.
     * Format: Name | Color | Speed | Handling | Lives | Discs | XP
     */
    private static Enemy parseEnemyLine(String line) {
        try {
            String[] parts = line.split("\\|");
            
            if (parts.length != 7) {
                System.err.println("Invalid enemy data format: " + line);
                return null;
            }
            
            String name = parts[0].trim();
            String colorName = parts[1].trim();
            int speed = Integer.parseInt(parts[2].trim());
            int handling = Integer.parseInt(parts[3].trim());
            int lives = Integer.parseInt(parts[4].trim());
            int discs = Integer.parseInt(parts[5].trim());
            int xp = Integer.parseInt(parts[6].trim());
            
            // Create specific enemy type based on name
            if (name.equalsIgnoreCase("Koura")) {
                return new Koura(speed, handling, lives, discs, xp);
            } else if (name.equalsIgnoreCase("Sark")) {
                return new Sark(speed, handling, lives, discs, xp);
            } else if (name.equalsIgnoreCase("Rinzler")) {
                return new Rinzler(speed, handling, lives, discs, xp);
            } else if (name.equalsIgnoreCase("Clu")) {
                return new Clu(speed, handling, lives, discs, xp);
            } else {
                System.err.println("Unknown enemy type: " + name);
                return null;
            }
            
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in enemy data: " + line);
            return null;
        }
    }
    
    /**
     * Parses color name to Color object.
     * Note: Color is actually set by the enemy subclass constructor,
     * but this can be used for validation.
     */
    private static Color parseColor(String colorName) {
        switch (colorName.toLowerCase()) {
            case "green":
                return Color.GREEN;
            case "yellow":
                return Color.YELLOW;
            case "red":
                return Color.RED;
            case "gold":
                return new Color(255, 215, 0);
            default:
                return Color.WHITE; // Default color
        }
    }
}
