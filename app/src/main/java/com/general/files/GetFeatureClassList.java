package com.general.files;

import com.roaddo.store.BuildConfig;
import com.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class GetFeatureClassList {

    private static String resourceFilePath = "res/layout/";
    private static String resourcePath = "layout";

    public static HashMap<String, String> getAllGeneralClasses() {
        HashMap<String, String> classParams = new HashMap<>();

        ArrayList<String> voipServiceClassList = new ArrayList<>();
        voipServiceClassList.add("com.sinch.android.rtc.SinchClient");
        voipServiceClassList.add("com.sinch.android.rtc.SinchClient");
        voipServiceClassList.add("com.general.files.SinchService");
        voipServiceClassList.add("com.general.files.SinchCallListener");
        voipServiceClassList.add("com.general.files.SinchCallClientListener");
        voipServiceClassList.add(BuildConfig.APPLICATION_ID + ".CallScreenActivity");
        voipServiceClassList.add(resourceFilePath + "callscreen");
        voipServiceClassList.add(BuildConfig.APPLICATION_ID + ".IncomingCallScreenActivity");
        voipServiceClassList.add(resourceFilePath + "incoming");

        classParams.put("VOIP_SERVICE", "No");
        for (String item : voipServiceClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("VOIP_SERVICE", "Yes");
                break;
            }
        }

        ArrayList<String> advertisementClassList = new ArrayList<>();
        advertisementClassList.add("com.general.files.OpenAdvertisementDialog");
        advertisementClassList.add(resourceFilePath + "advertisement_dailog");

        classParams.put("ADVERTISEMENT_MODULE", "No");
        for (String item : advertisementClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("ADVERTISEMENT_MODULE", "Yes");
                break;
            }
        }

        ArrayList<String> linkedInClassList = new ArrayList<>();
        linkedInClassList.add("com.general.files.OpenLinkedinDialog");
        linkedInClassList.add("com.general.files.RegisterLinkedinLoginResCallBack");

        classParams.put("LINKEDIN_MODULE", "No");
        for (String item : linkedInClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("LINKEDIN_MODULE", "Yes");
                break;
            }
        }

        ArrayList<String> cardIOClassList = new ArrayList<>();
        cardIOClassList.add("io.card.payment.CardIOActivity");

        classParams.put("CARD_IO", "No");
        for (String item : cardIOClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("CARD_IO", "Yes");
                break;
            }
        }

        ArrayList<String> liveChatClassList = new ArrayList<>();
        liveChatClassList.add("com.livechatinc.inappchat.ChatWindowActivity");

        classParams.put("LIVE_CHAT", "No");
        for (String item : liveChatClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("LIVE_CHAT", "Yes");
                break;
            }
        }

        classParams.put("WAYBILL_MODULE", "No");

        classParams.put("DELIVER_ALL", "No");

        classParams.put("MULTI_DELIVERY", "No");

        classParams.put("UBERX_SERVICE", "No");

        ArrayList<String> newsClassList = new ArrayList<>();
        newsClassList.add(BuildConfig.APPLICATION_ID + ".NotificationActivity");
        newsClassList.add(resourceFilePath + "activity_notification");
        newsClassList.add(BuildConfig.APPLICATION_ID + ".NotificationDetailsActivity");
        newsClassList.add(resourceFilePath + "activity_notification_details");
        newsClassList.add("com.fragments.NotiFicationFragment");
        newsClassList.add(resourceFilePath + "fragment_notification");
        newsClassList.add("com.adapter.files.NotificationAdapter");
        newsClassList.add(resourceFilePath + "item_notification_view");

        classParams.put("NEWS_SECTION", "No");
        for (String item : newsClassList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("NEWS_SECTION", "Yes");
                break;
            }
        }

        classParams.put("RENTAL_FEATURE", "No");

        classParams.put("DELIVERY_MODULE", "No");

        classParams.put("RIDE_SECTION", "No");


        ArrayList<String> thermalPrinterList = new ArrayList<>();
        newsClassList.add("com.general.files.thermalPrint.GenerateOrderBill");
        newsClassList.add("com.general.files.thermalPrint.PrinterCommands");
        newsClassList.add("com.general.files.thermalPrint.PrintUtils");
        thermalPrinterList.add(BuildConfig.APPLICATION_ID + ".ThermalPrintSettingActivity");
        thermalPrinterList.add(resourceFilePath + "activity_thermal_print_setting");
        thermalPrinterList.add("com.adapter.files.BlueToothDeviceListAdapter");
        thermalPrinterList.add(resourceFilePath + "item_blutooth_design");
        thermalPrinterList.add(resourceFilePath + "layout_list");

        classParams.put("THERMAL_PRINT_MODULE", "No");
        for (String item : thermalPrinterList) {
            if ((item.startsWith(resourceFilePath) && MyApp.getInstance().getApplicationContext() != null && Utils.isResourceFileExist(MyApp.getInstance().getApplicationContext(), item.replace(resourceFilePath, ""), resourcePath)) || Utils.isClassExist(item)) {
                classParams.put("THERMAL_PRINT_MODULE", "Yes");
                break;
            }
        }


        /** Removal file of libraries **/
        voipServiceClassList.add("libs/sinch_lib.aar");
        voipServiceClassList.add("Libs folder remove file called 'sinch_lib' Or any lib which is related to SINCH");
        cardIOClassList.add("Go to App's Level build.Gradle File and Remove Library 'io.card:android-sdk'");
        liveChatClassList.add("Go to App's Level build.Gradle File and Remove Library 'com.github.livechat:chat-window-android'");
        /** Removal file of libraries **/

        if (classParams.get("VOIP_SERVICE") != null && classParams.get("VOIP_SERVICE").equalsIgnoreCase("Yes")) {
            classParams.put("VOIP_SERVICE_FILES", android.text.TextUtils.join(",", voipServiceClassList));
        }

        if (classParams.get("ADVERTISEMENT_MODULE") != null && classParams.get("ADVERTISEMENT_MODULE").equalsIgnoreCase("Yes")) {
            classParams.put("ADVERTISEMENT_MODULE_FILES", android.text.TextUtils.join(",", advertisementClassList));
        }

        if (classParams.get("LINKEDIN_MODULE") != null && classParams.get("LINKEDIN_MODULE").equalsIgnoreCase("Yes")) {
            classParams.put("LINKEDIN_MODULE_FILES", android.text.TextUtils.join(",", linkedInClassList));
        }

        if (classParams.get("CARD_IO") != null && classParams.get("CARD_IO").equalsIgnoreCase("Yes")) {
            classParams.put("CARD_IO_FILES", android.text.TextUtils.join(",", cardIOClassList));
        }

        if (classParams.get("LIVE_CHAT") != null && classParams.get("LIVE_CHAT").equalsIgnoreCase("Yes")) {
            classParams.put("LIVE_CHAT_FILES", android.text.TextUtils.join(",", liveChatClassList));
        }


        if (classParams.get("NEWS_SECTION") != null && classParams.get("NEWS_SECTION").equalsIgnoreCase("Yes")) {
            classParams.put("NEWS_SERVICE_FILES", android.text.TextUtils.join(",", newsClassList));
        }

        if (classParams.get("THERMAL_PRINT_MODULE") != null && classParams.get("THERMAL_PRINT_MODULE").equalsIgnoreCase("Yes")) {
            classParams.put("THERMAL_PRINT_MODULE_FILES", android.text.TextUtils.join(",", thermalPrinterList));
        }

        classParams.put("PACKAGE_NAME", BuildConfig.APPLICATION_ID);

        return classParams;
    }
}
