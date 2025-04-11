package views;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.function.BiConsumer;

public class LoginView {
    private final Stage stage;
    private final BiConsumer<String, String> onLoginSuccess;

    public LoginView(Stage stage, BiConsumer<String, String> onLoginSuccess) {
        this.stage = stage;
        this.onLoginSuccess = onLoginSuccess;
    }

    public void show() {
        Label titleLabel = new Label("Arby’s Store Manager Login");
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");

        Label errorLabel = new Label();

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().toLowerCase();
            String password = passwordField.getText();

            if ((username.equals("tristan") && password.equals("admin")) ||
                    (username.equals("jess") && password.equals("team")) ||
                    (username.equals("jamie") && password.equals("train"))) {

                String role = switch (username) {
                    case "tristan" -> "manager";
                    case "jess" -> "team_lead";
                    case "jamie" -> "trainer";
                    default -> "user";
                };

                onLoginSuccess.accept(username, role);
            } else {
                errorLabel.setText("Invalid username or password");
            }
        });

        VBox layout = new VBox(10, titleLabel, usernameField, passwordField, loginButton, errorLabel);
        layout.setStyle("-fx-padding: 40; -fx-alignment: center;");

        stage.setScene(new Scene(layout, 400, 300));
        stage.setTitle("Login");
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }
}

