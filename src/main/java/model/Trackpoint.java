package model;

import java.time.ZonedDateTime;

/**
 * Αναπαριστά ένα μεμονωμένο σημείο μέτρησης GPS μέσα σε μια διαδρομή.
 *
 * <p>Το Trackpoint είναι η θεμελιώδης μονάδα δεδομένων που καταγράφεται
 * από αθλητικές συσκευές (GPS ρολόγια, ποδηλατόμετρα κτλ.). Κάθε σημείο
 * αντιστοιχεί σε μια χρονική στιγμή και περιέχει τις τρέχουσες μετρήσεις
 * του αθλητή εκείνη τη στιγμή.</p>
 *
 * <p>Παράδειγμα χρήσης:</p>
 * <pre>
 *   Trackpoint tp = new Trackpoint();
 *   tp.setTimestamp(ZonedDateTime.now());
 *   tp.setHeartRateBpm(145);
 *   tp.setDistanceMeters(1500.0);
 * </pre>
 */
public class Trackpoint {

    /** Ακριβής χρονική στιγμή καταγραφής του σημείου (ISO-8601 με timezone) */
    private ZonedDateTime timestamp;

    /** Γεωγραφικό πλάτος σε μοίρες δεκαδικής μορφής (π.χ. 37.9838) */
    private double latitude;

    /** Γεωγραφικό μήκος σε μοίρες δεκαδικής μορφής (π.χ. 23.7275) */
    private double longitude;

    /** Υψόμετρο πάνω από το επίπεδο της θάλασσας σε μέτρα */
    private double altitudeMeters;

    /**
     * Αθροιστική απόσταση από την αρχή της δραστηριότητας σε μέτρα.
     * Η τιμή αυξάνεται μονότονα από 0.0 στην αρχή μέχρι
     * τη συνολική απόσταση στο τέλος.
     */
    private double distanceMeters;

    /**
     * Καρδιακοί παλμοί ανά λεπτό (bpm).
     * Τιμή 0 σημαίνει ότι δεν υπάρχει διαθέσιμη μέτρηση.
     */
    private int heartRateBpm;

    /**
     * Ρυθμός (βήματα/λεπτό για τρέξιμο, στροφές/λεπτό για ποδήλατο).
     * Τιμή 0 σημαίνει ότι δεν υπάρχει διαθέσιμη μέτρηση.
     */
    private int cadence;

    /** Δημιουργεί ένα κενό Trackpoint με μη-διαθέσιμες προαιρετικές τιμές */
    public Trackpoint() {
        this.heartRateBpm = 0;
        this.cadence = 0;
    }

    // --- Getters και Setters ---

    public ZonedDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getAltitudeMeters() { return altitudeMeters; }
    public void setAltitudeMeters(double altitudeMeters) { this.altitudeMeters = altitudeMeters; }

    public double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(double distanceMeters) { this.distanceMeters = distanceMeters; }

    public int getHeartRateBpm() { return heartRateBpm; }
    public void setHeartRateBpm(int heartRateBpm) { this.heartRateBpm = heartRateBpm; }

    public int getCadence() { return cadence; }
    public void setCadence(int cadence) { this.cadence = cadence; }

    /**
     * Ελέγχει αν υπάρχει μέτρηση καρδιακών παλμών για αυτό το σημείο.
     *
     * @return {@code true} αν οι παλμοί είναι μεγαλύτεροι του 0
     */
    public boolean hasHeartRate() {
        return heartRateBpm > 0;
    }
}
