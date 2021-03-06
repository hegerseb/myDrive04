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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import funf.Schedule;
import funf.probe.Probe.Base;
import funf.probe.Probe.PassiveProbe;
import funf.probe.Probe.RequiredPermissions;

@Schedule.DefaultSchedule(interval=300)
@RequiredPermissions(android.Manifest.permission.BATTERY_STATS)
public class BatteryProbe extends Base implements PassiveProbe {

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				sendData(getGson().toJsonTree(intent.getExtras()).getAsJsonObject());
				stop();
			}
		}
	};
	
	@Override
	protected void onStart() {
		super.onStart();
		getContext().registerReceiver(receiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}

	@Override
	protected void onStop() {
		super.onStop();
		getContext().unregisterReceiver(receiver);
	}
}
