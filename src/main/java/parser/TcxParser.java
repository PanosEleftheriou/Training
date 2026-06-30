package parser;

import model.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Αναλύει αρχεία TCX (Training Center XML) και δημιουργεί αντικείμενα
 * {@link Activity} με τα αντίστοιχα δεδομένα.
 *
 * <p>Το format TCX χρησιμοποιείται από συσκευές Garmin και άλλες αθλητικές
 * συσκευές GPS. Είναι ένα XML format που περιγράφει δραστηριότητες ιεραρχικά:
 * Activities → Activity → Lap → Track → Trackpoint</p>
 *
 * <p>Παράδειγμα χρήσης:</p>
 * <pre>
 *   TcxParser parser = new TcxParser();
 *   List&lt;Activity&gt; activities = parser.parse(new File("run.tcx"));
 * </pre>
 */
public class TcxParser {

    /**
     * Αναλύει ένα αρχείο TCX και επιστρέφει τη λίστα των δραστηριοτήτων
     * που βρέθηκαν σε αυτό.
     *
     * @param file το αρχείο TCX προς ανάλυση
     * @return λίστα με τις δραστηριότητες (μπορεί να είναι κενή)
     * @throws Exception αν το αρχείο δεν βρεθεί ή έχει λανθασμένη μορφή XML
     */
    public List<Activity> parse(File file) throws Exception {
        List<Activity> activities = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Απενεργοποίηση DTD validation για ταχύτερη ανάλυση
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();

        // Ψάχνουμε για όλα τα στοιχεία <Activity> στο αρχείο
        NodeList activityNodes = doc.getElementsByTagName("Activity");

        for (int i = 0; i < activityNodes.getLength(); i++) {
            Element activityEl = (Element) activityNodes.item(i);
            Activity activity = parseActivity(activityEl);
            if (activity != null) {
                activities.add(activity);
            }
        }

        return activities;
    }

    /**
     * Δημιουργεί το κατάλληλο αντικείμενο Activity από ένα XML Element.
     * Επιλέγει την υποκλάση (Running, Cycling κτλ.) βάσει του Sport attribute.
     *
     * @param activityEl το XML element που αντιστοιχεί σε μία δραστηριότητα
     * @return το αντικείμενο Activity, ή null αν δεν αναγνωρίζεται ο τύπος
     */
    private Activity parseActivity(Element activityEl) {
        String sport = activityEl.getAttribute("Sport");

        // Διαβάζουμε τον χρόνο έναρξης (Id tag)
        ZonedDateTime startTime = null;
        NodeList idNodes = activityEl.getElementsByTagName("Id");
        if (idNodes.getLength() > 0) {
            String timeStr = idNodes.item(0).getTextContent().trim();
            startTime = ZonedDateTime.parse(timeStr);
        }

        // Δημιουργούμε την κατάλληλη υποκλάση με βάση τον τύπο άθλησης
        Activity activity = createActivity(sport, startTime);
        if (activity == null) return null;

        // Αναλύουμε όλους τους γύρους της δραστηριότητας
        NodeList lapNodes = activityEl.getElementsByTagName("Lap");
        for (int i = 0; i < lapNodes.getLength(); i++) {
            // Βεβαιωνόμαστε ότι ο Lap είναι άμεσο παιδί του Activity
            if (lapNodes.item(i).getParentNode() == activityEl) {
                Element lapEl = (Element) lapNodes.item(i);
                Lap lap = parseLap(lapEl);
                activity.addLap(lap);
            }
        }

        return activity;
    }

    /**
     * Επιλέγει και δημιουργεί την υποκλάση Activity που ταιριάζει
     * στον τύπο άθλησης από το TCX αρχείο.
     *
     * @param sport     το string τύπου άθλησης (π.χ. "Running", "Biking")
     * @param startTime η ώρα έναρξης
     * @return το αντικείμενο, ή generic Activity αν ο τύπος δεν αναγνωρίζεται
     */
    private Activity createActivity(String sport, ZonedDateTime startTime) {
        if (startTime == null) startTime = ZonedDateTime.now();

        return switch (sport.toLowerCase()) {
            case "running" -> new RunningActivity(startTime);
            case "biking", "cycling" -> new CyclingActivity(startTime);
            case "walking" -> new WalkingActivity(startTime);
            case "swimming" -> new SwimmingActivity(startTime);
            default -> {
                // Άγνωστος τύπος: δημιουργούμε Running ως fallback
                RunningActivity ra = new RunningActivity(startTime);
                ra.setSport(sport);
                yield ra;
            }
        };
    }

    /**
     * Αναλύει ένα Lap XML element και επιστρέφει το αντίστοιχο {@link Lap} αντικείμενο.
     *
     * @param lapEl το XML element του γύρου
     * @return ο αναλυμένος γύρος
     */
    private Lap parseLap(Element lapEl) {
        ZonedDateTime startTime = null;
        String startAttr = lapEl.getAttribute("StartTime");
        if (!startAttr.isEmpty()) {
            startTime = ZonedDateTime.parse(startAttr);
        }

        Lap lap = new Lap(startTime);

        // Αναλύουμε τις διαδρομές μέσα στον γύρο
        NodeList trackNodes = lapEl.getElementsByTagName("Track");
        for (int i = 0; i < trackNodes.getLength(); i++) {
            Element trackEl = (Element) trackNodes.item(i);
            Track track = parseTrack(trackEl);
            lap.addTrack(track);
        }

        return lap;
    }

    /**
     * Αναλύει ένα Track XML element και δημιουργεί το αντίστοιχο {@link Track}.
     *
     * @param trackEl το XML element της διαδρομής
     * @return η αναλυμένη διαδρομή με όλα τα trackpoints
     */
    private Track parseTrack(Element trackEl) {
        Track track = new Track();

        NodeList tpNodes = trackEl.getElementsByTagName("Trackpoint");
        for (int i = 0; i < tpNodes.getLength(); i++) {
            Element tpEl = (Element) tpNodes.item(i);
            Trackpoint tp = parseTrackpoint(tpEl);
            if (tp != null) {
                track.addTrackpoint(tp);
            }
        }

        return track;
    }

    /**
     * Αναλύει ένα Trackpoint XML element.
     * Κάθε πεδίο διαβάζεται ανεξάρτητα – αν λείπει, αγνοείται.
     *
     * @param tpEl το XML element του trackpoint
     * @return το Trackpoint, ή null αν δεν υπάρχει timestamp
     */
    private Trackpoint parseTrackpoint(Element tpEl) {
        Trackpoint tp = new Trackpoint();

        // Χρονική στιγμή (υποχρεωτική)
        String timeStr = getTagValue(tpEl, "Time");
        if (timeStr == null) return null;
        tp.setTimestamp(ZonedDateTime.parse(timeStr));

        // Γεωγραφικές συντεταγμένες (προαιρετικές)
        String lat = getTagValue(tpEl, "LatitudeDegrees");
        String lon = getTagValue(tpEl, "LongitudeDegrees");
        if (lat != null) tp.setLatitude(Double.parseDouble(lat));
        if (lon != null) tp.setLongitude(Double.parseDouble(lon));

        // Υψόμετρο
        String alt = getTagValue(tpEl, "AltitudeMeters");
        if (alt != null) tp.setAltitudeMeters(Double.parseDouble(alt));

        // Αθροιστική απόσταση
        String dist = getTagValue(tpEl, "DistanceMeters");
        if (dist != null) tp.setDistanceMeters(Double.parseDouble(dist));

        // Καρδιακοί παλμοί (nested: HeartRateBpm/Value)
        NodeList hrNodes = tpEl.getElementsByTagName("HeartRateBpm");
        if (hrNodes.getLength() > 0) {
            String hrValue = getTagValue((Element) hrNodes.item(0), "Value");
            if (hrValue != null) tp.setHeartRateBpm(Integer.parseInt(hrValue));
        }

        // Ρυθμός (cadence)
        String cad = getTagValue(tpEl, "Cadence");
        if (cad != null) tp.setCadence(Integer.parseInt(cad));

        return tp;
    }

    /**
     * Βοηθητική μέθοδος για την εξαγωγή κειμένου από ένα XML tag.
     *
     * @param parent  το γονικό element
     * @param tagName το όνομα του tag προς αναζήτηση
     * @return το κείμενο του tag, ή null αν το tag δεν υπάρχει
     */
    private String getTagValue(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) return null;
        // Παίρνουμε μόνο το πρώτο στοιχείο για αποφυγή ανεπιθύμητων αποτελεσμάτων
        Node node = nodes.item(0).getFirstChild();
        if (node == null) return null;
        return node.getNodeValue().trim();
    }
}
