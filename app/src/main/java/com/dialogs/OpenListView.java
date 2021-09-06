package com.dialogs;

import android.content.Context;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.appcompat.app.AppCompatDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageView;


import com.adapter.files.CustomDialogListAdapter;
import com.roaddo.store.R;
import com.view.MTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class OpenListView {

    public enum OpenDirection {CENTER, BOTTOM}

    private Context mContext;
    private String title;
    private OpenDirection openDirection;
    private boolean isCancelable;
    private ArrayList<HashMap<String, String>> arrayList;
    private OnItemClickList onItemClickList;
    private AppCompatDialog listDialog = null;

    private boolean isStringArrayList=false;
    private ArrayList<String> stringArrayList;

    private OpenListView(Context mContext, String title, ArrayList<HashMap<String, String>> arrayList, OpenDirection openDirection, boolean isCancelable, OnItemClickList onItemClickList) {
        this.mContext = mContext;
        this.title = title;
        this.openDirection = openDirection;
        this.isCancelable = isCancelable;
        this.arrayList = arrayList;
        this.onItemClickList = onItemClickList;
    }

    private OpenListView(Context mContext, String title, ArrayList<String> stringArrayList, OpenDirection openDirection, boolean isCancelable, OnItemClickList onItemClickList,boolean isStringArrayList) {
        this.mContext = mContext;
        this.title = title;
        this.openDirection = openDirection;
        this.isCancelable = isCancelable;
        this.isStringArrayList = isStringArrayList;
        this.stringArrayList = stringArrayList;
        this.onItemClickList = onItemClickList;
    }


    public void show(int selectedItemPosition, String keyToShowAsTitle) {
        if (openDirection == OpenDirection.BOTTOM) {
            final BottomSheetDialog dialog = new BottomSheetDialog(mContext);
            listDialog = dialog;
        }

        if (openDirection == OpenDirection.CENTER) {
            AppCompatDialog dialog = new AppCompatDialog(mContext);
            Objects.requireNonNull(dialog.getWindow()).getAttributes().windowAnimations = R.style.DialogAnimation;
            listDialog = dialog;
        }

        showDialog(selectedItemPosition, keyToShowAsTitle,isStringArrayList);
    }

    private void showDialog(int selectedItemPosition, String keyToShowAsTitle,boolean isStringArrayList) {
        if (listDialog == null) {
            return;
        }

        listDialog.setContentView(R.layout.dialog_filter);
        listDialog.setCancelable(isCancelable);

        CustomDialogListAdapter listAdapter;
        if (isStringArrayList)
        {
            listAdapter = new CustomDialogListAdapter(mContext, stringArrayList, selectedItemPosition,isStringArrayList);
        }else
        {
            listAdapter = new CustomDialogListAdapter(mContext, arrayList, selectedItemPosition, keyToShowAsTitle);
        }

        ImageView closeImg = (ImageView) listDialog.findViewById(R.id.closeImg);
        MTextView titleTxt = (MTextView) listDialog.findViewById(R.id.TitleTxt);
        titleTxt.setText(title);
        closeImg.setOnClickListener(v -> listDialog.dismiss());

        RecyclerView mRecyclerView = listDialog.findViewById(R.id.mRecyclerView);
        mRecyclerView.setAdapter(listAdapter);
        listAdapter.setOnItemClickList(position -> {
            listDialog.dismiss();
            if (onItemClickList != null) {
                onItemClickList.onItemClick(position);
            }
        });

        try {
            if (!listDialog.isShowing()) {
                listDialog.show();
            }
        } catch (Exception e) {

        }
    }

    public interface OnItemClickList {
        void onItemClick(int position);
    }

    public static OpenListView getInstance(Context mContext, String title, ArrayList<HashMap<String, String>> arrayList, OpenDirection openDirection, boolean isCancelable, OnItemClickList onItemClickList) {
        return new OpenListView(mContext, title, arrayList, openDirection, isCancelable, onItemClickList);
    }

    public static OpenListView getInstance(Context mContext, String title, ArrayList<String> arrayList, OpenDirection openDirection, boolean isCancelable,boolean isStringArrayList, OnItemClickList onItemClickList) {
        return new OpenListView(mContext, title, arrayList, openDirection, isCancelable, onItemClickList,isStringArrayList);
    }
}
