package com.roaddo.store;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;

import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;


import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.InternetConnection;
import com.general.files.MyApp;
import com.general.files.SetUserData;
import com.general.files.StartActProcess;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE;

public class VerifyInfoActivity extends AppCompatActivity {

    RelativeLayout emailView;
    RelativeLayout smsView;
    ProgressBar loading;


    ImageView backImgView;
    GeneralFunctions generalFunc;
    String required_str = "";
    String error_verification_code = "";

    JSONObject userProfileJsonObj;
    MTextView titleTxt;

    MButton okBtn, emailOkBtn, mobContinueBtn, emailContinueBtn;
    MButton emailResendBtn;
    MTextView resendBtn;

    Bundle bundle;
    String reqType = "";
    String vEmail = "", vPhone = "";

    String phoneVerificationCode = "";
    String emailVerificationCode = "";

    MTextView phonetxt;
    MTextView emailTxt;


    boolean isEditInfoTapped = false;
    CountDownTimer countDnTimer;
    CountDownTimer countDnEmailTimer;

    int maxAttemptCount = 0;
    int resendTime = 0;
    //    int resendSecAfter = 30 * 1000;
    int resendSecAfter;
    int maxAllowdCount;
    int resendSecInMilliseconds;
    boolean isProcessRunning = false;
    boolean isEmailSendProcessRunning = false;

    // Edit Email Or Number

    boolean isDialogOpen = false;
    private String error_email_str = "";
    BottomSheetDialog editInfoDialog;
    boolean isCountrySelected = false;
    private String vPhoneCode;
    private String vCountryCode;
    public int MY_PERMISSIONS_REQUEST_SMS = 53;
    String msg = "";
    ImageView logoutImageview;
    String LBL_RESEND_SMS = "", LBL_RESEND_EMAIL = "";
    LinearLayout mobeditArea, mobOtpArea, mobEditArea;
    LinearLayout emailEditArea, emailOtpArea, emaileditArea;
    OtpView mob_otp_view, email_otp_view;
    ImageView emailverifyImg, mobverifyImg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_info);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        LBL_RESEND_SMS = generalFunc.retrieveLangLBl("", "LBL_RESEND_SMS");
        LBL_RESEND_EMAIL = generalFunc.retrieveLangLBl("", "LBL_RESEND_EMAIL");

        bundle = new Bundle();
        bundle = getIntent().getExtras();
        msg = bundle.getString("msg");

        resendSecAfter = generalFunc.parseIntegerValue(30, generalFunc.getJsonValue(Utils.VERIFICATION_CODE_RESEND_TIME_IN_SECONDS_KEY, generalFunc.retrieveValue(Utils.USER_PROFILE_JSON)));
        maxAllowdCount = generalFunc.parseIntegerValue(5, generalFunc.getJsonValue(Utils.VERIFICATION_CODE_RESEND_COUNT_KEY, generalFunc.retrieveValue(Utils.USER_PROFILE_JSON)));
        resendTime = generalFunc.parseIntegerValue(30, generalFunc.getJsonValue(Utils.VERIFICATION_CODE_RESEND_COUNT_RESTRICTION_KEY, generalFunc.retrieveValue(Utils.USER_PROFILE_JSON)));
        resendSecInMilliseconds = resendSecAfter * 1 * 1000;

        emailverifyImg = (ImageView) findViewById(R.id.emailverifyImg);
        mobverifyImg = (ImageView) findViewById(R.id.mobverifyImg);
        mobeditArea = (LinearLayout) findViewById(R.id.mobeditArea);
        mobOtpArea = (LinearLayout) findViewById(R.id.mobOtpArea);
        emailOtpArea = (LinearLayout) findViewById(R.id.emailOtpArea);
        mobEditArea = (LinearLayout) findViewById(R.id.mobEditArea);
        emailEditArea = (LinearLayout) findViewById(R.id.emailEditArea);
        emaileditArea = (LinearLayout) findViewById(R.id.emaileditArea);
        mobeditArea.setOnClickListener(new setOnClickList());
        emaileditArea.setOnClickListener(new setOnClickList());
        phonetxt = ((MTextView) findViewById(R.id.phoneTxt));
        emailTxt = ((MTextView) findViewById(R.id.emailTxt));
//        Drawable emailshow = AppCompatResources.getDrawable(getActContext(), R.drawable.ic_email_verification);
//        emailverifyImg.setImageDrawable(emailshow);

        //Drawable mobshow = AppCompatResources.getDrawable(getActContext(), R.drawable.ic_phone_verify);
        // mobverifyImg.setImageDrawable(mobshow);

        if (!getIntent().hasExtra("MOBILE")) {
            vEmail = generalFunc.getJsonValueStr("vEmail", userProfileJsonObj);
            vPhone = generalFunc.getJsonValueStr("vCode", userProfileJsonObj) + generalFunc.getJsonValueStr("vPhone", userProfileJsonObj);
        } else {
            vPhone = getIntent().getStringExtra("MOBILE");
        }

        mob_otp_view = (OtpView) findViewById(R.id.otp_view);
        mob_otp_view.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                okBtn.performClick();


            }
        });


        email_otp_view = (OtpView) findViewById(R.id.email_otp_view);
        email_otp_view.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                emailOkBtn.performClick();

            }
        });

        if (generalFunc.isRTLmode()) {

            email_otp_view.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
            mob_otp_view.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);


        }


        emailView = (RelativeLayout) findViewById(R.id.emailView);
        smsView = (RelativeLayout) findViewById(R.id.smsView);

        if (msg.equalsIgnoreCase("DO_EMAIL_PHONE_VERIFY")) {

            emailView.setVisibility(View.VISIBLE);
            smsView.setVisibility(View.VISIBLE);
            reqType = "DO_EMAIL_PHONE_VERIFY";
        } else if (msg.equalsIgnoreCase("DO_EMAIL_VERIFY")) {
            emailView.setVisibility(View.VISIBLE);
            smsView.setVisibility(View.GONE);
            reqType = "DO_EMAIL_VERIFY";
        } else if (msg.equalsIgnoreCase("DO_PHONE_VERIFY")) {

            smsView.setVisibility(View.VISIBLE);
            emailView.setVisibility(View.GONE);
            reqType = "DO_PHONE_VERIFY";
        }

        mobContinueBtn = ((MaterialRippleLayout) findViewById(R.id.mobContinueBtn)).getChildView();
        emailContinueBtn = ((MaterialRippleLayout) findViewById(R.id.emailContinueBtn)).getChildView();
        okBtn = ((MaterialRippleLayout) findViewById(R.id.okBtn)).getChildView();
        resendBtn = ((MTextView) findViewById(R.id.resendBtn));

        mobContinueBtn.setId(Utils.generateViewId());
        mobContinueBtn.setOnClickListener(new setOnClickList());
        emailContinueBtn.setId(Utils.generateViewId());
        emailContinueBtn.setOnClickListener(new setOnClickList());


        emailOkBtn = ((MaterialRippleLayout) findViewById(R.id.emailOkBtn)).getChildView();
        emailResendBtn = ((MaterialRippleLayout) findViewById(R.id.emailResendBtn)).getChildView();

        logoutImageview = (ImageView) findViewById(R.id.logoutImageview);
        logoutImageview.setOnClickListener(new setOnClickList());

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        backImgView.setOnClickListener(new setOnClickList());

        if (getIntent().hasExtra("isbackshow") && getIntent().getStringExtra("isbackshow").equalsIgnoreCase("No")) {
            backImgView.setVisibility(View.GONE);
            logoutImageview.setVisibility(View.VISIBLE);
        } else {
            backImgView.setVisibility(View.VISIBLE);
            logoutImageview.setVisibility(View.GONE);

        }

        loading = (ProgressBar) findViewById(R.id.loading);

        okBtn.setId(Utils.generateViewId());
        okBtn.setOnClickListener(new setOnClickList());

        resendBtn.setOnClickListener(new setOnClickList());


        emailOkBtn.setId(Utils.generateViewId());
        emailOkBtn.setOnClickListener(new setOnClickList());

        emailResendBtn.setId(Utils.generateViewId());
        emailResendBtn.setOnClickListener(new setOnClickList());


        setLabels();

//        sendVerificationSMS(null);


//
//        if (msg.equalsIgnoreCase("DO_EMAIL_PHONE_VERIFY")) {
//            //sendVerificationSMS("Both");
//
//            requestReadSmsandReceiveSms("Both");
//
//        } else if (msg.equalsIgnoreCase("DO_EMAIL_VERIFY")) {
//            sendVerificationSMS("Email");
//        } else if (msg.equalsIgnoreCase("DO_PHONE_VERIFY")) {
//            //sendVerificationSMS("Mobile");
//            requestReadSmsandReceiveSms("Mobile");
//        }

        // handleSendSms();


        if (generalFunc.retrieveValue(Utils.SITE_TYPE_KEY).equalsIgnoreCase("Demo")) {
            findViewById(R.id.helpOTPTxtView).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.helpOTPTxtView).setVisibility(View.GONE);
        }
    }

    public void handleSendSms() {
        if (msg.equalsIgnoreCase("DO_EMAIL_PHONE_VERIFY")) {
            sendVerificationSMS("Both");

            // requestReadSmsandReceiveSms("Both");

        } else if (msg.equalsIgnoreCase("DO_EMAIL_VERIFY")) {
            sendVerificationSMS("Email");
        } else if (msg.equalsIgnoreCase("DO_PHONE_VERIFY")) {
            sendVerificationSMS("Mobile");
            //requestReadSmsandReceiveSms("Mobile");
        }
    }

    private void requestReadSmsandReceiveSms(String msg) {
        if (isSMSPermisionGranted()) {
            sendVerificationSMS(msg);
        } else if (SmsReadPermissionCheck()) {
            sendVerificationSMS(msg);
        }
    }


    // Auto Read Sms Permission check

    public boolean SmsReadPermissionCheck() {

        int permissionCheck_receiveSMs = ContextCompat.checkSelfPermission(getActContext(), android.Manifest.permission.RECEIVE_SMS);
        int permissionCheck_readSms = ContextCompat.checkSelfPermission(getActContext(), android.Manifest.permission.READ_SMS);

        if (permissionCheck_receiveSMs != PackageManager.PERMISSION_GRANTED || permissionCheck_readSms != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) getActContext(),
                    new String[]{android.Manifest.permission.RECEIVE_SMS, android.Manifest.permission.READ_SMS
                    },
                    MY_PERMISSIONS_REQUEST_SMS);


            // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            return false;
        }

        return true;
    }


    public boolean isSMSPermisionGranted() {
        int permissionCheck_receiveSMs = ContextCompat.checkSelfPermission(getActContext(), android.Manifest.permission.RECEIVE_SMS);
        int permissionCheck_readSms = ContextCompat.checkSelfPermission(getActContext(), android.Manifest.permission.READ_SMS);

        if (permissionCheck_receiveSMs == PackageManager.PERMISSION_GRANTED && permissionCheck_readSms == PackageManager.PERMISSION_GRANTED) {
            return true;

        } else {
            return false;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        handleSendSms();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    private void setLabels() {

        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ACCOUNT_VERIFY_TXT"));
        ((MTextView) findViewById(R.id.smsTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_MOBILE_VERIFy_TXT"));
        ((MTextView) findViewById(R.id.smsSubTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_MOBILE_NUMBER_HINT_TXT") + " ");
        ((MTextView) findViewById(R.id.emailTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_EMAIL_VERIFy_TXT"));
        ((MTextView) findViewById(R.id.smsHelpTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_SMS_SENT_NOTE"));
        ((MTextView) findViewById(R.id.emailHelpTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_EMAIL_SENT_NOTE"));
        mobContinueBtn.setText(generalFunc.retrieveLangLBl("", "LBL_CONTINUE_BTN"));
        emailContinueBtn.setText(generalFunc.retrieveLangLBl("", "LBL_CONTINUE_BTN"));

        ((MTextView) findViewById(R.id.emailSubTitleTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_EMAIL") + " ");

        ((MTextView) findViewById(R.id.phoneTxt)).setText("+" + vPhone);
        ((MTextView) findViewById(R.id.emailTxt)).setText(vEmail);

        okBtn.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        resendBtn.setText(LBL_RESEND_SMS);


        emailOkBtn.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        emailResendBtn.setText(LBL_RESEND_EMAIL);


        error_verification_code = generalFunc.retrieveLangLBl("", "LBL_VERIFICATION_CODE_INVALID");
        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD");
        error_email_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_EMAIL_ERROR_TXT");
        manageSmsHelpTxt(true);
        manageEmailHelpTxt(true);
    }

    public void manageSmsHelpTxt(Boolean isDefault) {

        if (isDefault) {
            String text1 = generalFunc.retrieveLangLBl("", "LBL_MOB_VERIFICATION_NOTE") + " ";
            String text2 = generalFunc.retrieveLangLBl("", "LBL_OTP_TO_VERIFY");
            SpannableString span1 = new SpannableString(text1);
            span1.setSpan(new AbsoluteSizeSpan(Utils.dpToPx(12, getActContext())), 0, text1.length(), SPAN_INCLUSIVE_INCLUSIVE);

            SpannableString span2 = new SpannableString(text2);
            span2.setSpan(new AbsoluteSizeSpan(Utils.dpToPx(12, getActContext())), 0, text2.length(), SPAN_INCLUSIVE_INCLUSIVE);
            span2.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.appThemeColor_1)), 0, text2.length(), 0);
            CharSequence finalText = TextUtils.concat(span1, "", span2);

            ((MTextView) findViewById(R.id.smsHelpTitleTxt)).setText(finalText);
        } else {
            String text1 = generalFunc.retrieveLangLBl("", "LBL_ENTER _OTP_NOTE") + " ";
            String text2 = "+" + vPhone;
            SpannableString span1 = new SpannableString(text1);
            span1.setSpan(new AbsoluteSizeSpan(Utils.dpToPx(12, getActContext())), 0, text1.length(), SPAN_INCLUSIVE_INCLUSIVE);

            SpannableString span2 = new SpannableString(text2);
            span2.setSpan(new AbsoluteSizeSpan(Utils.dpToPx(12, getActContext())), 0, text2.length(), SPAN_INCLUSIVE_INCLUSIVE);
            span2.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.appThemeColor_1)), 0, text2.length(), 0);
            CharSequence finalText = TextUtils.concat(span1, "", span2);

            ((MTextView) findViewById(R.id.smsHelpTitleTxt)).setText(finalText);

        }

    }

    public void sendVerificationSMS(String showTimerFor) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "sendVerificationSMS");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("MobileNo", vPhone);
        parameters.put("UserType", Utils.app_type);
        parameters.put("REQ_TYPE", reqType);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            loading.setVisibility(View.GONE);
            JSONObject responseObj = generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);

                if (isDataAvail == true) {

                    switch (reqType) {
                        case "DO_EMAIL_PHONE_VERIFY":
                            if (!generalFunc.getJsonValue(Utils.message_str, responseObj).equals("")) {
                                generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("",
                                        generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                            } else {
                                if (!generalFunc.getJsonValueStr(Utils.message_str + "_sms", responseObj).equalsIgnoreCase("LBL_MOBILE_VERIFICATION_FAILED_TXT")) {
                                    phoneVerificationCode = generalFunc.getJsonValueStr(Utils.message_str + "_sms", responseObj);
                                } else {
                                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("",
                                            generalFunc.getJsonValueStr(Utils.message_str + "_sms", responseObj)));
                                }
                                if (!generalFunc.getJsonValueStr(Utils.message_str + "_email", responseObj).equalsIgnoreCase("LBL_EMAIL_VERIFICATION_FAILED_TXT")) {
                                    emailVerificationCode = generalFunc.getJsonValueStr(Utils.message_str + "_email", responseObj);
                                } else {
                                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("",
                                            generalFunc.getJsonValueStr(Utils.message_str + "_email", responseObj)));
                                }
                            }
                            break;
                        case "DO_EMAIL_VERIFY":
                            emailVerificationCode = generalFunc.getJsonValue(Utils.message_str, responseString);
                            emailEditArea.setVisibility(View.GONE);
                            emailOtpArea.setVisibility(View.VISIBLE);
                            manageEmailHelpTxt(false);
                            break;
                        case "DO_PHONE_VERIFY":
                            phoneVerificationCode = generalFunc.getJsonValueStr(Utils.message_str, responseObj);
                            mobOtpArea.setVisibility(View.VISIBLE);
                            mobEditArea.setVisibility(View.GONE);
                            manageSmsHelpTxt(false);
                            break;
                        case "PHONE_VERIFIED":
                            enableOrDisable(true, showTimerFor);
                            removecountDownTimer("Mobile");
                            isProcessRunning = false;

                            verifySuccessMessage(generalFunc.retrieveLangLBl("",
                                    generalFunc.getJsonValueStr(Utils.message_str, responseObj)), true, false);

                            break;
                        case "EMAIL_VERIFIED":

                            enableOrDisable(true, showTimerFor);
                            removecountDownTimer("Email");
                            isEmailSendProcessRunning = false;

                            verifySuccessMessage(generalFunc.retrieveLangLBl("",
                                    generalFunc.getJsonValueStr(Utils.message_str, responseObj)), false, true);
                            break;


                    }
                    String userdetails = generalFunc.getJsonValueStr("userDetails", responseObj);
                    if (!userdetails.equals("") && userdetails != null) {
                        String messageData = generalFunc.getJsonValue(Utils.message_str, userdetails);
                        generalFunc.storeData(Utils.USER_PROFILE_JSON, messageData);
                    }
                    checkVerification(responseObj, isDataAvail, showTimerFor);
                } else {
                    checkVerification(responseObj, isDataAvail, showTimerFor);
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));

                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    private void checkVerification(JSONObject responseString, boolean isDataAvail, String showTimerFor) {
        boolean isEmailFailed = generalFunc.getJsonValue("eEmailFailed", responseString).equals("Yes");

        switch (reqType) {
            case "DO_EMAIL_PHONE_VERIFY":

                boolean isSMSFailed = generalFunc.getJsonValue("eSMSFailed", responseString).equals("Yes");

                if (isEmailFailed && isSMSFailed) {
                    enableOrDisable(true, showTimerFor);
                    removecountDownTimer("Both");
                } else if (isEmailFailed) {
                    enableOrDisable(true, showTimerFor);
                    removecountDownTimer("Email");
                    resendProcess("Mobile");
                } else if (isSMSFailed) {
                    enableOrDisable(true, showTimerFor);
                    removecountDownTimer("Mobile");
                    resendProcess("Email");
                } else if (isDataAvail) {
                    resendProcess(showTimerFor);
                } else if (!isDataAvail) {
                    enableOrDisable(true, showTimerFor);
                    removecountDownTimer("Both");
                }
                break;
            case "DO_EMAIL_VERIFY":
                if (isEmailFailed) {
                    enableOrDisable(true, showTimerFor);
                    removecountDownTimer("Email");
                } else if (isDataAvail) {
                    resendProcess(showTimerFor);
                } else if (!isDataAvail) {
                    enableOrDisable(true, showTimerFor);
                    removecountDownTimer("Email");
                }
                break;
            case "DO_PHONE_VERIFY":
                if (isEmailFailed) {
                    enableOrDisable(true, showTimerFor);
                    removecountDownTimer("Mobile");
                    break;
                } else if (isDataAvail) {
                    resendProcess(showTimerFor);
                } else if (!isDataAvail) {
                    enableOrDisable(true, showTimerFor);
                    removecountDownTimer("Mobile");
                }
        }
    }

    public void verifySuccessMessage(String message, final boolean sms, final boolean email) {
        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
        generateAlert.setCancelable(false);
        generateAlert.setBtnClickList(btn_id -> {
            generateAlert.closeAlertBox();
            if (TextUtils.isEmpty(generalFunc.getMemberId())) {
                if (TextUtils.isEmpty(generalFunc.getMemberId())) {
                    isProcessRunning = false;
                    new StartActProcess(getActContext()).setOkResult();
                    VerifyInfoActivity.super.onBackPressed();
                }
            } else {
                if (sms == true) {
                    smsView.setVisibility(View.GONE);
                    isProcessRunning = false;
                    if (emailView.getVisibility() == View.GONE) {
                        VerifyInfoActivity.super.onBackPressed();
                    }
                } else if (email == true) {
                    emailView.setVisibility(View.GONE);
                    isProcessRunning = false;
                    if (smsView.getVisibility() == View.GONE) {
                        VerifyInfoActivity.super.onBackPressed();
                    }
                }
            }
        });
        generateAlert.setContentMessage("", message);
        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        generateAlert.showAlertBox();
    }

    public void resendProcess(final String showTimerFor) {

        if (!Utils.checkText(showTimerFor)) {
            enableOrDisable(true, showTimerFor);
            removecountDownTimer(showTimerFor);
            return;
        }


        enableOrDisable(false, showTimerFor);

        if (Utils.checkText(showTimerFor)) {
            setTime(generalFunc.parseLongValue(0L, String.valueOf(resendSecInMilliseconds)), showTimerFor);
            removecountDownTimer(showTimerFor);

            if (showTimerFor.equalsIgnoreCase("Email")) {
                showEmailTimer(showTimerFor);
            } else if (showTimerFor.equalsIgnoreCase("Mobile")) {
                showTimer(showTimerFor);
            } else if (showTimerFor.equalsIgnoreCase("Both")) {
                showTimer("Mobile");
                showEmailTimer("Email");
            }


        } else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    enableOrDisable(true, showTimerFor);
                }
            }, resendSecInMilliseconds);
        }
    }

    private void setTime(long milliseconds, String showTimerFor) {
        int minutes = (int) (milliseconds / 1000) / 60;
        int seconds = (int) (milliseconds / 1000) % 60;

        int color = Color.parseColor("#FFFFFF");
        String formattedTxt = String.format("%02d:%02d", minutes, seconds);
        if (showTimerFor.equalsIgnoreCase("Both")) {
            resendBtn.setTextColor(color);
            emailResendBtn.setTextColor(color);

            resendBtn.setText(formattedTxt);
            emailResendBtn.setText(formattedTxt);

        } else if (showTimerFor.equalsIgnoreCase("Email")) {
            emailResendBtn.setTextColor(color);
            emailResendBtn.setText(formattedTxt);
        } else if (showTimerFor.equalsIgnoreCase("Mobile")) {
            resendBtn.setTextColor(color);
            resendBtn.setText(formattedTxt);
        }

    }

    public void showTimer(String showTimerFor) {
        countDnTimer = new CountDownTimer(resendSecInMilliseconds, 1000) {
            @Override
            public void onTick(long milliseconds) {
                isProcessRunning = true;
                setTime(milliseconds, showTimerFor);


            }

            @Override
            public void onFinish() {
                isProcessRunning = false;
                // this function will be called when the timecount is finished


//                resendBtn.setText(LBL_RESEND_SMS);
                /*resendBtn.setTextColor(getResources().getColor(R.color.appThemeColor_TXT_1));
                resendBtn.setClickable(true);*/
                enableOrDisable(true, showTimerFor);
                removecountDownTimer("Mobile");
            }
        }.start();

    }


    public void showEmailTimer(String showTimerFor) {


        countDnEmailTimer = new CountDownTimer(resendSecInMilliseconds, 1000) {
            @Override
            public void onTick(long milliseconds) {
                isEmailSendProcessRunning = true;
                setTime(milliseconds, showTimerFor);


            }

            @Override
            public void onFinish() {
                isEmailSendProcessRunning = false;
                // this function will be called when the timecount is finished

//                resendBtn.setText(LBL_RESEND_SMS);
                /*resendBtn.setTextColor(getResources().getColor(R.color.appThemeColor_TXT_1));
                resendBtn.setClickable(true);*/
                enableOrDisable(true, showTimerFor);
                removecountDownTimer("Email");
            }
        }.start();

    }

    private void removecountDownTimer(String type) {

        if (type.equalsIgnoreCase("Mobile")) {
            if (countDnTimer != null) {
                countDnTimer.cancel();
                countDnTimer = null;
                isProcessRunning = false;
            }
        } else if (type.equalsIgnoreCase("Email")) {
            if (countDnEmailTimer != null) {
                countDnEmailTimer.cancel();
                countDnEmailTimer = null;
                isEmailSendProcessRunning = false;
            }
        } else if (type.equalsIgnoreCase("Both")) {
            if (countDnTimer != null) {
                countDnTimer.cancel();
                countDnTimer = null;
                isProcessRunning = false;
            }

            if (countDnEmailTimer != null) {
                countDnEmailTimer.cancel();
                countDnEmailTimer = null;
                isEmailSendProcessRunning = false;
            }
        }

    }

    public Context getActContext() {
        return VerifyInfoActivity.this;
    }


    private StringBuffer getDigitsFromText(String messageText) {
        StringBuffer sBuffer = new StringBuffer();
        Pattern p = Pattern.compile("[0-9]+.[0-9]*|[0-9]*.[0-9]+|[0-9]+");
        Matcher m = p.matcher(messageText);
        while (m.find()) {
            sBuffer.append(m.group());
        }

        return sBuffer;

    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(VerifyInfoActivity.this);
            if (i == R.id.backImgView) {
                onBackPressed();
                // VerifyInfoActivity.super.onBackPressed();
            } else if (i == okBtn.getId()) {
                String finalCode = Utils.getText(mob_otp_view);
                boolean isCodeEntered = Utils.checkText(finalCode) ?
                        ((phoneVerificationCode.equalsIgnoreCase(finalCode) ||
                                (generalFunc.retrieveValue(Utils.SITE_TYPE_KEY).equalsIgnoreCase("Demo") && finalCode.equalsIgnoreCase("1234"))) ? true
                                : Utils.setErrorFields(mob_otp_view, error_verification_code)) : Utils.setErrorFields(mob_otp_view, required_str);
                if (isCodeEntered) {
                    reqType = "PHONE_VERIFIED";
                    sendVerificationSMS("");
                }
            } else if (i == resendBtn.getId()) {
                reqType = "DO_PHONE_VERIFY";

               /* if (maxAttemptCount>=maxAllowdCount)
                {
                    // show blockage msg
                    generalFunc.showGeneralMessage("","You reached maximum attempt limit.Please try after "+resendTime +"min");
                }
                else
                {*/
                // maxAttemptCount++;
                sendVerificationSMS("Mobile");

                //resendProcess(resendBtn);
                // }

            } else if (i == mobeditArea.getId()) {
                Bundle bn = new Bundle();
                bn.putBoolean("isEdit", true);
                bn.putBoolean("isMobile", true);

                isEditInfoTapped = true;

                openEditDilaog("Mobile");

//                new StartActProcess(getActContext()).startActForResult(MyProfileActivity.class, bn, Utils.MY_PROFILE_REQ_CODE);
            } else if (i == emailOkBtn.getId()) {
                String finalCode = Utils.getText(email_otp_view);
                boolean isEmailCodeEntered = Utils.checkText(finalCode) ?
                        ((emailVerificationCode.equalsIgnoreCase(finalCode) ||
                                (generalFunc.retrieveValue(Utils.SITE_TYPE_KEY).equalsIgnoreCase("Demo") && finalCode.equalsIgnoreCase("1234"))) ? true
                                : Utils.setErrorFields(email_otp_view, error_verification_code)) : Utils.setErrorFields(email_otp_view, required_str);
                if (isEmailCodeEntered) {
                    reqType = "EMAIL_VERIFIED";
                    sendVerificationSMS("");
                }
            } else if (i == emailResendBtn.getId()) {
                reqType = "DO_EMAIL_VERIFY";
//                resendProcess(emailResendBtn);
                sendVerificationSMS("Email");
            } else if (i == emaileditArea.getId()) {
                isEditInfoTapped = true;
                openEditDilaog("Email");
                /*Bundle bn = new Bundle();
                bn.putBoolean("isEdit", true);
                bn.putBoolean("isEmail", true);
                new StartActProcess(getActContext()).startActForResult(MyProfileActivity.class, bn, Utils.MY_PROFILE_REQ_CODE);*/
            } else if (i == logoutImageview.getId()) {
                if (new InternetConnection(getActContext()).isNetworkConnected()) {
                    MyApp.getInstance().logOutFromDevice(false);
                } else {
                    generalFunc.showMessage(findViewById(R.id.mainArea), generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                }


            } else if (i == mobContinueBtn.getId()) {
                handleSendSms();
            } else if (i == emailContinueBtn.getId()) {
                handleSendSms();
            }
        }
    }

    public void openEditDilaog(String type) {
        // Reset Country Selection

        isCountrySelected = false;
        vPhoneCode = "";
        vCountryCode = "";

        editInfoDialog = new BottomSheetDialog(getActContext());
        View contentView = View.inflate(getActContext(), R.layout.design_edit_phn_email_dialog, null);
        if (generalFunc.isRTLmode()) {
            contentView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        editInfoDialog.setContentView(contentView);
        BottomSheetBehavior mBehavior = BottomSheetBehavior.from((View) contentView.getParent());
        mBehavior.setPeekHeight(1500);
        View bottomSheetView = editInfoDialog.getWindow().getDecorView().findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior.from(bottomSheetView).setHideable(false);
        setCancelable(editInfoDialog, false);

        MTextView titleTxt, hintTxt, errorTxt, updateEmailTxt, updateMobileTxt, cancelTxt;
        LinearLayout updateEmailArea, updateMobileArea;
        ImageView iv_img_icon;
        EditText mobileBox, countryBox, emailBox;

        titleTxt = (MTextView) editInfoDialog.findViewById(R.id.titleTxt);
        hintTxt = (MTextView) editInfoDialog.findViewById(R.id.hintTxt);
        errorTxt = (MTextView) editInfoDialog.findViewById(R.id.errorTxt);
        iv_img_icon = (ImageView) editInfoDialog.findViewById(R.id.iv_img_icon);
        updateEmailTxt = (MTextView) editInfoDialog.findViewById(R.id.updateEmailTxt);
        cancelTxt = (MTextView) editInfoDialog.findViewById(R.id.cancelTxt);
        updateMobileTxt = (MTextView) editInfoDialog.findViewById(R.id.updateMobileTxt);
        emailBox = (EditText) editInfoDialog.findViewById(R.id.emailBox);
        mobileBox = (EditText) editInfoDialog.findViewById(R.id.mobileBox);
        countryBox = (EditText) editInfoDialog.findViewById(R.id.countryBox);
        updateEmailArea = (LinearLayout) editInfoDialog.findViewById(R.id.updateEmailArea);
        updateMobileArea = (LinearLayout) editInfoDialog.findViewById(R.id.updateMobileArea);


        String text = type.equalsIgnoreCase("Email") ? generalFunc.retrieveLangLBl("", "LBL_EMAIL_LBL_TXT") : generalFunc.retrieveLangLBl("", "LBL_MOBILE_NUMBER_HEADER_TXT");
        titleTxt.setText(text);

        String hintText = type.equalsIgnoreCase("Email") ? generalFunc.retrieveLangLBl("To update your existing email id, please enter new email id below.", "LBL_EMAIL_EDIT_NOTE") : generalFunc.retrieveLangLBl("To update your existing mobile number, please enter new mobile number below.", "LBL_MOBILE_EDIT_NOTE");
        hintTxt.setText(hintText);

        int icon = type.equalsIgnoreCase("Email") ? R.mipmap.ic_verify_email : R.mipmap.ic_mobile;
        iv_img_icon.setImageResource(icon);


        cancelTxt.setText(generalFunc.retrieveLangLBl("Cancel", "LBL_CANCEL_TXT"));

        updateEmailArea.setVisibility(View.GONE);
        updateMobileArea.setVisibility(View.GONE);
        errorTxt.setVisibility(View.GONE);

        if (type.equalsIgnoreCase("Email")) {
            updateEmailArea.setVisibility(View.VISIBLE);
            updateMobileArea.setVisibility(View.GONE);
        } else if (type.equalsIgnoreCase("Mobile")) {
            updateEmailArea.setVisibility(View.GONE);
            updateMobileArea.setVisibility(View.VISIBLE);
        }

        //set KeyPad

        mobileBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        emailBox.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_CLASS_TEXT);
        mobileBox.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // Set Existing Details
        String vCodeStr = generalFunc.getJsonValueStr("vCode", userProfileJsonObj);
        if (Utils.checkText(vCodeStr)) {
            countryBox.setText("+" + vCodeStr);
            isCountrySelected = true;
            vPhoneCode = vCodeStr;
            vCountryCode = generalFunc.getJsonValueStr("vCountry", userProfileJsonObj);
        }
        Utils.removeInput(countryBox);

        if (generalFunc.retrieveValue("showCountryList").equalsIgnoreCase("Yes")) {

            editInfoDialog.findViewById(R.id.imageView2).setVisibility(View.VISIBLE);

            countryBox.setOnClickListener(v -> new StartActProcess(getActContext()).startActForResult(SelectCountryActivity.class, Utils.SELECT_COUNTRY_REQ_CODE));

            countryBox.setOnTouchListener((v, motionEvent) -> {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP && !countryBox.hasFocus()) {
                    countryBox.performClick();
                }
                return true;
            });
        }


        updateEmailTxt.setOnClickListener(view -> {

            // Hide KeyBoard
            Utils.hideKeyPad(VerifyInfoActivity.this);

            if (type.equalsIgnoreCase("Email")) {

                boolean emailEntered = Utils.checkText(emailBox) ?
                        (generalFunc.isEmailValid(Utils.getText(emailBox)) ? true : false)
                        : false;


                if (!emailEntered && !Utils.checkText(emailBox)) {
                    errorTxt.setText(Utils.checkText(required_str) ? StringUtils.capitalize(required_str.toLowerCase().trim()) : required_str);
                } else if (!emailEntered && Utils.checkText(emailBox) && !generalFunc.isEmailValid(Utils.getText(emailBox))) {
                    errorTxt.setText(Utils.checkText(error_email_str) ? StringUtils.capitalize(error_email_str.toLowerCase().trim()) : error_email_str);
                }

                /*
                boolean emailEntered = Utils.checkText(emailBox) ?
                        (generalFunc.isEmailValid(Utils.getText(emailBox)) ? true : Utils.setErrorFields(emailBox, error_email_str))
                        : Utils.setErrorFields(emailBox, required_str);
*/

                if (emailEntered == false) {

                    errorTxt.setVisibility(View.VISIBLE);
                    return;
                }
                errorTxt.setVisibility(View.GONE);

                if (Utils.getText(emailBox).trim().equalsIgnoreCase(generalFunc.getJsonValueStr("vEmail", userProfileJsonObj).trim())) {
                    editInfoDialog.dismiss();
                    return;
                }
                updateProfile(type, Utils.getText(emailBox), "", vCountryCode, vPhoneCode);

            }
        });


        updateMobileTxt.setOnClickListener(view -> {

            // Hide KeyBoard
            Utils.hideKeyPad(VerifyInfoActivity.this);

            boolean mobileEntered = Utils.checkText(mobileBox) ? true : false;
            boolean countryEntered = isCountrySelected ? true : false;

//                if (mobileEntered) {
//                    mobileEntered = mobileBox.length() >= 3 ? true : Utils.setErrorFields(mobileBox, generalFunc.retrieveLangLBl("", "LBL_INVALID_MOBILE_NO"));
//                }
//

            if (!mobileEntered || countryEntered) {

                errorTxt.setText(Utils.checkText(required_str) ? StringUtils.capitalize(required_str.toLowerCase().trim()).toLowerCase() : required_str);
            } else if (mobileEntered && (mobileBox.length() < 3)) {
                errorTxt.setText(Utils.checkText(generalFunc.retrieveLangLBl("", "LBL_INVALID_MOBILE_NO")) ? StringUtils.capitalize(generalFunc.retrieveLangLBl("", "LBL_INVALID_MOBILE_NO").toLowerCase().trim()).toLowerCase() : generalFunc.retrieveLangLBl("", "LBL_INVALID_MOBILE_NO"));
            }

            if (mobileEntered == false || countryEntered == false) {
                errorTxt.setVisibility(View.VISIBLE);
                return;
            }

            errorTxt.setVisibility(View.GONE);

            String currentMobileNum = generalFunc.getJsonValueStr("vPhone", userProfileJsonObj);
            String currentPhoneCode = generalFunc.getJsonValueStr("vCode", userProfileJsonObj);

            if (!currentPhoneCode.equals(vPhoneCode) || !currentMobileNum.equals(Utils.getText(mobileBox))) {
                updateProfile(type, "", Utils.getText(mobileBox), vCountryCode, vPhoneCode);
                return;
            }

            editInfoDialog.dismiss();


        });

        cancelTxt.setOnClickListener(view -> editInfoDialog.dismiss());

        editInfoDialog.setOnDismissListener(dialogInterface -> isDialogOpen = false);
        isDialogOpen = true;
        editInfoDialog.show();

    }


    public void setCancelable(Dialog dialogview, boolean cancelable) {
        final Dialog dialog = dialogview;
        View touchOutsideView = dialog.getWindow().getDecorView().findViewById(R.id.touch_outside);
        View bottomSheetView = dialog.getWindow().getDecorView().findViewById(R.id.design_bottom_sheet);

        if (cancelable) {
            touchOutsideView.setOnClickListener(v -> {
                if (dialog.isShowing()) {
                    dialog.cancel();
                }
            });
            BottomSheetBehavior.from(bottomSheetView).setHideable(true);
        } else {
            touchOutsideView.setOnClickListener(null);
            BottomSheetBehavior.from(bottomSheetView).setHideable(false);
        }
    }


    public void updateProfile(String type, String email, String mobile, String countryCode, String vPhoneCode) {
        boolean isMobile = type.equalsIgnoreCase("Mobile");

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "updateUserProfileDetail");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("vName", generalFunc.getJsonValueStr("vCompany", userProfileJsonObj));
        parameters.put("vLastName", generalFunc.getJsonValueStr("vLastName", userProfileJsonObj));
        parameters.put("vPhone", isMobile ? mobile : generalFunc.getJsonValueStr("vPhone", userProfileJsonObj));
        parameters.put("vPhoneCode", isMobile ? vPhoneCode : generalFunc.getJsonValueStr("vCode", userProfileJsonObj));
        parameters.put("vCountry", isMobile ? countryCode : generalFunc.getJsonValueStr("vCountry", userProfileJsonObj));
        parameters.put("vEmail", type.equalsIgnoreCase("Email") ? email : generalFunc.getJsonValueStr("vEmail", userProfileJsonObj));
        parameters.put("CurrencyCode", generalFunc.getJsonValueStr("vCurrencyCompany", userProfileJsonObj));
        parameters.put("LanguageCode", generalFunc.getJsonValueStr("vLang", userProfileJsonObj));
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseObj = generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);

                if (isDataAvail) {

                    String currentLangCode = generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY);

                    String messgeJson = generalFunc.getJsonValueStr(Utils.message_str, responseObj);
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, messgeJson);
                    responseString = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);


                    new SetUserData(responseString, generalFunc, getActContext(), false);
                    userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));


                    vEmail = generalFunc.getJsonValueStr("vEmail", userProfileJsonObj);
                    vPhone = generalFunc.getJsonValueStr("vCode", userProfileJsonObj) + generalFunc.getJsonValueStr("vPhone", userProfileJsonObj);


                    ((MTextView) findViewById(R.id.phoneTxt)).setText("+" + vPhone);
                    ((MTextView) findViewById(R.id.emailTxt)).setText(vEmail);


                    String ePhoneVerified = generalFunc.getJsonValueStr("ePhoneVerified", userProfileJsonObj);
                    String eEmailVerified = generalFunc.getJsonValueStr("eEmailVerified", userProfileJsonObj);


                    enableOrDisable(true, type);
                    removecountDownTimer(type);

                    if (type.equalsIgnoreCase("Mobile") && !ePhoneVerified.equalsIgnoreCase("Yes")) {
                        reqType = "DO_PHONE_VERIFY";
                        phoneVerificationCode = "";
                    } else if (type.equalsIgnoreCase("Email") && !eEmailVerified.equalsIgnoreCase("Yes")) {
                        reqType = "DO_EMAIL_VERIFY";
                        emailVerificationCode = "";
                    }

                    editInfoDialog.dismiss();
                    sendVerificationSMS(type);


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

    public void enableOrDisable(boolean activate, String showTimerFor) {

        if (Utils.checkText(showTimerFor) && showTimerFor.equalsIgnoreCase("Both")) {
            setButtonEnabled(mobContinueBtn, activate);
            setButtonEnabled(emailContinueBtn, activate);
        } else if (Utils.checkText(showTimerFor) && showTimerFor.equalsIgnoreCase("Email")) {
            setButtonEnabled(emailContinueBtn, activate);
        } else if (Utils.checkText(showTimerFor) && showTimerFor.equalsIgnoreCase("Mobile")) {
            setButtonEnabled(mobContinueBtn, activate);
        } else if (!Utils.checkText(showTimerFor)) {
            setButtonEnabled(mobContinueBtn, activate);
            setButtonEnabled(emailContinueBtn, activate);
        }

        if (activate && Utils.checkText(showTimerFor)) {
            if (Utils.checkText(showTimerFor) && showTimerFor.equalsIgnoreCase("Both")) {
                mobContinueBtn.setText(LBL_RESEND_SMS);
                emailContinueBtn.setText(LBL_RESEND_EMAIL);
            } else if (Utils.checkText(showTimerFor) && showTimerFor.equalsIgnoreCase("Email")) {
                emailContinueBtn.setText(LBL_RESEND_EMAIL);
            } else if (Utils.checkText(showTimerFor) && showTimerFor.equalsIgnoreCase("Mobile")) {
                mobContinueBtn.setText(LBL_RESEND_SMS);

            } else if (!Utils.checkText(showTimerFor)) {
                mobContinueBtn.setText(LBL_RESEND_SMS);
                emailContinueBtn.setText(LBL_RESEND_EMAIL);
            }

        }
    }

    private void setButtonEnabled(MButton btn, boolean setEnable) {
        btn.setFocusableInTouchMode(setEnable);
        btn.setFocusable(setEnable);
        btn.setEnabled(setEnable);
        btn.setOnClickListener(setEnable ? new setOnClickList() : null);
        btn.setTextColor(setEnable ? Color.parseColor("#FFFFFF") : Color.parseColor("#BABABA"));
        btn.setClickable(setEnable);
    }

    @Override
    public void onBackPressed() {

        if (mobOtpArea.getVisibility() == View.VISIBLE) {
            mobOtpArea.setVisibility(View.GONE);
            mobEditArea.setVisibility(View.VISIBLE);
            manageSmsHelpTxt(true);
            return;
        }

        if (emailOtpArea.getVisibility() == View.VISIBLE) {
            emailOtpArea.setVisibility(View.GONE);
            emailEditArea.setVisibility(View.VISIBLE);
            manageEmailHelpTxt(true);
            return;
        }


        if (backImgView.getVisibility() == View.VISIBLE) {
            removecountDownTimer("Both");
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        removecountDownTimer("Both");
        super.onDestroy();
    }


    public void manageEmailHelpTxt(Boolean isDefault) {

        if (isDefault) {
            String text1 = generalFunc.retrieveLangLBl("", "LBL_EMAIL_VERIFICATION_NOTE") + " ";
            String text2 = generalFunc.retrieveLangLBl("", "LBL_OTP_TO_VERIFY");
            SpannableString span1 = new SpannableString(text1);
            span1.setSpan(new AbsoluteSizeSpan(Utils.dpToPx(12, getActContext())), 0, text1.length(), SPAN_INCLUSIVE_INCLUSIVE);

            SpannableString span2 = new SpannableString(text2);
            span2.setSpan(new AbsoluteSizeSpan(Utils.dpToPx(12, getActContext())), 0, text2.length(), SPAN_INCLUSIVE_INCLUSIVE);
            span2.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.appThemeColor_1)), 0, text2.length(), 0);
            CharSequence finalText = TextUtils.concat(span1, "", span2);

            ((MTextView) findViewById(R.id.emailHelpTitleTxt)).setText(finalText);
        } else {
            String text1 = generalFunc.retrieveLangLBl("", "LBL_ENTER _OTP_NOTE") + " ";
            String text2 = vEmail;
            SpannableString span1 = new SpannableString(text1);
            span1.setSpan(new AbsoluteSizeSpan(Utils.dpToPx(12, getActContext())), 0, text1.length(), SPAN_INCLUSIVE_INCLUSIVE);

            SpannableString span2 = new SpannableString(text2);
            span2.setSpan(new AbsoluteSizeSpan(Utils.dpToPx(12, getActContext())), 0, text2.length(), SPAN_INCLUSIVE_INCLUSIVE);
            span2.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.appThemeColor_1)), 0, text2.length(), 0);
            CharSequence finalText = TextUtils.concat(span1, "", span2);

            ((MTextView) findViewById(R.id.emailHelpTitleTxt)).setText(finalText);

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utils.SELECT_COUNTRY_REQ_CODE && resultCode == Activity.RESULT_OK && data != null) {
            if (editInfoDialog != null) {
                vCountryCode = data.getStringExtra("vCountryCode");
                vPhoneCode = data.getStringExtra("vPhoneCode");
                isCountrySelected = true;
                ((EditText) editInfoDialog.findViewById(R.id.countryBox)).setText("+" + vPhoneCode);
            }
        }
    }
}