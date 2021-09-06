package com.general.files;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.sls.yalgaar_api.YalgaarApiClient;
import com.sls.yalgaar_api.exception.ArgumentException;
import com.sls.yalgaar_api.exception.ConnectFailureException;
import com.sls.yalgaar_api.interfaces.ConnectionCallback;
import com.sls.yalgaar_api.interfaces.SubscribeCallback;
import com.sls.yalgaar_api.interfaces.UnSubscribeCallback;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import io.github.sac.Socket;

public class ConfigYalgaarConnection implements ConfigYalgaarConnectionResponseListener.ConfigYalgaarConnectionListener {


    /**
     * Variable declaration of singleton instance.
     */
    private static ConfigYalgaarConnection configYalgaarInstance = null;

    /**
     * Variable declaration of singleton instance.
     */
    private YalgaarApiClient client = new YalgaarApiClient.Builder(MyApp.getInstance().getApplicationContext()).build();

    /**
     * This list maintains list of subscribed channels list.
     */
    ArrayList<String> listOfSubscribedList = new ArrayList<>();

    /**
     * Used to remove redundant messages (trip message or others). Internal purpose only.
     */
    private HashMap<String, String> listOfCurrentTripMsg_Map = new HashMap<>();

    /**
     * This will indicates weather a client is killed or not.
     */
    boolean isClientKilled = false;

    ConfigYalgaarConnectionResponseListener yalgaarResponseListener;

    private boolean isDispatchThreadLocked = false;

    private boolean isClientConnected = false;

    public static ConfigYalgaarConnection getInstance() {
        if (configYalgaarInstance == null) {
            configYalgaarInstance = new ConfigYalgaarConnection();

        }
        return configYalgaarInstance;
    }

    /**
     * Fetch Singleton instance. By using this method will return instance of this class.
     */
    public static ConfigYalgaarConnection retrieveInstance() {
        return configYalgaarInstance;
    }

    public void buildConnection() {
        if(isClientConnected){
            continueChannelSubscribe();
            return;
        }
        yalgaarResponseListener = new ConfigYalgaarConnectionResponseListener(this);

        GeneralFunctions generalFunc = getGeneralFunc();
        try {
            client.connect(getGeneralFunc().retrieveValue(Utils.YALGAAR_CLIENT_KEY), false, (generalFunc.retrieveValue(Utils.DEVICE_SESSION_ID_KEY).equals("") ? generalFunc.getMemberId() : generalFunc.retrieveValue(Utils.DEVICE_SESSION_ID_KEY)), yalgaarResponseListener);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * This function is used to subscribe channels, which are not due to not connected to server.
     */
    private void continueChannelSubscribe() {

        try {
            System.gc();
        } catch (Exception e) {

        }

        if (getGeneralFunc().getMemberId().trim().equals("")) {
            forceDestroy();
            return;
        }

        //Subscribe to private channel
        ConfigYalgaarConnection.getInstance().subscribeToChannels("DRIVER_" + getGeneralFunc().getMemberId());

        // Resubscribe to all previously subscribed channels.
        for (int i = 1; i < listOfSubscribedList.size(); i++) {
            if (!listOfSubscribedList.get(i).equals("DRIVER_" + getGeneralFunc().getMemberId())) {
                ConfigYalgaarConnection.getInstance().subscribeToChannels(listOfSubscribedList.get(i));
            }
        }
    }

    /**
     * Function used to get general function object
     *
     * @return GeneralFunction object is returned.
     */
    private static GeneralFunctions getGeneralFunc() {
        GeneralFunctions generalFunc = MyApp.getInstance().getGeneralFun(MyApp.getInstance().getApplicationContext());

        return generalFunc;
    }

    /**
     * Function used to dispatch message to user when received some event on particular channel.
     */
    private void dispatchMsg(String message) {
//        Utils.printLog("SocketApp", "111CalledFrom:ConfigYalgaarConnection:" + message);
        if (!isDispatchThreadLocked) {
            isDispatchThreadLocked = true;

            Logger.d("SocketApp", "CalledFrom:ConfigYalgaarConnection:" + message);

            if (listOfCurrentTripMsg_Map.get(message) == null) {
                Logger.d("SocketApp", "1:CalledFrom:ConfigYalgaarConnection");
                listOfCurrentTripMsg_Map.put(message, "Yes");
                (new FireTripStatusMsg()).fireTripMsg(message);
            }

            isDispatchThreadLocked = false;
        }
    }

    public void subscribeToChannels(String channelName) {
        // Add any new Channels to subscribed channels list. We will use this list to un subscribe channels.
        if (!listOfSubscribedList.contains(channelName)) {
            listOfSubscribedList.add(channelName);
        }

        if (isClientConnected == false) {
            return;
        }

        try {
            client.subscribe(channelName, yalgaarResponseListener, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function used to unSubscribe from particular channel.
     *
     * @param channelName from which users will be un subscribed
     */
    public void unSubscribeFromChannels(String channelName) {
        try {
            client.unSubscribe(channelName, yalgaarResponseListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function used to publish message on particular channel.
     *
     * @param channelName Name of channel on which message will be published.
     * @param message     Message needs to be published.
     */
    public void publishMsg(String channelName, String message) {
        try {
            client.publish(channelName, message, yalgaarResponseListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Function used to un subscribe all channels. Generally this will be done when app is going to be terminate.
     */
    public void releaseAllChannels() {

        isClientKilled = true;

        for (int i = 1; i < listOfSubscribedList.size(); i++) {
            ConfigYalgaarConnection.getInstance().unSubscribeFromChannels(listOfSubscribedList.get(i));
        }

        listOfSubscribedList.clear();
    }

    /**
     * Function used to destroy current instance. Generally this will be done when app is going to be terminate OR instance of this class is no longer required.
     */
    public void forceDestroy() {
//        Utils.printLog("SocketApp", "::ForceDestroy::");
        releaseAllChannels();

        ConfigYalgaarConnection.getInstance().client.disConnect();

        configYalgaarInstance = null;
    }

    @Override
    public void onClientConnected() {
        isClientConnected = true;

        continueChannelSubscribe();
    }

    @Override
    public void onClientDisConnected() {
        isClientConnected = false;
    }

    @Override
    public void onDataReceived(String data) {
        Logger.d("Yalgaar","::DataReceived::"+data);
        dispatchMsg(data);
    }
}
