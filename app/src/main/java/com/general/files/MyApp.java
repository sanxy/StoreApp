package com.general.files;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothSocket;

import com.roaddo.store.BuildConfig;
import com.general.files.thermalPrint.GenerateOrderBill;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.multidex.MultiDex;
import androidx.appcompat.app.AppCompatDelegate;
import android.view.WindowManager;


import com.roaddo.store.LauncherActivity;
import com.roaddo.store.MainActivity;
import com.roaddo.store.NetworkChangeReceiver;
import com.roaddo.store.R;
import com.roaddo.store.TrackOrderActivity;
import com.facebook.appevents.AppEventsLogger;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.utils.WeViewFontConfig;
import com.view.GenerateAlertBox;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;



/**
 * Created by Admin on 28-06-2016.
 */
public class MyApp extends Application {
    protected static MyApp mMyApp;
    GeneralFunctions generalFun;
    Logger mLog;
    boolean isAppInBackground = true;

    private Activity currentAct = null;

    public MainActivity mainAct;
    public TrackOrderActivity trackOrderAct;
    private NetworkChangeReceiver mNetWorkReceiver = null;

    GenerateAlertBox generateSessionAlert;

    /*Thermal Print*/
    public static GenerateOrderBill generateOrderBill;
    public static BluetoothSocket btsocket;
    public static OutputStream outputStream;

    private  static ArrayList<String> requestPermissions = new ArrayList<>();

    public boolean isThermalPrintAllowed(boolean checkAutoPrint) {
        boolean isAllowed=false;
        HashMap<String,String> data=new HashMap<>();
        data.put(Utils.THERMAL_PRINT_ENABLE_KEY,"");
        data.put(Utils.THERMAL_PRINT_ALLOWED_KEY,"");
        if (checkAutoPrint)
        {
            data.put(Utils.AUTO_PRINT_KEY,"");

        }
        data=new GeneralFunctions(getCurrentAct()).retrieveValue(data);

        boolean printAllowed=data.get(Utils.THERMAL_PRINT_ENABLE_KEY).equalsIgnoreCase("Yes") ;//&& data.get(Utils.THERMAL_PRINT_ALLOWED_KEY).equalsIgnoreCase("Yes")
        boolean checkSettings=checkAutoPrint?printAllowed&&data.get(Utils.AUTO_PRINT_KEY).equalsIgnoreCase("Yes"):printAllowed;
        if (checkSettings) {
            isAllowed=true;
        }

        return isAllowed;

    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.SERVER_CONNECTION_URL = CommonUtilities.SERVER_URL;
        GeneralFunctions generalFunctions=new GeneralFunctions(this);
        HashMap<String,String> storeData=new HashMap<>();
        storeData.put("SERVERURL", CommonUtilities.SERVER_URL);
        storeData.put("SERVERWEBSERVICEPATH", CommonUtilities.SERVER_WEBSERVICE_PATH);
        storeData.put("USERTYPE", BuildConfig.USER_TYPE);
        GeneralFunctions.storeData(storeData,this);


        WeViewFontConfig.ASSETS_FONT_NAME = getResources().getString(R.string.systemRegular);
        WeViewFontConfig.FONT_FAMILY_NAME = getResources().getString(R.string.systemRegular_name);
        WeViewFontConfig.FONT_COLOR = "#343434";
        WeViewFontConfig.FONT_SIZE = "14px";

        try {
            Picasso.Builder builder = new Picasso.Builder(this);
            builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
            Picasso built = builder.build();
            built.setIndicatorsEnabled(false);
            built.setLoggingEnabled(false);
            Picasso.setSingletonInstance(built);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        Utils.IS_APP_IN_DEBUG_MODE = BuildConfig.DEBUG ? "Yes" : "No";
        Utils.userType = BuildConfig.USER_TYPE;
        Utils.app_type = BuildConfig.USER_TYPE;
        Utils.USER_ID_KEY = BuildConfig.USER_ID_KEY;

        setScreenOrientation();

        mMyApp = (MyApp) this.getApplicationContext();
        generalFun = new GeneralFunctions(this);

        try {
            AppEventsLogger.activateApp(this);
        } catch (Exception e) {
            Logger.d("FBError", "::" + e.toString());
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);


    }

    public GeneralFunctions getGeneralFun(Context mContext) {
        return new GeneralFunctions(mContext, R.id.backImgView);
    }

    public GeneralFunctions getAppLevelGeneralFunc() {
        if (generalFun == null) {
            generalFun = new GeneralFunctions(this);
        }
        return generalFun;
    }


    public void handleUncaughtException(Thread thread, Throwable e) {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically
        try {
            extractLogToFile();

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


    public static synchronized MyApp getInstance() {
        return mMyApp;
    }


    public boolean isMyAppInBackGround() {
        return this.isAppInBackground;
    }

    public void refreshWithConfigData()
    {
        GetUserData objRefresh = new GetUserData(generalFun, MyApp.getInstance().getCurrentAct());
        objRefresh.GetConfigData();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();

        Logger.d("Api", "Object Destroyed >> MYAPP onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        Logger.d("Api", "Object Destroyed >> MYAPP onTrimMemory");
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
        Logger.d("Api", "Object Destroyed >> MYAPP onTerminate");
        removePubSub();
    }

    public void removePubSub() {
        removeAllRunningInstances();
        terminatePuSubInstance();
    }


    private void removeAllRunningInstances() {
        Logger.e("NetWorkDEMO", "removeAllRunningInstances called");
        connectReceiver(false);
    }

    private void registerNetWorkReceiver() {

        if (mNetWorkReceiver == null) {
            try {
                Logger.e("NetWorkDemo", "Network connectivity registered");
                IntentFilter mIntentFilter = new IntentFilter();
                mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                mIntentFilter.addAction(ConnectivityManager.EXTRA_NO_CONNECTIVITY);
                /*Extra Filter Started */
                mIntentFilter.addAction(ConnectivityManager.EXTRA_IS_FAILOVER);
                mIntentFilter.addAction(ConnectivityManager.EXTRA_REASON);
                mIntentFilter.addAction(ConnectivityManager.EXTRA_EXTRA_INFO);
                /*Extra Filter Ended */
//                mIntentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
//                mIntentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");

                this.mNetWorkReceiver = new NetworkChangeReceiver();
                this.registerReceiver(this.mNetWorkReceiver, mIntentFilter);
            } catch (Exception e) {
                Logger.e("NetWorkDemo", "Network connectivity register error occurred");
            }
        }
    }

    private void unregisterNetWorkReceiver() {

        if (mNetWorkReceiver != null)
            try {
                Logger.e("NetWorkDemo", "Network connectivity unregistered");
                this.unregisterReceiver(mNetWorkReceiver);
                this.mNetWorkReceiver = null;
            } catch (Exception e) {
                Logger.e("NetWorkDemo", "Network connectivity register error occurred");
                e.printStackTrace();
            }

    }

    public void setScreenOrientation() {
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity,
                                          Bundle savedInstanceState) {
                try {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } catch (Exception e) {

                }
                activity.setTitle(getResources().getString(R.string.app_name));

                setCurrentAct(activity);
                Utils.runGC();

                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                if (activity instanceof MainActivity || activity instanceof TrackOrderActivity) {
                    //Reset PubNub instance
                    configPuSubInstance();
                }
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Utils.runGC();
            }

            @Override
            public void onActivityResumed(Activity activity) {

                setCurrentAct(activity);

                isAppInBackground = false;
                Utils.runGC();
                Logger.d("AppBackground", "FromResume");
                Utils.sendBroadCast(getApplicationContext(), Utils.BACKGROUND_APP_RECEIVER_INTENT_ACTION);
                LocalNotification.clearAllNotifications();



                if (generalFun.isUserLoggedIn()){

                    JSONObject userProfileJsonObj = generalFun.getJsonObject(generalFun.retrieveValue(Utils.USER_PROFILE_JSON));

                    if (!requestPermissions.contains(android.Manifest.permission.ACCESS_FINE_LOCATION))
                        requestPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
                    if (!requestPermissions.contains(android.Manifest.permission.ACCESS_COARSE_LOCATION))
                        requestPermissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
                   /* if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (!requestPermissions.contains(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                            requestPermissions.add(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION);
                    }*/

                    String Packagetype = generalFun.getJsonValueStr("PACKAGE_TYPE", userProfileJsonObj);

                    if (!Packagetype.equalsIgnoreCase("STANDARD")){
                        if (!requestPermissions.contains(android.Manifest.permission.RECORD_AUDIO))
                            requestPermissions.add(android.Manifest.permission.RECORD_AUDIO);
                        if (!requestPermissions.contains(android.Manifest.permission.READ_PHONE_STATE))
                            requestPermissions.add(android.Manifest.permission.READ_PHONE_STATE);
                    }


                    if(!generalFun.isAllPermissionGranted(false,requestPermissions))
                    {
                        if (activity instanceof LauncherActivity){

                        }else {
                            new StartActProcess(activity).startAct(LauncherActivity.class);
                            activity.finish();
                        }

                    }
                }


            }

            @Override
            public void onActivityPaused(Activity activity) {

                isAppInBackground = true;
                Utils.runGC();
                Logger.d("AppBackground", "FromPause");
                Utils.sendBroadCast(getApplicationContext(), Utils.BACKGROUND_APP_RECEIVER_INTENT_ACTION);
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Logger.d("AppBackground", "onStop");
                Utils.runGC();


            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
                /*Called to retrieve per-instance state from an activity before being killed so that the state can be restored in onCreate(Bundle) or onRestoreInstanceState(Bundle) (the Bundle populated by this method will be passed to both).*/
                removeAllRunningInstances();
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Utils.hideKeyboard(activity);
                Utils.runGC();

//                connectReceiver(false);

                if (activity instanceof TrackOrderActivity && activity
                        == trackOrderAct) {
                    trackOrderAct = null;
                }
                if (activity instanceof MainActivity && activity == mainAct) {
                    mainAct = null;
                }


                if ((activity instanceof TrackOrderActivity && activity == trackOrderAct) || (activity instanceof MainActivity && activity == mainAct)) {
                    terminatePuSubInstance();
                }

            }


        });
    }

    private void connectReceiver(boolean isConnect) {
        if (isConnect && mNetWorkReceiver == null) {
            registerNetWorkReceiver();
        } else if (!isConnect && mNetWorkReceiver != null) {
            unregisterNetWorkReceiver();
        }
    }

    public Activity getCurrentAct() {
        return currentAct;
    }

    private void setCurrentAct(Activity currentAct) {
        this.currentAct = currentAct;

        if (currentAct instanceof LauncherActivity) {
            mainAct = null;
            trackOrderAct = null;
        }

        if (currentAct instanceof MainActivity) {
            trackOrderAct = null;
            mainAct = (MainActivity) currentAct;
        }

        if (currentAct instanceof TrackOrderActivity) {
            mainAct = null;
            trackOrderAct = (TrackOrderActivity) currentAct;
        }

        connectReceiver(true);
    }

    private String extractLogToFile() {
        PackageManager manager = this.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(this.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e2) {
        }
        String model = Build.MODEL;
        if (!model.startsWith(Build.MANUFACTURER))
            model = Build.MANUFACTURER + " " + model;

        // Make file name - file must be saved to external storage or it wont be readable by
        // the email app.
        String path = Environment.getExternalStorageDirectory() + "/" + "MyApp/";
        String fullName = path + "Log";
        Logger.d("Api", "fullName" + fullName);
        // Extract to file.
        File file = new File(fullName);
        InputStreamReader reader = null;
        FileWriter writer = null;
        try {
            // For Android 4.0 and earlier, you will get all app's log output, so filter it to
            // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
            String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
                    "logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" :
                    "logcat -d -v time";

            // get input stream
            Process process = Runtime.getRuntime().exec(cmd);
            reader = new InputStreamReader(process.getInputStream());

            // write output stream
            writer = new FileWriter(file);
            writer.write("Android version: " + Build.VERSION.SDK_INT + "\n");
            writer.write("Device: " + model + "\n");
            writer.write("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");

            char[] buffer = new char[10000];
            do {
                int n = reader.read(buffer, 0, buffer.length);
                if (n == -1)
                    break;
                writer.write(buffer, 0, n);
            } while (true);

            reader.close();
            writer.close();
        } catch (IOException e) {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e1) {
                }
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e1) {
                }

            // You might want to write a failure message to the log here.
            return null;
        }

        return fullName;
    }

    private void configPuSubInstance() {
        ConfigPubNub.getInstance(true).buildPubSub();
    }

    private void terminatePuSubInstance() {
        if (ConfigPubNub.retrieveInstance() != null) {
            ConfigPubNub.getInstance().releasePubSubInstance();
        }
    }

    public void restartWithGetDataApp() {
        GetUserData objRefresh = new GetUserData(generalFun, MyApp.getInstance().getCurrentAct());
        objRefresh.getData();
    }


    public void notifySessionTimeOut() {
        if (generateSessionAlert != null) {
            return;
        }


        generateSessionAlert = new GenerateAlertBox(MyApp.getInstance().getCurrentAct());


        generateSessionAlert.setContentMessage(generalFun.retrieveLangLBl("", "LBL_BTN_TRIP_CANCEL_CONFIRM_TXT"),
                generalFun.retrieveLangLBl("Your session is expired. Please login again.", "LBL_SESSION_TIME_OUT"));
        generateSessionAlert.setPositiveBtn(generalFun.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));
        generateSessionAlert.setCancelable(false);
        generateSessionAlert.setBtnClickList(btn_id -> {

            if (btn_id == 1) {

                onTerminate();
                generalFun.logOutUser(MyApp.this);

                (new GeneralFunctions(getCurrentAct())).restartApp();
            }
        });

        generateSessionAlert.showSessionOutAlertBox();
    }

    public void logOutFromDevice(boolean isForceLogout) {

        if (generalFun != null) {
            final HashMap<String, String> parameters = new HashMap<String, String>();

            parameters.put("type", "callOnLogout");
            parameters.put("iMemberId", generalFun.getMemberId());
            parameters.put("UserType", Utils.userType);

            ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getCurrentAct(), parameters);
            exeWebServer.setLoaderConfig(getCurrentAct(), true, generalFun);

            exeWebServer.setDataResponseListener(responseString -> {
                JSONObject responseObj=generalFun.getJsonObject(responseString);
                if (responseObj != null && !responseObj.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);

                    if (isDataAvail == true) {

                        onTerminate();
                        generalFun.logOutUser(MyApp.this);

                        (new GeneralFunctions(getCurrentAct())).restartApp();

                    } else {
                        if (isForceLogout) {
                            generalFun.showGeneralMessage("",
                                    generalFun.retrieveLangLBl("", generalFun.getJsonValueStr(Utils.message_str, responseObj)), buttonId -> (MyApp.getInstance().getGeneralFun(getCurrentAct())).restartApp());
                        } else {
                            generalFun.showGeneralMessage("",
                                    generalFun.retrieveLangLBl("", generalFun.getJsonValueStr(Utils.message_str, responseObj)));
                        }
                    }
                } else {
                    if (isForceLogout) {
                        generalFun.showError(buttonId -> (MyApp.getInstance().getGeneralFun(getCurrentAct())).restartApp());
                    } else {
                        generalFun.showError();
                    }
                }
            });
            exeWebServer.execute();
        }
    }
}
