package model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Αναπαριστά έναν γύρο (Lap) μέσα σε μια αθλητική δραστηριότητα.
 *
 * <p>Ένας γύρος μπορεί να αντιπροσωπεύει ένα χιλιόμετρο σε τρέξιμο,
 * ένα "σετ" στο γυμναστήριο, ή οποιαδήποτε άλλη λογική υποδιαίρεση
 * της δραστηριότητας. Κάθε γύρος αποτελείται από μία ή περισσότερες
 * διαδρομές ({@link Track}).</p>
 */
public class Lap {

    /** Η ώρα έναρξης αυτού του γύρου */
    private ZonedDateTime startTime;

    /** Οι διαδρομές που απαρτίζουν τον γύρο (συνήθως μία) */
    private final List<Track> tracks;

    /**
     * Δημιουργεί έναν κενό γύρο.
     *
     * @param startTime η ώρα έναρξης του γύρου
     */
    public Lap(ZonedDateTime startTime) {
        this.startTime = startTime;
        this.tracks = new ArrayList<>();
    }

    /**
     * Προσθέτει μια διαδρομή σε αυτόν τον γύρο.
     *
     * @param track η διαδρομή που θα προστεθεί
     */
    public void addTrack(Track track) {
        tracks.add(track);
    }

    public ZonedDateTime getStartTime() { return startTime; }
    public void setStartTime(ZonedDateTime startTime) { this.startTime = startTime; }

    public List<Track> getTracks() { return tracks; }

    /**
     * Επιστρέφει όλα τα trackpoints από όλες τις διαδρομές αυτού του γύρου.
     * Βολικό για υπολογισμούς που χρειάζονται πρόσβαση σε κάθε μέτρηση.
     *
     * @return λίστα με όλα τα trackpoints του γύρου σε σειρά
     */
    public List<Trackpoint> getAllTrackpoints() {
        List<Trackpoint> all = new ArrayList<>();
        for (Track t : tracks) {
            all.addAll(t.getTrackpoints());
        }
        return all;
    }

    /**
     * Υπολογίζει τη συνολική διάρκεια του γύρου αθροίζοντας
     * τις διάρκειες όλων των διαδρομών του.
     *
     * @return συνολική διάρκεια σε δευτερόλεπτα
     */
    public long getDurationSeconds() {
        return tracks.stream().mapToLong(Track::getDurationSeconds).sum();
    }

    /**
     * Υπολογίζει τη συνολική απόσταση του γύρου αθροίζοντας
     * τις αποστάσεις όλων των διαδρομών του.
     *
     * @return συνολική απόσταση σε μέτρα
     */
    public double getDistanceMeters() {
        return tracks.stream().mapToDouble(Track::getDistanceMeters).sum();
    }

    /**
     * Υπολογίζει τον μέσο καρδιακό παλμό σε όλη τη διάρκεια του γύρου.
     * Λαμβάνει υπόψη μόνο τα trackpoints με έγκυρη μέτρηση.
     *
     * @return μέσος παλμός σε bpm, ή 0 αν δεν υπάρχουν μετρήσεις
     */
    public double getAverageHeartRate() {
        List<Trackpoint> all = getAllTrackpoints();
        List<Trackpoint> withHR = all.stream().filter(Trackpoint::hasHeartRate).collect(java.util.stream.Collectors.toList());
        if (withHR.isEmpty()) return 0;
        return withHR.stream().mapToInt(Trackpoint::getHeartRateBpm).average().orElse(0);
    }

    /**
     * Βρίσκει τον μέγιστο καρδιακό παλμό κατά τη διάρκεια του γύρου.
     *
     * @return μέγιστος παλμός σε bpm, ή 0 αν δεν υπάρχουν μετρήσεις
     */
    public int getMaxHeartRate() {
        return tracks.stream().mapToInt(Track::getMaxHeartRate).max().orElse(0);
    }
}
