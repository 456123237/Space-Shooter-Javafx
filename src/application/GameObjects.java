package application;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;

// Boss class
class Boss {
    private double x, y;
    private BossType type;
    private int health;
    private static final double SIZE_WIDTH = 200;
    private static final double SIZE_HEIGHT = 150;
    private double dx = 1;
    private long lastShootTime = 0;
    private static final long SHOOT_DELAY = 1_000_000_000L;
    private boolean movingRight = true;
    private int attackPattern = 0;
    private long lastPatternChange = 0;
    private static final long PATTERN_CHANGE_DELAY = 5_000_000_000L;
    
    public enum BossType { 
        MOTHERSHIP(10),  // The first boss, health value 10
        FORTRESS(50);    // The second boss, health value 15
        
        private final int maxHealth;
        
        BossType(int maxHealth) {
            this.maxHealth = maxHealth;
        }
        
        public int getMaxHealth() {
            return maxHealth;
        }
    }
    
    public Boss(BossType type) {
        this.type = type;
        this.health = type.getMaxHealth();  // Set initial health based on boss type
        this.x = 400 - SIZE_WIDTH/2;
        this.y = 50;
    }
    
    public void update(List<BossBullet> bossBullets) {
        if (movingRight) {
            x += dx;
            if (x > 600 - SIZE_WIDTH) movingRight = false;
        } else {
            x -= dx;
            if (x < 0) movingRight = true;
        }
        
        long now = System.nanoTime();
        if (now - lastPatternChange > PATTERN_CHANGE_DELAY) {
            attackPattern = (attackPattern + 1) % 3;
            lastPatternChange = now;
        }
        
        if (now - lastShootTime > SHOOT_DELAY) {
            switch (attackPattern) {
                case 0: // Normal attack
                    bossBullets.add(new BossBullet(x + SIZE_WIDTH/2, y + SIZE_HEIGHT, 0, 5));
                    break;
                case 1: // Spread
                    for (int i = -5; i <= 5; i++) {
                        bossBullets.add(new BossBullet(x + SIZE_WIDTH/2, y + SIZE_HEIGHT, i * 0.3, 5));
                    }
                    break;
                case 2: // Sweep
                    double angle = Math.sin(now * 0.000000001) * 2;
                    bossBullets.add(new BossBullet(x + SIZE_WIDTH/2, y + SIZE_HEIGHT, angle, 5));
                    break;
            }
            lastShootTime = now;
        }
    }
    
    public void render(GraphicsContext gc) {
        switch(type) {
            case MOTHERSHIP: renderMothership(gc); break;
            case FORTRESS: renderFortress(gc); break;
        }
        
        // Health bar
        gc.setFill(Color.RED);
        gc.fillRect(x, y - 20, SIZE_WIDTH * health / type.getMaxHealth(), 10);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(x, y - 20, SIZE_WIDTH, 10);
    }
    
    private void renderMothership(GraphicsContext gc) {
        gc.setFill(Color.SILVER);
        gc.fillOval(x, y, SIZE_WIDTH, SIZE_HEIGHT * 0.6);
        gc.setFill(Color.DARKGRAY);
        gc.fillOval(x + SIZE_WIDTH * 0.2, y - SIZE_HEIGHT * 0.1, SIZE_WIDTH * 0.6, SIZE_HEIGHT * 0.3);
        gc.setFill(Color.CYAN);
        gc.fillOval(x + SIZE_WIDTH * 0.4, y + SIZE_HEIGHT * 0.2, SIZE_WIDTH * 0.2, SIZE_HEIGHT * 0.2);
    }
    
    private void renderFortress(GraphicsContext gc) {
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(x, y, SIZE_WIDTH, SIZE_HEIGHT);
        gc.setFill(Color.RED);
        gc.fillRect(x + SIZE_WIDTH * 0.1, y + SIZE_HEIGHT * 0.3, SIZE_WIDTH * 0.8, SIZE_HEIGHT * 0.4);
        gc.setFill(Color.GRAY);
        for (int i = 0; i < 3; i++) {
            gc.fillRect(x + (SIZE_WIDTH * 0.25 * i) + SIZE_WIDTH * 0.15, 
                       y + SIZE_HEIGHT * 0.7, 
                       SIZE_WIDTH * 0.15, 
                       SIZE_HEIGHT * 0.3);
        }
    }
    
    public void damage() { health--; }
    public boolean isDestroyed() { return health <= 0; }
    public boolean intersects(Bullet bullet) {
        return bullet.getX() >= x && bullet.getX() <= x + SIZE_WIDTH &&
               bullet.getY() >= y && bullet.getY() <= y + SIZE_HEIGHT;
    }
    public double getX() { return x; }
    public double getY() { return y; }
    public BossType getType() { return type; }
    public boolean intersects(Player player) {
        return player.getX() < x + 200 && player.getX() + 40 > x &&
               player.getY() < y + 150 && player.getY() + 40 > y;
    }
    public boolean intersects(WingmanShip wingman) {
        return wingman.getX() < x + 200 && wingman.getX() + 40 > x &&
               wingman.getY() < y + 150 && wingman.getY() + 40 > y;
    }
    public int getHealth() { return health; }
}

// BossBullet class
class BossBullet {
    private double x, y, dx, dy;
    private static final double SIZE = 10;
    
    public BossBullet(double x, double y, double angle, double speed) {
        this.x = x;
        this.y = y;
        this.dx = Math.sin(angle) * speed;
        this.dy = Math.cos(angle) * speed;
    }
    
    public void update() {
        x += dx;
        y += dy;
    }
    
    public void render(GraphicsContext gc) {
        gc.setFill(Color.RED);
        gc.fillOval(x - SIZE/2, y - SIZE/2, SIZE, SIZE);
        gc.setGlobalAlpha(0.3);
        gc.setFill(Color.ORANGE);
        gc.fillOval(x - SIZE, y - SIZE, SIZE * 2, SIZE * 2);
        gc.setGlobalAlpha(1.0);
    }
    
    public boolean intersects(Player player) {
        return player.getX() + 40 >= x - SIZE/2 && player.getX() <= x + SIZE/2 &&
               player.getY() + 40 >= y - SIZE/2 && player.getY() <= y + SIZE/2;
    }
    
    public boolean isOffscreen() {
        return y > 600 || y < 0 || x < 0 || x > 800;
    }
    
    public boolean intersects(WingmanShip wingman) {
        return wingman.getX() + 40 >= x - SIZE/2 && wingman.getX() <= x + SIZE/2 &&
               wingman.getY() + 40 >= y - SIZE/2 && wingman.getY() <= y + SIZE/2;
    }
}

// Player class
class Player {
    private double x, y, dx, dy;
    private int health = 5;
    private boolean invincible = false;
    private long invincibleStartTime = 0;
    private PowerUp.PowerUpType currentPowerUp = null;
    private long powerUpStartTime = 0;
    private static final long POWER_UP_DURATION = 10_000_000_000L;
    private static final long INVINCIBLE_DURATION = 5_000_000_000L;
    
    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.dx = 0;
        this.dy = 0;
    }
    
    public void update() {
        double newX = x + dx;
        double newY = y + dy;
        
        // Limit to screen boundaries
        x = Math.max(0, Math.min(newX, 760));  // 800 - 40(airplane width)
        y = Math.max(0, Math.min(newY, 550));  // 600 - 40(airplane height)
    }
    
    public void update(List<WingmanShip> wingmen) {
        double newX = x + dx;
        double newY = y + dy;
        boolean canMove = true;
        
        // Check if moving would cause any friendly ships to go out of bounds
        for (WingmanShip wingman : wingmen) {
            double wingmanNewX = newX + wingman.getOffsetX();
            double wingmanNewY = newY + wingman.getOffsetY();
            
            if (wingmanNewX < 0 || wingmanNewX > 760 ||
                wingmanNewY < 0 || wingmanNewY > 550) {
                canMove = false;
                break;
            }
        }
        
        // If moving is safe, update position
        if (canMove) {
            x = Math.max(0, Math.min(newX, 760));
            y = Math.max(0, Math.min(newY, 550));
        } else {
            // If cannot move, stop current movement
            if (dx != 0) dx = 0;
            if (dy != 0) dy = 0;
        }
    }
    
    public void render(GraphicsContext gc) {
        gc.save();
        
        // Only render when not invincible or during flash effect
        if (!invincible || System.currentTimeMillis() % 200 < 100) {
            // Main body (white)
            gc.setFill(Color.WHITE);
            gc.fillRect(x + 15, y, 10, 40);  // Central body
            
            // Orange top
            gc.setFill(Color.ORANGE);
            gc.fillPolygon(
                new double[]{x + 15, x + 25, x + 20},
                new double[]{y, y, y - 10},
                3
            );
            
            // Red wings
            gc.setFill(Color.RED);
            // Left wing
            gc.fillPolygon(
                new double[]{x, x + 15, x + 15, x},
                new double[]{y + 25, y + 15, y + 35, y + 40},
                4
            );
            // Right wing
            gc.fillPolygon(
                new double[]{x + 40, x + 25, x + 25, x + 40},
                new double[]{y + 25, y + 15, y + 35, y + 40},
                4
            );
            
            // Engine effect
            gc.setFill(Color.YELLOW);
            gc.fillOval(x + 15, y + 40, 10, 15);
            gc.setGlobalAlpha(0.6);
            double flameHeight = 10 + Math.random() * 5;
            gc.setFill(Color.ORANGE);
            gc.fillOval(x + 15, y + 45, 10, flameHeight);
            gc.setGlobalAlpha(1.0);
        }
        
        // Health display
        gc.setFill(Color.RED);
        for (int i = 0; i < health; i++) {
            gc.fillOval(10 + i * 25, 20, 20, 20);
        }
        
        // Invincible effect
        if (invincible) {
            gc.setGlobalAlpha(0.3);
            gc.setFill(Color.CYAN);
            gc.fillOval(x - 10, y - 10, 60, 60);
            gc.setGlobalAlpha(1.0);
        }
        
        gc.restore();
    }
    
    public void activatePowerUp(PowerUp.PowerUpType type) {
        currentPowerUp = type;
        powerUpStartTime = System.nanoTime();
        if (type == PowerUp.PowerUpType.INVINCIBLE) {
            invincible = true;
            invincibleStartTime = System.nanoTime();
        }
    }
    
    public void checkPowerUpExpiration() {
        long now = System.nanoTime();
        if (currentPowerUp != null && now - powerUpStartTime > POWER_UP_DURATION) {
            currentPowerUp = null;
        }
        if (invincible && now - invincibleStartTime > INVINCIBLE_DURATION) {
            invincible = false;
        }
    }
    
    public void damage() {
        if (!invincible) health--;
    }
    
    public void setDx(double dx) { this.dx = dx; }
    public void setDy(double dy) { this.dy = dy; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getHealth() { return health; }
    public PowerUp.PowerUpType getCurrentPowerUp() { return currentPowerUp; }
    public boolean isInvincible() { return invincible; }
    
    public void deactivateInvincible() {
        invincible = false;
        invincibleStartTime = 0;
    }
}

// Enemy class
class Enemy {
    private double x, y;
    private static final double SPEED = 1;
    private EnemyType type;
    private long lastShot = 0;
    private static final long SHOOT_INTERVAL = 1_000_000_000L;  // Shoot every 2 seconds
    
    public enum EnemyType {
        FIGHTER,
        BOMBER,
        SCOUT
    }
    
    public Enemy(double x, double y) {
        this.x = x;
        this.y = y;
        EnemyType[] types = EnemyType.values();
        this.type = types[(int)(Math.random() * types.length)];
        this.lastShot = System.nanoTime();
    }
    
    public void update() {
        y += SPEED;
    }
    
    public boolean shouldShoot() {
        long now = System.nanoTime();
        if (now - lastShot > SHOOT_INTERVAL) {
            lastShot = now;
            return true;
        }
        return false;
    }
    
    public EnemyBullet shoot() {
        return new EnemyBullet(x + 40/2, y + 40);
    }
    
    public void render(GraphicsContext gc) {
        switch(type) {
            case FIGHTER:
                renderFighter(gc);
                break;
            case BOMBER:
                renderBomber(gc);
                break;
            case SCOUT:
                renderScout(gc);
                break;
        }
    }
    
    private void renderFighter(GraphicsContext gc) {
        gc.setFill(Color.RED);
        gc.fillPolygon(
            new double[]{x + 20, x + 40, x + 20, x},
            new double[]{y, y + 20, y + 40, y + 20},
            4
        );
        gc.setFill(Color.DARKBLUE);
        gc.fillPolygon(
            new double[]{x, x + 40, x + 30, x + 10},
            new double[]{y + 20, y + 20, y + 30, y + 30},
            4
        );
        gc.setFill(Color.DARKBLUE);
        gc.fillOval(x + 15, y, 10, 15);
    }
    
    private void renderBomber(GraphicsContext gc) {
        gc.setFill(Color.DARKRED);
        gc.fillOval(x + 10, y, 20, 40);
        gc.fillRect(x, y + 15, 40, 10);
        gc.setFill(Color.GRAY);
        gc.fillOval(x + 15, y + 10, 10, 10);
    }
    
    private void renderScout(GraphicsContext gc) {
        gc.setFill(Color.ORANGE);
        gc.fillPolygon(
            new double[]{x + 20, x + 35, x + 20, x + 5},
            new double[]{y, y + 20, y + 40, y + 20},
            4
        );
        gc.setFill(Color.YELLOW);
        gc.fillRect(x, y + 18, 40, 5);
    }
    
    public boolean intersects(Bullet bullet) {
        return bullet.getX() >= x && bullet.getX() <= x + 40 &&
               bullet.getY() >= y && bullet.getY() <= y + 40;
    }
    
    public boolean intersects(Player player) {
        return player.getX() + 40 >= x && player.getX() <= x + 40 &&
               player.getY() + 40 >= y && player.getY() <= y + 40;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    
    public boolean intersects(WingmanShip wingman) {
        double distance = Math.sqrt(Math.pow(x - wingman.getX(), 2) + Math.pow(y - wingman.getY(), 2));
        return distance < (40 + 20);  // 40 is the size of the ship, 20 is the enemy collision radius
    }
}

// EnemyBullet class
class EnemyBullet {
    private double x, y;
    private static final double SPEED = 5;  // Enemy bullet speed
    private static final int SIZE = 5;      // Enemy bullet size
    
    public EnemyBullet(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void update() {
        y += SPEED;  // Move downwards
    }
    
    public void render(GraphicsContext gc) {
        gc.setFill(Color.RED);
        gc.fillOval(x, y, SIZE, SIZE);
        gc.setGlobalAlpha(0.3);
        gc.fillOval(x - 1, y - 1, SIZE + 2, SIZE + 2);
        gc.setGlobalAlpha(1.0);
    }
    
    public boolean isOffscreen(double height) {
        return y > height;
    }
    
    public boolean intersects(Player player) {
        double distance = Math.sqrt(Math.pow(x - player.getX(), 2) + Math.pow(y - player.getY(), 2));
        return distance < (SIZE + 20);  // 20 is the player collision radius
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    
    public boolean intersects(WingmanShip wingman) {
        return wingman.getX() + 40 >= x - SIZE/2 && wingman.getX() <= x + SIZE/2 &&
               wingman.getY() + 40 >= y - SIZE/2 && wingman.getY() <= y + SIZE/2;
    }
}

// Bullet class
class Bullet {
    private double x, y, dx = 0;
    private static final double SPEED = 7;
    
    public Bullet(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public Bullet(double x, double y, double spread) {
        this.x = x;
        this.y = y;
        this.dx = spread * 2;
    }
    
    public void update() {
        y -= SPEED;
        x += dx;
    }
    
    public void render(GraphicsContext gc) {
        gc.setFill(Color.WHITE);
        gc.fillOval(x - 1, y - 1, 7, 12);
        gc.setFill(Color.YELLOW);
        gc.fillOval(x, y, 5, 10);
        gc.setGlobalAlpha(0.3);
        gc.setFill(Color.YELLOW);
        gc.fillOval(x - 2, y - 2, 9, 14);
        gc.setGlobalAlpha(1.0);
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
}

// PowerUp class
class PowerUp {
    private double x, y;
    private PowerUpType type;
    private static final double SIZE = 20;
    
    public enum PowerUpType {
        TRIPLE_SHOT,
        SPREAD_SHOT,
        INVINCIBLE
    }
    
    public PowerUp(double x, double y) {
        this.x = x;
        this.y = y;
        PowerUpType[] types = PowerUpType.values();
        this.type = types[(int)(Math.random() * types.length)];
    }
    
    public void update() {
        y += 1.5;
    }
    
    public void render(GraphicsContext gc) {
        switch(type) {
            case TRIPLE_SHOT:
                gc.setFill(Color.YELLOW);
                break;
            case SPREAD_SHOT:
                gc.setFill(Color.PURPLE);
                break;
            case INVINCIBLE:
                gc.setFill(Color.CYAN);
                break;
        }
        
        gc.fillOval(x, y, SIZE, SIZE);
        gc.setGlobalAlpha(0.3);
        gc.fillOval(x - 5, y - 5, SIZE + 10, SIZE + 10);
        gc.setGlobalAlpha(1.0);
        gc.setStroke(Color.WHITE);
        gc.strokeOval(x, y, SIZE, SIZE);
    }
    
    public boolean intersects(Bullet bullet) {
        return bullet.getX() >= x && bullet.getX() <= x + SIZE &&
               bullet.getY() >= y && bullet.getY() <= y + SIZE;
    }
    
    public PowerUpType getType() { return type; }
    public double getY() { return y; }
}

// Explosion class
class Explosion {
    private double x, y;
    private int frame;
    private static final int TOTAL_FRAMES = 16;
    private static final int FRAME_DELAY = 2;
    private int frameDelay;
    private static final double SIZE = 40;
    
    public Explosion(double x, double y) {
        this.x = x;
        this.y = y;
        this.frame = 0;
        this.frameDelay = FRAME_DELAY;
    }
    
    public void update() {
        frameDelay--;
        if (frameDelay <= 0) {
            frame++;
            frameDelay = FRAME_DELAY;
        }
    }
    
    public void render(GraphicsContext gc) {
        double alpha = 1.0 - (double)frame / TOTAL_FRAMES;
        double size = SIZE * (1 + (double)frame / TOTAL_FRAMES);
        
        gc.save();
        gc.setGlobalAlpha(alpha);
        
        gc.setFill(Color.WHITE);
        gc.fillOval(x + SIZE/2 - size/2, y + SIZE/2 - size/2, size, size);
        
        gc.setFill(Color.ORANGE);
        gc.fillOval(x + SIZE/2 - size/3, y + SIZE/2 - size/3, size/1.5, size/1.5);
        
        gc.setFill(Color.YELLOW);
        gc.fillOval(x + SIZE/2 - size/4, y + SIZE/2 - size/4, size/2, size/2);
        
        gc.restore();
    }
    
    public boolean isFinished() {
        return frame >= TOTAL_FRAMES;
    }
}

// Meteor class (meteorite)
class Meteor {
    private double x, y;
    private double dx, dy;  // Movement speed
    private MeteorSize size;
    private int health;
    private double speed = 3;  // Base speed
    
    // Storage for spot details and sizes
    private double[] detailX;
    private double[] detailY;
    private double[] detailSize;
    private double[] detailAngles;  // Storage for each spot's angle
    private double rotationSpeed;   // Rotation speed
    
    public enum MeteorSize {
        SMALL(1, 20),    // Small meteor: radius 20, health value 1
        MEDIUM(2, 40),   // Medium meteor: radius 40, health value 2
        LARGE(3, 60);    // Large meteor: radius 60, health value 3
        
        private final int health;
        private final int size;
        
        MeteorSize(int health, int size) {
            this.health = health;
            this.size = size;
        }
        
        public int getHealth() { return health; }
        public int getSize() { return size; }
    }
    
    public Meteor(double x, double y, MeteorSize size) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.health = size.getHealth();
        this.speed = 3;  // Initialize base speed
        
        // Random speed
        this.dx = (Math.random() - 0.5) * 4;  // Random speed between -2 and 2
        this.dy = 2 + Math.random() * 3;      // Random speed between 2 and 5
        
        // Initialize spot properties
        detailX = new double[3];
        detailY = new double[3];
        detailSize = new double[3];
        detailAngles = new double[3];
        rotationSpeed = 0.1; // Random rotation speed and direction
        
        // Generate three different sized spots
        for (int i = 0; i < 3; i++) {
            detailSize[i] = size.getSize() * (0.15 + Math.random() * 0.15); // 15%-30% of the meteor size
            double radius = size.getSize() * 0.3; // Distance from center to spot
            detailAngles[i] = Math.random() * Math.PI * 2; // Random initial angle
            // Calculate initial position
            detailX[i] = radius * Math.cos(detailAngles[i]);
            detailY[i] = radius * Math.sin(detailAngles[i]);
        }
    }
    
    public void setVelocity(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public void update() {
        x += dx;
        y += dy;
        
        // Update spot angles and positions
        for (int i = 0; i < 3; i++) {
            detailAngles[i] += rotationSpeed;
            double radius = size.getSize() * 0.3;
            detailX[i] = radius * Math.cos(detailAngles[i]);
            detailY[i] = radius * Math.sin(detailAngles[i]);
        }
        
        // Prevent meteor from going out of screen boundaries
        if (x < 0 || x > 800 - size.getSize()) {
            dx = -dx;
        }
    }
    
    public void render(GraphicsContext gc) {
        gc.setFill(Color.GRAY);
        gc.fillOval(x, y, size.getSize(), size.getSize());
        
        // Render rotating spots
        gc.setFill(Color.DARKGRAY);
        double centerX = x + size.getSize() / 2;
        double centerY = y + size.getSize() / 2;
        for (int i = 0; i < 3; i++) {
            gc.fillOval(centerX + detailX[i] - detailSize[i]/2, 
                       centerY + detailY[i] - detailSize[i]/2, 
                       detailSize[i], detailSize[i]);
        }
    }
    
    public boolean intersects(Bullet bullet) {
        double radius = size.getSize()/2;
        double dx = bullet.getX() - x;
        double dy = bullet.getY() - y;
        return Math.sqrt(dx * dx + dy * dy) < radius;
    }
    
    public boolean intersects(Player player) {
        double radius = size.getSize()/2;
        double dx = (player.getX() + 20) - x;  // Player center point
        double dy = (player.getY() + 20) - y;
        return Math.sqrt(dx * dx + dy * dy) < radius + 20;  // 20 is the player collision radius
    }
    
    public void damage() {
        health--;
    }
    
    public boolean isDestroyed() {
        return health <= 0;
    }
    
    public boolean isOffscreen(int height) {
        return y > height;
    }
    
    public double getX() { return x; }
    public double getY() { return y; }
    public MeteorSize getSize() { return size; }
    
    public boolean intersects(Enemy enemy) {
        double radius = size.getSize()/2;
        double dx = (enemy.getX() + 20) - x;  // Enemy center point
        double dy = (enemy.getY() + 20) - y;
        return Math.sqrt(dx * dx + dy * dy) < radius + 20;  // 20 is the enemy collision radius
    }
    
    public boolean intersects(WingmanShip wingman) {
        double radius = size.getSize()/2;
        double dx = (wingman.getX() + 20) - x;  // Ship center point
        double dy = (wingman.getY() + 20) - y;
        return Math.sqrt(dx * dx + dy * dy) < radius + 20;  // 20 is the ship collision radius
    }
}

// Friendly ship class
class WingmanShip {
    private double x, y;
    private double offsetX, offsetY;
    private int health = 3;
    private boolean invincible = false;
    private long invincibleStartTime;
    private PowerUp.PowerUpType currentPowerUp = null;
    private long powerUpStartTime;
    private static final long POWER_UP_DURATION = 5_000_000_000L; // Changed to 5 seconds, using nanoseconds
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int SHIP_SIZE = 40;

    public WingmanShip(double offsetX, double offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void update(Player player, List<WingmanShip> wingmen) {
        // Update position directly
        this.x = player.getX() + offsetX;
        this.y = player.getY() + offsetY;
        
        // Update invincible status
        if (invincible && System.currentTimeMillis() - invincibleStartTime > 2000) {
            invincible = false;
        }
        
        // Update skill status
        if (currentPowerUp != null && System.currentTimeMillis() - powerUpStartTime > 10000) {
            currentPowerUp = null;
        }
    }

    public void render(GraphicsContext gc) {
        // Only render when not invincible or during flash effect
        if (!invincible || System.currentTimeMillis() % 200 < 100) {
            // Main body (white)
            gc.setFill(Color.WHITE);
            // Draw triangular body
            gc.fillPolygon(
                new double[]{x + 20, x + 40, x},  // x coordinates
                new double[]{y, y + 40, y + 40},  // y coordinates
                3
            );
            
            // Blue circular window
            gc.setFill(Color.LIGHTBLUE);
            gc.fillOval(x + 15, y + 10, 10, 10);
            
            // Engine effect
            gc.setFill(Color.YELLOW);
            gc.fillOval(x + 15, y + 40, 10, 15);
            gc.setGlobalAlpha(0.6);
            double flameHeight = 10 + Math.random() * 5;
            gc.setFill(Color.ORANGE);
            gc.fillOval(x + 15, y + 45, 10, flameHeight);
            gc.setGlobalAlpha(1.0);
        }
        
        // Health bar
        double healthBarWidth = 40;
        double healthBarHeight = 5;
        // Health bar background
        gc.setFill(Color.RED);
        gc.fillRect(x, y + 45, healthBarWidth, healthBarHeight);
        // Current health
        gc.setFill(Color.GREEN);
        gc.fillRect(x, y + 45, healthBarWidth * (health / 3.0), healthBarHeight);
        
        // Invincible effect
        if (invincible) {
            gc.setGlobalAlpha(0.3);
            gc.setFill(Color.CYAN);
            gc.fillOval(x - 10, y - 10, 60, 60);
            gc.setGlobalAlpha(1.0);
        }
    }

    public Bullet shoot() {
        if (currentPowerUp == PowerUp.PowerUpType.TRIPLE_SHOT) {
            return new Bullet(x + 20, y, 0);
        } else if (currentPowerUp == PowerUp.PowerUpType.SPREAD_SHOT) {
            return new Bullet(x + 20, y, (Math.random() - 0.5) * 2);
        } else {
            return new Bullet(x + 20, y);
        }
    }

    public void damage() {
        if (!invincible) {
            health--;
            invincible = true;
            invincibleStartTime = System.currentTimeMillis();
        }
    }

    public void activatePowerUp(PowerUp.PowerUpType type) {
        currentPowerUp = type;
        powerUpStartTime = System.nanoTime();  // Use nanoseconds for timing
        if (type == PowerUp.PowerUpType.INVINCIBLE) {
            invincible = true;
            invincibleStartTime = System.nanoTime();
        }
    }

    public void deactivateInvincible() {
        invincible = false;
        if (currentPowerUp == PowerUp.PowerUpType.INVINCIBLE) {
            currentPowerUp = null;
            powerUpStartTime = 0;  // Reset timer
            invincibleStartTime = 0;
        }
    }

    public boolean isInvincible() {
        return invincible;
    }

    public void checkPowerUpExpiration() {
        if (currentPowerUp != null) {
            if (System.nanoTime() - powerUpStartTime > POWER_UP_DURATION) {  // Use nanoseconds for comparison
                if (currentPowerUp == PowerUp.PowerUpType.INVINCIBLE) {
                    invincible = false;
                }
                currentPowerUp = null;
            }
        }
    }

    public int getHealth() {
        return health;
    }

    public PowerUp.PowerUpType getCurrentPowerUp() {
        return currentPowerUp;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    // Check if given player position would cause friendly ship to go out of bounds
    public boolean wouldBeOutOfBounds(double playerNewX, double playerNewY) {
        double newX = playerNewX + offsetX;
        double newY = playerNewY + offsetY;
        return newX < 0 || newX > WIDTH - SHIP_SIZE || 
               newY < 0 || newY > HEIGHT - SHIP_SIZE;
    }

    // Add method to get offset
    public double getOffsetX() {
        return offsetX;
    }
    
    public double getOffsetY() {
        return offsetY;
    }
} 