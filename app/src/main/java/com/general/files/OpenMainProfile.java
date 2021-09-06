package com.general.files;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

import com.roaddo.store.AccountverificationActivity;
import com.roaddo.store.MainActivity;
import com.roaddo.store.SuspendedDriver_Activity;
import com.utils.AnimateMarker;
import com.utils.Utils;

import org.json.JSONObject;

/**
 * Created by Admin on 29-06-2016.
 */
public class OpenMainProfile {
    private final JSONObject userProfileJsonObj;
    Context mContext;
    String responseString;
    boolean isCloseOnError;
    GeneralFunctions generalFun;
    boolean isnotification = false;
    AnimateMarker animateMarker;

    public OpenMainProfile(Context mContext, String responseString, boolean isCloseOnError, GeneralFunctions generalFun) {
        this.mContext = mContext;
        //this.responseString = responseString;
        this.isCloseOnError = isCloseOnError;
        this.generalFun = generalFun;

        this.responseString = generalFun.retrieveValue(Utils.USER_PROFILE_JSON);

        userProfileJsonObj = generalFun.getJsonObject(this.responseString);
        animateMarker = new AnimateMarker();

    }

    public OpenMainProfile(Context mContext, String responseString, boolean isCloseOnError, GeneralFunctions generalFun, boolean isnotification) {
        this.mContext = mContext;
        //this.responseString = responseString;
        this.isCloseOnError = isCloseOnError;
        this.generalFun = generalFun;
        this.isnotification = isnotification;

        this.responseString = generalFun.retrieveValue(Utils.USER_PROFILE_JSON);

        userProfileJsonObj = generalFun.getJsonObject(this.responseString);
        animateMarker = new AnimateMarker();
//        HashMap<String,String> storeData=new HashMap<>();
//        storeData.put(Utils.DefaultCountry, generalFun.getJsonValueStr("vDefaultCountry", userProfileJsonObj));
//        storeData.put(Utils.DefaultCountryCode, generalFun.getJsonValueStr("vDefaultCountryCode", userProfileJsonObj));
//        storeData.put(Utils.DefaultPhoneCode, generalFun.getJsonValueStr("vDefaultPhoneCode", userProfileJsonObj));
//        storeData.put(Utils.DefaultCountryImage, generalFun.getJsonValueStr("vDefaultCountryImage", userProfileJsonObj));
//        generalFun.storeData(storeData);

    }

    public void startProcess() {
        generalFun.sendHeartBeat();

        // responseString = generalFun.retrieveValue(Utils.USER_PROFILE_JSON);
        //setGeneralData();
        new SetGeneralData(generalFun, userProfileJsonObj);


        animateMarker.driverMarkerAnimFinished = true;

        Bundle bn = new Bundle();
        bn.putString("USER_PROFILE_JSON", responseString);
        bn.putString("IsAppReStart", "true"); // flag for retrieving data to en route trip pages

        String vTripStatus = generalFun.getJsonValueStr("vTripStatus", userProfileJsonObj);

        boolean lastTripExist = false;

        if (vTripStatus.contains("Not Active")) {


            String ratings_From_Driver_str = generalFun.getJsonValueStr("Ratings_From_Driver", userProfileJsonObj);

            if (!ratings_From_Driver_str.equals("Done")) {
                lastTripExist = true;
            }
        }
        if (generalFun.getJsonValue("vPhone", userProfileJsonObj).equals("") || generalFun.getJsonValue("vEmail", userProfileJsonObj).equals("")) {
            if (generalFun.getMemberId() != null && !generalFun.getMemberId().equals("")) {
                new StartActProcess(mContext).startActWithData(AccountverificationActivity.class, bn);
            }
        } else {

            String eStatus = generalFun.getJsonValueStr("eStatus", userProfileJsonObj);

            if (eStatus.equalsIgnoreCase("suspend")) {
                new StartActProcess(mContext).startAct(SuspendedDriver_Activity.class);
            } else {
                new StartActProcess(mContext).startActWithData(MainActivity.class, bn);

            }
        }


        ActivityCompat.finishAffinity((Activity) mContext);
    }

    public void setGeneralData() {
        //HashMap<String,String> storeData=new HashMap<>();
        //  Utils.SKIP_MOCK_LOCATION_CHECK = generalFun.getJsonValueStr("eAllowFakeGPS", userProfileJsonObj).equalsIgnoreCase("Yes");

        //  storeData.put(Utils.SESSION_ID_KEY, generalFun.getJsonValueStr("tSessionId", userProfileJsonObj));
        //  storeData.put(Utils.DEVICE_SESSION_ID_KEY, generalFun.getJsonValueStr("tDeviceSessionId", userProfileJsonObj));

        //   storeData.put(Utils.iServiceId_KEY, generalFun.getJsonValueStr("iServiceId", userProfileJsonObj));

        // storeData.put(Utils.FETCH_TRIP_STATUS_TIME_INTERVAL_KEY, generalFun.getJsonValueStr("FETCH_TRIP_STATUS_TIME_INTERVAL", userProfileJsonObj));


        // storeData.put(Utils.VERIFICATION_CODE_RESEND_TIME_IN_SECONDS_KEY, generalFun.getJsonValueStr(Utils.VERIFICATION_CODE_RESEND_TIME_IN_SECONDS_KEY, userProfileJsonObj));
        // storeData.put(Utils.VERIFICATION_CODE_RESEND_COUNT_KEY, generalFun.getJsonValueStr(Utils.VERIFICATION_CODE_RESEND_COUNT_KEY, userProfileJsonObj));
        // storeData.put(Utils.VERIFICATION_CODE_RESEND_COUNT_RESTRICTION_KEY, generalFun.getJsonValueStr(Utils.VERIFICATION_CODE_RESEND_COUNT_RESTRICTION_KEY, userProfileJsonObj));

        // storeData.put(Utils.PUBNUB_PUB_KEY, generalFun.getJsonValueStr("PUBNUB_PUBLISH_KEY", userProfileJsonObj));
        //storeData.put(Utils.PUBNUB_SUB_KEY, generalFun.getJsonValueStr("PUBNUB_SUBSCRIBE_KEY", userProfileJsonObj));
        // storeData.put(Utils.PUBNUB_SEC_KEY, generalFun.getJsonValueStr("PUBNUB_SECRET_KEY", userProfileJsonObj));
        // storeData.put(Utils.SITE_TYPE_KEY, generalFun.getJsonValueStr("SITE_TYPE", userProfileJsonObj));
        // storeData.put("showCountryList", generalFun.getJsonValueStr("showCountryList", userProfileJsonObj));

        // storeData.put(Utils.MOBILE_VERIFICATION_ENABLE_KEY, generalFun.getJsonValueStr("MOBILE_VERIFICATION_ENABLE", userProfileJsonObj));
        // storeData.put("LOCATION_ACCURACY_METERS", generalFun.getJsonValueStr("LOCATION_ACCURACY_METERS", userProfileJsonObj));
        //   storeData.put("DRIVER_LOC_UPDATE_TIME_INTERVAL", generalFun.getJsonValueStr("DRIVER_LOC_UPDATE_TIME_INTERVAL", userProfileJsonObj));
        // storeData.put("RIDER_REQUEST_ACCEPT_TIME", generalFun.getJsonValueStr("RIDER_REQUEST_ACCEPT_TIME", userProfileJsonObj));
        //  storeData.put(Utils.PHOTO_UPLOAD_SERVICE_ENABLE_KEY, generalFun.getJsonValueStr(Utils.PHOTO_UPLOAD_SERVICE_ENABLE_KEY, userProfileJsonObj));

//        storeData.put(Utils.ENABLE_TOLL_COST, generalFun.getJsonValueStr("ENABLE_TOLL_COST", userProfileJsonObj));
//        storeData.put(Utils.TOLL_COST_APP_ID, generalFun.getJsonValueStr("TOLL_COST_APP_ID", userProfileJsonObj));
//        storeData.put(Utils.TOLL_COST_APP_CODE, generalFun.getJsonValueStr("TOLL_COST_APP_CODE", userProfileJsonObj));

        // storeData.put(Utils.PUBSUB_TECHNIQUE, generalFun.getJsonValueStr("PUBSUB_TECHNIQUE", userProfileJsonObj));
        //storeData.put(Utils.WALLET_ENABLE, generalFun.getJsonValueStr("WALLET_ENABLE", userProfileJsonObj));
        // storeData.put(Utils.REFERRAL_SCHEME_ENABLE, generalFun.getJsonValueStr("REFERRAL_SCHEME_ENABLE", userProfileJsonObj));

        // storeData.put(Utils.APP_DESTINATION_MODE, generalFun.getJsonValueStr("APP_DESTINATION_MODE", userProfileJsonObj));
        // storeData.put(Utils.APP_TYPE, generalFun.getJsonValueStr("APP_TYPE", userProfileJsonObj));
        // storeData.put(Utils.HANDICAP_ACCESSIBILITY_OPTION, generalFun.getJsonValueStr("HANDICAP_ACCESSIBILITY_OPTION", userProfileJsonObj));
        // storeData.put(Utils.FEMALE_RIDE_REQ_ENABLE, generalFun.getJsonValueStr("FEMALE_RIDE_REQ_ENABLE", userProfileJsonObj));

        // storeData.put(Utils.PUBNUB_DISABLED_KEY, generalFun.getJsonValueStr("PUBNUB_DISABLED", userProfileJsonObj));
        // storeData.put(Utils.ENABLE_SOCKET_CLUSTER_KEY, generalFun.getJsonValueStr("ENABLE_SOCKET_CLUSTER", userProfileJsonObj));
        // storeData.put(Utils.SC_CONNECT_URL_KEY, generalFun.getJsonValueStr("SC_CONNECT_URL", userProfileJsonObj));
        // storeData.put(Utils.GOOGLE_SERVER_ANDROID_COMPANY_APP_KEY, generalFun.getJsonValueStr(Utils.GOOGLE_SERVER_ANDROID_COMPANY_APP_KEY, userProfileJsonObj));

//        storeData.put(Utils.SINCH_APP_KEY, generalFun.getJsonValueStr(Utils.SINCH_APP_KEY, userProfileJsonObj));
//        storeData.put(Utils.SINCH_APP_SECRET_KEY, generalFun.getJsonValueStr(Utils.SINCH_APP_SECRET_KEY, userProfileJsonObj));
//        storeData.put(Utils.SINCH_APP_ENVIRONMENT_HOST, generalFun.getJsonValueStr(Utils.SINCH_APP_ENVIRONMENT_HOST, userProfileJsonObj));

        /*Thermal Print*/
//        storeData.put(Utils.THERMAL_PRINT_ENABLE_KEY, generalFun.getJsonValueStr(Utils.THERMAL_PRINT_ENABLE_KEY, userProfileJsonObj));
//        storeData.put(Utils.AUTO_PRINT_KEY, generalFun.getJsonValueStr(Utils.AUTO_PRINT_KEY, userProfileJsonObj));
//        storeData.put(Utils.THERMAL_PRINT_ALLOWED_KEY, generalFun.getJsonValueStr(Utils.THERMAL_PRINT_ALLOWED_KEY, userProfileJsonObj));
//        storeData.put(Utils.KOT_BILL_FORMAT_KEY, generalFun.getJsonValueStr(Utils.KOT_BILL_FORMAT_KEY, userProfileJsonObj));
//        storeData.put(Utils.SITE_NAME_KEY, generalFun.getJsonValueStr(Utils.SITE_NAME_KEY, userProfileJsonObj));
//
//
//        generalFun.storeData(storeData);

    }
}
