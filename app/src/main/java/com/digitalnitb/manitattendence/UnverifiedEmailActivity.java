package com.digitalnitb.manitattendence;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalnitb.manitattendence.Utilities.CommonFunctions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class UnverifiedEmailActivity extends AppCompatActivity {

    private TextView mEmailTextView;
    private Button mResendEmail;
    private Button mChangeEmail;
    private ProgressBar mLoading;
    private Button mRetryBtn;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unverified_email);
        final Toolbar toolbar = findViewById(R.id.account_setting_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Verify your Email");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mEmailTextView = findViewById(R.id.verify_email_tv);
        mResendEmail = findViewById(R.id.btnResendMail);
        mChangeEmail = findViewById(R.id.verify_btnChangeEmail);
        mLoading = findViewById(R.id.pb_verify_loading);
        mRetryBtn = findViewById(R.id.btn_retry);

        mEmailTextView.setText(mAuth.getCurrentUser().getEmail());

        mResendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoading.setVisibility(View.VISIBLE);
                mAuth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mLoading.setVisibility(View.INVISIBLE);
                        Toast.makeText(UnverifiedEmailActivity.this, "Email verification mail sent, please check your email", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        mChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(UnverifiedEmailActivity.this);
                final EditText edittext = new EditText(UnverifiedEmailActivity.this);
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
                                mLoading.setVisibility(View.INVISIBLE);
                                if(task.isSuccessful()){
                                    final AlertDialog.Builder alert = new AlertDialog.Builder(UnverifiedEmailActivity.this);
                                    final EditText edittext2 = new EditText(UnverifiedEmailActivity.this);
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
                                                    if(task.isSuccessful()){
                                                        String sch_no = CommonFunctions.getScholar_number();
                                                        FirebaseDatabase.getInstance().getReference().child("Scholar_Numbers").child(CommonFunctions.getBranch(sch_no))
                                                                .child(CommonFunctions.getYear(sch_no)).child(sch_no).child("email").setValue(mAuth.getCurrentUser().getEmail());
                                                        displayToast("Email Changed Successfully, please sign in again.");
                                                        mEmailTextView.setText(mAuth.getCurrentUser().getEmail());
                                                        mAuth.getCurrentUser().sendEmailVerification();
                                                        dialog.dismiss();
                                                        mAuth.signOut();
                                                        CommonFunctions.resetAll();
                                                        Intent intent = new Intent(UnverifiedEmailActivity.this, StartActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                        finish();
                                                    }else {
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
                                }else {
                                    displayToast("Invalid email/password");
                                }
                            }
                        });}});

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

                alert.show();
            }
        });
        mRetryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                CommonFunctions.resetAll();
                Intent intent = new Intent(UnverifiedEmailActivity.this, StartActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.removeItem(R.id.main_account_settings);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.main_logout_btn) {
            mAuth.signOut();
            CommonFunctions.resetAll();
            Intent intent = new Intent(UnverifiedEmailActivity.this, StartActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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
