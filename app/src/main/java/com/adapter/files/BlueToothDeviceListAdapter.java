package com.adapter.files;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.roaddo.store.R;
import com.general.files.GeneralFunctions;
import com.view.MTextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Esite on 30-03-2018.
 */

public class BlueToothDeviceListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;
    public GeneralFunctions generalFunc;

    ArrayList<HashMap<String,String>> list;
    Context mContext;
    boolean isFooterEnabled = false;
    View footerView;
    public int selectedPos=-1;

    FooterViewHolder footerHolder;
    private OnItemClickListener mItemClickListener;


    public BlueToothDeviceListAdapter(Context mContext, ArrayList<HashMap<String,String>> list, GeneralFunctions generalFunc, boolean isFooterEnabled) {
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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blutooth_design, parent, false);
            return new ViewHolder(view);
        }

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        if (holder instanceof ViewHolder) {
            final HashMap<String,String> item = list.get(position);
            ((ViewHolder) holder).deviceName.setText(item.get("Name"));
            ((ViewHolder) holder).deviceId.setText(item.get("Id"));

            if (selectedPos==position)
            {
                ((ViewHolder) holder).selectedArea.setVisibility(View.VISIBLE);
            }
            else
            {
                ((ViewHolder) holder).selectedArea.setVisibility(View.INVISIBLE);
            }

            ((ViewHolder) holder).contentArea.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClickList(view, position);
                    }
                }
            });


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

        public MTextView deviceName, deviceId, timeTxt;
        public LinearLayout contentArea;
        public LinearLayout selectedArea;

        public ViewHolder(View view) {
            super(view);

            deviceId = (MTextView) view.findViewById(R.id.deviceId);
            deviceName = (MTextView) view.findViewById(R.id.deviceName);

            selectedArea = (LinearLayout) view.findViewById(R.id.selectedArea);
            contentArea = (LinearLayout) view.findViewById(R.id.contentArea);
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {
        LinearLayout progressArea;

        public FooterViewHolder(View itemView) {
            super(itemView);

            progressArea = (LinearLayout) itemView;

        }
    }
}
