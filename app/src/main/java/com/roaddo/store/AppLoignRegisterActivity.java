package com.roaddo.store;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.facebook.FacebookSdk;
import com.fragments.SignInFragment;
import com.fragments.SignUpFragment;
import com.general.files.GeneralFunctions;
import com.general.files.GetDeviceToken;
import com.general.files.InternetConnection;
import com.general.files.MyApp;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.utils.Utils;
import com.view.MTextView;
import com.facebook.WebDialog;


public class AppLoignRegisterActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 001;
    static String TWITTER_CONSUMER_KEY = ""; // place your cosumer key here
    static String TWITTER_CONSUMER_SECRET = ""; // place your consumer secret here
    public MTextView titleTxt;
    public GeneralFunctions generalFunc;
    public MTextView signheaderHint, orTxt;
    ImageView backImgView;
    View container;
    String type = "";
    ImageView imagefacebook, imagetwitter, imageGoogle;
    SignUpFragment signUpFragment;
    LinearLayout socialarea;
    private InternetConnection intCheck;
    public   boolean isSignInFirst=false;
    public   boolean isSignUpFirst=false;
    boolean isBack = false;

    private final static String LOGIN_TAG_FRAGMENT = "LOGIN_TAG_FRAGMENT";
    private final static String SIGNUP_TAG_FRAGMENT = "SIGNUP_TAG_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //  FacebookSdk.sdkInitialize(getActContext());
            //    FacebookSdk.setWebDialogTheme(R.style.FBDialogtheme);
            WebDialog.setWebDialogTheme(R.style.FBDialogtheme);
            setContentView(R.layout.activity_app_loign_register);
            type = getIntent().getStringExtra("type");
            intCheck = new InternetConnection(this);


            initview();
            setLabel();
        } catch (Exception e) {

        }

    }


    private void initview() {



        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        FacebookSdk.setApplicationId(generalFunc.retrieveValue(Utils.FACEBOOK_APPID_KEY));
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        backImgView.setOnClickListener(new setOnClickList());
        container = findViewById(R.id.container);
        socialarea = (LinearLayout) findViewById(R.id.socialarea);

        signheaderHint = (MTextView) findViewById(R.id.signheaderHint);
        orTxt = (MTextView) findViewById(R.id.orTxt);


        imagefacebook = (ImageView) findViewById(R.id.imagefacebook);
        imagetwitter = (ImageView) findViewById(R.id.imagetwitter);
        imageGoogle = (ImageView) findViewById(R.id.imageGoogle);

        imagefacebook.setOnClickListener(new setOnClickList());
        imagetwitter.setOnClickListener(new setOnClickList());
        imageGoogle.setOnClickListener(new setOnClickList());

        imagefacebook.setVisibility(View.GONE);
        imageGoogle.setVisibility(View.GONE);
        imagetwitter.setVisibility(View.GONE);
        socialarea.setVisibility(View.GONE);
    }

    public Context getActContext() {
        return AppLoignRegisterActivity.this;
    }


    private void setLabel() {

        if (type != null) {
            if (type.equals("login")) {
                titleTxt.setText(generalFunc.retrieveLangLBl("Login", "LBL_SIGN_IN_TXT"));
                signheaderHint.setText(generalFunc.retrieveLangLBl("Log in with social account", "LBL_SIGN_IN_WITH_SOC_ACC"));

                handleFragment(new SignInFragment(), false, true);
                isSignInFirst=true;
            } else {
                titleTxt.setText(generalFunc.retrieveLangLBl("Register", "LBL_SIGN_UP"));
                signheaderHint.setText(generalFunc.retrieveLangLBl("Register with social account", "LBL_SIGN_UP_WITH_SOC_ACC"));
                handleFragment(new SignUpFragment(),false, false);
                isSignUpFirst = true;
            }
        }

        orTxt.setText(generalFunc.retrieveLangLBl("OR", "LBL_OR_TXT"));


    }

    public void handleFragment(Fragment fragment, boolean isback, boolean isLogin) {
        this.isBack = isback;
        String backStateName = fragment.getClass().getName();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment, isLogin ? LOGIN_TAG_FRAGMENT : SIGNUP_TAG_FRAGMENT);
        if (isback) {
            transaction.addToBackStack(backStateName);
        } else {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
        } else if (requestCode == Utils.SELECT_COUNTRY_REQ_CODE) {
            SignUpFragment.setdata(requestCode, resultCode, data);
        } else if (requestCode == 140) {
        } else {
        }
    }

    @Override
    public void onBackPressed() {
        if (isBack) {
            getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            super.onBackPressed();
            isBack = false;
            return;
        }
        finish();
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {

            int i = view.getId();
            Utils.hideKeyboard(AppLoignRegisterActivity.this);
            if (i == backImgView.getId()) {
                finish();
            }
        }
    }


    public void manageSinchClient() {
        if (getSinchServiceInterface()!=null && !getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(Utils.userType + "_" + generalFunc.getMemberId());

            GetDeviceToken getDeviceToken = new GetDeviceToken(generalFunc);

            getDeviceToken.setDataResponseListener(vDeviceToken -> {

                if (!vDeviceToken.equals("")) {
                    try {
                        getSinchServiceInterface().getSinchClient().registerPushNotificationData(vDeviceToken.getBytes());

                    } catch (Exception e) {

                    }
                }


            });
            getDeviceToken.execute();
        }
    }
}
