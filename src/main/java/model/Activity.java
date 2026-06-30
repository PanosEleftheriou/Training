package model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Αφηρημένη βασική κλάση για όλες τις αθλητικές δραστηριότητες.
 *
 * <p>Κάθε δραστηριότητα αναπαριστά μία ολόκληρη προπόνηση (π.χ. ένα τρέξιμο
 * ή μια ποδηλασία). Αποτελείται από έναν ή περισσότερους γύρους ({@link Lap}).
 * Οι υποκλάσεις εξειδικεύουν τον τύπο της δραστηριότητας και μπορούν να
 * προσθέτουν επιπλέον χαρακτηριστικά.</p>
 *
 * <p>Η ιεραρχία υπολογισμών λειτουργεί από κάτω προς τα πάνω:</p>
 * <pre>
 *   Activity → αθροίζει Laps
 *   Lap      → αθροίζει Tracks
 *   Track    → αθροίζει Trackpoints
 * </pre>
 *
 * @see RunningActivity
 * @see CyclingActivity
 * @see WalkingActivity
 * @see SwimmingActivity
 */
public abstract class Activity {

    /** Ο τύπος άθλησης όπως αναγράφεται στο TCX αρχείο (π.χ. "Running") */
    private String sport;

    /** Ώρα έναρξης της δραστηριότητας (ID tag στο TCX) */
    private ZonedDateTime startTime;

    /** Λίστα με τους γύρους που αποτελούν τη δραστηριότητα */
    private final List<Lap> laps;

    /**
     * Δημιουργεί μια νέα δραστηριότητα.
     *
     * @param sport     ο τύπος άθλησης (π.χ. "Running", "Biking")
     * @param startTime η ώρα έναρξης της δραστηριότητας
     */
    protected Activity(String sport, ZonedDateTime startTime) {
        this.sport = sport;
        this.startTime = startTime;
        this.laps = new ArrayList<>();
    }

    /**
     * Προσθέτει έναν γύρο στη δραστηριότητα.
     *
     * @param lap ο γύρος που θα προστεθεί
     */
    public void addLap(Lap lap) {
        laps.add(lap);
    }

    // --- Βασικά getters ---

    public String getSport() { return sport; }
    public void setSport(String sport) { this.sport = sport; }

    public ZonedDateTime getStartTime() { return startTime; }
    public void setStartTime(ZonedDateTime startTime) { this.startTime = startTime; }

    public List<Lap> getLaps() { return laps; }

    // --- Υπολογισμός στατιστικών (ιεραρχικά) ---

    /**
     * Υπολογίζει τη συνολική διάρκεια της δραστηριότητας αθροίζοντας
     * τις διάρκειες όλων των γύρων της.
     *
     * @return συνολική διάρκεια σε δευτερόλεπτα
     */
    public long getTotalDurationSeconds() {
        return laps.stream().mapToLong(Lap::getDurationSeconds).sum();
    }

    /**
     * Υπολογίζει τη συνολική απόσταση της δραστηριότητας αθροίζοντας
     * τις αποστάσεις όλων των γύρων της.
     *
     * @return συνολική απόσταση σε μέτρα
     */
    public double getTotalDistanceMeters() {
        return laps.stream().mapToDouble(Lap::getDistanceMeters).sum();
    }

    /**
     * Υπολογίζει τον μέσο καρδιακό παλμό σε ολόκληρη τη δραστηριότητα.
     * Συγκεντρώνει όλα τα trackpoints με έγκυρη μέτρηση HR.
     *
     * @return μέσος παλμός σε bpm, ή 0 αν δεν υπάρχουν μετρήσεις
     */
    public double getAverageHeartRate() {
        List<Trackpoint> all = getAllTrackpoints();
        List<Trackpoint> withHR = all.stream().filter(Trackpoint::hasHeartRate).toList();
        if (withHR.isEmpty()) return 0;
        return withHR.stream().mapToInt(Trackpoint::getHeartRateBpm).average().orElse(0);
    }

    /**
     * Βρίσκει τον μέγιστο καρδιακό παλμό σε ολόκληρη τη δραστηριότητα.
     *
     * @return μέγιστος παλμός σε bpm, ή 0 αν δεν υπάρχουν μετρήσεις
     */
    public int getMaxHeartRate() {
        return laps.stream().mapToInt(Lap::getMaxHeartRate).max().orElse(0);
    }

    /**
     * Υπολογίζει τη μέση ωριαία ταχύτητα σε km/h.
     *
     * @return μέση ταχύτητα σε km/h, ή 0 αν δεν υπάρχουν δεδομένα
     */
    public double getAverageSpeedKmh() {
        double hours = getTotalDurationSeconds() / 3600.0;
        double km    = getTotalDistanceMeters() / 1000.0;
        if (hours <= 0) return 0;
        return km / hours;
    }

    /**
     * Υπολογίζει τον μέσο ρυθμό σε λεπτά ανά χιλιόμετρο (min/km).
     * Χρησιμοποιείται κυρίως για τρέξιμο και περπάτημα.
     *
     * @return μέσος ρυθμός σε min/km, ή 0 αν δεν υπάρχουν δεδομένα
     */
    public double getAveragePaceMinPerKm() {
        double km      = getTotalDistanceMeters() / 1000.0;
        double minutes = getTotalDurationSeconds() / 60.0;
        if (km <= 0) return 0;
        return minutes / km;
    }

    /**
     * Συλλέγει όλα τα trackpoints από όλους τους γύρους της δραστηριότητας.
     * Χρησιμοποιείται εσωτερικά για υπολογισμούς που χρειάζονται
     * πρόσβαση σε κάθε μεμονωμένη μέτρηση.
     *
     * @return λίστα με όλα τα trackpoints σε χρονολογική σειρά
     */
    public List<Trackpoint> getAllTrackpoints() {
        List<Trackpoint> all = new ArrayList<>();
        for (Lap lap : laps) {
            all.addAll(lap.getAllTrackpoints());
        }
        return all;
    }

    /**
     * Επιστρέφει ένα αναγνωρίσιμο όνομα για τη δραστηριότητα (στα Ελληνικά).
     * Οι υποκλάσεις μπορούν να επικαλύψουν αυτή τη μέθοδο για πιο
     * εξειδικευμένα ονόματα.
     *
     * @return το εμφανιζόμενο όνομα της δραστηριότητας
     */
    public abstract String getDisplayName();

    /**
     * Ελέγχει αν αυτή η δραστηριότητα μετράει απόσταση.
     * Κολύμπι και γυμναστήριο μπορεί να μην έχουν GPS απόσταση.
     *
     * @return {@code true} αν η δραστηριότητα έχει έγκυρη απόσταση
     */
    public boolean hasDistance() {
        return getTotalDistanceMeters() > 0;
    }
}
