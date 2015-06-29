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

import android.content.Context;
import android.telephony.TelephonyManager;

import com.google.gson.JsonObject;

import funf.Schedule;
import funf.data.DataNormalizer.PhoneNumberNormalizer;
import funf.probe.Probe.DisplayName;
import funf.probe.Probe.RequiredFeatures;
import funf.probe.Probe.RequiredPermissions;
import funf.probe.builtin.ProbeKeys.TelephonyKeys;

@RequiredPermissions(android.Manifest.permission.READ_PHONE_STATE)
@RequiredFeatures("android.hardware.telephony")
@Schedule.DefaultSchedule(interval=604800)
@DisplayName("Mobile Network Info Probe")
public class TelephonyProbe extends ImpulseProbe implements TelephonyKeys {

	@Override
	protected void onStart() {
		super.onStart();
		TelephonyManager telephony = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
		JsonObject data = new JsonObject();
		data.addProperty(TelephonyKeys.CALL_STATE, telephony.getCallState());
		data.addProperty(TelephonyKeys.DEVICE_ID, telephony.getDeviceId());
		data.addProperty(TelephonyKeys.DEVICE_SOFTWARE_VERSION, telephony.getDeviceSoftwareVersion());
		data.addProperty(TelephonyKeys.LINE_1_NUMBER, sensitiveData(telephony.getLine1Number(), new PhoneNumberNormalizer()));
		data.addProperty(TelephonyKeys.NETWORK_COUNTRY_ISO, telephony.getNetworkCountryIso());
		data.addProperty(TelephonyKeys.NETWORK_OPERATOR, telephony.getNetworkOperator());
		data.addProperty(TelephonyKeys.NETWORK_OPERATOR_NAME, telephony.getNetworkOperatorName());
		data.addProperty(TelephonyKeys.NETWORK_TYPE, telephony.getNetworkType());
		data.addProperty(TelephonyKeys.PHONE_TYPE, telephony.getPhoneType());
		data.addProperty(TelephonyKeys.SIM_COUNTRY_ISO, telephony.getSimCountryIso());
		data.addProperty(TelephonyKeys.SIM_OPERATOR, telephony.getSimOperator());
		data.addProperty(TelephonyKeys.SIM_OPERATOR_NAME, telephony.getSimOperatorName());
		data.addProperty(TelephonyKeys.SIM_SERIAL_NUMBER, telephony.getSimSerialNumber()); // TODO: Should this be hashed?
		data.addProperty(TelephonyKeys.SIM_STATE, telephony.getSimState());
		data.addProperty(TelephonyKeys.SUBSCRIBER_ID, telephony.getSubscriberId());  // TODO: Should this be hashed?
		data.addProperty(TelephonyKeys.VOICEMAIL_ALPHA_TAG, telephony.getVoiceMailAlphaTag());
		data.addProperty(TelephonyKeys.VOICEMAIL_NUMBER, telephony.getVoiceMailNumber());
		data.addProperty(TelephonyKeys.HAS_ICC_CARD, telephony.hasIccCard());
		sendData(data);
		stop();
	}

	
	
}
