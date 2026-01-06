package game.ui;

import game.GameEngine;
import game.GameEngine.GamePhase;
import game.GameState;
import util.Direction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Main game window that brings together GamePanel and HUDPanel.
 * Handles keyboard input and game loop.
 */
public class GameWindow extends JFrame {
    
    private GameEngine gameEngine;
    private GamePanel gamePanel;
    private HUDPanel hudPanel;
    private TitleScreen titleScreen;
    private Timer gameLoopTimer;
    
    private JPanel mainContainer;
    private CardLayout cardLayout;
    
    private static final int GAME_UPDATE_RATE = 16; // ~60 FPS (like classic TRON)
    private static final String TITLE_CARD = "TITLE";
    private static final String GAME_CARD = "GAME";
    
    public GameWindow() {
        setTitle("FORTRON - Grid Combat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Initialize game engine
        gameEngine = new GameEngine();
        
        // Create UI components
        gamePanel = new GamePanel();
        hudPanel = new HUDPanel();
        titleScreen = new TitleScreen();
        
        // Link game engine to UI
        hudPanel.setGameEngine(gameEngine);
        
        // Setup title screen listener
        setupTitleScreenListener();
        
        // Setup card layout for switching between title and game
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        
        // Add title screen
        mainContainer.add(titleScreen, TITLE_CARD);
        
        // Add game screen (panel + HUD)
        JPanel gameContainer = new JPanel(new BorderLayout(10, 10));
        gameContainer.add(gamePanel, BorderLayout.CENTER);
        gameContainer.add(hudPanel, BorderLayout.EAST);
        mainContainer.add(gameContainer, GAME_CARD);
        
        // Add main container to frame
        add(mainContainer);
        
        // Add keyboard controls
        setupKeyboardControls();
        
        // Setup game loop
        setupGameLoop();
        
        // Pack and center window
        pack();
        setLocationRelativeTo(null);
        
        // Show title screen
        showTitleScreen();
    }
    
    private void setupTitleScreenListener() {
        titleScreen.setListener(new TitleScreen.TitleScreenListener() {
            @Override
            public void onStartGame() {
                startNewGameFromMenu();
            }
            
            @Override
            public void onLoadGame() {
                JOptionPane.showMessageDialog(GameWindow.this, 
                    "Load Game feature coming soon!", 
                    "Load Game", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            @Override
            public void onLeaderboard() {
                showLeaderboard();
            }
            
            @Override
            public void onExit() {
                exitGame();
            }
        });
    }
    
    private void setupKeyboardControls() {
        gamePanel.addKeyboardControls(new GameKeyListener());
    }
    
    private void setupGameLoop() {
        gameLoopTimer = new Timer(GAME_UPDATE_RATE, e -> updateGame());
        gameLoopTimer.start();
    }
    
    private void updateGame() {
        if (gameEngine.getCurrentPhase() == GamePhase.PLAYING) {
            GameState gameState = gameEngine.getGameState();
            
            if (gameState != null && gameState.isGameRunning() && !gameState.isGamePaused()) {
                gameEngine.update();
                
                // Force repaint after game logic update
                gamePanel.repaint();
                
                // Check for level completion
                if (gameState.isGameOver()) {
                    handleLevelEnd();
                }
            }
        }
    }
    
    private void handleLevelEnd() {
        GameState gameState = gameEngine.getGameState();
        
        if (gameState.isPlayerWon()) {
            // Player won the level
            if (gameEngine.getCurrentLevel() < 16) {
                // More levels to go
                int nextLevel = gameEngine.getCurrentLevel() + 1;
                showMessage("Level Complete!", 
                    gameState.getGameOverReason() + "\n\nPress N for Level " + nextLevel);
            } else {
                // Game completed!
                showMessage("Victory!", "All levels completed!\n" + gameEngine.getVictoryScreen());
            }
        } else {
            // Player lost
            showMessage("Game Over", gameState.getGameOverReason() + "\n\nPress R to Restart Level");
        }
    }
    
    private void showTitleScreen() {
        cardLayout.show(mainContainer, TITLE_CARD);
        titleScreen.requestFocusInWindow();
    }
    
    private void showGameScreen() {
        cardLayout.show(mainContainer, GAME_CARD);
        // Ensure focus is on game panel for keyboard input
        SwingUtilities.invokeLater(() -> {
            gamePanel.requestFocusInWindow();
        });
    }
    
    private void startNewGameFromMenu() {
        gameEngine.startNewGame();
        gamePanel.setGameState(gameEngine.getGameState());
        showGameScreen();
        
        // Ensure game panel has focus for keyboard input
        SwingUtilities.invokeLater(() -> {
            gamePanel.requestFocusInWindow();
        });
    }
    
    private void showLeaderboard() {
        String leaderboardText = gameEngine.getStatistics();
        JOptionPane.showMessageDialog(this, 
            leaderboardText, 
            "Game Statistics", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exitGame() {
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit?",
            "Exit FORTRON",
            JOptionPane.YES_NO_OPTION
        );
        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
    
    private void showMessage(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
        });
    }
    
    /**
     * Keyboard input handler
     */
    private class GameKeyListener implements KeyListener {
        
        @Override
        public void keyPressed(KeyEvent e) {
            GameState gameState = gameEngine.getGameState();
            
            // Global commands
            switch (e.getKeyCode()) {
                case KeyEvent.VK_F1:
                    // Return to title screen
                    showTitleScreen();
                    return;
                    
                case KeyEvent.VK_ESCAPE:
                    // Return to title screen instead of quitting
                    int result = JOptionPane.showConfirmDialog(
                        GameWindow.this,
                        "Return to title screen?",
                        "Exit to Menu",
                        JOptionPane.YES_NO_OPTION
                    );
                    if (result == JOptionPane.YES_OPTION) {
                        showTitleScreen();
                    }
                    return;
            }
            
            // Game-specific commands (require active game)
            if (gameState == null) {
                return;
            }
            
            switch (e.getKeyCode()) {
                case KeyEvent.VK_P:
                    // Pause/Resume
                    gameState.togglePause();
                    break;
                    
                case KeyEvent.VK_R:
                    // Restart level
                    restartLevel();
                    break;
                    
                case KeyEvent.VK_N:
                    // Next level (if current level is complete)
                    if (gameState.isGameOver() && gameState.isPlayerWon()) {
                        nextLevel();
                    }
                    break;
                    
                case KeyEvent.VK_SPACE:
                    // Throw disc
                    if (gameState.isGameRunning() && !gameState.isGamePaused()) {
                        gameState.playerThrowDisc(2); // Default range of 2
                    }
                    break;
                    
                // Movement controls
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    if (gameState.isGameRunning() && !gameState.isGamePaused()) {
                        gameState.setPlayerDirection(Direction.UP);
                    }
                    break;
                    
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    if (gameState.isGameRunning() && !gameState.isGamePaused()) {
                        gameState.setPlayerDirection(Direction.DOWN);
                    }
                    break;
                    
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    if (gameState.isGameRunning() && !gameState.isGamePaused()) {
                        gameState.setPlayerDirection(Direction.LEFT);
                    }
                    break;
                    
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    if (gameState.isGameRunning() && !gameState.isGamePaused()) {
                        gameState.setPlayerDirection(Direction.RIGHT);
                    }
                    break;
            }
            
            // Repaint after any key action
            gamePanel.repaint();
        }
        
        @Override
        public void keyReleased(KeyEvent e) {
            // Not used
        }
        
        @Override
        public void keyTyped(KeyEvent e) {
            // Not used
        }
    }
    
    private void restartLevel() {
        gameEngine.restartLevel();
        gamePanel.setGameState(gameEngine.getGameState());
        // Ensure focus is on game panel
        SwingUtilities.invokeLater(() -> {
            gamePanel.requestFocusInWindow();
        });
    }
    
    private void nextLevel() {
        if (gameEngine.getCurrentLevel() < 16) {
            gameEngine.nextLevel();
            gamePanel.setGameState(gameEngine.getGameState());
            // Ensure focus is on game panel
            SwingUtilities.invokeLater(() -> {
                gamePanel.requestFocusInWindow();
            });
            
            showMessage("Next Level", 
                "Level " + gameEngine.getCurrentLevel() + " - " + 
                gameEngine.getCurrentLevelConfig().getDisplayName());
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameWindow window = new GameWindow();
            window.setVisible(true);
        });
    }
}
