import calories.*;
import model.*;
import parser.TcxParser;
import stats.ActivityStats;
import gui.FitnessApp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Κεντρικό σημείο εισόδου. Χωρίς ορίσματα ανοίγει το GUI.
 * Με αρχεία .tcx τρέχει σε κονσόλα.
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0 || containsFlag(args, "--gui")) {
            FitnessApp.launch(args);
            return;
        }
        runConsoleMode(args);
    }

    private static void runConsoleMode(String[] args) {
        UserProfile profile = new UserProfile();
        List<String> tcxFiles = new ArrayList<String>();
        CalorieCalculator calculator = null;
        int calcMethod = 1;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-w") && i + 1 < args.length) {
                profile.setWeightKg(Double.parseDouble(args[++i]));
            } else if (args[i].equals("-a") && i + 1 < args.length) {
                profile.setAge(Integer.parseInt(args[++i]));
            } else if (args[i].equals("-g") && i + 1 < args.length) {
                String gender = args[++i].toLowerCase();
                profile.setGender(gender.startsWith("f") ? UserProfile.Gender.FEMALE : UserProfile.Gender.MALE);
            } else if (args[i].equals("-r") && i + 1 < args.length) {
                profile.setRestingHeartRate(Integer.parseInt(args[++i]));
            } else if (args[i].equals("-c") && i + 1 < args.length) {
                calcMethod = Integer.parseInt(args[++i]);
            } else if (!args[i].startsWith("-")) {
                tcxFiles.add(args[i]);
            }
        }

        if (profile.getWeightKg() > 0) {
            if (calcMethod == 2) calculator = new HRCalorieCalculator();
            else if (calcMethod == 3) calculator = new VO2MaxCalorieCalculator();
            else calculator = new SimpleCalorieCalculator();
        }

        if (tcxFiles.isEmpty()) {
            System.err.println("Χρήση: java -jar coach.jar [-w βάρος] [-a ηλικία] [-g m|f] αρχείο.tcx ...");
            System.exit(1);
        }

        TcxParser parser = new TcxParser();
        for (String filePath : tcxFiles) {
            File file = new File(filePath);
            if (!file.exists()) { System.err.println("Δεν βρέθηκε: " + filePath); continue; }
            try {
                List<Activity> activities = parser.parse(file);
                for (Activity activity : activities) {
                    System.out.println(new ActivityStats(activity, profile, calculator).format());
                }
            } catch (Exception e) {
                System.err.println("Σφάλμα: " + filePath + ": " + e.getMessage());
            }
        }
    }

    private static boolean containsFlag(String[] args, String flag) {
        for (String arg : args) { if (arg.equals(flag)) return true; }
        return false;
    }
}
