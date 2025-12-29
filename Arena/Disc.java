public class Disc {

    private Position position;
    private Direction direction;
    private Player owner;
    private int rangeLeft;
    private boolean active;

    public Disc(Position start, Direction direction, Player owner) {
        this.position = new Position(start.x, start.y);
        this.direction = direction;
        this.owner = owner;
        this.rangeLeft = 3; // Tron requirement
        this.active = true;
    }

    public void move() {
        if (!active || rangeLeft <= 0) return;

        Position next = MovementUtil.getNextPosition(position, direction);
        position.x = next.x;
        position.y = next.y;
        rangeLeft--;
    }

    public Position getPosition() {
        return position;
    }

    public Player getOwner() {
        return owner;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }
}
