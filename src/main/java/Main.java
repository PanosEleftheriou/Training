

import calories.*;
import model.*;
import parser.TcxParser;
import stats.ActivityStats;
import gui.FitnessApp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Κεντρικό σημείο εισόδου της εφαρμογής "Βοηθός Προπόνησης".
 *
 * <h2>Τρόποι χρήσης</h2>
 *
 * <h3>Κονσόλα (Μέρος 1)</h3>
 * <pre>
 *   java -jar coach.jar activity.tcx
 *   java -jar coach.jar run1.tcx run2.tcx run3.tcx
 *   java -jar coach.jar -w 70.5 run1.tcx run2.tcx
 *   java -jar coach.jar -w 70.5 -a 25 -g male run.tcx
 * </pre>
 *
 * <h3>Γραφική Διεπαφή (Μέρος 2)</h3>
 * <pre>
 *   java -jar coach.jar --gui
 *   mvn javafx:run
 * </pre>
 *
 * <h3>Παράμετροι</h3>
 * <ul>
 *   <li>{@code --gui}     – Εκκίνηση γραφικής διεπαφής</li>
 *   <li>{@code -w <kg>}  – Βάρος χρήστη σε kg (ενεργοποιεί υπολογισμό θερμίδων)</li>
 *   <li>{@code -a <age>} – Ηλικία χρήστη σε χρόνια</li>
 *   <li>{@code -g <m|f>} – Φύλο χρήστη (male/female)</li>
 *   <li>{@code -r <bpm>} – Παλμοί ηρεμίας (για VO2 Max)</li>
 *   <li>{@code -c <1|2|3>} – Μέθοδος θερμίδων: 1=Απλή, 2=HR, 3=VO2Max</li>
 * </ul>
 */
public class Main {

    public static void main(String[] args) {
        // Αν δεν δοθούν ορίσματα, εκκίνηση GUI
        if (args.length == 0 || containsFlag(args, "--gui")) {
            FitnessApp.launch(FitnessApp.class, args);
            return;
        }

        // Κονσόλα: ανάλυση ορισμάτων
        runConsoleMode(args);
    }

    /**
     * Εκτελεί την εφαρμογή σε λειτουργία κονσόλας.
     * Αναλύει τα ορίσματα, φορτώνει τα αρχεία TCX και εκτυπώνει στατιστικά.
     *
     * @param args τα ορίσματα γραμμής εντολών
     */
    private static void runConsoleMode(String[] args) {
        UserProfile profile = new UserProfile();
        List<String> tcxFiles = new ArrayList<>();
        CalorieCalculator calculator = null;
        int calcMethod = 1; // προεπιλογή: απλός υπολογισμός

        // Ανάλυση ορισμάτων
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-w" -> {
                    if (i + 1 < args.length) {
                        profile.setWeightKg(Double.parseDouble(args[++i]));
                    }
                }
                case "-a" -> {
                    if (i + 1 < args.length) {
                        profile.setAge(Integer.parseInt(args[++i]));
                    }
                }
                case "-g" -> {
                    if (i + 1 < args.length) {
                        String gender = args[++i].toLowerCase();
                        profile.setGender(gender.startsWith("f")
                                ? UserProfile.Gender.FEMALE
                                : UserProfile.Gender.MALE);
                    }
                }
                case "-r" -> {
                    if (i + 1 < args.length) {
                        profile.setRestingHeartRate(Integer.parseInt(args[++i]));
                    }
                }
                case "-c" -> {
                    if (i + 1 < args.length) {
                        calcMethod = Integer.parseInt(args[++i]);
                    }
                }
                default -> {
                    // Αν δεν είναι flag, θεωρούμε ότι είναι αρχείο TCX
                    if (!args[i].startsWith("-")) {
                        tcxFiles.add(args[i]);
                    }
                }
            }
        }

        // Επιλογή μεθόδου υπολογισμού θερμίδων
        if (profile.getWeightKg() > 0) {
            calculator = switch (calcMethod) {
                case 2  -> new HRCalorieCalculator();
                case 3  -> new VO2MaxCalorieCalculator();
                default -> new SimpleCalorieCalculator();
            };
        }

        if (tcxFiles.isEmpty()) {
            System.err.println("Χρήση: java -jar coach.jar [-w βάρος] [-a ηλικία] [-g m|f] αρχείο1.tcx ...");
            System.exit(1);
        }

        // Φόρτωση και εκτύπωση δεδομένων από όλα τα αρχεία
        TcxParser parser = new TcxParser();
        for (String filePath : tcxFiles) {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("Το αρχείο δεν βρέθηκε: " + filePath);
                continue;
            }
            try {
                List<Activity> activities = parser.parse(file);
                for (Activity activity : activities) {
                    ActivityStats stats = new ActivityStats(activity, profile, calculator);
                    System.out.println(stats.format());
                }
            } catch (Exception e) {
                System.err.println("Σφάλμα ανάγνωσης " + filePath + ": " + e.getMessage());
            }
        }
    }

    /** Ελέγχει αν ένα flag υπάρχει στη λίστα ορισμάτων */
    private static boolean containsFlag(String[] args, String flag) {
        for (String arg : args) {
            if (arg.equals(flag)) return true;
        }
        return false;
    }
}
