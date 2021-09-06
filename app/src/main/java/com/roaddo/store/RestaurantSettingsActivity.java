package com.roaddo.store;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.dialogs.OpenListView;
import com.fragments.EditProfileFragment;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.kyleduo.switchbutton.SwitchButton;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class RestaurantSettingsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    public GeneralFunctions generalFunc;
    MTextView titleTxt;
    ImageView backImgView;

    MTextView acceptingOrdersTxtView;
    SwitchButton onlineOfflineSwitch;

    MaterialEditText estTimeEditBox;
    MaterialEditText maxTotalQTYEditBox;
    MaterialEditText minOrderPriceEditBox;

    View setRestaurantDetailsArea;
    MTextView setRestaurantDetailsTxtView;

    View setBusinessHoursArea;
    MTextView setBusinessHoursTxtView;

    MButton saveBtn;

    View containerView;

    ProgressBar loadingBar;
    String userProfileJson = "";
    boolean isFromMain = false;
    FrameLayout driverOpSelectArea, takeawayArea;
    MaterialEditText driverOpBox, takeawayBox;
    int selDriverOpPosition = -1;
    int seltakeawayPosition = -1;
    String selDriverOp = "";
    String takeAwayOp = "";
    ArrayList<HashMap<String, String>> driverOpDataList = new ArrayList<>();
    ArrayList<HashMap<String, String>> takeawayDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_settings);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        driverOpSelectArea = (FrameLayout) findViewById(R.id.driverOpSelectArea);
        takeawayArea = (FrameLayout) findViewById(R.id.takeawayArea);
        driverOpBox = (MaterialEditText) findViewById(R.id.driverOpBox);
        takeawayBox = (MaterialEditText) findViewById(R.id.takeawayBox);

        acceptingOrdersTxtView = (MTextView) findViewById(R.id.acceptingOrdersTxtView);
        onlineOfflineSwitch = (SwitchButton) findViewById(R.id.onlineOfflineSwitch);
        estTimeEditBox = (MaterialEditText) findViewById(R.id.estTimeEditBox);
        maxTotalQTYEditBox = (MaterialEditText) findViewById(R.id.maxTotalQTYEditBox);
        minOrderPriceEditBox = (MaterialEditText) findViewById(R.id.minOrderPriceEditBox);

        loadingBar = (ProgressBar) findViewById(R.id.loadingBar);
        containerView = findViewById(R.id.containerView);

        setRestaurantDetailsArea = findViewById(R.id.setRestaurantDetailsArea);
        setRestaurantDetailsTxtView = (MTextView) findViewById(R.id.setRestaurantDetailsTxtView);

        setBusinessHoursArea = findViewById(R.id.setBusinessHoursArea);
        setBusinessHoursTxtView = (MTextView) findViewById(R.id.setBusinessHoursTxtView);

        saveBtn = ((MaterialRippleLayout) findViewById(R.id.saveBtn)).getChildView();
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

        saveBtn.setId(Utils.generateViewId());

        setLabels();
        Utils.removeInput(driverOpBox);
        Utils.removeInput(takeawayBox);

        driverOpBox.setOnClickListener(new setOnClickList());
        driverOpBox.setOnTouchListener(new setOnTouchList());

        takeawayBox.setOnClickListener(new setOnClickList());
        takeawayBox.setOnTouchListener(new setOnTouchList());
        saveBtn.setOnClickListener(new setOnClickList());
        backImgView.setOnClickListener(new setOnClickList());
        onlineOfflineSwitch.setOnCheckedChangeListener(this);

        int backColor = Color.parseColor("#FFFFFF");
        int btnRadius = Utils.dipToPixels(getActContext(), 5);
        int strokeColor = Color.parseColor("#cbcbcb");
        int strokeWidth = Utils.dipToPixels(getActContext(), 1);

        isFromMain = getIntent().hasExtra("isFromMain");

        if (isFromMain) {
            setRestaurantDetailsArea.setVisibility(View.VISIBLE);
            setBusinessHoursArea.setVisibility(View.VISIBLE);

            setRestaurantDetailsArea.setOnClickListener(new setOnClickList());
            setBusinessHoursArea.setOnClickListener(new setOnClickList());

            new CreateRoundedView(backColor, btnRadius, strokeWidth, strokeColor, setBusinessHoursArea);
            new CreateRoundedView(backColor, btnRadius, strokeWidth, strokeColor, setRestaurantDetailsArea);
        }

        configRestaurantSettings("Display");


        if (generalFunc.isRTLmode()) {
            ImageView arrowImgView = (ImageView) findViewById(R.id.arrowImgView);
            ImageView businessArrowImgView = (ImageView) findViewById(R.id.businessArrowImgView);

            arrowImgView.setRotation(180);
            businessArrowImgView.setRotation(180);
        }
        JSONArray driverOptionList_Arr = generalFunc.getJsonArray("driverOptionArr", userProfileJson);
        if (driverOptionList_Arr != null) {
            for (int i = 0; i < driverOptionList_Arr.length(); i++) {
                JSONObject obj_temp = generalFunc.getJsonObject(driverOptionList_Arr, i);

                HashMap<String, String> mapData = new HashMap<>();
                mapData.put("label", generalFunc.getJsonValueStr("label", obj_temp));
                mapData.put("value", generalFunc.getJsonValueStr("value", obj_temp));
                driverOpDataList.add(mapData);

            }
        }

        if (generalFunc.retrieveValue("ENABLE_ADD_PROVIDER_FROM_STORE").equalsIgnoreCase("Yes")) {
            driverOpSelectArea.setVisibility(View.VISIBLE);
        }
        if (generalFunc.retrieveValue("ENABLE_TAKE_AWAY").equalsIgnoreCase("Yes")) {
            setTakeAwayList();
            takeawayArea.setVisibility(View.VISIBLE);
        }


    }

    public void setTakeAwayList() {
        HashMap<String, String> yesHasmap = new HashMap<>();
        yesHasmap.put("label", generalFunc.retrieveLangLBl("", "LBL_BTN_YES_TXT"));
        yesHasmap.put("value", "Yes");


        HashMap<String, String> noHasmap = new HashMap<>();
        noHasmap.put("label", generalFunc.retrieveLangLBl("", "LBL_BTN_NO_TXT"));
        noHasmap.put("value", "No");
        takeawayDataList.add(yesHasmap);
        takeawayDataList.add(noHasmap);
    }

    public class setOnTouchList implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP && !view.hasFocus()) {
                view.performClick();
            }
            return true;
        }
    }

    public void setLabels() {

        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RESTAURANT_SETTINGS"));

        acceptingOrdersTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_ACCEPTING_ORDERS"));
        estTimeEditBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_ESTIMATED_TIME_PREPARE_ORDER"));
        maxTotalQTYEditBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_MAX_ALLOW_QTY_BY_USER"));
        maxTotalQTYEditBox.setHelperText(generalFunc.retrieveLangLBl("", "LBL_MAX_ALLOW_QTY_BY_USER_HELPER"));
        maxTotalQTYEditBox.setHelperTextAlwaysShown(true);
        minOrderPriceEditBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_MIN_ORDER_PRICE"));
        driverOpBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_PROVIDER_SELECTION_TXT"));
        takeawayBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_TAKE_AWAY"));

        setRestaurantDetailsTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SET_RESTAURANT_DETAILS"));
        setBusinessHoursTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SET_BUSINESS_HOURS"));

        estTimeEditBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        maxTotalQTYEditBox.setInputType(InputType.TYPE_CLASS_NUMBER);
//        minOrderPriceEditBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        minOrderPriceEditBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        saveBtn.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SUBMIT_TXT"));
    }

    public Context getActContext() {
        return RestaurantSettingsActivity.this;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
//            onlineOfflineSwitch.setCheckedNoEvent(true);
            onlineOfflineSwitch.setThumbColorRes(R.color.white);
            onlineOfflineSwitch.setBackColorRes(R.color.Green);
        } else {
//            onlineOfflineSwitch.setCheckedNoEvent(false);
            onlineOfflineSwitch.setThumbColorRes(android.R.color.white);
            onlineOfflineSwitch.setBackColorRes(android.R.color.holo_red_dark);
        }
    }

    public void checkValues() {
        String LBL_FEILD_REQUIRD_ERROR_TXT = generalFunc.retrieveLangLBl("Required", "LBL_FEILD_REQUIRD");
        String LBL_ZERO_NOT_ALLOW = generalFunc.retrieveLangLBl("", "LBL_ZERO_NOT_ALLOW");

        int estimatedTIme = GeneralFunctions.parseIntegerValue(0, Utils.getText(estTimeEditBox));
        int maxQTY = GeneralFunctions.parseIntegerValue(0, Utils.getText(maxTotalQTYEditBox));
        boolean isESTProper = Utils.checkText(estTimeEditBox) ? (estimatedTIme > 0 ? true : Utils.setErrorFields(estTimeEditBox, LBL_ZERO_NOT_ALLOW)) : Utils.setErrorFields(estTimeEditBox, LBL_FEILD_REQUIRD_ERROR_TXT);

        boolean isMaxQTYProper = Utils.checkText(maxTotalQTYEditBox) ? (maxQTY > 0 ? true : Utils.setErrorFields(maxTotalQTYEditBox, LBL_ZERO_NOT_ALLOW)) : Utils.setErrorFields(maxTotalQTYEditBox, LBL_FEILD_REQUIRD_ERROR_TXT);


        if (driverOpSelectArea.getVisibility() == View.VISIBLE && selDriverOp.equalsIgnoreCase("")) {

            Utils.setErrorFields(driverOpBox, LBL_FEILD_REQUIRD_ERROR_TXT);
            return;
        }
        if (takeawayArea.getVisibility() == View.VISIBLE && takeAwayOp.equalsIgnoreCase("")) {

            Utils.setErrorFields(takeawayBox, LBL_FEILD_REQUIRD_ERROR_TXT);
            return;
        }


        if (isESTProper && isMaxQTYProper) {
            configRestaurantSettings("Update");
        }
    }

    public void configRestaurantSettings(String callType) {

        if (!callType.equals("Update")) {
            containerView.setVisibility(View.GONE);
            loadingBar.setVisibility(View.VISIBLE);
        }

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("type", "UpdateDisplayRestaurantStoreSettings");
        parameters.put("iCompanyId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.userType);
        parameters.put("CALL_TYPE", callType);
        parameters.put("iMaxItemQty", Utils.getText(maxTotalQTYEditBox));
        parameters.put("fPrepareTime", Utils.getText(estTimeEditBox));
        parameters.put("fMinOrderValue", Utils.getText(minOrderPriceEditBox));
        parameters.put("eDriverOption", selDriverOp);
        parameters.put("eTakeAway", takeAwayOp);
//        parameters.put("eAvailable", onlineOfflineSwitch.isChecked() == true ? "Yes" : "No");

        ExecuteWebServerUrl exeServerTask = new ExecuteWebServerUrl(getActContext(), parameters);

        if (callType.equals("Update")) {
            exeServerTask.setLoaderConfig(getActContext(), true, generalFunc);
        }

        exeServerTask.setDataResponseListener(responseString -> {
            JSONObject responseObj = generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                String vCurrency = generalFunc.getJsonValueStr("vCurrency", responseObj);

                String LBL_VALUE_NOTE_PREFIX = generalFunc.retrieveLangLBl("", "LBL_VALUE_NOTE_PREFIX");

                minOrderPriceEditBox.setHelperText(LBL_VALUE_NOTE_PREFIX + " '" + vCurrency + "'");
                minOrderPriceEditBox.setHelperTextAlwaysShown(true);

                estTimeEditBox.setHelperText(LBL_VALUE_NOTE_PREFIX + " '" + generalFunc.retrieveLangLBl("", "LBL_MINUTES_TXT") + "'");
                estTimeEditBox.setHelperTextAlwaysShown(true);


                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);

                String message = generalFunc.getJsonValueStr(Utils.message_str, responseObj);

                Bundle bn = new Bundle();
                bn.putString("msg", "" + message);
                String eStatus = generalFunc.getJsonValue("eStatus", userProfileJson);

                if (!eStatus.equalsIgnoreCase("inactive")) {
                    if (message.equals("DO_EMAIL_PHONE_VERIFY") || message.equals("DO_PHONE_VERIFY") || message.equals("DO_EMAIL_VERIFY")) {
                        accountVerificationAlert(generalFunc.retrieveLangLBl("", "LBL_ACCOUNT_VERIFY_ALERT_RESTAURANT"), bn);
                        return;
                    }
                }

                if (isDataAvail) {


                    if (!callType.equals("Update")) {
                        JSONObject msgObj = generalFunc.getJsonObject(Utils.message_str, responseObj);
                        String eAvailable = generalFunc.getJsonValueStr("eAvailable", msgObj);
                        maxTotalQTYEditBox.setText(generalFunc.getJsonValueStr("iMaxItemQty", msgObj));
//                        onlineOfflineSwitch.setCheckedNoEvent(eAvailable.equalsIgnoreCase("Yes") ? true : false);
                        estTimeEditBox.setText(generalFunc.getJsonValueStr("fPrepareTime", msgObj));
                        minOrderPriceEditBox.setText(generalFunc.getJsonValueStr("fMinOrderValue", msgObj));


                        if (generalFunc.getJsonValueStr("eTakeaway", msgObj) != null && !generalFunc.getJsonValueStr("eTakeaway", msgObj).equalsIgnoreCase("") && takeawayDataList.size()>0) {
                            takeawayBox.setText(generalFunc.getJsonValueStr("eTakeaway", msgObj));
                            if (generalFunc.getJsonValueStr("eTakeaway", msgObj).equalsIgnoreCase("Yes")) {
                                seltakeawayPosition = 0;
                                takeAwayOp=takeawayDataList.get(0).get("value");
                            } else {
                                seltakeawayPosition = 1;
                                takeAwayOp=takeawayDataList.get(1).get("value");

                            }

                        }
                        containerView.setVisibility(View.VISIBLE);
                        loadingBar.setVisibility(View.GONE);

                        if (eAvailable.equalsIgnoreCase("Yes")) {
                            onlineOfflineSwitch.setCheckedNoEvent(true);
                            onlineOfflineSwitch.setThumbColorRes(R.color.white);
                            onlineOfflineSwitch.setBackColorRes(R.color.Green);
                        } else {
                            onlineOfflineSwitch.setCheckedNoEvent(false);
                            onlineOfflineSwitch.setThumbColorRes(android.R.color.white);
                            onlineOfflineSwitch.setBackColorRes(android.R.color.holo_red_dark);
                        }

                        if (driverOpSelectArea.getVisibility() == View.VISIBLE) {
                            driverOpBox.setText(generalFunc.getJsonValueStr("eDriverOptionLabel", msgObj));
                            selDriverOp = generalFunc.getJsonValueStr("eDriverOption", msgObj);

                            if (driverOpDataList != null && driverOpDataList.size() > 0) {
                                for (int i = 0; i < driverOpDataList.size(); i++) {
                                    if (selDriverOp.equalsIgnoreCase(driverOpDataList.get(i).get("label"))) {
                                        selDriverOpPosition = i;
                                    }
                                }

                            }
                            try {
                                if (selDriverOpPosition == -1) {
                                    selDriverOpPosition = 0;
                                    selDriverOp = driverOpDataList.get(0).get("label");
                                }
                            }
                            catch (Exception e)
                            {

                            }
                        }

                    } else {
                        generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", "LBL_INFO_UPDATED_TXT"));
                    }

                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_TRY_AGAIN_LATER"), true);
                }
            } else {
                generalFunc.showError(true);
            }
        });
        exeServerTask.execute();
    }

    public void accountVerificationAlert(String message, final Bundle bn) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 1) {
                generateAlert.closeAlertBox();
                (new StartActProcess(getActContext())).startActForResult(VerifyInfoActivity.class, bn, Utils.VERIFY_INFO_REQ_CODE);
            } else if (btn_id == 0) {
                generateAlert.closeAlertBox();
            }
        });
        generateAlert.setContentMessage("", message);
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_CANCEL_TRIP_TXT"));
        generateAlert.showAlertBox();
    }

    public void showDriverOption() {
        OpenListView.getInstance(getActContext(), generalFunc.retrieveLangLBl("", "LBL_PROVIDER_SELECTION_TXT"), driverOpDataList, OpenListView.OpenDirection.CENTER, true, position -> {


            selDriverOpPosition = position;
            selDriverOp = driverOpDataList.get(position).get("label");
            driverOpBox.setText(driverOpDataList.get(position).get("value"));
        }).show(selDriverOpPosition, "value");

    }

    public void showTakeAwayOption() {
        OpenListView.getInstance(getActContext(), generalFunc.retrieveLangLBl("", "LBL_TAKE_AWAY"), takeawayDataList, OpenListView.OpenDirection.CENTER, true, position -> {


            seltakeawayPosition = position;
            takeAwayOp = takeawayDataList.get(position).get("value");
            takeawayBox.setText(takeawayDataList.get(position).get("label"));
        }).show(seltakeawayPosition, "label");

    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            int i = v.getId();
            if (i == R.id.backImgView) {
                RestaurantSettingsActivity.super.onBackPressed();
            } else if (i == R.id.setRestaurantDetailsArea) {
                (new StartActProcess(getActContext())).startAct(RestaurantDetailActivity.class);

            } else if (i == R.id.setBusinessHoursArea) {
                (new StartActProcess(getActContext())).startAct(SetWorkingHoursActivity.class);

            } else if (i == saveBtn.getId()) {
                checkValues();
            } else if (i == R.id.driverOpBox) {
                showDriverOption();

            } else if (i == R.id.takeawayBox) {
                showTakeAwayOption();
            }
        }
    }
}
