package calories;

import model.Activity;
import model.UserProfile;
import vo2max.VO2MaxEstimator;

/**
 * Υπολογίζει θερμίδες χρησιμοποιώντας τον δείκτη VO2 Max.
 *
 * <p>Ο τύπος είναι: <b>C = (VO2Max · w · t) / 200</b></p>
 *
 * <p>Αυτή η μέθοδος αποτελεί μέρος του προαιρετικού τμήματος 2 της εργασίας
 * (Εκτίμηση VO2 Max).</p>
 */
public class VO2MaxCalorieCalculator implements CalorieCalculator {

    private final VO2MaxEstimator estimator = new VO2MaxEstimator();

    @Override
    public double calculate(Activity activity, UserProfile userProfile) {
        if (userProfile.getRestingHeartRate() <= 0 || userProfile.getWeightKg() <= 0) {
            return -1;
        }

        try {
            double vo2max     = estimator.estimateVO2Max(userProfile);
            double weightKg   = userProfile.getWeightKg();
            double durationMin = activity.getTotalDurationSeconds() / 60.0;
            return estimator.estimateCalories(vo2max, weightKg, durationMin);
        } catch (IllegalArgumentException e) {
            return -1;
        }
    }

    @Override
    public String getName() {
        return "VO2 Max (Uth et al.)";
    }
}
