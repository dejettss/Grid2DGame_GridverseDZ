import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        System.out.println("--- Game Start ---");
        Scanner input = new Scanner(System.in);
        Character player = CharacterLoader.loadCharacter("Kevin");

        if (player != null) {

            System.out.println("\n...Defeated Enemy Bot...");

            player.displayInfo();
        }
    }
}
