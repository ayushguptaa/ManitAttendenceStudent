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
import android.widget.Toast;

import com.digitalnitb.manitattendence.Utilities.CommonFunctions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout mScholarNumber;
    private TextInputLayout mFullName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private TextInputLayout mCnfPassword;
    private Button mCreateBtn;

    private Toolbar mToolbar;

    private Toast mToast;

    //Progress Dialog
    private ProgressDialog mRegProgress;

    //Firebase Auth
    private FirebaseAuth mAuth;

    //Getting Realtime Database
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();

    //Scholar Refrence
    private DatabaseReference mDbReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Toolbar set
        mToolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress = new ProgressDialog(this);

        //Firebase Auth Intitalize
        mAuth = FirebaseAuth.getInstance();

        //Getting all input views
        mScholarNumber = (TextInputLayout) findViewById(R.id.reg_scholar_number);
        mFullName = (TextInputLayout) findViewById(R.id.reg_full_name);
        mEmail = (TextInputLayout) findViewById(R.id.reg_email);
        mPassword = (TextInputLayout) findViewById(R.id.reg_password);
        mCnfPassword = (TextInputLayout) findViewById(R.id.reg_password_cnf);
        mCreateBtn = (Button) findViewById(R.id.reg_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String scholar_number = mScholarNumber.getEditText().getText().toString().trim();
                final String name = mFullName.getEditText().getText().toString().trim();
                final String email = mEmail.getEditText().getText().toString().trim();
                final String password = mPassword.getEditText().getText().toString().trim();
                final String cnfPassword = mCnfPassword.getEditText().getText().toString().trim();

                if (checkFields(scholar_number, name, email, password, cnfPassword)) {

                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account !");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    mDatabase.getReference().child("APP-VERSION").child("VER").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            double version = Double.parseDouble(dataSnapshot.getValue().toString());
                            if(version!=MainActivity.APP_VERSION){
                                Intent intent = new Intent(RegisterActivity.this, AppExpiredActivity.class);
                                mRegProgress.dismiss();
                                startActivity(intent);
                                finish();
                            }else {
                                registerUser(scholar_number, name, email, password);
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }
        });
    }

    private boolean checkFields(String scholar_number, String name, String email, String password, String cnf_password) {
        if (scholar_number.isEmpty() || name.isEmpty() || email.isEmpty() || password.isEmpty() || cnf_password.isEmpty()) {
            displayToast("Please fill in all fields");
            return false;
        }
        if (scholar_number.length() != 9) {
            displayToast("Please enter a valid Scholar Number");
            return false;
        }
        if (password.length() < 6) {
            displayToast("Password must be at least 6 characters");
            return false;
        }
        if(!password.equals(cnf_password)){
            displayToast("Passwords do not match");
            return false;
        }
        return isOnline();
    }

    private boolean checkScholarNumbers(String scholar_number) {
        String branch = CommonFunctions.getBranch(scholar_number);
        String year = CommonFunctions.getYear(scholar_number);
        if (branch.equals("")) {
            displayToast("Invalid Scholar Number.");
            return false;
        }
        if(year.equals("")) {
            displayToast("Currently we only support first year scholar number");
            return false;
        }

        mDbReference = mDatabase.getReference().child("Scholar_Numbers").child(branch).child(year).child(scholar_number);

        return true;
    }

    private void registerUser(final String scholar_number,final String name, final String email, final String password) {

        if (!checkScholarNumbers(scholar_number)) {
            mRegProgress.hide();
            return;
        }

        mDbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    displayToast("This Scholar Number is already registered");
                    mRegProgress.hide();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {

                                final FirebaseUser user = mAuth.getCurrentUser();
                                String uid = user.getUid();
                                HashMap<String, String> userMap = new HashMap<>();
                                userMap.put("uid", uid);
                                userMap.put("email", email);
                                userMap.put("name", CommonFunctions.BeautifyName(name));


                                mDbReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                    .setDisplayName(scholar_number).build();
                                            user.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        mRegProgress.dismiss();

                                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(mainIntent);
                                                        mAuth.getCurrentUser().sendEmailVerification();
                                                        finish();
                                                    }
                                                    else {
                                                        mRegProgress.hide();
                                                        Toast.makeText(RegisterActivity.this, task.getException().toString().split(":")[1].trim(), Toast.LENGTH_LONG).show();
                                                        user.delete();
                                                    }
                                                }
                                            });

                                        } else {
                                            mRegProgress.hide();
                                            Toast.makeText(RegisterActivity.this, task.getException().toString().split(":")[1].trim(), Toast.LENGTH_LONG).show();
                                            user.delete();
                                        }
                                    }
                                });


                            } else {
                                mRegProgress.hide();
                                Toast.makeText(RegisterActivity.this, task.getException().toString().split(":")[1].trim(), Toast.LENGTH_LONG).show();

                            }

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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
        mToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        mToast.show();
    }

}
