package com.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.roaddo.store.R;
import com.roaddo.store.RestaurantDetailActivity;
import com.roaddo.store.SetWorkingHoursActivity;
import com.roaddo.store.VerifyInfoActivity;
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

import org.json.JSONObject;

import java.util.HashMap;

public class ResturantSettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {

    View view;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_restaurant_settings, container, false);
        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        titleTxt = (MTextView) view.findViewById(R.id.titleTxt);
        backImgView = (ImageView) view.findViewById(R.id.backImgView);
        backImgView.setVisibility(View.GONE);

        acceptingOrdersTxtView = (MTextView) view.findViewById(R.id.acceptingOrdersTxtView);
        onlineOfflineSwitch = (SwitchButton) view.findViewById(R.id.onlineOfflineSwitch);
        estTimeEditBox = (MaterialEditText) view.findViewById(R.id.estTimeEditBox);
        maxTotalQTYEditBox = (MaterialEditText) view.findViewById(R.id.maxTotalQTYEditBox);
        minOrderPriceEditBox = (MaterialEditText) view.findViewById(R.id.minOrderPriceEditBox);

        loadingBar = (ProgressBar) view.findViewById(R.id.loadingBar);
        containerView = view.findViewById(R.id.containerView);

        setRestaurantDetailsArea = view.findViewById(R.id.setRestaurantDetailsArea);
        setRestaurantDetailsTxtView = (MTextView) view.findViewById(R.id.setRestaurantDetailsTxtView);

        setBusinessHoursArea = view.findViewById(R.id.setBusinessHoursArea);
        setBusinessHoursTxtView = (MTextView) view.findViewById(R.id.setBusinessHoursTxtView);

        saveBtn = ((MaterialRippleLayout) view.findViewById(R.id.saveBtn)).getChildView();
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

        saveBtn.setId(Utils.generateViewId());

        setLabels();

        saveBtn.setOnClickListener(new setOnClickList());

        setRestaurantDetailsArea.setOnClickListener(new setOnClickList());
        setBusinessHoursArea.setOnClickListener(new setOnClickList());

        backImgView.setOnClickListener(new setOnClickList());

        onlineOfflineSwitch.setOnCheckedChangeListener(this);

        int backColor = Color.parseColor("#FFFFFF");
        int btnRadius = Utils.dipToPixels(getActContext(), 5);
        int strokeColor = Color.parseColor("#cbcbcb");
        int strokeWidth = Utils.dipToPixels(getActContext(), 1);

        new CreateRoundedView(backColor, btnRadius, strokeWidth, strokeColor, setBusinessHoursArea);
        new CreateRoundedView(backColor, btnRadius, strokeWidth, strokeColor, setRestaurantDetailsArea);

        configRestaurantSettings("Display");

        if (generalFunc.isRTLmode()) {
            ImageView arrowImgView = (ImageView) view.findViewById(R.id.arrowImgView);
            ImageView businessArrowImgView = (ImageView) view.findViewById(R.id.businessArrowImgView);

            arrowImgView.setRotation(180);
            businessArrowImgView.setRotation(180);
        }


        return view;
    }

    public Context getActContext() {
        return getActivity();
    }


    public void setLabels() {

        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RESTAURANT_SETTINGS"));

        acceptingOrdersTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_ACCEPTING_ORDERS"));
        estTimeEditBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_ESTIMATED_TIME_PREPARE_ORDER"));
        maxTotalQTYEditBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_MAX_ALLOW_QTY_BY_USER"));
        //maxTotalQTYEditBox.setHelperText("user can add");
        maxTotalQTYEditBox.setHelperText(generalFunc.retrieveLangLBl("", "LBL_MAX_ALLOW_QTY_BY_USER_HELPER"));
        maxTotalQTYEditBox.setHelperTextAlwaysShown(true);

        minOrderPriceEditBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_MIN_ORDER_PRICE"));

        setRestaurantDetailsTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SET_RESTAURANT_DETAILS"));
        setBusinessHoursTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SET_BUSINESS_HOURS"));

        estTimeEditBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        maxTotalQTYEditBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        minOrderPriceEditBox.setInputType(InputType.TYPE_CLASS_NUMBER);

        saveBtn.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SUBMIT_TXT"));
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
//            onlineOfflineSwitch.setCheckedNoEvent(true);
            onlineOfflineSwitch.setThumbColorRes(R.color.white);
            onlineOfflineSwitch.setBackColorRes(R.color.appThemeColor_1);
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
                bn.putString("msg", "DO_PHONE_VERIFY");
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

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            int i = v.getId();
            if (i == R.id.setRestaurantDetailsArea) {
                (new StartActProcess(getActContext())).startAct(RestaurantDetailActivity.class);

            } else if (i == R.id.setBusinessHoursArea) {
                (new StartActProcess(getActContext())).startAct(SetWorkingHoursActivity.class);

            } else if (i == saveBtn.getId()) {
                checkValues();
            }
        }
    }
}
