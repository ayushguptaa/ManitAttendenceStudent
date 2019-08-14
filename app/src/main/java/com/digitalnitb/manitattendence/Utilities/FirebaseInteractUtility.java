package com.digitalnitb.manitattendence.Utilities;


import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseInteractUtility {

    private static FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private static Toast mToast;

    public static boolean mAttended = false;

    public static void confirmAttendance(final Context context, String scholar_number, final int column, int position){

        String branch = CommonFunctions.getBranch(scholar_number);
        String year = CommonFunctions.getYear(scholar_number);
        DatabaseReference myRef = mDatabase.getReference().child("Attendance").child(branch).child(year);
        String finalMessage = CommonFunctions.getRollNumber(scholar_number) + "-" + column + "-" + position;
        myRef.push().setValue(finalMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    displayToast("Successfully Submitted", context);
                }else {
                    displayToast("Failed, Please Try Again", context);
                }
            }
        });
    }
    private static void displayToast(String message, Context context) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        mToast.show();
    }

    public static void startChecking(final ProgressDialog progressDialog){
        final String sch_number = CommonFunctions.getScholar_number();
        DatabaseReference myRef = mDatabase.getReference().child("Attendance").child(CommonFunctions.getBranch(sch_number)).child(CommonFunctions.getYear(sch_number));
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 0){
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String addedString = dataSnapshot.getValue().toString();
                String sch = addedString.split("-")[0];
                int column = Integer.parseInt(addedString.split("-")[1]);
                int row = Integer.parseInt(addedString.split("-")[2]);
                if(sch.equals(CommonFunctions.getRollNumber(sch_number))){
                    mAttended = true;
                    ColourSeatsUtility.setSelected(column, row);
                    CommonFunctions.notifyAllAdapters();
                }else {
                    ColourSeatsUtility.setBooked(column, row);}
                progressDialog.dismiss();
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

    public static void resetAttended(){
        mAttended =false;
    }

}
