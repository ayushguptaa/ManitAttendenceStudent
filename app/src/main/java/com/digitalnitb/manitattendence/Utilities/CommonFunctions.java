package com.digitalnitb.manitattendence.Utilities;


import android.content.Context;
import android.widget.Toast;

import com.digitalnitb.manitattendence.LayoutFragments.SeatAdapter;
import com.digitalnitb.manitattendence.MainActivity;

import java.util.ArrayList;

public class CommonFunctions {

    private static String mScholar_number;

    private static boolean isMTech = false;

    private static Toast mToast;

    private static int mRows = 10;
    private static int mColumns = 5;

    private CommonFunctions() {

    }

    private static ArrayList<SeatAdapter> seatAdapters = new ArrayList<>();

    public static void addAdapter(SeatAdapter adapter) {
        seatAdapters.add(adapter);
    }

    public static void notifyAllAdapters() {
        for (SeatAdapter adapter : seatAdapters) {
            adapter.notifyDataSetChanged();
        }
    }

    public static String BeautifyName(String str) {
        String[] words = str.split(" ");
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            ret.append(Character.toUpperCase(words[i].charAt(0)));
            ret.append(words[i].substring(1));
            if (i < words.length - 1) {
                ret.append(' ');
            }
        }
        return ret.toString();
    }

    public static String getBranch(String scholar_number) {
        String verify = scholar_number.substring(3, 5);
        if(scholar_number.substring(2,4).equals("212"))
            return "MCA";
        if (!verify.equals("11")) {
            return "";
        }
        isMTech = scholar_number.charAt(2) - 48 == 2;

        int branchId = scholar_number.charAt(5) - 48;
        String branch = "";
        int roll = Integer.parseInt(getRollNumber(scholar_number));
        //ece 4 , chem 7, civil 1, cse 2, elec 3, msme 9 , mech 6
        if (!isMTech) {
            switch (branchId) {
                case 1:
                    if (roll <= 92) {
                        branch = "CIVIL";
                    }
                    break;
                case 2:
                    if (roll <= 102) {
                        branch = "CSE-1";
                    } else if (roll > 200 && roll <= 297) {
                        branch = "CSE-2";
                    }
                    break;
                case 3:
                    if (roll <= 122) {
                        branch = "ELEC";
                    }
                    break;
                case 4:
                    if (roll <= 148) {
                        branch = "ECE";
                    }
                    break;
                case 6:
                    if (roll <= 94) {
                        branch = "MECH-1";
                    } else if (roll > 200 && roll <= 294) {
                        branch = "MECH-2";
                    }
                    break;
                case 7:
                    //Not exact
                    if (roll <= 100) {
                        branch = "CHEM";
                    }
                    break;
                case 9:
                    if (roll <= 69) {
                        branch = "MSME";
                    }
                    break;
            }
        }else {
            branchId = scholar_number.charAt(6) - 48;
            switch (branchId){
                case 1:
                    if (roll >= 100 && roll <= 112) {
                        branch = "Computer Networking";
                    }
                    break;
                case 2:
                    if (roll >= 200 && roll <= 216) {
                        branch = "Information Security";
                    }
                    break;
                case 3:
                    if (roll >= 300 && roll <= 318) {
                        branch = "Advanced Computing";
                    }
                    break;
            }
        }

        return branch;
    }

    public static String getYear(String scholar_number) {
        int yearAdmission = Integer.parseInt(scholar_number.substring(0, 2));
        switch (yearAdmission) {
            case 17:
                if (isMTech) {
                    return "M-Tech-1";
                }
                return "1";
            default:
                return "";
        }
    }

    public static String getRollNumber(String sch_number) {
        return sch_number.substring(6, 9);
    }

    public static String getScholar_number() {
        return mScholar_number;
    }


    public static void setScholar_number(String scholar_number) {
        mScholar_number = scholar_number;
    }


    public static void adapterOnClick(Context context, int COLUMN_ID, int position) {
        if (FirebaseInteractUtility.mAttended) {
            displayToast("This scholar number already booked attendance", context);
            return;
        }
        if (MainActivity.mAttendanceStopped) {
            displayToast("Attendance has already stopped", context);
            return;
        }
        if (MainActivity.allowSubmission) {
            if (position % 4 != 0) {
                ColourSeatsUtility.setSelected(COLUMN_ID, position);
                notifyAllAdapters();
            }
        } else {
            displayToast("Attendance not yet started", context);
        }
    }

    private static void displayToast(String message, Context context) {
        if (mToast != null)
            mToast.cancel();
        mToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        mToast.show();
    }

    public static void resetAll() {
        ColourSeatsUtility.resetSelected();
        FirebaseInteractUtility.resetAttended();
    }


    public static int getRows() {
        return mRows;
    }

    public static int getColumns() {
        return mColumns;
    }

    public static void setRows(int rows) {
        mRows = rows;
    }

    public static void setColumns(int columns) {
        if (columns > mColumns) {
            int extra = columns - mColumns;
            ColourSeatsUtility.addColumns(extra);
        }
        mColumns = columns;
    }
}
