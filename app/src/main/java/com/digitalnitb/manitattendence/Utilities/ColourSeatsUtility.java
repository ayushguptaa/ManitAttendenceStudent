package com.digitalnitb.manitattendence.Utilities;


import com.digitalnitb.manitattendence.R;

import java.util.ArrayList;

public class ColourSeatsUtility {

    private static int mRows = CommonFunctions.getRows();

    private static final int MAX_ROWS = 20;

    private static final int ROW_SELECTED = 1;
    private static final int ROW_BOOKED = 2;

    private static ArrayList<Integer> column1 = new ArrayList<>();
    private static ArrayList<Integer> column2 = new ArrayList<>();
    private static ArrayList<Integer> column3 = new ArrayList<>();
    private static ArrayList<Integer> column4 = new ArrayList<>();
    private static ArrayList<Integer> column5 = new ArrayList<>();

    private static ArrayList<ArrayList<Integer>> columns = new ArrayList<>();

    private static boolean alreadyInstantiated = false;

    public static int mSelectedColumn = -1;
    public static int mSelectedPosition = -1;
    private static int mOldValue = 0;

    public static void instantiate() {
        if (!alreadyInstantiated) {
            for (int i = 0; i < MAX_ROWS * 4; i++) {
                column1.add(0);
                column2.add(0);
                column3.add(0);
                column4.add(0);
                column5.add(0);
            }
            columns.add(column1);
            columns.add(column2);
            columns.add(column3);
            columns.add(column4);
            columns.add(column5);
            alreadyInstantiated = true;
        }
    }

    private static void setAllzero() {
        for (int i = 0; i < columns.size(); i++) {
            for (int j = 0; j < MAX_ROWS * 4; j++) {
                columns.get(i).set(j, 0);
            }
        }
    }


    public static int getColor(int column, int position) {
        return colourLogic(columns.get(column - 1).get(position));
    }

    private static int colourLogic(Integer value) {
        if (value == 1) {
            return R.color.seatSelected;
        }
        if (value == 2) {
            return R.color.seatNotEmpty;
        }

        return R.color.seatEmpty;
    }

    public static void setSelected(int column, int position) {
        if (mSelectedColumn != -1) {
            columns.get(column-1).set(mSelectedPosition, mOldValue);
        }
        mSelectedColumn = column;
        mSelectedPosition = position;
        mOldValue = columns.get(column-1).get(position);
        columns.get(column-1).set(position, ROW_SELECTED);
    }

    public static void setBooked(int column, int position) {
        if (mSelectedColumn == column && mSelectedPosition == position) {
            mOldValue = ROW_BOOKED;
            return;
        }
        columns.get(column-1).set(position, ROW_BOOKED);
        CommonFunctions.notifyAllAdapters();
    }

    public static void resetSelected() {
        mSelectedColumn = -1;
        mSelectedPosition = -1;
        mOldValue = 0;
        setAllzero();
    }

    public static void addColumns(int extra){
        for(int i = 0; i<extra ; i++){
            ArrayList<Integer> temp = new ArrayList<>();
            for(int j = 0; j<MAX_ROWS*4 ; j++){
                temp.add(0);
            }
            columns.add(temp);
        }
    }
}
