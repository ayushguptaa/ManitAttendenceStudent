package com.digitalnitb.manitattendence;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class AppExpiredActivity extends AppCompatActivity {

    // button to show progress dialog
    Button btnShowProgress;
    private Button btnDownloadGdrive;

    // Progress Dialog
    private ProgressDialog pDialog;

    // Progress dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;

    private static final String TAG = AppExpiredActivity.class.getSimpleName();

    private StorageReference mStorageRef;

    private ProgressDialog mLoadingProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_expired);

        // show progress bar button
        btnShowProgress = (Button) findViewById(R.id.btnProgressBar);
        btnDownloadGdrive = findViewById(R.id.btnGdrive);
        /**
         * Show Progress bar click event
         * */
        mLoadingProgress = new ProgressDialog(AppExpiredActivity.this);

        mStorageRef = FirebaseStorage.getInstance().getReference().child("ManitAttendance.apk");

        btnShowProgress.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // starting new Async Task
                if(isStoragePermissionGranted()){
                    if(isOnline()){
                        mLoadingProgress.setTitle("Getting download url...");
                        mLoadingProgress.setMessage("Please wait while we download new version.");
                        mLoadingProgress.setCancelable(false);
                        mLoadingProgress.show();
                    mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            mLoadingProgress.dismiss();
                            new DownloadFileFromURL().execute(uri.toString());
                        }
                    });}}

            }
        });

        btnDownloadGdrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isOnline()){
                    mLoadingProgress.setTitle("Getting download url...");
                    mLoadingProgress.setMessage("Please wait while we download new version.");
                    mLoadingProgress.setCanceledOnTouchOutside(false);
                    mLoadingProgress.show();
                    FirebaseDatabase.getInstance().getReference().child("APP-VERSION").child("Drive").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String url = dataSnapshot.getValue().toString();
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            Intent browserChooserIntent = Intent.createChooser(browserIntent , "Download Manit Attendance");
                            mLoadingProgress.hide();
                            startActivity(browserChooserIntent);
                            FirebaseAuth.getInstance().signOut();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        });
    }
    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            Toast.makeText(AppExpiredActivity.this,"Please check your Internet connection!",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            if(isOnline()){
                mLoadingProgress.setTitle("Getting download url...");
                mLoadingProgress.setMessage("Please wait while we download new version.");
                mLoadingProgress.setCanceledOnTouchOutside(false);
                mLoadingProgress.show();
                mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        mLoadingProgress.dismiss();
                        new DownloadFileFromURL().execute(uri.toString());
                    }
                });}
        }else {
            Toast.makeText(AppExpiredActivity.this, "We can't download file without write permissions.", Toast.LENGTH_SHORT).show();;
        }
    }



    private class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread
         * Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(AppExpiredActivity.this);
            pDialog.setMessage("Downloading new version. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setMax(100);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                URL url = new URL(f_url[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                // getting file length
                int lenghtOfFile = conection.getContentLength();

                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream to write file
                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/download/" + "ManitAttendance.apk");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress(""+(int)((total*100)/lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task
         * Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            FirebaseAuth.getInstance().signOut();
            pDialog.dismiss();
            try {
                File toInstall = new File(Environment.getExternalStorageDirectory() + "/download/" + "ManitAttendance.apk");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri apkUri = FileProvider.getUriForFile(AppExpiredActivity.this, AppExpiredActivity.this.getApplicationContext().getPackageName() + ".fileProvider", toInstall);

                    Intent downloadIntent = new Intent(Intent.ACTION_VIEW);
                    downloadIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    downloadIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    downloadIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");

                    List<ResolveInfo> resInfoList = AppExpiredActivity.this.getPackageManager().queryIntentActivities(downloadIntent, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        AppExpiredActivity.this.grantUriPermission(packageName, apkUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                } else {
                    Uri apkUri = Uri.fromFile(toInstall);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }catch (Exception e){
                Toast.makeText(AppExpiredActivity.this, "Unable to start installer, please follow the above instructions", Toast.LENGTH_LONG).show();
            }

        }
}}
