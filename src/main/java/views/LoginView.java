package views;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.function.BiConsumer;
import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Consumer;
import javafx.scene.input.KeyCode;


public class LoginView {
    private final Stage stage;
    private final BiConsumer<String, String> onLoginSuccess;
    private static final Map<String, String> credentials = new HashMap<>();
    private static final Map<String, String> roles = new HashMap<>();
    private CheckBox rememberMeCheckBox = new CheckBox("Remember Me");

    static {
        credentials.put("manager", "manager123");
        credentials.put("team_lead", "leadpass");
        credentials.put("trainer", "trainme");

        roles.put("manager", "Manager");
        roles.put("team_lead", "Team Lead");
        roles.put("trainer", "Trainer");
    }

    public LoginView(Stage stage, BiConsumer<String, String> onLoginSuccess, Consumer<Boolean> onRememberMeChanged) {
        this.stage = stage;
        this.onLoginSuccess = onLoginSuccess;
        this.onRememberMeChanged = onRememberMeChanged;
    }

    private final Consumer<Boolean> onRememberMeChanged;

    private void saveRememberedUsername(String username) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("remember.txt"))) {
            writer.write(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String loadRememberedUsername() {
        File file = new File("remember.txt");
        if (!file.exists()) return "";
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine();
        } catch (IOException e) {
            return "";
        }
    }

    public void show() {
        Label titleLabel = new Label("Arby’s Store Manager Login");
        TextField usernameField = new TextField();
        usernameField.setText(loadRememberedUsername());
        PasswordField passwordField = new PasswordField();
        usernameField.setPromptText("Username");
        passwordField.setPromptText("Password");
        Button loginButton = new Button("Login");
        usernameField.setText(loadRememberedUsername());

        Label errorLabel = new Label();

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().toLowerCase();
            String password = passwordField.getText();

            if (rememberMeCheckBox.isSelected()) {
                saveRememberedUsername(username);
            } else {
                saveRememberedUsername(""); // clear it
            }

            if (credentials.containsKey(username) && credentials.get(username).equals(password)) {
                String role = roles.getOrDefault(username, "User");
                onLoginSuccess.accept(username, role);
                onRememberMeChanged.accept(rememberMeCheckBox.isSelected());
            } else {
                errorLabel.setText("Invalid username or password");
            }

        });


        VBox layout = new VBox(10, titleLabel, usernameField, passwordField, rememberMeCheckBox, loginButton, errorLabel);
        layout.setStyle("-fx-padding: 40; -fx-alignment: center;");

        Scene scene = new Scene(layout, 400, 300);

        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loginButton.fire();
            }
        });

        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loginButton.fire();
            }
        });
        stage.setScene(scene);
        stage.setTitle("Login");
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }
}

