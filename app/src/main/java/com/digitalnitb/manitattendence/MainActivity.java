package com.digitalnitb.manitattendence;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalnitb.manitattendence.LayoutFragments.ColumnsPagerAdapter;
import com.digitalnitb.manitattendence.Utilities.ColourSeatsUtility;
import com.digitalnitb.manitattendence.Utilities.CommonFunctions;
import com.digitalnitb.manitattendence.Utilities.FirebaseInteractUtility;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    public static final double APP_VERSION = 1.25;

    //Firebase Authentication
    private FirebaseAuth mAuth;

    //Getting the toolbar for other options
    private Toolbar mToolbar;

    //The confirm linear layout
    private LinearLayout mLinearLayout;

    private ViewPager mViewPager;

    private ColumnsPagerAdapter mColumnsPagerAdapter;

    private TabLayout mTabLayout;

    private Toast mToast;

    private ProgressDialog mLoadingProgress;

    private FirebaseDatabase mDb;

    public static boolean allowSubmission = false;

    public static boolean mAttendanceStopped = false;

    //Top display layout and textview
    private LinearLayout mStatusLayout;
    private TextView mStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializing Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance();

        //Setting the toolbar
        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle("MANIT Attendance");

        mLinearLayout = findViewById(R.id.confirm_linear_layout);

        mStatusLayout = findViewById(R.id.main_attendece_display_layout);
        mStatusTextView = findViewById(R.id.main_attendece_display_tv);

        //Tabs
        mViewPager = findViewById(R.id.main_tabPager);
        mTabLayout = findViewById(R.id.main_tabs);
        mColumnsPagerAdapter = new ColumnsPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mColumnsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);


        mLoadingProgress = new ProgressDialog(this);
        mLoadingProgress.setTitle("Loading data...");
        mLoadingProgress.setMessage("Please wait while we load data.");
        mLoadingProgress.setCancelable(false);

        ColourSeatsUtility.instantiate();

        mLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(allowSubmission){
                if (ColourSeatsUtility.mSelectedColumn == -1) {
                    displayToast("Please select a seat first!");
                } else if(FirebaseInteractUtility.mAttended){
                    displayToast("This scholar number already booked attendance");
                }
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setCancelable(false);
                    builder.setTitle("Confirm Bench");
                    builder.setMessage("Please make sure you are marking your row/column of your bench correctly.");
                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, int i) {
                            dialog.cancel();
                            builder.setTitle("Confirm Seat");
                            builder.setMessage("If you are seating single or double on a bench, DO NOT mark the central seat.");
                            builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    builder.setTitle("Final Confirmation");
                                    builder.setMessage("If your attendance is found wrong, " +
                                            "your attendance will be deducted by three and if you do it more than 3 times, " +
                                            "you won't be able to submit your attendance for this subject anymore.");
                                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialog.dismiss();
                                            FirebaseInteractUtility.confirmAttendance(MainActivity.this, mAuth.getCurrentUser().getDisplayName(), ColourSeatsUtility.mSelectedColumn, ColourSeatsUtility.mSelectedPosition);
                                        }
                                    });
                                    builder.show();
                                }
                            });
                            builder.show();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();

                }}else if(mAttendanceStopped){
                    displayToast("Attendance has already stopped.");
                }
                else {
                    displayToast("Attendance not yet started");
                }
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        try{
        currentUser.reload();
        }catch (Exception e){
            currentUser = null;
            e.printStackTrace();
        }

        if (currentUser == null) {
            sendToStart();
        }
    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_btn) {
            mAuth.signOut();
            CommonFunctions.resetAll();
            sendToStart();
            finish();
        }
        if(item.getItemId() == R.id.main_account_settings){
            Intent intent = new Intent(MainActivity.this, AccountSettingsActivity.class);
            startActivity(intent);
        }

        return true;
    }

    public void displayToast(String message) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        mToast.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.getCurrentUser().reload();
        if(!mAuth.getCurrentUser().isEmailVerified()){
            Intent intent = new Intent(MainActivity.this, UnverifiedEmailActivity.class);
            startActivity(intent);
            mAuth = FirebaseAuth.getInstance();
        }
        isOnline();
    }

    private void checkAppVersion(){
        mLoadingProgress.show();
        mDb.getReference().child("APP-VERSION").child("VER").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double version = Double.parseDouble(dataSnapshot.getValue().toString());
                if(version!=APP_VERSION){
                    Intent intent = new Intent(MainActivity.this, AppExpiredActivity.class);
                    mLoadingProgress.dismiss();
                    startActivity(intent);
                    finish();
                }
                else {
                    startActions();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Intent intent = new Intent(MainActivity.this, AppExpiredActivity.class);
                mLoadingProgress.dismiss();
                startActivity(intent);
                finish();
            }
        });
    }

    private void startActions(){
        if(mAuth.getCurrentUser()!=null){
            CommonFunctions.setScholar_number(mAuth.getCurrentUser().getDisplayName());

            String branch = CommonFunctions.getBranch(CommonFunctions.getScholar_number());
            String year = CommonFunctions.getYear(CommonFunctions.getScholar_number());

            DatabaseReference dbRef = mDb.getReference().child("Teacher").child(branch).child(year);

            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getChildrenCount()==0){
                        mLoadingProgress.dismiss();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getChildrenCount()==0){
                        mAttendanceStopped = false;
                        mStatusTextView.setText("Attendance not yet started");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            dbRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    String [] input = dataSnapshot.getValue().toString().split("-");

                    if(input[0].equals("start")){

                        FirebaseInteractUtility.startChecking(mLoadingProgress);
                        allowSubmission = true;
                        mAttendanceStopped = false;
                        mStatusLayout.setBackgroundColor(getResources().getColor(R.color.confirm_green));
                        mStatusTextView.setText(String.format("Attendance started for %s", input[1]));
                        CommonFunctions.resetAll();
                        int setColumn = Integer.parseInt(input[2]);
                        int setRow = Integer.parseInt(input[3]);
                        CommonFunctions.setColumns(setColumn);CommonFunctions.setRows(setRow);
                        CommonFunctions.notifyAllAdapters();
                        mColumnsPagerAdapter.notifyDataSetChanged();
                        mViewPager.setAdapter(mColumnsPagerAdapter);

                    }else if(input[0].equals("stop")){

                        allowSubmission = false;
                        mStatusLayout.setBackgroundColor(getResources().getColor(R.color.attendence_stop));
                        mStatusTextView.setText(String.format("Attendance stopped for %s", input[1]));
                        mAttendanceStopped = true;
                        mLoadingProgress.dismiss();

                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if(netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()){
            alertDialog();
        }else {
            checkAppVersion();
        }

    }

    private void alertDialog(){
        try {
            final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

            alertDialog.setTitle("No Internet");
            alertDialog.setMessage("Internet not available, Cross check your internet connectivity and try again");
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.setButton("Try Again", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.hide();
                    isOnline();
                }
            });
            alertDialog.show();
        }
        catch(Exception e)
        {
            Log.d("MainActivity", "Show Dialog: "+e.getMessage());
        }
    }
}
