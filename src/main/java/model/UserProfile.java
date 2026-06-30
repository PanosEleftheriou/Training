package model;

/**
 * Αποθηκεύει τα προσωπικά δεδομένα του χρήστη που χρειάζονται
 * για τον υπολογισμό θερμίδων και την εκτίμηση φυσικής κατάστασης.
 *
 * <p>Τα δεδομένα αυτά χρησιμοποιούνται από:</p>
 * <ul>
 *   <li>{@link calories.HRCalorieCalculator} – τύπος με καρδιακούς παλμούς</li>
 *   <li>{@link vo2max.VO2MaxEstimator} – εκτίμηση VO2 Max</li>
 * </ul>
 */
public class UserProfile {

    /** Φύλο χρήστη */
    public enum Gender { MALE, FEMALE }

    /** Φύλο του χρήστη (MALE ή FEMALE) */
    private Gender gender;

    /** Ηλικία σε χρόνια */
    private int age;

    /** Βάρος σε κιλόγραμμα */
    private double weightKg;

    /**
     * Παλμοί ηρεμίας (Resting Heart Rate) σε bpm.
     * Χρησιμοποιείται για τον υπολογισμό VO2 Max.
     * Τυπική τιμή για ενήλικα: 60-100 bpm.
     */
    private int restingHeartRate;

    /** Ημερήσιος στόχος θερμίδων (0 σημαίνει χωρίς στόχο) */
    private double dailyCalorieGoal;

    /**
     * Δημιουργεί ένα προφίλ χρήστη με προεπιλεγμένες τιμές.
     * Ο χρήστης πρέπει να τις ενημερώσει πριν από τους υπολογισμούς.
     */
    public UserProfile() {
        this.gender = Gender.MALE;
        this.age = 30;
        this.weightKg = 70.0;
        this.restingHeartRate = 65;
        this.dailyCalorieGoal = 0;
    }

    // --- Getters και Setters ---

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double weightKg) { this.weightKg = weightKg; }

    public int getRestingHeartRate() { return restingHeartRate; }
    public void setRestingHeartRate(int restingHeartRate) { this.restingHeartRate = restingHeartRate; }

    public double getDailyCalorieGoal() { return dailyCalorieGoal; }
    public void setDailyCalorieGoal(double dailyCalorieGoal) { this.dailyCalorieGoal = dailyCalorieGoal; }

    /**
     * Υπολογίζει τον μέγιστο καρδιακό παλμό (MHR) με βάση την ηλικία.
     * Χρησιμοποιείται ο τύπος: MHR = 220 - ηλικία
     *
     * @return εκτιμώμενος MHR σε bpm
     */
    public int getMaxHeartRate() {
        return 220 - age;
    }

    /**
     * Ελέγχει αν το προφίλ είναι αρκετά πλήρες για υπολογισμό θερμίδων
     * με τον τύπο καρδιακών παλμών.
     *
     * @return {@code true} αν υπάρχουν βάρος, ηλικία και φύλο
     */
    public boolean isCompleteForHRCalories() {
        return weightKg > 0 && age > 0 && gender != null;
    }
}
