package com.adapter.files;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.roaddo.store.CallScreenActivity;
import com.roaddo.store.R;
import com.roaddo.store.TrackOrderActivity;
import com.general.files.AppFunctions;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.SinchService;
import com.sinch.android.rtc.calling.Call;
import com.utils.CommonUtilities;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.MTextView;
import com.view.TimelineView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by admin on 01/05/18.
 */

public class TrackOrderAdapter extends RecyclerView.Adapter<TrackOrderAdapter.ViewHolder> {

    Context mContext;
    ArrayList<HashMap<String, String>> listData;
    GeneralFunctions generalFunctions;

    String userprofileJson = "";
    String RIDE_DRIVER_CALLING_METHOD = "";


    public TrackOrderAdapter(Context mContext, ArrayList<HashMap<String, String>> listData) {
        this.mContext = mContext;
        this.listData = listData;
        generalFunctions = MyApp.getInstance().getGeneralFun(mContext);
        userprofileJson = generalFunctions.retrieveValue(Utils.USER_PROFILE_JSON);
        RIDE_DRIVER_CALLING_METHOD=generalFunctions.getJsonValue("RIDE_DRIVER_CALLING_METHOD", userprofileJson);
    }

    @Override
    public TrackOrderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_track_item_design, parent, false);

        TrackOrderAdapter.ViewHolder viewHolder = new TrackOrderAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TrackOrderAdapter.ViewHolder holder, int position) {

        HashMap<String, String> mapData = listData.get(position);

        holder.contentTxtView.setText(mapData.get("vStatus"));

        int color=Color.parseColor("#FFFFFF");
        new CreateRoundedView(color, Utils.dipToPixels(mContext, 5), 0, color, holder.containerView);

        String eShowCallImg=mapData.get("eShowCallImg");
        if (eShowCallImg != null && eShowCallImg.equalsIgnoreCase("Yes")) {
            holder.callImgView.setVisibility(View.VISIBLE);
        } else {
            holder.callImgView.setVisibility(View.GONE);
        }

        holder.callImgView.setOnClickListener(v -> {


            if (RIDE_DRIVER_CALLING_METHOD.equals("Voip")) {
                sinchCall(mapData);
            } else {
                getMaskNumber(mapData);
            }
        });
        holder.contentTxtView.setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "fonts/Poppins_Light.ttf"));
        if (position == listData.size() - 1) {
            holder.contentTxtView.setTypeface(Typeface.createFromAsset(mContext.getResources().getAssets(), "fonts/Poppins_SemiBold.ttf"));

        }


        holder.mTimelineView.initLine(TimelineView.LineType.NORMAL);

        holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_check_mark_button));


        if (listData.size() == 1) {
            holder.mTimelineView.initLine(TimelineView.LineType.ONLYONE);

            holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_check_mark_button));

        } else if (listData.size() > 1) {

            if (position == 0) {
                holder.mTimelineView.initLine(TimelineView.LineType.BEGIN);

                holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_check_mark_button));

            } else {
                holder.mTimelineView.initLine(TimelineView.LineType.NORMAL);

                holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_check_mark_button));
            }

            if (position == listData.size() - 1) {
                holder.mTimelineView.initLine(TimelineView.LineType.END);

                holder.mTimelineView.setMarker(ContextCompat.getDrawable(mContext, R.drawable.ic_brightness_1_black_24dp));

            }

        }
    }


    public void sinchCall(HashMap<String, String> mapData) {


        if (generalFunctions.isCallPermissionGranted(false) == false) {
            generalFunctions.isCallPermissionGranted(true);
        } else {
            TrackOrderActivity activity = (TrackOrderActivity) mContext;

            if (new AppFunctions(mContext).checkSinchInstance(activity != null ? activity.getSinchServiceInterface() : null)) {
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("Id", generalFunctions.getMemberId());
                hashMap.put("Name", mapData.get("vCompany"));
                hashMap.put("PImage", generalFunctions.getJsonValue("vImage", userprofileJson));
                hashMap.put("type", Utils.userType);

                String iDriverId = mapData.get("iDriverId");
                String image_url = CommonUtilities.PROVIDER_PHOTO_PATH + iDriverId + "/"
                        + mapData.get("driverImage");


                activity.getSinchServiceInterface().getSinchClient().setPushNotificationDisplayName(generalFunctions.retrieveLangLBl("", "LBL_INCOMING_CALL"));
                Call call = activity.getSinchServiceInterface().callUser(Utils.CALLTODRIVER + "_" + iDriverId, hashMap);

                String callId = call.getCallId();

                Intent callScreen = new Intent(mContext, CallScreenActivity.class);
                callScreen.putExtra(SinchService.CALL_ID, callId);
                callScreen.putExtra("vImage", image_url);
                callScreen.putExtra("vName", mapData.get("driverName"));
                callScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                mContext.startActivity(callScreen);
            }

        }


    }

    public void call(String phoneNumber) {

        try {

            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            mContext.startActivity(callIntent);

        } catch (Exception e) {
        }
    }

    public void getMaskNumber(HashMap<String, String> mapData) {
        if (generalFunctions.getJsonValue("CALLMASKING_ENABLED", userprofileJson).equalsIgnoreCase("Yes")) {

            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("type", "getCallMaskNumber");
            parameters.put("iOrderId", mapData.containsKey("iOrderId") ? mapData.get("iOrderId") : "");
            parameters.put("UserType", Utils.userType);
            parameters.put("iMemberId", generalFunctions.getMemberId());

            ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(mContext, parameters);
            exeWebServer.setLoaderConfig(mContext, true, generalFunctions);

            exeWebServer.setDataResponseListener(responseString -> {
                JSONObject responseObj = generalFunctions.getJsonObject(responseString);

                if (responseObj != null && !responseObj.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);

                    if (isDataAvail == true) {
                        String message = generalFunctions.getJsonValueStr(Utils.message_str, responseObj);

                        call(message);
                    } else {
                        directCall(mapData);
                    }
                } else {
                    generalFunctions.showError();
                }
            });
            exeWebServer.execute();
        } else {
            directCall(mapData);
        }
    }

    public void directCall(HashMap<String, String> mapData) {
        String DriverPhone = mapData.get("DriverPhone");
        if (DriverPhone != null) {
            call(DriverPhone);
        }
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        public LinearLayout containerView;
        public MTextView contentTxtView;
        public ImageView callImgView;
        public TimelineView mTimelineView;

        public ViewHolder(View view) {
            super(view);

            contentTxtView = (MTextView) view.findViewById(R.id.contentTxtView);
            containerView = (LinearLayout) view.findViewById(R.id.containerView);
            callImgView = (ImageView) view.findViewById(R.id.callImgView);
            mTimelineView = (TimelineView) view.findViewById(R.id.time_marker);

        }
    }
}
