package fr.cs.aerospace.orekit.tle;

import fr.cs.aerospace.orekit.errors.OrekitException;
import fr.cs.aerospace.orekit.time.AbsoluteDate;
import fr.cs.aerospace.orekit.time.TTScale;
import fr.cs.aerospace.orekit.time.UTCScale;

/** This class contains methods to compute propagated coordinates with the SDP4 model.
 * 
 * @author SPACETRACK Report #3 project. Felix R. Hoots, Ronald L. Roehrich, December 1980 (original fortran)
 * @author Revisiting Spacetrack Report #3. David A. Vallado, Paul Crawford, Richard Hujsak, T.S. Kelso (C++ translation and improvements)
 * @author Fabien Maussion (Java translation)
 */
abstract class SDP4  extends TLEPropagator {

  /** Constructor for a unique initial TLE.
   * @param initialTLE the TLE to propagate.
   * @throws OrekitException if some specific error occurs
   */
  protected SDP4(TLE initialTLE) throws OrekitException {
    super (initialTLE);
  }
  
  /** Initialization proper to each propagator (SGP or SDP).
   * @param tSince the offset from initial epoch (min)
   * @throws OrekitException when UTC time steps can't be read
   */
  protected void sxpInitialize() throws OrekitException {
    luniSolarTermsComputation();
  }  // End of initialization

  /** Propagation proper to each propagator (SGP or SDP).
   * @param tSince the offset from initial epoch (min)
   */
  protected void sxpPropagate(double tSince) throws OrekitException {

    // Update for secular gravity and atmospheric drag
    omgadf = tle.getPerigeeArgument() + omgdot * tSince;
    double xnoddf = tle.getRaan() + xnodot * tSince;
    double tSinceSq = tSince * tSince;
    xnode = xnoddf + xnodcf * tSinceSq;
    xn = xn0dp;

    // Update for deep-space secular effects
    xll = tle.getMeanAnomaly() + xmdot * tSince;

    deepSecularEffects(tSince);

    double tempa = 1 - c1 * tSince;    
    a = Math.pow(TLEConstants.xke/xn, TLEConstants.twoThirds)*tempa*tempa;
    em -= tle.getBStar()*c4*tSince;
    
    // Update for deep-space periodic effects
    xll += xn0dp * t2cof * tSinceSq;

    deepPeriodicEffects(tSince);

    xl = xll + omgadf + xnode;
    
    // Dundee change:  Reset cosio,  sinio for new xinc:
    cosi0 = Math.cos(xinc);
    sini0 = Math.sin(xinc);
    e = em;
    i = xinc;
    omega = omgadf;
    // end of calculus, go for PV computation
  }

  /** Computes SPACETRACK#3 compliant earth rotation angle.
   * @param date the current date
   * @return the ERA (rad)
   * @throws OrekitException when UTC time steps can't be read
   */
  protected static double thetaG(AbsoluteDate date) throws OrekitException {

    // Reference:  The 1992 Astronomical Almanac, page B6.
    double omega_E = 1.00273790934;
    double jd = (date.minus(AbsoluteDate.JulianEpoch)+
          date.timeScalesOffset(UTCScale.getInstance(), TTScale.getInstance()))/86400;
      
    // Earth rotations per sidereal day (non-constant)

    double UT = (jd + .5)%1;
    double seconds_per_day = 86400.;
    double jd_2000 = 2451545.0;   /* 1.5 Jan 2000 = JD 2451545. */
    double t_cen, GMST, rval;

    t_cen = (jd - UT - jd_2000) / 36525.;
    GMST = 24110.54841 + t_cen * (8640184.812866 + t_cen *
        (0.093104 - t_cen * 6.2E-6));
    GMST = (GMST + seconds_per_day * omega_E * UT)%seconds_per_day;
    if( GMST < 0.) GMST += seconds_per_day;
    rval = 2 * Math.PI * GMST / seconds_per_day;

    return( rval);
    
  }  

  /** Computes luni - solar terms from initial coordinates and epoch. 
   * @throws OrekitException when UTC time steps can't be read
   */
  protected abstract void luniSolarTermsComputation() throws OrekitException;
      
  /** Computes secular terms from current coordinates and epoch. 
   * @param t offset from initial epoch (min)
   */
  protected abstract void deepSecularEffects(double t);
  
  /** Computes periodic terms from current coordinates and epoch. 
   * @param t offset from initial epoch (min)
   */
  protected abstract void deepPeriodicEffects(double t);
  
  /** Params to determine for PV computation. */
  protected double omgadf; // new perigee argument
  protected double xn; // new mean motion
  protected double xll; // parameter for xl computation
  protected double em; // new eccentricity
  protected double xinc; // new inclination
  
}