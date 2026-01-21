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

public class Level5Scene {
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
    private List<PowerUp> powerUps;
    private List<Explosion> explosions;
    private List<EnemyBullet> enemyBullets;
    private List<Meteor> meteors;
    private List<WingmanShip> wingmen;
    private List<BossBullet> bossBullets;
    private Boss currentBoss;
    private int score = 0;
    
    // Game states
    private boolean gameOver = false;
    private long startTime;
    private static final long GAME_DURATION = 60_000_000_000L; // 60 seconds game duration
    private boolean bossSpawned = false;
    private List<Star> backgroundStars = new ArrayList<>();
    private boolean victory = false;  // Victory state
    private boolean showEndScreen = false;  // End screen display state
    private long lastBulletTime = 0; // Last bullet firing time
    private long lastWingmanBulletTime = 0; // Last wing-man bullet firing time
    private static final long BULLET_COOLDOWN = 100_000_000L; // Bullet cool-down time (0.1 seconds in nanoseconds)
    private static final long WINGMAN_BULLET_COOLDOWN = 100_000_000L; // Wing-man bullet cool-down time (0.1 seconds in nanoseconds)
    
    // Tutorial related
    private boolean tutorialMode = true;
    private long tutorialStartTime;
    private static final long TUTORIAL_DURATION = 5_000_000_000L; // Tutorial lasts 5 seconds
    
    public Level5Scene(Stage primaryStage, String playerName) {
        this.primaryStage = primaryStage;
        this.playerName = playerName;
        
        resetGame();
        initializeStars();
        setupScene();
        tutorialStartTime = System.nanoTime();
        player.activatePowerUp(PowerUp.PowerUpType.INVINCIBLE);
        for (WingmanShip wingman : wingmen) {
            wingman.activatePowerUp(PowerUp.PowerUpType.INVINCIBLE);
        }
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
            KeyCode key = e.getCode();
            if (showEndScreen) {
                switch (key) {
                    case ENTER:
                        // Replay current level
                        Main main = new Main();
                        main.startLevel5(primaryStage, playerName);
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

            double newX = player.getX();
            double newY = player.getY();
            
            switch(key) {
                case LEFT: newX -= 5; break;
                case RIGHT: newX += 5; break; 
                case UP: newY -= 5; break;
                case DOWN: newY += 5; break;
                case SPACE:
                    long currentTime = System.nanoTime();
                    if (currentTime - lastBulletTime >= BULLET_COOLDOWN) {
                        if (player.getCurrentPowerUp() == PowerUp.PowerUpType.TRIPLE_SHOT) {
                            bullets.add(new Bullet(player.getX() + 20, player.getY()));
                            bullets.add(new Bullet(player.getX() + 10, player.getY()));
                            bullets.add(new Bullet(player.getX() + 30, player.getY()));
                        } else if (player.getCurrentPowerUp() == PowerUp.PowerUpType.SPREAD_SHOT) {
                            bullets.add(new Bullet(player.getX() + 20, player.getY(), -1));
                            bullets.add(new Bullet(player.getX() + 20, player.getY(), 0));
                            bullets.add(new Bullet(player.getX() + 20, player.getY(), 1));
                        } else {
                            bullets.add(new Bullet(player.getX() + 20, player.getY()));
                        }
                        lastBulletTime = currentTime;
                    }
                    
                    // Wing-men fire bullets with cool-down
                    if (currentTime - lastWingmanBulletTime >= WINGMAN_BULLET_COOLDOWN) {
                        for (WingmanShip wingman : wingmen) {
                            bullets.add(wingman.shoot());
                        }
                        lastWingmanBulletTime = currentTime;
                    }
                    break;
                case ESCAPE:
                    Main main = new Main();
                    main.startLevelSelect(primaryStage, playerName);
                    break;
                default:
                    break;
            }
            
            boolean canMove = true;
            double newPlayerX = newX;
            double newPlayerY = newY;

            if (newPlayerX < 0 || newPlayerX > 760 ||
                newPlayerY < 0 || newPlayerY > 560) {
                canMove = false;
            }

            if (canMove) {
                for (WingmanShip wingman : wingmen) {
                    double wingmanNewX = newPlayerX + wingman.getOffsetX();
                    double wingmanNewY = newPlayerY + wingman.getOffsetY();
                    
                    if (wingmanNewX < 0 || wingmanNewX > 760 ||
                        wingmanNewY < 0 || wingmanNewY > 560) {
                        canMove = false;
                        break;
                    }
                }
            }
            
            if (canMove) {
                switch(key) {
                    case LEFT: player.setDx(-5); break;
                    case RIGHT: player.setDx(5); break;
                    case UP: player.setDy(-5); break;
                    case DOWN: player.setDy(5); break;
                    default: break;
                }
            }
        });
        
        scene.setOnKeyReleased(e -> {
            KeyCode key = e.getCode();
            switch (key) {
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
            private long lastEnemy = 0;
            private long lastPowerUp = 0;
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
                            player.deactivateInvincible();
                            for (WingmanShip wingman : wingmen) {
                                wingman.deactivateInvincible();
                            }
                        }
                    } else {
                        // Check if it's time to spawn Boss
                        if (now - startTime > GAME_DURATION && !bossSpawned) {
                            currentBoss = new Boss(Boss.BossType.FORTRESS); // Specify Boss type
                            bossSpawned = true;
                        }
                    }
                    
                    // Spawn enemies and meteors (regardless of tutorial mode)
                    if (now - lastMeteor > 500_000_000L) { // Every 0.5 seconds
                        spawnMeteor();
                        lastMeteor = now;
                    }
                    
                    if (currentBoss == null && now - lastEnemy > 2_000_000_000L) {
                        enemies.add(new Enemy(Math.random() * (WIDTH-40), 0));
                        lastEnemy = now;
                    }
                    
                    if (now - lastPowerUp > 15_000_000_000L) {
                        powerUps.add(new PowerUp(Math.random() * (WIDTH-40), 0));
                        lastPowerUp = now;
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
        player.update(wingmen);
        player.checkPowerUpExpiration();
        
        // Update wingmen
        Iterator<WingmanShip> wingmanIt = wingmen.iterator();
        while (wingmanIt.hasNext()) {
            WingmanShip wingman = wingmanIt.next();
            wingman.update(player, wingmen);
            wingman.checkPowerUpExpiration();
            
            // Check health, remove if zero
            if (wingman.getHealth() <= 0) {
                explosions.add(new Explosion(wingman.getX(), wingman.getY()));
                wingmanIt.remove();
            }
        }
        
        // Update Boss related
        if (currentBoss != null) {
            currentBoss.update(bossBullets);
            updateBossBullets();
            
            // Check collision between player and Boss
            if (currentBoss.intersects(player)) {
                currentBoss.damage();  // Boss always takes damage
                explosions.add(new Explosion(currentBoss.getX(), currentBoss.getY()));
                if (!player.isInvincible()) {  // Player only takes damage if not invincible
                    explosions.add(new Explosion(player.getX(), player.getY()));
                    player.damage();
                }
            }
        
            
            // Check collision between wing-men and Boss
            for (WingmanShip wingman : wingmen) {
                if (currentBoss.intersects(wingman)) {
                    currentBoss.damage();  // Boss always takes damage
                    explosions.add(new Explosion(currentBoss.getX(), currentBoss.getY()));
                    if (!wingman.isInvincible()) {  // Player only takes damage if not invincible
                        explosions.add(new Explosion(wingman.getX(), wingman.getY()));
                        wingman.damage();
                    }
                }
            }
        }
        
        // Update all moving objects
        updateBullets();
        updateEnemies();
        updateEnemyBullets();
        updateMeteors();
        updateExplosions();
        updatePowerUps();
        
        // Check player health
        checkPlayerHealth();
    }
    
    private void updateMeteors() {
        Iterator<Meteor> meteorIt = meteors.iterator();
        while (meteorIt.hasNext()) {
            Meteor meteor = meteorIt.next();
            meteor.update();
            boolean shouldRemoveMeteor = false;

            // Check collision with player
            if (meteor.intersects(player)) {
                explosions.add(new Explosion(meteor.getX(), meteor.getY()));
                if (!player.isInvincible()) {
                    explosions.add(new Explosion(player.getX(), player.getY()));
                    player.damage();
                }
                shouldRemoveMeteor = true;
            }

            // If meteor hasn't been marked for removal, check collision with wing-men
            if (!shouldRemoveMeteor) {
                for (WingmanShip wingman : wingmen) {
                    if (meteor.intersects(wingman)) {
                        explosions.add(new Explosion(meteor.getX(), meteor.getY()));
                        if (!wingman.isInvincible()) {
                            explosions.add(new Explosion(wingman.getX(), wingman.getY()));
                            wingman.damage();
                        }
                        shouldRemoveMeteor = true;
                        break;
                    }
                }
            }

            // If meteor hasn't been marked for removal, check bullet collisions
            if (!shouldRemoveMeteor) {
                Iterator<Bullet> bulletIt = bullets.iterator();
                while (bulletIt.hasNext()) {
                    Bullet bullet = bulletIt.next();
                    if (meteor.intersects(bullet)) {
                        meteor.damage();
                        bulletIt.remove();
                        if (meteor.isDestroyed()) {
                            explosions.add(new Explosion(meteor.getX(), meteor.getY()));
                            shouldRemoveMeteor = true;
                            if (!tutorialMode) {
                                score += 50;
                            }
                            break;
                        }
                    }
                }
            }

            // If meteor is off-screen
            if (!shouldRemoveMeteor && meteor.isOffscreen(HEIGHT)) {
                shouldRemoveMeteor = true;
            }

            // If meteor has been marked for removal, remove it
            if (shouldRemoveMeteor) {
                meteorIt.remove();
            }
        }
    }
    
    private void updatePowerUps() {
        Iterator<PowerUp> powerUpIt = powerUps.iterator();
        while (powerUpIt.hasNext()) {
            PowerUp powerUp = powerUpIt.next();
            powerUp.update();

            // Check collision with player bullets
            Iterator<Bullet> bulletIt = bullets.iterator();
            while (bulletIt.hasNext()) {
                Bullet bullet = bulletIt.next();
                if (powerUp.intersects(bullet)) {
                    player.activatePowerUp(powerUp.getType());
                    // Activate power-ups for all wing-men
                    for (WingmanShip wingman : wingmen) {
                        wingman.activatePowerUp(powerUp.getType());
                    }
                    powerUpIt.remove();
                    bulletIt.remove();
                    break;
                }
            }

            if (powerUp.getY() > 600) {
                powerUpIt.remove();
            }
        }
    }
    
    private void updateBullets() {
        Iterator<Bullet> bulletIt = bullets.iterator();
        while (bulletIt.hasNext()) {
            Bullet bullet = bulletIt.next();
            bullet.update();
            
            if (bullet.getY() < 0) {
                bulletIt.remove();
                continue;
            }
            
            // Check collision with Boss
            if (currentBoss != null && currentBoss.intersects(bullet)) {
                currentBoss.damage();
                bulletIt.remove();
                explosions.add(new Explosion(bullet.getX(), bullet.getY()));
                if (!tutorialMode) {
                    score += 20;  // Add 20 points for hitting Boss
                }
                if (currentBoss.isDestroyed()) {
                    handleBossDefeat();
                }
                continue;
            }
        }
    }
    
    private void updateEnemies() {
        Iterator<Enemy> enemyIt = enemies.iterator();
        while (enemyIt.hasNext()) {
            Enemy enemy = enemyIt.next();
            enemy.update();
            
            // Add logic for enemy firing bullets
            if (enemy.shouldShoot()) {  // Only shoot bullets in non-tutorial mode
                enemyBullets.add(enemy.shoot());
            }
            
            // Check collision with player and wing-men
            if (enemy.intersects(player)) {
                explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                if (!player.isInvincible()) {
                    explosions.add(new Explosion(player.getX(), player.getY()));
                    player.damage();
                }
                enemyIt.remove();
                continue;
            }
            
            for (WingmanShip wingman : wingmen) {
                if (enemy.intersects(wingman)) {
                    explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                    if (!wingman.isInvincible()) {
                        explosions.add(new Explosion(wingman.getX(), wingman.getY()));
                        wingman.damage();
                    } else if (!tutorialMode) {
                        score += 100;
                    }
                    enemyIt.remove();
                    break;
                }
            }
            
            // Check collision with bullets
            Iterator<Bullet> bulletIt = bullets.iterator();
            while (bulletIt.hasNext()) {
                Bullet bullet = bulletIt.next();
                if (enemy.intersects(bullet)) {
                    explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                    enemyIt.remove();
                    bulletIt.remove();
                    if (!tutorialMode) {
                        score += 100;  // Only score in non-tutorial mode
                    }
                    break;
                }
            }
        }
    }
    
    private void updateEnemyBullets() {
        Iterator<EnemyBullet> bulletIt = enemyBullets.iterator();
        while (bulletIt.hasNext()) {
            EnemyBullet bullet = bulletIt.next();
            bullet.update();

            if (bullet.intersects(player)) {
                explosions.add(new Explosion(player.getX(), player.getY()));
                if (!player.isInvincible()) {
                    player.damage();
                }
                bulletIt.remove();
                continue;
            }

            for (WingmanShip wingman : wingmen) {
                if (bullet.intersects(wingman)) {
                    explosions.add(new Explosion(wingman.getX(), wingman.getY()));
                    if (!wingman.isInvincible()) {
                        wingman.damage();
                    }
                    bulletIt.remove();
                    break;
                }
            }

            if (bullet.isOffscreen(600)) {
                bulletIt.remove();
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
    
    private void checkPlayerHealth() {
        if (player.getHealth() <= 0) {
            gameOver = true;
            player.setDx(0);
            player.setDy(0);
            // Wing-men will automatically stop moving because their position is relative to the main ship
        }
    }
    
    private void renderGame() {
        // Clear background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Render background stars
        gc.setFill(Color.WHITE);
        for (Star star : backgroundStars) {
            gc.setGlobalAlpha(star.brightness);
            gc.fillOval(star.x, star.y, 2, 2);
        }
        gc.setGlobalAlpha(1.0);
        
        if (!showEndScreen) {
            // Render game objects in layers
            // 1. Background layer
            powerUps.forEach(powerUp -> powerUp.render(gc));
            meteors.forEach(meteor -> meteor.render(gc));
            
            // 2. Middle layer
            enemies.forEach(enemy -> enemy.render(gc));
            if (currentBoss != null) {
                currentBoss.render(gc);
            }
            
            // 3. Foreground layer
            wingmen.forEach(wingman -> wingman.render(gc));
            player.render(gc);
            bullets.forEach(bullet -> bullet.render(gc));
            enemyBullets.forEach(bullet -> bullet.render(gc));
            bossBullets.forEach(bullet -> bullet.render(gc));
            explosions.forEach(explosion -> explosion.render(gc));
            
            // Display tutorial or game information
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", 20));
            
            if (tutorialMode) {
                // Tutorial screen
                gc.setFill(Color.RED);
                gc.setFont(Font.font("Arial", 24));
                gc.fillText("Level 5: Final Nemesis", WIDTH/2 - 132, 50);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", 20));
                gc.fillText("Move: ← → ↑ ↓", WIDTH/2 - 75, 100);
                gc.fillText("Fire: Space", WIDTH/2 - 75, 130);
                gc.fillText("Return: ESC", WIDTH/2 - 75, 160);
                
                gc.setFont(Font.font("Arial", 18));
                gc.fillText("Win condition: Defeat the Final Boss", WIDTH/2 - 145, 200);
                gc.fillText("Tip: Collect power-ups for special weapons", WIDTH/2 - 182, 230);
                gc.fillText("Tutorial time remaining: " + (5 - (System.nanoTime() - tutorialStartTime) / 1_000_000_000L) + "s", WIDTH/2 - 115, 260);
            } else {
                // Show count-down or Boss health
                if (!bossSpawned) {
                    double timeLeft = Math.max(0, 60.0 - (System.nanoTime() - startTime) / 1_000_000_000.0);
                    gc.fillText(String.format("Boss appears in: %.1f", timeLeft), 10, 80);
                } else if (currentBoss != null) {
                    gc.fillText("Boss Health: " + currentBoss.getHealth(), 10, 30);
                }
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
            gc.fillText("Enter - Replay Level", WIDTH/2 - 100, HEIGHT/2 + 50);
            gc.fillText("ESC - Return to Menu", WIDTH/2 - 100, HEIGHT/2 + 80);
        }
    }
    
    private void resetGame() {
        player = new Player(WIDTH/2, HEIGHT-50);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        powerUps = new ArrayList<>();
        explosions = new ArrayList<>();
        enemyBullets = new ArrayList<>();
        meteors = new ArrayList<>();
        bossBullets = new ArrayList<>();
        
        // Initialize wing-men
        wingmen = new ArrayList<>();
        wingmen.add(new WingmanShip(-70, 0));     // Left side, back
        wingmen.add(new WingmanShip(-100, -20));  // Left side, front
        wingmen.add(new WingmanShip(70, 0));      // Right side, back
        wingmen.add(new WingmanShip(100, -20));   // Right side, front
        
        currentBoss = null;
        bossSpawned = false;
        score = 0;
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
    
    private void updateBossBullets() {
        Iterator<BossBullet> bulletIt = bossBullets.iterator();
        while (bulletIt.hasNext()) {
            BossBullet bullet = bulletIt.next();
            bullet.update();

            if (bullet.intersects(player)) {
                explosions.add(new Explosion(player.getX(), player.getY()));
                if (!player.isInvincible()) {
                    player.damage();
                }
                bulletIt.remove();
                continue;
            }

            for (WingmanShip wingman : wingmen) {
                if (bullet.intersects(wingman)) {
                    explosions.add(new Explosion(wingman.getX(), wingman.getY()));
                    if (!wingman.isInvincible()) {
                        wingman.damage();
                    }
                    bulletIt.remove();
                    break;
                }
            }

            if (bullet.isOffscreen()) {
                bulletIt.remove();
            }
        }
    }
    
    private void handleBossDefeat() {
        explosions.add(new Explosion(currentBoss.getX() + 100, currentBoss.getY() + 75));
        explosions.add(new Explosion(currentBoss.getX() + 50, currentBoss.getY() + 50));
        explosions.add(new Explosion(currentBoss.getX() + 150, currentBoss.getY() + 50));
        score += 500;
        victory = true;
        // Update highest score
        UserData.updateLevelScore(playerName, 5, score);
    }
    
    public Scene getScene() {
        return scene;
    }
} 
