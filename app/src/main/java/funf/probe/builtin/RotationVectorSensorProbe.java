/**
 * 
 * Funf: Open Sensing Framework
 * Copyright (C) 2010-2011 Nadav Aharony, Wei Pan, Alex Pentland.
 * Acknowledgments: Alan Gardner
 * Contact: nadav@media.mit.edu
 * 
 * This file is part of Funf.
 * 
 * Funf is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Funf is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with Funf. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package funf.probe.builtin;

import funf.Schedule;
import funf.probe.Probe.Description;
import funf.probe.Probe.RequiredFeatures;
import funf.probe.builtin.ProbeKeys.RotationVectorSensorKeys;

@Description("Returns sensor distance in centimeters or only a binary near/far measurement.")
@RequiredFeatures("android.hardware.sensor.gyroscope")
@Schedule.DefaultSchedule(interval=300, duration=15)
public class RotationVectorSensorProbe extends SensorProbe implements RotationVectorSensorKeys {

	public int getSensorType() {
		return 11;  //SensorKeys.TYPE_ROTATION_VECTOR; // API Level 9
	}
	
	public String[] getValueNames() {
		return new String[] {
			RotationVectorSensorKeys.X_SIN_THETA_OVER_2, RotationVectorSensorKeys.Y_SIN_THETA_OVER_2, RotationVectorSensorKeys.Z_SIN_THETA_OVER_2, RotationVectorSensorKeys.COS_THETA_OVER_2
		};
	}

}
