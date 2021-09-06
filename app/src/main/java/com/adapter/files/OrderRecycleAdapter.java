package com.adapter.files;

import android.content.Context;
import android.graphics.Color;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.roaddo.store.R;
import com.general.files.GeneralFunctions;
import com.utils.Logger;
import com.view.CreateRoundedView;
import com.view.MTextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Esite on 30-03-2018.
 */

public class OrderRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    public static String DateFormateInDetailScreen = "EEE, MMM dd, yyyy HH:mm aa";
    public GeneralFunctions generalFunc;

    ArrayList<HashMap<String, String>> list;
    Context mContext;
    boolean isFooterEnabled = false;
    View footerView;

    FooterViewHolder footerHolder;
    private OnItemClickListener mItemClickListener;
    int newOrder = -1;
    private static final String TAG = "OrderRecycleAdapter";


    public OrderRecycleAdapter(Context mContext, ArrayList<HashMap<String, String>> list, GeneralFunctions generalFunc, boolean isFooterEnabled) {
        this.mContext = mContext;
        this.list = list;
        this.generalFunc = generalFunc;
        this.isFooterEnabled = isFooterEnabled;
    }

    public void setOnItemClickListener(OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_FOOTER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_list, parent, false);
            this.footerView = v;
            return new FooterViewHolder(v);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_design, parent, false);
            return new ViewHolder(view);
        }

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof ViewHolder) {
            final HashMap<String, String> item = list.get(position);
            final ViewHolder viewHolder = (ViewHolder) holder;


            if (item.get("eTakeaway") != null && item.get("eTakeaway").equalsIgnoreCase("Yes")) {
                ((ViewHolder) holder).takeAwayTxt.setVisibility(View.VISIBLE);
                ((ViewHolder) holder).takeAwayTxt.setText(item.get("LBL_TAKE_AWAY"));
            } else {
                ((ViewHolder) holder).takeAwayTxt.setVisibility(View.GONE);
            }

            viewHolder.orderIdTxt.setText("#" + item.get("vOrderNoConverted"));
            viewHolder.noItemsTxt.setText((GeneralFunctions.parseIntegerValue(1, item.get("TotalItems")) > 1 ? item.get("TotalItemsConverted") + " " + item.get("LBL_ITEMS") : item.get("TotalItemsConverted") + " " + item.get("LBL_ITEM")));

            viewHolder.timeTxt.setText(item.get("tOrderRequestDateConverted"));

            if (item.get("ePaid") != null && item.get("ePaid").equalsIgnoreCase("Yes")) {
                Logger.d(TAG, "onBindViewHolder 2: "+ item.get("ePaid"));

            } else {
                ((ViewHolder) holder).statusTxt.setText("Un-Paid");
                ((ViewHolder) holder).statusTxt.setTextColor(mContext.getResources().getColor(R.color.bt_error_red));
//                Log.d(TAG, "onBindViewHolder: "+ item.get("ePaid"));
            }

            viewHolder.orderItem.setOnClickListener(view -> {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClickList(view, position);
                }
            });


            if (newOrder == 1) {
                if (position == 0) {
                    new CreateRoundedView(Color.parseColor("#ffffff"), (int) mContext.getResources().getDimension(R.dimen._8sdp), 2, mContext.getResources().getColor(R.color.requestTimerColor), viewHolder.orderItem);
                }
            } else {
                new CreateRoundedView(Color.parseColor("#ffffff"), (int) mContext.getResources().getDimension(R.dimen._8sdp), 2, mContext.getResources().getColor(R.color.white), viewHolder.orderItem);
            }

            if (position == list.size() - 1) {
                viewHolder.extraView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.extraView.setVisibility(View.GONE);
            }


        } else {
            FooterViewHolder footerHolder = (FooterViewHolder) holder;
            this.footerHolder = footerHolder;
        }


    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionFooter(position) && isFooterEnabled == true) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    private boolean isPositionFooter(int position) {
        return position == list.size();
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (isFooterEnabled == true) {
            return list.size() + 1;
        } else {
            return list.size();
        }

    }

    public void addFooterView() {
        this.isFooterEnabled = true;
        notifyDataSetChanged();
        if (footerHolder != null)
            footerHolder.progressArea.setVisibility(View.VISIBLE);
    }

    public void removeFooterView() {
        if (footerHolder != null) {
            footerHolder.progressArea.setVisibility(View.GONE);
            try {
                footerHolder.progressArea.setPadding(0, -1 * footerView.getMeasuredHeight(), 0, 0);
            } catch (Exception e) {

            }
        }

    }


    public interface OnItemClickListener {
        void onItemClickList(View v, int position);
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {

        public MTextView orderIdTxt, noItemsTxt, timeTxt, takeAwayTxt, statusTxt;
        public LinearLayout orderItem;
        public LinearLayout main_layout;
        MTextView orderStatusTxtView;
        View extraView;

        public ViewHolder(View view) {
            super(view);
            main_layout = (LinearLayout) view.findViewById(R.id.main_layout);
            orderIdTxt = (MTextView) view.findViewById(R.id.orderIdTxt);
            statusTxt = (MTextView) view.findViewById(R.id.statusTxt);
            takeAwayTxt = (MTextView) view.findViewById(R.id.takeAwayTxt);
            noItemsTxt = (MTextView) view.findViewById(R.id.noItemsTxt);
            timeTxt = (MTextView) view.findViewById(R.id.timeTxt);
            orderStatusTxtView = (MTextView) view.findViewById(R.id.orderStatusTxtView);
            orderItem = (LinearLayout) view.findViewById(R.id.orderItem);
            extraView = (View) view.findViewById(R.id.extraView);
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {
        LinearLayout progressArea;

        public FooterViewHolder(View itemView) {
            super(itemView);

            progressArea = (LinearLayout) itemView;

        }
    }

    public void hilightNewOrder() {
        newOrder = 1;
        notifyDataSetChanged();

    }

    public void delightNewOrder() {
        newOrder = -1;
        notifyDataSetChanged();
    }
}
