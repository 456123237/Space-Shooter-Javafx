package application;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class Level1Scene {
    private Scene scene;
    private Stage primaryStage;
    private String playerName;
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private Canvas canvas;
    private GraphicsContext gc;
    
    // Game objects
    private Player player;
    private List<Bullet> bullets;
    private List<Meteor> meteors;
    private List<Explosion> explosions;
    private List<Star> backgroundStars;
    
    // Game states
    private boolean gameOver = false;
    private boolean victory = false;  // Add victory state
    private boolean showEndScreen = false;  // Add end screen display state
    private long startTime;
    private static final long GAME_DURATION = 60_000_000_000L; // 60 seconds
    private boolean tutorialMode = true;  // Tutorial mode flag
    private long tutorialStartTime;  // Tutorial start time
    private static final long TUTORIAL_DURATION = 5_000_000_000L; // Tutorial lasts 5 seconds
    private int score = 0;  // Add score variable
    private long lastBulletTime = 0; // Last bullet firing time
    private static final long BULLET_COOLDOWN = 100_000_000L; // Bullet cool-down time (0.5 seconds in nanoseconds)
    
    public Level1Scene(Stage primaryStage, String playerName) {
        this.primaryStage = primaryStage;
        this.playerName = playerName;
        
        resetGame();
        initializeStars();
        setupScene();
        tutorialStartTime = System.nanoTime();  // Initialize tutorial start time
        player.activatePowerUp(PowerUp.PowerUpType.INVINCIBLE);  // Activate invincible state at the start of the tutorial
    }
    
    private void setupScene() {
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        Pane root = new Pane(canvas);
        scene = new Scene(root, WIDTH, HEIGHT);
        
        setupInputHandlers();
        startGameLoop();
    }
    
    private void setupInputHandlers() {
        scene.setOnKeyPressed(e -> {
            if (showEndScreen) {
            	KeyCode keycode = e.getCode();
                switch (keycode) {
                    case SPACE:
                        if (victory) {
                            // Enter next level
                            Main main = new Main();
                            main.startLevel2(primaryStage, playerName);
                        }
                        break;
                    case ENTER:
                        // Replay current level
                        Main main = new Main();
                        main.startLevel1(primaryStage, playerName);
                        break;
                    case ESCAPE:
                        // Return to main menu
                        main = new Main();
                        main.startLevelSelect(primaryStage, playerName);
                        break;
                    default:
                    	break;
                }
                return;
            }

            switch (e.getCode()) {
                case LEFT: player.setDx(-5); break;
                case RIGHT: player.setDx(5); break;
                case UP: player.setDy(-5); break;
                case DOWN: player.setDy(5); break;
                case SPACE:
                    long currentTime = System.nanoTime();
                    if (currentTime - lastBulletTime >= BULLET_COOLDOWN) {
                        bullets.add(new Bullet(player.getX() + 20, player.getY()));
                        lastBulletTime = currentTime;
                    }
                    break;
                case ESCAPE:
                    Main main = new Main();
                    main.startLevelSelect(primaryStage, playerName);
                    break;
                default:
                	break;
            }
        });
        
        scene.setOnKeyReleased(e -> {
            if (showEndScreen) return;
            
            switch (e.getCode()) {
                case LEFT:
                case RIGHT:
                    player.setDx(0);
                    break;
                case UP:
                case DOWN:
                    player.setDy(0);
                    break;
			    default:
				    break;
            }
        });
    }
    
    private void startGameLoop() {
        new AnimationTimer() {
            private long lastMeteor = 0;
            
            @Override
            public void handle(long now) {
                if (gameOver || victory) {
                    showEndScreen = true;
                    updateStars();  // Continue updating background stars
                    renderGame();
                    return;
                }

                if (!gameOver) {
                    // Check tutorial state
                    if (tutorialMode) {
                        if (now - tutorialStartTime > TUTORIAL_DURATION) {
                            tutorialMode = false;
                            startTime = System.nanoTime();  // Start game timer
                            player.deactivateInvincible();  // End invincible state
                        }
                    } else {
                        // Game time check
                        if (now - startTime > GAME_DURATION) {
                            victory = true;
                            // Update highest score and unlock next level
                            UserData.updateLevelScore(playerName, 1, score);
                            UserData.updateUnlockedLevel(playerName, 2);
                            return;
                        }
                    }
                    
                    // Spawn meteor
                    if (now - lastMeteor > 500_000_000L) { // Every 0.5 seconds
                        spawnMeteor();
                        lastMeteor = now;
                    }
                    
                    updateGame();
                    renderGame();
                }
            }
        }.start();
    }
    
    private void spawnMeteor() {
        double x = Math.random() * (WIDTH - 100) + 50;
        Meteor.MeteorSize[] sizes = Meteor.MeteorSize.values();
        Meteor.MeteorSize randomSize = sizes[(int)(Math.random() * sizes.length)];
        meteors.add(new Meteor(x, -50, randomSize));
    }
    
    private void updateGame() {
        updateStars();
        player.update();
        
        // Update bullets
        Iterator<Bullet> bulletIt = bullets.iterator();
        while (bulletIt.hasNext()) {
            Bullet bullet = bulletIt.next();
            bullet.update();
            if (bullet.getY() < 0) {
                bulletIt.remove();
            }
        }
        
        // Update meteors
        updateMeteors();
        
        // Update explosion effects
        Iterator<Explosion> explosionIt = explosions.iterator();
        while (explosionIt.hasNext()) {
            Explosion explosion = explosionIt.next();
            explosion.update();
            if (explosion.isFinished()) {
                explosionIt.remove();
            }
        }
    }
    
    private void renderGame() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Draw background stars
        for (Star star : backgroundStars) {
            gc.setFill(Color.rgb(255, 255, 255, star.brightness));
            gc.fillOval(star.x, star.y, 2, 2);
        }
        
        if (!showEndScreen) {
            // Draw game objects
            player.render(gc);
            bullets.forEach(bullet -> bullet.render(gc));
            meteors.forEach(meteor -> meteor.render(gc));
            explosions.forEach(explosion -> explosion.render(gc));
            
            // Draw UI
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 20));
            
            if (tutorialMode) {
                // Tutorial screen
                gc.setFill(Color.RED);
                gc.setFont(Font.font("Arial", 24));
                gc.fillText("Level 1: The Beginning", WIDTH/2 - 143, 50);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", 20));
                gc.fillText("Move: ← → ↑ ↓", WIDTH/2 - 80, 100);
                gc.fillText("Fire: Space", WIDTH/2 - 80, 130);
                gc.fillText("Return: ESC", WIDTH/2 - 80, 160);
                
                gc.setFont(Font.font("Arial", 18));
                gc.fillText("Win condition: Survive meteor shower for 60 seconds", WIDTH/2 - 220, 200);
                gc.fillText("Tutorial time remaining: " + (5 - (System.nanoTime() - tutorialStartTime) / 1_000_000_000L) + "s", WIDTH/2 - 125, 240);
            } else {
                // Game count_down
                long timeLeft = (GAME_DURATION - (System.nanoTime() - startTime)) / 1_000_000_000L;
                gc.fillText("Time left: " + timeLeft + "s", 10, 80);
            }
            
            // Score display in top right
            gc.fillText("Score: " + score, WIDTH - 150, 32);
        } else {
            // Draw end screen
            gc.setFill(Color.GOLD);
            gc.setFont(Font.font("Arial", 36));
            
            if (victory) {
                gc.fillText("Victory", WIDTH/2 - 60, HEIGHT/2 - 100);
            } else {
                gc.setFill(Color.BLUE);
                gc.fillText("Failed", WIDTH/2 - 58, HEIGHT/2 - 100);
            }
            gc.setFill(Color.RED);
            gc.setFont(Font.font("Arial", 24));
            gc.fillText("Score: " + score, WIDTH/2 - 58, HEIGHT/2 - 40);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 20));
            if (victory) {
                gc.fillText("Space - Next Level", WIDTH/2 - 100, HEIGHT/2 + 20);
            }
            gc.fillText("Enter - Replay Level", WIDTH/2 - 100, HEIGHT/2 + 50);
            gc.fillText("ESC - Return to Menu", WIDTH/2 - 100, HEIGHT/2 + 80);
        }
    }
    
    private void checkPlayerHealth() {
        if (player.getHealth() <= 0) {
            gameOver = true;
            player.setDx(0);
            player.setDy(0);
        }
    }
    
    private void resetGame() {
        player = new Player(WIDTH/2, HEIGHT-50);
        bullets = new ArrayList<>();
        meteors = new ArrayList<>();
        explosions = new ArrayList<>();
        backgroundStars = new ArrayList<>();
        score = 0;  // Reset score
        gameOver = false;
    }
    
    private void initializeStars() {
        for (int i = 0; i < 100; i++) {
            backgroundStars.add(new Star());
        }
    }
    
    private void updateStars() {
        for (Star star : backgroundStars) {
            star.update();
        }
    }
    
    private void updateMeteors() {
        Iterator<Meteor> meteorIt = meteors.iterator();
        while (meteorIt.hasNext()) {
            Meteor meteor = meteorIt.next();
            meteor.update();
            
            // Check bullet collision
            Iterator<Bullet> bulletIt = bullets.iterator();
            while (bulletIt.hasNext()) {
                Bullet bullet = bulletIt.next();
                if (meteor.intersects(bullet)) {
                    meteor.damage();
                    bulletIt.remove();  // Remove bullet that hit the target
                    if (meteor.isDestroyed()) {
                        explosions.add(new Explosion(meteor.getX(), meteor.getY()));
                        meteorIt.remove();
                        if (!tutorialMode) {  // Only score in non-tutorial mode
                            score += 50;  // Add 50 points for destroying a meteor
                        }
                        break;
                    }
                }
            }
            
            // Check player collision - Reference Level3 implementation
            if (meteor.intersects(player)) {
                explosions.add(new Explosion(meteor.getX(), meteor.getY()));
                meteorIt.remove();
                if (!player.isInvincible()) {
                    explosions.add(new Explosion(player.getX(), player.getY()));
                    player.damage();
                    checkPlayerHealth();
                }
                continue;
            }
            
            // Check if off_screen
            if (meteor.isOffscreen(HEIGHT)) {
                meteorIt.remove();
            }
        }
    }
    
    private class Star {
        double x, y;
        double speed;
        double brightness;
        
        Star() {
            reset();
            y = Math.random() * HEIGHT;
        }
        
        void reset() {
            x = Math.random() * WIDTH;
            y = 0;
            speed = 1 + Math.random() * 3;
            brightness = 0.2 + Math.random() * 0.8;
        }
        
        void update() {
            y += speed;
            if (y > HEIGHT) {
                reset();
            }
        }
    }
    
    public Scene getScene() {
        return scene;
    }
}