/* Copyright 2002-2016 CS Systèmes d'Information
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
package org.orekit.forces.drag;

import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
import org.apache.commons.math3.geometry.euclidean.threed.FieldRotation;
import org.apache.commons.math3.geometry.euclidean.threed.FieldVector3D;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.time.AbsoluteDate;

/** Interface for spacecraft that are sensitive to atmospheric drag forces.
 *
 * @see DragForce
 * @author Luc Maisonobe
 * @author Pascal Parraud
 */
public interface DragSensitive {

    /** Parameter name for drag coefficient enabling jacobian processing. */
    String DRAG_COEFFICIENT = "drag coefficient";

    /** Compute the acceleration due to drag.
     * <p>
     * The computation includes all spacecraft specific characteristics
     * like shape, area and coefficients.
     * </p>
     * @param date current date
     * @param frame inertial reference frame for state (both orbit and attitude)
     * @param position position of spacecraft in reference frame
     * @param rotation orientation (attitude) of the spacecraft with respect to reference frame
     * @param mass current mass
     * @param density atmospheric density at spacecraft position
     * @param relativeVelocity relative velocity of atmosphere with respect to spacecraft,
     * in the same inertial frame as spacecraft orbit (m/s)
     * @return spacecraft acceleration in the same inertial frame as spacecraft orbit (m/s²)
     * @throws OrekitException if acceleration cannot be computed
     */
    Vector3D dragAcceleration(AbsoluteDate date, Frame frame, Vector3D position,
                              Rotation rotation, double mass,
                              double density, Vector3D relativeVelocity)
        throws OrekitException;

    /** Compute the acceleration due to drag, with state derivatives.
     * <p>
     * The computation includes all spacecraft specific characteristics
     * like shape, area and coefficients.
     * </p>
     * @param date current date
     * @param frame inertial reference frame for state (both orbit and attitude)
     * @param position position of spacecraft in reference frame
     * @param rotation orientation (attitude) of the spacecraft with respect to reference frame
     * @param mass spacecraft mass
     * @param density atmospheric density at spacecraft position
     * @param relativeVelocity relative velocity of atmosphere with respect to spacecraft,
     * in the same inertial frame as spacecraft orbit (m/s)
     * @return spacecraft acceleration in the same inertial frame as spacecraft orbit (m/s²)
     * @throws OrekitException if acceleration cannot be computed
     */
    FieldVector3D<DerivativeStructure> dragAcceleration(AbsoluteDate date, Frame frame, FieldVector3D<DerivativeStructure> position,
                                                        FieldRotation<DerivativeStructure> rotation, DerivativeStructure mass,
                                                        DerivativeStructure density, FieldVector3D<DerivativeStructure> relativeVelocity)
        throws OrekitException;

    /** Compute acceleration due to drag, with parameters derivatives.
     * @param date current date
     * @param frame inertial reference frame for state (both orbit and attitude)
     * @param position position of spacecraft in reference frame
     * @param rotation orientation (attitude) of the spacecraft with respect to reference frame
     * @param mass current mass
     * @param density atmospheric density at spacecraft position
     * @param relativeVelocity relative velocity of atmosphere with respect to spacecraft,
     * in the same inertial frame as spacecraft orbit (m/s)
     * @param paramName name of the parameter with respect to which derivatives are required
     * @return spacecraft acceleration in the same inertial frame as spacecraft orbit (m/s²)
     * @exception OrekitException if derivatives cannot be computed
     */
    FieldVector3D<DerivativeStructure> dragAcceleration(AbsoluteDate date, Frame frame, Vector3D position,
                                Rotation rotation, double mass,
                                double density, Vector3D relativeVelocity, String paramName)
        throws OrekitException;

    /** Set the drag coefficient.
     *  @param value drag coefficient
     */
    void setDragCoefficient(double value);

    /** Get the drag coefficient.
     * @return drag coefficient
     */
    double getDragCoefficient();

}
