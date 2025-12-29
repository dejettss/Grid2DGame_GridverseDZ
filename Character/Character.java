import java.sql.SQLOutput;

public abstract class Character {
    protected String name;
    protected String color;
    protected double speed;
    protected double handling;
    protected int lives;
    protected int discsOwned;
    protected int xp;
    protected int level;

    // Max level
    protected final int MAX_LEVEL = 99;

    public Character(String name, String color, double speed, double handling, int lives, int discs, int xp) {
        this.name = name;
        this.color = color;
        this.speed = speed;
        this.handling = handling;
        this.lives = lives;
        this.discsOwned = discs;
        this.xp = xp;
        this.level = 1; //We hardcode this to 1 because every new character starts at Level 1
    }

    public String getName() { return name; }
    public double getSpeed() { return speed; }
    public double getHandling () { return handling; }
    public int getLives () { return  lives; }
    public int getDiscsOwned () { return discsOwned; }
    public int getLevel () { return level; }


    public void displayInfo() {
        System.out.println("--- " + name + " (Level " + level + ") ---");
        System.out.println("Speed: " + speed + " | Handling: " + handling);
        System.out.println("Lives: " + lives + " | Discs: " + discsOwned);
    }
    private long lastDiscThrowTime = 0;

public boolean canThrowDisc() {
    return System.currentTimeMillis() - lastDiscThrowTime >= 5000
           && discsOwned > 0;
}

public void recordDiscThrow() {
    lastDiscThrowTime = System.currentTimeMillis();
    discsOwned--;
}

public void addDisc() {
    discsOwned++;
}

public void loseLife(double amount) {
    lives -= amount;
    if (lives < 0) lives = 0;
}
}





