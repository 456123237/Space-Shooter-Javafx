package application;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

public class Main extends Application {
    private static MediaPlayer mediaPlayer;
    
    @Override
    public void start(Stage primaryStage) {
        // Initialize background music
        try {
            String musicFile = "BackgroundMusic.MP3";
            Media sound = new Media(new File(musicFile).toURI().toString());
            mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop indefinitely
            mediaPlayer.setVolume(0.5); // Set volume to 50%
            mediaPlayer.play(); // Start playing
        } catch (Exception e) {
            System.out.println("Error loading background music: " + e.getMessage());
        }
        
        // Start the game with StartScene
        StartScene startScene = new StartScene(primaryStage);
        primaryStage.setScene(startScene.getScene());
        primaryStage.setTitle("Space Shooter");
        primaryStage.show();
    }
    
    @Override
    public void stop() {
        // Stop the music when the application closes
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
    
    // Add getter for mediaPlayer to control music in other scenes
    public static MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }
    
    public void startLevel1(Stage primaryStage, String username) {
        Level1Scene level1Scene = new Level1Scene(primaryStage, username);
        primaryStage.setScene(level1Scene.getScene());
    }
    
    public void startLevel2(Stage primaryStage, String username) {
        Level2Scene level2Scene = new Level2Scene(primaryStage, username);
        primaryStage.setScene(level2Scene.getScene());
    }
    
    public void startLevel3(Stage primaryStage, String username) {
        Level3Scene level3Scene = new Level3Scene(primaryStage, username);
        primaryStage.setScene(level3Scene.getScene());
    }
    
    public void startLevel4(Stage primaryStage, String username) {
        Level4Scene level4Scene = new Level4Scene(primaryStage, username);
        primaryStage.setScene(level4Scene.getScene());
    }
    
    public void startLevel5(Stage primaryStage, String username) {
        Level5Scene level5Scene = new Level5Scene(primaryStage, username);
        primaryStage.setScene(level5Scene.getScene());
    }
    
    public void startLevelSelect(Stage primaryStage, String username) {
        LevelSelectScene levelSelectScene = new LevelSelectScene(primaryStage, username);
        primaryStage.setScene(levelSelectScene.getScene());
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}