package views;

import com.tristan.Entry;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import com.tristan.Entry;
import java.util.function.Consumer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;


public class SalesEntryView {
    private final Stage stage;
    private final ObservableList<Entry> entryList;
    private final Runnable onBackToDashboard;
    private boolean isDarkMode;
    private boolean isFullscreen;
    private final File dataFile = new File("sales_data.csv");
    private final String currentUserRole;
    private final Runnable onPreferencesChanged;

    private void ensureMaximized(Stage stage, boolean isFullscreen) {
        stage.setFullScreen(false); // You are not using JavaFX fullScreen
        stage.setMaximized(isFullscreen);
    }

    public SalesEntryView(Stage stage,
                          ObservableList<Entry> entryList,
                          Runnable onBackToDashboard,
                          boolean isDarkMode,
                          boolean isFullscreen,
                          String currentUserRole,
                          Runnable onPreferencesChanged) {
        this.stage = stage;
        this.entryList = entryList;
        this.onBackToDashboard = onBackToDashboard;
        this.isDarkMode = isDarkMode;
        this.isFullscreen = isFullscreen;
        this.currentUserRole = currentUserRole;
        this.onPreferencesChanged = onPreferencesChanged;
    }

    private void saveEntriesToCSV(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Entry entry : entryList) {
                writer.write(entry.getDate() + "," +
                        entry.getSales() + "," +
                        entry.getLabor() + "," +
                        entry.getProfit() + "," +
                        entry.getNotes().replace(",", ";") + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void loadEntriesFromCSV(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            entryList.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 5); // limit to 5 parts to preserve commas in notes
                if (parts.length == 5) {
                    LocalDate date = LocalDate.parse(parts[0]);
                    double sales = Double.parseDouble(parts[1]);
                    double labor = Double.parseDouble(parts[2]);
                    double profit = Double.parseDouble(parts[3]);
                    String notes = parts[4];
                    Entry entry = new Entry(date, sales, labor, profit, notes); // ✅ correct
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean loadDarkModePreference() {
        File file = new File("preferences.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("darkMode=true")) return true;
                if (line.equals("darkMode=false")) return false;
            }
        } catch (IOException ignored) {}
        return false; // fallback default
    }

    private void saveDarkModePreference(boolean value) {
        File file = new File("preferences.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("darkMode=" + value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportSummaryToFile(String summary) {
        File file = new File("summary.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(summary);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void show() {
        Stage primaryStage = stage; // reuse the field already provided
        boolean darkMode = isDarkMode; // for local convenience
        loadEntriesFromCSV(dataFile);
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
                saveEntriesToCSV(dataFile); // Save updated list

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
                    saveEntriesToCSV(dataFile);

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
            onBackToDashboard.run(); // ✅
        });
        /**Button toggleDarkModeBtn = new Button("🌙 Toggle Dark Mode");
        toggleDarkModeBtn.setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");
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
                    "-fx-background-color: #2b2b2b;");
        });**/


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
                Entry newEntry = new Entry(date, sales, labor, profit, notesArea.getText());
                notesArea.clear();
                entryList.add(newEntry);
                saveEntriesToCSV(dataFile);
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

        HBox topRow = new HBox(10, backButton, spacer); // re-add toggleDarkModeBtn if you want the dark mode button
        topRow.setPadding(new Insets(10, 10, 0, 10));
        topRow.setAlignment(Pos.TOP_CENTER);

        if (darkMode) {
            root.setStyle("-fx-padding: 20; -fx-spacing: 10; -fx-background-color: #1e1e1e; -fx-alignment: center;");
        } else {
            root.setStyle("-fx-padding: 20; -fx-spacing: 10; -fx-alignment: center;"); // No forced color
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
            ensureMaximized(primaryStage, isFullscreen);
        } else {
            primaryStage.setMaximized(false);
        }
        if (isDarkMode) {
            scene.getRoot().setStyle("-fx-background-color: #2b2b2b; -fx-text-fill: white;");
        } else {
            scene.getRoot().setStyle(""); // default light theme
        }
        primaryStage.show();
        onPreferencesChanged.run(); // This ensures current theme/fullscreen is applied on show
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

}

