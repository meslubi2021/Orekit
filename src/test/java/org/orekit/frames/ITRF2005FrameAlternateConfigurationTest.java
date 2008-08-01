/* Copyright 2002-2008 CS Communication & Systèmes
 * Licensed to CS Communication & Systèmes (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orekit.frames;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.math.geometry.Vector3D;
import org.orekit.errors.OrekitException;
import org.orekit.iers.IERSDirectoryCrawler;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.ChunkedDate;
import org.orekit.time.ChunkedTime;
import org.orekit.time.UTCScale;
import org.orekit.utils.PVCoordinates;


public class ITRF2005FrameAlternateConfigurationTest extends TestCase {

    public void testAASReferenceLEO() throws OrekitException {

        // this reference test has been extracted from the following paper:
        // Implementation Issues Surrounding the New IAU Reference Systems for Astrodynamics
        // David A. Vallado, John H. Seago, P. Kenneth Seidelmann
        // http://www.centerforspace.com/downloads/files/pubs/AAS-06-134.pdf
        AbsoluteDate t0 = new AbsoluteDate(new ChunkedDate(2004, 04, 06),
                                           new ChunkedTime(07, 51, 28.386009),
                                           UTCScale.getInstance());

        // Positions LEO
        Frame itrfA = Frame.getITRF2005A();
        Frame itrfB = Frame.getITRF2005C();
        PVCoordinates pvITRF =
            new PVCoordinates(new Vector3D(-1033479.3830, 7901295.2754, 6380356.5958),
                              new Vector3D(-3225.636520, -2872.451450, 5531.924446));

        // Reference coordinates
        PVCoordinates pvGcrfIau2000A =
            new PVCoordinates(new Vector3D(5102508.9579, 6123011.4038, 6378136.9252),
                              new Vector3D(-4743.220156, 790.536497, 5533.755728));
        checkPV(pvGcrfIau2000A,
                itrfA.getTransformTo(Frame.getGCRF(), t0).transformPVCoordinates(pvITRF),
                0.01, 1.6e-5);

        PVCoordinates pvJ2000EqA =
            new PVCoordinates(new Vector3D(5102509.0383, 6123011.9758, 6378136.3118),
                              new Vector3D(-4743.219766, 790.536344, 5533.756084));
        checkPV(pvJ2000EqA,
                itrfA.getTransformTo(Frame.getJ2000(), t0).transformPVCoordinates(pvITRF),
                0.01, 1.6e-5);

        PVCoordinates pvGcrfIau2000B =
            new PVCoordinates(new Vector3D(5102508.9579, 6123011.4012, 6378136.9277),
                              new Vector3D(-4743.220156, 790.536495, 5533.755729));
        checkPV(pvGcrfIau2000B,
                itrfB.getTransformTo(Frame.getGCRF(), t0).transformPVCoordinates(pvITRF),
                0.02, 1.8e-5);

        PVCoordinates pvJ2000EqB =
            new PVCoordinates(new Vector3D(5102509.0383, 6123011.9733, 6378136.3142),
                              new Vector3D(-4743.219766, 790.536342, 5533.756085));
        checkPV(pvJ2000EqB,
                itrfB.getTransformTo(Frame.getJ2000(), t0).transformPVCoordinates(pvITRF),
                0.02, 1.8e-5);

    }

    public void testAASReferenceGEO() throws OrekitException {

        // this reference test has been extracted from the following paper:
        // Implementation Issues Surrounding the New IAU Reference Systems for Astrodynamics
        // David A. Vallado, John H. Seago, P. Kenneth Seidelmann
        // http://www.centerforspace.com/downloads/files/pubs/AAS-06-134.pdf
        AbsoluteDate t0 = new AbsoluteDate(new ChunkedDate(2004, 06, 01),
                                           ChunkedTime.H00,
                                           UTCScale.getInstance());

        //  Positions GEO
        Frame itrfA = Frame.getITRF2005A();
        Frame itrfC = Frame.getITRF2005C();
        PVCoordinates pvITRF =
            new PVCoordinates(new Vector3D(24796919.2915, -34115870.9234, 10226.0621),
                              new Vector3D(-0.979178, -1.476538, -0.928776));


        PVCoordinates pvGCRFiau2000A =
            new PVCoordinates(new Vector3D(-40588150.3617, -11462167.0397, 27143.1974),
                              new Vector3D(834.787458, -2958.305691, -1.172993));
        checkPV(pvGCRFiau2000A,
                itrfA.getTransformTo(Frame.getGCRF(), t0).transformPVCoordinates(pvITRF),
                0.061, 0.5e-5);

        PVCoordinates pvJ2000EqA =
            new PVCoordinates(new Vector3D(-40588149.5482, -11462169.9118, 27146.8462),
                              new Vector3D(834.787667, -2958.305632, -1.172963));
        checkPV(pvJ2000EqA,
                itrfA.getTransformTo(Frame.getJ2000(), t0).transformPVCoordinates(pvITRF),
                0.061, 0.5e-5);

        PVCoordinates pvGCRFiau2000B =
            new PVCoordinates(new Vector3D(-40588150.3617,-11462167.0397, 27143.2125),
                              new Vector3D(834.787458,-2958.305691,-1.172999));

        checkPV(pvGCRFiau2000B,
                itrfC.getTransformTo(Frame.getGCRF(), t0).transformPVCoordinates(pvITRF),
                0.063, 0.7e-5);

        PVCoordinates pvJ2000EqB =
            new PVCoordinates(new Vector3D(-40588149.5481, -11462169.9118, 27146.8613),
                              new Vector3D(834.787667, -2958.305632, -1.172968));
        checkPV(pvJ2000EqB,
                itrfA.getTransformTo(Frame.getJ2000(), t0).transformPVCoordinates(pvITRF),
                0.063, 0.7e-5);

    }

    public void setUp() {
        System.setProperty(IERSDirectoryCrawler.IERS_ROOT_DIRECTORY, "testitrf-data");
    }

    private void checkPV(PVCoordinates reference, PVCoordinates result,
                         double positionThreshold, double velocityThreshold) {

        Vector3D dP = result.getPosition().subtract(reference.getPosition());
        Vector3D dV = result.getVelocity().subtract(reference.getVelocity());
        assertEquals(0, dP.getNorm(), positionThreshold);
        assertEquals(0, dV.getNorm(), velocityThreshold);
    }

    public static Test suite() {
        return new TestSuite(ITRF2005FrameAlternateConfigurationTest.class);
    }

}