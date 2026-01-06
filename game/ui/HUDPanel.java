package game.ui;

import game.GameEngine;
import game.GameState;
import model.objects.Player;

import javax.swing.*;
import java.awt.*;

/**
 * Heads-Up Display panel showing game statistics and information.
 * Displays level info, player stats, lives, discs, and controls.
 */
public class HUDPanel extends JPanel {
    
    private GameEngine gameEngine;
    private Timer updateTimer;
    
    // UI Components
    private JLabel levelLabel;
    private JLabel chapterLabel;
    private JLabel livesLabel;
    private JLabel discsLabel;
    private JLabel enemiesLabel;
    private JLabel xpLabel;
    private XPBar xpBar;
    private JLabel statsLabel;
    private JLabel controlsLabel;
    
    // Colors
    private static final Color BACKGROUND_COLOR = new Color(15, 15, 25);
    private static final Color TEXT_COLOR = new Color(0, 200, 255);
    private static final Color ACCENT_COLOR = new Color(255, 200, 0);
    private static final Color LIVES_COLOR = new Color(0, 255, 100);
    private static final Color DISCS_COLOR = new Color(255, 100, 255);
    private static final Color XP_COLOR = new Color(255, 215, 0);
    
    public HUDPanel() {
        this.gameEngine = null;
        
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEXT_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Create panels
        JPanel topPanel = createTopPanel();
        JPanel centerPanel = createCenterPanel();
        JPanel bottomPanel = createBottomPanel();
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Update timer (4 times per second)
        updateTimer = new Timer(250, e -> updateDisplay());
        updateTimer.start();
    }
    
    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        updateDisplay();
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Title
        JLabel titleLabel = new JLabel("F O R T R O N", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        titleLabel.setForeground(ACCENT_COLOR);
        
        // Level info
        JPanel levelPanel = new JPanel(new GridLayout(2, 1));
        levelPanel.setBackground(BACKGROUND_COLOR);
        
        chapterLabel = createStyledLabel("Chapter 1", Font.BOLD, 16, TEXT_COLOR);
        chapterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        levelLabel = createStyledLabel("Level 1", Font.PLAIN, 14, TEXT_COLOR);
        levelLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        levelPanel.add(chapterLabel);
        levelPanel.add(levelLabel);
        
        panel.add(titleLabel);
        panel.add(levelPanel);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Player stats
        livesLabel = createStyledLabel("Lives: 3", Font.BOLD, 16, LIVES_COLOR);
        discsLabel = createStyledLabel("Discs: 1", Font.BOLD, 16, DISCS_COLOR);
        enemiesLabel = createStyledLabel("Enemies: 0", Font.BOLD, 14, TEXT_COLOR);
        
        // XP Label
        xpLabel = createStyledLabel("XP: 0 / 50", Font.PLAIN, 12, XP_COLOR);
        
        // XP Progress Bar (classic style)
        xpBar = new XPBar();
        xpBar.setMaximumSize(new Dimension(230, 20));
        xpBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        statsLabel = createStyledLabel("Defeats: 0", Font.PLAIN, 12, new Color(150, 150, 150));
        
        // Add components with spacing
        panel.add(livesLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(discsLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(enemiesLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(xpLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(xpBar);
        panel.add(Box.createVerticalStrut(8));
        panel.add(statsLabel);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEXT_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        JLabel controlsTitle = createStyledLabel("CONTROLS", Font.BOLD, 12, ACCENT_COLOR);
        controlsTitle.setHorizontalAlignment(SwingConstants.CENTER);
        
        controlsLabel = createStyledLabel(getControlsText(), Font.PLAIN, 10, new Color(200, 200, 200));
        controlsLabel.setVerticalAlignment(SwingConstants.TOP);
        
        panel.add(controlsTitle, BorderLayout.NORTH);
        panel.add(controlsLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JLabel createStyledLabel(String text, int style, int size, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Monospaced", style, size));
        label.setForeground(color);
        return label;
    }
    
    private String getControlsText() {
        return "<html>" +
               "Arrow Keys: Move<br>" +
               "WASD: Move<br>" +
               "SPACE: Throw Disc<br>" +
               "P: Pause/Resume<br>" +
               "R: Restart Level<br>" +
               "N: Next Level<br>" +
               "F1: New Game<br>" +
               "ESC: Quit" +
               "</html>";
    }
    
    private void updateDisplay() {
        if (gameEngine == null) {
            return;
        }
        
        GameState gameState = gameEngine.getGameState();
        
        // Update level info
        int currentLevel = gameEngine.getCurrentLevel();
        int currentChapter = ((currentLevel - 1) / 4) + 1;
        int levelInChapter = ((currentLevel - 1) % 4) + 1;
        
        chapterLabel.setText("Chapter " + currentChapter);
        levelLabel.setText("Level " + levelInChapter);
        
        if (gameState != null) {
            Player player = gameState.getPlayer();
            
            if (player != null) {
                // Update player stats - show actual current values
                livesLabel.setText("❤ Lives: " + player.getCurrentLivesInt());
                discsLabel.setText("⬢ Discs: " + player.getDiscsAvailable());
                
                // Update XP info as "current / threshold" with progress bar
                if (player.getLevelingSystem() != null) {
                    int currentXP = player.getLevelingSystem().getCurrentXP();
                    int xpForNext = player.getLevelingSystem().getXPToNextLevel();
                    xpLabel.setText("XP: " + currentXP + " / " + xpForNext);
                    
                    // Update XP bar
                    xpBar.setProgress(currentXP, xpForNext);
                }
            }
            
            // Update enemies
            int aliveEnemies = gameState.getAliveEnemyCount();
            int totalEnemies = gameState.getTotalEnemyCount();
            enemiesLabel.setText("Enemies: " + aliveEnemies + " / " + totalEnemies);
            
            // Update stats
            int defeats = gameEngine.getTotalEnemiesDefeated();
            int deaths = gameEngine.getTotalPlayerDeaths();
            statsLabel.setText("Defeats: " + defeats + " | Deaths: " + deaths);
        }
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(250, 0); // Fixed width, flexible height
    }
    
    /**
     * Classic-style XP progress bar component.
     */
    private class XPBar extends JPanel {
        private int currentXP;
        private int maxXP;
        
        public XPBar() {
            this.currentXP = 0;
            this.maxXP = 100;
            setBackground(BACKGROUND_COLOR);
            setPreferredSize(new Dimension(230, 20));
        }
        
        public void setProgress(int current, int max) {
            this.currentXP = Math.max(0, current);
            this.maxXP = Math.max(1, max);
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            
            // Background (empty bar)
            g2d.setColor(new Color(30, 30, 40));
            g2d.fillRect(0, 0, width, height);
            
            // Border
            g2d.setColor(XP_COLOR);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(1, 1, width - 2, height - 2);
            
            // Fill (progress)
            if (maxXP > 0) {
                float progress = (float) currentXP / maxXP;
                int fillWidth = (int) ((width - 4) * progress);
                
                // Gradient fill
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(255, 215, 0),
                    fillWidth, height, new Color(255, 165, 0)
                );
                g2d.setPaint(gradient);
                g2d.fillRect(2, 2, fillWidth, height - 4);
                
                // Add glow effect
                g2d.setColor(new Color(255, 215, 0, 80));
                g2d.fillRect(2, 2, fillWidth, (height - 4) / 2);
            }
            
            // Percentage text (centered)
            if (maxXP > 0) {
                float percentage = (float) currentXP / maxXP * 100;
                String percentText = String.format("%.0f%%", percentage);
                
                g2d.setFont(new Font("Monospaced", Font.BOLD, 11));
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(percentText);
                int textX = (width - textWidth) / 2;
                int textY = (height + fm.getAscent() - fm.getDescent()) / 2;
                
                // Shadow
                g2d.setColor(Color.BLACK);
                g2d.drawString(percentText, textX + 1, textY + 1);
                
                // Text
                g2d.setColor(Color.WHITE);
                g2d.drawString(percentText, textX, textY);
            }
        }
    }
}
