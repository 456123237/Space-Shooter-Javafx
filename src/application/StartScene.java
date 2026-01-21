package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Background;

public class StartScene {
    private Scene scene;
    private Stage primaryStage;
    
    public StartScene(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Create main layout
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(30));
        
        // Set background image
        try {
            Image backgroundImage = new Image("file:./Figure1.jpg");
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
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setEffect(new DropShadow(10, Color.BLUE));
        
        TabPane tabPane = new TabPane();
        tabPane.setTabMinWidth(100);
        tabPane.setStyle("-fx-background-color: transparent;");
        
        Tab loginTab = new Tab("Login");
        loginTab.setClosable(false);
        loginTab.setContent(createLoginPane());
        
        Tab registerTab = new Tab("Register");
        registerTab.setClosable(false);
        registerTab.setContent(createRegisterPane());
        
        tabPane.getTabs().addAll(loginTab, registerTab);
        
        // Create exit button
        Button exitBtn = new Button("Exit");
        exitBtn.setStyle("-fx-background-color: #e94560; -fx-text-fill: white;");
        exitBtn.setPrefWidth(200);
        exitBtn.setOnAction(e -> System.exit(0));
        
        mainLayout.getChildren().addAll(titleLabel, tabPane, exitBtn);
        
        scene = new Scene(mainLayout, 400, 500);
    }
    
    private GridPane createLoginPane() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(20);
        grid.setPadding(new Insets(25));
        grid.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1);");
        
        // Create styled input fields and buttons
        TextField userTextField = new TextField();
        userTextField.setPromptText("Enter username");
        styleTextField(userTextField);
        
        PasswordField pwField = new PasswordField();
        pwField.setPromptText("Enter password");
        styleTextField(pwField);
        
        Button loginBtn = new Button("Login");
        styleButton(loginBtn);
        
        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.WHITE);
        
        // Add components to grid
        grid.add(createLabel("Username:"), 0, 0);
        grid.add(userTextField, 1, 0);
        grid.add(createLabel("Password:"), 0, 1);
        grid.add(pwField, 1, 1);
        grid.add(loginBtn, 1, 2);
        grid.add(messageLabel, 1, 3);
        
        loginBtn.setOnAction(e -> {
            String username = userTextField.getText();
            String password = pwField.getText();
            
            if (UserData.validateUser(username, password)) {
                Main main = new Main();
                main.startLevelSelect(primaryStage, username);
            } else {
                messageLabel.setText("Invalid username or password!");
                messageLabel.setTextFill(Color.RED);
            }
        });
        
        return grid;
    }
    
    private GridPane createRegisterPane() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(20);
        grid.setPadding(new Insets(25));
        grid.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1);");
        
        TextField userTextField = new TextField();
        userTextField.setPromptText("Enter username");
        styleTextField(userTextField);
        
        PasswordField pwField = new PasswordField();
        pwField.setPromptText("Enter password");
        styleTextField(pwField);
        
        PasswordField confirmPwField = new PasswordField();
        confirmPwField.setPromptText("Confirm password");
        styleTextField(confirmPwField);
        
        Button registerBtn = new Button("Register");
        styleButton(registerBtn);
        
        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.WHITE);
        
        grid.add(createLabel("Username:"), 0, 0);
        grid.add(userTextField, 1, 0);
        grid.add(createLabel("Password:"), 0, 1);
        grid.add(pwField, 1, 1);
        grid.add(createLabel("Confirm Password:"), 0, 2);
        grid.add(confirmPwField, 1, 2);
        grid.add(registerBtn, 1, 3);
        grid.add(messageLabel, 1, 4);
        
        registerBtn.setOnAction(e -> {
            String username = userTextField.getText();
            String password = pwField.getText();
            String confirmPassword = confirmPwField.getText();
            
            if (!password.equals(confirmPassword)) {
                messageLabel.setText("Passwords do not match!");
                messageLabel.setTextFill(Color.RED);
                return;
            }
            
            if (UserData.registerUser(username, password)) {
                messageLabel.setText("Registration successful! Please switch to login tab.");
                messageLabel.setTextFill(Color.GREEN);
            } else {
                messageLabel.setText("Username already exists!");
                messageLabel.setTextFill(Color.RED);
            }
        });
        
        return grid;
    }
    
    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        return label;
    }
    
    private void styleTextField(TextField textField) {
        textField.setPrefWidth(200);
        textField.setStyle("-fx-background-color: rgba(255, 255, 255, 0.2); " +
                         "-fx-text-fill: white; " +
                         "-fx-prompt-text-fill: lightgray;");
    }
    
    private void styleButton(Button button) {
        button.setPrefWidth(200);
        button.setStyle("-fx-background-color: #0f3460; " +
                      "-fx-text-fill: white; " +
                      "-fx-font-weight: bold;");
    }
    
    public Scene getScene() {
        return scene;
    }
} 