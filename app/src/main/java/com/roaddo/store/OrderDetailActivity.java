package com.roaddo.store;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.adapter.files.ManageVehicleListAdapter;
import com.adapter.files.OrderItemsRecyclerAdapter;

import com.dialogs.OpenListView;
import com.fragments.OrderFragment;
import com.general.files.AppFunctions;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.SinchService;
import com.general.files.StartActProcess;
import com.general.files.Closure;
import com.general.files.CustomDialog;
import com.general.files.thermalPrint.GenerateOrderBill;
import com.kyleduo.switchbutton.SwitchButton;
import com.sinch.android.rtc.calling.Call;
import com.twitter.sdk.android.core.internal.CommonUtils;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.FloatingActionButton.FloatingActionButton;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.SelectableRoundedImageView;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Esite on 30-03-2018.
 */

public class OrderDetailActivity extends BaseActivity implements OrderItemsRecyclerAdapter.OnItemClickList, ManageVehicleListAdapter.OnItemClickList {

    SwitchButton availItemSwitch;
    MTextView titleTxt;
    MTextView subTitleTxt;
    MTextView totalItemsTxt;
    MTextView deliveryStatusTxtView;
    View containerView;
    View bgView;
    String userPhoneNumber = "";
    String userName = "";
    String vUserImage = "";
    String vCompany = "";
    String vRestuarantImage = "";
    String iUserid = "";
    String vInstruction = "";
    View iconRefreshImgView;
    View iconInstructionView;
    View reAssignArea;
    ProgressBar mAssignDriverProgressBar;
    View confirmDeclineAreaView;
    View assignDriverBtnAra;
    RecyclerView itemsRecyclerView;
    ImageView iconImgView;
    ProgressBar loadingBar;
    HashMap<String, String> orderData;
    OrderItemsRecyclerAdapter adapter;
    AlertDialog dialog_declineOrder;
    ExecuteWebServerUrl currExeTask;
    private LinearLayout chargeDetailArea;
    private LinearLayout chargeDetailTitleArea;
    private ArrayList<HashMap<String, String>> dataList = new ArrayList<>();
    private ArrayList<HashMap<String, String>> fareList = new ArrayList<>();
    private ImageView backImgView;
    private GeneralFunctions generalFunc;
    private MButton confirmBtn;
    private MButton declineBtn;
    private MButton assignDriverBtn;
    private MButton reAssignBtn;
    private MButton declineAssignBtn;
    private MButton trackOrderBtn;
    private boolean isExpanded = false;
    GenerateAlertBox reqInProgressAlertBox;
    String userprofileJson = "";
    FloatingActionButton viewPrescTxtView;

    ArrayList<String> imageList = new ArrayList<>();
    Menu menu;

    /*Thermal*/
    HashMap<String, String> reqiuredDetails = new HashMap<>();
    CustomDialog customDialog;
    public static final int REQUEST_COARSE_LOCATION = 200;
    JSONArray PrescriptionImages;
    FloatingActionButton connectPrinterArea;
    FrameLayout connectPrinterlayout;
    HashMap<String, String> data = new HashMap<>();
    boolean isOpenDriverSelection = false;
    String eDriverType = "";
    private boolean iseTakeAway;
    boolean isPrefrence;
    ArrayList<HashMap<String, String>> instructionslit;
    RelativeLayout moreinstructionLyout;
    MTextView textmoreinsview;
    MTextView textuserprefrence;

    JSONObject DeliveryPreferences;

    View orderPickedUpBtnArea;
    private MButton orderPickedUpBtn;
    private MTextView takeAwayOrderTitleTxt;
    ArrayList<String> items_txt_car = new ArrayList<String>();
    ArrayList<String> items_txt_car_json = new ArrayList<String>();
    ArrayList<String> items_isHail_json = new ArrayList<String>();
    ArrayList<String> items_car_id = new ArrayList<String>();

    androidx.appcompat.app.AlertDialog list_car;
    private String selected_carId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        userprofileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        orderData = (HashMap<String, String>) getIntent().getSerializableExtra("OrderData");

       /* Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);*/

        initView();
//        setChargeDetails();
        setLabels();
    }

    private void initView() {

        instructionslit = new ArrayList<>();
        textuserprefrence = findViewById(R.id.textuserprefrence);
        textmoreinsview = findViewById(R.id.textmoreinsview);
        moreinstructionLyout = findViewById(R.id.moreinstructionLyout);
        chargeDetailArea = findViewById(R.id.chargeDetailArea);
        chargeDetailTitleArea = findViewById(R.id.chargeDetailTitleArea);
        availItemSwitch = (SwitchButton) findViewById(R.id.availItemSwitch);

        textmoreinsview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putString("DeliveryPreferences", DeliveryPreferences.toString());
                new StartActProcess(getActContext()).startActWithData(UserPrefrenceActivity.class, bundle);
            }
        });

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        viewPrescTxtView = (FloatingActionButton) findViewById(R.id.viewPrescTxtView);
        viewPrescTxtView.setOnClickListener(new setOnClickList());
        containerView = findViewById(R.id.containerView);
        subTitleTxt = (MTextView) findViewById(R.id.subTitleTxt);
        subTitleTxt.setSelected(true);
        totalItemsTxt = (MTextView) findViewById(R.id.totalItemsTxt);
        deliveryStatusTxtView = (MTextView) findViewById(R.id.deliveryStatusTxtView);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        iconImgView = (ImageView) findViewById(R.id.iconImgView);
        loadingBar = (ProgressBar) findViewById(R.id.loadingBar);
        mAssignDriverProgressBar = (ProgressBar) findViewById(R.id.mAssignDriverProgressBar);
        iconRefreshImgView = findViewById(R.id.iconRefreshImgView);
        iconInstructionView = findViewById(R.id.iconInstructionView);
        assignDriverBtnAra = findViewById(R.id.assignDriverBtnAra);
        orderPickedUpBtnArea = findViewById(R.id.orderPickedUpBtnArea);
        confirmDeclineAreaView = findViewById(R.id.confirmDeclineAreaView);
        reAssignArea = findViewById(R.id.reAssignArea);
        bgView = findViewById(R.id.bgView);

        itemsRecyclerView = (RecyclerView) findViewById(R.id.itemsRecyclerView);

        adapter = new OrderItemsRecyclerAdapter(getActContext(), dataList, generalFunc);
        itemsRecyclerView.setAdapter(adapter);
        itemsRecyclerView.setClipToPadding(false);


        declineBtn = ((MaterialRippleLayout) findViewById(R.id.declineBtn)).getChildView();
        declineBtn.setBackgroundColor(getResources().getColor(R.color.red));
        confirmBtn = ((MaterialRippleLayout) findViewById(R.id.confirmBtn)).getChildView();
        confirmBtn.setBackgroundColor(getResources().getColor(R.color.green));
        trackOrderBtn = ((MaterialRippleLayout) findViewById(R.id.trackOrderBtn)).getChildView();

        assignDriverBtn = ((MaterialRippleLayout) findViewById(R.id.assignDriverBtn)).getChildView();
        takeAwayOrderTitleTxt = findViewById(R.id.takeAwayOrderTitleTxt);
        orderPickedUpBtn = ((MaterialRippleLayout) findViewById(R.id.orderPickedUpBtn)).getChildView();
        orderPickedUpBtn.setId(Utils.generateViewId());

        reAssignBtn = ((MaterialRippleLayout) findViewById(R.id.reAssignBtn)).getChildView();
        declineAssignBtn = ((MaterialRippleLayout) findViewById(R.id.declineAssignBtn)).getChildView();

        declineBtn.setId(Utils.generateViewId());
        confirmBtn.setId(Utils.generateViewId());
        trackOrderBtn.setId(Utils.generateViewId());
        assignDriverBtn.setId(Utils.generateViewId());
        reAssignBtn.setId(Utils.generateViewId());
        declineAssignBtn.setId(Utils.generateViewId());

        declineBtn.setOnClickListener(new setOnClickList());
        confirmBtn.setOnClickListener(new setOnClickList());
        trackOrderBtn.setOnClickListener(new setOnClickList());
        assignDriverBtn.setOnClickListener(new setOnClickList());
        orderPickedUpBtn.setOnClickListener(new setOnClickList());
        reAssignBtn.setOnClickListener(new setOnClickList());
        declineAssignBtn.setOnClickListener(new setOnClickList());
        iconImgView.setOnClickListener(new setOnClickList());
        iconRefreshImgView.setOnClickListener(new setOnClickList());

        data.put(Utils.THERMAL_PRINT_ALLOWED_KEY, "");
        data = new GeneralFunctions(getActContext()).retrieveValue(data);

        connectPrinterlayout = (FrameLayout) findViewById(R.id.connectPrinterlayout);
        connectPrinterArea = (FloatingActionButton) findViewById(R.id.connectPrinterArea);
        connectPrinterlayout.setVisibility(MyApp.getInstance().isThermalPrintAllowed(false) && data.get(Utils.THERMAL_PRINT_ALLOWED_KEY).equalsIgnoreCase("Yes") ? View.VISIBLE : View.GONE);
        connectPrinterArea.setOnClickListener(new setOnClickList());


        backImgView.setOnClickListener(new setOnClickList());
        bgView.setOnClickListener(new setOnClickList());

        iconImgView.setVisibility(View.GONE);
        iconRefreshImgView.setVisibility(View.GONE);
        iconInstructionView.setVisibility(View.GONE);


        int dp4 = Utils.dipToPixels(getActContext(), 4);
        iconImgView.setPadding(dp4, dp4, dp4, dp4);
        iconRefreshImgView.setPadding(dp4, dp4, dp4, dp4);

        //   iconInstructionView.setPadding(Utils.dipToPixels(getActContext(), 4), Utils.dipToPixels(getActContext(), 4), Utils.dipToPixels(getActContext(), 4), Utils.dipToPixels(getActContext(), 4));

        containerView.setVisibility(View.GONE);


        adapter.setOnItemClickList(this);
        getOrderDetails();
    }

    private void setLabels() {
        trackOrderBtn.setText(generalFunc.retrieveLangLBl("Track Order", "LBL_TRACK_ORDER"));
        takeAwayOrderTitleTxt.setText(generalFunc.retrieveLangLBl("Take Away Order", "LBL_TAKE_WAY_ORDER"));

        deliveryStatusTxtView.setText(generalFunc.retrieveLangLBl("Delivery Executive not found", "LBL_NO_PROVER_FOUND"));


        confirmBtn.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_CONFIRM_TXT"));
        declineBtn.setText(generalFunc.retrieveLangLBl("", "LBL_DECLINE_TXT"));

        assignDriverBtn.setText(generalFunc.retrieveLangLBl("Assign Driver", "LBL_ASSIGN_DRIVER"));
        orderPickedUpBtn.setText(generalFunc.retrieveLangLBl("Mark As Picked-up", "LBL_PICKEDUP_ORDER"));
        reAssignBtn.setText(generalFunc.retrieveLangLBl("Assign Driver", "LBL_ASSIGN_DRIVER"));
        declineAssignBtn.setText(generalFunc.retrieveLangLBl("", "LBL_DECLINE_TXT"));
        // viewPrescTxtView.setText(generalFunc.retrieveLangLBl("View Prescription", "LBL_VIEW_PRESCRIPTION"));

        assignDriverBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        assignDriverBtn.setTextColor(getResources().getColor(android.R.color.white));

        reAssignBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        reAssignBtn.setTextColor(getResources().getColor(android.R.color.white));

        declineAssignBtn.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        declineAssignBtn.setTextColor(getResources().getColor(android.R.color.white));

        titleTxt.setText("#" + generalFunc.convertNumberWithRTL(orderData.get("vOrderNo")));
        subTitleTxt.setText(generalFunc.convertNumberWithRTL(orderData.get("tOrderRequestDateFormatted")));
    }

    private void setChargeDetails(JSONArray fareArr) {

        if (chargeDetailArea.getChildCount() > 0) {
            chargeDetailArea.removeAllViewsInLayout();
        }


        if (chargeDetailTitleArea.getChildCount() > 0) {
            chargeDetailTitleArea.removeAllViewsInLayout();
            isExpanded = false;
        }

        fareList.clear();
        chargeDetailArea.removeAllViews();
        chargeDetailTitleArea.removeAllViews();

        for (int i = 0; i < fareArr.length(); i++) {
            HashMap<String, String> mapData = new HashMap<>();
            JSONObject obj_tmp = generalFunc.getJsonObject(fareArr, i);

            try {
                mapData.put("Name", obj_tmp.names().getString(0));
                mapData.put("Amount", obj_tmp.get(obj_tmp.names().getString(0)).toString());
            } catch (JSONException e) {
                e.printStackTrace();
                mapData.put("Name", "");
                mapData.put("Amount", "");
            }

            fareList.add(mapData);
        }


        List<HashMap<String, String>> answer = new ArrayList<HashMap<String, String>>();

        for (int i = 0; i < fareList.size(); i++) {
            if (i == fareList.size() - 1) {
                setChildData(i, true);
            } else {
                answer.add(fareList.get(i));
                setChildData(i, false);
            }
        }

//        btn_type1.setTextColor(this.getResources().getColor(R.color.appThemeColor_TXT_1));
//        btn_type2.setTextColor(this.getResources().getColor(R.color.appThemeColor_TXT_1));

//        chargeDetailArea.setVisibility(View.VISIBLE);
    }

    private void setChildData(int i, boolean isLast) {
        Log.d("setChildData", "setChildData: " + i);
        LayoutInflater infalInflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View convertView = infalInflater.inflate(R.layout.charge_header_cell, null);

        MTextView itemNameTxt = (MTextView) convertView.findViewById(R.id.itemNameTxt);
        RelativeLayout orderDetailArea = (RelativeLayout) convertView.findViewById(R.id.orderDetailArea);
        MTextView fareTxt = (MTextView) convertView.findViewById(R.id.fareTxt);
        View shadowView = (View) convertView.findViewById(R.id.shadowView);
        itemNameTxt.setText(fareList.get(i).get("Name"));
        fareTxt.setText(generalFunc.convertNumberWithRTL(fareList.get(i).get("Amount")));

        final ImageView indicatorImg = (ImageView) convertView.findViewById(R.id.indicatorImg);

        if (i == 0) {
            shadowView.setVisibility(View.GONE);
            indicatorImg.setVisibility(View.GONE);

         /*   convertView.setOnClickListener(view -> {
                if (chargeDetailArea.getVisibility() == View.VISIBLE) {
                    chargeDetailArea.setVisibility(View.GONE);
                    bgView.setVisibility(View.GONE);

                    View totalChargeView = chargeDetailTitleArea.getChildAt(0);
                    ((ImageView) totalChargeView.findViewById(R.id.indicatorImg)).setVisibility(View.VISIBLE);
                    (totalChargeView.findViewById(R.id.shadowView)).setVisibility(View.VISIBLE);
                }
            });*/
            chargeDetailArea.addView(convertView);

            Typeface fontLight = Typeface.createFromAsset(getAssets(), getActContext().getResources().getString(R.string.systemLightFont));
            itemNameTxt.setTypeface(fontLight);
            itemNameTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            itemNameTxt.setTextColor(Color.parseColor("#272727"));

            Typeface fontMedium = Typeface.createFromAsset(getAssets(), getActContext().getResources().getString(R.string.systemMediumFont));
            fareTxt.setTypeface(fontMedium);

            fareTxt.setTextColor(Color.parseColor("#272727"));
            fareTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

        } else if (i == (fareList.size() - 1)) {
            shadowView.setVisibility(View.VISIBLE);
            Typeface fontMedium = Typeface.createFromAsset(getAssets(), getActContext().getResources().getString(R.string.systemSemiBold));
            itemNameTxt.setTypeface(fontMedium);
            Typeface fontBold = Typeface.createFromAsset(getAssets(), getActContext().getResources().getString(R.string.systemBold));
            fareTxt.setTypeface(fontBold);

            itemNameTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);

            itemNameTxt.setTextColor(Color.parseColor("#4a4a4a"));

            fareTxt.setTextColor(Color.parseColor("#000000"));
            fareTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);


            if (isExpanded) {
                indicatorImg.setVisibility(View.INVISIBLE);
            } else {
                indicatorImg.setVisibility(View.VISIBLE);
            }
            indicatorImg.setImageResource(R.mipmap.ic_arrow_up);

            orderDetailArea.getLayoutParams().height = Utils.dpToPx(40, getActContext());

            chargeDetailTitleArea.addView(convertView);

            chargeDetailTitleArea.setOnClickListener(new setOnClickList());
        } else {
            Typeface fontLight = Typeface.createFromAsset(getAssets(), getActContext().getResources().getString(R.string.systemLightFont));
            itemNameTxt.setTypeface(fontLight);
            itemNameTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            itemNameTxt.setTextColor(Color.parseColor("#272727"));

            Typeface fontMedium = Typeface.createFromAsset(getAssets(), getActContext().getResources().getString(R.string.systemMediumFont));
            fareTxt.setTypeface(fontMedium);

            fareTxt.setTextColor(Color.parseColor("#272727"));
            fareTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

            shadowView.setVisibility(View.GONE);
            indicatorImg.setVisibility(View.INVISIBLE);

            chargeDetailArea.addView(convertView);
        }

    }

    // slide the view from below itself to the current position
    public void slideUp(View view, Animation.AnimationListener listener) {
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setAnimationListener(listener);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public void slideDown(View view) {
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    private Context getActContext() {
        return OrderDetailActivity.this;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onItemAvailabilityChanged(int position, boolean isAvailable) {
        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("type", "UpdateOrderDetailsRestaurant");
        parameters.put("iOrderId", orderData.get("iOrderId"));
        parameters.put("iOrderDetailId", dataList.get(position).get("iOrderDetailId"));
        parameters.put("eAvailable", isAvailable == true ? "Yes" : "No");

        ExecuteWebServerUrl exeServerTask = new ExecuteWebServerUrl(getActContext(), parameters);
        exeServerTask.setLoaderConfig(getActContext(), true, generalFunc);
        exeServerTask.setDataResponseListener(responseString -> {
            JSONObject responseObj = generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                boolean isDataAvail = generalFunc.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {
                    getOrderDetails();
                }

                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));

            } else {
                generalFunc.showError();
            }

        });
        exeServerTask.execute();
    }

    public void getDeclineReasonsList() {
        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("type", "GetCancelReasons");
        parameters.put("iOrderId", orderData.get("iOrderId"));
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("eUserType", Utils.app_type);

        ExecuteWebServerUrl exeServerTask = new ExecuteWebServerUrl(getActContext(), parameters);
        exeServerTask.setLoaderConfig(getActContext(), true, generalFunc);
        exeServerTask.setDataResponseListener(responseString -> {
            JSONObject responseObj = generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                boolean isDataAvail = generalFunc.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {
                    showDeclineReasonsAlert(responseString);
                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }

            } else {
                generalFunc.showError();
            }

        });
        exeServerTask.execute();
    }


    int selCurrentPosition = -1;

    public void showDeclineReasonsAlert(String responseString) {
        selCurrentPosition = -1;
        String titleDailog = generalFunc.retrieveLangLBl("Decline Order", "LBL_DECLINE_ORDER");

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());
        // builder.setTitle(generalFunc.retrieveLangLBl("Decline Order", "LBL_DECLINE_ORDER"));

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.decline_order_dialog_design, null);
        builder.setView(dialogView);

        MaterialEditText reasonBox = (MaterialEditText) dialogView.findViewById(R.id.inputBox);
        RelativeLayout commentArea = (RelativeLayout) dialogView.findViewById(R.id.commentArea);
        reasonBox.setHideUnderline(true);
        if (generalFunc.isRTLmode()) {
            reasonBox.setPaddings(0, 0, (int) getResources().getDimension(R.dimen._10sdp), 0);
        } else {
            reasonBox.setPaddings((int) getResources().getDimension(R.dimen._10sdp), 0, 0, 0);
        }

        reasonBox.setSingleLine(false);
        reasonBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        reasonBox.setGravity(Gravity.TOP);
        if (generalFunc.isRTLmode()) {
            reasonBox.setPaddings(0, 0, (int) getResources().getDimension(R.dimen._10sdp), 0);
        } else {
            reasonBox.setPaddings((int) getResources().getDimension(R.dimen._10sdp), 0, 0, 0);
        }

        reasonBox.setVisibility(View.GONE);
        commentArea.setVisibility(View.GONE);
        new CreateRoundedView(Color.parseColor("#ffffff"), 5, 1, Color.parseColor("#C5C3C3"), commentArea);
        reasonBox.setBothText("", generalFunc.retrieveLangLBl("", "LBL_ENTER_REASON"));


        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        // HashMap<String, String> map = new HashMap<>();
        // map.put("title","-- " + generalFunc.retrieveLangLBl("Select Reason", "LBL_SELECT_CANCEL_REASON") + " --");
        //  map.put("id", "");
        // list.add(map);
        JSONArray arr_msg = generalFunc.getJsonArray(Utils.message_str, responseString);
        if (arr_msg != null) {

            for (int i = 0; i < arr_msg.length(); i++) {

                JSONObject obj_tmp = generalFunc.getJsonObject(arr_msg, i);

                //  arrListTitle.add(generalFunc.getJsonValueStr("vTitle", obj_tmp));
                //   arrListIDs.add(generalFunc.getJsonValueStr("iCancelReasonId", obj_tmp));

                HashMap<String, String> datamap = new HashMap<>();
                datamap.put("title", generalFunc.getJsonValueStr("vTitle", obj_tmp));
                datamap.put("id", generalFunc.getJsonValueStr("iCancelReasonId", obj_tmp));
                list.add(datamap);


            }

            // arrListTitle.add(generalFunc.retrieveLangLBl("", "LBL_OTHER_DL"));
            // arrListIDs.add("");


            HashMap<String, String> othermap = new HashMap<>();
            othermap.put("title", generalFunc.retrieveLangLBl("", "LBL_OTHER_TXT"));
            othermap.put("id", "");
            list.add(othermap);


            //   AppCompatSpinner spinner = (AppCompatSpinner) dialogView.findViewById(R.id.declineReasonsSpinner);

            MTextView cancelTxt = (MTextView) dialogView.findViewById(R.id.cancelTxt);
            MTextView submitTxt = (MTextView) dialogView.findViewById(R.id.submitTxt);
            MTextView subTitleTxt = (MTextView) dialogView.findViewById(R.id.subTitleTxt);
            ImageView cancelImg = (ImageView) dialogView.findViewById(R.id.cancelImg);
            subTitleTxt.setText(titleDailog);

            MTextView declinereasonBox = (MTextView) dialogView.findViewById(R.id.declinereasonBox);
            declinereasonBox.setText(generalFunc.retrieveLangLBl("Select Reason", "LBL_SELECT_CANCEL_REASON"));
            submitTxt.setClickable(false);
            submitTxt.setTextColor(getResources().getColor(R.color.gray_holo_light));

            // style="@style/decline_order_spinner_style"


            submitTxt.setText(generalFunc.retrieveLangLBl("", "LBL_YES"));
            cancelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_NO"));
            submitTxt.setOnClickListener(v -> {
                // String selectedItemId = arrListIDs.get(spinner.getSelectedItemPosition());

                if (selCurrentPosition == -1) {
                    return;
                }

                if (Utils.checkText(reasonBox) == false && selCurrentPosition == (list.size() - 1)) {
                    reasonBox.setError(generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD"));
                    return;
                }

                declineOrder(list.get(selCurrentPosition).get("id"), Utils.getText(reasonBox));

                dialog_declineOrder.dismiss();

            });
            cancelTxt.setOnClickListener(v -> {
                Utils.hideKeyboard(getActContext());
                dialog_declineOrder.dismiss();
            });

            cancelImg.setOnClickListener(v -> {
                Utils.hideKeyboard(getActContext());
                dialog_declineOrder.dismiss();
            });


           /* ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arrListTitle);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerAdapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if (spinner.getSelectedItemPosition() == (arrListIDs.size() - 1)) {
                        reasonBox.setVisibility(View.VISIBLE);
                        commentArea.setVisibility(View.VISIBLE);
                    } else if (spinner.getSelectedItemPosition() == 0) {
                        commentArea.setVisibility(View.GONE);
                        reasonBox.setVisibility(View.GONE);
                    } else {
                        commentArea.setVisibility(View.GONE);
                        reasonBox.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });*/

            declinereasonBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OpenListView.getInstance(getActContext(), generalFunc.retrieveLangLBl("", "LBL_SELECT_REASON"), list, OpenListView.OpenDirection.CENTER, true, position -> {


                        selCurrentPosition = position;
                        HashMap<String, String> mapData = list.get(position);
                        declinereasonBox.setText(mapData.get("title"));
                        if (selCurrentPosition == (list.size() - 1)) {
                            reasonBox.setVisibility(View.VISIBLE);
                            commentArea.setVisibility(View.VISIBLE);
                        } else {
                            reasonBox.setVisibility(View.GONE);
                            commentArea.setVisibility(View.GONE);
                        }
                        submitTxt.setClickable(true);
                        submitTxt.setTextColor(getResources().getColor(R.color.white));


                    }).show(selCurrentPosition, "title");
                }
            });


            dialog_declineOrder = builder.create();
            dialog_declineOrder.setCancelable(false);
            dialog_declineOrder.getWindow().setBackgroundDrawable(getActContext().getResources().getDrawable(R.drawable.all_roundcurve_card));
            dialog_declineOrder.show();

            //  dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            /*dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String selectedItemId = arrListIDs.get(spinner.getSelectedItemPosition());

                    if (spinner.getSelectedItemPosition() == 0) {
                        return;
                    }

                    if (Utils.checkText(reasonBox) == false && spinner.getSelectedItemPosition() == (arrListIDs.size() - 1)) {
                        reasonBox.setError(generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD"));
                        return;
                    }

                    declineOrder(arrListIDs.get(spinner.getSelectedItemPosition()), Utils.getText(reasonBox));

                    dialog_declineOrder.dismiss();
                }
            });*/
/*
            dialog_declineOrder.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog_declineOrder.dismiss();
                }
            });*/
        } else {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_NO_DATA_AVAIL"));
        }
    }

    public void declineOrder(String iCancelReasonId, String reason) {

        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("type", "DeclineOrder");
        parameters.put("iCompanyId", generalFunc.getMemberId());
        parameters.put("iOrderId", orderData.get("iOrderId"));
        parameters.put("eUserType", Utils.app_type);
        parameters.put("eCancelledBy", Utils.app_type);
        parameters.put("iCancelReasonId", iCancelReasonId);
        parameters.put("vCancelReason", iCancelReasonId.equals("") ? reason : "");

        ExecuteWebServerUrl exeServerTask = new ExecuteWebServerUrl(getActContext(), parameters);
        exeServerTask.setLoaderConfig(getActContext(), true, generalFunc);
        exeServerTask.setDataResponseListener(responseString -> {
            JSONObject responseObj = generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                boolean isDataAvail = generalFunc.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {
                    (new StartActProcess(getActContext())).setOkResult();
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)), true);
                } else {

                    GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                    generateAlert.setContentMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Allow", "LBL_ALLOW"));
                    generateAlert.showAlertBox();

                    generateAlert.setCancelable(false);
                    generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
                        @Override
                        public void handleBtnClick(int btn_id) {

                            generateAlert.closeAlertBox();
                            if (generalFunc.getJsonValue("DO_RESTART", responseString).equalsIgnoreCase("Yes")) {
                                finish();
                            }


                        }
                    });

                    // generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }

            } else {


                generalFunc.showError();
            }

        });
        exeServerTask.execute();
    }

    public void confirmOrder(String eTakeAwayPickedUp) {

        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("type", "ConfirmOrderByRestaurant");
        parameters.put("iCompanyId", generalFunc.getMemberId());
        parameters.put("iOrderId", orderData.get("iOrderId"));

        if (Utils.checkText(eTakeAwayPickedUp)) {
            parameters.put("ePickedUp", eTakeAwayPickedUp);
        }
        ExecuteWebServerUrl exeServerTask = new ExecuteWebServerUrl(getActContext(), parameters);
        exeServerTask.setLoaderConfig(getActContext(), true, generalFunc);
        exeServerTask.setDataResponseListener(responseString -> {
            JSONObject responseObj = generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                boolean isDataAvail = generalFunc.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {
                    (new StartActProcess(getActContext())).setOkResult();
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)), false);

                    if (!Utils.checkText(eTakeAwayPickedUp)) {
                        if (MyApp.getInstance().isThermalPrintAllowed(true)) {
                            if (MyApp.generateOrderBill != null && MyApp.btsocket != null) {
                                MyApp.generateOrderBill.printBill(getMsgFormate(), dataList, reqiuredDetails);
                            }
                        }

                        if (MyApp.getInstance().isThermalPrintAllowed(false) && data.get(Utils.THERMAL_PRINT_ALLOWED_KEY).equalsIgnoreCase("Yes")) {
                            connectPrinterlayout.setVisibility(View.VISIBLE);

                        } else {
                            connectPrinterlayout.setVisibility(View.GONE);
                        }
                        getOrderDetails();
                    } else {
                        finish();
                    }
                } else {

                    GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                    generateAlert.setContentMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Allow", "LBL_ALLOW"));
                    generateAlert.showAlertBox();

                    generateAlert.setCancelable(false);
                    generateAlert.setBtnClickList(new GenerateAlertBox.HandleAlertBtnClick() {
                        @Override
                        public void handleBtnClick(int btn_id) {

                            generateAlert.closeAlertBox();
                            if (generalFunc.getJsonValue("DO_RESTART", responseString).equalsIgnoreCase("Yes")) {
                                finish();
                            }


                        }
                    });
                    // generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }

            } else {
                generalFunc.showError();
            }

        });
        exeServerTask.execute();
    }


    public void confirmIncomingDriver(String iOrderId) {

        if (orderData.get("iOrderId").equalsIgnoreCase(iOrderId)) {
            if (reqInProgressAlertBox != null) {
                reqInProgressAlertBox.closeAlertBox();
                reqInProgressAlertBox = null;
            }

            getOrderDetails();
        }
    }

    public boolean confirmIncomingDriver(String iOrderId, boolean isForCheck) {

        if (orderData.get("iOrderId").equalsIgnoreCase(iOrderId)) {
            return true;
        }

        return false;
    }


    public void assignDriver(String selected_carId) {

        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("type", "sendRequestToDrivers");
        parameters.put("iOrderId", orderData.get("iOrderId"));
        parameters.put("eDriverType", eDriverType);
        parameters.put("iVehicleTypeId", selected_carId);

        ExecuteWebServerUrl exeServerTask = new ExecuteWebServerUrl(getActContext(), parameters);
        exeServerTask.setLoaderConfig(getActContext(), true, generalFunc);
        exeServerTask.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeServerTask.setDataResponseListener(responseString -> {
            JSONObject responseObj = generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {
                generalFunc.sendHeartBeat();
                boolean isDataAvail = generalFunc.checkDataAvail(Utils.action_str, responseObj);

                String message = generalFunc.getJsonValueStr(Utils.message_str, responseObj);

                if (isDataAvail == false) {
                    if (message.equals("SESSION_OUT")) {
                        MyApp.getInstance().notifySessionTimeOut();
                        Utils.runGC();
                        return;
                    }

                    if (message.equals("NO_CARS")) {


                        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                        generateAlert.setCancelable(false);
                        generateAlert.setBtnClickList(btn_id -> {

                            if (btn_id == 1) {
                                generateAlert.closeAlertBox();
                            } else if (btn_id == 0) {
                                declineBtn.performClick();
                            }

                        });
                        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", "LBL_NO_DRIVER_FOUND"));
                        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_DECLINE_TXT"));

                        generateAlert.showAlertBox();

                        return;
                    }

                    if (message.equals(Utils.GCM_FAILED_KEY) || message.equals(Utils.APNS_FAILED_KEY)) {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_REQUEST_FAILED_TXT"));
                        return;
                    }
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", message));

                } else {
                    reqInProgressAlertBox = generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_REQ_IN_PROCESS"));

                    getOrderDetails();
                }
            } else {
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_REQUEST_FAILED_TXT"));
            }

        });
        exeServerTask.execute();
    }

    public void getOrderDetails() {
        if (loadingBar.getVisibility() != View.VISIBLE) {
            loadingBar.setVisibility(View.VISIBLE);
        }

        bgView.performClick();

        dataList.clear();
        adapter.notifyDataSetChanged();

        containerView.setVisibility(View.GONE);
        chargeDetailArea.setVisibility(View.GONE);

        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("type", "GetOrderDetailsRestaurant");
        parameters.put("iOrderId", orderData.get("iOrderId"));


        if (currExeTask != null) {
            currExeTask.cancel(true);
            currExeTask = null;
        }
        ExecuteWebServerUrl exeServerTask = new ExecuteWebServerUrl(getActContext(), parameters);
        exeServerTask.setLoaderConfig(getActContext(), false, generalFunc);
        currExeTask = exeServerTask;
        exeServerTask.setDataResponseListener(responseString -> {
            iconRefreshImgView.setVisibility(View.VISIBLE);
            JSONObject responseObj = generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                boolean isDataAvail = generalFunc.checkDataAvail(Utils.action_str, responseObj);

                if (isDataAvail) {
                    imageList.clear();
                    JSONObject obj_msg = generalFunc.getJsonObject(Utils.message_str, responseObj);

                    String driverAssign = generalFunc.getJsonValueStr("DriverAssign", obj_msg);
                    String assignStatus = generalFunc.getJsonValueStr("AssignStatus", obj_msg);
                    String eConfirm = generalFunc.getJsonValueStr("eConfirm", obj_msg);
                    String eDecline = generalFunc.getJsonValueStr("eDecline", obj_msg);
                    String eOrderPicked = generalFunc.getJsonValueStr("eOrderPickedByDriver", obj_msg);
                    String eTakeaway = generalFunc.getJsonValueStr("eTakeAway", obj_msg);

                    iseTakeAway = Utils.checkText(eTakeaway) && eTakeaway.equalsIgnoreCase("Yes");
                    takeAwayOrderTitleTxt.setVisibility(iseTakeAway ? View.VISIBLE : View.GONE);

                    String UserPhone = generalFunc.getJsonValueStr("UserPhone", obj_msg);
                    userName = generalFunc.getJsonValueStr("UserName", obj_msg);
                    vUserImage = generalFunc.getJsonValueStr("vUserImage", obj_msg);
                    vCompany = generalFunc.getJsonValueStr("vCompany", obj_msg);
                    vRestuarantImage = generalFunc.getJsonValueStr("vRestuarantImage", obj_msg);
                    iUserid = generalFunc.getJsonValueStr("iUserId", obj_msg);
                    vInstruction = generalFunc.getJsonValueStr("vInstruction", obj_msg);

                    isOpenDriverSelection = generalFunc.getJsonValueStr("isOpenDriverSelection", obj_msg).equals("Yes") ? true : false;

                    reqiuredDetails = new HashMap<>();
                    reqiuredDetails.put("CUSTOMER_NAME", userName);
                    reqiuredDetails.put("COMPANY_NAME", vCompany);
                    reqiuredDetails.put("ORDER_DATETIME", orderData.get("tOrderRequestDateFormatted"));
                    reqiuredDetails.put("ORDER_NO", generalFunc.convertNumberWithRTL(orderData.get("vOrderNo")));
                    reqiuredDetails.put("ORDER_VIA", generalFunc.retrieveValue(Utils.SITE_NAME_KEY));


                    DeliveryPreferences = generalFunc.getJsonObject("DeliveryPreferences", responseObj);

                    isPrefrence = generalFunc.getJsonValueStr("Enable", DeliveryPreferences).equalsIgnoreCase("Yes") ? true : false;
                    if (isPrefrence) {
                        moreinstructionLyout.setVisibility(View.GONE);
                    } else {
                        moreinstructionLyout.setVisibility(View.GONE);
                    }


                    if ((vInstruction != null && !vInstruction.equals("")) || isPrefrence) {
                        iconInstructionView.setVisibility(View.VISIBLE);
                        iconInstructionView.setOnClickListener(new setOnClickList());
                    } else {
                        iconRefreshImgView.setVisibility(View.GONE);
                    }


                    PrescriptionImages = generalFunc.getJsonArray("PrescriptionImages", obj_msg);

                    if (PrescriptionImages != null && !PrescriptionImages.equals("")) {
                        viewPrescTxtView.setVisibility(View.VISIBLE);

                        for (int i = 0; i < PrescriptionImages.length(); i++) {

                            imageList.add(generalFunc.getJsonValue(PrescriptionImages, i).toString());

                        }

                    }

                    int REQUEST_REMAINS_SEC = GeneralFunctions.parseIntegerValue(0, generalFunc.getJsonValueStr("REQUEST_REMAINS_SEC", obj_msg));

                    userPhoneNumber = UserPhone;

                    iconImgView.setVisibility(View.VISIBLE);

                    if (assignStatus.equalsIgnoreCase("REQ_NOT_FOUND")) {
                        deliveryStatusTxtView.setVisibility(View.GONE);

                        reAssignArea.setVisibility(View.GONE);
                        assignDriverBtnAra.setVisibility(View.GONE);

                        if (eConfirm.equalsIgnoreCase("Yes") && !iseTakeAway) {
                            assignDriverBtnAra.setVisibility(View.VISIBLE);
                            mAssignDriverProgressBar.setVisibility(View.GONE);
                            assignDriverBtn.setText(generalFunc.retrieveLangLBl("Assign Driver", "LBL_ASSIGN_DRIVER"));
                        } else if (iseTakeAway & eConfirm.equalsIgnoreCase("Yes")) {
                            orderPickedUpBtnArea.setVisibility(View.VISIBLE);
                            orderPickedUpBtn.setText(generalFunc.retrieveLangLBl("Mark As Picked-up", "LBL_PICKEDUP_ORDER"));
                        }

                        assignDriverBtn.setEnabled(true);

                        ((MaterialRippleLayout) trackOrderBtn.getParent()).setVisibility(View.GONE);
                    } else if (assignStatus.equalsIgnoreCase("DRIVER_ASSIGN")) {
                        deliveryStatusTxtView.setVisibility(View.VISIBLE);
                        deliveryStatusTxtView.setText(generalFunc.retrieveLangLBl("Delivery executive is on way to pick order.", "LBL_DELIVERY_DRIVER_ASSIGNED"));
                        if (eOrderPicked.equalsIgnoreCase("Yes")) {
                            deliveryStatusTxtView.setText(generalFunc.retrieveLangLBl("Delivery executive is on way to deliver the order.", "LBL_DELIVERY_DRIVER_IN_ROUTE_DELIVER"));
                        }
                        assignDriverBtn.setText(generalFunc.retrieveLangLBl("Assign Driver", "LBL_ASSIGN_DRIVER"));
                        assignDriverBtn.setEnabled(false);
                        assignDriverBtnAra.setVisibility(View.GONE);
                        reAssignArea.setVisibility(View.GONE);
                        ((MaterialRippleLayout) trackOrderBtn.getParent()).setVisibility(View.VISIBLE);
                    } else if (assignStatus.equalsIgnoreCase("REQ_PROCESS")) {
                        deliveryStatusTxtView.setVisibility(View.GONE);

                        mAssignDriverProgressBar.setVisibility(View.VISIBLE);
                        mAssignDriverProgressBar.setIndeterminate(true);
                        mAssignDriverProgressBar.getIndeterminateDrawable().setColorFilter(
                                Color.parseColor("#FFFFFF"), android.graphics.PorterDuff.Mode.SRC_IN);

                        assignDriverBtnAra.setVisibility(View.VISIBLE);
                        assignDriverBtn.setEnabled(false);

                        if (REQUEST_REMAINS_SEC > 0) {

                            new Handler().postDelayed(() -> {
                                getOrderDetails();
                            }, REQUEST_REMAINS_SEC * 1000);

                        }


                        reAssignArea.setVisibility(View.GONE);
                        assignDriverBtn.setText(generalFunc.retrieveLangLBl("Assigning Driver", "LBL_ASSIGNING_DELIVERY_DRIVER"));
                        ((MaterialRippleLayout) trackOrderBtn.getParent()).setVisibility(View.GONE);
                    } else if (assignStatus.equalsIgnoreCase("REQ_FAILED")) {
                        deliveryStatusTxtView.setText(generalFunc.retrieveLangLBl("Delivery executive not assigned.", "LBL_DELIVERY_DRIVER_NOT_ASSIGNED"));
                        deliveryStatusTxtView.setVisibility(View.VISIBLE);
                        reAssignArea.setVisibility(View.VISIBLE);
                        assignDriverBtnAra.setVisibility(View.GONE);
                        assignDriverBtn.setText(generalFunc.retrieveLangLBl("Assign Driver", "LBL_ASSIGN_DRIVER"));
                        ((MaterialRippleLayout) trackOrderBtn.getParent()).setVisibility(View.GONE);
                    } else {
                        ((MaterialRippleLayout) trackOrderBtn.getParent()).setVisibility(View.GONE);
                        deliveryStatusTxtView.setVisibility(View.GONE);
                        assignDriverBtn.setText(generalFunc.retrieveLangLBl("Assign Driver", "LBL_ASSIGN_DRIVER"));
                    }


                    if (eConfirm.equalsIgnoreCase("Yes") || eDecline.equalsIgnoreCase("Yes")) {
                        confirmDeclineAreaView.setVisibility(View.GONE);
                    }


                    int totalItems = GeneralFunctions.parseIntegerValue(0, generalFunc.getJsonValueStr("TotalItems", obj_msg));

                    totalItemsTxt.setText(totalItems > 1 ? (generalFunc.convertNumberWithRTL("" + totalItems) + " " + generalFunc.retrieveLangLBl("Items", "LBL_ITEMS")) : (generalFunc.convertNumberWithRTL("" + totalItems) + " " + generalFunc.retrieveLangLBl("Item", "LBL_ITEM")));


                    JSONArray fareArr = generalFunc.getJsonArray("FareDetailsArr", obj_msg);

                    if (fareArr != null) {
                        setChargeDetails(fareArr);
                    }

                    dataList.clear();
                    JSONArray msgArr = generalFunc.getJsonArray("itemlist", obj_msg);
                    if (msgArr != null) {

                        for (int i = 0; i < msgArr.length(); i++) {
                            JSONObject obj_tmp = generalFunc.getJsonObject(msgArr, i);
                            HashMap<String, String> dataMap = new HashMap<>();
                            dataMap.put("DriverAssign", driverAssign);
                            dataMap.put("eConfirm", eConfirm);
                            dataMap.put("eDecline", eDecline);

                            String iQty = generalFunc.getJsonValueStr("iQty", obj_tmp);
                            dataMap.put("iQty", iQty);
                            dataMap.put("iQtyConveretd", generalFunc.convertNumberWithRTL(iQty));

                            dataMap.put("MenuItem", generalFunc.getJsonValueStr("MenuItem", obj_tmp));
                            dataMap.put("MenuItemToppings", generalFunc.getJsonValueStr("MenuItemToppings", obj_tmp));
                            dataMap.put("MenuItemOptions", generalFunc.getJsonValueStr("MenuItemOptions", obj_tmp));
                            dataMap.put("eAvailable", generalFunc.getJsonValueStr("eAvailable", obj_tmp));
                            dataMap.put("SubTitle", generalFunc.getJsonValueStr("SubTitle", obj_tmp));
                            dataMap.put("SubTitleConverted", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("SubTitle", obj_tmp)));
                            dataMap.put("iOrderDetailId", generalFunc.getJsonValueStr("iOrderDetailId", obj_tmp));
                            String fTotPrice = generalFunc.getJsonValueStr("fTotPrice", obj_tmp);
                            dataMap.put("fTotPrice", fTotPrice);
                            dataMap.put("fTotPriceConverted", generalFunc.convertNumberWithRTL(fTotPrice));

                            String TotalDiscountPrice = generalFunc.getJsonValueStr("TotalDiscountPrice", obj_tmp);
                            dataMap.put("TotalDiscountPrice", TotalDiscountPrice);
                            dataMap.put("TotalDiscountPriceConverted", generalFunc.convertNumberWithRTL(TotalDiscountPrice));

                            dataMap.put("LBL_AVAILABLE", generalFunc.retrieveLangLBl("", "LBL_AVAILABLE"));
                            dataMap.put("LBL_NOT_AVAILABLE", generalFunc.retrieveLangLBl("", "LBL_NOT_AVAILABLE"));

                            dataList.add(dataMap);
                        }
                        adapter.notifyDataSetChanged();
                        containerView.setVisibility(View.VISIBLE);
                    } else {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_TRY_AGAIN_LATER_TXT"), true);
                    }
                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)), true);
                }
            } else {
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_TRY_AGAIN_LATER_TXT"), true);
            }

            loadingBar.setVisibility(View.GONE);
        });
        exeServerTask.execute();

    }

  /*  public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.trip_accept_menu, menu);

        menu.findItem(R.id.menu_specialInstruction).setTitle(generalFunc.retrieveLangLBl("Special Instruction", "LBL_FOOD_INSTRUCTION_TXT"));
        menu.findItem(R.id.menu_specialInstruction).setVisible(true);

        Utils.setMenuTextColor(menu.findItem(R.id.menu_specialInstruction), getResources().getColor(R.color.appThemeColor_TXT_1));
       
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_specialInstruction:

                if (vInstruction != null && !vInstruction.equals("")) {
                    generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Special Instruction", "LBL_FOOD_INSTRUCTION_TXT"), vInstruction);
                } else {
                    generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Special Instruction", "LBL_FOOD_INSTRUCTION_TXT"), generalFunc.retrieveLangLBl("", "LBL_NO_FOOD_INSTRUCTION"));

                }
                return true;
            case R.id.menu_printItems:
                generateBillData();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
 
*/

    /*Thermal Print Start*/
    private void generateBillData(boolean skipRedirect) {


        if (skipRedirect) {
            if (MyApp.generateOrderBill != null && MyApp.btsocket != null) {
                MyApp.generateOrderBill.printBill(getMsgFormate(), dataList, reqiuredDetails);
            }
        } else {
            if (MyApp.generateOrderBill == null) {
                MyApp.generateOrderBill = GenerateOrderBill.getInstance();
                MyApp.generateOrderBill.init(OrderDetailActivity.this);

            }


            if (MyApp.btsocket == null) {

           /* if (ContextCompat.checkSelfPermission(getActContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions((Activity) getActContext(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_COARSE_LOCATION);
            }else {
                OpenPrinterList();
            }*/
                Bundle bn = new Bundle();
                new StartActProcess(getActContext()).startActForResult(ThermalPrintSettingActivity.class, bn, CommonUtilities.MY_THERMAL_REQ_CODE);

            } else {
                MyApp.generateOrderBill.printBill(getMsgFormate(), dataList, reqiuredDetails);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_COARSE_LOCATION: {
                connectThermalPrinter();
                break;

            }
            case Utils.REQUEST_ENABLE_BT: {
                if (customDialog != null) {
                    customDialog.closeDialog(false);
                }
                break;

            }
        }
    }


    private void connectThermalPrinter() {

        if (customDialog != null && customDialog.isShowing()) {
            return;
        }

        customDialog = new CustomDialog(getActContext());
        customDialog.setDetails(generalFunc.retrieveLangLBl("", "LBL_T_PRINTER_ALERT_TITLE_TXT"), null, null, generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"), false, R.drawable.ic_printer, true, true, 2);
        customDialog.setPrintDetails(true, dataList, reqiuredDetails);
        customDialog.setDirection(CustomDialog.OpenDirection.BOTTOM);
        customDialog.setRoundedViewBackgroundColor(R.color.appThemeColor_1);
        customDialog.setIconTintColor(R.color.white);
        customDialog.setBtnRadius(10);
        customDialog.setImgStrokWidth(10);
        customDialog.setTitleTxtColor(R.color.appThemeColor_1);
        customDialog.createDialog();
       /* customDialog.setNegativeButtonClick(new Closure() {
            @Override
            public void exec() {
            }
        });*/
        customDialog.setCloseDialogListener(new Closure() {
            @Override
            public void exec() {
                customDialog = null;
            }
        });
        customDialog.show();
    }

    private String[] getMsgFormate() {
        String currentString = generalFunc.retrieveValue(Utils.KOT_BILL_FORMAT_KEY);

        String[] items = currentString.split("#");
        return items;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.TRACK_ORDER_REQ_CODE && resultCode == RESULT_OK) {
            backImgView.performClick();
        } else if (requestCode == Utils.REQUEST_ENABLE_BT /*&& resultCode == RESULT_OK */) {
            customDialog.startDiscovery();
        } else if (requestCode == CommonUtilities.MY_THERMAL_REQ_CODE /*&& resultCode == RESULT_OK */) {
//            generateBillData(true);
        }
    }

    /*Thermal Print End*/

    public void sinchCall() {


        if (generalFunc.isCallPermissionGranted(false) == false) {
            generalFunc.isCallPermissionGranted(true);
        } else {
            try {
                if (new AppFunctions(getActContext()).checkSinchInstance(getSinchServiceInterface())) {

                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("Id", generalFunc.getMemberId());
                    hashMap.put("Name", vCompany);
                    hashMap.put("type", Utils.userType);
                    hashMap.put("PImage", vRestuarantImage);

                    String image_url = CommonUtilities.USER_PHOTO_PATH + iUserid + "/"
                            + vUserImage;

                    getSinchServiceInterface().getSinchClient().setPushNotificationDisplayName(generalFunc.retrieveLangLBl("", "LBL_INCOMING_CALL"));
                    Call call = getSinchServiceInterface().callUser(Utils.CALLTOPASSENGER + "_" + iUserid, hashMap);

                    String callId = call.getCallId();

                    Intent callScreen = new Intent(getActContext(), CallScreenActivity.class);
                    callScreen.putExtra(SinchService.CALL_ID, callId);
                    callScreen.putExtra("vImage", image_url);
                    callScreen.putExtra("vName", userName);
                    callScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(callScreen);
                }

            } catch (Exception e) {
                Logger.d("SinchException", "::" + e.toString());
                if (new AppFunctions(getActContext()).checkSinchInstance(getSinchServiceInterface())) {
                    getSinchServiceInterface().startClient(Utils.userType + "_" + generalFunc.getMemberId());
                    sinchCall();
                }
            }
        }


    }

    public void call(String phoneNumber) {

        try {

            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);

        } catch (Exception e) {
        }
    }

    public void getMaskNumber() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getCallMaskNumber");
        //parameters.put("iTripid", mapData.get("iTripId"));
        parameters.put("UserType", Utils.userType);
        parameters.put("iMemberId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);

        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseObj = generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);

                if (isDataAvail == true) {
                    String message = generalFunc.getJsonValueStr(Utils.message_str, responseObj);

                    call(message);
                } else {

                    call(userPhoneNumber);


                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActContext());

            if (view.getId() == declineBtn.getId() || view.getId() == declineAssignBtn.getId()) {
                getDeclineReasonsList();
                return;
            } else if (view.getId() == confirmBtn.getId()) {

                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Are you sure, you want to confirm order? After confirming order you will not be able to change items' availability.", "LBL_CONFIRM_ORDER_ALERT"), generalFunc.retrieveLangLBl("", "LBL_BTN_NO_TXT"), generalFunc.retrieveLangLBl("", "LBL_BTN_YES_TXT"), buttonId -> {
                    if (buttonId == 1) {
                        confirmOrder("");
                    }
                });


                return;
            } else if (view.getId() == trackOrderBtn.getId()) {
                Bundle bn = new Bundle();
                bn.putString("iOrderId", orderData.get("iOrderId"));
                (new StartActProcess(getActContext())).startActForResult(TrackOrderActivity.class, bn, Utils.TRACK_ORDER_REQ_CODE);
                return;
            } else if (view.getId() == assignDriverBtn.getId() || view.getId() == reAssignBtn.getId()) {
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_CONFIRM_ASSIGN_DRIVER"), generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"), generalFunc.retrieveLangLBl("", "LBL_ASSIGN"), buttonId -> {

                    if (buttonId == 1) {


                        configCarList();

                    }
                });

                return;
            } else if (view.getId() == orderPickedUpBtn.getId()) {
                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("Are you sure, you want to confirm order? After confirming order you will not be able to change items' availability.", "LBL_CONFIRM_NOTE_PICKUP_ORDER"), generalFunc.retrieveLangLBl("", "LBL_BTN_NO_TXT"), generalFunc.retrieveLangLBl("", "LBL_BTN_YES_TXT"), buttonId -> {
                    if (buttonId == 1) {
                        confirmOrder("Yes");
                    }
                });
                return;
            } else if (view.getId() == viewPrescTxtView.getId()) {

                Bundle bn = new Bundle();
                bn.putSerializable("imageList", imageList);
                (new StartActProcess(getActContext())).startActWithData(PrescriptionActivity.class, bn);

            }

            switch (view.getId()) {
                case R.id.backImgView:
                    OrderDetailActivity.super.onBackPressed();
                    break;

                case R.id.iconImgView:
                    // call(userPhoneNumber);

                    if (generalFunc.getJsonValue("RIDE_DRIVER_CALLING_METHOD", userprofileJson).equals("Voip")) {
                        sinchCall();
                    } else {
                        getMaskNumber();
                    }

                    break;

                case R.id.connectPrinterArea:
                    generateBillData(false);
                    break;

                case R.id.iconInstructionView:
                    Bundle bundle = new Bundle();

                    boolean hasInstructions = vInstruction != null && !vInstruction.equals("");
                    if (hasInstructions) {
                        bundle.putString("vInstruction", vInstruction);
                        // generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Special Instruction", "LBL_FOOD_INSTRUCTION_TXT"), vInstruction);
                    } //else {
                    // generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Special Instruction", "LBL_FOOD_INSTRUCTION_TXT"), generalFunc.retrieveLangLBl("", "LBL_NO_FOOD_INSTRUCTION"));
                    // }

                    if (isPrefrence || hasInstructions) {
                        iconInstructionView.setVisibility(View.VISIBLE);

                        bundle.putString("DeliveryPreferences", DeliveryPreferences.toString());
                        new StartActProcess(getActContext()).startActWithData(UserPrefrenceActivity.class, bundle);
                    }

                    break;

                case R.id.iconRefreshImgView:
                    getOrderDetails();
                    break;

                case R.id.bgView:
                    if (chargeDetailArea.getVisibility() == View.VISIBLE) {
                        chargeDetailArea.setVisibility(View.GONE);
                        bgView.setVisibility(View.GONE);
                        if (isPrefrence) {
                            moreinstructionLyout.setVisibility(View.GONE);
                        }
                        View totalChargeView = chargeDetailTitleArea.getChildAt(0);
                        ((ImageView) totalChargeView.findViewById(R.id.indicatorImg)).setVisibility(View.VISIBLE);
                        (totalChargeView.findViewById(R.id.shadowView)).setVisibility(View.VISIBLE);
                        ((ImageView) totalChargeView.findViewById(R.id.indicatorImg)).setImageResource(R.mipmap.ic_arrow_up);
                        if (PrescriptionImages != null && !PrescriptionImages.equals("")) {
                            viewPrescTxtView.setVisibility(View.VISIBLE);
                        }

                    }
                    break;
                case R.id.chargeDetailTitleArea:
                    if (chargeDetailArea.getVisibility() == View.VISIBLE) {
                        chargeDetailArea.setVisibility(View.GONE);
                        bgView.setVisibility(View.GONE);

                        if (isPrefrence) {
                            moreinstructionLyout.setVisibility(View.GONE);
                        }

                        View totalChargeView = chargeDetailTitleArea.getChildAt(0);
                        ((ImageView) totalChargeView.findViewById(R.id.indicatorImg)).setVisibility(View.VISIBLE);
                        (totalChargeView.findViewById(R.id.shadowView)).setVisibility(View.VISIBLE);
                        ((ImageView) totalChargeView.findViewById(R.id.indicatorImg)).setImageResource(R.mipmap.ic_arrow_up);
                        if (PrescriptionImages != null && !PrescriptionImages.equals("")) {
                            viewPrescTxtView.setVisibility(View.VISIBLE);
                        }

                    } else {
                        if (isPrefrence) {
                            moreinstructionLyout.setVisibility(View.GONE);
                        }


                        if (PrescriptionImages != null && !PrescriptionImages.equals("")) {
                            viewPrescTxtView.setVisibility(View.GONE);
                        }

                        chargeDetailArea.setVisibility(View.VISIBLE);
                        bgView.setVisibility(View.VISIBLE);

                        View totalChargeView = chargeDetailTitleArea.getChildAt(0);
                        ((ImageView) totalChargeView.findViewById(R.id.indicatorImg)).setVisibility(View.VISIBLE);
                        ((ImageView) totalChargeView.findViewById(R.id.indicatorImg)).setImageResource(R.mipmap.ic_arrow_down);
                        (totalChargeView.findViewById(R.id.shadowView)).setVisibility(View.GONE);

                        View firstChargeView = chargeDetailArea.getChildAt(0);
                        ((ImageView) firstChargeView.findViewById(R.id.indicatorImg)).setVisibility(View.GONE);
                        (firstChargeView.findViewById(R.id.shadowView)).setVisibility(View.VISIBLE);

                    }
                    break;
            }
        }
    }


    public void openDriverType(String selected_carId) {

        new DriverTypeDialog().run();
    }

    class DriverTypeDialog implements Runnable {

        @Override
        public void run() {

            final Dialog dialog_img_update = new Dialog(getActContext(), R.style.ImageSourceDialogStyle);
            dialog_img_update.setContentView(R.layout.design_image_source_select_new);
            MTextView personalTxt = (MTextView) dialog_img_update.findViewById(R.id.cameraTxt);
            MTextView otherTxt = (MTextView) dialog_img_update.findViewById(R.id.galleryTxt);
            MTextView titleTxt = (MTextView) dialog_img_update.findViewById(R.id.titleTxt);
            LinearLayout cameraView = (LinearLayout) dialog_img_update.findViewById(R.id.cameraView);
            LinearLayout galleryView = (LinearLayout) dialog_img_update.findViewById(R.id.galleryView);

            MButton btn_type2 = ((MaterialRippleLayout) dialog_img_update.findViewById(R.id.btn_type2)).getChildView();
            btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));

            personalTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PERSONAL"));
            otherTxt.setText(generalFunc.retrieveLangLBl("", "LBL_OTHER_TXT"));
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PROVIDER_SELECTION_TXT"));

            SelectableRoundedImageView cameraIconImgView = (SelectableRoundedImageView) dialog_img_update.findViewById(R.id.cameraIconImgView);
            SelectableRoundedImageView galleryIconImgView = (SelectableRoundedImageView) dialog_img_update.findViewById(R.id.galleryIconImgView);

            ImageView closeDialogImgView = (ImageView) dialog_img_update.findViewById(R.id.closeDialogImgView);

            closeDialogImgView.setOnClickListener(v -> {

                if (dialog_img_update != null) {
                    dialog_img_update.cancel();
                }

            });

            btn_type2.setOnClickListener(view -> dialog_img_update.dismiss());


            cameraView.setOnClickListener(v -> {

                if (dialog_img_update != null) {
                    dialog_img_update.cancel();
                }
                eDriverType = "Personal";
                assignDriver(selected_carId);


            });

            galleryView.setOnClickListener(v -> {

                if (dialog_img_update != null) {
                    dialog_img_update.cancel();
                }
                eDriverType = "Site";
                assignDriver(selected_carId);


            });

            dialog_img_update.setCanceledOnTouchOutside(true);

            Window window = dialog_img_update.getWindow();
            window.setGravity(Gravity.BOTTOM);

            window.setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            dialog_img_update.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            if (generalFunc.isRTLmode()) {
                dialog_img_update.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }

            dialog_img_update.show();

        }

    }


    public void configCarList() {
        final HashMap<String, String> parameters = new HashMap<String, String>();

        parameters.put("type", "LoadAvailableStoreCars");
        parameters.put("iCompanyId", generalFunc.getMemberId());


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {


                    LoadCarList(generalFunc.getJsonArray(Utils.message_str, responseString));


                } else {
                    String msg = generalFunc.getJsonValue(Utils.message_str, responseString);
                    if (msg.equalsIgnoreCase("LBL_NO_CAR_AVAIL_TXT")) {
                        GenerateAlertBox alertBox = new GenerateAlertBox(getActContext());
                        alertBox.setContentMessage("", generalFunc.retrieveLangLBl("", msg));
                        alertBox.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
                        alertBox.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CONTACT_US_TXT"));
                        alertBox.setBtnClickList(btn_id -> {

                            alertBox.closeAlertBox();
                            if (btn_id == 0) {
                                new StartActProcess(getActContext()).startAct(ContactUsActivity.class);
                            }
                        });
                        alertBox.showAlertBox();
                    }
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    @Override
    public void onItemClick(int position, int viewClickId) {
        list_car.dismiss();

        selected_carId = items_car_id.get(position);

        if (isOpenDriverSelection) {
            openDriverType(selected_carId);
        } else {
            assignDriver(selected_carId);
        }

    }



    public void LoadCarList(JSONArray array) {

        final ArrayList list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj_temp = generalFunc.getJsonObject(array, i);

            items_txt_car.add(generalFunc.getJsonValueStr("vVehicleTitle", obj_temp));

            items_car_id.add(generalFunc.getJsonValueStr("iVehicleTypeId", obj_temp));
            items_txt_car_json.add(obj_temp.toString());

            HashMap<String, String> map = new HashMap<String, String>();
            map.put("VehicleTitle", items_txt_car.get(i).toString());
            map.put("iVehicleTypeId", items_car_id.get(i).toString());
            list.add(map);
        }

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActContext());

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_selectcar_view, null);

        final MTextView vehTitleTxt = (MTextView) dialogView.findViewById(R.id.VehiclesTitleTxt);
        final MTextView mangeVehiclesTxt = (MTextView) dialogView.findViewById(R.id.mangeVehiclesTxt);
        final MTextView addVehiclesTxt = (MTextView) dialogView.findViewById(R.id.addVehiclesTxt);
        final ImageView cancel = (ImageView) dialogView.findViewById(R.id.cancel);
        final RecyclerView vehiclesRecyclerView = (RecyclerView) dialogView.findViewById(R.id.vehiclesRecyclerView);

        cancel.setOnClickListener(v -> list_car.dismiss());
        builder.setView(dialogView);
        vehTitleTxt.setText(generalFunc.retrieveLangLBl("Select Delivery Vehicles", "LBL_SELECT_DELIVERY_TXT"));

        ManageVehicleListAdapter adapter = new ManageVehicleListAdapter(getActContext(), list, generalFunc);
        vehiclesRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickList(this);


        list_car = builder.create();
        if (generalFunc.isRTLmode()) {
            generalFunc.forceRTLIfSupported(list_car);
        }
        list_car.getWindow().setBackgroundDrawable(getActContext().getResources().getDrawable(R.drawable.all_roundcurve_card));
        list_car.show();

        list_car.setCancelable(false);
        final Button positiveButton = list_car.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.appThemeColor_1));
        final Button negativeButton = list_car.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(R.color.black));
        list_car.setOnCancelListener(dialogInterface -> Utils.hideKeyboard(getActContext()));
    }

}
