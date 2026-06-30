package model;

import java.time.ZonedDateTime;

/**
 * Αναπαριστά μια δραστηριότητα ποδηλασίας.
 *
 * <p>Η ποδηλασία μετράει απόσταση και χρησιμοποιεί ωριαία ταχύτητα
 * (km/h) ως κύριο δείκτη απόδοσης αντί για ρυθμό (pace).</p>
 */
public class CyclingActivity extends Activity {

    /**
     * Πολλαπλασιαστής θερμίδων για μέτρια ποδηλασία (MET ≈ 8 / 60)
     */
    private static final double CALORIE_MULTIPLIER = 0.0133;

    /**
     * Δημιουργεί μια νέα δραστηριότητα ποδηλασίας.
     *
     * @param startTime η ώρα έναρξης
     */
    public CyclingActivity(ZonedDateTime startTime) {
        super("Biking", startTime);
    }

    @Override
    public String getDisplayName() {
        return "Ποδήλατο (Biking)";
    }

    public double getCalorieMultiplier() {
        return CALORIE_MULTIPLIER;
    }
}
