package com.tristan;

import views.LoginView;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.time.DayOfWeek;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
import java.time.LocalDate;
import java.util.StringTokenizer;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.input.KeyCombination;



public class Main extends Application {

    private File dataFile = new File("entries.csv");
    private ObservableList<Entry> entryList = FXCollections.observableArrayList();
    private String currentUserRole = "manager"; // default
    private boolean hasPermission(String action) {
        return switch (currentUserRole) {
            case "manager" -> true;
            case "team_lead" -> !action.equals("delete");
            case "trainer" -> action.equals("view");
            default -> false;
        };
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        isDarkMode = loadDarkModePreference();
        isFullscreen = loadFullscreenPreference();

        LoginView loginView = new LoginView(primaryStage, (username, role) -> {
            currentUsername = username;
            currentUserRole = role;
            showDashboard(primaryStage);
        });

        loginView.show();
    }

    private boolean loadDarkModePreference() {
        File file = new File("darkmode.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                return Boolean.parseBoolean(reader.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false; // Default to light mode
    }

    private boolean shouldRememberUser() {
        File file = new File("remember.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = reader.readLine();
                return line != null && line.equalsIgnoreCase("true");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean isDarkMode = false;
    private boolean isFullscreen = true;

    private void saveDarkModePreference(boolean darkMode) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("darkmode.txt"))) {
            writer.write(darkMode ? "true" : "false");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFullscreenPreference(boolean enabled) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("fullscreen.txt"))) {
            writer.write(Boolean.toString(enabled));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean loadFullscreenPreference() {
        File file = new File("fullscreen.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                return Boolean.parseBoolean(reader.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false; // default to windowed mode
    }

    private void saveLastUsername(String username) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("lastuser.txt"))) {
            writer.write(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String loadLastUsername() {
        File file = new File("lastuser.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                return reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String currentUsername = "";

    private Stage mainStage; //Aid for fullscreen window


    private void saveRememberPreference(boolean remember) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("remember.txt"))) {
            writer.write(remember ? "true" : "false");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final Map<String, String> userRoles = Map.of(
            "manager", "Manager",
            "team_lead", "Team Lead",
            "trainer", "Trainer"
    );

    private static final Map<String, String> roleNames = Map.of(
            "manager", "Manager",
            "team_lead", "Team Lead",
            "trainer", "Trainer"
    );

    private static final Map<String, String> displayNames = Map.of(
            "manager", "Tristan",
            "team_lead", "Jess",
            "trainer", "Jamie"
    );

    private static final Map<String, List<String>> motivationalQuotes = Map.of(
            "manager", List.of(
                    "🧠 Vision. Strategy. Execution. You’ve got it all.",
                    "💼 Lead by example and stay sharp!"
            ),
            "team_lead", List.of(
                    "👥 Leadership isn't a position, it's an action!",
                    "📈 Guide your team to greatness!"
            ),
            "trainer", List.of(
                    "📚 Keep leveling up your crew!",
                    "🎯 Training today builds wins tomorrow!"
            )
    );

    private void showDashboard(Stage stage) {
        String roleLabel = roleNames.getOrDefault(currentUserRole, "Staff");
        String displayName;
        if (displayNames.containsKey(currentUsername)) {
            displayName = displayNames.get(currentUsername);
        } else if (currentUsername != null && !currentUsername.isEmpty()) {
            displayName = currentUsername.substring(0, 1).toUpperCase() + currentUsername.substring(1);
        } else {
            displayName = "User";
        }

        String emoji = switch (currentUserRole) {
            case "manager" -> "🧠";
            case "team_lead" -> "👥";
            case "trainer" -> "📚";
            default -> "👋";
        };
        String welcomeMessage = emoji + " Welcome back, " + roleLabel + " " + displayName + "!";

        Label welcomeLabel = new Label(welcomeMessage);
        welcomeLabel.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 18));
        List<String> quotes = motivationalQuotes.getOrDefault(currentUserRole, List.of("💬 Ready to work!"));
        String randomQuote = quotes.get((int) (Math.random() * quotes.size()));
        Label motivationLabel = new Label(randomQuote);
        motivationLabel.setFont(Font.font("Segoe UI Emoji", 14));

        Button enterSalesBtn = new Button("Enter Sales & View Data");
        enterSalesBtn.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white;");
        enterSalesBtn.setOnAction(e -> showMainApp(stage, isDarkMode));
        Button logoutButton = new Button("🚪 Log Out");
        logoutButton.setStyle("-fx-background-color: #999; -fx-text-fill: white;");
        logoutButton.setOnAction(e -> {
            LoginView loginView = new LoginView(stage, (username, role) -> {
                currentUsername = username;
                currentUserRole = role;
                showDashboard(stage);
            });
            loginView.show();
        });
        Button logoutForgetButton = new Button("🚪 Log Out & Forget Me");
        logoutForgetButton.setStyle("-fx-background-color: #999; -fx-text-fill: white;");
        logoutForgetButton.setOnAction(e -> {
            saveRememberPreference(false);  // Forget the user
            LoginView loginView = new LoginView(stage, (username, role) -> {
                currentUsername = username;
                currentUserRole = role;
                showDashboard(stage);
            });
            loginView.show();
        });

        Button statsBtn = new Button("View Advanced Stats");
        statsBtn.setStyle("-fx-background-color: #8B0000; -fx-text-fill: white;");
        TextArea statsOutput = new TextArea();
        statsOutput.setEditable(false);
        statsOutput.setVisible(false);
        statsOutput.setPrefRowCount(8);
        statsOutput.setWrapText(true);
        statsBtn.setOnAction(e -> {
            statsOutput.setVisible(!statsOutput.isVisible());
            if (statsOutput.isVisible()) {
                statsOutput.setText(generateAdvancedStats()); // ⬅ we'll make this method next
            }
        });

        VBox layout;
        layout = new VBox(20);
        Button toggleDarkModeBtn = new Button("🌙 Toggle Dark Mode");
        toggleDarkModeBtn.setStyle("-fx-background-color: #444; -fx-text-fill: white;");
        toggleDarkModeBtn.setOnAction(e -> {
            isDarkMode = !isDarkMode;
            saveDarkModePreference(isDarkMode);

            String darkStyle = "-fx-background-color: #2b2b2b; -fx-text-fill: #e0e0e0;";
            String lightStyle = "-fx-background-color: #f0f0f0; -fx-text-fill: black;";

            // Loop through all children of the layout and set styles
            for (javafx.scene.Node node : layout.getChildren()) {
                if (node instanceof Label || node instanceof TextArea || node instanceof TextField) {
                    node.setStyle(isDarkMode ? darkStyle : lightStyle);
                } else if (node instanceof Button button) {
                    // Only restyle uncolored buttons like the toggle itself
                    if (button == toggleDarkModeBtn || button.getStyle().contains("#f0f0f0")) {
                        button.setStyle(isDarkMode ? darkStyle : lightStyle);
                    }
                }
            }

            // Change the layout's background
            layout.setStyle("-fx-padding: 30; -fx-alignment: center;" +
                    (isDarkMode ? "-fx-background-color: #1e1e1e;" : "-fx-background-color: #f0f0f0;"));
        });

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Day of Week");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Average Profit ($)");

        BarChart<String, Number> profitChart = new BarChart<>(xAxis, yAxis);
        profitChart.setTitle("Average Profit by Day of Week");
        profitChart.setLegendVisible(false);
        profitChart.setVisible(false); // Hide until toggled
        profitChart.setPrefHeight(300);

        Button toggleChartBtn = new Button("📊 Show Profit Chart");
        toggleChartBtn.setStyle("-fx-background-color: #4682B4; -fx-text-fill: white;");
        toggleChartBtn.setOnAction(e -> {
            profitChart.setVisible(!profitChart.isVisible());
            if (profitChart.isVisible()) {
                profitChart.getData().clear(); // Refresh
                profitChart.getData().add(generateProfitBarSeries());
            }
        });

        Button settingsButton = new Button("⚙️ Settings");
        settingsButton.setStyle("-fx-background-color: #666; -fx-text-fill: white;");
        settingsButton.setOnAction(e -> showSettingsWindow());


        layout.setStyle("-fx-padding: 30; -fx-alignment: center; -fx-background-color: #f0f0f0;");
        HBox topRightRow = new HBox(toggleDarkModeBtn);
        topRightRow.setAlignment(Pos.TOP_RIGHT);
        topRightRow.setPadding(new Insets(10));
        HBox logoutRow = new HBox(10, logoutButton, logoutForgetButton);
        logoutRow.setAlignment(Pos.CENTER);
        logoutRow.setPadding(new Insets(20));
        layout.getChildren().addAll(
                topRightRow,
                welcomeLabel,
                motivationLabel,
                enterSalesBtn,
                statsBtn,
                statsOutput,
                toggleChartBtn,
                profitChart,
                settingsButton,
                logoutRow
        );
        if (isDarkMode) {
            String darkStyle = "-fx-background-color: #2b2b2b; -fx-text-fill: #e0e0e0;";

            for (javafx.scene.Node node : layout.getChildren()) {
                if (node instanceof Label || node instanceof TextArea || node instanceof TextField) {
                    node.setStyle(darkStyle);
                } else if (node instanceof Button button) {
                    if (button.getStyle().contains("#f0f0f0") || button.getStyle().isEmpty()) {
                        button.setStyle(darkStyle);
                    }
                }
            }

            layout.setStyle("-fx-padding: 30; -fx-alignment: center; -fx-background-color: #1e1e1e;");
        }

        Scene scene = new Scene(layout, 400, 300);
        stage.setTitle("Dashboard");
        mainStage.setScene(scene);
        if (isFullscreen) {
            ensureMaximized(mainStage);
        } else {
            mainStage.setMaximized(false);
        }
        stage.show();
    }

    private void showSettingsWindow() {
        Stage settingsStage = new Stage();
        settingsStage.setTitle("Settings");

        CheckBox darkModeToggle = new CheckBox("Enable Dark Mode");
        darkModeToggle.setSelected(isDarkMode);

        CheckBox fullscreenToggle = new CheckBox("Fullscreen Windowed Mode (Maximized)");
        fullscreenToggle.setSelected(isFullscreen);

        Button clearPrefsBtn = new Button("🧹 Clear Saved Preferences");
        Button applyBtn = new Button("✅ Apply Changes");
        Button cancelBtn = new Button("❌ Cancel");
        Label messageLabel = new Label();

        clearPrefsBtn.setOnAction(e -> {
            new File("remember.txt").delete();
            new File("lastuser.txt").delete();
            new File("darkmode.txt").delete();
            messageLabel.setText("✅ Preferences cleared. They’ll reset next launch.");
        });

        applyBtn.setOnAction(e -> {
            isDarkMode = darkModeToggle.isSelected();
            saveDarkModePreference(isDarkMode);

            isFullscreen = fullscreenToggle.isSelected();
            saveFullscreenPreference(isFullscreen);

            // Instantly apply fullscreen change
            if (isFullscreen) {
                mainStage.setMaximized(true);
            } else {
                mainStage.setMaximized(false);
            }

            settingsStage.close();
        });

        cancelBtn.setOnAction(e -> settingsStage.close());

        HBox buttonRow = new HBox(10, applyBtn, cancelBtn);
        buttonRow.setAlignment(Pos.CENTER);

        VBox layout = new VBox(15, darkModeToggle, fullscreenToggle, clearPrefsBtn, buttonRow, messageLabel);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        settingsStage.setScene(new Scene(layout, 350, 220));
        settingsStage.show();
    }

    private XYChart.Series<String, Number> generateProfitBarSeries() {
    Map<DayOfWeek, List<Entry>> grouped = new HashMap<>();

    for (Entry entry : entryList) {
        DayOfWeek day = entry.getDate().getDayOfWeek();
        grouped.putIfAbsent(day, new ArrayList<>());
        grouped.get(day).add(entry);
    }

    XYChart.Series<String, Number> series = new XYChart.Series<>();

    for (DayOfWeek day : DayOfWeek.values()) {
        List<Entry> entries = grouped.getOrDefault(day, new ArrayList<>());
        double avg = entries.stream().mapToDouble(Entry::getProfit).average().orElse(0);
        series.getData().add(new XYChart.Data<>(capitalize(day.toString()), avg));
    }

    return series;
}
    private String generateAdvancedStats() {
        if (entryList.isEmpty()) return "No entries available for analysis.";

        StringBuilder stats = new StringBuilder();

        // 1. Average profit
        double totalProfit = entryList.stream().mapToDouble(Entry::getProfit).sum();
        double avgProfit = totalProfit / entryList.size();
        stats.append("📊 Average Profit per Day: $").append(String.format("%.2f", avgProfit)).append("\n");

        // 2. Biggest drop in sales
        double biggestDrop = 0;
        LocalDate dropDate = null;
        for (int i = 1; i < entryList.size(); i++) {
            double diff = entryList.get(i - 1).getSales() - entryList.get(i).getSales();
            if (diff > biggestDrop) {
                biggestDrop = diff;
                dropDate = entryList.get(i).getDate();
            }
        }
        if (dropDate != null) {
            stats.append("📉 Biggest Sales Drop: $").append(String.format("%.2f", biggestDrop))
                    .append(" on ").append(dropDate).append("\n");
        }

        // 3. Labor % of Sales
        double totalSales = entryList.stream().mapToDouble(Entry::getSales).sum();
        double totalLabor = entryList.stream().mapToDouble(Entry::getLabor).sum();
        double laborRatio = (totalSales > 0) ? (totalLabor / totalSales) * 100 : 0;
        stats.append("⚙️ Avg. Labor Cost %: ").append(String.format("%.2f", laborRatio)).append("%\n");

        // 4. Best day
        Entry best = entryList.stream()
                .max((a, b) -> Double.compare(a.getProfit(), b.getProfit()))
                .orElse(null);
        if (best != null) {
            stats.append("💰 Best Day: ")
                    .append(best.getDate())
                    .append(" — $").append(String.format("%.2f", best.getProfit()))
                    .append("\n");
        }

// 5. Worst day
        Entry worst = entryList.stream()
                .min((a, b) -> Double.compare(a.getProfit(), b.getProfit()))
                .orElse(null);
        if (worst != null) {
            stats.append("📉 Worst Day: ")
                    .append(worst.getDate())
                    .append(" — $").append(String.format("%.2f", worst.getProfit()))
                    .append("\n");
        }

        // 6. This week vs last week profit comparison
        LocalDate today = LocalDate.now();
        LocalDate thisWeekStart = today.minusDays(6);
        LocalDate lastWeekStart = today.minusDays(13);
        LocalDate lastWeekEnd = today.minusDays(7);

// This week's total profit
        double thisWeekProfit = entryList.stream()
                .filter(e -> !e.getDate().isBefore(thisWeekStart) && !e.getDate().isAfter(today))
                .mapToDouble(Entry::getProfit)
                .sum();

// Last week's total profit
        double lastWeekProfit = entryList.stream()
                .filter(e -> !e.getDate().isBefore(lastWeekStart) && !e.getDate().isAfter(lastWeekEnd))
                .mapToDouble(Entry::getProfit)
                .sum();

        stats.append("\n📅 This Week Profit: $").append(String.format("%.2f", thisWeekProfit)).append("\n");
        stats.append("📅 Last Week Profit: $").append(String.format("%.2f", lastWeekProfit)).append("\n");

        if (lastWeekProfit == 0 && thisWeekProfit == 0) {
            stats.append("⚠️ No data for the past 2 weeks.\n");
        } else if (lastWeekProfit == 0) {
            stats.append("📈 100% increase (no profit last week)\n");
        } else {
            double change = ((thisWeekProfit - lastWeekProfit) / lastWeekProfit) * 100;
            stats.append((change >= 0 ? "📈 Increase: " : "📉 Decrease: "))
                    .append(String.format("%.2f", change)).append("%\n");
        }

        // 7. Most profitable day of the week (on average)
        Map<DayOfWeek, List<Entry>> groupedByDay = new HashMap<>();

        for (Entry entry : entryList) {
            DayOfWeek day = entry.getDate().getDayOfWeek();
            groupedByDay.putIfAbsent(day, new ArrayList<>());
            groupedByDay.get(day).add(entry);
        }

        DayOfWeek bestDay = null;
        double bestAvgProfit = 0;

        for (DayOfWeek day : groupedByDay.keySet()) {
            List<Entry> entries = groupedByDay.get(day);
            double avg = entries.stream().mapToDouble(Entry::getProfit).average().orElse(0);

            if (avg > bestAvgProfit) {
                bestAvgProfit = avg;
                bestDay = day;
            }
        }

        if (bestDay != null) {
            stats.append("\n🏆 Most Profitable Day of Week: ")
                    .append(capitalize(bestDay.toString()))
                    .append(" — $").append(String.format("%.2f", bestAvgProfit)).append(" avg profit\n");
        }

        return stats.toString();
    }

    private void showMainApp(Stage primaryStage, boolean darkMode) {
        loadEntriesFromCSV(dataFile, entryList);
        VBox root = new VBox(10);

        Label titleLabel = new Label("Daily Sales Entry");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField salesField = new TextField();
        TextField laborField = new TextField();
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Enter notes for the day...");
        notesArea.setPrefRowCount(2);
        salesField.setPromptText("Enter sales total ($)");
        laborField.setPromptText("Enter labor cost ($)");
        Button saveButton = new Button("💾 Save Entry");
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        Button updateButton = new Button("🔄 Update Entry");
        updateButton.setStyle("-fx-background-color: #FFA500; -fx-text-fill: white;");
        Button deleteButton = new Button("🗑️ Delete Entry");
        Button exportButton = new Button("📤 Export CSV");
        exportButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        Button backButton = new Button("🔙 Back to Dashboard");
        backButton.setStyle("-fx-background-color: #999; -fx-text-fill: white;");
        Button statsBtn = new Button("📊 View Advanced Stats");
        Button toggleChartBtn = new Button("📈 Show Profit Chart");
        Button exportSummaryBtn = new Button("📝 Export Sales Summary (.txt)");
        exportSummaryBtn.setStyle("-fx-background-color: #6A1B9A; -fx-text-fill: white;");

        saveButton.setDisable(!hasPermission("add"));
        updateButton.setDisable(!hasPermission("edit"));
        deleteButton.setDisable(!hasPermission("delete"));
        exportButton.setDisable(!hasPermission("export"));
        exportSummaryBtn.setDisable(!hasPermission("export"));
        if (!hasPermission("add")) {
            saveButton.setTooltip(new Tooltip("You don’t have permission to add entries."));
        }
        if (!hasPermission("edit")) {
            updateButton.setTooltip(new Tooltip("You don’t have permission to edit entries."));
        }
        if (!hasPermission("delete")) {
            deleteButton.setTooltip(new Tooltip("You don’t have permission to delete entries."));
        }
        if (!hasPermission("export")) {
            exportButton.setTooltip(new Tooltip("You don’t have permission to export files."));
            exportSummaryBtn.setTooltip(new Tooltip("You don’t have permission to export files."));
        }

        TableView<Entry> tableView = new TableView<>(entryList);
        tableView.setOnMouseClicked(event -> {
            Entry selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                datePicker.setValue(selected.getDate());
                salesField.setText(String.valueOf(selected.getSales()));
                laborField.setText(String.valueOf(selected.getLabor()));
            }
        });


        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: green;");
        updateButton.setOnAction(e -> {
            Entry selected = tableView.getSelectionModel().getSelectedItem();
            if (!hasPermission("edit")) {
                messageLabel.setText("❌ You don’t have permission to edit entries.");
                return; }
            if (selected == null) {
                messageLabel.setText("❌ No entry selected.");
                return;
            }


            try {
                LocalDate newDate = datePicker.getValue();
                double newSales = Double.parseDouble(salesField.getText());
                double newLabor = Double.parseDouble(laborField.getText());

                selected.setDate(datePicker.getValue());
                selected.setSales(Double.parseDouble(salesField.getText()));
                selected.setLabor(Double.parseDouble(laborField.getText()));
                selected.setNotes(notesArea.getText());

                tableView.refresh(); // Updates the table view
                saveEntriesToCSV(dataFile, entryList); // Save updated list

                messageLabel.setText("✅ Entry updated!");

            } catch (Exception ex) {
                messageLabel.setText("❌ Invalid input. Please check your numbers.");
            }
        });

        deleteButton.setStyle("-fx-background-color: #B22222; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> {
            Entry selected = tableView.getSelectionModel().getSelectedItem();
            if (!hasPermission("delete")) {
                messageLabel.setText("❌ You don't have permission to delete entries.");
                return;
            }
            if (selected == null) {
                messageLabel.setText("❌ No entry selected.");
                return;
            }

            // 🧠 Show confirmation dialog
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("Are you sure you want to delete this entry?");
            confirm.setContentText("Date: " + selected.getDate() + "\nSales: $" + selected.getSales() + "\nLabor: $" + selected.getLabor());

            // 🗑️ If they click OK, delete it
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    entryList.remove(selected);
                    tableView.refresh();
                    saveEntriesToCSV(dataFile, entryList);

                    datePicker.setValue(LocalDate.now());
                    salesField.clear();
                    laborField.clear();
                    notesArea.clear();

                    messageLabel.setText("🗑️ Entry deleted.");
                }
            });
        });


        backButton.setPrefWidth(140);

        HBox backRow = new HBox(backButton);
        backRow.setAlignment(Pos.TOP_LEFT);
        backRow.setPadding(new Insets(10, 0, 0, 10)); // top and left spacing

        Label tipsLabel = new Label();
        tipsLabel.setWrapText(true);
        tipsLabel.setStyle("-fx-text-fill: #555;");

        backButton.setOnAction(e -> {
            isDarkMode = loadDarkModePreference(); // optional if needed
            showDashboard(primaryStage);
        });
        Button toggleDarkModeBtn = new Button("🌙 Toggle Dark Mode");
        toggleDarkModeBtn.setStyle("-fx-background-color: #444; -fx-text-fill: white;");
        toggleDarkModeBtn.setOnAction(e -> {
            isDarkMode = !isDarkMode;
            saveDarkModePreference(isDarkMode);

            String darkStyle = "-fx-background-color: #2b2b2b; -fx-text-fill: #e0e0e0;";
            String lightStyle = "-fx-background-color: #f0f0f0; -fx-text-fill: black;";

            for (javafx.scene.Node node : root.getChildren()) {
                if (node instanceof Label || node instanceof TextArea || node instanceof TextField) {
                    node.setStyle(isDarkMode ? darkStyle : lightStyle);
                } else if (node instanceof Button button) {
                    // Preserve buttons with color styles
                    if (button == toggleDarkModeBtn || button.getStyle().contains("#f0f0f0") || button.getStyle().isEmpty()) {
                        button.setStyle(isDarkMode ? darkStyle : lightStyle);
                    }
                }
            }

            root.setStyle("-fx-padding: 20; -fx-spacing: 10; -fx-alignment: center;" +
                    (isDarkMode ? "-fx-background-color: #1e1e1e;" : "-fx-background-color: #f9f9f9;"));
        });


        Button summaryButton = new Button("Show Weekly Summary");
        summaryButton.setStyle("-fx-background-color: #6A5ACD; -fx-text-fill: white;");
        summaryButton.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            LocalDate oneWeekAgo = today.minusDays(6); // 7 days including today

            double totalSales = 0;
            double totalLabor = 0;
            double totalProfit = 0;

            for (Entry entry : entryList) {
                if (!entry.getDate().isBefore(oneWeekAgo) && !entry.getDate().isAfter(today)) {
                    totalSales += entry.getSales();
                    totalLabor += entry.getLabor();
                    totalProfit += entry.getProfit();
                }
            }

            Alert summary = new Alert(Alert.AlertType.INFORMATION);
            summary.setTitle("Weekly Summary");
            summary.setHeaderText("Summary for the last 7 days");
            summary.setContentText(
                    "📅 From: " + oneWeekAgo + " to " + today + "\n\n" +
                            "💵 Total Sales: $" + String.format("%.2f", totalSales) + "\n" +
                            "🛠️ Total Labor: $" + String.format("%.2f", totalLabor) + "\n" +
                            "📈 Total Profit: $" + String.format("%.2f", totalProfit)
            );
            summary.showAndWait();
        });

        Button monthlySummaryButton = new Button("Show Monthly Summary");
        monthlySummaryButton.setStyle("-fx-background-color: #20B2AA; -fx-text-fill: white;");
        monthlySummaryButton.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            LocalDate startOfMonth = today.withDayOfMonth(1); // First day of current month

            double totalSales = 0;
            double totalLabor = 0;
            double totalProfit = 0;

            for (Entry entry : entryList) {
                if (!entry.getDate().isBefore(startOfMonth) && !entry.getDate().isAfter(today)) {
                    totalSales += entry.getSales();
                    totalLabor += entry.getLabor();
                    totalProfit += entry.getProfit();
                }
            }

            Alert summary = new Alert(Alert.AlertType.INFORMATION);
            summary.setTitle("Monthly Summary");
            summary.setHeaderText("Summary for " + startOfMonth.getMonth().toString() + " " + startOfMonth.getYear());
            summary.setContentText(
                    "📅 From: " + startOfMonth + " to " + today + "\n\n" +
                            "💵 Total Sales: $" + String.format("%.2f", totalSales) + "\n" +
                            "🛠️ Total Labor: $" + String.format("%.2f", totalLabor) + "\n" +
                            "📈 Total Profit: $" + String.format("%.2f", totalProfit)
            );
            summary.showAndWait();
        });

        DatePicker customStartDate = new DatePicker();
        customStartDate.setPromptText("Start Date");

        DatePicker customEndDate = new DatePicker();
        customEndDate.setPromptText("End Date");
        Button customSummaryButton = new Button("Show Custom Summary");
        customSummaryButton.setStyle("-fx-background-color: #FF8C00; -fx-text-fill: white;");
        customSummaryButton.setOnAction(e -> {
            LocalDate start = customStartDate.getValue();
            LocalDate end = customEndDate.getValue();

            if (start == null || end == null) {
                messageLabel.setText("❌ Please select both start and end dates.");
                return;
            }

            if (end.isBefore(start)) {
                messageLabel.setText("❌ End date must be after start date.");
                return;
            }

            double totalSales = 0;
            double totalLabor = 0;
            double totalProfit = 0;

            for (Entry entry : entryList) {
                if (!entry.getDate().isBefore(start) && !entry.getDate().isAfter(end)) {
                    totalSales += entry.getSales();
                    totalLabor += entry.getLabor();
                    totalProfit += entry.getProfit();
                }
            }

            Alert summary = new Alert(Alert.AlertType.INFORMATION);
            summary.setTitle("Custom Summary");
            summary.setHeaderText("Summary from " + start + " to " + end);
            summary.setContentText(
                    "💵 Total Sales: $" + String.format("%.2f", totalSales) + "\n" +
                            "🛠️ Total Labor: $" + String.format("%.2f", totalLabor) + "\n" +
                            "📈 Total Profit: $" + String.format("%.2f", totalProfit)
            );
            summary.showAndWait();
        });


        exportSummaryBtn.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Summary Report");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            fileChooser.setInitialFileName("summary_report.txt");
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                exportSummaryToFile(file);
                messageLabel.setText("✅ Summary exported to: " + file.getName());
            }

            if (file == null) return;

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                LocalDate startDate = entryList.stream().map(Entry::getDate).min(LocalDate::compareTo).orElse(LocalDate.now());
                LocalDate endDate = entryList.stream().map(Entry::getDate).max(LocalDate::compareTo).orElse(LocalDate.now());

                double totalSales = entryList.stream().mapToDouble(Entry::getSales).sum();
                double totalLabor = entryList.stream().mapToDouble(Entry::getLabor).sum();
                double totalProfit = totalSales - totalLabor;

                Entry best = entryList.stream().max((a, b) -> Double.compare(a.getProfit(), b.getProfit())).orElse(null);
                Entry worst = entryList.stream().min((a, b) -> Double.compare(a.getProfit(), b.getProfit())).orElse(null);

                writer.write("📊 Arby’s Sales Summary\n");
                writer.write("Date Range: " + startDate + " – " + endDate + "\n\n");
                writer.write("Total Sales: $" + String.format("%.2f", totalSales) + "\n");
                writer.write("Total Labor: $" + String.format("%.2f", totalLabor) + "\n");
                writer.write("Total Profit: $" + String.format("%.2f", totalProfit) + "\n\n");

                if (best != null)
                    writer.write("💰 Best Day: " + best.getDate() + " — $" + String.format("%.2f", best.getProfit()) + "\n");

                if (worst != null)
                    writer.write("📉 Worst Day: " + worst.getDate() + " — $" + String.format("%.2f", worst.getProfit()) + "\n");

                writer.write("\nNotes:\n");
                for (Entry entry : entryList) {
                    if (entry.getNotes() != null && !entry.getNotes().isBlank()) {
                        writer.write("- " + entry.getDate() + ": " + entry.getNotes() + "\n");
                    }
                }

                messageLabel.setText("✅ Summary exported to " + file.getName());

            } catch (IOException ex) {
                ex.printStackTrace();
                messageLabel.setText("❌ Failed to export summary.");
            }
        });


        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount ($)");
        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Performance Over Time");
        lineChart.setPrefHeight(300);
        XYChart.Series<String, Number> salesSeries = new XYChart.Series<>();
        salesSeries.setName("Sales");
        XYChart.Series<String, Number> laborSeries = new XYChart.Series<>();
        laborSeries.setName("Labor");
        XYChart.Series<String, Number> profitSeries = new XYChart.Series<>();
        profitSeries.setName("Profit");

        Runnable updateChart = () -> {
            salesSeries.getData().clear();
            laborSeries.getData().clear();
            profitSeries.getData().clear();
            for (Entry entry : entryList) {
                String date = entry.getDate().toString();
                salesSeries.getData().add(new XYChart.Data<>(date, entry.getSales()));
                laborSeries.getData().add(new XYChart.Data<>(date, entry.getLabor()));
                profitSeries.getData().add(new XYChart.Data<>(date, entry.getProfit()));
            }
        };

        Label bestDayLabel = new Label("💰 Best Day: (will update automatically)");
        bestDayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #228B22;");

        Label worstDayLabel = new Label("📉 Worst Day: (will update automatically)");
        worstDayLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #B22222;");

        Runnable updateBestDay = () -> {
            if (entryList.isEmpty()) {
                bestDayLabel.setText("💰 Best Day: No data yet.");
                return;
            }

            Entry best = entryList.get(0);
            for (Entry entry : entryList) {
                if (entry.getProfit() > best.getProfit()) {
                    best = entry;
                }
            }

            bestDayLabel.setText("💰 Best Day: " + best.getDate() + " | Profit: $" + String.format("%.2f", best.getProfit()));
        };

        Runnable updateWorstDay = () -> {
            if (entryList.isEmpty()) {
                worstDayLabel.setText("📉 Worst Day: No data yet.");
                return;
            }

            Entry worst = entryList.get(0);
            for (Entry entry : entryList) {
                if (entry.getProfit() < worst.getProfit()) {
                    worst = entry;
                }
            }

            worstDayLabel.setText("📉 Worst Day: " + worst.getDate() + " | Profit: $" + String.format("%.2f", worst.getProfit()));
        };

        lineChart.getData().addAll(salesSeries, laborSeries, profitSeries);


        TableColumn<Entry, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<Entry, Double> salesCol = new TableColumn<>("Sales");
        salesCol.setCellValueFactory(new PropertyValueFactory<>("sales"));
        TableColumn<Entry, Double> laborCol = new TableColumn<>("Labor");
        laborCol.setCellValueFactory(new PropertyValueFactory<>("labor"));
        TableColumn<Entry, Double> profitCol = new TableColumn<>("Profit");
        profitCol.setCellValueFactory(new PropertyValueFactory<>("profit"));
        TableColumn<Entry, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        tableView.getColumns().addAll(dateCol, salesCol, laborCol, profitCol, notesCol);
        tableView.setPrefHeight(200);

        tableView.setOnMouseClicked(event -> {
            Entry selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                datePicker.setValue(selected.getDate());
                salesField.setText(String.valueOf(selected.getSales()));
                laborField.setText(String.valueOf(selected.getLabor()));
                notesArea.setText(selected.getNotes());
            }
        });

        tableView.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Entry item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setStyle("");
                    setTooltip(null);
                } else {
                    // Add tooltip if notes exist
                    if (item.getNotes() != null && !item.getNotes().isBlank()) {
                        Tooltip noteTooltip = new Tooltip(item.getNotes());
                        noteTooltip.setWrapText(true);
                        noteTooltip.setMaxWidth(250);
                        noteTooltip.setStyle("-fx-font-size: 13px;");
                        setTooltip(noteTooltip);
                    } else {
                        setTooltip(null);
                    }

                    // Optional: Highlight best and worst days (already handled elsewhere too)
                    double maxProfit = entryList.stream().mapToDouble(Entry::getProfit).max().orElse(0);
                    double minProfit = entryList.stream().mapToDouble(Entry::getProfit).min().orElse(0);

                    if (item.getProfit() == maxProfit) {
                        setStyle("-fx-background-color: #d4fcdc;");
                    } else if (item.getProfit() == minProfit) {
                        setStyle("-fx-background-color: #ffe4e1;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        saveButton.setOnAction(e -> {
            LocalDate date = datePicker.getValue();
            String salesText = salesField.getText();
            String laborText = laborField.getText();
            if (!hasPermission("add")) {
                messageLabel.setText("❌ You don’t have permission to add entries.");
                return;
            }
            if (salesText.isEmpty() || laborText.isEmpty()) {
                messageLabel.setText("Please fill out all fields.");
                return;
            }

            try {
                double sales = Double.parseDouble(salesText);
                double labor = Double.parseDouble(laborText);
                double profit = sales - labor;

                Entry newEntry = new Entry(date, sales, labor, notesArea.getText());
                notesArea.clear();
                entryList.add(newEntry);
                saveEntriesToCSV(dataFile, entryList);
                updateChart.run();
                updateBestDay.run();
                updateWorstDay.run();


                messageLabel.setText("Saved: " + date + " | Profit: $" + profit);
                salesField.clear();
                laborField.clear();
                datePicker.setValue(LocalDate.now());

                StringBuilder tip = new StringBuilder();
                double laborPercent = (sales > 0) ? (labor / sales) * 100 : 0;

                if (laborPercent > 40) {
                    tip.append("⚠️ Labor cost is ").append(String.format("%.1f", laborPercent)).append("% of sales — try reducing staff. ");
                } else if (laborPercent > 30) {
                    tip.append("✅ Labor cost is reasonable at ").append(String.format("%.1f", laborPercent)).append("%. ");
                } else {
                    tip.append("💡 Great job! Labor cost is low today. ");
                }

                if (entryList.size() > 1) {
                    Entry prev = entryList.get(entryList.size() - 2);
                    if (sales < prev.getSales()) {
                        tip.append("⬇️ Sales dropped from $").append(prev.getSales()).append(" to $").append(sales).append(". ");
                    } else if (sales > prev.getSales()) {
                        tip.append("⬆️ Sales increased from $").append(prev.getSales()).append(" to $").append(sales).append("! ");
                    }

                    if (profit > prev.getProfit()) {
                        tip.append("📈 Profit improved compared to yesterday!");
                    } else if (profit < prev.getProfit()) {
                        tip.append("📉 Profit is down from yesterday — watch costs closely.");
                    }
                }

                tipsLabel.setText(tip.toString());

            } catch (NumberFormatException ex) {
                messageLabel.setText("Please enter valid numbers for sales and labor.");
            }
        });

        exportButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export CSV");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("arbys_export.csv");

            File selectedFile = fileChooser.showSaveDialog(primaryStage);
            if (selectedFile != null) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                    for (Entry entry : entryList) {
                        writer.write(entry.getDate() + "," + entry.getSales() + "," + entry.getLabor() + "," + entry.getProfit());
                        writer.newLine();
                    }
                    messageLabel.setText("✅ Exported to: " + selectedFile.getName());
                } catch (IOException ex) {
                    messageLabel.setText("❌ Failed to export file.");
                    ex.printStackTrace();
                }
            }
        });

        updateChart.run();
        lineChart.layout(); // 🔁 Force the chart to re-layout its visuals

        HBox updateRow = new HBox(10, updateButton, deleteButton);
        updateRow.setAlignment(Pos.CENTER);

        HBox summaryRow = new HBox(10, summaryButton, monthlySummaryButton);
        summaryRow.setAlignment(Pos.CENTER);

        HBox customRangeRow = new HBox(10, customStartDate, customEndDate, customSummaryButton);
        customRangeRow.setAlignment(Pos.CENTER);

        HBox typeOfDayRow = new HBox(10, bestDayLabel, worstDayLabel);
        typeOfDayRow.setAlignment(Pos.CENTER);

        HBox exportRow = new HBox(10, exportButton, exportSummaryBtn); //Btn for PDF, Button for CSV
        exportRow.setAlignment(Pos.CENTER);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(10, backButton, spacer, toggleDarkModeBtn);
        topRow.setPadding(new Insets(10, 10, 0, 10));
        topRow.setAlignment(Pos.TOP_CENTER);

        if (darkMode) {
            root.setStyle("-fx-padding: 20; -fx-spacing: 10; -fx-background-color: #1e1e1e; -fx-alignment: center;");
        } else {
            root.setStyle("-fx-padding: 20; -fx-spacing: 10; -fx-background-color: #f9f9f9; -fx-alignment: center;");
        }
        root.getChildren().addAll(
                topRow,
                titleLabel,
                datePicker,
                salesField,
                laborField,
                notesArea,
                saveButton,
                messageLabel,
                tipsLabel,
                tableView,
                updateRow,
                lineChart,
                typeOfDayRow,
                summaryRow,
                new Label("Custom Range:"),
                customRangeRow,
                exportRow
        );


        Scene scene = new Scene(root, 650, 750);
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER -> saveButton.fire();
            }
        });
        primaryStage.setTitle("Arby’s Store Manager Simulator");
        primaryStage.setScene(scene);
        if (isFullscreen) {
            ensureMaximized(primaryStage);
        } else {
            primaryStage.setMaximized(false);
        }
        primaryStage.show();
    }

    private void ensureMaximized(Stage stage) {
        if (!stage.isMaximized()) {
            stage.setMaximized(true);
        }
    }

    private void saveEntriesToCSV(File file, ObservableList<Entry> entries) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Entry entry : entries) {
                writer.write(entry.getDate() + "," + entry.getSales() + "," + entry.getLabor() + "," + entry.getProfit() + "," + entry.getNotes());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportSummaryToFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("📊 Arby's Store Summary Report\n");
            writer.write("===============================\n\n");

            double totalSales = 0, totalLabor = 0;
            for (Entry entry : entryList) {
                writer.write(entry.getDate() + " - Sales: $" + entry.getSales() + ", Labor: $" + entry.getLabor() + ", Profit: $" + entry.getProfit() + "\n");
                totalSales += entry.getSales();
                totalLabor += entry.getLabor();
            }

            double totalProfit = totalSales - totalLabor;

            writer.write("\n");
            writer.write("Total Sales: $" + totalSales + "\n");
            writer.write("Total Labor: $" + totalLabor + "\n");
            writer.write("Total Profit: $" + totalProfit + "\n");

            writer.write("\nTips:\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Label tipsLabel = new Label(); // declared at class level

    private void loadEntriesFromCSV(File file, ObservableList<Entry> entries) {
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, ",");
                if (tokenizer.countTokens() >= 5) {
                    LocalDate date = LocalDate.parse(tokenizer.nextToken());
                    double sales = Double.parseDouble(tokenizer.nextToken());
                    double labor = Double.parseDouble(tokenizer.nextToken());
                    tokenizer.nextToken(); // skip profit (recalculated)
                    String notes = tokenizer.nextToken();
                    entries.add(new Entry(date, sales, labor, notes));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}
