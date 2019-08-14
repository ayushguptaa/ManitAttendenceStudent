package com.digitalnitb.manitattendence.LayoutFragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.digitalnitb.manitattendence.Utilities.CommonFunctions;

/**
 * A simple {@link Fragment} subclass.
 */
public class ColumnFragment extends Fragment implements SeatAdapter.ItemClickListener {

    private int COLUMN_ID;

    public ColumnFragment() {
        // Required empty public constructor
    }



    private SeatAdapter mAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        COLUMN_ID = getArguments().getInt("column_id", 0);

        // set up the RecyclerView
        RecyclerView recyclerView = new RecyclerView(getContext());
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        mAdapter = new SeatAdapter(getContext(), COLUMN_ID);
        //Onclick method below
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);
        CommonFunctions.addAdapter(mAdapter);
        return recyclerView;
    }

    @Override
    public void onItemClick(View view, int position) {
        CommonFunctions.adapterOnClick(getContext(), COLUMN_ID, position);
    }
}
