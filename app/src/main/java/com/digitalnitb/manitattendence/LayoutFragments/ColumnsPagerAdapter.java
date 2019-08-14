package com.digitalnitb.manitattendence.LayoutFragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.digitalnitb.manitattendence.Utilities.CommonFunctions;


public class ColumnsPagerAdapter extends FragmentPagerAdapter{
    public ColumnsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        ColumnFragment columnFragment = new ColumnFragment();

        Bundle args = new Bundle();
        args.putInt("column_id", position+1);
        columnFragment.setArguments(args);

        return columnFragment;
    }

    @Override
    public int getCount() {
        return CommonFunctions.getColumns();
    }

    @Override
    public CharSequence getPageTitle(int position) {

        int number = position+1;

        return "Column " + number;
    }
}
