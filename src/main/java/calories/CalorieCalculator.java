package calories;

import model.Activity;
import model.UserProfile;

/**
 * Διεπαφή (Strategy) για τον υπολογισμό θερμίδων μιας δραστηριότητας.
 *
 * <p>Υπάρχουν πολλοί τρόποι εκτίμησης των θερμίδων, με διαφορετική
 * ακρίβεια και απαιτήσεις δεδομένων. Αυτή η διεπαφή επιτρέπει εύκολη
 * εναλλαγή μεταξύ τους χωρίς αλλαγή στον υπόλοιπο κώδικα (Strategy Pattern).</p>
 *
 * <p>Διαθέσιμες υλοποιήσεις:</p>
 * <ul>
 *   <li>{@link SimpleCalorieCalculator} – Απλός τύπος με πολλαπλασιαστή MET</li>
 *   <li>{@link HRCalorieCalculator} – Ακριβέστερος τύπος με καρδιακούς παλμούς</li>
 *   <li>{@link VO2MaxCalorieCalculator} – Τύπος βασισμένος στο VO2 Max</li>
 * </ul>
 */
public interface CalorieCalculator {

    /**
     * Υπολογίζει τις θερμίδες που καταναλώθηκαν κατά τη δραστηριότητα.
     *
     * @param activity    η δραστηριότητα για την οποία υπολογίζονται οι θερμίδες
     * @param userProfile τα προσωπικά δεδομένα του χρήστη
     * @return εκτίμηση θερμίδων σε kcal, ή -1 αν τα δεδομένα δεν επαρκούν
     */
    double calculate(Activity activity, UserProfile userProfile);

    /**
     * Επιστρέφει το εμφανιζόμενο όνομα αυτής της μεθόδου υπολογισμού.
     * Χρησιμοποιείται στη GUI για την επιλογή μεθόδου.
     *
     * @return το όνομα της μεθόδου
     */
    String getName();
}
