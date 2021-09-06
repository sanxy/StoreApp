package com.roaddo.store;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.GetLocationUpdates;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;

public class SearchPickupLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GetAddressFromLocation.AddressFound,
        GetLocationUpdates.LocationUpdates, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {

    public static final int REQUEST_CHECK_SETTINGS = 875;

    MTextView titleTxt;
    ImageView backImgView;
    GeneralFunctions generalFunc;
    MButton btn_type2;
    int btnId;
    MTextView placeTxtView;
    boolean isPlaceSelected = false;
    LatLng placeLocation;
    Marker placeMarker;
    SupportMapFragment map;
    GoogleMap gMap;
    GetAddressFromLocation getAddressFromLocation;
    GetLocationUpdates getLastLocation;

    private boolean isFirstLocation = true;
    ImageView pinImgView;
    MTextView locationImage;
    private Location userLocation;
    public boolean isAddressEnable = false;
    private SearchPickupLocationActivity listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_pickup_location);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        placeTxtView = (MTextView) findViewById(R.id.placeTxtView);
        pinImgView = (ImageView) findViewById(R.id.pinImgView);
        locationImage = (MTextView) findViewById(R.id.locationImage);

        map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapV2);

        getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
        getAddressFromLocation.setAddressList(this);

        setLabels();

        map.getMapAsync(SearchPickupLocationActivity.this);

        backImgView.setOnClickListener(new setOnClickAct());
        locationImage.setOnClickListener(new setOnClickAct());

        btnId = Utils.generateViewId();
        btn_type2.setId(btnId);

        btn_type2.setOnClickListener(new setOnClickAct());
        (findViewById(R.id.pickUpLocSearchArea)).setOnClickListener(new setOnClickAct());
    }

    public void setLabels() {
        placeTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SEARCH_LOC"));
        pinImgView.setVisibility(View.GONE);
        String isPickUpLoc = getIntent().getStringExtra("isPickUpLoc");
        if (isPickUpLoc != null && isPickUpLoc.equals("true")) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SET_STORE_LOCATION"));
        } else if (getIntent().getStringExtra("isHome") != null && getIntent().getStringExtra("isHome").equals("true")) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_BIG_TXT"));
        } else if (getIntent().getStringExtra("isWork") != null && getIntent().getStringExtra("isWork").equals("true")) {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_WORK_HEADER_TXT"));
        } else {
            titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_SELECT_DESTINATION_HEADER_TXT"));
        }

        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_LOC"));
        placeTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SEARCH_PLACE_HINT_TXT"));
        locationImage.setText(generalFunc.retrieveLangLBl("", "LBL_CHANGE"));
    }

    @Override
    public void onAddressFound(String address, double latitude, double longitude) {
        if (getCenterLocation() != null && (getCenterLocation().latitude != latitude || getCenterLocation().longitude != longitude)) {
            return;
        }
        placeTxtView.setText(address);
        isPlaceSelected = true;
        this.placeLocation = new LatLng(latitude, longitude);

        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(this.placeLocation, 14.0f);

        if (gMap != null) {
            gMap.clear();
            if (isFirstLocation) {
                gMap.moveCamera(cu);
            }
            isFirstLocation = false;

            setGoogleMapCameraListener(this);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.gMap = googleMap;

        /*if (getIntent().hasExtra("PickUpLatitude") && getIntent().hasExtra("PickUpLongitude")) {

            LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLatitude")),
                    generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLongitude")));

            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(placeLocation, 14);

            gMap.moveCamera(cu);

        }*/

        setGoogleMapCameraListener(this);

        if (getLastLocation != null) {
            getLastLocation.stopLocationUpdates();
            getLastLocation = null;
        }

        boolean permission = generalFunc.isLocationPermissionGranted(true);
        if (permission) {
            getLastLocation = new GetLocationUpdates(getActContext(), Utils.LOCATION_UPDATE_MIN_DISTANCE_IN_MITERS, true, this);
        }
    }


    @Override
    public void onLocationUpdate(Location location) {
        if (location == null) {
            return;
        }
        if (getLastLocation != null) {
            getLastLocation.stopLocationUpdates();
            getLastLocation = null;
        }
//        setCameraPosition(new LatLng(location.getLatitude(), location.getLongitude()));


        if (isFirstLocation == true) {
            placeLocation = getLocationLatLng(true);
            if (listener==null && isAddressEnable)
            {
                setGoogleMapCameraListener(this);
            }
            if (placeLocation != null) {
                setCameraPosition(new LatLng(placeLocation.latitude, placeLocation.longitude));
            } else {
                setCameraPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            }

            pinImgView.setVisibility(View.VISIBLE);
            isFirstLocation = false;
        }

        userLocation = location;

    }

    private void setCameraPosition(LatLng location) {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(
                new LatLng(location.latitude,
                        location.longitude))
                .zoom(Utils.defaultZomLevel).build();
        gMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

    }

    private LatLng getLocationLatLng(boolean setText) {
        LatLng placeLocation = null;

        String isPickUpLoc = getIntent().getStringExtra("isPickUpLoc");

        if (isPickUpLoc != null && isPickUpLoc.equals("true")) {
            if (getIntent().hasExtra("PickUpLatitude") && getIntent().hasExtra("PickUpLongitude")) {

                isAddressEnable = true;
                placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLatitude")),
                        generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLongitude")));

            }

            if (setText && getIntent().hasExtra("PickUpAddress")) {
                pinImgView.setVisibility(View.VISIBLE);
                isPlaceSelected = true;
                placeTxtView.setText("" + getIntent().getStringExtra("PickUpAddress"));
            }

        } else if (getIntent().getStringExtra("isDestLoc") != null && getIntent().hasExtra("DestLatitude") && getIntent().hasExtra("DestLongitude")) {

            isAddressEnable = true;
            placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("DestLatitude")),
                    generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("DestLongitude")));

            if (setText && getIntent().hasExtra("DestAddress")) {
                pinImgView.setVisibility(View.VISIBLE);
                isPlaceSelected = true;
                placeTxtView.setText("" + getIntent().getStringExtra("DestAddress"));
            }

        } else if (userLocation != null) {
            placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, "" + userLocation.getLatitude()),
                    generalFunc.parseDoubleValue(0.0, "" + userLocation.getLongitude()));

        } else {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager = (LocationManager) getSystemService
                    (Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return placeLocation;
            }
            Location getLastLocation = locationManager.getLastKnownLocation
                    (LocationManager.PASSIVE_PROVIDER);
            if (getLastLocation != null) {
                LatLng UserLocation = new LatLng(generalFunc.parseDoubleValue(0.0, "" + getLastLocation.getLatitude()),
                        generalFunc.parseDoubleValue(0.0, "" + getLastLocation.getLongitude()));
                if (UserLocation != null) {
                    placeLocation = UserLocation;
                }
            }
        }


        return placeLocation;
    }

    private LatLng getCenterLocation() {
        if (gMap != null && gMap.getCameraPosition() != null) {
            return gMap.getCameraPosition().target;
        }
        return null;
    }

    @Override
    public void onCameraIdle() {


        if (getAddressFromLocation == null) {
            return;
        }

        if (pinImgView.getVisibility() == View.GONE) {
            return;
        }

        LatLng center = getCenterLocation();


        if (center == null) {
            return;
        }

//        setGoogleMapCameraListener(null);
        if (!isAddressEnable) {
            setGoogleMapCameraListener(null);
            getAddressFromLocation.setLocation(center.latitude, center.longitude);
            getAddressFromLocation.setLoaderEnable(true);
            getAddressFromLocation.execute();
        } else {
            isAddressEnable = false;
        }

    }


    public void setGoogleMapCameraListener(SearchPickupLocationActivity act) {
        listener=act;
        this.gMap.setOnCameraMoveStartedListener(act);
        this.gMap.setOnCameraIdleListener(act);

    }

    @Override
    public void onCameraMoveStarted(int i) {
        if (pinImgView.getVisibility() == View.VISIBLE) {
            if (!isAddressEnable) {
                placeTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SELECTING_LOCATION_TXT"));
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                Bundle bn = new Bundle();
                bn.putString("Latitude", data.getStringExtra("Latitude"));
                bn.putString("Longitude", "" + data.getStringExtra("Longitude"));
                bn.putString("Address", "" + data.getStringExtra("Address"));

                bn.putBoolean("isSkip", false);
                new StartActProcess(getActContext()).setOkResult(bn);
                finish();
            }
        } else if (requestCode == REQUEST_CHECK_SETTINGS) {
            switch (resultCode) {
                case RESULT_OK:
//                    startLocationUpdates();
                    break;
                case RESULT_CANCELED:
//                    settingsrequest();
                    break;
            }
        }
    }

    public Context getActContext() {
        return SearchPickupLocationActivity.this;
    }

    public class setOnClickAct implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(SearchPickupLocationActivity.this);

            if (i == R.id.backImgView) {
                SearchPickupLocationActivity.super.onBackPressed();

            } else if (i == locationImage.getId()) {
                (findViewById(R.id.pickUpLocSearchArea)).performClick();
            }else if (i == R.id.pickUpLocSearchArea) {

                try {
                    LatLngBounds bounds = null;

                    if (getIntent().hasExtra("PickUpLatitude") && getIntent().hasExtra("PickUpLongitude")) {

                        LatLng pickupPlaceLocation = new LatLng(generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLatitude")),
                                generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLongitude")));
                        bounds = new LatLngBounds(pickupPlaceLocation, pickupPlaceLocation);
                    }

                    Bundle bn = new Bundle();
                    bn.putString("locationArea", "dest");
                    bn.putDouble("lat", generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLatitude")));
                    bn.putDouble("long", generalFunc.parseDoubleValue(0.0, getIntent().getStringExtra("PickUpLongitude")));
                    new StartActProcess(getActContext()).startActForResult(SearchLocationActivity.class, bn,
                            Utils.PLACE_AUTOCOMPLETE_REQUEST_CODE);

                } catch (Exception e) {

                }
            } else if (i == btnId) {

                if (isPlaceSelected == false) {
                    generalFunc.showMessage(generalFunc.getCurrentView(SearchPickupLocationActivity.this),
                            generalFunc.retrieveLangLBl("Please set location.", "LBL_SET_LOCATION"));
                    return;
                }

                Bundle bn = new Bundle();
                bn.putString("Address", placeTxtView.getText().toString());
                bn.putString("Latitude", "" + placeLocation.latitude);
                bn.putString("Longitude", "" + placeLocation.longitude);

                new StartActProcess(getActContext()).setOkResult(bn);
                backImgView.performClick();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case GeneralFunctions.MY_PERMISSIONS_REQUEST: {

                boolean permission = generalFunc.isLocationPermissionGranted(true);
                if (permission) {
                    getLastLocation = new GetLocationUpdates(getActContext(), Utils.LOCATION_UPDATE_MIN_DISTANCE_IN_MITERS, true, this);
                }

                return;
            }
        }
    }

}
