package com.roaddo.store;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.adapter.files.ViewPagerAdapter;
import com.fragments.HistoryFragment;
import com.fragments.InactiveFragment;
import com.fragments.Itemfragment;
import com.fragments.MyProfileFragment;
import com.fragments.OrderFragment;
import com.general.files.GeneralFunctions;
import com.general.files.InternetConnection;
import com.general.files.MyApp;
import com.general.files.OpenAdvertisementDialog;
import com.general.files.StartActProcess;
import com.general.files.UpdateFrequentTask;
import com.general.files.Closure;
import com.general.files.CustomDialog;
import com.general.files.thermalPrint.GenerateOrderBill;
import com.pubnub.api.enums.PNStatusCategory;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MTextView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public GeneralFunctions generalFunc;


    ImageView menuImgView;
    ArrayList<String[]> list_menu_items;

    UpdateFrequentTask updateRequest;

    //BackgroundAppReceiver bgAppReceiver;

    boolean isCurrentReqHandled = false;


    MTextView leftTitleTxt;

    InternetConnection intCheck;
    View mainPageContainerView;
    View fragContainer;
    TabLayout material_tabs;
    public ViewPager viewPager;
    ImageView headerLogo;
    ViewPagerAdapter ordersPagerAdapter;
    ImageView menuImgRightView;
    public boolean isFirstCall = true;
    Handler newOrderHandler;


    private JSONObject obj_userProfile;
    String LBL_NEW = "";
    String LBL_PROCESSING = "";
    String LBL_DISPATCHED = "";
    /*Thermal Print*/
    public CustomDialog customDialog;


    public LinearLayout profileArea, homeArea, earningArea, SettingArea;
    MTextView profileTxt, homeTxt, earnIngTxt, settingTxt;
    ImageView home_img, profileImg, earnImg, settingImg;
    MyProfileFragment myProfileFragment = null;
    Itemfragment settingsFragment = null;
    HistoryFragment historyFragment = null;
    FrameLayout frameLayout;

    public LinearLayout bottomArea;
    public View line;
    int bottomBtnpos = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        intCheck = new InternetConnection(this);

        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        String advertise_banner_data = generalFunc.getJsonValueStr("advertise_banner_data", obj_userProfile);
        if (advertise_banner_data != null && !advertise_banner_data.equalsIgnoreCase("")) {

            String image_url = generalFunc.getJsonValue("image_url", advertise_banner_data);
            if (image_url != null && !image_url.equalsIgnoreCase("")) {
                HashMap<String, String> map = new HashMap<>();
                map.put("image_url", image_url);
                map.put("tRedirectUrl", generalFunc.getJsonValue("tRedirectUrl", advertise_banner_data));
                map.put("vImageWidth", generalFunc.getJsonValue("vImageWidth", advertise_banner_data));
                map.put("vImageHeight", generalFunc.getJsonValue("vImageHeight", advertise_banner_data));
                new OpenAdvertisementDialog(getActContext(), map, generalFunc);
            }
        }


        frameLayout = (FrameLayout) findViewById(R.id.container);
        leftTitleTxt = (MTextView) findViewById(R.id.leftTitleTxt);
        menuImgView = (ImageView) findViewById(R.id.menuImgView);
        menuImgView.setVisibility(View.GONE);
        headerLogo = (ImageView) findViewById(R.id.headerLogo);
        bottomArea = (LinearLayout) findViewById(R.id.bottomArea);
        line = (View) findViewById(R.id.line);


        mainPageContainerView = findViewById(R.id.mainPageContainerView);
        fragContainer = findViewById(R.id.fragContainer);
        material_tabs = (TabLayout) findViewById(R.id.material_tabs);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        menuImgRightView = (ImageView) findViewById(R.id.menuImgRightView);


        menuImgRightView.setVisibility(View.VISIBLE);
        menuImgRightView.setImageResource(R.mipmap.ic_header_refresh);


        profileTxt = findViewById(R.id.profileTxt);
        homeTxt = findViewById(R.id.homeTxt);
        earnIngTxt = findViewById(R.id.earnIngTxt);
        settingTxt = findViewById(R.id.settingTxt);
        home_img = findViewById(R.id.home_img);
        profileImg = findViewById(R.id.profileImg);
        earnImg = findViewById(R.id.earnImg);
        settingImg = findViewById(R.id.settingImg);


        profileArea = findViewById(R.id.profileArea);
        homeArea = findViewById(R.id.homeArea);
        earningArea = findViewById(R.id.earningArea);
        SettingArea = findViewById(R.id.SeetingArea);
        profileArea.setOnClickListener(new setOnClickList());
        homeArea.setOnClickListener(new setOnClickList());
        earningArea.setOnClickListener(new setOnClickList());
        SettingArea.setOnClickListener(new setOnClickList());
        manageBottomMenu(homeTxt);
        setLabel();
        //bgAppReceiver = new BackgroundAppReceiver(getActContext());


        setGeneralData();

        buildMenu();

        setUserInfo();
        setData();

        menuImgView.setOnClickListener(new setOnClickList());
        menuImgRightView.setOnClickListener(new setOnClickList());

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            String restratValue_str = savedInstanceState.getString("RESTART_STATE");

            if (restratValue_str != null && !restratValue_str.equals("") && restratValue_str.trim().equals("true")) {
                generalFunc.restartApp();
            }
        }

        generalFunc.storeData(Utils.DRIVER_CURRENT_REQ_OPEN_KEY, "false");

        removeOldRequestsCode();

        // registerBackgroundAppReceiver();

        String eStatus = generalFunc.getJsonValueStr("eStatus", obj_userProfile);

        if (eStatus.equalsIgnoreCase("inactive")) {

            menuImgRightView.setVisibility(View.GONE);

            headerLogo.setVisibility(View.VISIBLE);
            fragContainer.setVisibility(View.VISIBLE);
            leftTitleTxt.setVisibility(View.GONE);
            mainPageContainerView.setVisibility(View.GONE);
            InactiveFragment inactiveFragment = new InactiveFragment();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            ft.replace(R.id.fragContainer, inactiveFragment);
            ft.commit();

        } else {
            fragContainer.setVisibility(View.GONE);
            mainPageContainerView.setVisibility(View.VISIBLE);

            ordersPagerAdapter = buildViewPager();
            viewPager.setOffscreenPageLimit(3);
            viewPager.setAdapter(ordersPagerAdapter);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    try {
                        ((OrderFragment) ordersPagerAdapter.getItem(position)).getOrders(false);
                    } catch (Exception e) {

                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            material_tabs.setupWithViewPager(viewPager);
        }

        generalFunc.deleteTripStatusMessages();

    }


    public void openProfileFragment() {
        frameLayout.setVisibility(View.VISIBLE);

//        if (myProfileFragment != null) {
//            myProfileFragment = null;
//            Utils.runGC();
//        }


        if (myProfileFragment == null) {
            myProfileFragment = new MyProfileFragment();
        }

//
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.container, myProfileFragment).commit();
        openPageFrag(4, myProfileFragment);

        bottomBtnpos = 4;

    }

    public void openSettingFragment() {
        frameLayout.setVisibility(View.VISIBLE);

//        if (myProfileFragment != null) {
//            myProfileFragment = null;
//            Utils.runGC();
//        }


        if (settingsFragment == null) {
            settingsFragment = new Itemfragment();
        }


//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.container, settingsFragment).commit();
        openPageFrag(2, settingsFragment);

        bottomBtnpos = 2;

    }

    public void openEarningFragment() {
        frameLayout.setVisibility(View.VISIBLE);

//        if (myProfileFragment != null) {
//            myProfileFragment = null;
//            Utils.runGC();
//        }


        if (historyFragment == null) {
            historyFragment = new HistoryFragment();
        } else {
            historyFragment.onDestroy();
            historyFragment = new HistoryFragment();
        }


//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.container, historyFragment).commit();

        openPageFrag(3, historyFragment);

        bottomBtnpos = 3;


    }


    public void openPageFrag(int position, Fragment fragToOpen) {
        int leftAnim = bottomBtnpos > position ? R.anim.slide_from_left : R.anim.slide_from_right;
        int rightAnim = bottomBtnpos > position ? R.anim.slide_to_right : R.anim.slide_to_left;

        getSupportFragmentManager().beginTransaction().setCustomAnimations(leftAnim,
                rightAnim)
                .replace(R.id.container, fragToOpen).commit();


//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.container, fragToOpen).commit();

    }


    public void setLabel() {
        profileTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HEADER_RDU_PROFILE"));
        homeTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME"));
        earnIngTxt.setText(generalFunc.retrieveLangLBl("", "LBL_EARNING"));
        settingTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ITEMS"));
    }

    public void manageBottomMenu(MTextView selTextView) {
        //manage Select deselect Bottom Menu
        if (selTextView.getId() == homeTxt.getId()) {
            homeTxt.setTextColor(getActContext().getResources().getColor(R.color.appThemeColor_1));
            home_img.setColorFilter(ContextCompat.getColor(getActContext(), R.color.appThemeColor_1), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            homeTxt.setTextColor(getActContext().getResources().getColor(R.color.homedeSelectColor));
            home_img.setColorFilter(ContextCompat.getColor(getActContext(), R.color.homedeSelectColor), android.graphics.PorterDuff.Mode.SRC_IN);
        }


        if (selTextView.getId() == profileTxt.getId()) {
            profileTxt.setTextColor(getActContext().getResources().getColor(R.color.appThemeColor_1));
            profileImg.setColorFilter(ContextCompat.getColor(getActContext(), R.color.appThemeColor_1), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            profileTxt.setTextColor(getActContext().getResources().getColor(R.color.homedeSelectColor));
            profileImg.setColorFilter(ContextCompat.getColor(getActContext(), R.color.homedeSelectColor), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        if (selTextView.getId() == earnIngTxt.getId()) {
            earnIngTxt.setTextColor(getActContext().getResources().getColor(R.color.appThemeColor_1));
            earnImg.setColorFilter(ContextCompat.getColor(getActContext(), R.color.appThemeColor_1), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            earnIngTxt.setTextColor(getActContext().getResources().getColor(R.color.homedeSelectColor));
            earnImg.setColorFilter(ContextCompat.getColor(getActContext(), R.color.homedeSelectColor), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        if (selTextView.getId() == settingTxt.getId()) {
            settingTxt.setTextColor(getActContext().getResources().getColor(R.color.appThemeColor_1));
            settingImg.setColorFilter(ContextCompat.getColor(getActContext(), R.color.appThemeColor_1), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            settingTxt.setTextColor(getActContext().getResources().getColor(R.color.homedeSelectColor));
            settingImg.setColorFilter(ContextCompat.getColor(getActContext(), R.color.homedeSelectColor), android.graphics.PorterDuff.Mode.SRC_IN);

        }


        bottomBtnpos = 1;

    }

    private ViewPagerAdapter buildViewPager() {
        ArrayList<Fragment> fragmentList = new ArrayList<>();

        material_tabs.setVisibility(View.VISIBLE);

        fragmentList.add(generateOrderFrag("NEW"));
        fragmentList.add(generateOrderFrag("INPROCESS"));
        fragmentList.add(generateOrderFrag("DISPATCHED"));

        LBL_NEW = generalFunc.retrieveLangLBl("", "LBL_NEW");
        LBL_PROCESSING = generalFunc.retrieveLangLBl("", "LBL_PROCESSING");
        LBL_DISPATCHED = generalFunc.retrieveLangLBl("", "LBL_DISPATCHED");

        CharSequence[] titles = new CharSequence[]{LBL_NEW, LBL_PROCESSING, LBL_DISPATCHED};
        ViewPagerAdapter ordersPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, fragmentList);

        return ordersPagerAdapter;
    }

    public void resetNames(int size, int size1, int size2) {
        if (ordersPagerAdapter == null) {
            return;
        }
        String tabName, secondTabName, thirdTabName;

        /*tabName = size > 0 ? LBL_NEW + "\n (" + generalFunc.convertNumberWithRTL(size + "") + ")" : (size == 0 ? LBL_NEW : ordersPagerAdapter.getPageTitle(0).toString());*/

        tabName = size > 0 ? LBL_NEW + "(" + generalFunc.convertNumberWithRTL(size + "") + ")" : (size == 0 ? LBL_NEW : ordersPagerAdapter.getPageTitle(0).toString());

        secondTabName = size1 > 0 ? LBL_PROCESSING + "(" + generalFunc.convertNumberWithRTL(size1 + "") + ")" : (size1 == 0 ? LBL_PROCESSING : ordersPagerAdapter.getPageTitle(1).toString());

        thirdTabName = size2 > 0 ? LBL_DISPATCHED + "(" + generalFunc.convertNumberWithRTL(size2 + "") + ")" : (size2 == 0 ? LBL_DISPATCHED : ordersPagerAdapter.getPageTitle(2).toString());


        CharSequence[] titles = new CharSequence[]{tabName, secondTabName, thirdTabName};

        if (size == 0 && size1 == 0 && size2 == 0) {
            return;
        }
        ordersPagerAdapter.reSetTitles(titles);
        ordersPagerAdapter.notifyDataSetChanged();

    }

    public OrderFragment generateOrderFrag(String bookingType) {
        OrderFragment frag = new OrderFragment();
        Bundle bn = new Bundle();
        bn.putString("ORDER_TYPE", bookingType);
        frag.setArguments(bn);
        return frag;
    }

    public void newOrderReceived() {

        if (ordersPagerAdapter.getCount() > 0 && ordersPagerAdapter.getItem(0) instanceof OrderFragment) {

            if (!MyApp.getInstance().isMyAppInBackGround()) {
                Utils.vibratePhone(getActContext(), 1000);
                Utils.playNotificationSound(getActContext());
            }
            if (((OrderFragment) ordersPagerAdapter.getItem(0)).newOrderNotificationArea != null) {
                ((OrderFragment) ordersPagerAdapter.getItem(0)).newOrderNotificationArea.setVisibility(View.VISIBLE);
                ((OrderFragment) ordersPagerAdapter.getItem(0)).orderRecyclerAdapter.hilightNewOrder();
            }

            if (newOrderHandler != null) {
                newOrderHandler.removeCallbacks(newOrderRunnable);
            }
            newOrderHandler = new Handler();

            newOrderHandler.postDelayed(newOrderRunnable, 6000);

            ((OrderFragment) ordersPagerAdapter.getItem(0)).getOrders(false);
        }
    }

    public void setData() {

        headerLogo.setVisibility(View.GONE);
        leftTitleTxt.setVisibility(View.VISIBLE);
        leftTitleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ORDER"));
        leftTitleTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
    }

    public void removeOldRequestsCode() {

        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(getActContext());
        Map<String, ?> keys = mPrefs.getAll();

        for (Map.Entry<String, ?> entry : keys.entrySet()) {

            if (entry.getKey().contains(Utils.DRIVER_REQ_CODE_PREFIX_KEY)) {
                //generalFunc.removeValue(entry.getKey());
                Long CURRENTmILLI = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 1);
                String value_ = generalFunc.retrieveValue(entry.getKey()) + "";
                long value = generalFunc.parseLongValue(0, value_);
                if (CURRENTmILLI >= value) {
                    generalFunc.removeValue(entry.getKey());
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString("RESTART_STATE", "true");
        super.onSaveInstanceState(outState);
    }

    public void setWalletInfo() {
        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
        // ((MTextView) findViewById(R.id.walletbalncetxt)).setText(generalFunc.retrieveLangLBl("", "LBL_WALLET_BALANCE") + ": " + generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("user_available_balance", obj_userProfile)));
    }

    public void setUserInfo() {
//        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
//        ((MTextView) findViewById(R.id.userNameTxt)).setText(generalFunc.getJsonValueStr("vCompany", obj_userProfile));
//        setWalletInfo();
//        if (generalFunc.retrieveValue(Utils.THERMAL_PRINT_ENABLE_KEY).equalsIgnoreCase("Yes")) {
//            imgSetting.setVisibility(View.VISIBLE);
//            imgSetting.setOnClickListener(new setOnClickList());
//        }
//        (new AppFunctions(getActContext())).checkProfileImage((SelectableRoundedImageView) findViewById(R.id.userImgView), obj_userProfile, "vImage");

    }

    public void showMessageWithAction(View view, String message, final Bundle bn) {
        Snackbar snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_INDEFINITE).setAction(generalFunc.retrieveLangLBl("", "LBL_BTN_VERIFY_TXT"), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        new StartActProcess(getActContext()).startActForResult(VerifyInfoActivity.class, bn, Utils.VERIFY_INFO_REQ_CODE);

                    }
                });
        snackbar.setActionTextColor(getActContext().getResources().getColor(R.color.appThemeColor_1));
        snackbar.setDuration(10000);
        snackbar.show();
    }

    public void setGeneralData() {
        HashMap<String, String> storeData = new HashMap<>();
        storeData.put(Utils.MOBILE_VERIFICATION_ENABLE_KEY, generalFunc.getJsonValueStr("MOBILE_VERIFICATION_ENABLE", obj_userProfile));
        storeData.put("LOCATION_ACCURACY_METERS", generalFunc.getJsonValueStr("LOCATION_ACCURACY_METERS", obj_userProfile));
        storeData.put("DRIVER_LOC_UPDATE_TIME_INTERVAL", generalFunc.getJsonValueStr("DRIVER_LOC_UPDATE_TIME_INTERVAL", obj_userProfile));
        storeData.put(Utils.REFERRAL_SCHEME_ENABLE, generalFunc.getJsonValueStr("REFERRAL_SCHEME_ENABLE", obj_userProfile));

        storeData.put(Utils.WALLET_ENABLE, generalFunc.getJsonValueStr("WALLET_ENABLE", obj_userProfile));
        storeData.put(Utils.REFERRAL_SCHEME_ENABLE, generalFunc.getJsonValueStr("REFERRAL_SCHEME_ENABLE", obj_userProfile));

        generalFunc.storeData(storeData);

    }

    public void buildMenu() {
        if (list_menu_items == null) {
            list_menu_items = new ArrayList();


        } else {
            list_menu_items.clear();
        }

        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_profile, generalFunc.retrieveLangLBl("Profile", "LBL_MY_PROFILE_HEADER_TXT"), "" + Utils.MENU_PROFILE});

        list_menu_items.add(new String[]{"" + R.mipmap.ic_order_history, generalFunc.retrieveLangLBl("", "LBL_EARNING_HISTORY"), "" + Utils.MENU_YOUR_TRIPS});

        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_items, generalFunc.retrieveLangLBl("Menu Items", "LBL_FOOD_MENU"), "" + Utils.MENU_FOOD});

        /*list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_yourtrip, generalFunc.retrieveLangLBl("Earning", "LBL_EARNINGS"), "" + Utils.MENU_EARNING});*/

        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_bank_detail_icon, generalFunc.retrieveLangLBl("Bank Details", "LBL_BANK_DETAILS_TXT"), "" + Utils.MENU_BANK_DETAIL});

        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_doc, generalFunc.retrieveLangLBl("Manage Documents", "LBL_MANAGE_DOCUMENT"), "" + Utils.MENU_YOUR_DOCUMENTS});

        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_chart, generalFunc.retrieveLangLBl("Trip Statistics", "LBL_TRIP_STATISTICS_TXT_DL"), "" + Utils.MENU_TRIP_STATISTICS});

        list_menu_items.add(new String[]{"" + R.mipmap.ic_settings, generalFunc.retrieveLangLBl("Restaurant Settings", "LBL_RESTAURANT_SETTINGS"), "" + Utils.MENU_RESTAURANT_SETTINGS});

        if (generalFunc.getJsonValueStr("ENABLE_NEWS_SECTION", obj_userProfile) != null && generalFunc.getJsonValueStr("ENABLE_NEWS_SECTION", obj_userProfile).equalsIgnoreCase("yes")) {
            list_menu_items.add(new String[]{"" + R.drawable.ic_menu_notification, generalFunc.retrieveLangLBl("Notifications", "LBL_NOTIFICATIONS"), "" + Utils.MENU_NOTIFICATION});
        }

        list_menu_items.add(new String[]{"" + R.mipmap.ic_menu_support, generalFunc.retrieveLangLBl("Support", "LBL_SUPPORT_HEADER_TXT"), "" + Utils.MENU_SUPPORT});


    }

    public void accountVerificationAlert(String message, final Bundle bn) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 1) {
                generateAlert.closeAlertBox();
//                bn.putString("msg", "DO_PHONE_VERIFY");
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

    public void openMenuProfile() {
        new StartActProcess(getActContext()).startActForResult(MyProfileActivity.class, Utils.MY_PROFILE_REQ_CODE);
    }


    public Context getActContext() {
        return MainActivity.this;
    }

   /* public void registerBackgroundAppReceiver() {

        unRegisterBackgroundAppReceiver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Utils.BACKGROUND_APP_RECEIVER_INTENT_ACTION);

        registerReceiver(bgAppReceiver, filter);
    }

    public void unRegisterBackgroundAppReceiver() {
        if (bgAppReceiver != null) {
            try {
                unregisterReceiver(bgAppReceiver);
            } catch (Exception e) {

            }
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        setUserInfo();

        Utils.dismissBackGroundNotification(getActContext());

        updateOrders();

    }

    public void updateOrders() {
        if (ordersPagerAdapter != null) {
            if (isFirstCall == false) {

                try {
                    ((OrderFragment) ordersPagerAdapter.getItem(0)).getOrders(false);
                    ((OrderFragment) ordersPagerAdapter.getItem(1)).getOrders(false);
                    ((OrderFragment) ordersPagerAdapter.getItem(2)).getOrders(false);
                } catch (Exception e) {

                }
            } else {
                isFirstCall = false;
            }

            if (viewPager.getCurrentItem() == 0) {

                if (ordersPagerAdapter.getCount() > 0) {
                    try {
                        ((OrderFragment) ordersPagerAdapter.getItem(0)).configRestaurantSettings("Display", false);
                    } catch (Exception e) {

                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    public MyApp getApp() {
        return ((MyApp) getApplication());
    }

    public void configBackground() {

        if (isCurrentReqHandled == false) {
            generalFunc.removeValue(Utils.DRIVER_ACTIVE_REQ_MSG_KEY);
            return;
        }

        if (getApp().isMyAppInBackGround()) {

        } else {

        }
    }

    @Override
    protected void onDestroy() {

//        disconnectPrinter();

        try {
            //  unRegisterBackgroundAppReceiver();

            if (updateRequest != null) {
                updateRequest.stopRepeatingTask();
                updateRequest = null;
            }


            Utils.runGC();
        } catch (Exception e) {

        }

        super.onDestroy();
    }

    /*Thermal Print Start*/

    public void connectThermalPrinter() {

        Bundle bn = new Bundle();
        new StartActProcess(getActContext()).startActForResult(ThermalPrintSettingActivity.class, bn, CommonUtilities.MY_THERMAL_REQ_CODE);

      /*  MyApp.generateOrderBill = GenerateOrderBill.getInstance();
        MyApp.generateOrderBill.init(this);

        *//*if (MyApp.btsocket == null) {
            Intent BTIntent = new Intent(getApplicationContext(), DeviceList.class);
            this.startActivityForResult(BTIntent, CommonUtilities.REQUEST_CONNECT_BT);

        }*//*

        if (customDialog != null) {
            return;
        }


        customDialog = new CustomDialog(getActContext());
        customDialog.setDetails(null, generalFunc.retrieveLangLBl("", "LBL_T_PRINTER_ALERT_TITLE_TXT"), null, generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"), false, R.drawable.ic_printer, true, true, 2);
        customDialog.setRoundedViewBackgroundColor(R.color.appThemeColor_1);
        customDialog.setIconTintColor(R.color.white);
        customDialog.setBtnRadius(10);
        customDialog.setImgStrokWidth(10);
        customDialog.setTitleTxtColor(R.color.appThemeColor_1);
        customDialog.createDialog();
        customDialog.setNegativeButtonClick(new Closure() {
            @Override
            public void exec() {
            }
        });
        customDialog.setCloseDialogListener(new Closure() {
            @Override
            public void exec() {
                customDialog = null;
            }
        });
        customDialog.show();*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Utils.REQUEST_COARSE_LOCATION: {
                if (MyApp.generateOrderBill != null && MyApp.btsocket != null) {
                    return;
                }

                if (customDialog != null && customDialog.isShowing()) {
                    return;
                }


                if (MyApp.getInstance().isThermalPrintAllowed(false) && MyApp.btsocket == null) {
                    connectThermalPrinter();
                }

                break;

            }
        }
    }


    @Override
    public void onBackPressed() {


        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            if (btn_id == 0) {
                generateAlert.closeAlertBox();
            } else {
                generateAlert.closeAlertBox();
                MainActivity.super.onBackPressed();
            }

        });

        generateAlert.setContentMessage(generalFunc.retrieveLangLBl("Exit App", "LBL_EXIT_APP_TITLE_TXT"), generalFunc.retrieveLangLBl("Are you sure you want to exit?", "LBL_WANT_EXIT_APP_TXT"));
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_YES"));
        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_NO"));
        generateAlert.showAlertBox();

//        if (!generateAlert.getAlertDialog().isShowing()) {
//            super.onBackPressed();
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.MY_PROFILE_REQ_CODE && resultCode == RESULT_OK && data != null) {
            obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
            setUserInfo();
            // ((MTextView) findViewById(R.id.userNameTxt)).setText(generalFunc.getJsonValueStr("vCompany", obj_userProfile));
        } else if (requestCode == Utils.VERIFY_INFO_REQ_CODE && resultCode == RESULT_OK && data != null) {

            String msgType = data.getStringExtra("MSG_TYPE");

            if (msgType.equalsIgnoreCase("EDIT_PROFILE")) {
                openMenuProfile();
            }
        } else if (requestCode == Utils.VERIFY_INFO_REQ_CODE) {
            obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
            buildMenu();
        } else if (requestCode == Utils.CARD_PAYMENT_REQ_CODE && resultCode == RESULT_OK && data != null) {
            obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        } else if (requestCode == Utils.REQUEST_CODE_GPS_ON) {

        } else if (requestCode == Utils.REQUEST_CODE_NETWOEK_ON) {

        } else if (requestCode == Utils.SEARCH_PICKUP_LOC_REQ_CODE) {

        }
        /*Thermal Print*/
        else if (requestCode == Utils.SETTINGS_UPDATED_CODE && resultCode == RESULT_OK) {
            obj_userProfile = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));
            buildMenu();
        } else if (requestCode == Utils.REQUEST_CONNECT_BT || requestCode == Utils.REQUEST_ENABLE_BT/*&& resultCode == Activity.RESULT_OK*/) {
            try {
                MyApp.btsocket = customDialog.getSocket();
                customDialog.startDiscovery();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    /*Thermal Print End*/


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        int itemId = generalFunc.parseIntegerValue(0, list_menu_items.get(position)[2]);

        Utils.hideKeyboard(MainActivity.this);


        Bundle bn = new Bundle();

        switch (itemId) {
            case Utils.MENU_PROFILE:
                openMenuProfile();
                break;

            case Utils.MENU_YOUR_TRIPS:
                new StartActProcess(getActContext()).startActWithData(HistoryActivity.class, bn);
                break;

            case Utils.MENU_FOOD:
                new StartActProcess(getActContext()).startActWithData(ItemAvailabilityActivity.class, bn);
                break;

            case Utils.MENU_YOUR_DOCUMENTS:
                bn.putString("PAGE_TYPE", "Company");
                bn.putString("iDriverVehicleId", "");
                bn.putString("doc_file", "");
                bn.putString("iDriverVehicleId", "");
                new StartActProcess(getActContext()).startActWithData(ListOfDocumentActivity.class, bn);
                break;

            case Utils.MENU_TRIP_STATISTICS:
                new StartActProcess(getActContext()).startActWithData(StatisticsActivity.class, bn);
                break;

            case Utils.MENU_BANK_DETAIL:
                new StartActProcess(getActContext()).startActWithData(BankDetailActivity.class, bn);
                break;

            case Utils.MENU_RESTAURANT_SETTINGS:
                new StartActProcess(getActContext()).startAct(RestaurantSettingsActivity.class);
                break;

            case Utils.MENU_NOTIFICATION:
                new StartActProcess(getActContext()).startAct(NotificationActivity.class);
                break;

            case Utils.MENU_SUPPORT:
                new StartActProcess(getActContext()).startAct(SupportActivity.class);
                break;
        }


    }

    public void pubNubStatus(PNStatusCategory category) {
    }

    public void handleNoNetworkDial() {
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(MainActivity.this);


            if (view.getId() == menuImgRightView.getId()) {
                if (viewPager.getCurrentItem() == 0) {
                    ((OrderFragment) ordersPagerAdapter.getItem(0)).getOrders(false);
                } else if (viewPager.getCurrentItem() == 1) {
                    ((OrderFragment) ordersPagerAdapter.getItem(1)).getOrders(false);
                } else {
                    ((OrderFragment) ordersPagerAdapter.getItem(2)).getOrders(false);
                }
            }

            if (view.getId() == profileArea.getId()) {
                manageBottomMenu(profileTxt);
                openProfileFragment();


            } else if (view.getId() == homeArea.getId()) {
                frameLayout.setVisibility(View.GONE);
                manageBottomMenu(homeTxt);


            } else if (view.getId() == earningArea.getId()) {
                manageBottomMenu(earnIngTxt);
                openEarningFragment();
            } else if (view.getId() == SettingArea.getId()) {
                manageBottomMenu(settingTxt);
                openSettingFragment();
            }


//            if (view.getId() == imgSetting.getId()) {
//                (new StartActProcess(getActContext())).startActForResult(ThermalPrintSettingActivity.class, Utils.SETTINGS_UPDATED_CODE);
//            }

//            if (view.getId() == logoutarea.getId()) {
//                final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
//                generateAlert.setCancelable(false);
//                generateAlert.setBtnClickList(btn_id -> {
//                    if (btn_id == 0) {
//                        generateAlert.closeAlertBox();
//                    } else {
//                        generateAlert.closeAlertBox();
////                            logoutFromDevice();
//                        MyApp.getInstance().logOutFromDevice(false);
//
//                    }
//                });
//                generateAlert.setContentMessage(generalFunc.retrieveLangLBl("Logout", "LBL_LOGOUT"), generalFunc.retrieveLangLBl("Are you sure you want to logout?", "LBL_WANT_LOGOUT_APP_TXT"));
//                generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_YES"));
//                generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_NO"));
//                generateAlert.showAlertBox();
//            }

        }
    }


    Runnable newOrderRunnable = () -> {
        ((OrderFragment) ordersPagerAdapter.getItem(0)).orderRecyclerAdapter.delightNewOrder();
        ((OrderFragment) ordersPagerAdapter.getItem(0)).newOrderNotificationArea.setVisibility(View.GONE);
    };
}
