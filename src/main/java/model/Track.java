package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Αναπαριστά μια συνεχόμενη ακολουθία GPS σημείων (Trackpoints).
 *
 * <p>Μια διαδρομή (Track) αποτελεί μέρος ενός γύρου (Lap). Συνήθως
 * υπάρχει μία διαδρομή ανά γύρο, αλλά το format TCX επιτρέπει
 * περισσότερες (π.χ. αν η συσκευή επανεκκινήσει την καταγραφή).</p>
 */
public class Track {

    /** Η λίστα με τα σημεία GPS που απαρτίζουν αυτή τη διαδρομή */
    private final List<Trackpoint> trackpoints;

    /** Δημιουργεί μια κενή διαδρομή */
    public Track() {
        this.trackpoints = new ArrayList<>();
    }

    /**
     * Προσθέτει ένα νέο σημείο μέτρησης στη διαδρομή.
     *
     * @param trackpoint το σημείο που θα προστεθεί (δεν πρέπει να είναι null)
     */
    public void addTrackpoint(Trackpoint trackpoint) {
        trackpoints.add(trackpoint);
    }

    /**
     * Επιστρέφει όλα τα σημεία της διαδρομής.
     *
     * @return αμετάβλητη λίστα με τα trackpoints
     */
    public List<Trackpoint> getTrackpoints() {
        return trackpoints;
    }

    /**
     * Υπολογίζει τη διάρκεια αυτής της διαδρομής σε δευτερόλεπτα.
     * Η διάρκεια είναι η διαφορά μεταξύ του τελευταίου και του
     * πρώτου timestamp.
     *
     * @return διάρκεια σε δευτερόλεπτα, ή 0 αν δεν υπάρχουν αρκετά σημεία
     */
    public long getDurationSeconds() {
        if (trackpoints.size() < 2) return 0;
        Trackpoint first = trackpoints.get(0);
        Trackpoint last  = trackpoints.get(trackpoints.size() - 1);
        if (first.getTimestamp() == null || last.getTimestamp() == null) return 0;
        return java.time.Duration.between(first.getTimestamp(), last.getTimestamp()).getSeconds();
    }

    /**
     * Υπολογίζει την απόσταση που καλύπτει αυτή η διαδρομή σε μέτρα.
     * Χρησιμοποιεί την αθροιστική τιμή DistanceMeters του TCX αρχείου.
     *
     * @return απόσταση σε μέτρα, ή 0 αν η διαδρομή είναι κενή
     */
    public double getDistanceMeters() {
        if (trackpoints.isEmpty()) return 0.0;
        double first = trackpoints.get(0).getDistanceMeters();
        double last  = trackpoints.get(trackpoints.size() - 1).getDistanceMeters();
        return Math.max(0, last - first);
    }

    /**
     * Υπολογίζει τον μέσο καρδιακό παλμό σε αυτή τη διαδρομή.
     * Αγνοεί σημεία χωρίς διαθέσιμη μέτρηση (τιμή 0).
     *
     * @return μέσος παλμός σε bpm, ή 0 αν δεν υπάρχουν μετρήσεις
     */
    public double getAverageHeartRate() {
        List<Trackpoint> withHR = trackpoints.stream()
                .filter(Trackpoint::hasHeartRate)
                .collect(java.util.stream.Collectors.toList());
        if (withHR.isEmpty()) return 0;
        return withHR.stream().mapToInt(Trackpoint::getHeartRateBpm).average().orElse(0);
    }

    /**
     * Βρίσκει τον μέγιστο καρδιακό παλμό σε αυτή τη διαδρομή.
     *
     * @return μέγιστος παλμός σε bpm, ή 0 αν δεν υπάρχουν μετρήσεις
     */
    public int getMaxHeartRate() {
        return trackpoints.stream()
                .filter(Trackpoint::hasHeartRate)
                .mapToInt(Trackpoint::getHeartRateBpm)
                .max().orElse(0);
    }
}
