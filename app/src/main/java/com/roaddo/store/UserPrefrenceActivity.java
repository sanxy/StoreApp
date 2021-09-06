package com.roaddo.store;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.adapter.files.MoreInstructionAdapter;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class UserPrefrenceActivity extends AppCompatActivity {


    RecyclerView moreInstruction;
    private GeneralFunctions generalFunc;
    MoreInstructionAdapter moreInstructionAdapter;
    LinearLayout moreInstructionLayout;
    ArrayList<HashMap<String, String>> instructionsList;
    MTextView titleTxt;
    boolean isPreference=false;
    ImageView backImgView;
    LinearLayout specialInstructionLayout;
    MTextView specialInstruction;
    MTextView specialInstructionDetail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_prefrence);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());


        specialInstructionLayout = findViewById(R.id.specialInstructionLayout);
        specialInstruction = findViewById(R.id.specialinstruction);
        specialInstructionDetail = findViewById(R.id.specialinstructiondetail);


        if (getIntent().getExtras().getString("vInstruction")!=null){
            specialInstructionLayout.setVisibility(View.VISIBLE);
            specialInstructionDetail.setText(getIntent().getExtras().getString("vInstruction"));
            specialInstruction.setText(generalFunc.retrieveLangLBl("Special Instruction", "LBL_FOOD_INSTRUCTION_TXT"));
        }

        titleTxt = findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        instructionsList = new ArrayList<>();
        moreInstructionLayout = findViewById(R.id.moreinstructionLyout);

        moreInstruction = findViewById(R.id.moreinstuction);
        moreInstructionAdapter = new MoreInstructionAdapter(getActContext(),instructionsList, new MoreInstructionAdapter.OnItemCheckListener() {
            @Override
            public void onItemCheck(HashMap<String, String> map) {

            }

        });
        moreInstruction.setAdapter(moreInstructionAdapter);

        JSONObject DeliveryPreferences = null;
        try {
            DeliveryPreferences = new JSONObject(getIntent().getStringExtra("DeliveryPreferences"));
            getUserPreference(DeliveryPreferences);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        backImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserPrefrenceActivity.super.onBackPressed();
            }
        });

    }



    public void getUserPreference(JSONObject DeliveryPreferences) {
        JSONArray Data = generalFunc.getJsonArray("Data",DeliveryPreferences);
        titleTxt.setText(generalFunc.getJsonValueStr("vTitle",DeliveryPreferences));

        if (Data!=null) {
            for (int i = 0; i < Data.length(); i++) {
                try {
                    JSONObject jsonObject = (JSONObject) Data.get(i);
                    String tTitle = generalFunc.getJsonValueStr("tTitle", jsonObject);
                    String tDescription = generalFunc.getJsonValueStr("tDescription", jsonObject);
                    String ePreferenceFor = generalFunc.getJsonValueStr("ePreferenceFor", jsonObject);
                    String eImageUpload = generalFunc.getJsonValueStr("eImageUpload", jsonObject);
                    String iDisplayOrder = generalFunc.getJsonValueStr("iDisplayOrder", jsonObject);
                    String eContactLess = generalFunc.getJsonValueStr("eContactLess", jsonObject);
                    String eStatus = generalFunc.getJsonValueStr("eStatus", jsonObject);
                    String iPreferenceId = generalFunc.getJsonValueStr("iPreferenceId", jsonObject);
                    HashMap<String, String> hashMap = new HashMap<>();

                    hashMap.put("tTitle", tTitle);
                    hashMap.put("tDescription", tDescription);
                    hashMap.put("ePreferenceFor", ePreferenceFor);
                    hashMap.put("eImageUpload", eImageUpload);
                    hashMap.put("iDisplayOrder", iDisplayOrder);
                    hashMap.put("eContactLess", eContactLess);
                    hashMap.put("eStatus", eStatus);
                    hashMap.put("iPreferenceId", iPreferenceId);
                    instructionsList.add(hashMap);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                moreInstructionAdapter.notifyDataSetChanged();
            }
        }
    }


    public Context getActContext() {
        return UserPrefrenceActivity.this;
    }
}
