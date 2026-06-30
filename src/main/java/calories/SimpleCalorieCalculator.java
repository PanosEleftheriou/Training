package calories;

import model.*;

/**
 * Υπολογίζει θερμίδες με τον απλό τύπο βάσει πολλαπλασιαστή MET.
 *
 * <p>Ο τύπος είναι: <b>C = μ · w · t</b> όπου:</p>
 * <ul>
 *   <li>μ = πολλαπλασιαστής ειδικός για κάθε δραστηριότητα (MET/60)</li>
 *   <li>w = βάρος χρήστη σε kg</li>
 *   <li>t = διάρκεια δραστηριότητας σε λεπτά</li>
 * </ul>
 *
 * <p>Αυτός ο τύπος είναι ο απλούστερος αλλά και λιγότερο ακριβής,
 * καθώς δεν λαμβάνει υπόψη την ένταση της δραστηριότητας.</p>
 */
public class SimpleCalorieCalculator implements CalorieCalculator {

    /** Πολλαπλασιαστής για δραστηριότητες χωρίς εξειδίκευση */
    private static final double DEFAULT_MULTIPLIER = 0.0100;

    @Override
    public double calculate(Activity activity, UserProfile userProfile) {
        if (userProfile.getWeightKg() <= 0) return -1;

        double mu = getMultiplier(activity);
        double w  = userProfile.getWeightKg();
        double t  = activity.getTotalDurationSeconds() / 60.0; // σε λεπτά

        return mu * w * t;
    }

    /**
     * Εξάγει τον πολλαπλασιαστή από τον τύπο δραστηριότητας.
     * Αν δεν αναγνωριστεί ο τύπος, χρησιμοποιείται προεπιλεγμένη τιμή.
     *
     * @param activity η δραστηριότητα
     * @return ο κατάλληλος πολλαπλασιαστής μ
     */
    private double getMultiplier(Activity activity) {
        if (activity instanceof RunningActivity ra) return ra.getCalorieMultiplier();
        if (activity instanceof CyclingActivity ca) return ca.getCalorieMultiplier();
        if (activity instanceof WalkingActivity  wa) return wa.getCalorieMultiplier();
        if (activity instanceof SwimmingActivity sa) return sa.getCalorieMultiplier();
        return DEFAULT_MULTIPLIER;
    }

    @Override
    public String getName() {
        return "Απλός (MET · βάρος · χρόνος)";
    }
}
