package com.tristan;

import views.LoginView;
import views.DashboardView;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import views.SalesEntryView;



public class Main extends Application {

    private String currentUserRole = "manager"; // default
    private Stage mainStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        isDarkMode = loadDarkModePreference();
        isFullscreen = loadFullscreenPreference();

        String rememberedUsername = loadRememberedUsername();
        boolean shouldRemember = loadRememberPreference();

        if (shouldRemember && !rememberedUsername.isEmpty()) {
            currentUsername = rememberedUsername;
            currentUserRole = inferUserRole(rememberedUsername);
            showDashboard(primaryStage);
        } else {
            showLogin(primaryStage);
        }
    }

    private String inferUserRole(String username) {
        return switch (username) {
            case "manager" -> "Manager";
            case "team_lead" -> "Team Lead";
            case "trainer" -> "Trainer";
            default -> "User";
        };
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

    private void saveRememberedUsername(String username) {
        File file = new File("remembered_user.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(username);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ObservableList<Entry> entryList = FXCollections.observableArrayList();

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

    private boolean loadRememberPreference() {
        File file = new File("preferences.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("rememberMe=true")) return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void saveRememberPreference(boolean value) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("preferences.txt"))) {
            writer.write("rememberMe=" + value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showLogin(Stage stage) {
        LoginView loginView = new LoginView(
                mainStage,
                (username, role) -> {currentUsername = username; currentUserRole = role; showDashboard(mainStage);},
                rememberMe -> {
                    saveRememberPreference(rememberMe);
                    if (rememberMe) {
                        saveRememberedUsername(currentUsername);
                    } else {
                        saveRememberedUsername("");
                    }
                }
        );
        loginView.show();
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

    private void applyTheme(Scene scene) {
        if (isDarkMode) {
            scene.getRoot().setStyle("-fx-base: #2b2b2b; -fx-text-fill: white;");
        } else {
            scene.getRoot().setStyle("");
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

    private String loadRememberedUsername() {
        File file = new File("remembered_user.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                return reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
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
        DashboardView dashboardView = new DashboardView(
                mainStage,
                currentUserRole,
                currentUsername,
                v -> showMainApp(mainStage),
                () -> {
                    LoginView loginView = new LoginView(mainStage,
                            (username, role) -> {
                                currentUsername = username;
                                currentUserRole = role;
                                showDashboard(mainStage);
                            },
                            rememberMe -> {
                                saveRememberPreference(rememberMe);
                                if (rememberMe) {
                                    saveRememberedUsername(currentUsername);
                                } else {
                                    saveRememberedUsername(""); // clear
                                }
                            }
                    );

                    loginView.show();
                },
                () -> {
                    saveRememberPreference(false);
                    LoginView loginView = new LoginView(
                            mainStage,
                            (username, role) -> {currentUsername = username;currentUserRole = role;showDashboard(mainStage);},
                            rememberMe -> {
                                saveRememberPreference(rememberMe);
                                if (rememberMe) {
                                    saveRememberedUsername(currentUsername);
                                } else {
                                    saveRememberedUsername("");
                                }
                            }
                    );
                    loginView.show();
                },
                isDarkMode,
                isFullscreen,
                () -> {
                    saveDarkModePreference(isDarkMode);
                    saveFullscreenPreference(isFullscreen);
                    applyTheme(mainStage.getScene());
                    ensureMaximized(mainStage, isFullscreen);
                }
        );

        dashboardView.show();
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

    private boolean hasPermission(String action) {
        switch (currentUserRole) {
            case "Manager":
                return true;
            case "Team Lead":
                return !action.equals("Delete");
            case "Trainer":
                return action.equals("Add") || action.equals("Update");
            default:
                return false;
        }
    }

    private void showMainApp(Stage stage) {
        SalesEntryView salesEntryView = new SalesEntryView(
                stage,
                entryList,
                () -> showDashboard(stage),
                isDarkMode,
                isFullscreen,
                currentUserRole,
                () -> {
                    saveDarkModePreference(isDarkMode);
                    saveFullscreenPreference(isFullscreen);
                    applyTheme(stage.getScene());
                    ensureMaximized(mainStage, isFullscreen);
                }
        );

        salesEntryView.show();
    }

    private void applyTheme(Scene scene, boolean isDarkMode) {
        if (isDarkMode) {
            scene.getRoot().setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");
        } else {
            scene.getRoot().setStyle("");
        }
    }

    private void ensureMaximized(Stage stage, boolean isFullscreen) {
        stage.setFullScreen(false); // You are not using JavaFX fullScreen
        stage.setMaximized(isFullscreen);
    }


    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return "";
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}
