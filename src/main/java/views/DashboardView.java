package views;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import views.SettingsWindow;

import java.util.function.Consumer;

public class DashboardView {
    private final Stage stage;
    private final String currentUserRole;
    private final String currentUsername;
    private final Consumer<Void> onOpenSales;
    private final Runnable onLogout;
    private final Runnable onLogoutForget;
    private boolean isDarkMode;
    private boolean isFullscreen;
    private final Runnable onPreferencesChanged;

    public DashboardView(Stage stage,
                         String currentUserRole,
                         String currentUsername,
                         Consumer<Void> onOpenSales,
                         Runnable onLogout,
                         Runnable onLogoutForget,
                         boolean isDarkMode,
                         boolean isFullscreen,
                         Runnable onPreferencesChanged) {
        this.stage = stage;
        this.currentUserRole = currentUserRole;
        this.currentUsername = currentUsername;
        this.onOpenSales = onOpenSales;
        this.onLogout = onLogout;
        this.onLogoutForget = onLogoutForget;
        this.isDarkMode = isDarkMode;
        this.isFullscreen = isFullscreen;
        this.onPreferencesChanged = onPreferencesChanged;
    }

    public void show() {
        // Top Bar
        Button settingsButton = new Button("⚙️ Settings");
        Button logoutButton = new Button("Logout");
        Button logoutForgetButton = new Button("Logout and Forget");

        HBox topBar = new HBox();
        Region spacer = new Region(); // flexible space between settings and logout
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.getChildren().addAll(settingsButton, spacer, logoutButton, logoutForgetButton);
        topBar.setSpacing(10);
        topBar.setStyle("-fx-padding: 10;");

        // Welcome Text
        Label titleLabel = new Label("Welcome to the Dashboard, " + currentUserRole + "!");
        Label messageLabel = new Label("Let’s make today a great day, " + currentUsername + "!");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        messageLabel.setStyle("-fx-font-size: 14px;");

        VBox centerText = new VBox(5, titleLabel, messageLabel);
        centerText.setStyle("-fx-alignment: center;");

        // Sales Button
        Button salesButton = new Button("📊 Sales Entry");
        salesButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px 20px;");
        VBox centerBox = new VBox(20, centerText, salesButton);
        centerBox.setStyle("-fx-alignment: center;");

        // Full Layout
        BorderPane layout = new BorderPane();
        layout.setTop(topBar);
        layout.setCenter(centerBox);
        layout.setStyle("-fx-padding: 30;");

        // Actions
        salesButton.setOnAction(e -> onOpenSales.accept(null));
        logoutButton.setOnAction(e -> onLogout.run());
        logoutForgetButton.setOnAction(e -> onLogoutForget.run());
        settingsButton.setOnAction(e -> {
            new SettingsWindow(
                    isDarkMode,
                    isFullscreen,
                    stage,
                    newValue -> this.isDarkMode = newValue,
                    newValue -> this.isFullscreen = newValue,
                    () -> {
                        onPreferencesChanged.run();
                    }
            ).show();
        });

        Scene scene = new Scene(layout, 800, 600);
        stage.setScene(scene);
        if (isDarkMode) {
            scene.getRoot().setStyle("-fx-base: #2b2b2b; -fx-text-fill: white;");
        } else {
            scene.getRoot().setStyle("");
        }

        stage.setTitle("Dashboard");
        stage.show();
        onPreferencesChanged.run(); // This ensures current theme/fullscreen is applied on show
    }
}
