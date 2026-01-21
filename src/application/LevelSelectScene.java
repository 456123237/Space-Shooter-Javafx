package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Background;
import javafx.scene.media.MediaPlayer;

public class LevelSelectScene {
    private Scene scene;
    private Stage primaryStage;
    private String username;
    
    public LevelSelectScene(Stage primaryStage, String username) {
        this.primaryStage = primaryStage;
        this.username = username;
        
        // Create main layout
        VBox mainLayout = new VBox(30);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(40));
        
        // Set background image
        try {
            Image backgroundImage = new Image("file:./Figure2.jpg");
            BackgroundImage background = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
            );
            mainLayout.setBackground(new Background(background));
        } catch (Exception e) {
            // Fallback to gradient background if image loading fails
            mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e);");
            System.out.println("Failed to load background image: " + e.getMessage());
        }
        
        // Create title
        Label titleLabel = new Label("Space Shooter");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setEffect(new DropShadow(10, Color.BLUE));
        
        // Create welcome label with semi-transparent background
        Label welcomeLabel = new Label("Welcome back, " + username);
        welcomeLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        welcomeLabel.setTextFill(Color.WHITE);
        welcomeLabel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-padding: 5 10 5 10; -fx-background-radius: 5;");
        
        // Create level grid
        GridPane levelGrid = new GridPane();
        levelGrid.setAlignment(Pos.CENTER);
        levelGrid.setHgap(30);
        levelGrid.setVgap(30);
        
        int unlockedLevel = UserData.getUnlockedLevel(username);
        
        // Create level buttons
        for (int i = 0; i < 4; i++) {
            VBox levelBox = createLevelBox(i + 1, unlockedLevel);
            levelGrid.add(levelBox, i % 2, i / 2);
        }
        
        // Create level 5 button
        VBox level5Box = createLevelBox(5, unlockedLevel);
        levelGrid.add(level5Box, 0, 2, 2, 1); // Span two columns
        
        // Create bottom button area
        HBox bottomButtons = new HBox(20);
        bottomButtons.setAlignment(Pos.CENTER);
        
        Button logoutBtn = new Button("Logout");
        styleButton(logoutBtn, "#e94560");
        logoutBtn.setOnAction(e -> {
            // Stop current music first
            MediaPlayer currentPlayer = Main.getMediaPlayer();
            if (currentPlayer != null) {
                currentPlayer.stop();
            }
            
            // Then restart to start scene
            Main main = new Main();
            main.start(primaryStage);
        });
        
        bottomButtons.getChildren().add(logoutBtn);
        
        mainLayout.getChildren().addAll(titleLabel, welcomeLabel, levelGrid, bottomButtons);
        
        scene = new Scene(mainLayout, 800, 600);
    }
    
    private VBox createLevelBox(int level, int unlockedLevel) {
        VBox levelBox = new VBox(15);
        levelBox.setAlignment(Pos.CENTER);
        levelBox.setPadding(new Insets(20));
        levelBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 10;");
        
        Button levelBtn = new Button("Level " + level);
        levelBtn.setPrefSize(150, 150);
        
        Label scoreLabel = new Label("Highest Score: " + UserData.getLevelHighScore(username, level));
        scoreLabel.setTextFill(Color.RED);
        scoreLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        
        if (level <= unlockedLevel) {
            styleButton(levelBtn, "#0f3460");
            levelBtn.setOnAction(e -> startLevel(level));
        } else {
            levelBtn.setDisable(true);
            levelBtn.setText("🔒\nLevel " + level);
            levelBtn.setStyle("-fx-background-color: #2a2a4a; -fx-text-fill: gray;");
            scoreLabel.setText("Locked");
        }
        
        levelBox.getChildren().addAll(levelBtn, scoreLabel);
        return levelBox;
    }
    
    private void styleButton(Button button, String color) {
        button.setStyle(
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 16px; " +
            "-fx-background-radius: 5;"
        );
        
        // Add mouse hover effect
        button.setOnMouseEntered(e -> 
            button.setStyle(
                "-fx-background-color: derive(" + color + ", 20%); " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 16px; " +
                "-fx-background-radius: 5;"
            )
        );
        
        button.setOnMouseExited(e -> 
            button.setStyle(
                "-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 16px; " +
                "-fx-background-radius: 5;"
            )
        );
    }
    
    private void startLevel(int level) {
        Main main = new Main();
        switch (level) {
            case 1:
                main.startLevel1(primaryStage, username);
                break;
            case 2:
                main.startLevel2(primaryStage, username);
                break;
            case 3:
                main.startLevel3(primaryStage, username);
                break;
            case 4:
                main.startLevel4(primaryStage, username);
                break;
            case 5:
                main.startLevel5(primaryStage, username);
                break;
        }
    }
    
    public Scene getScene() {
        return scene;
    }
} 