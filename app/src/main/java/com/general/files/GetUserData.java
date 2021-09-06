package com.general.files;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import androidx.core.app.ActivityCompat;

import com.roaddo.store.BuildConfig;
import com.utils.Utils;
import com.view.GenerateAlertBox;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Admin on 19-06-2017.
 */

public class GetUserData {

    GeneralFunctions generalFunc;
    Context mContext;

    public GetUserData(GeneralFunctions generalFunc, Context mContext) {
        this.generalFunc = generalFunc;
        this.mContext = mContext;
    }

    public void getData() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getDetail");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("vDeviceType", Utils.deviceType);
        parameters.put("UserType", Utils.app_type);
        parameters.put("AppVersion", BuildConfig.VERSION_NAME);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        exeWebServer.setLoaderConfig(mContext, true, generalFunc);
        exeWebServer.setIsDeviceTokenGenerate(true, "vDeviceToken", generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            JSONObject responseObj=generalFunc.getJsonObject(responseString);
            if (responseObj != null && !responseObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);

                String message = generalFunc.getJsonValueStr(Utils.message_str, responseObj);

                if (message.equals("SESSION_OUT")) {
                    MyApp.getInstance().notifySessionTimeOut();
                    Utils.runGC();
                    return;
                }

                if (isDataAvail == true) {
                    generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValueStr(Utils.message_str, responseObj));
                    new OpenMainProfile(mContext,
                            generalFunc.getJsonValueStr(Utils.message_str, responseObj), true, generalFunc).startProcess();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ActivityCompat.finishAffinity((Activity) mContext);
                                Utils.runGC();
                            } catch (Exception e) {

                            }
                        }
                    }, 300);


                } else {
                    if (!generalFunc.getJsonValueStr("isAppUpdate", responseObj).trim().equals("")
                            && generalFunc.getJsonValueStr("isAppUpdate", responseObj).equals("true")) {

                    } else {

                        if (generalFunc.getJsonValueStr(Utils.message_str, responseObj).equalsIgnoreCase("LBL_CONTACT_US_STATUS_NOTACTIVE_COMPANY") ||
                                generalFunc.getJsonValueStr(Utils.message_str, responseObj).equalsIgnoreCase("LBL_ACC_DELETE_TXT") ||
                                generalFunc.getJsonValueStr(Utils.message_str, responseObj).equalsIgnoreCase("LBL_CONTACT_US_STATUS_NOTACTIVE_DRIVER")) {

                            GenerateAlertBox alertBox = generalFunc.notifyRestartApp("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                            alertBox.setCancelable(false);
                            alertBox.setBtnClickList(btn_id -> {

                                if (btn_id == 1) {
                                    MyApp.getInstance().logOutFromDevice(true);
                                }
                            });
                            return;
                        }

                    }
                }
            }
        });
        exeWebServer.execute();
    }
    public void  GetConfigData()
    {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "generalConfigData");
        parameters.put("UserType", Utils.app_type);
        parameters.put("AppVersion", BuildConfig.VERSION_NAME);
        parameters.put("vLang", generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY));
        parameters.put("vCurrency", generalFunc.retrieveValue(Utils.DEFAULT_CURRENCY_VALUE));

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
        exeWebServer.setDataResponseListener(responseString -> {

            JSONObject responseObj=generalFunc.getJsonObject(responseString);


            if (responseObj != null && !responseObj.equals("")) {
                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);
                if (isDataAvail) {


                    new SetGeneralData(generalFunc,responseObj);


                }
            } else {
                //  showError();
            }

        });
        exeWebServer.execute();
    }
}
