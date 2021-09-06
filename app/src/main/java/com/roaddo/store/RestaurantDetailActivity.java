package com.roaddo.store;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.dialogs.OpenListView;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 24-05-18.
 */

public class RestaurantDetailActivity extends AppCompatActivity {

    GeneralFunctions generalFunc;
    MButton submitBtn;
    ImageView backImgView;
    MTextView titleTxt;

    MaterialEditText contactPersonNameBox;
    MaterialEditText restaurantLocationOnMapBox;
    MaterialEditText restaurantAddressBox;
    MaterialEditText stateBox;
    MaterialEditText cityBox;
    MaterialEditText zipBox;
    MaterialEditText estTimeEditBox;
    MaterialEditText maxTotalQTYEditBox;
    MaterialEditText minOrderPriceEditBox;

    MTextView detailsHTxtView;
    MTextView settingsHTxtView;

    View settingsArea;

    View restaurantLocationSelectArea;

    String required_str = "";
    String error_email_str = "";

    JSONObject userProfileJsonObj;
    String vCountryCode = "";
    String iCompanyId = "";

    GenerateAlertBox list_state;
    GenerateAlertBox list_city;

    ArrayList<HashMap<String, String>> stateDataList = new ArrayList<>();
    ArrayList<HashMap<String, String>> cityDataList = new ArrayList<>();

    /*ArrayList<String> items_txt_state = new ArrayList<String>();
    ArrayList<String> items_txt_state_code = new ArrayList<String>();
    ArrayList<String> items_txt_state_id = new ArrayList<String>();

    ArrayList<String> items_txt_iCityId = new ArrayList<String>();
    ArrayList<String> items_txt_vCity = new ArrayList<String>();
    ArrayList<String> items_txt_eStatus = new ArrayList<String>();*/

    String selected_iStateId = "";
    String selected_vState = "";

    String selected_iCityId = "";
    String selected_vCity = "";
    String restaurantLocationAddress = "";

    String latitude = "";
    String longitude = "";

    View containerView;
    ProgressBar loadingBar;
    FrameLayout citySelectArea;

    int cityPosition=-1;
    int statePosition=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        vCountryCode = generalFunc.getJsonValueStr("vCountry", userProfileJsonObj);
        iCompanyId = generalFunc.getJsonValueStr("iCompanyId", userProfileJsonObj);
        citySelectArea = (FrameLayout) findViewById(R.id.citySelectArea);

        if (generalFunc.getJsonValueStr("SHOW_CITY_FIELD", userProfileJsonObj).equalsIgnoreCase("Yes")) {
            citySelectArea.setVisibility(View.VISIBLE);
        } else {
            citySelectArea.setVisibility(View.GONE);

        }

        submitBtn = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);


        detailsHTxtView = (MTextView) findViewById(R.id.detailsHTxtView);
        settingsHTxtView = (MTextView) findViewById(R.id.settingsHTxtView);

        contactPersonNameBox = (MaterialEditText) findViewById(R.id.contactPersonNameBox);
        restaurantLocationOnMapBox = (MaterialEditText) findViewById(R.id.restaurantLocationOnMapBox);
        restaurantAddressBox = (MaterialEditText) findViewById(R.id.restaurantAddressBox);
        stateBox = (MaterialEditText) findViewById(R.id.stateBox);
        cityBox = (MaterialEditText) findViewById(R.id.cityBox);
        zipBox = (MaterialEditText) findViewById(R.id.zipBox);
        loadingBar = (ProgressBar) findViewById(R.id.loadingBar);
        restaurantLocationSelectArea = findViewById(R.id.restaurantLocationSelectArea);
        settingsArea = findViewById(R.id.settingsArea);

        estTimeEditBox = (MaterialEditText) findViewById(R.id.estTimeEditBox);
        maxTotalQTYEditBox = (MaterialEditText) findViewById(R.id.maxTotalQTYEditBox);
        minOrderPriceEditBox = (MaterialEditText) findViewById(R.id.minOrderPriceEditBox);

        containerView = findViewById(R.id.containerView);


        setData();

        removeInput();

        restaurantAddressBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        restaurantAddressBox.setSingleLine(false);
        restaurantAddressBox.setMaxLines(4);

        restaurantLocationOnMapBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        restaurantLocationOnMapBox.setSingleLine(false);
        restaurantLocationOnMapBox.setMaxLines(4);

        submitBtn.setId(Utils.generateViewId());
        submitBtn.setOnClickListener(new setOnClickList());
        backImgView.setOnClickListener(new setOnClickList());
        stateBox.setOnClickListener(new setOnClickList());
        cityBox.setOnClickListener(new setOnClickList());

        restaurantLocationOnMapBox.setPaddings(generalFunc.isRTLmode() ? Utils.dipToPixels(getActContext(), 34) : 0, 0, generalFunc.isRTLmode() ? 0 : Utils.dipToPixels(getActContext(), 34), 0);

//        restaurantLocationOnMapBox.setPaddingRelative(0, 0, Utils.dipToPixels(getActContext(), 34), 0);

        if (isResSettingsVisible()) {

            detailsHTxtView.setVisibility(View.VISIBLE);
            settingsHTxtView.setVisibility(View.VISIBLE);
            settingsArea.setVisibility(View.VISIBLE);

        }
        sendRestaurantDetail("Display");


    }

    private boolean isResSettingsVisible() {
        if (getIntent().getStringExtra("IS_OPEN_FROM_STEPPERS") != null && getIntent().getStringExtra("IS_OPEN_FROM_STEPPERS").equalsIgnoreCase("Yes")) {
            return true;
        }
        return false;
    }

    private void setData() {

        detailsHTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_CONTACT_ADDRESS"));
        settingsHTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_ORDER_SETTINGS"));
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RESTAURANT_DETAILS"));
        submitBtn.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SUBMIT_TXT"));

        contactPersonNameBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CONTACT_PERSON_NAME"));
        restaurantLocationOnMapBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_RESTAURANT_LOCATION_MAP"));
        restaurantAddressBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_RESTAURANT_ADDRESS"));

        restaurantAddressBox.setHelperText(generalFunc.retrieveLangLBl("","LBL_STORE_ADDRESS_HELPER_TXT"));
        restaurantAddressBox.setHelperTextAlwaysShown(true);

        stateBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_STATE_TXT"));
        cityBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CITY_TXT"));
        zipBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_ZIP_TXT"));
        zipBox.setInputType(InputType.TYPE_CLASS_NUMBER);

        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD");
        error_email_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_EMAIL_ERROR_TXT");

        estTimeEditBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_ESTIMATED_TIME_PREPARE_ORDER"));
        maxTotalQTYEditBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_MAX_ALLOW_QTY_BY_USER"));
        maxTotalQTYEditBox.setHelperText(generalFunc.retrieveLangLBl("", "LBL_MAX_ALLOW_QTY_BY_USER_HELPER"));
        maxTotalQTYEditBox.setHelperTextAlwaysShown(true);
        minOrderPriceEditBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_MIN_ORDER_PRICE"));

        estTimeEditBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        maxTotalQTYEditBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        minOrderPriceEditBox.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    public void buildStateList() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GetStatesFromCountry");
        parameters.put("iCompanyId", iCompanyId);
        parameters.put("vCountry", vCountryCode);
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseObj = generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);

                if (isDataAvail == true) {

                    String totalValues = generalFunc.getJsonValueStr("totalValues", responseObj);
                    JSONArray arr_stateList = generalFunc.getJsonArray("StateList", responseObj);

                    stateDataList.clear();

                    for (int i = 0; i < arr_stateList.length(); i++) {
                        JSONObject jobj = generalFunc.getJsonObject(arr_stateList, i);

                        HashMap<String, String> mapData = new HashMap<>();
                        mapData.put("vState", generalFunc.getJsonValueStr("vState", jobj));
                        mapData.put("vStateCode", generalFunc.getJsonValueStr("vStateCode", jobj));
                        mapData.put("iStateId", generalFunc.getJsonValueStr("iStateId", jobj));

                        stateDataList.add(mapData);

                        //selected_iStateId = generalFunc.getJsonValueStr("iStateId", jobj);
//                        selected_vState = generalFunc.getJsonValueStr("vState", jobj);

                        //stateBox.setText(generalFunc.getJsonValueStr("vState", jobj));
                    }


                    GenerateAlertBox generateAlertBox = new GenerateAlertBox(getActContext());

                    generateAlertBox.setContentMessage(getSelectStateText(), null);
                    generateAlertBox.setCancelable(true);
                    generateAlertBox.createList(stateDataList, "vState", (position) -> {

                        if (list_state != null) {
                            list_state.closeAlertBox();
                        }

                        HashMap<String, String> mapData = stateDataList.get(position);

                        selected_iStateId = mapData.get("iStateId");
                        selected_vState = mapData.get("vState");
                        generalFunc.storeData(Utils.DEFAULT_STATE_VALUE, mapData.get("vState"));
                        stateBox.setText(mapData.get("vState"));

                        selected_vCity = "";
                        selected_iCityId = "";
                        cityBox.setText("");

                    });

                    list_state = generateAlertBox;


                    showStateList();

                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void buildCityList() {
        if (selected_iStateId.equals("")) {
            generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", "LBL_SELECT_STATE_NOTE"));
            return;
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GetCityFromState");
        parameters.put("iCompanyId", iCompanyId);
        parameters.put("iStateId", selected_iStateId);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                JSONObject responseObj = generalFunc.getJsonObject(responseString);

                if (responseObj != null && !responseObj.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);

                    if (isDataAvail == true) {

                        String totalValues = generalFunc.getJsonValueStr("totalValues", responseObj);
                        JSONArray arr_stateList = generalFunc.getJsonArray("CityList", responseObj);
                        cityDataList.clear();
                        for (int i = 0; i < arr_stateList.length(); i++) {
                            JSONObject jobj = generalFunc.getJsonObject(arr_stateList, i);

                            HashMap<String, String> mapData = new HashMap<>();
                            mapData.put("vCity", generalFunc.getJsonValueStr("vCity", jobj));
                            mapData.put("eStatus", generalFunc.getJsonValueStr("eStatus", jobj));
                            mapData.put("iCityId", generalFunc.getJsonValueStr("iCityId", jobj));

                            cityDataList.add(mapData);

                        }

                        GenerateAlertBox generateAlertBox = new GenerateAlertBox(getActContext());

                        generateAlertBox.setContentMessage(getSelectStateText(), null);
                        generateAlertBox.setCancelable(true);
                        generateAlertBox.createList(cityDataList, "vCity", (position) -> {

                            if (list_city != null) {
                                list_city.closeAlertBox();
                            }

                            HashMap<String, String> mapData = cityDataList.get(position);

                            selected_iCityId = mapData.get("iCityId");
                            selected_vCity = mapData.get("vCity");
                            generalFunc.storeData(Utils.DEFAULT_CITY_VALUE, mapData.get("vCity"));
                            cityBox.setText(mapData.get("vCity"));

                        });

                        list_city = generateAlertBox;

                        showCityList();

                    } else {

                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                    }
                } else {
                    generalFunc.showError();
                }
            }
        });
        exeWebServer.execute();
    }

    public void showStateList() {
        //list_state.showAlertBox();
        OpenListView.getInstance(getActContext(),generalFunc.retrieveLangLBl("Select state", "LBL_SELECT_STATE"), stateDataList, OpenListView.OpenDirection.CENTER, true, position -> {

            statePosition = position;

            HashMap<String, String> mapData = stateDataList.get(position);

            selected_iStateId = mapData.get("iStateId");
            selected_vState = mapData.get("vState");
            generalFunc.storeData(Utils.DEFAULT_STATE_VALUE, mapData.get("vState"));
            stateBox.setText(mapData.get("vState"));

            selected_vCity = "";
            selected_iCityId = "";
            cityBox.setText("");

        }).show(statePosition, "vState");
    }

    public void showCityList() {
        //list_city.showAlertBox();
        OpenListView.getInstance(getActContext(),generalFunc.retrieveLangLBl("Select city", "LBL_SELECT_CITY"), cityDataList, OpenListView.OpenDirection.CENTER, true, position -> {

            cityPosition = position;
            HashMap<String, String> mapData = cityDataList.get(position);

            selected_iCityId = mapData.get("iCityId");
            selected_vCity = mapData.get("vCity");
            generalFunc.storeData(Utils.DEFAULT_CITY_VALUE, mapData.get("vCity"));
            cityBox.setText(mapData.get("vCity"));


        }).show(cityPosition, "vCity");
    }

    public String getSelectStateText() {
        return ("" + generalFunc.retrieveLangLBl("Select State", "LBL_SELECT_STATE"));
    }

    public String getSelectCityText() {
        return ("" + generalFunc.retrieveLangLBl("Select City", "LBL_SELECT_CITY"));
    }

    public Context getActContext() {
        return RestaurantDetailActivity.this;
    }

    public void removeInput() {
        Utils.removeInput(restaurantLocationOnMapBox);
        Utils.removeInput(stateBox);
        Utils.removeInput(cityBox);

        restaurantLocationOnMapBox.setOnTouchListener(new setOnTouchList());
        stateBox.setOnTouchListener(new setOnTouchList());
        cityBox.setOnTouchListener(new setOnTouchList());

        restaurantLocationOnMapBox.setOnClickListener(new setOnClickList());
        stateBox.setOnClickListener(new setOnClickList());
        cityBox.setOnClickListener(new setOnClickList());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.SEARCH_PICKUP_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {
            restaurantLocationOnMapBox.setText(data.getStringExtra("Address"));

            restaurantLocationAddress = data.getStringExtra("Address");
            latitude = data.getStringExtra("Latitude");
            longitude = data.getStringExtra("Longitude");
        }
    }

    private void checkData() {

        boolean iscontactPersonNameEntered = Utils.checkText(contactPersonNameBox) ? true : Utils.setErrorFields(contactPersonNameBox, required_str);
        boolean isrestaurantLocationOnMapBoxEntered = Utils.checkText(restaurantLocationOnMapBox) ? true : Utils.setErrorFields(restaurantLocationOnMapBox, required_str);
        boolean isrestaurantAddressEntered = Utils.checkText(restaurantAddressBox) ? true : Utils.setErrorFields(restaurantAddressBox, required_str);
        boolean isstateEntered = Utils.checkText(stateBox) ? true : Utils.setErrorFields(stateBox, required_str);
        //boolean iscityEntered = Utils.checkText(cityBox) ? true : Utils.setErrorFields(cityBox, required_str);
        boolean iszipEntered = Utils.checkText(zipBox) ? true : Utils.setErrorFields(zipBox, required_str);

        int estimatedTIme = GeneralFunctions.parseIntegerValue(0, Utils.getText(estTimeEditBox));
        int maxQTY = GeneralFunctions.parseIntegerValue(0, Utils.getText(maxTotalQTYEditBox));

        boolean isESTProper_main = true;
        boolean isMaxQTYProper_main = true;

        if (isResSettingsVisible()) {
            boolean isESTProper = Utils.checkText(estTimeEditBox) ? (estimatedTIme > 0 ? true : Utils.setErrorFields(estTimeEditBox, generalFunc.retrieveLangLBl("", "LBL_ZERO_NOT_ALLOW"))) : Utils.setErrorFields(estTimeEditBox, generalFunc.retrieveLangLBl("Required", "LBL_FEILD_REQUIRD"));

            isESTProper_main = isESTProper;

            boolean isMaxQTYProper = Utils.checkText(maxTotalQTYEditBox) ? (maxQTY > 0 ? true : Utils.setErrorFields(maxTotalQTYEditBox, generalFunc.retrieveLangLBl("", "LBL_ZERO_NOT_ALLOW"))) : Utils.setErrorFields(maxTotalQTYEditBox, generalFunc.retrieveLangLBl("Required", "LBL_FEILD_REQUIRD"));

            isMaxQTYProper_main = isMaxQTYProper;
        }

        if (iscontactPersonNameEntered == false || isrestaurantLocationOnMapBoxEntered == false || isrestaurantAddressEntered == false || isstateEntered == false || iszipEntered == false || isESTProper_main == false || isMaxQTYProper_main == false) {
            return;
        }


        sendRestaurantDetail("Update");

    }

    public void sendRestaurantDetail(String CALL_TYPE) {

        if (CALL_TYPE.equals("Display")) {
            containerView.setVisibility(View.GONE);
            loadingBar.setVisibility(View.VISIBLE);
        }


        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateRestaurantDetails");
        parameters.put("iCompanyId", iCompanyId);
        parameters.put("vContactName", Utils.getText(contactPersonNameBox));
        parameters.put("vRestuarantLocation", restaurantLocationAddress);
        parameters.put("vRestuarantLocationLat", latitude);
        parameters.put("vRestuarantLocationLong", longitude);
        parameters.put("vCaddress", Utils.getText(restaurantAddressBox));
        parameters.put("vState", selected_iStateId);
        parameters.put("vCity", selected_iCityId);
        parameters.put("vZip", Utils.getText(zipBox));
        parameters.put("CALL_TYPE", CALL_TYPE);

        if (isResSettingsVisible() && !CALL_TYPE.equalsIgnoreCase("Display")) {
            parameters.put("iMaxItemQty", Utils.getText(maxTotalQTYEditBox));
            parameters.put("fPrepareTime", Utils.getText(estTimeEditBox));
            parameters.put("fMinOrderValue", Utils.getText(minOrderPriceEditBox));
        }

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        if (!CALL_TYPE.equals("Display")) {
            exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        }
        exeWebServer.setDataResponseListener(responseString -> {
            loadingBar.setVisibility(View.GONE);
            JSONObject responseObj = generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {


                if (generalFunc.checkDataAvail(Utils.action_str, responseObj) == true) {

                    String vCurrency = generalFunc.getJsonValueStr("vCurrency", responseObj);
                    minOrderPriceEditBox.setHelperText(generalFunc.retrieveLangLBl("", "LBL_VALUE_NOTE_PREFIX") + " '" + vCurrency + "'");
                    minOrderPriceEditBox.setHelperTextAlwaysShown(true);

                    estTimeEditBox.setHelperText(generalFunc.retrieveLangLBl("", "LBL_VALUE_NOTE_PREFIX") + " '" + generalFunc.retrieveLangLBl("", "LBL_MINUTES_TXT") + "'");
                    estTimeEditBox.setHelperTextAlwaysShown(true);

                    if (CALL_TYPE.equals("Display")) {

                        JSONObject msg_obj = generalFunc.getJsonObject(Utils.message_str, responseObj);

                        if (msg_obj != null) {

                            String iMaxItemQty = generalFunc.getJsonValueStr("iMaxItemQty", msg_obj);
                            String fPrepareTime = generalFunc.getJsonValueStr("fPrepareTime", msg_obj);
                            String fMinOrderValue = generalFunc.getJsonValueStr("fMinOrderValue", msg_obj);

                            String vContactName = generalFunc.getJsonValueStr("vContactName", msg_obj);
                            String vRestuarantLocation = generalFunc.getJsonValueStr("vRestuarantLocation", msg_obj);
                            String vRestuarantLocationLat = generalFunc.getJsonValueStr("vRestuarantLocationLat", msg_obj);
                            String vRestuarantLocationLong = generalFunc.getJsonValueStr("vRestuarantLocationLong", msg_obj);
                            String vCaddress = generalFunc.getJsonValueStr("vCaddress", msg_obj);
                            String iStateId = generalFunc.getJsonValueStr("iStateId", msg_obj);
                            String iCityId = generalFunc.getJsonValueStr("iCityId", msg_obj);
                            String vZip = generalFunc.getJsonValueStr("vZip", msg_obj);
                            String vState = generalFunc.getJsonValueStr("vState", msg_obj);
                            String vCity = generalFunc.getJsonValueStr("vCity", msg_obj);

                            contactPersonNameBox.setText(vContactName);
                            restaurantLocationOnMapBox.setText(vRestuarantLocation);
                            latitude = vRestuarantLocationLat;
                            longitude = vRestuarantLocationLong;
                            restaurantLocationAddress = vRestuarantLocation;
                            restaurantAddressBox.setText(vCaddress);
                            selected_iStateId = iStateId;
                            selected_iCityId = iCityId;
                            zipBox.setText(vZip);
                            stateBox.setText(vState);
                            cityBox.setText(vCity);

                            if (isResSettingsVisible()) {
                                maxTotalQTYEditBox.setText(iMaxItemQty);
                                estTimeEditBox.setText(fPrepareTime);
                                minOrderPriceEditBox.setText(fMinOrderValue);
                            }
                        }

                    } else {

                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)), true);
                    }

                    containerView.setVisibility(View.VISIBLE);

                } else {
                    if (CALL_TYPE.equals("Display")) {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)), true);
                    } else {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)), false);
                    }
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void showSoftKeyboard(EditText view) {
        if (view.requestFocus()) {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            InputMethodManager imm = (InputMethodManager)
                    this.getSystemService(getActContext().INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hideSoftKeyboard(View view) {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        InputMethodManager imm = (InputMethodManager) this.getSystemService(getActContext().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();

            if (i == submitBtn.getId()) {
                checkData();
            } else if (i == R.id.backImgView) {
                RestaurantDetailActivity.this.onBackPressed();
            } else if (i == R.id.restaurantLocationOnMapBox) {
                Bundle bn = new Bundle();
                bn.putString("isPickUpLoc", "true");
                double lattitude_ = generalFunc.parseDoubleValue(0, latitude);
                double longitude_ = generalFunc.parseDoubleValue(0, longitude);
                if (lattitude_ != 0.0 && longitude_ != 0.0) {
                    bn.putString("PickUpLatitude", "" + latitude);
                    bn.putString("PickUpLongitude", "" + longitude);
                    bn.putString("PickUpAddress", "" + restaurantLocationAddress);
                }
                new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                        bn, Utils.SEARCH_PICKUP_LOC_REQ_CODE);
            } else if (i == R.id.stateBox) {
                buildStateList();
            } else if (i == R.id.cityBox) {
                buildCityList();
            }
        }
    }

}
