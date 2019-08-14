package com.digitalnitb.manitattendence;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalnitb.manitattendence.Utilities.CommonFunctions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout mSchNumber;
    private TextInputLayout mPassword;

    private TextView mForgotPass;

    private Button mLoginBtn;

    private Toast mToast;

    private Toolbar mToolbar;

    //Getting Realtime Database
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    //Scholar Refrence
    private DatabaseReference mDbReference;

    //Firebase Auth
    private FirebaseAuth mAuth;

    private ProgressDialog mLoginProgress;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Toolbar set
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login to your Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mLoginProgress = new ProgressDialog(this);

        //Firebase Auth Intitalize
        mAuth = FirebaseAuth.getInstance();

        mSchNumber = (TextInputLayout) findViewById(R.id.login_sch_number);
        mPassword = (TextInputLayout) findViewById(R.id.login_password);
        mLoginBtn = (Button) findViewById(R.id.login_create_btn);
        mForgotPass = findViewById(R.id.tv_forgotPass);

        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String sch_number = mSchNumber.getEditText().getText().toString().trim();
                final String password = mPassword.getEditText().getText().toString().trim();

                if (checkFields(sch_number, password)) {
                    mLoginProgress.setTitle("Logging in...");
                    mLoginProgress.setMessage("Please wait while we check your credentials.");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();
                    mDatabase.getReference().child("APP-VERSION").child("VER").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            double version = Double.parseDouble(dataSnapshot.getValue().toString());
                            if (version != MainActivity.APP_VERSION) {
                                Intent intent = new Intent(LoginActivity.this, AppExpiredActivity.class);
                                mLoginProgress.dismiss();
                                startActivity(intent);
                                finish();
                            } else {
                                getEmailAndLogin(sch_number, password);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        });

        mForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String sch_num = mSchNumber.getEditText().getText().toString();
                if (sch_num.isEmpty()) {
                    displayToast("Please enter your Scholar Number");
                } else {
                    mLoginProgress.setTitle("Forgot password...");
                    mLoginProgress.setMessage("Please wait while we send you a password reset email.");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();
                    mDatabase.getReference().child("APP-VERSION").child("VER").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            double version = Double.parseDouble(dataSnapshot.getValue().toString());
                            if (version != MainActivity.APP_VERSION) {
                                Intent intent = new Intent(LoginActivity.this, AppExpiredActivity.class);
                                startActivity(intent);
                                mLoginProgress.hide();
                                finish();
                            } else {
                                String branch = CommonFunctions.getBranch(sch_num);
                                String year = CommonFunctions.getYear(sch_num);
                                if (branch.equals("") || year.equals("")) {
                                    displayToast("Invalid Scholar Number");
                                    mLoginProgress.hide();
                                    return;
                                }
                                mDbReference = mDatabase.getReference().child("Scholar_Numbers").child(branch).child(year).child(sch_num).child("email");
                                mDbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String email = dataSnapshot.getValue().toString();
                                        if (email == null) {
                                            displayToast("Invalid Scholar Number. Please Register.");
                                            mLoginProgress.hide();
                                        } else {
                                            mAuth.sendPasswordResetEmail(email);
                                            mLoginProgress.hide();
                                            displayToast("Sent password reset mail to " + email);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }
                    @Override
                    public void onCancelled (DatabaseError databaseError){

                    }
                });
            }}});}

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mLoginProgress.dismiss();
                    Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(mainIntent);
                    finish();
                } else {
                    mLoginProgress.hide();
                    displayToast("Invalid Scholar Number/Password");
                }
            }
        });
    }

    private boolean checkFields(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            displayToast("Please fill in all the fields");
            return false;
        }
        if (email.length() != 9) {
            displayToast("Invalid Scholar Number");
            return false;
        }
        return isOnline();
    }

    public boolean isOnline() {
        ConnectivityManager conMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            displayToast("Please check your Internet connection!");
            return false;
        }
        return true;
    }

    public void displayToast(String message) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    private void getEmailAndLogin(String sch_number, final String password) {
        String branch = CommonFunctions.getBranch(sch_number);
        String year = CommonFunctions.getYear(sch_number);
        if (branch.equals("") || year.equals("")) {
            displayToast("Invalid Scholar Number");
            mLoginProgress.dismiss();
            return;
        }
        mDbReference = mDatabase.getReference().child("Scholar_Numbers").child(branch).child(year).child(sch_number).child("email");
        mDbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String email = dataSnapshot.getValue().toString();
                if (email.isEmpty()) {
                    displayToast("Invalid Scholar Number/Password. Please Register.");
                } else {
                    loginUser(email, password);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
