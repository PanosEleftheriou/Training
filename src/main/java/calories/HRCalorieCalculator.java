package calories;

import model.Activity;
import model.UserProfile;

/**
 * Υπολογίζει θερμίδες με τον ακριβέστερο τύπο που χρησιμοποιεί
 * τους μέσους καρδιακούς παλμούς, την ηλικία και το φύλο.
 *
 * <p>Για <b>άνδρες</b>:</p>
 * <pre>C = (-55.0969 + (0.6309·h) + (0.1966·w) + (0.2017·a)) · t / 4.184</pre>
 *
 * <p>Για <b>γυναίκες</b>:</p>
 * <pre>C = (-20.4022 + (0.4472·h) + (0.1263·w) + (0.074·a)) · t / 4.184</pre>
 *
 * <p>Όπου h = μέσοι καρδιακοί παλμοί (bpm), w = βάρος (kg),
 * a = ηλικία (χρόνια), t = χρόνος άσκησης (λεπτά).</p>
 *
 * <p>Αυτός ο τύπος απαιτεί μετρήσεις καρδιακών παλμών στη δραστηριότητα.
 * Αν δεν υπάρχουν, επιστρέφεται -1.</p>
 *
 * <p>Πηγή: Keytel et al., "Prediction of energy expenditure from heart rate
 * monitoring during submaximal exercise", Journal of Sports Sciences, 2005.</p>
 */
public class HRCalorieCalculator implements CalorieCalculator {

    @Override
    public double calculate(Activity activity, UserProfile userProfile) {
        double avgHR = activity.getAverageHeartRate();

        // Αν δεν υπάρχουν μετρήσεις παλμών, δεν μπορούμε να χρησιμοποιήσουμε αυτόν τον τύπο
        if (avgHR <= 0) return -1;
        if (!userProfile.isCompleteForHRCalories()) return -1;

        double h = avgHR;
        double w = userProfile.getWeightKg();
        double a = userProfile.getAge();
        double t = activity.getTotalDurationSeconds() / 60.0; // σε λεπτά

        double calories;
        if (userProfile.getGender() == UserProfile.Gender.MALE) {
            calories = (-55.0969 + (0.6309 * h) + (0.1966 * w) + (0.2017 * a)) * t / 4.184;
        } else {
            calories = (-20.4022 + (0.4472 * h) + (0.1263 * w) + (0.074  * a)) * t / 4.184;
        }

        // Ο τύπος μπορεί να δώσει αρνητική τιμή σε πολύ χαμηλούς παλμούς – ελάχιστο 0
        return Math.max(0, calories);
    }

    @Override
    public String getName() {
        return "Καρδιακοί παλμοί (Keytel et al.)";
    }
}
