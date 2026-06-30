package stats;

import model.Activity;
import model.UserProfile;
import calories.CalorieCalculator;

/**
 * Υπολογίζει και μορφοποιεί στατιστικά μιας δραστηριότητας
 * για εκτύπωση στην κονσόλα ή εμφάνιση στη GUI.
 *
 * <p>Συγκεντρώνει όλους τους υπολογισμούς και τη μορφοποίηση
 * σε ένα μέρος, διατηρώντας τις κλάσεις μοντέλου καθαρές.</p>
 */
public class ActivityStats {

    private final Activity activity;
    private final UserProfile userProfile;
    private final CalorieCalculator calorieCalculator;

    /**
     * Δημιουργεί ένα αντικείμενο στατιστικών για μια δραστηριότητα.
     *
     * @param activity          η δραστηριότητα προς ανάλυση
     * @param userProfile       τα δεδομένα χρήστη (μπορεί να είναι null αν δεν χρειάζεται)
     * @param calorieCalculator η μέθοδος υπολογισμού θερμίδων (null = δεν υπολογίζεται)
     */
    public ActivityStats(Activity activity, UserProfile userProfile, CalorieCalculator calorieCalculator) {
        this.activity = activity;
        this.userProfile = userProfile;
        this.calorieCalculator = calorieCalculator;
    }

    /**
     * Μορφοποιεί τα στατιστικά της δραστηριότητας για εκτύπωση.
     * Το format ακολουθεί την προδιαγραφή της εργασίας.
     *
     * @return String με τα στατιστικά έτοιμα για εκτύπωση
     */
    public String format() {
        StringBuilder sb = new StringBuilder();

        sb.append("Activity: ").append(activity.getSport()).append("\n");
        sb.append("Total Time: ").append(formatDuration(activity.getTotalDurationSeconds())).append("\n");

        if (activity.hasDistance()) {
            double km = activity.getTotalDistanceMeters() / 1000.0;
            sb.append(String.format("Total Distance: %.2f km%n", km));

            // Ρυθμός (pace) για τρέξιμο/περπάτημα, ταχύτητα για ποδήλατο
            if (activity.getSport().equalsIgnoreCase("Biking") ||
                activity.getSport().equalsIgnoreCase("Cycling")) {
                double speedKmh = activity.getAverageSpeedKmh();
                sb.append(String.format("Avg Speed: %.2f km/h%n", speedKmh));
            } else {
                double paceMinKm = activity.getAveragePaceMinPerKm();
                sb.append(String.format("Avg Pace: %.2f min/km%n", paceMinKm));
            }
        }

        double avgHR = activity.getAverageHeartRate();
        if (avgHR > 0) {
            sb.append(String.format("Avg Heart Rate: %.0f bpm%n", avgHR));
        }

        int maxHR = activity.getMaxHeartRate();
        if (maxHR > 0) {
            sb.append(String.format("Max Heart Rate: %d bpm%n", maxHR));
        }

        // Υπολογισμός θερμίδων αν υπάρχουν τα απαραίτητα δεδομένα
        if (calorieCalculator != null && userProfile != null) {
            double calories = calorieCalculator.calculate(activity, userProfile);
            if (calories >= 0) {
                sb.append(String.format("Calories: %.0f kcal%n", calories));
            }
        }

        return sb.toString();
    }

    /**
     * Μετατρέπει τα δευτερόλεπτα σε μορφή mm:ss ή hh:mm:ss.
     *
     * @param totalSeconds η διάρκεια σε δευτερόλεπτα
     * @return η μορφοποιημένη διάρκεια (π.χ. "45:32" ή "1:05:10")
     */
    public static String formatDuration(long totalSeconds) {
        long hours   = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
    }

    // --- Getters για χρήση στη GUI ---

    public Activity getActivity() { return activity; }

    /**
     * Επιστρέφει τις θερμίδες ή -1 αν δεν μπορούν να υπολογιστούν.
     *
     * @return kcal ή -1
     */
    public double getCalories() {
        if (calorieCalculator == null || userProfile == null) return -1;
        return calorieCalculator.calculate(activity, userProfile);
    }
}
