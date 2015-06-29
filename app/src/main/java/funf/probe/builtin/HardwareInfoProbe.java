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

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import funf.Schedule;
import funf.probe.Probe.RequiredPermissions;
import funf.probe.builtin.ProbeKeys.HardwareInfoKeys;

@Schedule.DefaultSchedule(interval=604800)
@RequiredPermissions({android.Manifest.permission.ACCESS_WIFI_STATE, android.Manifest.permission.BLUETOOTH, android.Manifest.permission.READ_PHONE_STATE})
public class HardwareInfoProbe extends ImpulseProbe implements HardwareInfoKeys {

	@Override
	protected void onStart() {
		super.onStart();
		sendData(getGson().toJsonTree(getData()).getAsJsonObject());
		stop();
	}

	
	private Bundle getData() {
		Context context = getContext();
		Bundle data = new Bundle();
		data.putString(HardwareInfoKeys.WIFI_MAC, ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getMacAddress());
		String bluetoothMac = getBluetoothMac();
		if (bluetoothMac != null) {
			data.putString(HardwareInfoKeys.BLUETOOTH_MAC, bluetoothMac);
		}
		data.putString(HardwareInfoKeys.ANDROID_ID, Secure.getString(context.getContentResolver(), Secure.ANDROID_ID));
		data.putString(HardwareInfoKeys.BRAND, Build.BRAND);
		data.putString(HardwareInfoKeys.MODEL, Build.MODEL);
		data.putString(HardwareInfoKeys.DEVICE_ID, ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId());
		return data;
	}

	private String getBluetoothMac() {
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		return (adapter != null) ? adapter.getAddress() : null;
	}
}
