package com.winsoltesting.autoupdatesample;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NetworkOperations {
	private Context context;
	public static String ERROR_STRING = "Unable to communicate with the server at the moment. Please try again later.";
	
	public NetworkOperations(Context context) {
		this.context = context;
	}
	
	public NetworkType getNetworkType() {
		NetworkType networkType = null;

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (ni.isConnected()) {
					networkType = NetworkType.WIFI;
					break;
				}
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
				if (ni.isConnected()) {
					networkType = NetworkType.GPRS;
					break;
				}
		}
		return networkType;
	}
	
	public String pushToServer(List<NameValuePair> parameterValues, String pageAddress) {
		String response = null;

		try {
			////Better use AsyncTask
			if(android.os.Build.VERSION.SDK_INT>9){
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
				StrictMode.setThreadPolicy(policy);
			}
			/////
			HttpParams httpParams = new BasicHttpParams();

	        HttpConnectionParams.setConnectionTimeout(httpParams, 40000);
	        HttpConnectionParams.setSoTimeout(httpParams, 40000);
	        
			DefaultHttpClient httpClient = new DefaultHttpClient( httpParams );
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			
//			HttpGet httpPost = new HttpGet(SERVER_ADDRESS + pageAddress);
			HttpPost httpPost = new HttpPost(AppSettings.SERVER_ADDRESS  + pageAddress);
			httpPost.setParams( httpParams );
			
			List<NameValuePair> nameValuePairs = parameterValues;
			nameValuePairs.add(new BasicNameValuePair("origin-android", "true"));
			httpPost.setEntity(new UrlEncodedFormEntity( nameValuePairs ,"UTF-8"));
			response = httpClient.execute(httpPost, responseHandler);
			Log.d("debug", "NetworkOpeartions:PushToServer84: Response: "+response);
//			Log.e("Server Response", response);

		} catch (Exception ex) {
			return null;
		}
		return response;
	}
	


	public String processUpdate(String version, String imei) {

		String response;
		try {
			float VERSION_CODE= Float.parseFloat(version);
			File updateFolder = new File( Environment.getExternalStorageDirectory().getPath() + "/"+AppSettings.DEVICE_FOLDER+"/updates" );
			if(updateFolder.exists()){
				updateFolder.delete();
			}
			if( getNetworkType() != null ) {

				List<NameValuePair> nvp = new ArrayList<>();
				nvp.add(new BasicNameValuePair("operation"	, "check"));
				nvp.add(new BasicNameValuePair("imei"		, imei));
				nvp.add(new BasicNameValuePair("appname"		, AppSettings.APP_FOR));

				response = pushToServer(nvp, "rest/user/checkForUpdate");

				if( response != null  && !response.isEmpty()) {
					if(response.contains(AppSettings.APP_FOR)) {
						if(!response.contains(AppSettings.APP_VERSION_NUMBER)){
							return response;
						}
					}
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return "0";
	}
}
