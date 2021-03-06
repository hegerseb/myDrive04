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
package funf.util;

import static funf.util.AsyncSharedPrefs.async;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;

public class UuidUtil {

	public static String getInstallationId(Context context) {
		if (UuidUtil.uuid == null) {
			SharedPreferences prefs = async(context.getSharedPreferences(UuidUtil.FUNF_UTILS_PREFS, Context.MODE_PRIVATE));
			UuidUtil.uuid = prefs.getString(UuidUtil.INSTALLATION_UUID_KEY, null);
			if (UuidUtil.uuid == null) {
				UuidUtil.uuid = UUID.randomUUID().toString();
				prefs.edit().putString(UuidUtil.INSTALLATION_UUID_KEY, UuidUtil.uuid).commit();
			}
		}
		return UuidUtil.uuid;
	}

	public static String uuid = null;
	public static final String INSTALLATION_UUID_KEY = "INSTALLATION_UUID";
	public static final String FUNF_UTILS_PREFS = "edu.mit.media.funf.Utils";

}
