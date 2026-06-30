package calories;

import model.*;

/**
 * Υπολογίζει θερμίδες με τον απλό τύπο βάσει πολλαπλασιαστή MET.
 * C = μ · w · t
 */
public class SimpleCalorieCalculator implements CalorieCalculator {

    private static final double DEFAULT_MULTIPLIER = 0.0100;

    @Override
    public double calculate(Activity activity, UserProfile userProfile) {
        if (userProfile.getWeightKg() <= 0) return -1;
        double mu = getMultiplier(activity);
        double w  = userProfile.getWeightKg();
        double t  = activity.getTotalDurationSeconds() / 60.0;
        return mu * w * t;
    }

    private double getMultiplier(Activity activity) {
        if (activity instanceof RunningActivity) return ((RunningActivity) activity).getCalorieMultiplier();
        if (activity instanceof CyclingActivity) return ((CyclingActivity) activity).getCalorieMultiplier();
        if (activity instanceof WalkingActivity) return ((WalkingActivity) activity).getCalorieMultiplier();
        if (activity instanceof SwimmingActivity) return ((SwimmingActivity) activity).getCalorieMultiplier();
        return DEFAULT_MULTIPLIER;
    }

    @Override
    public String getName() {
        return "Απλός (MET · βάρος · χρόνος)";
    }
}
