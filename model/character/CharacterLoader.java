package model.character;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads character data from characters.txt file.
 * Handles file I/O and parsing of character attributes.
 */
public class CharacterLoader {
    private static final String CHARACTERS_FILE = "characters.txt";
    
    /**
     * Loads all characters from the characters.txt file.
     * @return List of Character objects
     * @throws IOException if file cannot be read
     */
    public static List<Character> loadCharacters() throws IOException {
        return loadCharacters(CHARACTERS_FILE);
    }
    
    /**
     * Loads all characters from a specified file.
     * @param filename Path to the characters file
     * @return List of Character objects
     * @throws IOException if file cannot be read
     */
    public static List<Character> loadCharacters(String filename) throws IOException {
        List<Character> characters = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // Skip empty lines and comments
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    isFirstLine = false;
                    continue;
                }
                
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // Parse character data
                Character character = parseCharacterLine(line);
                if (character != null) {
                    characters.add(character);
                }
            }
        }
        
        return characters;
    }
    
    /**
     * Parses a single line of character data.
     * Format: CharacterName | Color | Speed | Handling | Lives | Discs | XP
     * Creates specific character instances (Tron or Kevin) based on name.
     */
    private static Character parseCharacterLine(String line) {
        try {
            // Split by pipe character and trim whitespace
            String[] parts = line.split("\\|");
            if (parts.length != 7) {
                System.err.println("Invalid character data format: " + line);
                return null;
            }
            
            String name = parts[0].trim();
            Color color = parseColor(parts[1].trim());
            int speed = Integer.parseInt(parts[2].trim());
            int handling = Integer.parseInt(parts[3].trim());
            int lives = Integer.parseInt(parts[4].trim());
            int discs = Integer.parseInt(parts[5].trim());
            int xp = Integer.parseInt(parts[6].trim());
            
            // Create specific character instances based on name
            if (name.equalsIgnoreCase("Tron")) {
                return new Tron(speed, handling, lives, discs, xp);
            } else if (name.equalsIgnoreCase("Kevin")) {
                return new Kevin(speed, handling, lives, discs, xp);
            } else {
                // Generic character for unknown types
                return new Character(name, color, speed, handling, lives, discs, xp);
            }
            
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric values in line: " + line);
            return null;
        }
    }
    
    /**
     * Parses color string to Color object.
     */
    private static Color parseColor(String colorName) {
        switch (colorName.toLowerCase()) {
            case "blue":
                return Color.BLUE;
            case "white":
                return Color.WHITE;
            case "red":
                return Color.RED;
            case "green":
                return Color.GREEN;
            case "yellow":
                return Color.YELLOW;
            case "orange":
                return Color.ORANGE;
            case "cyan":
                return Color.CYAN;
            case "magenta":
                return Color.MAGENTA;
            default:
                return Color.WHITE; // Default color
        }
    }
    
    /**
     * Loads a specific character by name.
     */
    public static Character loadCharacterByName(String characterName) throws IOException {
        List<Character> characters = loadCharacters();
        for (Character character : characters) {
            if (character.getName().equalsIgnoreCase(characterName)) {
                return character;
            }
        }
        return null;
    }
    
    /**
     * Gets the number of available characters.
     */
    public static int getCharacterCount() {
        try {
            return loadCharacters().size();
        } catch (IOException e) {
            return 0;
        }
    }
    
    /**
     * Checks if a character exists in the file.
     */
    public static boolean characterExists(String characterName) {
        try {
            return loadCharacterByName(characterName) != null;
        } catch (IOException e) {
            return false;
        }
    }
}
