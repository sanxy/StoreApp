package com.adapter.files;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.roaddo.store.R;
import com.general.files.GeneralFunctions;
import com.kyleduo.switchbutton.SwitchButton;
import com.view.MTextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by admin on 16/05/18.
 */

public class OrderItemsRecyclerAdapter extends RecyclerView.Adapter<OrderItemsRecyclerAdapter.ViewHolder> {

    public GeneralFunctions generalFunc;
    public OnItemClickList onItemClickList;
    ArrayList<HashMap<String, String>> list_item;
    Context mContext;

    public OrderItemsRecyclerAdapter(Context mContext, ArrayList<HashMap<String, String>> list_item, GeneralFunctions generalFunc) {
        this.mContext = mContext;
        this.list_item = list_item;
        this.generalFunc = generalFunc;

    }

    @Override
    public OrderItemsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_detail_item_design, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        HashMap<String, String> item = list_item.get(position);

        viewHolder.itemNameTxt.setText(item.get("MenuItem"));
        if (!item.get("SubTitleConverted").isEmpty()){
            viewHolder.itemSubNameTxt.setText("("+item.get("SubTitleConverted")+")");
            viewHolder.itemSubNameTxt.setVisibility(View.VISIBLE);
        }else {
            viewHolder.itemSubNameTxt.setVisibility(View.GONE);
        }


        if (position==list_item.size()-1)
        {
            viewHolder.extraView.setVisibility(View.VISIBLE);
        }else
        {
            viewHolder.extraView.setVisibility(View.GONE);
        }


        if (item.get("eAvailable").equalsIgnoreCase("Yes")) {
            viewHolder.availItemSwitch.setCheckedNoEvent(true);
            viewHolder.availItemSwitch.setThumbColorRes(android.R.color.holo_green_dark);
            viewHolder.availableTxtView.setText(item.get("LBL_AVAILABLE"));
        } else {
            viewHolder.availItemSwitch.setCheckedNoEvent(false);
            viewHolder.availItemSwitch.setThumbColorRes(android.R.color.holo_red_dark);
            viewHolder.availableTxtView.setText(item.get("LBL_NOT_AVAILABLE"));
        }

        viewHolder.itemPriceTxt.setText(item.get("fTotPriceConverted"));
        viewHolder.noItemsTxt.setText("x " + item.get("iQtyConveretd"));

        viewHolder.availItemSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (onItemClickList != null) {
                onItemClickList.onItemAvailabilityChanged(position, isChecked);
            }
        });

        String TotalDiscountPrice=item.get("TotalDiscountPrice");
        if(TotalDiscountPrice != null && !TotalDiscountPrice.equals("")){
            viewHolder.itemPriceTxt.setTextColor(mContext.getResources().getColor(R.color.gray));

            SpannableStringBuilder spanBuilder = new SpannableStringBuilder();
            SpannableString origSpan = new SpannableString(viewHolder.itemPriceTxt.getText());

            origSpan.setSpan(new StrikethroughSpan(), 0, viewHolder.itemPriceTxt.getText().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            spanBuilder.append(origSpan);

            String priceStr = " "+item.get("TotalDiscountPriceConverted");

            SpannableString discountSpan = new SpannableString(priceStr);
            discountSpan.setSpan(new ForegroundColorSpan(mContext.getResources().getColor(R.color.appThemeColor_1)), 0 , priceStr.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spanBuilder.append(discountSpan);

            viewHolder.itemPriceTxt.setText(spanBuilder);
        }else{

            viewHolder.itemPriceTxt.setTextColor(mContext.getResources().getColor(R.color.appThemeColor_1));
            viewHolder.itemPriceTxt.setPaintFlags(viewHolder.itemPriceTxt.getPaintFlags());
        }

        if (item.get("eConfirm").equalsIgnoreCase("Yes") || item.get("eDecline").equalsIgnoreCase("Yes")) {
            viewHolder.availItemSwitch.setEnabled(false);
            viewHolder.availItemSwitch.setOnCheckedChangeListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return list_item.size();
    }

    public void setOnItemClickList(OnItemClickList onItemClickList) {
        this.onItemClickList = onItemClickList;
    }

    public interface OnItemClickList {
        void onItemClick(int position);

        void onItemAvailabilityChanged(int position, boolean isAvailable);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public MTextView itemNameTxt, itemSubNameTxt, noItemsTxt, availableTxtView, itemPriceTxt;
        public SwitchButton availItemSwitch;
        public View extraView;

        public ViewHolder(View view) {
            super(view);

            itemNameTxt = (MTextView) view.findViewById(R.id.itemNameTxt);
            itemPriceTxt = (MTextView) view.findViewById(R.id.itemPriceTxt);
            itemSubNameTxt = (MTextView) view.findViewById(R.id.itemSubNameTxt);
            noItemsTxt = (MTextView) view.findViewById(R.id.noItemsTxt);
            availableTxtView = (MTextView) view.findViewById(R.id.availableTxtView);
            availItemSwitch = (SwitchButton) view.findViewById(R.id.availItemSwitch);
            extraView = (View) view.findViewById(R.id.extraView);

        }
    }

}
