package model;

import java.time.ZonedDateTime;

/**
 * Αναπαριστά μια δραστηριότητα περπατήματος.
 *
 * <p>Το περπάτημα έχει χαμηλότερη ένταση από το τρέξιμο και κατά
 * συνέπεια χαμηλότερο πολλαπλασιαστή θερμίδων.</p>
 */
public class WalkingActivity extends Activity {

    /** Πολλαπλασιαστής θερμίδων για γρήγορο περπάτημα (MET ≈ 4 / 60) */
    private static final double CALORIE_MULTIPLIER = 0.0067;

    public WalkingActivity(ZonedDateTime startTime) {
        super("Walking", startTime);
    }

    @Override
    public String getDisplayName() {
        return "Περπάτημα (Walking)";
    }

    public double getCalorieMultiplier() {
        return CALORIE_MULTIPLIER;
    }
}
