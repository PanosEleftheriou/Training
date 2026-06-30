package vo2max;

import model.UserProfile;

/**
 * Υπολογίζει και αξιολογεί το VO2 Max (μέγιστη αερόβια ικανότητα).
 *
 * <p>Το VO2 Max είναι ο δείκτης της μέγιστης ποσότητας οξυγόνου (σε ml)
 * που μπορεί να καταναλώσει ο οργανισμός ανά λεπτό ανά κιλό σωματικού
 * βάρους (ml/kg/min). Είναι η πιο έγκυρη μέτρηση αερόβιας φυσικής κατάστασης.</p>
 *
 * <h2>Υπολογισμός</h2>
 * <ol>
 *   <li>MHR = 220 − ηλικία</li>
 *   <li>VO2Max = 15.3 × (MHR / RHR)</li>
 * </ol>
 *
 * <p>Όπου RHR = παλμοί ηρεμίας (Resting Heart Rate)</p>
 *
 * <p>Αυτή είναι η μέθοδος Uth-Sørensen-Overgaard-Pedersen (2004) και
 * αποτελεί μια εκτίμηση χωρίς εξοπλισμό εργαστηρίου.</p>
 */
public class VO2MaxEstimator {

    /**
     * Πίνακας αξιολόγησης VO2 Max για <b>άνδρες</b> ανά ηλικιακή ομάδα.
     * Κάθε γραμμή: {minAge, maxAge, excellent, good, above_avg, avg, below_avg}
     * Τιμές σε ml/kg/min.
     */
    private static final double[][] MALE_RATING_TABLE = {
        // ηλικία   Άριστο  Καλό  Πάνω  Μέσο  Κάτω
        {13, 19,    55.9,   51.0, 45.2, 41.8, 38.4},
        {20, 29,    52.4,   46.4, 42.4, 37.1, 33.0},
        {30, 39,    49.4,   43.4, 39.2, 35.4, 31.1},
        {40, 49,    48.0,   43.7, 38.4, 33.6, 30.2},
        {50, 59,    45.3,   39.5, 35.6, 32.3, 26.1},
        {60, 120,   44.2,   37.4, 33.0, 29.4, 23.0},
    };

    /**
     * Πίνακας αξιολόγησης VO2 Max για <b>γυναίκες</b> ανά ηλικιακή ομάδα.
     * Τιμές σε ml/kg/min.
     */
    private static final double[][] FEMALE_RATING_TABLE = {
        // ηλικία   Άριστο  Καλό  Πάνω  Μέσο  Κάτω
        {13, 19,    44.0,   38.4, 35.0, 31.4, 29.0},
        {20, 29,    41.0,   36.6, 33.0, 29.4, 25.0},
        {30, 39,    40.0,   35.6, 31.5, 27.7, 23.0},
        {40, 49,    36.9,   33.0, 30.1, 26.7, 22.0},
        {50, 59,    35.2,   31.4, 27.5, 24.4, 20.0},
        {60, 120,   31.4,   27.0, 24.5, 21.7, 18.0},
    };

    /**
     * Υπολογίζει την εκτιμώμενη VO2 Max με τη μέθοδο Uth et al.
     *
     * @param userProfile το προφίλ χρήστη με ηλικία και παλμούς ηρεμίας
     * @return η εκτιμώμενη VO2 Max σε ml/kg/min
     * @throws IllegalArgumentException αν ο παλμός ηρεμίας είναι 0 ή αρνητικός
     */
    public double estimateVO2Max(UserProfile userProfile) {
        int rhr = userProfile.getRestingHeartRate();
        if (rhr <= 0) {
            throw new IllegalArgumentException("Οι παλμοί ηρεμίας πρέπει να είναι > 0");
        }
        int mhr = userProfile.getMaxHeartRate();
        return 15.3 * ((double) mhr / rhr);
    }

    /**
     * Αξιολογεί το αποτέλεσμα VO2 Max με βάση πίνακες αναφοράς
     * ανά ηλικία και φύλο.
     *
     * @param vo2max      η τιμή VO2 Max σε ml/kg/min
     * @param userProfile το προφίλ του χρήστη (ηλικία και φύλο)
     * @return αξιολόγηση: "Άριστο", "Καλό", "Πάνω από Μέσο",
     *         "Μέσο", "Κάτω από Μέσο" ή "Χαμηλό"
     */
    public String getRating(double vo2max, UserProfile userProfile) {
        int age = userProfile.getAge();
        boolean isMale = userProfile.getGender() == UserProfile.Gender.MALE;
        double[][] table = isMale ? MALE_RATING_TABLE : FEMALE_RATING_TABLE;

        for (double[] row : table) {
            int minAge = (int) row[0];
            int maxAge = (int) row[1];
            if (age < minAge || age > maxAge) continue;

            // Σύγκριση με κατώτατα όρια από υψηλότερο προς χαμηλότερο επίπεδο
            if (vo2max >= row[2]) return "Άριστο";
            if (vo2max >= row[3]) return "Καλό";
            if (vo2max >= row[4]) return "Πάνω από Μέσο";
            if (vo2max >= row[5]) return "Μέσο";
            if (vo2max >= row[6]) return "Κάτω από Μέσο";
            return "Χαμηλό";
        }
        return "Άγνωστο";
    }

    /**
     * Υπολογίζει τις θερμίδες μιας δραστηριότητας βάσει VO2 Max.
     *
     * <p>Τύπος: C = (VO2Max · w · t) / 200</p>
     *
     * <p>Όπου w = βάρος σε kg, t = χρόνος σε λεπτά.</p>
     *
     * @param vo2max      η εκτιμώμενη VO2 Max (ml/kg/min)
     * @param weightKg    το βάρος του χρήστη σε kg
     * @param durationMin η διάρκεια δραστηριότητας σε λεπτά
     * @return εκτιμώμενες θερμίδες σε kcal
     */
    public double estimateCalories(double vo2max, double weightKg, double durationMin) {
        return (vo2max * weightKg * durationMin) / 200.0;
    }
}
