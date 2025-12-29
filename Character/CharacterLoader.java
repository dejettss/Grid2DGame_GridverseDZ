import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class CharacterLoader {

    public static Character loadCharacter(String chosenName) {
        
        // FIX: Use relative path (just the filename) instead of a specific C:\Users path
        // Change this line:
        File file = new File("character.txt");
        try {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                // The text file looks like: Tron;Blue;50.0;50.0;3;1;0
                String[] data = line.split(";");

                if (data.length < 7) continue;

                // Check if line matches the character we want
                if (data[0].equalsIgnoreCase(chosenName)) {

                    String name = data[0];
                    String color = data[1];

                    // Now these will work because we changed the text file to numbers
                    double speed = Double.parseDouble(data[2]);
                    double handling = Double.parseDouble(data[3]);

                    int lives = Integer.parseInt(data[4]);
                    int discs = Integer.parseInt(data[5]);
                    int xp = Integer.parseInt(data[6]);

                    if (name.equalsIgnoreCase("Tron")) {
                        return new Tron(name, color, speed, handling, lives, discs, xp);
                    } else if (name.equalsIgnoreCase("Kevin")) {
                        return new Kevin(name, color, speed, handling, lives, discs, xp);
                    }
                }
            }
            scanner.close();

        } catch (FileNotFoundException e) {
            System.out.println("Error: 'character.txt' not found.");
            System.out.println("Current folder is: " + System.getProperty("user.dir"));
            System.out.println("Make sure the .txt file is in this folder!");
        } catch (NumberFormatException e) {
            System.out.println("Error: The text file has invalid numbers.");
        }

        return null;
    }
}
