package gui;

import calories.*;
import model.*;
import parser.TcxParser;
import stats.ActivityStats;
import vo2max.VO2MaxEstimator;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Κεντρική κλάση της JavaFX εφαρμογής "Βοηθός Προπόνησης".
 *
 * <p>Η εφαρμογή διαρθρώνεται σε 4 tabs:</p>
 * <ol>
 *   <li><b>Δραστηριότητες</b> – Φόρτωση αρχείων TCX και εμφάνιση στατιστικών</li>
 *   <li><b>Προσθήκη</b> – Χειροκίνητη προσθήκη νέας δραστηριότητας</li>
 *   <li><b>Προφίλ</b> – Εισαγωγή προσωπικών δεδομένων και ημερήσιου στόχου</li>
 *   <li><b>VO2 Max</b> – Εκτίμηση αερόβιας ικανότητας (Προαιρετικό 2)</li>
 * </ol>
 */
public class FitnessApp extends Application {

    // --- Κεντρική κατάσταση εφαρμογής ---
    private final List<Activity> allActivities = new ArrayList<>();
    private final UserProfile userProfile = new UserProfile();
    private CalorieCalculator currentCalculator = new SimpleCalorieCalculator();

    // --- UI Components ---
    private TextArea statsArea;
    private Label statusLabel;
    private Label vo2ResultLabel;
    private Label vo2RatingLabel;
    private Label dailyGoalLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Βοηθός Προπόνησης – Χαροκόπειο Πανεπιστήμιο");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().addAll(
            buildActivitiesTab(),
            buildAddActivityTab(),
            buildProfileTab(),
            buildVO2MaxTab()
        );

        statusLabel = new Label("Έτοιμο – Φορτώστε αρχεία TCX για να ξεκινήσετε.");
        statusLabel.setPadding(new Insets(4, 8, 4, 8));

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);
        root.setBottom(statusLabel);

        primaryStage.setScene(new Scene(root, 900, 650));
        primaryStage.show();
    }

    // =========================================================================
    // Tab 1: Δραστηριότητες
    // =========================================================================

    /**
     * Δημιουργεί το tab φόρτωσης αρχείων και εμφάνισης στατιστικών.
     */
    private Tab buildActivitiesTab() {
        Tab tab = new Tab("📊 Δραστηριότητες");

        // Επιλογή μεθόδου θερμίδων
        Label calcLabel = new Label("Μέθοδος θερμίδων:");
        ComboBox<String> calcCombo = new ComboBox<>();
        calcCombo.getItems().addAll(
            "Απλός (MET)",
            "Καρδιακοί παλμοί",
            "VO2 Max"
        );
        calcCombo.getSelectionModel().selectFirst();
        calcCombo.setOnAction(e -> {
            int idx = calcCombo.getSelectionModel().getSelectedIndex();
            currentCalculator = switch (idx) {
                case 1  -> new HRCalorieCalculator();
                case 2  -> new VO2MaxCalorieCalculator();
                default -> new SimpleCalorieCalculator();
            };
            refreshStats();
        });

        // Κουμπιά φόρτωσης
        Button loadBtn = new Button("📂 Φόρτωση TCX...");
        loadBtn.setOnAction(e -> loadTcxFiles(loadBtn.getScene().getWindow()));

        Button clearBtn = new Button("🗑 Εκκαθάριση");
        clearBtn.setOnAction(e -> {
            allActivities.clear();
            refreshStats();
        });

        HBox toolbar = new HBox(10, loadBtn, clearBtn, calcLabel, calcCombo);
        toolbar.setPadding(new Insets(8));
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // Περιοχή εμφάνισης στατιστικών
        statsArea = new TextArea();
        statsArea.setEditable(false);
        statsArea.setFont(javafx.scene.text.Font.font("Monospaced", 13));
        statsArea.setPromptText("Φορτώστε αρχεία TCX για να δείτε τα στατιστικά εδώ...");

        // Ημερήσιος στόχος θερμίδων
        dailyGoalLabel = new Label("(Ορίστε ημερήσιο στόχο θερμίδων στο tab Προφίλ)");
        dailyGoalLabel.setPadding(new Insets(4, 8, 4, 8));
        dailyGoalLabel.setStyle("-fx-text-fill: #555;");

        VBox content = new VBox(toolbar, new Separator(), statsArea, dailyGoalLabel);
        VBox.setVgrow(statsArea, Priority.ALWAYS);
        content.setPadding(new Insets(4));
        tab.setContent(content);

        return tab;
    }

    /**
     * Ανοίγει FileChooser για επιλογή αρχείων TCX και τα φορτώνει.
     *
     * @param owner το παράθυρο που θα ανήκει ο dialog
     */
    private void loadTcxFiles(javafx.stage.Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Επιλογή αρχείων TCX");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Αρχεία TCX", "*.tcx")
        );

        List<File> files = chooser.showOpenMultipleDialog(owner);
        if (files == null || files.isEmpty()) return;

        TcxParser parser = new TcxParser();
        int loaded = 0;
        for (File file : files) {
            try {
                List<Activity> activities = parser.parse(file);
                allActivities.addAll(activities);
                loaded += activities.size();
            } catch (Exception e) {
                showAlert("Σφάλμα", "Αδυναμία ανάγνωσης: " + file.getName() + "\n" + e.getMessage());
            }
        }

        setStatus("Φορτώθηκαν " + loaded + " δραστηριότητες από " + files.size() + " αρχεία.");
        refreshStats();
    }

    /**
     * Ανανεώνει την περιοχή στατιστικών με τις τρέχουσες δραστηριότητες.
     */
    private void refreshStats() {
        if (allActivities.isEmpty()) {
            statsArea.setText("Δεν υπάρχουν φορτωμένες δραστηριότητες.");
            updateDailyGoalLabel();
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(50)).append("\n");
        sb.append(" ΣΤΑΤΙΣΤΙΚΑ ΔΡΑΣΤΗΡΙΟΤΗΤΩΝ\n");
        sb.append("=".repeat(50)).append("\n\n");

        double totalCalories = 0;
        for (Activity activity : allActivities) {
            ActivityStats stats = new ActivityStats(activity, userProfile, currentCalculator);
            sb.append(stats.format());
            sb.append("-".repeat(40)).append("\n");
            double cal = stats.getCalories();
            if (cal >= 0) totalCalories += cal;
        }

        // Σύνολα αν υπάρχουν πολλές δραστηριότητες
        if (allActivities.size() > 1) {
            sb.append("\n").append("=".repeat(50)).append("\n");
            sb.append("ΣΥΝΟΛΑ\n");
            sb.append("=".repeat(50)).append("\n");
            long totalSec = allActivities.stream().mapToLong(Activity::getTotalDurationSeconds).sum();
            double totalKm = allActivities.stream().mapToDouble(Activity::getTotalDistanceMeters).sum() / 1000.0;
            sb.append("Συνολικός χρόνος: ").append(ActivityStats.formatDuration(totalSec)).append("\n");
            sb.append(String.format("Συνολική απόσταση: %.2f km%n", totalKm));
            if (totalCalories > 0) {
                sb.append(String.format("Συνολικές θερμίδες: %.0f kcal%n", totalCalories));
            }
        }

        statsArea.setText(sb.toString());
        updateDailyGoalLabel();
    }

    /**
     * Ενημερώνει το label ημερήσιου στόχου θερμίδων.
     * Ομαδοποιεί τις δραστηριότητες ανά ημέρα και συγκρίνει με τον στόχο.
     */
    private void updateDailyGoalLabel() {
        double goal = userProfile.getDailyCalorieGoal();
        if (goal <= 0 || allActivities.isEmpty()) {
            dailyGoalLabel.setText("(Ορίστε ημερήσιο στόχο θερμίδων στο tab Προφίλ)");
            dailyGoalLabel.setStyle("-fx-text-fill: #555;");
            return;
        }

        // Ομαδοποίηση θερμίδων ανά ημέρα
        Map<LocalDate, Double> dailyCals = new TreeMap<>();
        for (Activity a : allActivities) {
            ZonedDateTime start = a.getStartTime();
            if (start == null) continue;
            LocalDate day = start.toLocalDate();
            double cal = currentCalculator.calculate(a, userProfile);
            if (cal >= 0) {
                dailyCals.merge(day, cal, Double::sum);
            }
        }

        if (dailyCals.isEmpty()) {
            dailyGoalLabel.setText("Δεν υπάρχουν θερμίδες για ανάλυση στόχου.");
            return;
        }

        StringBuilder sb = new StringBuilder("Ημερήσιος Στόχος: " + (int) goal + " kcal  |  ");
        for (Map.Entry<LocalDate, Double> entry : dailyCals.entrySet()) {
            double cal = entry.getValue();
            String status = cal >= goal ? "✅" : "❌";
            double remaining = goal - cal;
            sb.append(entry.getKey()).append(": ").append((int) cal).append(" kcal ")
              .append(status);
            if (remaining > 0) sb.append(" (απομένουν ").append((int) remaining).append(" kcal)");
            sb.append("  |  ");
        }
        dailyGoalLabel.setText(sb.toString());
        dailyGoalLabel.setStyle("-fx-text-fill: #1a1a1a; -fx-font-weight: bold;");
    }

    // =========================================================================
    // Tab 2: Προσθήκη Δραστηριότητας
    // =========================================================================

    /**
     * Δημιουργεί το tab χειροκίνητης εισαγωγής νέας δραστηριότητας.
     */
    private Tab buildAddActivityTab() {
        Tab tab = new Tab("➕ Προσθήκη");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        // Τύπος δραστηριότητας
        Label typeLabel = new Label("Τύπος:");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Running", "Biking", "Walking", "Swimming");
        typeCombo.getSelectionModel().selectFirst();

        // Διάρκεια
        Label durLabel = new Label("Διάρκεια (λεπτά):");
        TextField durField = new TextField("30");

        // Απόσταση
        Label distLabel = new Label("Απόσταση (km):");
        TextField distField = new TextField("5.0");

        // Μέσοι παλμοί
        Label hrLabel = new Label("Μέσοι παλμοί (bpm):");
        TextField hrField = new TextField("0");

        // Κουμπί προσθήκης
        Button addBtn = new Button("➕ Προσθήκη δραστηριότητας");
        addBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 13px;");
        addBtn.setOnAction(e -> {
            try {
                String sport    = typeCombo.getValue();
                double durMin   = Double.parseDouble(durField.getText());
                double distKm   = Double.parseDouble(distField.getText());
                int    avgHr    = Integer.parseInt(hrField.getText());

                Activity activity = createManualActivity(sport, durMin, distKm, avgHr);
                allActivities.add(activity);
                setStatus("Προστέθηκε: " + sport + " – " + (int) durMin + " λεπτά");
                refreshStats();
            } catch (NumberFormatException ex) {
                showAlert("Σφάλμα", "Ελέγξτε τις αριθμητικές τιμές που εισάγατε.");
            }
        });

        grid.add(typeLabel, 0, 0);  grid.add(typeCombo,  1, 0);
        grid.add(durLabel,  0, 1);  grid.add(durField,   1, 1);
        grid.add(distLabel, 0, 2);  grid.add(distField,  1, 2);
        grid.add(hrLabel,   0, 3);  grid.add(hrField,    1, 3);
        grid.add(addBtn,    0, 4, 2, 1);

        tab.setContent(grid);
        return tab;
    }

    /**
     * Δημιουργεί χειροκίνητα μια δραστηριότητα από τα δεδομένα του χρήστη.
     * Κατασκευάζει ένα συνθετικό Track με δύο trackpoints για τον υπολογισμό
     * χρόνου και απόστασης.
     *
     * @param sport   ο τύπος άθλησης
     * @param durMin  η διάρκεια σε λεπτά
     * @param distKm  η απόσταση σε km
     * @param avgHr   ο μέσος καρδιακός παλμός
     * @return το δημιουργημένο Activity
     */
    private Activity createManualActivity(String sport, double durMin, double distKm, int avgHr) {
        ZonedDateTime now = ZonedDateTime.now();
        Activity activity = switch (sport.toLowerCase()) {
            case "biking"  -> new CyclingActivity(now);
            case "walking" -> new WalkingActivity(now);
            case "swimming" -> new SwimmingActivity(now);
            default        -> new RunningActivity(now);
        };
        activity.setSport(sport);

        // Δημιουργία συνθετικού Lap → Track → 2 Trackpoints
        Lap lap = new Lap(now);
        Track track = new Track();

        Trackpoint start = new Trackpoint();
        start.setTimestamp(now);
        start.setDistanceMeters(0.0);
        if (avgHr > 0) start.setHeartRateBpm(avgHr);

        Trackpoint end = new Trackpoint();
        end.setTimestamp(now.plusSeconds((long)(durMin * 60)));
        end.setDistanceMeters(distKm * 1000.0);
        if (avgHr > 0) end.setHeartRateBpm(avgHr);

        track.addTrackpoint(start);
        track.addTrackpoint(end);
        lap.addTrack(track);
        activity.addLap(lap);

        return activity;
    }

    // =========================================================================
    // Tab 3: Προφίλ Χρήστη
    // =========================================================================

    /**
     * Δημιουργεί το tab εισαγωγής προσωπικών δεδομένων χρήστη.
     */
    private Tab buildProfileTab() {
        Tab tab = new Tab("👤 Προφίλ");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        // Φύλο
        Label genderLbl = new Label("Φύλο:");
        ToggleGroup genderGroup = new ToggleGroup();
        RadioButton maleRb  = new RadioButton("Άνδρας");
        RadioButton femaleRb = new RadioButton("Γυναίκα");
        maleRb.setToggleGroup(genderGroup);
        femaleRb.setToggleGroup(genderGroup);
        maleRb.setSelected(true);
        HBox genderBox = new HBox(15, maleRb, femaleRb);

        // Ηλικία
        Label ageLbl = new Label("Ηλικία (χρόνια):");
        TextField ageField = new TextField(String.valueOf(userProfile.getAge()));

        // Βάρος
        Label weightLbl = new Label("Βάρος (kg):");
        TextField weightField = new TextField(String.valueOf(userProfile.getWeightKg()));

        // Παλμοί ηρεμίας
        Label rhrLbl = new Label("Παλμοί ηρεμίας (bpm):");
        TextField rhrField = new TextField(String.valueOf(userProfile.getRestingHeartRate()));

        // Ημερήσιος στόχος θερμίδων
        Label goalLbl = new Label("Ημερήσιος στόχος θερμίδων (kcal, 0 = χωρίς στόχο):");
        TextField goalField = new TextField("0");

        // Κουμπί αποθήκευσης
        Button saveBtn = new Button("💾 Αποθήκευση προφίλ");
        saveBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 13px;");
        saveBtn.setOnAction(e -> {
            try {
                userProfile.setGender(femaleRb.isSelected()
                        ? UserProfile.Gender.FEMALE
                        : UserProfile.Gender.MALE);
                userProfile.setAge(Integer.parseInt(ageField.getText()));
                userProfile.setWeightKg(Double.parseDouble(weightField.getText()));
                userProfile.setRestingHeartRate(Integer.parseInt(rhrField.getText()));
                userProfile.setDailyCalorieGoal(Double.parseDouble(goalField.getText()));
                setStatus("Το προφίλ αποθηκεύτηκε επιτυχώς.");
                refreshStats();
            } catch (NumberFormatException ex) {
                showAlert("Σφάλμα", "Ελέγξτε τις αριθμητικές τιμές.");
            }
        });

        grid.add(genderLbl,  0, 0); grid.add(genderBox, 1, 0);
        grid.add(ageLbl,     0, 1); grid.add(ageField,  1, 1);
        grid.add(weightLbl,  0, 2); grid.add(weightField, 1, 2);
        grid.add(rhrLbl,     0, 3); grid.add(rhrField,  1, 3);
        grid.add(goalLbl,    0, 4); grid.add(goalField, 1, 4);
        grid.add(saveBtn,    0, 5, 2, 1);

        tab.setContent(grid);
        return tab;
    }

    // =========================================================================
    // Tab 4: VO2 Max (Προαιρετικό μέρος 2)
    // =========================================================================

    /**
     * Δημιουργεί το tab εκτίμησης VO2 Max.
     * Αποτελεί την υλοποίηση του Προαιρετικού Μέρους 2 της εργασίας.
     */
    private Tab buildVO2MaxTab() {
        Tab tab = new Tab("🫀 VO2 Max");
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));

        // Επεξήγηση
        Label infoLabel = new Label(
            "Εκτίμηση VO2 Max με τη μέθοδο Uth et al.\n" +
            "Απαιτεί: ηλικία, φύλο και παλμοί εν ηρεμία από το Προφίλ."
        );
        infoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #444;");

        // Αποτελέσματα
        vo2ResultLabel = new Label("VO2 Max: –");
        vo2ResultLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        vo2RatingLabel = new Label("Αξιολόγηση: –");
        vo2RatingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #1565C0;");

        Label formulaLabel = new Label(
            "Τύπος: VO2Max = 15.3 × (MHR / RHR)   |   MHR = 220 − ηλικία"
        );
        formulaLabel.setStyle("-fx-font-family: monospace; -fx-text-fill: #555;");

        Button calcBtn = new Button("Υπολόγισε VO2 Max");
        calcBtn.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-font-size: 13px;");
        calcBtn.setOnAction(e -> calculateVO2Max());

        // Bar Chart για θερμίδες ανά δραστηριότητα
        Label chartTitle = new Label("Εκτίμηση θερμίδων βάσει VO2 Max ανά δραστηριότητα:");
        chartTitle.setStyle("-fx-font-weight: bold;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        yAxis.setLabel("kcal");
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Θερμίδες ανά δραστηριότητα (VO2 Max)");
        barChart.setLegendVisible(false);
        barChart.setPrefHeight(250);

        // Αποθηκεύουμε αναφορά για ανανέωση από calculateVO2Max()
        vo2BarChart = barChart;

        vbox.getChildren().addAll(
            infoLabel, new Separator(),
            formulaLabel,
            calcBtn,
            vo2ResultLabel,
            vo2RatingLabel,
            new Separator(),
            chartTitle,
            barChart
        );

        ScrollPane scroll = new ScrollPane(vbox);
        scroll.setFitToWidth(true);
        tab.setContent(scroll);
        return tab;
    }

    /** Αναφορά στο bar chart του tab VO2 Max για ανανέωση */
    private BarChart<String, Number> vo2BarChart;

    /**
     * Εκτελεί τον υπολογισμό VO2 Max και ενημερώνει τα labels και το γράφημα.
     */
    private void calculateVO2Max() {
        VO2MaxEstimator estimator = new VO2MaxEstimator();
        try {
            double vo2max  = estimator.estimateVO2Max(userProfile);
            String rating  = estimator.getRating(vo2max, userProfile);

            vo2ResultLabel.setText(String.format("VO2 Max: %.1f ml/kg/min", vo2max));
            vo2RatingLabel.setText("Αξιολόγηση: " + rating);

            // Χρωματισμός αξιολόγησης
            String color = switch (rating) {
                case "Άριστο"          -> "#1B5E20";
                case "Καλό"            -> "#2E7D32";
                case "Πάνω από Μέσο"  -> "#F57F17";
                case "Μέσο"            -> "#E65100";
                default                -> "#B71C1C";
            };
            vo2RatingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: " + color + "; -fx-font-weight: bold;");

            // Ενημέρωση bar chart με θερμίδες ανά δραστηριότητα
            updateVO2BarChart(estimator, vo2max);
            setStatus(String.format("VO2 Max: %.1f ml/kg/min – %s", vo2max, rating));

        } catch (IllegalArgumentException e) {
            showAlert("Σφάλμα", "Ορίστε τους παλμούς ηρεμίας στο tab Προφίλ.");
        }
    }

    /**
     * Ανανεώνει το bar chart με τις θερμίδες κάθε δραστηριότητας
     * υπολογισμένες μέσω VO2 Max.
     *
     * @param estimator ο υπολογιστής VO2 Max
     * @param vo2max    η εκτιμώμενη τιμή VO2 Max
     */
    private void updateVO2BarChart(VO2MaxEstimator estimator, double vo2max) {
        vo2BarChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        int idx = 1;
        for (Activity a : allActivities) {
            double durationMin = a.getTotalDurationSeconds() / 60.0;
            double calories    = estimator.estimateCalories(vo2max, userProfile.getWeightKg(), durationMin);
            String label       = a.getSport() + " #" + idx++;
            series.getData().add(new XYChart.Data<>(label, calories));
        }

        if (!series.getData().isEmpty()) {
            vo2BarChart.getData().add(series);
        }
    }

    // =========================================================================
    // Βοηθητικές μέθοδοι UI
    // =========================================================================

    /** Ενημερώνει το status bar στο κάτω μέρος του παραθύρου */
    private void setStatus(String message) {
        if (statusLabel != null) statusLabel.setText(message);
    }

    /** Εμφανίζει dialog σφάλματος */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
