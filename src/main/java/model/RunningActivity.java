package model;

import java.time.ZonedDateTime;

/**
 * Αναπαριστά μια δραστηριότητα τρεξίματος.
 *
 * <p>Το τρέξιμο είναι μια δραστηριότητα με απόσταση. Ο κύριος δείκτης
 * απόδοσης είναι ο ρυθμός (pace) σε λεπτά ανά χιλιόμετρο.</p>
 *
 * <p>Ο πολλαπλασιαστής θερμίδων {@code μ = 0.0175} βασίζεται στο
 * Metabolic Equivalent of Task (MET) για μέτριο τρέξιμο.</p>
 */
public class RunningActivity extends Activity {

    /**
     * Πολλαπλασιαστής θερμίδων για απλό υπολογισμό (MET / 60):
     * C = μ * βάρος(kg) * χρόνος(λεπτά)
     */
    private static final double CALORIE_MULTIPLIER = 0.0175;

    /**
     * Δημιουργεί μια νέα δραστηριότητα τρεξίματος.
     *
     * @param startTime η ώρα έναρξης του τρεξίματος
     */
    public RunningActivity(ZonedDateTime startTime) {
        super("Running", startTime);
    }

    @Override
    public String getDisplayName() {
        return "Τρέξιμο (Running)";
    }

    /**
     * Επιστρέφει τον πολλαπλασιαστή θερμίδων για τον απλό τύπο.
     *
     * @return ο πολλαπλασιαστής μ
     */
    public double getCalorieMultiplier() {
        return CALORIE_MULTIPLIER;
    }
}
