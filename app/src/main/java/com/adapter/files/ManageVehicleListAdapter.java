package com.adapter.files;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.recyclerview.widget.RecyclerView;

import com.general.files.GeneralFunctions;
import com.roaddo.store.R;
import com.view.MTextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 09-06-2017.
 */
public class ManageVehicleListAdapter extends RecyclerView.Adapter<ManageVehicleListAdapter.ViewHolder> {

    public GeneralFunctions generalFunc;
    ArrayList<HashMap<String, String>> list_item;
    Context mContext;
    OnItemClickList onItemClickList;


    public ManageVehicleListAdapter(Context mContext, ArrayList<HashMap<String, String>> list_item, GeneralFunctions generalFunc) {
        this.mContext = mContext;
        this.list_item = list_item;
        this.generalFunc = generalFunc;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_vehicle_design, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        HashMap<String, String> item = list_item.get(position);

        viewHolder.vNameTxtView.setText(item.get("VehicleTitle"));
        //viewHolder.vNameTxtView.setText(  "(" + item.get("vLicencePlate") + ")");

//        new CreateRoundedView(Color.parseColor("#ffa524"), 0, Utils.dipToPixels(mContext, 1),
//                Color.parseColor("#ffffff"), viewHolder.radioname);


        viewHolder.radioname.setOnClickListener(v -> {
            if (onItemClickList != null) {
                onItemClickList.onItemClick(position, 0);
            }


        });
        viewHolder.vNameTxtView.setOnClickListener(v -> {

            if (onItemClickList != null) {
                onItemClickList.onItemClick(position, 0);
            }

        });
        viewHolder.contentView.setOnClickListener(v -> {

            if (onItemClickList != null) {
                onItemClickList.onItemClick(position, 0);
            }

        });

    }

    @Override
    public int getItemCount() {
        return list_item.size();
    }

    public void setOnItemClickList(OnItemClickList onItemClickList) {
        this.onItemClickList = onItemClickList;
    }

    public interface OnItemClickList {
        void onItemClick(int position, int viewClickId);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        public MTextView vNameTxtView;

        public RadioButton radioname;

        public View contentView;

        public ViewHolder(View view) {
            super(view);

            contentView = view;

            vNameTxtView = (MTextView) view.findViewById(R.id.vNameTxtView);
            radioname = (RadioButton) view.findViewById(R.id.radioName);

        }
    }

}
