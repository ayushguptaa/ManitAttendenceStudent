package com.digitalnitb.manitattendence;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

public class AccountSettingsActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private TextView mNameTextView;
    private TextView mBranchTextView;
    private TextView mScholarNumTextView;
    private TextView mEmailTextView;
    private Button mChangeEmail;
    private Button mChangePassword;
    private ProgressBar mLoading;

    private DatabaseReference mDatabase;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        Toolbar toolbar = findViewById(R.id.account_setting_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressBar = findViewById(R.id.account_setting_progress);
        mNameTextView = findViewById(R.id.account_tv_name);
        mBranchTextView = findViewById(R.id.account_tv_branch);
        mScholarNumTextView = findViewById(R.id.account_tv_schnum);
        mEmailTextView = findViewById(R.id.account_tv_email);
        mChangeEmail = findViewById(R.id.btnChangeEmail);
        mChangePassword = findViewById(R.id.btnChangePassword);
        mLoading = findViewById(R.id.pb_setting_loading);

        String sch_num = CommonFunctions.getScholar_number();
        String branch = CommonFunctions.getBranch(sch_num);
        String year = CommonFunctions.getYear(sch_num);

        mScholarNumTextView.setText(String.format("Your Scholar Number is\n%s", sch_num));
        mBranchTextView.setText(String.format("%s, %s Year", branch, getYearFormat(year)));

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Scholar_Numbers").child(branch).child(year).child(sch_num);

        mDatabase.child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mEmailTextView.setText(dataSnapshot.getValue().toString());
                mDatabase.child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mNameTextView.setText(String.format("Hello, %s", dataSnapshot.getValue().toString()));
                        startAnimation();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(AccountSettingsActivity.this);
                final EditText edittext = new EditText(AccountSettingsActivity.this);
                alert.setMessage("Enter your current password");
                alert.setTitle("Change Email");
                alert.setCancelable(false);

                alert.setView(edittext);

                alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int whichButton) {
                        mLoading.setVisibility(View.VISIBLE);
                        String password = edittext.getText().toString();
                        mAuth.signInWithEmailAndPassword(mAuth.getCurrentUser().getEmail(), password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    mLoading.setVisibility(View.INVISIBLE);
                                    final AlertDialog.Builder alert = new AlertDialog.Builder(AccountSettingsActivity.this);
                                    final EditText edittext2 = new EditText(AccountSettingsActivity.this);
                                    alert.setMessage("Enter new email");
                                    alert.setTitle("Change Email");
                                    alert.setView(edittext2);
                                    alert.setCancelable(false);
                                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.dismiss();
                                        }
                                    });
                                    alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    mLoading.setVisibility(View.VISIBLE);
                                                    String email = edittext2.getText().toString();
                                                    mAuth.getCurrentUser().updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            mLoading.setVisibility(View.INVISIBLE);
                                                            if (task.isSuccessful()) {
                                                                mDatabase.child("email").setValue(mAuth.getCurrentUser().getEmail());
                                                                displayToast("Email Changed Successfully");
                                                                mEmailTextView.setText(mAuth.getCurrentUser().getEmail());
                                                                mAuth.getCurrentUser().sendEmailVerification();
                                                                dialog.dismiss();
                                                            } else {
                                                                dialog.dismiss();
                                                                displayToast(task.getException().toString().split(":")[1].trim());
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                    );
                                    dialog.dismiss();
                                    alert.show();
                                } else {
                                    displayToast("Invalid email/password");
                                }
                            }
                        });
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

                alert.show();
            }
        });
        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(AccountSettingsActivity.this);
                final EditText edittext = new EditText(AccountSettingsActivity.this);
                alert.setMessage("Enter your current password");
                alert.setTitle("Change Password");
                alert.setCancelable(false);

                alert.setView(edittext);

                alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int whichButton) {
                        mLoading.setVisibility(View.VISIBLE);
                        String password = edittext.getText().toString();
                        mAuth.signInWithEmailAndPassword(mAuth.getCurrentUser().getEmail(), password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                mLoading.setVisibility(View.INVISIBLE);
                                if (task.isSuccessful()) {
                                    final AlertDialog.Builder alert = new AlertDialog.Builder(AccountSettingsActivity.this);
                                    final EditText edittext2 = new EditText(AccountSettingsActivity.this);
                                    alert.setMessage("Enter new password");
                                    alert.setTitle("Change Password");
                                    alert.setView(edittext2);
                                    alert.setCancelable(false);
                                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            dialog.dismiss();
                                        }
                                    });
                                    alert.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    mLoading.setVisibility(View.VISIBLE);
                                                    String password = edittext2.getText().toString();
                                                    mAuth.getCurrentUser().updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            mLoading.setVisibility(View.INVISIBLE);
                                                            if (task.isSuccessful()) {
                                                                displayToast("Password Changed Successfully");
                                                                dialog.dismiss();
                                                            } else {
                                                                dialog.dismiss();
                                                                displayToast(task.getException().toString().split(":")[1].trim());
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                    );
                                    dialog.dismiss();
                                    alert.show();
                                } else {
                                    displayToast("Invalid password");
                                }
                            }
                        });
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

                alert.show();
            }
        });
    }

    ;


    private String getYearFormat(String year) {
        try{
        int yearInt = Integer.valueOf(year);
        switch (yearInt) {
            case 1:
                return "1st";
            case 2:
                return "2nd";
            case 3:
                return "3rd";
            default:
                return "4th";
        }}catch (Exception e){
            return year;
        }
    }

    private void startAnimation() {

        int time = 1000;

        final Animation in0 = new AlphaAnimation(1.0f, 0.0f);
        in0.setDuration(time/2);

        final Animation in1 = new AlphaAnimation(0.0f, 1.0f);
        in1.setDuration(time);

        mProgressBar.startAnimation(in0);
        mNameTextView.startAnimation(in1);
        mBranchTextView.startAnimation(in1);
        mScholarNumTextView.startAnimation(in1);
        mEmailTextView.startAnimation(in1);
        mChangeEmail.startAnimation(in1);
        mChangePassword.startAnimation(in1);

        in0.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                mProgressBar.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        in1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                mNameTextView.setVisibility(View.VISIBLE);
                mBranchTextView.setVisibility(View.VISIBLE);
                mScholarNumTextView.setVisibility(View.VISIBLE);
                mEmailTextView.setVisibility(View.VISIBLE);
                mChangeEmail.setVisibility(View.VISIBLE);
                mChangePassword.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    public void displayToast(String message) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        mToast.show();
    }

}
