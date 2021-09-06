package com.roaddo.store;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

import com.adapter.files.TrackOrderAdapter;
import com.fragments.ScrollSupportMapFragment;
import com.general.files.ConfigPubNub;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.general.files.UpdateDirections;
import com.general.files.UpdateFrequentTask;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.SphericalUtil;
import com.utils.AnimateMarker;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TrackOrderActivity extends BaseActivity implements OnMapReadyCallback {

    public String iDriverId = "";
    public LatLng userLatLng = null;
    public LatLng restLatLng = null;
    RecyclerView dataRecyclerView;
    TrackOrderAdapter adapter;
    GoogleMap gMap;
    ScrollSupportMapFragment map;
    ArrayList<HashMap<String, String>> listData = new ArrayList<>();
    GeneralFunctions generalFunctions;
    MTextView titleTxt, subTitleTxt;
    UpdateFrequentTask updateDriverLocTask;
    boolean isTaskKilled = false;
    Marker driverMarker;
    LatLng driverLocation;
    AnimateMarker animDriverMarker;
    UpdateDirections updateDirections;
    Polyline polyline = null;
    String eDisplayDottedLine = "";
    String eDisplayRouteLine = "";
    View iconRefreshImgView;
    View iconImgView;
    String vCompany;
    String DeliveryAddress;
    private ImageView backImgView;
    public static String serviceId = "";
    Polyline linePoly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_order);

        generalFunctions = MyApp.getInstance().getGeneralFun(getActContext());
        animDriverMarker = new AnimateMarker();
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        subTitleTxt = (MTextView) findViewById(R.id.subTitleTxt);
        dataRecyclerView = (RecyclerView) findViewById(R.id.dataRecyclerView);
        iconRefreshImgView = findViewById(R.id.iconRefreshImgView);
        iconImgView = findViewById(R.id.iconImgView);
        map = (ScrollSupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);

        map.getMapAsync(this);

        map.setListener(() -> ((NestedScrollView) findViewById(R.id.mScrollView)).requestDisallowInterceptTouchEvent(true));

        adapter = new TrackOrderAdapter(getActContext(), listData);

        iconImgView.setVisibility(View.GONE);

        dataRecyclerView.setAdapter(adapter);
        backImgView.setOnClickListener(new setOnClickList());
        iconRefreshImgView.setOnClickListener(new setOnClickList());

        //generateData();
//        getTrackDetails();
    }

    public void setTaskKilledValue(boolean isTaskKilled) {
        this.isTaskKilled = isTaskKilled;

        if (isTaskKilled == true) {
            onPauseCalled();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        setTaskKilledValue(true);

        unSubscribeToDriverLocChannel();

    }

    public void onPauseCalled() {

        if (updateDriverLocTask != null) {
            updateDriverLocTask.stopRepeatingTask();
        }
        updateDriverLocTask = null;

        unSubscribeToDriverLocChannel();
    }

    public void subscribeToDriverLocChannel() {

        if (!iDriverId.equalsIgnoreCase("")) {

            ArrayList<String> channelName = new ArrayList<>();
            channelName.add(Utils.pubNub_Update_Loc_Channel_Prefix + iDriverId);
            ConfigPubNub.getInstance().subscribeToChannels(channelName);
        }

    }

    public void pubnubMessage(JSONObject obj_msg) {

        String messageStr = generalFunctions.getJsonValueStr("Message", obj_msg);
        if (!messageStr.equals("")) {
            String vTitle = generalFunctions.getJsonValueStr("vTitle", obj_msg);
            if (messageStr.equalsIgnoreCase("OrderConfirmByRestaurant") || messageStr.equalsIgnoreCase("OrderPickedup")) {
                handleDailog(false, vTitle);

            } else if (messageStr.equalsIgnoreCase("OrderDeclineByRestaurant") || messageStr.equalsIgnoreCase("OrderDelivered") || messageStr.equalsIgnoreCase("OrderCancelByAdmin")) {

                unSubscribeToDriverLocChannel();

                (new StartActProcess(getActContext())).setOkResult();

                handleDailog(true, vTitle);

            } else if (messageStr.equalsIgnoreCase("CabRequestAccepted")) {

//                if (generalFunctions.getJsonValue("iDriverId", obj_msg) != null && !generalFunctions.getJsonValue("iDriverId", obj_msg).equals("")) {
//                    iDriverId = generalFunctions.getJsonValue("iDriverId", obj_msg).toString();
//                    subscribeToDriverLocChannel();
//                }
                handleDailog(false, vTitle);

            }

        } else if (generalFunctions.getJsonValueStr("MsgType", obj_msg) != null && !generalFunctions.getJsonValueStr("MsgType", obj_msg).equalsIgnoreCase("")) {

            pubNubMsgArrived(obj_msg.toString(), true);

        }

    }

    public void handleDailog(boolean isfinish, String vTitle) {

        runOnUiThread(() -> {
            final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
            generateAlert.setCancelable(false);
            generateAlert.setBtnClickList(btn_id -> {

                if (isfinish) {
                    backImgView.performClick();
                } else {
                    getTrackDetails();
                }
            });
            generateAlert.setContentMessage("", vTitle);
            generateAlert.setPositiveBtn(generalFunctions.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
            generateAlert.showAlertBox();

        });

    }

    public void unSubscribeToDriverLocChannel() {

        if (!iDriverId.equalsIgnoreCase("")) {
            ArrayList<String> channelName = new ArrayList<>();
            channelName.add(Utils.pubNub_Update_Loc_Channel_Prefix + iDriverId);
            ConfigPubNub.getInstance().unSubscribeToChannels(channelName);
        }

    }

    public void pubNubMsgArrived(final String message, final Boolean ishow) {

        runOnUiThread(() -> {

            String msgType = generalFunctions.getJsonValue("MsgType", message);
            String DriverId = generalFunctions.getJsonValue("iDriverId", message);

            if (msgType.equals("LocationUpdateOnTrip") && DriverId.equalsIgnoreCase(iDriverId)) {
                String vLatitude = generalFunctions.getJsonValue("vLatitude", message);
                String vLongitude = generalFunctions.getJsonValue("vLongitude", message);

                Location driverLoc = new Location("Driverloc");
                driverLoc.setLatitude(generalFunctions.parseDoubleValue(0.0, vLatitude));
                driverLoc.setLongitude(generalFunctions.parseDoubleValue(0.0, vLongitude));

//                if (eDisplayRouteLine.equalsIgnoreCase("Yes")&&  eDisplayDottedLine.equalsIgnoreCase("No")) {
                if (eDisplayRouteLine.equalsIgnoreCase("Yes")) {
                    callUpdateDeirection(driverLoc);
                }

                LatLng driverLocation_update = new LatLng(generalFunctions.parseDoubleValue(0.0, vLatitude),
                        generalFunctions.parseDoubleValue(0.0, vLongitude));

                driverLocation = driverLocation_update;
                updateDriverLocation(driverLocation_update);

            }
        });
    }

    public void callUpdateDeirection(Location driverlocation) {
        if (userLatLng == null) {
            return;

        }
        if (updateDirections == null) {

            Location location = new Location("userLocation");
            location.setLatitude(userLatLng.latitude);
            location.setLongitude(userLatLng.longitude);
            updateDirections = new UpdateDirections(getActContext(), gMap, driverlocation, location);
            updateDirections.scheduleDirectionUpdate();
        }

    }

    public void updateDriverLocation(LatLng latlog) {
        if (latlog == null) {
            return;
        }

        if (driverMarker == null) {
            try {
                if (gMap != null) {
                    MarkerOptions markerOptions = new MarkerOptions();

                    int iconId = R.mipmap.ic_delivery_scooter;

                    markerOptions.position(latlog).title("DriverId" + iDriverId).icon(BitmapDescriptorFactory.fromResource(iconId))
                            .anchor(0.5f, 0.5f).flat(true);

                    driverMarker = gMap.addMarker(markerOptions);

                    driverLocation = latlog;
                    CameraPosition cameraPosition = cameraForDriverPosition();
                    gMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                }
            } catch (Exception e) {
                Logger.d("markerException", e.toString());
            }
        } else {
            double currentZoomLevel = gMap.getCameraPosition().zoom;

            if (Utils.defaultZomLevel > currentZoomLevel) {
                currentZoomLevel = Utils.defaultZomLevel;
            }
            CameraPosition cameraPosition = new CameraPosition.Builder().target(latlog)
                    .zoom((float) currentZoomLevel).build();

            gMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            Location location = new Location("livetrack");
            location.setLatitude(latlog.latitude);
            location.setLongitude(latlog.longitude);
            animDriverMarker.animateMarker(driverMarker, gMap, location, 0, 800, iDriverId, "");

        }

    }

    public CameraPosition cameraForDriverPosition() {

        double currentZoomLevel = gMap == null ? Utils.defaultZomLevel : gMap.getCameraPosition().zoom;

        if (Utils.defaultZomLevel > currentZoomLevel) {
            currentZoomLevel = Utils.defaultZomLevel;
        }

        if (driverLocation != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(this.driverLocation.latitude, this.driverLocation.longitude))
                    .zoom((float) currentZoomLevel).build();

            return cameraPosition;
        } else {
            return null;
        }
    }

    public Context getActContext() {
        return TrackOrderActivity.this;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;
        gMap.getUiSettings().setMapToolbarEnabled(false);
//        this.gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                return true;
//            }
//        });

        //generateMapLocations();
    }

    public void generateMapLocations(double resLat, double resLong, double userLat, double userLong) {
        LatLng sourceLnt = new LatLng(resLat, resLong);
        restLatLng = sourceLnt;

        if (eDisplayRouteLine.equalsIgnoreCase("Yes")&&  eDisplayDottedLine.equalsIgnoreCase("No")) {
            if (linePoly != null) {
                linePoly.remove();
            }

        }

        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View restaurantMarkerView = inflater.inflate(R.layout.marker_view, null);

        ImageView restaurantPinImgView = (ImageView) restaurantMarkerView.findViewById(R.id.pinImgView);
        restaurantPinImgView.setImageResource(R.mipmap.ic_track_restaurant);

        MTextView restaurantMarkerTxtView = (MTextView) restaurantMarkerView.findViewById(R.id.addressTxtView);
        restaurantMarkerTxtView.setText(vCompany);

        View userMarkerView = inflater.inflate(R.layout.marker_view, null);
        ImageView userPinImgView = (ImageView) userMarkerView.findViewById(R.id.pinImgView);
        userPinImgView.setImageResource(R.mipmap.ic_track_user);

        MTextView userMarkerTxtView = (MTextView) userMarkerView.findViewById(R.id.addressTxtView);
        userMarkerTxtView.setText(DeliveryAddress);

        Marker sourceMarker = gMap.addMarker(new MarkerOptions().position(sourceLnt).icon(BitmapDescriptorFactory.fromBitmap(Utils.getBitmapFromView(restaurantMarkerView))));

        LatLng destLnt = new LatLng(userLat, userLong);
        userLatLng = destLnt;
        Marker destMarker = gMap.addMarker(new MarkerOptions().position(destLnt).icon(BitmapDescriptorFactory.fromBitmap(Utils.getBitmapFromView(userMarkerView))));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(sourceMarker.getPosition());
        builder.include(destMarker.getPosition());

        LatLngBounds bounds = builder.build();

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
        gMap.animateCamera(cu);


        if (eDisplayDottedLine.equalsIgnoreCase("Yes")|| eDisplayRouteLine.equalsIgnoreCase("No") && eDisplayDottedLine.equalsIgnoreCase("No")) {
//        if (!eDisplayDottedLine.equalsIgnoreCase("") && eDisplayDottedLine.equalsIgnoreCase("Yes")) {
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions
                    .jointType(JointType.ROUND)
                    .pattern(Arrays.asList(new Gap(20), new Dash(20)))
                    .add(sourceMarker.getPosition())
                    .add(destMarker.getPosition());
            polylineOptions.width(Utils.dipToPixels(getActContext(), 4));

            linePoly = gMap.addPolyline(polylineOptions);
            linePoly.setColor(Color.parseColor("#cecece"));
            linePoly.setStartCap(new RoundCap());
            linePoly.setEndCap(new RoundCap());
        }


        buildArcLine(sourceLnt, destLnt, 0.050);
    }

    private void buildArcLine(LatLng p1, LatLng p2, double arcCurvature) {
        //Calculate distance and heading between two points
        double d = SphericalUtil.computeDistanceBetween(p1, p2);
        double h = SphericalUtil.computeHeading(p1, p2);

        if (h < 0) {
            LatLng tmpP1 = p1;
            p1 = p2;
            p2 = tmpP1;

            d = SphericalUtil.computeDistanceBetween(p1, p2);
            h = SphericalUtil.computeHeading(p1, p2);
        }

        //Midpoint position
        LatLng midPointLnt = SphericalUtil.computeOffset(p1, d * 0.5, h);

        //Apply some mathematics to calculate position of the circle center
        double x = (1 - arcCurvature * arcCurvature) * d * 0.5 / (2 * arcCurvature);
        double r = (1 + arcCurvature * arcCurvature) * d * 0.5 / (2 * arcCurvature);

        LatLng centerLnt = SphericalUtil.computeOffset(midPointLnt, x, h + 90.0);

        //Polyline options
        PolylineOptions options = new PolylineOptions();
        List<PatternItem> pattern = Arrays.<PatternItem>asList(new Dash(30), new Gap(20));

        //Calculate heading between circle center and two points
        double h1 = SphericalUtil.computeHeading(centerLnt, p1);
        double h2 = SphericalUtil.computeHeading(centerLnt, p2);

        //Calculate positions of points on circle border and add them to polyline options
        int numPoints = 100;
        double step = (h2 - h1) / numPoints;

        for (int i = 0; i < numPoints; i++) {
            LatLng middlePointTemp = SphericalUtil.computeOffset(centerLnt, r, h1 + i * step);
            options.add(middlePointTemp);
        }

//        if (!eDisplayDottedLine.equalsIgnoreCase("") && eDisplayDottedLine.equalsIgnoreCase("Yes")) {
        //Draw polyline ic_track_restaurant

        if (polyline != null) {
            polyline.remove();
            polyline = null;
        }

//        if (!eDisplayDottedLine.equalsIgnoreCase("") && eDisplayDottedLine.equalsIgnoreCase("Yes")) {
        if (eDisplayDottedLine.equalsIgnoreCase("Yes")|| eDisplayRouteLine.equalsIgnoreCase("No") && eDisplayDottedLine.equalsIgnoreCase("No")) {
            polyline = gMap.addPolyline(options.width(10).color(Color.BLACK).geodesic(false).pattern(pattern));
        }

//        } else {
//            if (polyline != null) {
//                polyline.remove();
//                polyline = null;
//                gMap.clear();
//                if (restLatLng != null && userLatLng != null) {
//                    generateMapLocations(restLatLng.latitude, restLatLng.longitude, userLatLng.latitude, userLatLng.longitude);
//                }
//            }
//
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getTrackDetails();
    }

    public void getTrackDetails() {
        try {

            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("type", "getOrderDeliveryLog");
            parameters.put("iOrderId", getIntent().getStringExtra("iOrderId"));
            parameters.put("UserType", Utils.app_type);
            parameters.put("iUserId", generalFunctions.getMemberId());

            ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);

            exeWebServer.setLoaderConfig(getActContext(), true, generalFunctions);
            exeWebServer.setDataResponseListener(responseString -> {
                JSONObject responseObj = generalFunctions.getJsonObject(responseString);

                if (responseString != null && !responseString.equals("")) {
                    String action = generalFunctions.getJsonValue(Utils.action_str, responseString);
                    if (action.equals("1")) {
                        listData.clear();
                        adapter.notifyDataSetChanged();
                        if (generalFunctions.getJsonValue("iServiceId", responseString) != null && !generalFunctions.getJsonValue("iServiceId", responseString).equalsIgnoreCase("")) {
                            serviceId = generalFunctions.getJsonValue("iServiceId", responseString);
                        }

                        eDisplayDottedLine = generalFunctions.getJsonValue("eDisplayDottedLine", responseString);
                        eDisplayRouteLine = generalFunctions.getJsonValue("eDisplayRouteLine", responseString);
                        vCompany = generalFunctions.getJsonValue("vCompany", responseString);
                        DeliveryAddress = generalFunctions.getJsonValue("DeliveryAddress", responseString);
                        String DriverPhone = generalFunctions.getJsonValue("DriverPhone", responseString);

                        generateMapLocations(GeneralFunctions.parseDoubleValue(0, generalFunctions.getJsonValue("CompanyLat", responseString)), GeneralFunctions.parseDoubleValue(0, generalFunctions.getJsonValue("CompanyLong", responseString)), GeneralFunctions.parseDoubleValue(0, generalFunctions.getJsonValue("PassengerLat", responseString)), GeneralFunctions.parseDoubleValue(0, generalFunctions.getJsonValue("PassengerLong", responseString)));

                        if (generalFunctions.getJsonValue("iDriverId", responseString) != null && !generalFunctions.getJsonValue("iDriverId", responseString).equalsIgnoreCase("")) {
                            iDriverId = generalFunctions.getJsonValue("iDriverId", responseString);
                        }

                        titleTxt.setText(generalFunctions.retrieveLangLBl("", "LBL_ORDER") + "#" + generalFunctions.convertNumberWithRTL(generalFunctions.getJsonValue("vOrderNo", responseString)));
//                        subTitleTxt.setText(generalFunctions.convertNumberWithRTL(generalFunctions.getDateFormatedType(generalFunctions.getJsonValue("tOrderRequestDate", responseString), Utils.OriginalDateFormate, Utils.TrackDateFormatewithTime)));

                        String tOrderRequestDate=generalFunctions.getJsonValue("tOrderRequestDate", responseString);

                        String formattedDate=generalFunctions.getDateFormatedType(tOrderRequestDate, Utils.OriginalDateFormate, CommonUtilities.OriginalDateFormate)+", "+generalFunctions.getDateFormatedType(tOrderRequestDate, Utils.OriginalDateFormate, CommonUtilities.OriginalTimeFormate);
                        subTitleTxt.setText(generalFunctions.convertNumberWithRTL(formattedDate));

                        JSONArray messageArr = null;
                        messageArr = generalFunctions.getJsonArray(Utils.message_str, responseString);

                        if (generalFunctions.getJsonValue("iDriverId", responseString) != null && !generalFunctions.getJsonValue("iDriverId", responseString).equalsIgnoreCase("")) {
                            iDriverId = generalFunctions.getJsonValue("iDriverId", responseString);

                            //  unSubscribeToDriverLocChannel();
                            subscribeToDriverLocChannel();

                        }

                        for (int i = 0; i < messageArr.length(); i++) {
                            JSONObject rowObject = generalFunctions.getJsonObject(messageArr, i);
                            HashMap<String, String> mapData = new HashMap<>();
                            mapData.put("vStatus", generalFunctions.getJsonValueStr("vStatus_Track", rowObject));
                            mapData.put("eShowCallImg", generalFunctions.getJsonValueStr("eShowCallImg", rowObject));
                            mapData.put("DriverPhone", DriverPhone);
                            mapData.put("iDriverId", generalFunctions.getJsonValueStr("iDriverId", rowObject));
                            mapData.put("driverImage", generalFunctions.getJsonValueStr("driverImage", rowObject));
                            mapData.put("driverName", generalFunctions.getJsonValueStr("driverName", rowObject));
                            mapData.put("vCompany", vCompany);
                            Object iorderId = generalFunctions.getJsonValue("iOrderId", rowObject);
                            if (iorderId != null && Utils.checkText(iorderId.toString())) {
                                mapData.put("iOrderId", iorderId.toString());
                            }

                            listData.add(mapData);
                        }


                        adapter = new TrackOrderAdapter(getActContext(), listData);
                        dataRecyclerView.setAdapter(adapter);

                    } else {
                    }
                } else {
                    generalFunctions.showError();
                }
            });
            exeWebServer.execute();
        } catch (Exception e) {

        }
    }

    public void validateIncomingOrderMessage(String message) {

        String iOrderId = generalFunctions.getJsonValue("iOrderId", message);
        if (iOrderId.equalsIgnoreCase(getIntent().getStringExtra("iOrderId"))) {
//            getTrackDetails();
            pubnubMessage(generalFunctions.getJsonObject(message));
        }
    }

    public void validateIncomingMessage(String message) {

        String iDriverId_tmp = generalFunctions.getJsonValue("iDriverId", message);
        if (iDriverId_tmp.equalsIgnoreCase(iDriverId)) {
            pubnubMessage(generalFunctions.getJsonObject(message));
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {

        super.onOptionsMenuClosed(menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {

        return super.onMenuOpened(featureId, menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.backImgView:
                    TrackOrderActivity.super.onBackPressed();
                    break;
            }
        }
    }
}