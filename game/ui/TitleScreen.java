package game.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Title screen with main menu for FORTRON game.
 * Displays game title and menu options: Start, Load, Leaderboard, Exit.
 */
public class TitleScreen extends JPanel {
    
    private static final Color BACKGROUND_COLOR = new Color(10, 10, 20);
    private static final Color TITLE_COLOR = new Color(0, 200, 255);
    private static final Color MENU_COLOR = new Color(200, 200, 200);
    private static final Color SELECTED_COLOR = new Color(255, 200, 0);
    private static final Color BORDER_COLOR = new Color(0, 150, 200);
    
    private int selectedOption = 0;
    private MenuOption[] menuOptions;
    private TitleScreenListener listener;
    
    public enum MenuOption {
        START_GAME(1, "Start Game"),
        LOAD_GAME(2, "Load Game"),
        LEADERBOARD(3, "Leaderboard"),
        EXIT(4, "Exit");
        
        private final int number;
        private final String text;
        
        MenuOption(int number, String text) {
            this.number = number;
            this.text = text;
        }
        
        public int getNumber() { return number; }
        public String getText() { return text; }
    }
    
    public interface TitleScreenListener {
        void onStartGame();
        void onLoadGame();
        void onLeaderboard();
        void onExit();
    }
    
    public TitleScreen() {
        menuOptions = MenuOption.values();
        
        setBackground(BACKGROUND_COLOR);
        setFocusable(true);
        setPreferredSize(new Dimension(800, 600));
        
        addKeyListener(new MenuKeyListener());
        
        // Animation timer for glow effects
        Timer animationTimer = new Timer(50, e -> repaint());
        animationTimer.start();
    }
    
    public void setListener(TitleScreenListener listener) {
        this.listener = listener;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw animated grid background
        drawGridBackground(g2d, width, height);
        
        // Draw border frame
        drawBorderFrame(g2d, width, height);
        
        // Draw title
        drawTitle(g2d, width, height);
        
        // Draw menu options
        drawMenu(g2d, width, height);
        
        // Draw instructions
        drawInstructions(g2d, width, height);
    }
    
    private void drawGridBackground(Graphics2D g2d, int width, int height) {
        g2d.setColor(new Color(0, 100, 150, 30));
        g2d.setStroke(new BasicStroke(1));
        
        int gridSize = 40;
        
        // Vertical lines
        for (int x = 0; x < width; x += gridSize) {
            g2d.drawLine(x, 0, x, height);
        }
        
        // Horizontal lines
        for (int y = 0; y < height; y += gridSize) {
            g2d.drawLine(0, y, width, y);
        }
    }
    
    private void drawBorderFrame(Graphics2D g2d, int width, int height) {
        int margin = 40;
        
        // Outer glow
        g2d.setColor(new Color(0, 200, 255, 30));
        g2d.setStroke(new BasicStroke(6));
        g2d.drawRect(margin - 3, margin - 3, width - 2 * margin + 6, height - 2 * margin + 6);
        
        // Main border
        g2d.setColor(BORDER_COLOR);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(margin, margin, width - 2 * margin, height - 2 * margin);
        
        // Inner border
        g2d.setColor(new Color(0, 150, 200, 150));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(margin + 10, margin + 10, width - 2 * margin - 20, height - 2 * margin - 20);
    }
    
    private void drawTitle(Graphics2D g2d, int width, int height) {
        int titleY = height / 4;
        
        // Top separator
        drawSeparatorLine(g2d, width, titleY - 80, "=");
        
        // Main title
        g2d.setFont(new Font("Monospaced", Font.BOLD, 56));
        String title = "F O P : T R O N";
        FontMetrics fm = g2d.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        int titleX = (width - titleWidth) / 2;
        
        // Title glow effect
        g2d.setColor(new Color(0, 200, 255, 100));
        for (int i = 1; i <= 3; i++) {
            g2d.drawString(title, titleX - i, titleY - i);
            g2d.drawString(title, titleX + i, titleY + i);
        }
        
        // Title text
        g2d.setColor(TITLE_COLOR);
        g2d.drawString(title, titleX, titleY);
        
        // Bottom separator
        drawSeparatorLine(g2d, width, titleY + 40, "=");
        
        // Subtitle
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 18));
        String subtitle = "Grid Combat Challenge";
        fm = g2d.getFontMetrics();
        int subtitleWidth = fm.stringWidth(subtitle);
        int subtitleX = (width - subtitleWidth) / 2;
        
        g2d.setColor(new Color(0, 200, 255, 200));
        g2d.drawString(subtitle, subtitleX, titleY + 70);
    }
    
    private void drawSeparatorLine(Graphics2D g2d, int width, int y, String pattern) {
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 20));
        String line = pattern.repeat(30);
        FontMetrics fm = g2d.getFontMetrics();
        int lineWidth = fm.stringWidth(line);
        int lineX = (width - lineWidth) / 2;
        
        g2d.setColor(BORDER_COLOR);
        g2d.drawString(line, lineX, y);
    }
    
    private void drawMenu(Graphics2D g2d, int width, int height) {
        int menuStartY = height / 2 + 10;
        int menuSpacing = 55;
        
        g2d.setFont(new Font("Monospaced", Font.BOLD, 28));
        
        for (int i = 0; i < menuOptions.length; i++) {
            MenuOption option = menuOptions[i];
            int y = menuStartY + i * menuSpacing;
            
            String menuText = "[" + option.getNumber() + "] " + option.getText();
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(menuText);
            int x = (width - textWidth) / 2;
            
            boolean isSelected = (i == selectedOption);
            
            if (isSelected) {
                // Selected option - highlighted box
                int boxPadding = 20;
                int boxX = x - boxPadding;
                int boxY = y - fm.getAscent() - 5;
                int boxWidth = textWidth + 2 * boxPadding;
                int boxHeight = fm.getHeight() + 10;
                
                // Glow effect
                g2d.setColor(new Color(255, 200, 0, 50));
                g2d.fillRect(boxX - 3, boxY - 3, boxWidth + 6, boxHeight + 6);
                
                // Selection box
                g2d.setColor(new Color(255, 200, 0, 30));
                g2d.fillRect(boxX, boxY, boxWidth, boxHeight);
                
                // Border
                g2d.setColor(SELECTED_COLOR);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(boxX, boxY, boxWidth, boxHeight);
                
                // Arrow indicators
                g2d.setFont(new Font("Monospaced", Font.BOLD, 32));
                g2d.drawString("►", boxX - 40, y);
                g2d.drawString("◄", boxX + boxWidth + 15, y);
                
                // Text
                g2d.setFont(new Font("Monospaced", Font.BOLD, 28));
                g2d.setColor(SELECTED_COLOR);
                g2d.drawString(menuText, x, y);
                
            } else {
                // Unselected option
                g2d.setColor(MENU_COLOR);
                g2d.drawString(menuText, x, y);
            }
        }
    }
    
    private void drawInstructions(Graphics2D g2d, int width, int height) {
        int instructionY = height - 80;
        
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g2d.setColor(new Color(150, 150, 150));
        
        String[] instructions = {
            "Use ↑/↓ or 1-4 to navigate  •  Press ENTER to select",
            "ESC to exit anytime"
        };
        
        for (int i = 0; i < instructions.length; i++) {
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(instructions[i]);
            int x = (width - textWidth) / 2;
            g2d.drawString(instructions[i], x, instructionY + i * 25);
        }
    }
    
    private class MenuKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    selectedOption = (selectedOption - 1 + menuOptions.length) % menuOptions.length;
                    repaint();
                    break;
                    
                case KeyEvent.VK_DOWN:
                    selectedOption = (selectedOption + 1) % menuOptions.length;
                    repaint();
                    break;
                    
                case KeyEvent.VK_1:
                    selectedOption = 0;
                    repaint();
                    break;
                    
                case KeyEvent.VK_2:
                    selectedOption = 1;
                    repaint();
                    break;
                    
                case KeyEvent.VK_3:
                    selectedOption = 2;
                    repaint();
                    break;
                    
                case KeyEvent.VK_4:
                    selectedOption = 3;
                    repaint();
                    break;
                    
                case KeyEvent.VK_ENTER:
                    executeSelectedOption();
                    break;
                    
                case KeyEvent.VK_ESCAPE:
                    if (listener != null) {
                        listener.onExit();
                    }
                    break;
            }
        }
    }
    
    private void executeSelectedOption() {
        if (listener == null) return;
        
        MenuOption selected = menuOptions[selectedOption];
        
        switch (selected) {
            case START_GAME:
                listener.onStartGame();
                break;
            case LOAD_GAME:
                listener.onLoadGame();
                break;
            case LEADERBOARD:
                listener.onLeaderboard();
                break;
            case EXIT:
                listener.onExit();
                break;
        }
    }
    
    public int getSelectedOption() {
        return selectedOption;
    }
}
