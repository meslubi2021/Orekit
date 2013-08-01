/* Copyright 2002-2013 CS Systèmes d'Information
 * Licensed to CS Systèmes d'Information (CS) under one or more
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.orekit.Utils;
import org.orekit.errors.OrekitException;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.IERSConventions;


public class EOP2000HistoryTest {

    @Test
    public void testRegular() throws OrekitException {
        AbsoluteDate date = new AbsoluteDate(2004, 1, 4, TimeScalesFactory.getUTC());
        double dt = FramesFactory.getEOPHistory(IERSConventions.IERS_2010).getUT1MinusUTC(date);
        Assert.assertEquals(-0.3906070, dt, 1.0e-10);
    }

    @Test
    public void testOutOfRange() throws OrekitException {
        EOPHistory history = FramesFactory.getEOPHistory(IERSConventions.IERS_2010);
        AbsoluteDate endDate = new AbsoluteDate(2006, 3, 5, TimeScalesFactory.getUTC());
        for (double t = -1000; t < 1000 ; t += 3) {
            AbsoluteDate date = endDate.shiftedBy(t);
            double dt = history.getUT1MinusUTC(date);
            if (t <= 0) {
                Assert.assertTrue(dt < 0.29236);
                Assert.assertTrue(dt > 0.29233);
            } else {
                // no more data after end date
                Assert.assertEquals(0.0, dt, 1.0e-10);
            }
        }
    }

    @Test
    public void testUTCLeap() throws OrekitException {
        EOPHistory history = FramesFactory.getEOPHistory(IERSConventions.IERS_2003);
        AbsoluteDate endLeap = new AbsoluteDate(2006, 1, 1, TimeScalesFactory.getUTC());
        for (double dt = -200; dt < 200; dt += 3) {
            final AbsoluteDate date = endLeap.shiftedBy(dt);
            double dtu1 = history.getUT1MinusUTC(date);
            if (dt <= 0) {
                Assert.assertEquals(-0.6612, dtu1, 3.0e-5);
            } else {
                Assert.assertEquals(0.3388, dtu1, 3.0e-5);
            }
        }
    }

    @Before
    public void setUp() {
        Utils.setDataRoot("regular-data");
    }

}
