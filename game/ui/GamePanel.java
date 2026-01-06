package game.ui;

import game.GameState;
import model.arena.Arena;
import model.objects.*;
import model.objects.staticobj.*;
import util.Position;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Main game panel that renders the arena and game objects.
 * Completely separated from game logic - only displays the game state.
 */
public class GamePanel extends JPanel {
    
    private static final int CELL_SIZE = 15; // Size of each grid cell in pixels
    private static final int GRID_WIDTH = Arena.GRID_WIDTH;
    private static final int GRID_HEIGHT = Arena.GRID_HEIGHT;
    
    private GameState gameState;
    private Timer renderTimer;
    
    // Neon color scheme
    private static final Color BACKGROUND_COLOR = new Color(10, 10, 20);
    private static final Color GRID_LINE_COLOR = new Color(0, 100, 150, 50);
    private static final Color PLAYER_COLOR = new Color(0, 200, 255); // Cyan
    private static final Color PLAYER_GLOW = new Color(0, 200, 255, 100);
    
    public GamePanel() {
        this.gameState = null;
        
        // Set panel size
        setPreferredSize(new Dimension(
            GRID_WIDTH * CELL_SIZE,
            GRID_HEIGHT * CELL_SIZE
        ));
        
        setBackground(BACKGROUND_COLOR);
        setFocusable(true);
        
        // Render timer (60 FPS)
        renderTimer = new Timer(16, e -> repaint());
        renderTimer.start();
    }
    
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }
    
    public void addKeyboardControls(KeyListener listener) {
        addKeyListener(listener);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing for smooth graphics
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        if (gameState == null || gameState.getArena() == null) {
            drawNoGameMessage(g2d);
            return;
        }
        
        // Draw grid lines
        drawGrid(g2d);
        
        // Draw static objects (walls, obstacles, empty cells)
        drawStaticObjects(g2d);
        
        // Draw JetWalls (light trails)
        drawJetWalls(g2d);
        
        // Draw discs
        drawDiscs(g2d);
        
        // Draw enemies
        drawEnemies(g2d);
        
        // Draw player (on top)
        drawPlayer(g2d);
        
        // Draw pause overlay if needed
        if (gameState.isGamePaused()) {
            drawPauseOverlay(g2d);
        }
        
        // Draw game over overlay if needed
        if (gameState.isGameOver()) {
            drawGameOverOverlay(g2d);
        }
    }
    
    private void drawNoGameMessage(Graphics2D g2d) {
        g2d.setColor(new Color(0, 200, 255));
        g2d.setFont(new Font("Monospaced", Font.BOLD, 24));
        
        String message = "Press F1 to Start Game";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(message);
        int x = (getWidth() - textWidth) / 2;
        int y = getHeight() / 2;
        
        g2d.drawString(message, x, y);
    }
    
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(GRID_LINE_COLOR);
        g2d.setStroke(new BasicStroke(1));
        
        // Vertical lines
        for (int x = 0; x <= GRID_WIDTH; x++) {
            int px = x * CELL_SIZE;
            g2d.drawLine(px, 0, px, GRID_HEIGHT * CELL_SIZE);
        }
        
        // Horizontal lines
        for (int y = 0; y <= GRID_HEIGHT; y++) {
            int py = y * CELL_SIZE;
            g2d.drawLine(0, py, GRID_WIDTH * CELL_SIZE, py);
        }
    }
    
    private void drawStaticObjects(Graphics2D g2d) {
        Arena arena = gameState.getArena();
        
        for (int x = 0; x < GRID_WIDTH; x++) {
            for (int y = 0; y < GRID_HEIGHT; y++) {
                Position pos = new Position(x, y);
                GameObject obj = arena.getObjectAt(pos);
                
                if (obj != null && obj.isStatic()) {
                    drawStaticObject(g2d, obj, x, y);
                }
            }
        }
    }
    
    private void drawStaticObject(Graphics2D g2d, GameObject obj, int gridX, int gridY) {
        int px = gridX * CELL_SIZE;
        int py = gridY * CELL_SIZE;
        
        if (obj instanceof Wall) {
            // Cyan neon walls
            g2d.setColor(new Color(0, 200, 255));
            g2d.fillRect(px + 1, py + 1, CELL_SIZE - 2, CELL_SIZE - 2);
            
            // Glow effect
            g2d.setColor(new Color(0, 200, 255, 50));
            g2d.fillRect(px, py, CELL_SIZE, CELL_SIZE);
            
        } else if (obj instanceof Obstacle) {
            // Orange obstacles
            g2d.setColor(new Color(255, 100, 0));
            g2d.fillRect(px + 2, py + 2, CELL_SIZE - 4, CELL_SIZE - 4);
            
            // Glow effect
            g2d.setColor(new Color(255, 100, 0, 50));
            g2d.fillRect(px, py, CELL_SIZE, CELL_SIZE);
            
        } else if (obj instanceof Derez) {
            // Dark void
            g2d.setColor(new Color(5, 5, 10));
            g2d.fillRect(px, py, CELL_SIZE, CELL_SIZE);
        }
        // Empty cells are not drawn (background shows through)
    }
    
    private void drawJetWalls(Graphics2D g2d) {
        List<JetWall> jetWalls = gameState.getArena().getAllJetWalls();
        
        for (JetWall jetWall : jetWalls) {
            Position pos = jetWall.getPosition();
            int px = pos.getX() * CELL_SIZE;
            int py = pos.getY() * CELL_SIZE;
            
            Color wallColor = jetWall.getOwnerColor();
            
            // Main trail
            g2d.setColor(wallColor);
            g2d.fillRect(px + 2, py + 2, CELL_SIZE - 4, CELL_SIZE - 4);
            
            // Glow effect
            g2d.setColor(new Color(wallColor.getRed(), wallColor.getGreen(), wallColor.getBlue(), 80));
            g2d.fillRect(px, py, CELL_SIZE, CELL_SIZE);
        }
    }
    
    private void drawDiscs(Graphics2D g2d) {
        List<Disc> discs = gameState.getDiscManager().getDiscsOnGrid();
        
        for (Disc disc : discs) {
            if (!disc.isHeld()) {
                Position pos = disc.getPosition();
                int px = pos.getX() * CELL_SIZE + CELL_SIZE / 2;
                int py = pos.getY() * CELL_SIZE + CELL_SIZE / 2;
                int radius = CELL_SIZE / 3;
                
                Color discColor = disc.getOwnerColor();
                
                // Glow effect
                g2d.setColor(new Color(discColor.getRed(), discColor.getGreen(), discColor.getBlue(), 50));
                g2d.fillOval(px - radius - 2, py - radius - 2, (radius + 2) * 2, (radius + 2) * 2);
                
                // Disc
                g2d.setColor(discColor);
                g2d.fillOval(px - radius, py - radius, radius * 2, radius * 2);
                
                // Highlight
                g2d.setColor(Color.WHITE);
                g2d.fillOval(px - radius / 2, py - radius / 2, radius / 2, radius / 2);
            }
        }
    }
    
    private void drawEnemies(Graphics2D g2d) {
        List<EnemyPlayer> enemies = gameState.getEnemies();
        
        for (EnemyPlayer enemy : enemies) {
            if (enemy.isAlive()) {
                Position pos = enemy.getPosition();
                int px = pos.getX() * CELL_SIZE;
                int py = pos.getY() * CELL_SIZE;
                
                Color enemyColor = enemy.getColor();
                
                // Glow effect
                g2d.setColor(new Color(enemyColor.getRed(), enemyColor.getGreen(), enemyColor.getBlue(), 100));
                g2d.fillOval(px - 2, py - 2, CELL_SIZE + 4, CELL_SIZE + 4);
                
                // Enemy body
                g2d.setColor(enemyColor);
                g2d.fillOval(px + 1, py + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                
                // Highlight
                g2d.setColor(Color.WHITE);
                g2d.fillOval(px + 3, py + 3, CELL_SIZE / 3, CELL_SIZE / 3);
            }
        }
    }
    
    private void drawPlayer(Graphics2D g2d) {
        Player player = gameState.getPlayer();
        
        if (player != null && player.isAlive()) {
            Position pos = player.getPosition();
            int px = pos.getX() * CELL_SIZE;
            int py = pos.getY() * CELL_SIZE;
            
            // Glow effect
            g2d.setColor(PLAYER_GLOW);
            g2d.fillOval(px - 3, py - 3, CELL_SIZE + 6, CELL_SIZE + 6);
            
            // Player body
            g2d.setColor(PLAYER_COLOR);
            g2d.fillOval(px, py, CELL_SIZE, CELL_SIZE);
            
            // Highlight
            g2d.setColor(Color.WHITE);
            g2d.fillOval(px + 2, py + 2, CELL_SIZE / 2, CELL_SIZE / 2);
            
            // Direction indicator
            drawDirectionIndicator(g2d, player, px, py);
        }
    }
    
    private void drawDirectionIndicator(Graphics2D g2d, Player player, int px, int py) {
        util.Direction dir = player.getDirection();
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        
        int centerX = px + CELL_SIZE / 2;
        int centerY = py + CELL_SIZE / 2;
        int length = CELL_SIZE / 2;
        
        int endX = centerX;
        int endY = centerY;
        
        switch (dir) {
            case UP:
                endY = centerY - length;
                break;
            case DOWN:
                endY = centerY + length;
                break;
            case LEFT:
                endX = centerX - length;
                break;
            case RIGHT:
                endX = centerX + length;
                break;
        }
        
        g2d.drawLine(centerX, centerY, endX, endY);
    }
    
    private void drawPauseOverlay(Graphics2D g2d) {
        // Semi-transparent overlay
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Pause text
        g2d.setColor(new Color(255, 200, 0));
        g2d.setFont(new Font("Monospaced", Font.BOLD, 48));
        
        String message = "PAUSED";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(message);
        int x = (getWidth() - textWidth) / 2;
        int y = getHeight() / 2;
        
        g2d.drawString(message, x, y);
        
        // Instructions
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
        String instruction = "Press P to Resume";
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(instruction);
        x = (getWidth() - textWidth) / 2;
        
        g2d.drawString(instruction, x, y + 40);
    }
    
    private void drawGameOverOverlay(Graphics2D g2d) {
        // Semi-transparent overlay
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Game over text
        g2d.setColor(gameState.isPlayerWon() ? new Color(0, 255, 100) : new Color(255, 50, 50));
        g2d.setFont(new Font("Monospaced", Font.BOLD, 36));
        
        String message = gameState.isPlayerWon() ? "LEVEL COMPLETE!" : "GAME OVER";
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(message);
        int textX = (getWidth() - textWidth) / 2;
        int textY = getHeight() / 2;
        
        g2d.drawString(message, textX, textY);
        
        // Reason
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
        String reason = gameState.getGameOverReason();
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(reason);
        textX = (getWidth() - textWidth) / 2;
        
        g2d.drawString(reason, textX, textY + 40);
        
        // Instructions
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        String instruction = gameState.isPlayerWon() ? "Press N for Next Level" : "Press R to Restart";
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(instruction);
        textX = (getWidth() - textWidth) / 2;
        
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString(instruction, textX, textY + 70);
    }
}
