public class CollisionManager {

    // Disc vs Player / Wall / Boundary
    public static void handleDiscCollision(Disc disc, Arena arena, Player player) {

        if (!disc.isActive()) return;

        int x = disc.getPosition().x;
        int y = disc.getPosition().y;

        // Out of bounds → disc stops
        if (!arena.isInside(x, y)) {
            disc.deactivate();
            return;
        }

        CellType cell = arena.getCellType(x, y);

        // Hit wall or jetwall → disc stops
        if (cell == CellType.WALL || cell == CellType.JETWALL) {
            disc.deactivate();
            return;
        }

        // Hit player
        if (player.getPosition().x == x &&
            player.getPosition().y == y &&
            disc.getOwner() != player) {

            player.getCharacter().loseLife(1);
            disc.deactivate();

            System.out.println("Disc hit! -1 life");
        }
    }

    // Player vs Jetwall / Boundary
    public static void handlePlayerCollision(Arena arena, Player player) {

        int x = player.getPosition().x;
        int y = player.getPosition().y;

        // Fell off open arena
        if (!arena.isInside(x, y)) {
            player.getCharacter().loseLife(player.getCharacter().getLives());
            System.out.println("Fell off arena! All lives lost.");
            return;
        }

        // Jetwall collision
        if (arena.getCellType(x, y) == CellType.JETWALL) {
            player.getCharacter().loseLife(0.5);
            System.out.println("Jetwall collision! -0.5 life");
        }
    }

    // Disc recapture
    public static void handleRecapture(Disc disc, Player player) {

        if (!disc.isActive()) return;

        if (disc.getPosition().x == player.getPosition().x &&
            disc.getPosition().y == player.getPosition().y &&
            disc.getOwner().getCharacter().getColor()
                .equals(player.getCharacter().getColor())) {

            player.getCharacter().addDisc();
            disc.deactivate();
            System.out.println("Disc recaptured!");
        }
    }
}

