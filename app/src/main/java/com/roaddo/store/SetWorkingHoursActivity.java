package com.roaddo.store;

import android.content.Context;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.datepicker.files.SlideDateTimeListener;
import com.datepicker.files.SlideDateTimePicker;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Admin on 24-05-18.
 */

public class SetWorkingHoursActivity extends AppCompatActivity {

    GeneralFunctions generalFunc;
    MButton submitBtn;
    ImageView backImgView;
    MTextView titleTxt;

    MTextView fromtimeSlotMonVTxt;
    MTextView totimeSlotFriVTxt;
    MTextView fromtimeSlotTwoMonVTxt;
    MTextView totimeSlotTwoFriVTxt;
    MTextView fromtimeSlotSatVTxt;
    MTextView totimeSlotSunVTxt;
    MTextView fromtimeSlotTwoSatVTxt;
    MTextView totimeSlotTwoSunVTxt;

    View slotMonCalenderArea;
    View slotFriCalenderArea;
    View slotTwoMonCalenderArea;
    View slotTwoFriCalenderArea;
    View slotSatCalenderArea;
    View slotSunCalenderArea;
    View slotTwoSatCalenderArea;
    View slotTwoSunCalenderArea;

    MTextView monfriSlotOneTxtView;
    MTextView monfriSlotTwoTxtView;
    MTextView satSunSlotOneTxtView;
    MTextView satSunSlotTwoTxtView;

    String required_str = "";
    String error_email_str = "";

    JSONObject userProfileJsonObj;
    String iCompanyId = "";

    View loadingBar;
    View containerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_working_hour);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        iCompanyId = generalFunc.getJsonValueStr("iCompanyId", userProfileJsonObj);

        submitBtn = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);

        monfriSlotOneTxtView = (MTextView) findViewById(R.id.monfriSlotOneTxtView);
        monfriSlotTwoTxtView = (MTextView) findViewById(R.id.monfriSlotTwoTxtView);
        satSunSlotOneTxtView = (MTextView) findViewById(R.id.satSunSlotOneTxtView);
        satSunSlotTwoTxtView = (MTextView) findViewById(R.id.satSunSlotTwoTxtView);

        fromtimeSlotMonVTxt = (MTextView) findViewById(R.id.fromtimeSlotMonVTxt);
        totimeSlotFriVTxt = (MTextView) findViewById(R.id.totimeSlotFriVTxt);
        fromtimeSlotTwoMonVTxt = (MTextView) findViewById(R.id.fromtimeSlotTwoMonVTxt);
        totimeSlotTwoFriVTxt = (MTextView) findViewById(R.id.totimeSlotTwoFriVTxt);
        fromtimeSlotSatVTxt = (MTextView) findViewById(R.id.fromtimeSlotSatVTxt);
        totimeSlotSunVTxt = (MTextView) findViewById(R.id.totimeSlotSunVTxt);
        fromtimeSlotTwoSatVTxt = (MTextView) findViewById(R.id.fromtimeSlotTwoSatVTxt);
        totimeSlotTwoSunVTxt = (MTextView) findViewById(R.id.totimeSlotTwoSunVTxt);

        loadingBar = findViewById(R.id.loadingBar);
        containerView = findViewById(R.id.containerView);


        slotMonCalenderArea = findViewById(R.id.slotMonCalenderArea);
        slotMonCalenderArea.setOnClickListener(new setOnClickList());

        slotFriCalenderArea = findViewById(R.id.slotFriCalenderArea);
        slotFriCalenderArea.setOnClickListener(new setOnClickList());

        slotTwoMonCalenderArea = findViewById(R.id.slotTwoMonCalenderArea);
        slotTwoMonCalenderArea.setOnClickListener(new setOnClickList());

        slotTwoFriCalenderArea = findViewById(R.id.slotTwoFriCalenderArea);
        slotTwoFriCalenderArea.setOnClickListener(new setOnClickList());

        slotSatCalenderArea = findViewById(R.id.slotSatCalenderArea);
        slotSatCalenderArea.setOnClickListener(new setOnClickList());

        slotSunCalenderArea = findViewById(R.id.slotSunCalenderArea);
        slotSunCalenderArea.setOnClickListener(new setOnClickList());

        slotTwoSatCalenderArea = findViewById(R.id.slotTwoSatCalenderArea);
        slotTwoSatCalenderArea.setOnClickListener(new setOnClickList());

        slotTwoSunCalenderArea = findViewById(R.id.slotTwoSunCalenderArea);
        slotTwoSunCalenderArea.setOnClickListener(new setOnClickList());

        submitBtn.setId(Utils.generateViewId());
        submitBtn.setOnClickListener(new setOnClickList());
        backImgView.setOnClickListener(new setOnClickList());

        setData();

        sendTimeData("Display");
    }

    public void setData() {
        titleTxt.setText(generalFunc.retrieveLangLBl("SET TIMINGS", "LBL_SET_TIMING"));
        submitBtn.setText(generalFunc.retrieveLangLBl("", "LBL_SAVE_ADDRESS_TXT")); //LBL_BTN_SUBMIT_TXT

        monfriSlotOneTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_MON_TO_FRI_SLOT1"));
        monfriSlotTwoTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_MON_TO_FRI_SLOT2"));
        satSunSlotOneTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SAT_AND_SUN_SLOT1"));
        satSunSlotTwoTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SAT_AND_SUN_SLOT2"));


        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD");
        error_email_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_EMAIL_ERROR_TXT");
    }

    public Context getActContext() {
        return SetWorkingHoursActivity.this;
    }

    public void checkData() {

        if (Utils.getText(fromtimeSlotMonVTxt).equalsIgnoreCase("00:00") || Utils.getText(totimeSlotFriVTxt).equalsIgnoreCase("00:00")) {
            Snackbar.make(findViewById(android.R.id.content), generalFunc.retrieveLangLBl("", "LBL_SELECT_MON_FRI_SLT1"), Snackbar.LENGTH_LONG)
                    .show();
            return;
        } else if (Utils.getText(fromtimeSlotSatVTxt).equalsIgnoreCase("00:00") ||
                Utils.getText(totimeSlotSunVTxt).equalsIgnoreCase("00:00")) {
            Snackbar.make(findViewById(android.R.id.content), generalFunc.retrieveLangLBl("", "LBL_SELECT_SAT_SUN_SLT1"), Snackbar.LENGTH_LONG)
                    .show();
            return;
        } else if (!Utils.getText(fromtimeSlotTwoMonVTxt).equalsIgnoreCase("00:00") &&
                Utils.getText(totimeSlotTwoFriVTxt).equalsIgnoreCase("00:00")) {
            Snackbar.make(findViewById(android.R.id.content), generalFunc.retrieveLangLBl("", "LBL_MON_FRI_SLT2_RESTRICT"), Snackbar.LENGTH_LONG)
                    .show();
            return;
        } else if (!Utils.getText(fromtimeSlotTwoSatVTxt).equalsIgnoreCase("00:00") &&
                Utils.getText(totimeSlotTwoSunVTxt).equalsIgnoreCase("00:00")) {
            Snackbar.make(findViewById(android.R.id.content), generalFunc.retrieveLangLBl("", "LBL_SAT_SUN_SLT2_RESTRICT"), Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        /*generalFunc.retrieveLangLBl("","LB_SET_TIMING")*/

        sendTimeData("Update");
    }

    public void sendTimeData(String callType) {

        if (callType.equalsIgnoreCase("Display")) {
            containerView.setVisibility(View.GONE);
            loadingBar.setVisibility(View.VISIBLE);
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateCompanyTiming");
        parameters.put("iCompanyId", iCompanyId);
        parameters.put("vFromMonFriTimeSlot1", Utils.getText(fromtimeSlotMonVTxt));
        parameters.put("vToMonFriTimeSlot1", Utils.getText(totimeSlotFriVTxt));
        parameters.put("vFromMonFriTimeSlot2", Utils.getText(fromtimeSlotTwoMonVTxt));
        parameters.put("vToMonFriTimeSlot2", Utils.getText(totimeSlotTwoFriVTxt));
        parameters.put("vFromSatSunTimeSlot1", Utils.getText(fromtimeSlotSatVTxt));
        parameters.put("vToSatSunTimeSlot1", Utils.getText(totimeSlotSunVTxt));
        parameters.put("vFromSatSunTimeSlot2", Utils.getText(fromtimeSlotTwoSatVTxt));
        parameters.put("vToSatSunTimeSlot2", Utils.getText(totimeSlotTwoSunVTxt));
        parameters.put("CALL_TYPE", callType);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        if (callType.equalsIgnoreCase("Display")) {
            exeWebServer.setLoaderConfig(getActContext(), false, generalFunc);
        } else {
            exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        }
        exeWebServer.setDataResponseListener(responseString -> {
            loadingBar.setVisibility(View.GONE);
            if (responseString != null && !responseString.equals("")) {

                if (generalFunc.checkDataAvail(Utils.action_str, responseString) == true) {

                    if (callType.equalsIgnoreCase("Display")) {

                        JSONObject msg_obj = generalFunc.getJsonObject(Utils.message_str, responseString);

                        String vFromMonFriTimeSlot1 = Utils.formatDate("HH:mm:ss", "HH:mm", generalFunc.getJsonValueStr("vFromMonFriTimeSlot1", msg_obj));
                        String vToMonFriTimeSlot1 = Utils.formatDate("HH:mm:ss", "HH:mm", generalFunc.getJsonValueStr("vToMonFriTimeSlot1", msg_obj));
                        String vFromMonFriTimeSlot2 = Utils.formatDate("HH:mm:ss", "HH:mm", generalFunc.getJsonValueStr("vFromMonFriTimeSlot2", msg_obj));
                        String vToMonFriTimeSlot2 = Utils.formatDate("HH:mm:ss", "HH:mm", generalFunc.getJsonValueStr("vToMonFriTimeSlot2", msg_obj));
                        String vFromSatSunTimeSlot1 = Utils.formatDate("HH:mm:ss", "HH:mm", generalFunc.getJsonValueStr("vFromSatSunTimeSlot1", msg_obj));
                        String vToSatSunTimeSlot1 = Utils.formatDate("HH:mm:ss", "HH:mm", generalFunc.getJsonValueStr("vToSatSunTimeSlot1", msg_obj));
                        String vFromSatSunTimeSlot2 = Utils.formatDate("HH:mm:ss", "HH:mm", generalFunc.getJsonValueStr("vFromSatSunTimeSlot2", msg_obj));
                        String vToSatSunTimeSlot2 = Utils.formatDate("HH:mm:ss", "HH:mm", generalFunc.getJsonValueStr("vToSatSunTimeSlot2", msg_obj));

                        fromtimeSlotMonVTxt.setText(generalFunc.convertNumberWithRTL(vFromMonFriTimeSlot1));
                        totimeSlotFriVTxt.setText(generalFunc.convertNumberWithRTL(vToMonFriTimeSlot1));
                        fromtimeSlotTwoMonVTxt.setText(generalFunc.convertNumberWithRTL(vFromMonFriTimeSlot2));
                        totimeSlotTwoFriVTxt.setText(generalFunc.convertNumberWithRTL(vToMonFriTimeSlot2));
                        fromtimeSlotSatVTxt.setText(generalFunc.convertNumberWithRTL(vFromSatSunTimeSlot1));
                        totimeSlotSunVTxt.setText(generalFunc.convertNumberWithRTL(vToSatSunTimeSlot1));
                        fromtimeSlotTwoSatVTxt.setText(generalFunc.convertNumberWithRTL(vFromSatSunTimeSlot2));
                        totimeSlotTwoSunVTxt.setText(generalFunc.convertNumberWithRTL(vToSatSunTimeSlot2));

//                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)), false);
                    } else {

                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)), true);
//                        generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    }


                    containerView.setVisibility(View.VISIBLE);
                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)), true);
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void selectTimeSlot(MTextView txtView) {
//        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
//        int minute = mcurrentTime.get(Calendar.MINUTE);
//        TimePickerDialog mTimePicker;
//        mTimePicker = new TimePickerDialog(getActContext(), (timePicker, selectedHour, selectedMinute) -> txtView.setText(String.format("%02d", selectedHour) + ":" + String.format("%02d", selectedMinute)), hour, minute, true);
//        mTimePicker.setTitle(generalFunc.retrieveLangLBl("Select Time", "LBL_SELECT_TIME_TXT"));
//        mTimePicker.show();

//        Calendar mCurrCal = Calendar.getInstance();
//        mCurrCal.set(Calendar.HOUR,GeneralFunctions.parseIntegerValue(0,Utils.getText(totimeSlotFriVTxt).toString().split(":")[0]));
//        mCurrCal.set(Calendar.MINUTE,GeneralFunctions.parseIntegerValue(0,Utils.getText(totimeSlotFriVTxt).toString().split(":")[1]));


        int i = txtView.getId();
        if (i == R.id.totimeSlotFriVTxt) {
            if (GeneralFunctions.parseIntegerValue(0, Utils.getText(fromtimeSlotMonVTxt).replace(":", "")) < 1) {
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_ADD_FROM_TIME"));
                return;
            }
        } else if (i == R.id.totimeSlotTwoFriVTxt) {
            if (GeneralFunctions.parseIntegerValue(0, Utils.getText(fromtimeSlotTwoMonVTxt).replace(":", "")) < 1) {
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_ADD_FROM_TIME"));
                return;
            }
        } else if (i == R.id.totimeSlotSunVTxt) {
            if (GeneralFunctions.parseIntegerValue(0, Utils.getText(fromtimeSlotSatVTxt).replace(":", "")) < 1) {
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_ADD_FROM_TIME"));
                return;
            }
        } else if (i == R.id.totimeSlotTwoSunVTxt) {
            if (GeneralFunctions.parseIntegerValue(0, Utils.getText(fromtimeSlotTwoSatVTxt).replace(":", "")) < 1) {
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_ADD_FROM_TIME"));
                return;
            }
        }

        new SlideDateTimePicker.Builder(getSupportFragmentManager())
                .setListener(new SlideDateTimeListener() {
                    @Override
                    public void onDateTimeSet(Date date) {
                        String selectedTime = Utils.convertDateToFormat("HH:mm", date);

                        boolean isSetTime = true;

                        switch (txtView.getId()) {
                            /*case R.id.fromtimeSlotMonVTxt:
                                if (GeneralFunctions.parseIntegerValue(0, Utils.getText(totimeSlotFriVTxt).replace(":", "")) != 0 && GeneralFunctions.parseIntegerValue(0, Utils.getText(totimeSlotFriVTxt).replace(":", "")) < GeneralFunctions.parseIntegerValue(0, selectedTime.replace(":", ""))) {
                                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_FROM_DATE_RESTRICT"));
                                    isSetTime = false;
                                }
                                break;*/
                            case R.id.totimeSlotFriVTxt:
                                /*if (GeneralFunctions.parseIntegerValue(0, Utils.getText(fromtimeSlotMonVTxt).replace(":", "")) > GeneralFunctions.parseIntegerValue(0, selectedTime.replace(":", ""))) {
                                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_TO_DATE_RESTRICT"));
                                    isSetTime = false;
                                } else {*/
//                                fromtimeSlotTwoMonVTxt.setText("00:00");
//                                totimeSlotTwoFriVTxt.setText("00:00");
                                /*}*/
                                break;
                            case R.id.fromtimeSlotTwoMonVTxt:
                                /*if (GeneralFunctions.parseIntegerValue(0, Utils.getText(totimeSlotTwoFriVTxt).replace(":", "")) != 0 && GeneralFunctions.parseIntegerValue(0, Utils.getText(totimeSlotTwoFriVTxt).replace(":", "")) < GeneralFunctions.parseIntegerValue(0, selectedTime.replace(":", ""))) {
                                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_FROM_DATE_RESTRICT"));
                                    isSetTime = false;
                                } else */
                                if (GeneralFunctions.parseIntegerValue(0, selectedTime.replace(":", "")) < GeneralFunctions.parseIntegerValue(0, Utils.getText(totimeSlotFriVTxt).replace(":", ""))) {
                                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_SLT_2_FRM_RESTRICT"));
                                    isSetTime = false;
                                }
                                break;
                            /*case R.id.totimeSlotTwoFriVTxt:
                                if (GeneralFunctions.parseIntegerValue(0, Utils.getText(fromtimeSlotTwoMonVTxt).replace(":", "")) > GeneralFunctions.parseIntegerValue(0, selectedTime.replace(":", ""))) {
                                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_TO_DATE_RESTRICT"));
                                    isSetTime = false;
                                }
                                break;*/
                            /*case R.id.fromtimeSlotSatVTxt:
                                if (GeneralFunctions.parseIntegerValue(0, Utils.getText(totimeSlotSunVTxt).replace(":", "")) != 0 && GeneralFunctions.parseIntegerValue(0, Utils.getText(totimeSlotSunVTxt).replace(":", "")) < GeneralFunctions.parseIntegerValue(0, selectedTime.replace(":", ""))) {
                                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_FROM_DATE_RESTRICT"));
                                    isSetTime = false;
                                }
                                break;*/
                            case R.id.totimeSlotSunVTxt:
                                /*if (GeneralFunctions.parseIntegerValue(0, Utils.getText(fromtimeSlotSatVTxt).replace(":", "")) > GeneralFunctions.parseIntegerValue(0, selectedTime.replace(":", ""))) {
                                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_TO_DATE_RESTRICT"));
                                    isSetTime = false;
                                } else {*/
//                                fromtimeSlotTwoSatVTxt.setText("00:00");
//                                totimeSlotTwoSunVTxt.setText("00:00");
                                /*}*/
                                break;
                            case R.id.fromtimeSlotTwoSatVTxt:
                                /*if (GeneralFunctions.parseIntegerValue(0, Utils.getText(totimeSlotTwoSunVTxt).replace(":", "")) != 0 && GeneralFunctions.parseIntegerValue(0, Utils.getText(totimeSlotTwoSunVTxt).replace(":", "")) < GeneralFunctions.parseIntegerValue(0, selectedTime.replace(":", ""))) {
                                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_FROM_DATE_RESTRICT"));
                                    isSetTime = false;
                                } else */
                                if (GeneralFunctions.parseIntegerValue(0, selectedTime.replace(":", "")) < GeneralFunctions.parseIntegerValue(0, Utils.getText(totimeSlotSunVTxt).replace(":", ""))) {
                                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_SLT_2_FRM_RESTRICT"));
                                    isSetTime = false;
                                }
                                break;
                            /*case R.id.totimeSlotTwoSunVTxt:
                                if (GeneralFunctions.parseIntegerValue(0, Utils.getText(fromtimeSlotTwoSatVTxt).replace(":", "")) > GeneralFunctions.parseIntegerValue(0, selectedTime.replace(":", ""))) {
                                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_TO_DATE_RESTRICT"));
                                    isSetTime = false;
                                }
                                break;*/
                        }


                        if (isSetTime) {
                            txtView.setText(selectedTime);
                        }

                    }

                    @Override
                    public void onDateTimeCancel() {

                    }

                })
                .setDatePickerEnabled(false)
                .setTimePickerEnabled(true)
                .setPreSetTimeEnabled(Utils.checkText(txtView.toString()) && !txtView.getText().toString().equalsIgnoreCase("00:00")? true : false)
                .setPreSelectedTime(Utils.checkText(txtView.toString()) && !txtView.getText().toString().equalsIgnoreCase("00:00")? txtView.getText().toString() : "")
                .setInitialDate(new Date())
                .setMaxDate(new Date())
                .setIs24HourTime(false)
                .setIndicatorColor(getResources().getColor(R.color.appThemeColor_2))
                .build()
                .show();
    }

    // slot one mon-fri

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();

            if (i == submitBtn.getId()) {
                checkData();
            } else if (i == R.id.backImgView) {
                SetWorkingHoursActivity.this.onBackPressed();
            } else if (i == R.id.slotMonCalenderArea) {
                selectTimeSlot(fromtimeSlotMonVTxt);
            } else if (i == R.id.slotFriCalenderArea) {
                selectTimeSlot(totimeSlotFriVTxt);
            } else if (i == R.id.slotTwoMonCalenderArea) {
                int slotToTime = GeneralFunctions.parseIntegerValue(0, Utils.getText(totimeSlotFriVTxt).replace(":", ""));
                if (slotToTime > 0) {
                    selectTimeSlot(fromtimeSlotTwoMonVTxt);
                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_SELECT_MON_FRI_SLT_1"));
                }
            } else if (i == R.id.slotTwoFriCalenderArea) {
                int slotToTime = GeneralFunctions.parseIntegerValue(0, Utils.getText(totimeSlotFriVTxt).replace(":", ""));
                if (slotToTime > 0) {
                    selectTimeSlot(totimeSlotTwoFriVTxt);
                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_SELECT_MON_FRI_SLT_1"));
                }
            } else if (i == R.id.slotSatCalenderArea) {
                selectTimeSlot(fromtimeSlotSatVTxt);
            } else if (i == R.id.slotSunCalenderArea) {
                selectTimeSlot(totimeSlotSunVTxt);
            } else if (i == R.id.slotTwoSatCalenderArea) {
                int slotToTime = GeneralFunctions.parseIntegerValue(0, Utils.getText(totimeSlotSunVTxt).replace(":", ""));
                if (slotToTime > 0) {
                    selectTimeSlot(fromtimeSlotTwoSatVTxt);
                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_SELECT_SAT_SUN_SLT_1"));
                }
            } else if (i == R.id.slotTwoSunCalenderArea) {
                int slotToTime = GeneralFunctions.parseIntegerValue(0, Utils.getText(totimeSlotSunVTxt).replace(":", ""));
                if (slotToTime > 0) {
                    selectTimeSlot(totimeSlotTwoSunVTxt);
                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_SELECT_SAT_SUN_SLT_1"));
                }
            }
        }
    }

}
