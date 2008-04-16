package fr.cs.orekit.propagation.analytical;

import org.apache.commons.math.util.MathUtils;

import fr.cs.orekit.attitudes.AttitudeKinematicsProvider;
import fr.cs.orekit.attitudes.models.IdentityAttitude;
import fr.cs.orekit.errors.OrekitException;
import fr.cs.orekit.errors.PropagationException;
import fr.cs.orekit.orbits.CircularParameters;
import fr.cs.orekit.orbits.Orbit;
import fr.cs.orekit.orbits.OrbitalParameters;
import fr.cs.orekit.propagation.AttitudePropagator;
import fr.cs.orekit.propagation.Ephemeris;
import fr.cs.orekit.propagation.SpacecraftState;
import fr.cs.orekit.time.AbsoluteDate;

/** This class propagates a {@link fr.cs.orekit.propagation.SpacecraftState}
 *  using the analytical Eckstein-Hechler model.
 * <p>The Eckstein-Hechler model is suited for near circular orbits
 * (e < 0.1, with poor accuracy between 0.005 and 0.1) and inclination
 * neither equatorial (direct or retrograde) nor critical (direct or
 * retrograde).</p>
 * @see Orbit
 * @author G. Prat
 */
public class EcksteinHechlerPropagator implements Ephemeris, AttitudePropagator {

    /** Serializable UID. */
    private static final long serialVersionUID = 4924905512408375026L;

    /** Attitude provider */
    private AttitudeKinematicsProvider akProvider;

    /** Initial date. */
    private final AbsoluteDate initialDate;

    /** Mean parameters at the initial date. */
    private CircularParameters mean;

    /** Preprocessed values. */
    private double q;
    private double ql;
    private double g2;
    private double g3;
    private double g4;
    private double g5;
    private double g6;
    private double cosI1;
    private double sinI1;
    private double sinI2;
    private double sinI4;
    private double sinI6;

    /** Model parameters. */
    private double referenceRadius;
    private double mu;
    private double c20;
    private double c30;
    private double c40;
    private double c50;
    private double c60;
    private double mass;

    /** Create a new instance.
     * <p>The C<sub>n,0</sub> coefficients are the denormalized zonal coefficients, they
     * are related to both the normalized coefficients
     * <span style="text-decoration: overline">C</span><sub>n,0</sub>
     *  and the J<sub>n</sub> one as follows:</p>
     * <pre>
     *   C<sub>n,0</sub> = [(2-&delta;<sub>0,m</sub>)(2n+1)(n-m)!/(n+m)!]<sup>&frac12;</sup><span style="text-decoration: overline">C</span><sub>n,0</sub>
     *   C<sub>n,0</sub> = -J<sub>n</sub>
     * </pre>
     * @param initialState initial state
     * @param referenceRadius reference radius of the Earth for the potential model (m)
     * @param mu central attraction coefficient (m<sup>3</sup>/s<sup>2</sup>)
     * @param c20 un-normalized zonal coefficient (about -1.08e-3 for Earth)
     * @param c30 un-normalized zonal coefficient (about +2.53e-6 for Earth)
     * @param c40 un-normalized zonal coefficient (about +1.62e-6 for Earth)
     * @param c50 un-normalized zonal coefficient (about +2.28e-7 for Earth)
     * @param c60 un-normalized zonal coefficient (about -5.41e-7 for Earth)
     * @exception PropagationException if the mean parameters cannot be computed
     */
    public EcksteinHechlerPropagator(SpacecraftState initialState, double referenceRadius, double mu,
                                     double c20, double c30, double c40,
                                     double c50, double c60)
        throws PropagationException {

        // store model coefficients
        this.referenceRadius = referenceRadius;
        this.mu  = mu;
        this.c20 = c20;
        this.c30 = c30;
        this.c40 = c40;
        this.c50 = c50;
        this.c60 = c60;

        // transformation into circular adapted parameters
        // (used by the Eckstein-Hechler model)
        final CircularParameters osculating = new CircularParameters(initialState.getParameters(), mu);

        // compute mean parameters
        initialDate = initialState.getDate();
        mass = initialState.getMass();
        this.akProvider = new IdentityAttitude();
        computeMeanParameters(osculating);

    }

    /** Get the state extrapolated up to a given date with an analytical model.
     * The extrapolated parameters are osculating circular parameters.
     * @param date target date for the propagation
     * @return propagated state (in circular parameters)
     * @exception PropagationException if orbit or attitude cannot be
     * propagated at given date
     */
    public SpacecraftState getSpacecraftState(AbsoluteDate date)
        throws PropagationException {
        final OrbitalParameters op = propagate(date);
        try {
            return new SpacecraftState(new Orbit(date, op), mass,
                                       akProvider.getAttitudeKinematics(date,
                                                                        op.getPVCoordinates(mu), op.getFrame()));
        } catch (OrekitException oe) {
            throw new PropagationException(oe.getMessage(), oe);
        }
    }

    /** Compute mean parameters according to the Eckstein-Hechler analytical model.
     * @param osculating osculating orbit
     * @exception PropagationException
     */
    private void computeMeanParameters(CircularParameters osculating)
        throws PropagationException {

        // sanity check
        if (osculating.getA() < referenceRadius) {
            throw new PropagationException("trajectory inside the Brillouin sphere (r = {0})",
                                           new Object[] { new Double(osculating.getA()) });
        }

        // rough initialization of the mean parameters
        mean = new CircularParameters(osculating , mu);

        // threshold for each parameter
        final double epsilon         = 1.0e-13;
        final double thresholdA      = epsilon * (1 + Math.abs(mean.getA()));
        final double thresholdE      = epsilon * (1 + mean.getE());
        final double thresholdAngles = epsilon * Math.PI;

        int i = 0;
        while (i++ < 100) {

            // preliminary processing
            q = referenceRadius / mean.getA();
            ql = q * q;
            g2 = c20 * ql;
            ql *= q;
            g3 = c30 * ql;
            ql *= q;
            g4 = c40 * ql;
            ql *= q;
            g5 = c50 * ql;
            ql *= q;
            g6 = c60 * ql;

            cosI1 = Math.cos(mean.getI());
            sinI1 = Math.sin(mean.getI());
            sinI2 = sinI1 * sinI1;
            sinI4 = sinI2 * sinI2;
            sinI6 = sinI2 * sinI4;

            // recompute the osculation parameters from the current mean parameters
            final CircularParameters rebuilt = propagate(initialDate);

            // adapted parameters residuals
            final double deltaA      = osculating.getA()  - rebuilt.getA();
            final double deltaEx     = osculating.getCircularEx() - rebuilt.getCircularEx();
            final double deltaEy     = osculating.getCircularEy() - rebuilt.getCircularEy();
            final double deltaI      = osculating.getI()  - rebuilt.getI();
            final double deltaRAAN   = MathUtils.normalizeAngle(osculating.getRightAscensionOfAscendingNode() -
                                                 rebuilt.getRightAscensionOfAscendingNode(),
                                                 0.0);
            final double deltaAlphaM = MathUtils.normalizeAngle(osculating.getAlphaM() - rebuilt.getAlphaM(), 0.0);

            // update mean parameters
            mean= new CircularParameters(mean.getA()          + deltaA,
                                         mean.getCircularEx() + deltaEx,
                                         mean.getCircularEy() + deltaEy,
                                         mean.getI()          + deltaI,
                                         mean.getRightAscensionOfAscendingNode() + deltaRAAN,
                                         mean.getAlphaM()     + deltaAlphaM,
                                         CircularParameters.MEAN_LONGITUDE_ARGUMENT,
                                         mean.getFrame());

            // check convergence
            if ((Math.abs(deltaA)         < thresholdA) &&
                (Math.abs(deltaEx)     < thresholdE) &&
                (Math.abs(deltaEy)     < thresholdE) &&
                (Math.abs(deltaI)      < thresholdAngles) &&
                (Math.abs(deltaRAAN)   < thresholdAngles) &&
                (Math.abs(deltaAlphaM) < thresholdAngles)) {

                // sanity checks
                final double e = mean.getE();
                if (e > 0.1) {
                    // if 0.005 < e < 0.1 no error is triggered, but accuracy is poor
                    throw new PropagationException("too excentric orbit (e = {0})",
                                                   new Object[] { new Double(e) });
                }

                final double meanI = mean.getI();
                if ((meanI < 0.) || (meanI > Math.PI) || (Math.abs(Math.sin(meanI)) < 1.0e-10)) {
                    throw new PropagationException("almost equatorial orbit (i = {0} degrees)",
                                                   new Object[] {
                                                       new Double(Math.toDegrees(meanI))
                                                   });
                }

                if ((Math.abs(meanI - 1.1071487) < 1.0e-3) || (Math.abs(meanI - 2.0344439) < 1.0e-3)) {
                    throw new PropagationException("almost critically inclined orbit (i = {0} degrees)",
                                                   new Object[] {
                                                       new Double(Math.toDegrees(meanI))
                                                   });
                }

                return;

            }

        }

        throw new PropagationException("unable to compute Eckstein-Hechler mean" +
                                       " parameters after {0} iterations",
                                       new Object[] { new Integer(i) });

    }

    /** Extrapolate an orbit up to a specific target date.
     * @param targetDate target date for the orbit
     * @exception PropagationException if some parameters are out of bounds
     */
    private CircularParameters propagate(AbsoluteDate date)
        throws PropagationException {

        // keplerian evolution
        final double xnot = date.minus(initialDate) * Math.sqrt(mu / mean.getA()) / mean.getA();

        // secular effects

        // eccentricity
        final double rdpom = -0.75 * g2 * (4.0 - 5.0 * sinI2);
        final double rdpomp = 7.5 * g4 * (1.0 - 31.0 / 8.0 * sinI2 + 49.0 / 16.0 * sinI4) -
                              13.125 * g6 * (1.0 - 8.0 * sinI2 + 129.0 / 8.0 * sinI4 - 297.0 / 32.0 * sinI6);
        final double x = (rdpom + rdpomp) * xnot;
        final double cx = Math.cos(x);
        final double sx = Math.sin(x);
        q = 3.0 / (32.0 * rdpom);
        final double eps1 =
            q * g4 * sinI2 * (30.0 - 35.0 * sinI2) -
            175.0 * q * g6 * sinI2 * (1.0 - 3.0 * sinI2 + 2.0625 * sinI4);
        q = 3.0 * sinI1 / (8.0 * rdpom);
        final double eps2 =
            q * g3 * (4.0 - 5.0 * sinI2) - q * g5 * (10.0 - 35.0 * sinI2 + 26.25 * sinI4);
        final double exm = mean.getCircularEx() * cx - (1.0 - eps1) * mean.getCircularEy() * sx + eps2 * sx;
        final double eym = (1.0 + eps1) * mean.getCircularEx() * sx + (mean.getCircularEy() - eps2) * cx + eps2;

        // inclination
        final double xim = mean.getI();

        // right ascension of ascending node
        q = 1.50 * g2 - 2.25 * g2 * g2 * (2.5 - 19.0 / 6.0 * sinI2) +
            0.9375 * g4 * (7.0 * sinI2 - 4.0) +
            3.28125 * g6 * (2.0 - 9.0 * sinI2 + 8.25 * sinI4);
        final double omm =
            MathUtils.normalizeAngle(mean.getRightAscensionOfAscendingNode() + q * cosI1 * xnot, Math.PI);

        // latitude argument
        final double rdl = 1.0 - 1.50 * g2 * (3.0 - 4.0 * sinI2);
        q = rdl +
            2.25 * g2 * g2 * (9.0 - 263.0 / 12.0 * sinI2 + 341.0 / 24.0 * sinI4) +
            15.0 / 16.0 * g4 * (8.0 - 31.0 * sinI2 + 24.5 * sinI4) +
            105.0 / 32.0 * g6 * (-10.0 / 3.0 + 25.0 * sinI2 - 48.75 * sinI4 + 27.5 * sinI6);
        final double xlm = MathUtils.normalizeAngle(mean.getAlphaM()+ q * xnot, Math.PI);

        // periodical terms
        final double cl1 = Math.cos(xlm);
        final double sl1 = Math.sin(xlm);
        final double cl2 = cl1 * cl1 - sl1 * sl1;
        final double sl2 = cl1 * sl1 + sl1 * cl1;
        final double cl3 = cl2 * cl1 - sl2 * sl1;
        final double sl3 = cl2 * sl1 + sl2 * cl1;
        final double cl4 = cl3 * cl1 - sl3 * sl1;
        final double sl4 = cl3 * sl1 + sl3 * cl1;
        final double cl5 = cl4 * cl1 - sl4 * sl1;
        final double sl5 = cl4 * sl1 + sl4 * cl1;
        final double cl6 = cl5 * cl1 - sl5 * sl1;

        final double qq = -1.5 * g2 / rdl;
        final double qh = 0.375 * (eym - eps2) / rdpom;
        ql = 0.375 * exm / (sinI1 * rdpom);

        // semi major axis
        double f = (2.0 - 3.5 * sinI2) * exm * cl1 +
                   (2.0 - 2.5 * sinI2) * eym * sl1 +
                   sinI2 * cl2 +
                   3.5 * sinI2 * (exm * cl3 + eym * sl3);
        double rda = qq * f;

        q = 0.75 * g2 * g2 * sinI2;
        f = 7.0 * (2.0 - 3.0 * sinI2) * cl2 + sinI2 * cl4;
        rda += q * f;

        q = -0.75 * g3 * sinI1;
        f = (4.0 - 5.0 * sinI2) * sl1 + 5.0 / 3.0 * sinI2 * sl3;
        rda += q * f;

        q = 0.25 * g4 * sinI2;
        f = (15.0 - 17.5 * sinI2) * cl2 + 4.375 * sinI2 * cl4;
        rda += q * f;

        q = 3.75 * g5 * sinI1;
        f = (2.625 * sinI4 - 3.5 * sinI2 + 1.0) * sl1 +
            7.0 / 6.0 * sinI2 * (1.0 - 1.125 * sinI2) * sl3 +
            21.0 / 80.0 * sinI4 * sl5;
        rda += q * f;

        q = 105.0 / 16.0 * g6 * sinI2;
        f = (3.0 * sinI2 - 1.0 - 33.0 / 16.0 * sinI4) * cl2 +
            0.75 * (1.1 * sinI4 - sinI2) * cl4 -
            11.0 / 80.0 * sinI4 * cl6;
        rda += q * f;

        // eccentricity
        f = (1.0 - 1.25 * sinI2) * cl1 +
            0.5 * (3.0 - 5.0 * sinI2) * exm * cl2 +
            (2.0 - 1.5 * sinI2) * eym * sl2 +
            7.0 / 12.0 * sinI2 * cl3 +
            17.0 / 8.0 * sinI2 * (exm * cl4 + eym * sl4);
        final double rdex = qq * f;

        f = (1.0 - 1.75 * sinI2) * sl1 +
            (1.0 - 3.0 * sinI2) * exm * sl2 +
            (2.0 * sinI2 - 1.5) * eym * cl2 +
            7.0 / 12.0 * sinI2 * sl3 +
            17.0 / 8.0 * sinI2 * (exm * sl4 - eym * cl4);
        final double rdey = qq * f;

        // ascending node
        q = -qq * cosI1;
        f = 3.5 * exm * sl1 -
            2.5 * eym * cl1 -
            0.5 * sl2 +
            7.0 / 6.0 * (eym * cl3 - exm * sl3);
        double rdom = q * f;

        f = g3 * cosI1 * (4.0 - 15.0 * sinI2);
        rdom += ql * f;

        f = 2.5 * g5 * cosI1 * (4.0 - 42.0 * sinI2 + 52.5 * sinI4);
        rdom -= ql * f;

        // inclination
        q = 0.5 * qq * sinI1 * cosI1;
        f = eym * sl1 - exm * cl1 + cl2 + 7.0 / 3.0 * (exm * cl3 + eym * sl3);
        double rdxi = q * f;

        f = g3 * cosI1 * (4.0 - 5.0 * sinI2);
        rdxi -= qh * f;

        f = 2.5 * g5 * cosI1 * (4.0 - 14.0 * sinI2 + 10.5 * sinI4);
        rdxi += qh * f;

        // latitude argument
        f = (7.0 - 77.0 / 8.0 * sinI2) * exm * sl1 +
            (55.0 / 8.0 * sinI2 - 7.50) * eym * cl1 +
            (1.25 * sinI2 - 0.5) * sl2 +
            (77.0 / 24.0 * sinI2 - 7.0 / 6.0) * (exm * sl3 - eym * cl3);
        double rdxl = qq * f;

        f = g3 * (53.0 * sinI2 - 4.0 - 57.5 * sinI4);
        rdxl += ql * f;

        f = 2.5 * g5 * (4.0 - 96.0 * sinI2 + 269.5 * sinI4 - 183.75 * sinI6);
        rdxl += ql * f;

        // osculating parameters
        return new CircularParameters(mean.getA() * (1.0 + rda), exm + rdex, eym + rdey,
                                      xim + rdxi, MathUtils.normalizeAngle(omm + rdom, Math.PI),
                                      MathUtils.normalizeAngle(xlm + rdxl, Math.PI),
                                      CircularParameters.MEAN_LONGITUDE_ARGUMENT, mean.getFrame());

    }

    public void setAkProvider(AttitudeKinematicsProvider akProvider) {
        this.akProvider = akProvider;
    }

}