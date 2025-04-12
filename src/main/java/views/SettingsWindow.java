package views;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.function.Consumer;

public class SettingsWindow {
    private final boolean isDarkMode;
    private final boolean isFullscreen;
    private final Stage stage;
    private final Runnable onPreferencesChanged;
    private final Consumer<Boolean> onDarkModeChanged;
    private final Consumer<Boolean> onFullscreenChanged;

    public SettingsWindow(
            boolean isDarkMode,
            boolean isFullscreen,
            Stage stage,
            Consumer<Boolean> onDarkModeChanged,
            Consumer<Boolean> onFullscreenChanged,
            Runnable onPreferencesChanged) {
        this.isDarkMode = isDarkMode;
        this.isFullscreen = isFullscreen;
        this.stage = stage;
        this.onDarkModeChanged = onDarkModeChanged;
        this.onFullscreenChanged = onFullscreenChanged;
        this.onPreferencesChanged = onPreferencesChanged;
    }

    public void show() {
        Stage settingsStage = new Stage();
        settingsStage.setTitle("Settings");

        CheckBox darkModeCheck = new CheckBox("Enable Dark Mode");
        darkModeCheck.setSelected(isDarkMode);

        CheckBox fullscreenCheck = new CheckBox("Fullscreen Windowed Mode");
        fullscreenCheck.setSelected(isFullscreen);

        Button applyButton = new Button("Apply Changes");
        applyButton.setOnAction(e -> {
            onDarkModeChanged.accept(darkModeCheck.isSelected());
            onFullscreenChanged.accept(fullscreenCheck.isSelected());
            onPreferencesChanged.run();
            settingsStage.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> settingsStage.close());

        VBox layout = new VBox(10, darkModeCheck, fullscreenCheck, applyButton, cancelButton);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Scene scene = new Scene(layout, 300, 200);
        settingsStage.setScene(scene);
        settingsStage.initOwner(stage);
        settingsStage.show();
    }
}