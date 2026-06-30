package gui;

import calories.*;
import model.*;
import parser.TcxParser;
import stats.ActivityStats;
import vo2max.VO2MaxEstimator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.List;

/** Κεντρική κλάση της Swing εφαρμογής "Βοηθός Προπόνησης". */
public class FitnessApp extends JFrame {

    private final List<Activity> allActivities = new ArrayList<Activity>();
    private final UserProfile userProfile = new UserProfile();
    private CalorieCalculator currentCalculator = new SimpleCalorieCalculator();

    private JTextArea statsArea;
    private JLabel statusLabel;
    private JLabel vo2ResultLabel;
    private JLabel vo2RatingLabel;
    private JLabel dailyGoalLabel;

    public FitnessApp() {
        setTitle("Βοηθός Προπόνησης – Χαροκόπειο Πανεπιστήμιο");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(850, 600));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Δραστηριότητες", buildActivitiesPanel());
        tabs.addTab("Προσθήκη",       buildAddPanel());
        tabs.addTab("Προφίλ",         buildProfilePanel());
        tabs.addTab("VO2 Max",        buildVO2MaxPanel());

        statusLabel = new JLabel("Έτοιμο – Φορτώστε αρχεία TCX για να ξεκινήσετε.");
        statusLabel.setBorder(new EmptyBorder(4, 8, 4, 8));

        add(tabs, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new FitnessApp().setVisible(true);
            }
        });
    }

    // ── Tab 1: Δραστηριότητες ────────────────────────────────────────────────

    private JPanel buildActivitiesPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Toolbar
        JButton loadBtn  = new JButton("Φόρτωση TCX...");
        JButton clearBtn = new JButton("Εκκαθάριση");
        JLabel calcLabel = new JLabel("Μέθοδος θερμίδων:");
        String[] methods = {"Απλός (MET)", "Καρδιακοί παλμοί", "VO2 Max"};
        JComboBox<String> calcCombo = new JComboBox<String>(methods);

        loadBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { loadTcxFiles(); }
        });
        clearBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { allActivities.clear(); refreshStats(); }
        });
        calcCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int idx = calcCombo.getSelectedIndex();
                if (idx == 1)      currentCalculator = new HRCalorieCalculator();
                else if (idx == 2) currentCalculator = new VO2MaxCalorieCalculator();
                else               currentCalculator = new SimpleCalorieCalculator();
                refreshStats();
            }
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(loadBtn); toolbar.add(clearBtn);
        toolbar.add(calcLabel); toolbar.add(calcCombo);

        statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        dailyGoalLabel = new JLabel("(Ορίστε ημερήσιο στόχο θερμίδων στο tab Προφίλ)");
        dailyGoalLabel.setBorder(new EmptyBorder(4, 4, 4, 4));

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(statsArea), BorderLayout.CENTER);
        panel.add(dailyGoalLabel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadTcxFiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle("Επιλογή αρχείων TCX");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Αρχεία TCX", "tcx"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        TcxParser parser = new TcxParser();
        int loaded = 0;
        for (File file : chooser.getSelectedFiles()) {
            try {
                List<Activity> activities = parser.parse(file);
                allActivities.addAll(activities);
                loaded += activities.size();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Σφάλμα: " + file.getName() + "\n" + e.getMessage(), "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            }
        }
        setStatus("Φορτώθηκαν " + loaded + " δραστηριότητες.");
        refreshStats();
    }

    private void refreshStats() {
        if (allActivities.isEmpty()) {
            statsArea.setText("Δεν υπάρχουν φορτωμένες δραστηριότητες.");
            updateDailyGoalLabel();
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append(" ΣΤΑΤΙΣΤΙΚΑ ΔΡΑΣΤΗΡΙΟΤΗΤΩΝ\n");
        sb.append("==================================================\n\n");

        double totalCalories = 0;
        for (Activity activity : allActivities) {
            ActivityStats stats = new ActivityStats(activity, userProfile, currentCalculator);
            sb.append(stats.format());
            sb.append("----------------------------------------\n");
            double cal = stats.getCalories();
            if (cal >= 0) totalCalories += cal;
        }
        if (allActivities.size() > 1) {
            long totalSec = 0; double totalKm = 0;
            for (Activity a : allActivities) {
                totalSec += a.getTotalDurationSeconds();
                totalKm  += a.getTotalDistanceMeters() / 1000.0;
            }
            sb.append("\n==================================================\n");
            sb.append("ΣΥΝΟΛΑ\n");
            sb.append("==================================================\n");
            sb.append("Συνολικός χρόνος: ").append(ActivityStats.formatDuration(totalSec)).append("\n");
            sb.append(String.format("Συνολική απόσταση: %.2f km%n", totalKm));
            if (totalCalories > 0) sb.append(String.format("Συνολικές θερμίδες: %.0f kcal%n", totalCalories));
        }
        statsArea.setText(sb.toString());
        statsArea.setCaretPosition(0);
        updateDailyGoalLabel();
    }

    private void updateDailyGoalLabel() {
        double goal = userProfile.getDailyCalorieGoal();
        if (goal <= 0 || allActivities.isEmpty()) {
            dailyGoalLabel.setText("(Ορίστε ημερήσιο στόχο θερμίδων στο tab Προφίλ)");
            return;
        }
        Map<LocalDate, Double> dailyCals = new TreeMap<LocalDate, Double>();
        for (Activity a : allActivities) {
            ZonedDateTime start = a.getStartTime();
            if (start == null) continue;
            LocalDate day = start.toLocalDate();
            double cal = currentCalculator.calculate(a, userProfile);
            if (cal >= 0) {
                Double existing = dailyCals.get(day);
                dailyCals.put(day, existing == null ? cal : existing + cal);
            }
        }
        StringBuilder sb = new StringBuilder("Στόχος: " + (int) goal + " kcal  |  ");
        for (Map.Entry<LocalDate, Double> entry : dailyCals.entrySet()) {
            double cal = entry.getValue();
            String status = cal >= goal ? "✅" : "❌";
            double rem = goal - cal;
            sb.append(entry.getKey()).append(": ").append((int) cal).append(" kcal ").append(status);
            if (rem > 0) sb.append(" (απομένουν ").append((int) rem).append(")");
            sb.append("  ");
        }
        dailyGoalLabel.setText(sb.toString());
    }

    // ── Tab 2: Προσθήκη ──────────────────────────────────────────────────────

    private JPanel buildAddPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        String[] sports = {"Running", "Biking", "Walking", "Swimming"};
        JComboBox<String> typeCombo = new JComboBox<String>(sports);
        JTextField durField  = new JTextField("30", 10);
        JTextField distField = new JTextField("5.0", 10);
        JTextField hrField   = new JTextField("0", 10);
        JButton addBtn = new JButton("Προσθήκη δραστηριότητας");
        addBtn.setBackground(new Color(76, 175, 80));
        addBtn.setForeground(Color.WHITE);

        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String sport  = (String) typeCombo.getSelectedItem();
                    double durMin = Double.parseDouble(durField.getText());
                    double distKm = Double.parseDouble(distField.getText());
                    int    avgHr  = Integer.parseInt(hrField.getText());
                    allActivities.add(createManualActivity(sport, durMin, distKm, avgHr));
                    setStatus("Προστέθηκε: " + sport + " – " + (int) durMin + " λεπτά");
                    refreshStats();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "Ελέγξτε τις αριθμητικές τιμές.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        c.gridx=0; c.gridy=0; panel.add(new JLabel("Τύπος:"), c);
        c.gridx=1; panel.add(typeCombo, c);
        c.gridx=0; c.gridy=1; panel.add(new JLabel("Διάρκεια (λεπτά):"), c);
        c.gridx=1; panel.add(durField, c);
        c.gridx=0; c.gridy=2; panel.add(new JLabel("Απόσταση (km):"), c);
        c.gridx=1; panel.add(distField, c);
        c.gridx=0; c.gridy=3; panel.add(new JLabel("Μέσοι παλμοί (bpm):"), c);
        c.gridx=1; panel.add(hrField, c);
        c.gridx=0; c.gridy=4; c.gridwidth=2; panel.add(addBtn, c);
        return panel;
    }

    private Activity createManualActivity(String sport, double durMin, double distKm, int avgHr) {
        ZonedDateTime now = ZonedDateTime.now();
        Activity activity;
        String s = sport.toLowerCase();
        if (s.equals("biking"))        activity = new CyclingActivity(now);
        else if (s.equals("walking"))  activity = new WalkingActivity(now);
        else if (s.equals("swimming")) activity = new SwimmingActivity(now);
        else                           activity = new RunningActivity(now);
        activity.setSport(sport);
        Lap lap = new Lap(now);
        Track track = new Track();
        Trackpoint start = new Trackpoint();
        start.setTimestamp(now); start.setDistanceMeters(0.0);
        if (avgHr > 0) start.setHeartRateBpm(avgHr);
        Trackpoint end = new Trackpoint();
        end.setTimestamp(now.plusSeconds((long)(durMin * 60)));
        end.setDistanceMeters(distKm * 1000.0);
        if (avgHr > 0) end.setHeartRateBpm(avgHr);
        track.addTrackpoint(start); track.addTrackpoint(end);
        lap.addTrack(track); activity.addLap(lap);
        return activity;
    }

    // ── Tab 3: Προφίλ ────────────────────────────────────────────────────────

    private JPanel buildProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.WEST;

        JRadioButton maleRb   = new JRadioButton("Άνδρας", true);
        JRadioButton femaleRb = new JRadioButton("Γυναίκα");
        ButtonGroup bg = new ButtonGroup();
        bg.add(maleRb); bg.add(femaleRb);
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        genderPanel.add(maleRb); genderPanel.add(femaleRb);

        JTextField ageField    = new JTextField(String.valueOf(userProfile.getAge()), 10);
        JTextField weightField = new JTextField(String.valueOf(userProfile.getWeightKg()), 10);
        JTextField rhrField    = new JTextField(String.valueOf(userProfile.getRestingHeartRate()), 10);
        JTextField goalField   = new JTextField("0", 10);
        JButton saveBtn = new JButton("Αποθήκευση προφίλ");
        saveBtn.setBackground(new Color(33, 150, 243));
        saveBtn.setForeground(Color.WHITE);

        saveBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    userProfile.setGender(femaleRb.isSelected() ? UserProfile.Gender.FEMALE : UserProfile.Gender.MALE);
                    userProfile.setAge(Integer.parseInt(ageField.getText()));
                    userProfile.setWeightKg(Double.parseDouble(weightField.getText()));
                    userProfile.setRestingHeartRate(Integer.parseInt(rhrField.getText()));
                    userProfile.setDailyCalorieGoal(Double.parseDouble(goalField.getText()));
                    setStatus("Το προφίλ αποθηκεύτηκε.");
                    refreshStats();
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(panel, "Ελέγξτε τις αριθμητικές τιμές.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        c.gridx=0; c.gridy=0; panel.add(new JLabel("Φύλο:"), c);
        c.gridx=1; panel.add(genderPanel, c);
        c.gridx=0; c.gridy=1; panel.add(new JLabel("Ηλικία (χρόνια):"), c);
        c.gridx=1; panel.add(ageField, c);
        c.gridx=0; c.gridy=2; panel.add(new JLabel("Βάρος (kg):"), c);
        c.gridx=1; panel.add(weightField, c);
        c.gridx=0; c.gridy=3; panel.add(new JLabel("Παλμοί εν ηρεμία (bpm):"), c);
        c.gridx=1; panel.add(rhrField, c);
        c.gridx=0; c.gridy=4; panel.add(new JLabel("Ημερήσιος στόχος (kcal):"), c);
        c.gridx=1; panel.add(goalField, c);
        c.gridx=0; c.gridy=5; c.gridwidth=2; panel.add(saveBtn, c);
        return panel;
    }

    // ── Tab 4: VO2 Max ───────────────────────────────────────────────────────

    private JPanel buildVO2MaxPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel infoLabel = new JLabel("<html><i>Εκτίμηση VO2 Max με τη μέθοδο Uth et al.<br>Απαιτεί: ηλικία, φύλο και παλμοί εν ηρεμία από το Προφίλ.</i></html>");
        JLabel formulaLabel = new JLabel("Τύπος: VO2Max = 15.3 × (MHR / RHR)   |   MHR = 220 − ηλικία");
        formulaLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        formulaLabel.setForeground(Color.DARK_GRAY);

        vo2ResultLabel = new JLabel("VO2 Max: –");
        vo2ResultLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));

        vo2RatingLabel = new JLabel("Αξιολόγηση: –");
        vo2RatingLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        vo2RatingLabel.setForeground(new Color(21, 101, 192));

        JButton calcBtn = new JButton("Υπολόγισε VO2 Max");
        calcBtn.setBackground(new Color(21, 101, 192));
        calcBtn.setForeground(Color.WHITE);
        calcBtn.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        calcBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { calculateVO2Max(); }
        });

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(infoLabel);
        top.add(Box.createVerticalStrut(8));
        top.add(formulaLabel);
        top.add(Box.createVerticalStrut(12));
        top.add(calcBtn);
        top.add(Box.createVerticalStrut(12));
        top.add(vo2ResultLabel);
        top.add(Box.createVerticalStrut(6));
        top.add(vo2RatingLabel);

        panel.add(top, BorderLayout.NORTH);
        return panel;
    }

    private void calculateVO2Max() {
        VO2MaxEstimator estimator = new VO2MaxEstimator();
        try {
            double vo2max = estimator.estimateVO2Max(userProfile);
            String rating = estimator.getRating(vo2max, userProfile);
            vo2ResultLabel.setText(String.format("VO2 Max: %.1f ml/kg/min", vo2max));
            vo2RatingLabel.setText("Αξιολόγηση: " + rating);

            Color color;
            if (rating.equals("Άριστο"))              color = new Color(27, 94, 32);
            else if (rating.equals("Καλό"))            color = new Color(46, 125, 50);
            else if (rating.equals("Πάνω από Μέσο"))  color = new Color(245, 127, 23);
            else if (rating.equals("Μέσο"))            color = new Color(230, 81, 0);
            else                                        color = new Color(183, 28, 28);
            vo2RatingLabel.setForeground(color);

            setStatus(String.format("VO2 Max: %.1f ml/kg/min – %s", vo2max, rating));
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Ορίστε τους παλμούς εν ηρεμία στο tab Προφίλ.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void setStatus(String msg) { if (statusLabel != null) statusLabel.setText(msg); }

    /** Εκκίνηση από κονσόλα ή ως GUI */
    public static void launch(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { new FitnessApp().setVisible(true); }
        });
    }
}
