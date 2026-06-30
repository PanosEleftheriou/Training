package model;

import java.time.ZonedDateTime;

/**
 * Αναπαριστά μια δραστηριότητα κολύμβησης.
 *
 * <p>Το κολύμπι συνήθως δεν έχει GPS δεδομένα (η συσκευή δεν φοριέται
 * στο νερό ή δεν λαμβάνει σήμα), οπότε η απόσταση μπορεί να
 * υπολογιστεί από τον αριθμό μήκους πισίνας.</p>
 */
public class SwimmingActivity extends Activity {

    /** Πολλαπλασιαστής θερμίδων για ελεύθερο κολύμβησης (MET ≈ 8 / 60) */
    private static final double CALORIE_MULTIPLIER = 0.0133;

    public SwimmingActivity(ZonedDateTime startTime) {
        super("Swimming", startTime);
    }

    @Override
    public String getDisplayName() {
        return "Κολύμπι (Swimming)";
    }

    public double getCalorieMultiplier() {
        return CALORIE_MULTIPLIER;
    }
}
