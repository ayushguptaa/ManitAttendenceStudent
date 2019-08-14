package com.digitalnitb.manitattendence.LayoutFragments;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.digitalnitb.manitattendence.R;
import com.digitalnitb.manitattendence.Utilities.ColourSeatsUtility;
import com.digitalnitb.manitattendence.Utilities.CommonFunctions;

public class SeatAdapter extends RecyclerView.Adapter<SeatAdapter.ViewHolder>{

    private int mRows;
    private LayoutInflater mInflater;
    private Context mContext;
    private int mColumn;

    private ItemClickListener mClickListener;

    //Constructor for Recycler View
    SeatAdapter(Context context, int column) {
        mContext = context;
        mRows = CommonFunctions.getRows();
        mInflater = LayoutInflater.from(context);
        mColumn = column;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.seat_item_rv, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if(position%4 == 0){
            holder.rowIdtextView.setText(String.valueOf(position/4 + 1));
            holder.rowIdtextView.setBackgroundColor(mContext.getResources().getColor(R.color.seatRow));
        }else {
            holder.rowIdtextView.setText("");
            holder.rowIdtextView.setBackgroundColor(0);
            holder.rootLayout.setBackgroundColor(mContext.getResources().getColor(ColourSeatsUtility.getColor(mColumn, position)));
        }

    }

    @Override
    public int getItemCount() {
        return mRows*4;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LinearLayout rootLayout;

        TextView rowIdtextView;

        ViewHolder(View itemView) {
            super(itemView);
            rowIdtextView = itemView.findViewById(R.id.tv_row_id);
            rootLayout = itemView.findViewById(R.id.rv_linear_layout);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
