package com.winsoltesting.autoupdatesample;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class UserOperations {
    Context context;

    public UserOperations(Context context) {
        this.context = context;
    }


	public String getDeviceIMEI() {
		String serviceName = Context.TELEPHONY_SERVICE;
		String imei = "0";
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(serviceName);
		if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
				!= PackageManager.PERMISSION_GRANTED) {
			// We do not have this permission. Let's ask the user
		}else{
			imei = telephonyManager.getDeviceId();
		}
		return imei;
	}



}

