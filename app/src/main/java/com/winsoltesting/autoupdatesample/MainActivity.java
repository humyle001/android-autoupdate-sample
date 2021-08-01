package com.winsoltesting.autoupdatesample;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import io.fabric.sdk.android.Fabric;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE = 321;
    private static String TAG_PERMISSION = "permission_check";
    private UserOperations userOperations;
    private NetworkOperations networkOperations;
    private CheckUpdateTask  checkUpdateTask;
    private Context context;
    private AlertDialog alert;
    private Dialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (!Fabric.isInitialized()) {
//            Fabric.with(this, new Crashlytics());
//        }
        setContentView(R.layout.activity_main);
        context = this;
        if (Build.VERSION.SDK_INT >= 23) {
            checkAndRequestPermissions();
        }
        networkOperations = new NetworkOperations( this );
        userOperations = new UserOperations(this);
        TextView textView = findViewById(R.id.version_id);
        textView.setText(AppSettings.APP_VERSION);
//        forceCrash();
        checkUpdate();

    }


    //Permission Handling
    private boolean checkAndRequestPermissions() {
        int externalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int recordAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int phoneStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (externalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (recordAudioPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (phoneStatePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }


        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSIONS_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.d(TAG_PERMISSION, "Permission callback called-------");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.SEND_SMS, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d(TAG_PERMISSION, "all permissions granted");
                        // process the normal flow
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(TAG_PERMISSION, "Some permissions are not granted, ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)
                        ) {
                            showDialogOK("Service Permissions are required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    Intent intent = new Intent(Intent.ACTION_MAIN);
                                                    intent.addCategory(Intent.CATEGORY_HOME);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);

                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            //proceed with logic by disabling the related features or quit the app.

//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                                finishAffinity();
//                            } else {
//                               finish();
//                            }
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    public  void checkUpdate() {
        if( checkUpdateTask == null || checkUpdateTask.getStatus() != AsyncTask.Status.RUNNING ){
            checkUpdateTask = new CheckUpdateTask();
            checkUpdateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    class CheckUpdateTask extends AsyncTask<Integer, Integer, String> {
        private String response="";

        @Override
        protected String doInBackground(Integer... params) {
            // TODO Auto-generated method stub
            response = networkOperations.processUpdate(AppSettings.APP_VERSION_NUMBER,userOperations.getDeviceIMEI());
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(response.equalsIgnoreCase("0") || response.equalsIgnoreCase("")){
                // preEmptiveCheck();
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("An updated version for the application is available. Click here to download and install the latest version for this application. Once the installation is complete, kindly reboot your device for the update changes to take place. Thank you.")
                        .setCancelable(false)
                        .setPositiveButton("OK", (arg0, arg1) -> {
                            alert.hide();
                            runUpdate(response);
                        });
                alert = builder.create();
                alert.show();
            }

        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(progress);
        }
    }

    public void runUpdate(final String response){
        showProgressDialog();
        String split[]=response.split("_");
        String appVersion[] = split[0].split("-");
        // String latestVersionFile = appVersion[1].replace(".apk", "");
//
        String latestVersionFile =  split[0];
        final String destination = Environment.getExternalStoragePublicDirectory(AppSettings.DEVICE_FOLDER+"/updates") + "/"+latestVersionFile+".apk";
        File apk = new File(destination);
        if(apk.exists()){
            apk.delete();
        }
        String url = AppSettings.SERVER_ADDRESS + "rest/user/UpdateApk?operation=device&update-file=" + latestVersionFile+"&appname="+split[1];
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Updating Application.");

        final Uri uri = Uri.parse("file://" +destination);
        request.setDestinationUri(uri);
        // get download service and enqueue file
        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        final long downloadId = manager.enqueue(request);
        //set BroadcastReceiver to install app when .apk is downloaded
        BroadcastReceiver onComplete = new BroadcastReceiver() {

            public void onReceive(Context ctxt, Intent intent) {
                dismissProgressDialog();
                final File toInstall = new File(destination);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {//com.winsoltesting.autoupdatesample.provider
                    Uri apkUri = FileProvider.getUriForFile( context, "com.winsoltesting.autoupdatesample.provider", toInstall);
                    Intent intents = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    intents.setData(apkUri);
                    intents.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(intents);
                } else {
                    Uri apkUri = Uri.fromFile(toInstall);
                    Intent intents = new Intent(Intent.ACTION_VIEW);
                    intents.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intents.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intents);
                }

                context.unregisterReceiver(this);
                finish();
            }
        };
        //register receiver for when .apk download is compete
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void showProgressDialog() {
        progressDialog = new Dialog(MainActivity.this);
        progressDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.processing_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if(progressDialog.isShowing()){
            progressDialog.setCancelable(true);
            progressDialog.dismiss();
        }

    }

    private void forceCrash(){
        int crash = 10/0;
    }
}