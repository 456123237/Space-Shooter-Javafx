package application;

import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import javafx.scene.text.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class Level2Scene {
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
    private List<Enemy> enemies;
    private List<Explosion> explosions;
    private List<EnemyBullet> enemyBullets;
    private long startTime;
    
    // Game states
    private boolean gameOver = false;
    private List<Star> backgroundStars = new ArrayList<>();
    private long lastBulletTime = 0; // Last bullet firing time
    private static final long BULLET_COOLDOWN = 100_000_000L; // Bullet cool-down time (0.1 seconds in nanoseconds)
    
    // Tutorial related
    private boolean tutorialMode = true;
    private long tutorialStartTime;
    private static final long TUTORIAL_DURATION = 5_000_000_000L; // Tutorial lasts 5 seconds
    private int score = 0;  // Score tracking
    private boolean victory = false;  // Victory state
    private boolean showEndScreen = false;  // End screen display state
    private static final long GAME_DURATION = 60_000_000_000L; // 60 seconds game duration
    
    public Level2Scene(Stage primaryStage, String playerName) {
        this.primaryStage = primaryStage;
        this.playerName = playerName;
        
        resetGame();
        initializeStars();
        setupScene();
        tutorialStartTime = System.nanoTime();
        player.activatePowerUp(PowerUp.PowerUpType.INVINCIBLE);
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
                KeyCode key = e.getCode();
                switch (key) {
                    case SPACE:
                        if (victory) {
                            // Enter next level
                            Main main = new Main();
                            main.startLevel3(primaryStage, playerName);
                        }
                        break;
                    case ENTER:
                        // Replay current level
                        Main main = new Main();
                        main.startLevel2(primaryStage, playerName);
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

            if (e.getCode() == KeyCode.LEFT) {
                player.setDx(-5);
            }
            if (e.getCode() == KeyCode.RIGHT) {
                player.setDx(5);
            }
            if (e.getCode() == KeyCode.UP) {
                player.setDy(-5);
            }
            if (e.getCode() == KeyCode.DOWN) {
                player.setDy(5);
            }
            if (e.getCode() == KeyCode.SPACE) {
                long currentTime = System.nanoTime();
                if (currentTime - lastBulletTime >= BULLET_COOLDOWN) {
                    bullets.add(new Bullet(player.getX() + 20, player.getY()));
                    lastBulletTime = currentTime;
                }
            }
            if (e.getCode() == KeyCode.ESCAPE) {
                Main main = new Main();
                main.startLevelSelect(primaryStage, playerName);
            }
        });
        
        scene.setOnKeyReleased(e -> {
            if (showEndScreen) return;
            
            if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.RIGHT) {
                player.setDx(0);
            }
            if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
                player.setDy(0);
            }
        });
    }
    
    private void startGameLoop() {
        new AnimationTimer() {
            private long lastEnemy = 0;
            
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
                            UserData.updateLevelScore(playerName, 2, score);
                            UserData.updateUnlockedLevel(playerName, 3);
                            return;
                        }
                    }
                    
                    // Spawn enemy ships
                    if (now - lastEnemy > 1_000_000_000L) { // Spawn enemy every 1 second
                        enemies.add(new Enemy(Math.random() * (WIDTH-40), 0));
                        lastEnemy = now;
                    }
                    
                    updateGame();
                    renderGame();
                }
            }
        }.start();
    }
    
    private void updateGame() {
        updateStars();
        player.update();
        updateBullets();
        updateEnemies();
        updateEnemyBullets();
        updateExplosions();
        
        checkPlayerHealth();
    }
    
    private void updateBullets() {
        Iterator<Bullet> bulletIt = bullets.iterator();
        while (bulletIt.hasNext()) {
            Bullet bullet = bulletIt.next();
            bullet.update();
            
            if (bullet.getY() < 0) {
                bulletIt.remove();
            }
        }
    }
    
    private void updateEnemies() {
        Iterator<Enemy> enemyIt = enemies.iterator();
        while (enemyIt.hasNext()) {
            Enemy enemy = enemyIt.next();
            enemy.update();
            
            if (enemy.shouldShoot()) {
                enemyBullets.add(enemy.shoot());
            }
            
            Iterator<Bullet> bulletIt = bullets.iterator();
            while (bulletIt.hasNext()) {
                Bullet bullet = bulletIt.next();
                if (enemy.intersects(bullet)) {
                    explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                    enemyIt.remove();
                    bulletIt.remove();
                    if (!tutorialMode) {  // Only score in non-tutorial mode
                        score += 100;  // Add 100 points for destroying an enemy
                    }
                    break;
                }
            }
            
            // Reference Level3's collision logic
            if (enemy.intersects(player)) {
                explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                enemyIt.remove();
                if (!player.isInvincible()) {
                    explosions.add(new Explosion(player.getX(), player.getY()));
                    player.damage();
                    checkPlayerHealth();
                }
            }
            
            if (enemy.getY() > HEIGHT) {
                enemyIt.remove();
            }
        }
    }
    
    private void updateExplosions() {
        Iterator<Explosion> explosionIt = explosions.iterator();
        while (explosionIt.hasNext()) {
            Explosion explosion = explosionIt.next();
            explosion.update();
            if (explosion.isFinished()) {
                explosionIt.remove();
            }
        }
    }
    
    private void updateEnemyBullets() {
        Iterator<EnemyBullet> bulletIt = enemyBullets.iterator();
        while (bulletIt.hasNext()) {
            EnemyBullet bullet = bulletIt.next();
            bullet.update();
            
            if (bullet.isOffscreen(HEIGHT)) {
                bulletIt.remove();
            }
            
            // Reference Level3's collision logic
            if (bullet.intersects(player)) {
                bulletIt.remove();
                if (!player.isInvincible()) {
                    player.damage();
                    explosions.add(new Explosion(player.getX(), player.getY()));
                    checkPlayerHealth();
                }
            }
        }
    }
    
    private void renderGame() {
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        
        for (Star star : backgroundStars) {
            gc.setFill(Color.rgb(255, 255, 255, star.brightness));
            gc.fillOval(star.x, star.y, 2, 2);
        }
        
        if (!showEndScreen) {
            player.render(gc);
            bullets.forEach(bullet -> bullet.render(gc));
            enemies.forEach(enemy -> enemy.render(gc));
            enemyBullets.forEach(bullet -> bullet.render(gc));
            explosions.forEach(explosion -> explosion.render(gc));
            
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 20));
            
            if (tutorialMode) {
                // Tutorial screen
                gc.setFill(Color.RED);
                gc.setFont(Font.font("Arial", 24));
                gc.fillText("Level 2: Fire Storm", WIDTH/2 - 122, 50);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", 20));
                gc.fillText("Move: ← → ↑ ↓", WIDTH/2 - 78, 100);
                gc.fillText("Fire: Space", WIDTH/2 - 78, 130);
                gc.fillText("Return: ESC", WIDTH/2 - 78, 160);
                
                gc.setFont(Font.font("Arial", 18));
                gc.fillText("Win condition: Survive enemy attacks for 60 seconds", WIDTH/2 - 220, 200);
                gc.fillText("Tutorial time remaining: " + (5 - (System.nanoTime() - tutorialStartTime) / 1_000_000_000L) + "s", WIDTH/2 - 125, 240);
            } else {
                // Game count-down
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
    
    private void resetGame() {
        player = new Player(WIDTH/2, HEIGHT-50);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        explosions = new ArrayList<>();
        enemyBullets = new ArrayList<>();
        score = 0;  // Reset score
        startTime = System.nanoTime();
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
    
    private void checkPlayerHealth() {
        if (player.getHealth() <= 0) {
            gameOver = true;
            player.setDx(0);
            player.setDy(0);
        }
    }
} 