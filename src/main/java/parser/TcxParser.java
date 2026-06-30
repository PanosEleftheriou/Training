package parser;

import model.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/** Αναλύει αρχεία TCX (Training Center XML) και δημιουργεί αντικείμενα Activity. */
public class TcxParser {

    public List<Activity> parse(File file) throws Exception {
        List<Activity> activities = new ArrayList<Activity>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        doc.getDocumentElement().normalize();
        NodeList activityNodes = doc.getElementsByTagName("Activity");
        for (int i = 0; i < activityNodes.getLength(); i++) {
            Activity activity = parseActivity((Element) activityNodes.item(i));
            if (activity != null) activities.add(activity);
        }
        return activities;
    }

    private Activity parseActivity(Element activityEl) {
        String sport = activityEl.getAttribute("Sport");
        ZonedDateTime startTime = null;
        NodeList idNodes = activityEl.getElementsByTagName("Id");
        if (idNodes.getLength() > 0) {
            startTime = ZonedDateTime.parse(idNodes.item(0).getTextContent().trim());
        }
        Activity activity = createActivity(sport, startTime);
        if (activity == null) return null;
        NodeList lapNodes = activityEl.getElementsByTagName("Lap");
        for (int i = 0; i < lapNodes.getLength(); i++) {
            if (lapNodes.item(i).getParentNode() == activityEl) {
                activity.addLap(parseLap((Element) lapNodes.item(i)));
            }
        }
        return activity;
    }

    private Activity createActivity(String sport, ZonedDateTime startTime) {
        if (startTime == null) startTime = ZonedDateTime.now();
        String s = sport.toLowerCase();
        if (s.equals("running")) return new RunningActivity(startTime);
        if (s.equals("biking") || s.equals("cycling")) return new CyclingActivity(startTime);
        if (s.equals("walking")) return new WalkingActivity(startTime);
        if (s.equals("swimming")) return new SwimmingActivity(startTime);
        RunningActivity ra = new RunningActivity(startTime);
        ra.setSport(sport);
        return ra;
    }

    private Lap parseLap(Element lapEl) {
        ZonedDateTime startTime = null;
        String startAttr = lapEl.getAttribute("StartTime");
        if (!startAttr.isEmpty()) startTime = ZonedDateTime.parse(startAttr);
        Lap lap = new Lap(startTime);
        NodeList trackNodes = lapEl.getElementsByTagName("Track");
        for (int i = 0; i < trackNodes.getLength(); i++) {
            lap.addTrack(parseTrack((Element) trackNodes.item(i)));
        }
        return lap;
    }

    private Track parseTrack(Element trackEl) {
        Track track = new Track();
        NodeList tpNodes = trackEl.getElementsByTagName("Trackpoint");
        for (int i = 0; i < tpNodes.getLength(); i++) {
            Trackpoint tp = parseTrackpoint((Element) tpNodes.item(i));
            if (tp != null) track.addTrackpoint(tp);
        }
        return track;
    }

    private Trackpoint parseTrackpoint(Element tpEl) {
        Trackpoint tp = new Trackpoint();
        String timeStr = getTagValue(tpEl, "Time");
        if (timeStr == null) return null;
        tp.setTimestamp(ZonedDateTime.parse(timeStr));
        String lat = getTagValue(tpEl, "LatitudeDegrees");
        String lon = getTagValue(tpEl, "LongitudeDegrees");
        if (lat != null) tp.setLatitude(Double.parseDouble(lat));
        if (lon != null) tp.setLongitude(Double.parseDouble(lon));
        String alt = getTagValue(tpEl, "AltitudeMeters");
        if (alt != null) tp.setAltitudeMeters(Double.parseDouble(alt));
        String dist = getTagValue(tpEl, "DistanceMeters");
        if (dist != null) tp.setDistanceMeters(Double.parseDouble(dist));
        NodeList hrNodes = tpEl.getElementsByTagName("HeartRateBpm");
        if (hrNodes.getLength() > 0) {
            String hrValue = getTagValue((Element) hrNodes.item(0), "Value");
            if (hrValue != null) tp.setHeartRateBpm(Integer.parseInt(hrValue));
        }
        String cad = getTagValue(tpEl, "Cadence");
        if (cad != null) tp.setCadence(Integer.parseInt(cad));
        return tp;
    }

    private String getTagValue(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) return null;
        Node node = nodes.item(0).getFirstChild();
        if (node == null) return null;
        return node.getNodeValue().trim();
    }
}
