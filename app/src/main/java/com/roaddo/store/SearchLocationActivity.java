package com.roaddo.store;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.adapter.files.PlacesAdapter;
import com.general.files.DividerItemDecoration;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.InternetConnection;
import com.general.files.MapDelegate;
import com.general.files.MapServiceApi;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.general.files.UpdateFrequentTask;
import com.utils.Utils;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchLocationActivity extends AppCompatActivity implements PlacesAdapter.setRecentLocClickList, MapDelegate {


    public boolean isAddressEnable;
    MTextView titleTxt;
    ImageView backImgView;
    GeneralFunctions generalFunc;
    JSONObject userProfileJsonObj;
    String whichLocation = "";
    MTextView cancelTxt;
    RecyclerView placesRecyclerView;
    EditText searchTxt;
    ArrayList<HashMap<String, String>> placelist;
    PlacesAdapter placesAdapter;
    ImageView imageCancel;
    MTextView noPlacedata;
    InternetConnection intCheck;
    ImageView googleimagearea;

    String session_token = "";
    int MIN_CHAR_REQ_GOOGLE_AUTO_COMPLETE = 2;
    String currentSearchQuery = "";
    UpdateFrequentTask sessionTokenFreqTask = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJsonObj = generalFunc.getJsonObject(generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        intCheck = new InternetConnection(getActContext());

        googleimagearea = (ImageView) findViewById(R.id.googleimagearea);
        cancelTxt = (MTextView) findViewById(R.id.cancelTxt);
        cancelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));

        placesRecyclerView = (RecyclerView) findViewById(R.id.placesRecyclerView);
        placesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Utils.hideKeyboard(getActContext());
            }
        });
        searchTxt = (EditText) findViewById(R.id.searchTxt);
        searchTxt.setHint(generalFunc.retrieveLangLBl("Search", "LBL_Search"));

        cancelTxt.setOnClickListener(new setOnClickList());
        imageCancel = (ImageView) findViewById(R.id.imageCancel);
        noPlacedata = (MTextView) findViewById(R.id.noPlacedata);
        imageCancel.setOnClickListener(new setOnClickList());

        placelist = new ArrayList<>();
        MIN_CHAR_REQ_GOOGLE_AUTO_COMPLETE = GeneralFunctions.parseIntegerValue(2, generalFunc.getJsonValueStr("MIN_CHAR_REQ_GOOGLE_AUTO_COMPLETE", userProfileJsonObj));

        setWhichLocationAreaSelected(getIntent().getStringExtra("locationArea"));


        searchTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // If it loses focus...
                if (!hasFocus) {
                    Utils.hideSoftKeyboard((Activity) getActContext(), searchTxt);
                } else {
                    Utils.showSoftKeyboard((Activity) getActContext(), searchTxt);
                }
            }
        });


        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (currentSearchQuery.equals(s.toString().trim())) {
                    return;
                }

                currentSearchQuery = searchTxt.getText().toString();

                if (s.length() >= MIN_CHAR_REQ_GOOGLE_AUTO_COMPLETE) {
                    if (session_token.trim().equalsIgnoreCase("")) {
                        session_token = Utils.userType + "_" + generalFunc.getMemberId() + "_" + System.currentTimeMillis();
                        initializeSessionRegeneration();
                    }

                    placesRecyclerView.setVisibility(View.VISIBLE);

                    getGooglePlaces(currentSearchQuery);
                } else {
                    placesRecyclerView.setVisibility(View.GONE);
                    noPlacedata.setVisibility(View.GONE);
                }

            }
        });

        searchTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {

                    getSearchGooglePlace(v.getText().toString());


                    return true;
                }
                return false;
            }
        });


        placesRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        placesRecyclerView.setLayoutManager(mLayoutManager);
        placesRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        placesRecyclerView.setItemAnimator(new DefaultItemAnimator());


    }

    public void getSearchGooglePlace(String input) {
        noPlacedata.setVisibility(View.GONE);

        String serverKey = generalFunc.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_COMPANY_APP_KEY);

        String url = null;
        //   URLEncoder.encode(input.replace(" ", "%20"), "UTF-8")
        try {


            String s = input.trim();
            String[] split = s.split("\\s+");


            /* url = "https://maps.googleapis.com/maps/api/place/queryautocomplete/json?input=" + *//*input.replace(" ", "%20")*//*URLEncoder.encode(input*//*.replace(" ", "%20")*//*, "utf8") + "&key=" + serverKey +
                    "&language=" + generalFunc.retrieveValue(CommonUtilities.GOOGLE_MAP_LANGUAGE_CODE_KEY) + "&sensor=true";
*/
            url = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=" + /*input.replace(" ", "%20")*/URLEncoder.encode(input/*.replace(" ", "%20")*/, "utf8") + "&key=" + serverKey + "&inputtype=" + "textquery" + "&fields=" + "photos,formatted_address,name,rating,geometry" +
                    "&language=" + generalFunc.retrieveValue(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY) + "&sensor=true";


            if (getIntent().getDoubleExtra("long", 0.0) != 0.0) {

                url = url + "&location=" + getIntent().getDoubleExtra("lat", 0.0) + "," + getIntent().getDoubleExtra("long", 0.0) + "&radius=20";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (url == null) {
            return;
        }
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), url, true);

        exeWebServer.setDataResponseListener(responseString -> {
            JSONObject responseObj = generalFunc.getJsonObject(responseString);
            if (generalFunc.getJsonValue("status", responseObj).equals("OK")) {
                JSONArray candidatesArr = generalFunc.getJsonArray("candidates", responseObj);

                if (searchTxt.getText().toString().length() == 0) {
                    placesRecyclerView.setVisibility(View.GONE);
                    noPlacedata.setVisibility(View.GONE);
                    googleimagearea.setVisibility(View.GONE);

                    return;
                }

                placelist.clear();
                for (int i = 0; i < candidatesArr.length(); i++) {
                    JSONObject item = generalFunc.getJsonObject(candidatesArr, i);

                    if (!generalFunc.getJsonValue("formatted_address", item).equals("")) {

                        HashMap<String, String> map = new HashMap<String, String>();

                        String structured_formatting = generalFunc.getJsonValueStr("structured_formatting", item);
                        /* map.put("main_text", generalFunc.getJsonValue("formatted_address", structured_formatting));*/

                        map.put("main_text", generalFunc.getJsonValueStr("formatted_address", item));
                        map.put("secondary_text", "");
                        map.put("place_id", "");
                        map.put("description", generalFunc.getJsonValueStr("name", item));

                        map.put("lat", generalFunc.getJsonValueStr("lat", generalFunc.getJsonObject("location", generalFunc.getJsonObject("geometry", item))));
                        map.put("lng", generalFunc.getJsonValueStr("lng", generalFunc.getJsonObject("location", generalFunc.getJsonObject("geometry", item))));


                        placelist.add(map);
                    }

                }

                if (placelist.size() > 0) {
                    placesRecyclerView.setVisibility(View.VISIBLE);
                    googleimagearea.setVisibility(View.VISIBLE);
                    noPlacedata.setVisibility(View.GONE);

                    if (placesAdapter == null) {
                        placesAdapter = new PlacesAdapter(getActContext(), placelist);
                        placesRecyclerView.setAdapter(placesAdapter);
                        placesAdapter.itemRecentLocClick(SearchLocationActivity.this);

                    } else {
                        placesAdapter.notifyDataSetChanged();
                    }
                }
            } else if (generalFunc.getJsonValue("status", responseObj).equals("ZERO_RESULTS")) {
                placelist.clear();
                if (placesAdapter != null) {
                    placesAdapter.notifyDataSetChanged();
                }

                String msg = generalFunc.retrieveLangLBl("We didn't find any places matched to your entered place. Please try again with another text.", "LBL_NO_PLACES_FOUND");
                noPlacedata.setText(msg);
                placesRecyclerView.setVisibility(View.GONE);
                googleimagearea.setVisibility(View.GONE);
                noPlacedata.setVisibility(View.VISIBLE);


            } else {
                placelist.clear();
                if (placesAdapter != null) {
                    placesAdapter.notifyDataSetChanged();
                }
                String msg = "";
                if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
                    msg = generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT");

                } else {
                    msg = generalFunc.retrieveLangLBl("Error occurred while searching nearest places. Please try again later.", "LBL_PLACE_SEARCH_ERROR");

                }

                noPlacedata.setText(msg);

                placesRecyclerView.setVisibility(View.GONE);
                noPlacedata.setVisibility(View.VISIBLE);

            }

        });
        exeWebServer.execute();
    }

    public void initializeSessionRegeneration() {

        if (sessionTokenFreqTask != null) {
            sessionTokenFreqTask.stopRepeatingTask();
        }
        sessionTokenFreqTask = new UpdateFrequentTask(170000);
        sessionTokenFreqTask.setTaskRunListener(() -> session_token = Utils.userType + "_" + generalFunc.getMemberId() + "_" + System.currentTimeMillis());

        sessionTokenFreqTask.startRepeatingTask();
    }

    @Override
    public void itemRecentLocClick(int position) {

        //  getSelectAddresLatLong(placelist.get(position).get("place_id"), placelist.get(position).get("description"));
       // getSelectAddresLatLong(placelist.get(position).get("place_id"), placelist.get(position).get("description"), placelist.get(position).get("session_token"), placelist.get(position).get("lat"), placelist.get(position).get("lng"));

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("Place_id", placelist.get(position).get("Place_id"));
        hashMap.put("description", placelist.get(position).get("description"));
        hashMap.put("session_token", placelist.get(position).get("session_token"));


        if (getIntent().getDoubleExtra("long", 0.0) != 0.0) {

            hashMap.put("latitude", placelist.get(position).get("lat"));
            hashMap.put("longitude", placelist.get(position).get("lng"));
        } else {
            hashMap.put("latitude", "");
            hashMap.put("longitude", "");
        }
        // hashMap.put("selectedPos", selectedPos + "");


        if (placelist.get(position).get("Place_id") == null || placelist.get(position).get("Place_id").equals("")) {
            resetOrAddDest(0, placelist.get(position).get("description"), GeneralFunctions.parseDoubleValue(0, placelist.get(position).get("latitude")), GeneralFunctions.parseDoubleValue(0, placelist.get(position).get("longitude")), "" + false);
        } else {
            MapServiceApi.getPlaceDetailsService(getActContext(), hashMap, this);
        }

    }

    public void setWhichLocationAreaSelected(String locationArea) {
        this.whichLocation = locationArea;
    }

    public Context getActContext() {
        return SearchLocationActivity.this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    public void getGooglePlaces(String input) {

        noPlacedata.setVisibility(View.GONE);


        String session_token = this.session_token;

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("input", input);

        if (getIntent().getDoubleExtra("long", 0.0) != 0.0) {

            hashMap.put("latitude", getIntent().getDoubleExtra("lat", 0.0) + "");
            hashMap.put("longitude", getIntent().getDoubleExtra("long", 0.0) + "");
        } else {
            hashMap.put("latitude", "");
            hashMap.put("longitude", "");
        }

        hashMap.put("session_token", session_token);

        MapServiceApi.getAutoCompleteService(getActContext(), hashMap, this);

//        try {
//
//            url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" + URLEncoder.encode(input, "UTF-8") + "&key=" + serverKey +
//                    "&language=" + generalFunc.retrieveValue(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY) +"&sensor=true&sessiontoken=" + session_token;
//
//            if (getIntent().getDoubleExtra("long", 0.0) != 0.0) {
//                url = url + "&location=" + getIntent().getDoubleExtra("lat", 0.0) + "," + getIntent().getDoubleExtra("long", 0.0) + "&radius=20";
//            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//        if (url == null) {
//            return;
//        }
//        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), url, true);
//
//        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
//            @Override
//            public void setResponse(String responseString) {
//                JSONObject responseObj=generalFunc.getJsonObject(responseString);
//                if (!currentSearchQuery.equalsIgnoreCase(input)) {
//                    return;
//                }
//                searchResult(responseObj);
//            }
//        });
//        exeWebServer.execute();
    }

    private void searchResult(JSONObject responseString) {
        if (generalFunc.getJsonValueStr("status", responseString).equals("OK")) {
            JSONArray predictionsArr = generalFunc.getJsonArray("predictions", responseString);

            placelist.clear();
            for (int i = 0; i < predictionsArr.length(); i++) {
                JSONObject item = generalFunc.getJsonObject(predictionsArr, i);

                if (!generalFunc.getJsonValue("place_id", item).equals("")) {

                    HashMap<String, String> map = new HashMap<String, String>();

                    String structured_formatting = generalFunc.getJsonValueStr("structured_formatting", item);
                    map.put("main_text", generalFunc.getJsonValue("main_text", structured_formatting));
                    map.put("secondary_text", generalFunc.getJsonValue("secondary_text", structured_formatting));
                    map.put("place_id", generalFunc.getJsonValueStr("place_id", item));
                    map.put("description", generalFunc.getJsonValueStr("description", item));
                    map.put("session_token", session_token);

                    placelist.add(map);

                }
            }
            if (placelist.size() > 0) {
                placesRecyclerView.setVisibility(View.VISIBLE);

                if (placesAdapter == null) {
                    placesAdapter = new PlacesAdapter(getActContext(), placelist);
                    placesRecyclerView.setAdapter(placesAdapter);
                    placesAdapter.itemRecentLocClick(SearchLocationActivity.this);

                } else {
                    placesAdapter.notifyDataSetChanged();
                }
            }
        } else if (generalFunc.getJsonValue("status", responseString).equals("ZERO_RESULTS")) {
            placelist.clear();
            if (placesAdapter != null) {
                placesAdapter.notifyDataSetChanged();
            }

            String msg = generalFunc.retrieveLangLBl("We didn't find any places matched to your entered place. Please try again with another text.", "LBL_NO_PLACES_FOUND");
            noPlacedata.setText(msg);
            placesRecyclerView.setVisibility(View.VISIBLE);

            noPlacedata.setVisibility(View.VISIBLE);

        } else {
            placelist.clear();
            if (placesAdapter != null) {
                placesAdapter.notifyDataSetChanged();
            }
            String msg = "";
            if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
                msg = generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT");

            } else {
                msg = generalFunc.retrieveLangLBl("Error occurred while searching nearest places. Please try again later.", "LBL_PLACE_SEARCH_ERROR");

            }

            noPlacedata.setText(msg);
            placesRecyclerView.setVisibility(View.VISIBLE);

            noPlacedata.setVisibility(View.VISIBLE);
        }

    }

    public void getSelectAddresLatLong(String Place_id, final String address, String session_token, String lat, String lng) {


        if (lat == null || lat.equalsIgnoreCase("") || lng
                == null || lng.equalsIgnoreCase("")) {
            String serverKey = generalFunc.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_COMPANY_APP_KEY);


            String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + Place_id + "&key=" + serverKey +
                    "&language=" + generalFunc.retrieveValue(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY) + "&sensor=true&fields=formatted_address,name,geometry&sessiontoken=" + session_token;

            ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), url, true);
            exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
            exeWebServer.setDataResponseListener(responseString -> {
                JSONObject responseObj = generalFunc.getJsonObject(responseString);
                if (generalFunc.getJsonValue("status", responseObj).equals("OK")) {
                    String resultObj = generalFunc.getJsonValueStr("result", responseObj);
                    String geometryObj = generalFunc.getJsonValue("geometry", resultObj);
                    String locationObj = generalFunc.getJsonValue("location", geometryObj);
                    String latitude = generalFunc.getJsonValue("lat", locationObj);
                    String longitude = generalFunc.getJsonValue("lng", locationObj);

                    Bundle bn = new Bundle();
                    bn.putString("Address", address);
                    bn.putString("Latitude", "" + latitude);
                    bn.putString("Longitude", "" + longitude);

                    new StartActProcess(getActContext()).setOkResult(bn);

                    finish();


                }


            });
            exeWebServer.execute();
        } else if (!lat.equals("") && !lng.equalsIgnoreCase("")) {
            Bundle bn = new Bundle();
            bn.putString("Address", address);
            bn.putString("Latitude", "" + lat);
            bn.putString("Longitude", "" + lng);
            bn.putBoolean("isSkip", false);
            new StartActProcess(getActContext()).setOkResult(bn);
            finish();

        }

    }

    @Override
    public void searchResult(ArrayList<HashMap<String, String>> placelist, int selectedPos, String input) {
        this.placelist.clear();
        this.placelist.addAll(placelist);
        imageCancel.setVisibility(View.VISIBLE);


        if (currentSearchQuery.length() == 0) {
            placesRecyclerView.setVisibility(View.GONE);
            noPlacedata.setVisibility(View.GONE);

            return;
        }


        if (placelist.size() > 0) {
            placesRecyclerView.setVisibility(View.VISIBLE);
            if (placesAdapter == null) {
                placesAdapter = new PlacesAdapter(getActContext(), this.placelist);
                placesRecyclerView.setAdapter(placesAdapter);
                placesAdapter.itemRecentLocClick(SearchLocationActivity.this);

            } else {
                placesAdapter.notifyDataSetChanged();
            }
        } else if (currentSearchQuery.length() == 0) {
            placelist.clear();
            if (placesAdapter != null) {
                placesAdapter.notifyDataSetChanged();
            }

            String msg = generalFunc.retrieveLangLBl("We didn't find any places matched to your entered place. Please try again with another text.", "LBL_NO_PLACES_FOUND");
            noPlacedata.setText(msg);
            placesRecyclerView.setVisibility(View.VISIBLE);

            noPlacedata.setVisibility(View.VISIBLE);

            return;
        } else {

            placelist.clear();
            if (placesAdapter != null) {
                placesAdapter.notifyDataSetChanged();
            }
            String msg = "";
            if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
                msg = generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT");

            } else {
                msg = generalFunc.retrieveLangLBl("Error occurred while searching nearest places. Please try again later.", "LBL_PLACE_SEARCH_ERROR");

            }

            noPlacedata.setText(msg);
            placesRecyclerView.setVisibility(View.VISIBLE);
            noPlacedata.setVisibility(View.VISIBLE);

            //} else if (generalFunc.getJsonValue("status", responseString).equals("ZERO_RESULTS")) {
        }


    }

    @Override
    public void resetOrAddDest(int selPos, String address, double latitude, double longitude, String isSkip) {
        Bundle bn = new Bundle();
        bn.putString("Address", address);
        bn.putString("Latitude", "" + latitude);
        bn.putString("Longitude", "" + longitude);
        if (Utils.checkText(isSkip)) {
            bn.putBoolean("isSkip", isSkip.equalsIgnoreCase("true") ? true : false);
        }

        Utils.hideKeyboard(this);

        new StartActProcess(getActContext()).setOkResult(bn);


        finish();
    }

    @Override
    public void directionResult(HashMap<String, String> directionlist) {

    }

    @Override
    public void geoCodeAddressFound(String address, double latitude, double longitude, String geocodeobject) {

    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.cancelTxt) {
                finish();

            } else if (i == R.id.imageCancel) {
                placesRecyclerView.setVisibility(View.GONE);
                searchTxt.setText("");
                noPlacedata.setVisibility(View.GONE);
            }

        }
    }

    @Override
    protected void onDestroy() {
        if (sessionTokenFreqTask != null) {
            sessionTokenFreqTask.stopRepeatingTask();
        }
        super.onDestroy();
    }
}
