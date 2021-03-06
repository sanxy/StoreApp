package com.roaddo.store;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.general.files.InternetConnection;
import com.general.files.MyApp;

/**
 * Created by Admin on 31-08-2017.
 */

public class NetworkChangeReceiver extends BroadcastReceiver {


    private MyApp mApplication;

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean status = new InternetConnection(context).isNetworkConnected();

        mApplication = ((MyApp) context.getApplicationContext());


        try {
            Activity currentActivity = mApplication.getCurrentAct();

            if (currentActivity instanceof MainActivity) {
                ((MainActivity) currentActivity).handleNoNetworkDial();

            }

        } catch (Exception e) {

        }
    }
}