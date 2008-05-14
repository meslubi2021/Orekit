package fr.cs.orekit.bodies;

import org.apache.commons.math.util.MathUtils;

/** Point location relative to a 2D body surface.
 * <p>This class is a simple immutable container,
 * it does not provide any processing method.</p>
 * @see BodyShape
 * @author Luc Maisonobe
 */
public class GeodeticPoint {

    /** Longitude of the point (rad). */
    private final double longitude;

    /** Latitude of the point (rad). */
    private final double latitude;

    /** Altitude of the point (m). */
    private final double altitude;

    /** Build a new instance.
     * @param longitude longitude of the point
     * @param latitude of the point
     * @param altitude altitude of the point
     */
    public GeodeticPoint(final double longitude, final double latitude,
                         final double altitude) {
        this.longitude = MathUtils.normalizeAngle(longitude, 0.);
        this.latitude  = MathUtils.normalizeAngle(latitude, 0.);
        this.altitude  = altitude;
    }

    /** Get the longitude.
     * @return longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /** Get the latitude.
     * @return latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /** Get the altitude.
     * @return altitude
     */
    public double getAltitude() {
        return altitude;
    }

}
